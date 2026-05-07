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

class MarkerManager(
    private val mapView: MapView,
    private val iconFactory: MarkerIconFactory,
    private val onUserTap: (String, String, String?, Double, Double, Boolean) -> Unit,
    private val onEventTap: (String) -> Unit
) {

    private val userPlacemarks = mutableMapOf<String, PlacemarkMapObject>()
    private val eventPlacemarks = mutableMapOf<String, PlacemarkMapObject>()
    private val iconCache = mutableMapOf<String, ImageProvider>()

    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val moveJobs = mutableMapOf<String, Job>()

    // ✅ Кластерная коллекция — все маркеры добавляются в неё
    private val clusterCollection: ClusterizedPlacemarkCollection =
        mapView.map.mapObjects.addClusterizedPlacemarkCollection { cluster ->
            cluster.appearance.setIcon(createClusterBitmap(cluster.size))
        }

    private var lastClusterUpdate = 0L

    private fun createClusterBitmap(count: Int): ImageProvider {
        val size = 80
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.rgb(33, 150, 243); style = Paint.Style.FILL }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4, paint)
        paint.apply { color = AndroidColor.WHITE; style = Paint.Style.STROKE; strokeWidth = 4f }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4, paint)
        paint.apply { color = AndroidColor.WHITE; style = Paint.Style.FILL; textSize = 32f; textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD }
        canvas.drawText(count.toString(), size / 2f, size / 2f + 12f, paint)
        return ImageProvider.fromBitmap(bitmap)
    }

    private fun isValid(): Boolean = mapView.context != null

    // ==================== USERS ====================

    fun updateUsers(users: List<UserLocationData>, currentUserId: String) {
        if (!isValid()) return

        val newIds = users.map { it.userId }.toSet() + "current"
        userPlacemarks.keys.filter { it !in newIds }.forEach { id ->
            userPlacemarks[id]?.let { clusterCollection.remove(it) }
            userPlacemarks.remove(id)
        }

        users.find { it.userId == currentUserId }?.let { current ->
            updateOrCreateUser("current", current, true)
        }
        users.filter { it.userId != currentUserId }.forEach { user ->
            updateOrCreateUser(user.userId, user, false)
        }

        val now = System.currentTimeMillis()
        if (now - lastClusterUpdate > 3000) {
            clusterCollection.clusterPlacemarks(60.0, 15)
            lastClusterUpdate = now
        }
    }

    private fun updateOrCreateUser(id: String, user: UserLocationData, isCurrent: Boolean) {
        if (!isValid()) return
        val point = Point(user.latitude, user.longitude)
        val existing = userPlacemarks[id]

        scope.launch {
            if (!isActive || !isValid()) return@launch

            // ✅ Ключ кэша включает avatarUrl — обновляется при смене аватара
            val cacheKey = "${user.userId}_${user.avatarUrl}_$isCurrent"
            val icon = iconCache[cacheKey] ?: iconFactory.getUserIcon(user.userId, user.name, user.avatarUrl, isCurrent).also { iconCache[cacheKey] = it }

            if (existing == null) {
                // ✅ Маркер добавляется в clusterCollection
                val placemark = clusterCollection.addPlacemark(point, icon) ?: return@launch
                placemark.isDraggable = false
                placemark.setZIndex(if (isCurrent) 10f else 5f)
                placemark.addTapListener(MapObjectTapListener { _, _ ->
                    if (isValid()) onUserTap(user.userId, user.name, user.avatarUrl, user.latitude, user.longitude, user.isOnline)
                    true
                })
                userPlacemarks[id] = placemark
            } else {
                smoothMove(existing, point)
            }
        }
    }

    // ==================== EVENTS ====================

    fun updateEvents(events: List<EventMarkerData>) {
        if (!isValid()) return
        val newIds = events.map { it.id }.toSet()
        eventPlacemarks.keys.filter { it !in newIds }.forEach { id ->
            eventPlacemarks[id]?.let { clusterCollection.remove(it) }
            eventPlacemarks.remove(id)
        }
        events.forEach { event ->
            if (event.id !in eventPlacemarks) {
                val icon = iconFactory.getEventIcon(event.type)
                val point = Point(event.latitude, event.longitude)
                // ✅ Маркер добавляется в clusterCollection
                val placemark = clusterCollection.addPlacemark(point, icon) ?: return@forEach
                placemark.isDraggable = false
                placemark.setZIndex(3f)
                placemark.addTapListener(MapObjectTapListener { _, _ ->
                    if (isValid()) onEventTap(event.id)
                    true
                })
                eventPlacemarks[event.id] = placemark
            }
        }
    }

    // ==================== SMOOTH MOVE ====================

    private fun smoothMove(placemark: PlacemarkMapObject, target: Point) {
        if (!isValid()) return
        val start = placemark.geometry
        if (Math.abs(start.latitude - target.latitude) < 0.000001 && Math.abs(start.longitude - target.longitude) < 0.000001) return
        val jobKey = placemark.hashCode().toString()
        moveJobs[jobKey]?.cancel()
        moveJobs[jobKey] = scope.launch {
            if (!isActive || !isValid()) return@launch
            val duration = 250L; val steps = 20
            val dLat = (target.latitude - start.latitude) / steps; val dLon = (target.longitude - start.longitude) / steps
            repeat(steps) {
                // ✅ Проверка на каждом шаге анимации
                if (!isActive || !isValid()) return@launch
                placemark.geometry = Point(placemark.geometry.latitude + dLat, placemark.geometry.longitude + dLon)
                delay(duration / steps)
            }
            if (isActive && isValid()) placemark.geometry = target
            moveJobs.remove(jobKey)
        }
    }

    // ==================== CAMERA ====================

    fun setInitialCamera(lat: Double, lon: Double) {
        if (!isValid()) return
        mapView.map.move(CameraPosition(Point(lat, lon), 14.0f, 0.0f, 0.0f), Animation(Animation.Type.SMOOTH, 1.5f), null)
    }

    // ==================== LIFECYCLE ====================

    fun clear() {
        moveJobs.values.forEach { it.cancel() }
        moveJobs.clear()
        iconCache.clear()
        userPlacemarks.values.forEach { clusterCollection.remove(it) }
        eventPlacemarks.values.forEach { clusterCollection.remove(it) }
        userPlacemarks.clear()
        eventPlacemarks.clear()
        clusterCollection.clear()
    }

    fun destroy() {
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