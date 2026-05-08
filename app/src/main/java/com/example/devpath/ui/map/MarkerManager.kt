package com.example.devpath.ui.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.Color as AndroidColor
import com.yandex.mapkit.Animation
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.*
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import kotlinx.coroutines.*
import kotlin.math.abs

class MarkerManager(
    private val mapView: MapView,
    private val iconFactory: MarkerIconFactory,
    private val onUserTap: (String, String, String?, Double, Double, Boolean) -> Unit,
    private val onEventTap: (String) -> Unit
) {

    // ✅ СИЛЬНЫЕ ССЫЛКИ — предотвращают сборку мусора нативных объектов
    private val strongReferences = ArrayList<Any>(200)

    // Данные пользователей и событий (сохраняем отдельно от маркеров)
    private val userDataCache = mutableMapOf<String, UserLocationData>()
    private val eventDataCache = mutableMapOf<String, EventMarkerData>()

    // Маркеры на карте
    private val userPlacemarks = mutableMapOf<String, PlacemarkMapObject>()
    private val eventPlacemarks = mutableMapOf<String, PlacemarkMapObject>()
    private val iconCache = mutableMapOf<String, ImageProvider>()

    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val moveJobs = mutableMapOf<String, Job>()

    // Сборщик мусора
    private var gcJob: Job? = null

    // Коллекция объектов карты
    private val mapObjects: MapObjectCollection = mapView.map.mapObjects

    // Кластеры
    private data class ClusterInfo(
        val centerLat: Double,
        val centerLon: Double,
        val count: Int,
        val placemark: PlacemarkMapObject
    )

    private val activeClusters = mutableListOf<ClusterInfo>()
    private var isClustering = false
    private var lastZoomLevel = 0f

    private fun isValid(): Boolean {
        return try {
            mapView.context != null && mapView.map.isValid
        } catch (e: Exception) {
            false
        }
    }

    // ==================== СБОРЩИК МУСОРА ====================

    private fun startGC() {
        gcJob?.cancel()
        gcJob = scope.launch {
            while (isActive) {
                delay(5_000) // Каждые 5 секунд
                if (!isValid()) continue

                // Проверяем и восстанавливаем маркеры
                val invalidUsers = userPlacemarks.filter { !it.value.isValid }
                invalidUsers.forEach { (id, placemark) ->
                    try { mapObjects.remove(placemark) } catch (_: Exception) {}
                    strongReferences.remove(placemark)
                    userPlacemarks.remove(id)
                    // Восстанавливаем маркер
                    userDataCache[id]?.let { data ->
                        scope.launch { restoreUserMarker(id, data) }
                    }
                }

                val invalidEvents = eventPlacemarks.filter { !it.value.isValid }
                invalidEvents.forEach { (id, placemark) ->
                    try { mapObjects.remove(placemark) } catch (_: Exception) {}
                    strongReferences.remove(placemark)
                    eventPlacemarks.remove(id)
                    // Восстанавливаем маркер
                    eventDataCache[id]?.let { data ->
                        restoreEventMarker(data)
                    }
                }
            }
        }
    }

    private suspend fun restoreUserMarker(id: String, data: UserLocationData) {
        if (!isValid()) return

        val isCurrent = id == "current" || data.userId == "current"
        val cacheKey = "${data.userId}_${data.avatarUrl}_$isCurrent"
        val icon = iconCache[cacheKey] ?: iconFactory.getUserIcon(
            data.userId, data.name, data.avatarUrl, isCurrent
        ).also { iconCache[cacheKey] = it }

        val point = Point(data.latitude, data.longitude)
        val placemark = mapObjects.addPlacemark(point, icon) ?: return

        placemark.isDraggable = false
        placemark.setZIndex(if (isCurrent) 10f else 5f)

        // ✅ Сильные ссылки
        strongReferences.add(placemark)

        val tapListener = MapObjectTapListener { _, _ ->
            if (isValid()) {
                onUserTap(data.userId, data.name, data.avatarUrl, data.latitude, data.longitude, data.isOnline)
            }
            true
        }
        strongReferences.add(tapListener) // ✅ Сохраняем листенер
        placemark.addTapListener(tapListener)

        userPlacemarks[id] = placemark
    }

    private fun restoreEventMarker(data: EventMarkerData) {
        if (!isValid()) return

        val icon = iconFactory.getEventIcon(data.type)
        val point = Point(data.latitude, data.longitude)
        val placemark = mapObjects.addPlacemark(point, icon) ?: return

        placemark.isDraggable = false
        placemark.setZIndex(3f)

        // ✅ Сильные ссылки
        strongReferences.add(placemark)

        val tapListener = MapObjectTapListener { _, _ ->
            if (isValid()) {
                onEventTap(data.id)
            }
            true
        }
        strongReferences.add(tapListener) // ✅ Сохраняем листенер
        placemark.addTapListener(tapListener)

        eventPlacemarks[data.id] = placemark
    }

    // ==================== КЛАСТЕРИЗАЦИЯ ====================

    private fun updateClustering(zoomLevel: Float) {
        if (isClustering || !isValid()) return
        if (abs(zoomLevel - lastZoomLevel) < 0.3f) return

        isClustering = true
        lastZoomLevel = zoomLevel

        try {
            // Удаляем старые кластеры
            activeClusters.forEach { cluster ->
                try { mapObjects.remove(cluster.placemark) } catch (_: Exception) {}
                strongReferences.remove(cluster.placemark)
            }
            activeClusters.clear()

            if (zoomLevel < 13f) {
                createClusters(zoomLevel)
            } else {
                showAllMarkers()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isClustering = false
        }
    }

    private fun createClusters(zoomLevel: Float) {
        if (!isValid()) return

        // Собираем все точки
        val allPoints = mutableListOf<Triple<String, Double, Double>>()

        userPlacemarks.forEach { (id, placemark) ->
            if (placemark.isValid) {
                allPoints.add(Triple(id, placemark.geometry.latitude, placemark.geometry.longitude))
            }
        }

        eventPlacemarks.forEach { (id, placemark) ->
            if (placemark.isValid) {
                allPoints.add(Triple(id, placemark.geometry.latitude, placemark.geometry.longitude))
            }
        }

        if (allPoints.isEmpty()) {
            showAllMarkers()
            return
        }

        // Размер сетки зависит от зума
        val gridSize = when {
            zoomLevel < 8 -> 0.5
            zoomLevel < 10 -> 0.1
            zoomLevel < 12 -> 0.05
            else -> 0.01
        }

        // Группируем по сетке
        val clusters = mutableMapOf<Pair<Int, Int>, MutableList<String>>()

        allPoints.forEach { (id, lat, lon) ->
            val gridX = (lat / gridSize).toInt()
            val gridY = (lon / gridSize).toInt()
            clusters.getOrPut(gridX to gridY) { mutableListOf() }.add(id)
        }

        // Скрываем все оригинальные маркеры
        userPlacemarks.values.forEach {
            try { it.isVisible = false } catch (_: Exception) {}
        }
        eventPlacemarks.values.forEach {
            try { it.isVisible = false } catch (_: Exception) {}
        }

        // Создаем кластеры
        clusters.forEach { (_, ids) ->
            if (ids.size == 1) {
                // Одиночный маркер — показываем
                userPlacemarks[ids[0]]?.let {
                    try { it.isVisible = true } catch (_: Exception) {}
                }
                eventPlacemarks[ids[0]]?.let {
                    try { it.isVisible = true } catch (_: Exception) {}
                }
            } else if (ids.size > 1) {
                // Вычисляем центр кластера
                var sumLat = 0.0
                var sumLon = 0.0
                var count = 0

                ids.forEach { id ->
                    userPlacemarks[id]?.let {
                        sumLat += it.geometry.latitude
                        sumLon += it.geometry.longitude
                        count++
                    }
                    eventPlacemarks[id]?.let {
                        sumLat += it.geometry.latitude
                        sumLon += it.geometry.longitude
                        count++
                    }
                }

                if (count > 0) {
                    val centerLat = sumLat / count
                    val centerLon = sumLon / count

                    val clusterIcon = createClusterBitmap(ids.size)
                    val clusterPoint = Point(centerLat, centerLon)

                    val clusterPlacemark = mapObjects.addPlacemark(clusterPoint, clusterIcon)
                    if (clusterPlacemark != null) {
                        clusterPlacemark.setZIndex(20f)

                        // ✅ Сильные ссылки
                        strongReferences.add(clusterPlacemark)

                        val tapListener = MapObjectTapListener { _, _ ->
                            if (isValid()) {
                                mapView.map.move(
                                    CameraPosition(clusterPoint, 15f, 0f, 0f),
                                    Animation(Animation.Type.SMOOTH, 0.5f),
                                    null
                                )
                            }
                            true
                        }
                        strongReferences.add(tapListener) // ✅ Сохраняем листенер
                        clusterPlacemark.addTapListener(tapListener)

                        activeClusters.add(ClusterInfo(centerLat, centerLon, ids.size, clusterPlacemark))
                    }
                }
            }
        }
    }

    private fun showAllMarkers() {
        // Удаляем кластеры
        activeClusters.forEach { cluster ->
            try { mapObjects.remove(cluster.placemark) } catch (_: Exception) {}
            strongReferences.remove(cluster.placemark)
        }
        activeClusters.clear()

        // Показываем все оригинальные маркеры
        userPlacemarks.values.forEach {
            try { it.isVisible = true } catch (_: Exception) {}
        }
        eventPlacemarks.values.forEach {
            try { it.isVisible = true } catch (_: Exception) {}
        }
    }

    private fun createClusterBitmap(count: Int): ImageProvider {
        val size = 80
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Цвет кластера
        val clusterColor = when {
            count < 10 -> AndroidColor.rgb(33, 150, 243)
            count < 25 -> AndroidColor.rgb(255, 152, 0)
            else -> AndroidColor.rgb(244, 67, 54)
        }

        // Фон
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = clusterColor
            style = Paint.Style.FILL
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4, paint)

        // Обводка
        paint.apply {
            color = AndroidColor.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4, paint)

        // Текст
        paint.apply {
            color = AndroidColor.WHITE
            style = Paint.Style.FILL
            textSize = when {
                count < 10 -> 36f
                count < 100 -> 30f
                else -> 24f
            }
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }

        val displayText = if (count > 99) "99+" else count.toString()
        canvas.drawText(displayText, size / 2f, size / 2f + 12f, paint)

        return ImageProvider.fromBitmap(bitmap)
    }

    // ==================== USERS ====================

    fun updateUsers(users: List<UserLocationData>, currentUserId: String) {
        if (!isValid()) return

        // Сохраняем данные
        users.forEach { userDataCache[it.userId] = it }

        val newIds = users.map { it.userId }.toSet() + "current"

        // Удаляем старые маркеры
        userPlacemarks.keys.filter { it !in newIds }.forEach { id ->
            userPlacemarks[id]?.let {
                try { mapObjects.remove(it) } catch (_: Exception) {}
                strongReferences.remove(it)
            }
            userPlacemarks.remove(id)
            userDataCache.remove(id)
        }

        // Обновляем текущего пользователя
        users.find { it.userId == currentUserId }?.let { current ->
            userDataCache["current"] = current
            updateOrCreateUser("current", current, true)
        }

        // Обновляем других пользователей
        users.filter { it.userId != currentUserId }.forEach { user ->
            updateOrCreateUser(user.userId, user, false)
        }
    }

    private fun updateOrCreateUser(id: String, user: UserLocationData, isCurrent: Boolean) {
        if (!isValid()) return

        val point = Point(user.latitude, user.longitude)
        val existing = userPlacemarks[id]

        // Проверяем валидность существующего маркера
        if (existing != null && !existing.isValid) {
            try { mapObjects.remove(existing) } catch (_: Exception) {}
            strongReferences.remove(existing)
            userPlacemarks.remove(id)
        }

        scope.launch {
            if (!isActive || !isValid()) return@launch

            val cacheKey = "${user.userId}_${user.avatarUrl}_$isCurrent"
            val icon = iconCache[cacheKey] ?: iconFactory.getUserIcon(
                user.userId, user.name, user.avatarUrl, isCurrent
            ).also { iconCache[cacheKey] = it }

            val currentPlacemark = userPlacemarks[id]

            if (currentPlacemark == null || !currentPlacemark.isValid) {
                // Создаем новый маркер
                val placemark = mapObjects.addPlacemark(point, icon) ?: return@launch
                placemark.isDraggable = false
                placemark.setZIndex(if (isCurrent) 10f else 5f)

                // ✅ Сильные ссылки
                strongReferences.add(placemark)

                val tapListener = MapObjectTapListener { _, _ ->
                    if (isValid()) {
                        onUserTap(user.userId, user.name, user.avatarUrl, user.latitude, user.longitude, user.isOnline)
                    }
                    true
                }
                strongReferences.add(tapListener) // ✅ Сохраняем листенер
                placemark.addTapListener(tapListener)

                userPlacemarks[id] = placemark
            } else {
                // Плавно перемещаем
                smoothMove(currentPlacemark, point)
            }
        }
    }

    // ==================== EVENTS ====================

    fun updateEvents(events: List<EventMarkerData>) {
        if (!isValid()) return

        // Сохраняем данные
        events.forEach { eventDataCache[it.id] = it }

        val newIds = events.map { it.id }.toSet()

        // Удаляем старые
        eventPlacemarks.keys.filter { it !in newIds }.forEach { id ->
            eventPlacemarks[id]?.let {
                try { mapObjects.remove(it) } catch (_: Exception) {}
                strongReferences.remove(it)
            }
            eventPlacemarks.remove(id)
            eventDataCache.remove(id)
        }

        // Добавляем/обновляем
        events.forEach { event ->
            val existing = eventPlacemarks[event.id]

            if (existing != null && !existing.isValid) {
                try { mapObjects.remove(existing) } catch (_: Exception) {}
                strongReferences.remove(existing)
                eventPlacemarks.remove(event.id)
            }

            if (event.id !in eventPlacemarks) {
                val icon = iconFactory.getEventIcon(event.type)
                val point = Point(event.latitude, event.longitude)
                val placemark = mapObjects.addPlacemark(point, icon) ?: return@forEach

                placemark.isDraggable = false
                placemark.setZIndex(3f)

                // ✅ Сильные ссылки
                strongReferences.add(placemark)

                val tapListener = MapObjectTapListener { _, _ ->
                    if (isValid()) {
                        onEventTap(event.id)
                    }
                    true
                }
                strongReferences.add(tapListener) // ✅ Сохраняем листенер
                placemark.addTapListener(tapListener)

                eventPlacemarks[event.id] = placemark
            }
        }

        // Обновляем кластеризацию
        val zoom = try { mapView.map.cameraPosition.zoom } catch (_: Exception) { 14f }
        updateClustering(zoom)
    }

    // ==================== SMOOTH MOVE ====================

    private fun smoothMove(placemark: PlacemarkMapObject, target: Point) {
        if (!isValid()) return

        val start = placemark.geometry

        if (abs(start.latitude - target.latitude) < 0.000001 &&
            abs(start.longitude - target.longitude) < 0.000001) return

        val jobKey = placemark.hashCode().toString()
        moveJobs[jobKey]?.cancel()
        moveJobs[jobKey] = scope.launch {
            if (!isActive || !isValid()) return@launch

            val steps = 15
            val dLat = (target.latitude - start.latitude) / steps
            val dLon = (target.longitude - start.longitude) / steps

            for (i in 1..steps) {
                if (!isActive || !isValid()) {
                    // Находим ID маркера и пересоздаём
                    val id = userPlacemarks.entries.find { it.value == placemark }?.key
                    if (id != null) {
                        userPlacemarks.remove(id)
                        strongReferences.remove(placemark)
                        userDataCache[id]?.let { restoreUserMarker(id, it) }
                    }
                    return@launch
                }
                try {
                    placemark.geometry = Point(
                        start.latitude + dLat * i,
                        start.longitude + dLon * i
                    )
                } catch (_: Exception) {
                    // Маркер стал невалидным — пересоздаём
                    val id = userPlacemarks.entries.find { it.value == placemark }?.key
                    if (id != null) {
                        userPlacemarks.remove(id)
                        strongReferences.remove(placemark)
                        userDataCache[id]?.let { restoreUserMarker(id, it) }
                    }
                    return@launch
                }
                delay(16)
            }

            moveJobs.remove(jobKey)
        }
    }

    // ==================== CAMERA ====================

    fun setInitialCamera(lat: Double, lon: Double) {
        if (!isValid()) return
        mapView.map.move(
            CameraPosition(Point(lat, lon), 15f, 0f, 0f),
            Animation(Animation.Type.SMOOTH, 1.5f),
            null
        )
    }

    // ==================== СЛУШАТЕЛЬ ЗУМА ====================

    private val cameraListener = object : CameraListener {
        override fun onCameraPositionChanged(
            map: com.yandex.mapkit.map.Map,
            cameraPosition: CameraPosition,
            cameraUpdateReason: CameraUpdateReason,
            finished: Boolean
        ) {
            if (finished) {
                updateClustering(cameraPosition.zoom)
            }
        }
    }

    init {
        mapView.map.addCameraListener(cameraListener)
        startGC()
    }

    // ==================== LIFECYCLE ====================

    fun clear() {
        moveJobs.values.forEach { it.cancel() }
        moveJobs.clear()
        gcJob?.cancel()
        iconCache.clear()

        userPlacemarks.values.forEach {
            try { mapObjects.remove(it) } catch (_: Exception) {}
        }
        eventPlacemarks.values.forEach {
            try { mapObjects.remove(it) } catch (_: Exception) {}
        }
        activeClusters.forEach {
            try { mapObjects.remove(it.placemark) } catch (_: Exception) {}
        }

        userPlacemarks.clear()
        eventPlacemarks.clear()
        activeClusters.clear()
        userDataCache.clear()
        eventDataCache.clear()
        strongReferences.clear() // ✅ Очищаем сильные ссылки
    }

    fun destroy() {
        try {
            mapView.map.removeCameraListener(cameraListener)
        } catch (_: Exception) {}
        clear()
        scope.cancel()
    }
}

data class UserLocationData(
    val userId: String,
    val name: String,
    val avatarUrl: String?,
    val latitude: Double,
    val longitude: Double,
    val isOnline: Boolean
)

data class EventMarkerData(
    val id: String,
    val type: String,
    val title: String,
    val latitude: Double,
    val longitude: Double
)