package com.example.devpath.ui

import android.Manifest
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.graphics.Color as AndroidColor
import android.location.Geocoder
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.devpath.data.repository.LocationSettings
import com.example.devpath.data.repository.UserLocation
import com.example.devpath.domain.models.MapMarker
import com.example.devpath.domain.models.MarkerType
import com.example.devpath.ui.components.UserAvatar
import com.example.devpath.ui.map.MarkerIconFactory
import com.example.devpath.ui.map.MarkerManager
import com.example.devpath.ui.map.UserLocationData
import com.example.devpath.ui.map.EventMarkerData
import com.example.devpath.ui.viewmodel.MapViewModel
import com.google.accompanist.permissions.*
import com.yandex.mapkit.Animation
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.mapview.MapView
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext
import java.util.Locale

// ============================================================================
// 🎨 ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ ОТРИСОВКИ МАРКЕРОВ ПОЛЬЗОВАТЕЛЕЙ
// ============================================================================

private fun drawUserName(
    canvas: Canvas, name: String, totalWidth: Int, avatarSize: Int, avatarY: Float
) {
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = AndroidColor.BLACK; textSize = 30f; typeface = Typeface.defaultFromStyle(Typeface.BOLD); textAlign = Paint.Align.CENTER; setShadowLayer(4f, 2f, 2f, AndroidColor.WHITE)
    }
    val textY = avatarY + avatarSize + 40f
    val displayName = if (name.length > 18) name.take(16) + "…" else name
    canvas.drawText(displayName, totalWidth / 2f, textY, textPaint)
}

private fun drawPointer(canvas: Canvas, totalWidth: Int, totalHeight: Int, markerColor: Int) {
    val pointerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = markerColor; style = Paint.Style.FILL }
    val pointerPath = Path().apply { moveTo((totalWidth / 2 - 18).toFloat(), (totalHeight - 35).toFloat()); lineTo((totalWidth / 2 + 18).toFloat(), (totalHeight - 35).toFloat()); lineTo((totalWidth / 2).toFloat(), (totalHeight - 12).toFloat()); close() }
    canvas.drawPath(pointerPath, pointerPaint)
    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = AndroidColor.WHITE; style = Paint.Style.STROKE; strokeWidth = 4f }
    canvas.drawPath(pointerPath, strokePaint)
}

private fun drawFallbackMarker(canvas: Canvas, name: String, markerColor: Int, totalWidth: Int, totalHeight: Int, avatarSize: Int) {
    val centerX = totalWidth / 2f; val avatarY = 22f; val radius = avatarSize / 2f
    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = markerColor; style = Paint.Style.FILL }
    canvas.drawCircle(centerX, avatarY + radius, radius + 6, bgPaint)
    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = AndroidColor.WHITE; style = Paint.Style.STROKE; strokeWidth = 5f }
    canvas.drawCircle(centerX, avatarY + radius, radius + 6, strokePaint)
    val letterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = AndroidColor.WHITE; textSize = 60f; typeface = Typeface.defaultFromStyle(Typeface.BOLD); textAlign = Paint.Align.CENTER }
    val initial = name.take(1).uppercase(); val textY = avatarY + radius - (letterPaint.descent() + letterPaint.ascent()) / 2
    canvas.drawText(initial, centerX, textY, letterPaint)
    drawUserName(canvas, name, totalWidth, avatarSize, avatarY)
    drawPointer(canvas, totalWidth, totalHeight, markerColor)
}

private fun drawMarkerContent(canvas: Canvas, avatarBitmap: Bitmap, name: String, markerColor: Int, totalWidth: Int, totalHeight: Int, avatarSize: Int) {
    val centerX = totalWidth / 2f; val avatarY = 22f; val radius = avatarSize / 2f
    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = markerColor; style = Paint.Style.FILL }
    canvas.drawCircle(centerX, avatarY + radius, radius + 6, bgPaint)
    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = AndroidColor.WHITE; style = Paint.Style.STROKE; strokeWidth = 5f }
    canvas.drawCircle(centerX, avatarY + radius, radius + 6, strokePaint)
    canvas.save()
    val clipPath = Path().apply { addCircle(centerX, avatarY + radius, radius, Path.Direction.CW) }
    canvas.clipPath(clipPath)
    canvas.drawBitmap(avatarBitmap, centerX - radius - 2, avatarY - 2, null)
    canvas.restore()
    drawUserName(canvas, name, totalWidth, avatarSize, avatarY)
    drawPointer(canvas, totalWidth, totalHeight, markerColor)
}

// ============================================================================
// 🌍 ФУНКЦИЯ ОБРАТНОГО ГЕОКОДИРОВАНИЯ
// ============================================================================

private suspend fun reverseGeocode(context: android.content.Context, latitude: Double, longitude: Double): String = withContext(Dispatchers.IO) {
    try {
        if (!Geocoder.isPresent()) { "${latitude.format(3)}, ${longitude.format(3)}" } else {
            val geocoder = Geocoder(context, Locale.getDefault()); val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) { val addr = addresses[0]; val city = addr.locality ?: addr.adminArea ?: ""; val street = addr.thoroughfare ?: ""; when { city.isNotEmpty() && street.isNotEmpty() -> "$city, $street"; city.isNotEmpty() -> city; street.isNotEmpty() -> street; else -> "${latitude.format(3)}, ${longitude.format(3)}" } } else { "${latitude.format(3)}, ${longitude.format(3)}" }
        }
    } catch (e: Exception) { "${latitude.format(3)}, ${longitude.format(3)}" }
}

private fun Double.format(digits: Int) = "%.${digits}f".format(this)

// ============================================================================
// 🔹 ПРОВЕРКА СТАТУСА ЗАЯВКИ В ДРУЗЬЯ
// ============================================================================

private enum class FriendRequestStatus { NONE, ALREADY_SENT, INCOMING_PENDING, ALREADY_FRIENDS }

// ============================================================================
// 🎨 UI: ДИАЛОГ ДЕТАЛЕЙ МАРКЕРА-СОБЫТИЯ
// ============================================================================

@Composable
fun MarkerDetailDialog(marker: MapMarker, currentUserId: String, onDismiss: () -> Unit, onJoin: () -> Unit, onLeave: () -> Unit, onOpenChat: () -> Unit, onReport: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) { Icon(imageVector = when(marker.type) { MarkerType.ANNOUNCEMENT -> Icons.Outlined.Campaign; MarkerType.EVENT -> Icons.Outlined.Event; MarkerType.COMMUNITY -> Icons.Outlined.Groups; MarkerType.DISCUSSION -> Icons.Outlined.Chat }, contentDescription = null, tint = when(marker.type) { MarkerType.ANNOUNCEMENT -> MaterialTheme.colorScheme.primary; MarkerType.EVENT -> MaterialTheme.colorScheme.secondary; MarkerType.COMMUNITY -> MaterialTheme.colorScheme.tertiary; MarkerType.DISCUSSION -> MaterialTheme.colorScheme.tertiary }); Text(marker.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) } }, text = { Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) { Text(marker.description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface); marker.endsAt?.let { endsAt -> val timeLeft = getTimeLeftString(endsAt); Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) { Icon(Icons.Outlined.Timer, null, Modifier.size(16.dp), tint = if(marker.isExpired) Color.Gray else MaterialTheme.colorScheme.primary); Text(if(marker.isExpired) "Завершено" else "До конца: $timeLeft", style = MaterialTheme.typography.bodyMedium, color = if(marker.isExpired) Color.Gray else MaterialTheme.colorScheme.primary) } }; if(marker.type != MarkerType.ANNOUNCEMENT && marker.type != MarkerType.DISCUSSION) { Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) { Icon(Icons.Outlined.People, null, Modifier.size(16.dp)); Text("Участников: ${marker.participantCount}" + (marker.participantLimit?.let { " / $it" } ?: ""), style = MaterialTheme.typography.bodyMedium) } }; if(marker.type != MarkerType.ANNOUNCEMENT) { val isParticipant = marker.isParticipant(currentUserId); val statusText = when { isParticipant -> "Вы участвуете ✅"; marker.isFull -> "Мест нет ❌"; marker.isExpired -> "Событие завершено ⏰"; else -> "Можно присоединиться" }; Text(statusText, style = MaterialTheme.typography.bodySmall, color = when { isParticipant -> MaterialTheme.colorScheme.primary; marker.isFull || marker.isExpired -> Color.Gray; else -> MaterialTheme.colorScheme.onSurfaceVariant }) } } }, confirmButton = { Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { TextButton(onClick = onReport, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Icon(Icons.Outlined.Flag, null, Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("Пожаловаться") }; Spacer(Modifier.weight(1f)); when(marker.type) { MarkerType.ANNOUNCEMENT -> TextButton(onClick = onDismiss) { Text("Закрыть") }; MarkerType.EVENT -> { if(marker.isParticipant(currentUserId)) { OutlinedButton(onClick = onLeave, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Выйти") } } else if(marker.isFull || marker.isExpired) { OutlinedButton(onClick = {}, enabled = false) { Text(if(marker.isExpired) "Завершено" else "Нет мест") } } else { Button(onClick = onJoin, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) { Icon(Icons.Outlined.Add, null, Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("Участвую") } }; TextButton(onClick = onDismiss) { Text("Закрыть") } }; MarkerType.COMMUNITY, MarkerType.DISCUSSION -> { if(marker.isParticipant(currentUserId)) { Button(onClick = onOpenChat, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)) { Icon(Icons.Outlined.Chat, null, Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("В чат") }; OutlinedButton(onClick = onLeave, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Выйти") } } else { Button(onClick = onJoin, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)) { Text("Присоединиться") } }; TextButton(onClick = onDismiss) { Text("Закрыть") } } } } }, dismissButton = null, shape = RoundedCornerShape(20.dp))
}

private fun getTimeLeftString(endsAt: com.google.firebase.Timestamp): String { val now = java.util.Date(); val end = endsAt.toDate(); val diffMillis = end.time - now.time; return when { diffMillis <= 0 -> "0 мин"; diffMillis < 60_000 -> "менее 1 мин"; diffMillis < 3_600_000 -> "${diffMillis / 60_000} мин"; diffMillis < 86_400_000 -> "${diffMillis / 3_600_000} ч"; else -> "${diffMillis / 86_400_000} дн" } }

// ============================================================================
// 🎨 UI: ДИАЛОГ СОЗДАНИЯ МАРКЕРА
// ============================================================================

@Composable
fun CreateMarkerDialog(initialLocation: Point, currentUserId: String, onDismiss: () -> Unit, onCreate: (MapMarker) -> Unit) {
    var title by remember { mutableStateOf("") }; var description by remember { mutableStateOf("") }; var selectedType by remember { mutableStateOf(MarkerType.ANNOUNCEMENT) }; var endsAt by remember { mutableStateOf<java.util.Date?>(null) }; var visibility by remember { mutableStateOf("public") }; var participantLimit by remember { mutableStateOf<Int?>(null) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Создать метку", fontWeight = FontWeight.Bold) }, text = { Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) { Text("Тип метки:", fontWeight = FontWeight.Medium); Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { FilterChip(selected = selectedType == MarkerType.ANNOUNCEMENT, onClick = { selectedType = MarkerType.ANNOUNCEMENT }, label = { Text("📢 Объявление") }, modifier = Modifier.weight(1f)); FilterChip(selected = selectedType == MarkerType.EVENT, onClick = { selectedType = MarkerType.EVENT }, label = { Text("🎉 Событие") }, modifier = Modifier.weight(1f)) }; Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { FilterChip(selected = selectedType == MarkerType.COMMUNITY, onClick = { selectedType = MarkerType.COMMUNITY }, label = { Text("👥 Сообщество") }, modifier = Modifier.weight(1f)); FilterChip(selected = selectedType == MarkerType.DISCUSSION, onClick = { selectedType = MarkerType.DISCUSSION }, label = { Text("💬 Обсуждение") }, modifier = Modifier.weight(1f)) } }; OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Название *") }, singleLine = true, modifier = Modifier.fillMaxWidth()); OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Описание") }, modifier = Modifier.fillMaxWidth().height(100.dp), maxLines = 4); if(selectedType == MarkerType.EVENT || selectedType == MarkerType.COMMUNITY) { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text("Завершится:", modifier = Modifier.weight(1f)); Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { listOf("1ч" to 1, "6ч" to 6, "24ч" to 24, "3д" to 72, "7д" to 168, "∞" to -1).forEach { (label, hours) -> FilterChip(selected = when { hours == -1 && endsAt == null -> true; hours != -1 && endsAt != null -> Math.abs(endsAt!!.time - (System.currentTimeMillis() + hours * 3_600_000L)) < 60_000; else -> false }, onClick = { endsAt = if(hours == -1) null else java.util.Date(System.currentTimeMillis() + hours * 3_600_000L) }, label = { Text(label) }) } } } }; Text("Кто видит:", fontWeight = FontWeight.Medium); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("public" to "Все", "friends" to "Друзья", "private" to "Только я").forEach { (value, label) -> FilterChip(selected = visibility == value, onClick = { visibility = value }, label = { Text(label) }) } }; if(selectedType == MarkerType.EVENT || selectedType == MarkerType.COMMUNITY) { OutlinedTextField(value = participantLimit?.toString() ?: "", onValueChange = { participantLimit = it.toIntOrNull() }, label = { Text("Лимит участников") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth()) } } }, confirmButton = { Button(onClick = { val marker = MapMarker(id = "", type = selectedType, title = title.ifEmpty { "Без названия" }, description = description, createdBy = currentUserId, createdAt = com.google.firebase.Timestamp.now(), endsAt = endsAt?.let { com.google.firebase.Timestamp(it) }, latitude = initialLocation.latitude, longitude = initialLocation.longitude, visibility = visibility, participantLimit = if(selectedType == MarkerType.EVENT || selectedType == MarkerType.COMMUNITY) participantLimit else null, chatId = if(selectedType == MarkerType.COMMUNITY || selectedType == MarkerType.DISCUSSION) "auto" else null); onCreate(marker) }, enabled = title.isNotBlank()) { Text("Создать") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }, shape = RoundedCornerShape(20.dp))
}

// ============================================================================
// 🗺️ COMPOSABLE: MapScreen
// ============================================================================

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun MapScreen(navController: NavHostController, mapView: MapView) {
    val context = LocalContext.current
    val viewModel: MapViewModel = hiltViewModel()

    val currentLocation by viewModel.currentLocation.collectAsState(initial = null)
    val nearbyUsers by viewModel.nearbyUsers.collectAsState(initial = emptyList())
    val locationSettings by viewModel.locationSettings.collectAsState(initial = LocationSettings())
    val friends by viewModel.friends.collectAsState(initial = emptyList())
    val currentUserProfile by viewModel.currentUserProfile.collectAsState(initial = null)
    val nearbyMarkers by viewModel.nearbyMarkers.collectAsState(initial = emptyList())

    val coroutineScope = rememberCoroutineScope()
    val geocodeScope = rememberCoroutineScope()

    var showSettingsDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<UserLocation?>(null) }
    var showUserDialog by remember { mutableStateOf(false) }

    var cameraAddress by remember { mutableStateOf<String?>(null) }
    var isAddressLoading by remember { mutableStateOf(false) }
    var geocodeJob by remember { mutableStateOf<Job?>(null) }
    var friendRequestMessage by remember { mutableStateOf<String?>(null) }

    var showCreateMarkerDialog by remember { mutableStateOf(false) }
    var createMarkerLocation by remember { mutableStateOf<Point?>(null) }
    var selectedEventMarker by remember { mutableStateOf<MapMarker?>(null) }
    var showMarkerReportDialog by remember { mutableStateOf(false) }

    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    val prefs = context.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
    val currentUserId = prefs.getString("user_id", "") ?: ""
    val currentUserName = prefs.getString("user_name", "Пользователь") ?: "Пользователь"

    val iconFactory = remember { MarkerIconFactory(context) }
    val markerManager = remember { mutableStateOf<MarkerManager?>(null) }
    var isMapReady by remember { mutableStateOf(false) }
    var isMapLoading by remember { mutableStateOf(true) }

    fun updateCameraAddress(latitude: Double, longitude: Double) {
        geocodeJob?.cancel()
        geocodeJob = geocodeScope.launch {
            delay(800L); isAddressLoading = true; cameraAddress = null
            try { cameraAddress = reverseGeocode(context, latitude, longitude) } catch (e: Exception) {} finally { isAddressLoading = false }
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionState.launchPermissionRequest()
        if (locationPermissionState.status.isGranted) {
            viewModel.startLocationUpdates(currentUserId, currentUserName, null)
            viewModel.loadNearbyUsers()
            viewModel.loadLocationSettings(currentUserId)
        }
    }

    LaunchedEffect(currentLocation) {
        currentLocation?.let { loc -> viewModel.loadNearbyMarkers(currentUserId, loc.latitude, loc.longitude) }
    }

    val inputListener = remember {
        object : InputListener {
            override fun onMapTap(map: Map, point: Point) {}
            override fun onMapLongTap(map: Map, point: Point) { createMarkerLocation = point; showCreateMarkerDialog = true }
        }
    }

    val cameraListener = remember {
        object : CameraListener {
            override fun onCameraPositionChanged(map: com.yandex.mapkit.map.Map, cameraPosition: CameraPosition, cameraUpdateReason: com.yandex.mapkit.map.CameraUpdateReason, finished: Boolean) {
                if (finished) updateCameraAddress(cameraPosition.target.latitude, cameraPosition.target.longitude)
            }
        }
    }

    // Инициализация MapView
    LaunchedEffect(mapView) {
        mapView.apply {
            map.isRotateGesturesEnabled = true
            map.isScrollGesturesEnabled = true
            map.isTiltGesturesEnabled = true
            map.isZoomGesturesEnabled = true
            map.move(CameraPosition(Point(55.751574, 37.573856), 10.0f, 0.0f, 0.0f), Animation(Animation.Type.SMOOTH, 0.5f), null)
            map.addCameraListener(cameraListener)
            map.addInputListener(inputListener)
        }

        markerManager.value = MarkerManager(mapView, iconFactory,
            onUserTap = { userId, name, avatarUrl, lat, lon, isOnline ->
                selectedUser = UserLocation(userId = userId, name = name, avatarUrl = avatarUrl, latitude = lat, longitude = lon, isOnline = isOnline, lastUpdated = System.currentTimeMillis())
                showUserDialog = true
            },
            onEventTap = { markerId -> selectedEventMarker = nearbyMarkers.find { it.id == markerId } }
        )

        markerManager.value?.setInitialCamera(currentLocation?.latitude ?: 55.751574, currentLocation?.longitude ?: 37.573856)
        isMapReady = true
        isMapLoading = false
    }

    // Обновление маркеров
    LaunchedEffect(isMapReady) {
        if (!isMapReady || markerManager.value == null) return@LaunchedEffect

        snapshotFlow { Triple(nearbyUsers, nearbyMarkers, currentLocation) }
            .debounce(500)
            .collect { (users, markers, location) ->
                val userList = users.map { user -> UserLocationData(user.userId, user.name, user.avatarUrl, user.latitude, user.longitude, user.isOnline) }.toMutableList()
                location?.let { loc -> userList.add(UserLocationData(currentUserId, currentUserProfile?.name ?: "Я", currentUserProfile?.avatarUrl, loc.latitude, loc.longitude, true)) }
                markerManager.value?.updateUsers(userList, currentUserId)

                val eventList = markers.filter { it.status == "active" && !it.isExpired }.map { marker -> EventMarkerData(marker.id, marker.type.name, marker.title, marker.latitude, marker.longitude) }
                markerManager.value?.updateEvents(eventList)
            }
    }

    DisposableEffect(Unit) {
        onDispose {
            geocodeJob?.cancel(); geocodeScope.cancel()
            markerManager.value?.clear(); iconFactory.clearCache()
            try { mapView.map.removeCameraListener(cameraListener); mapView.map.removeInputListener(inputListener) } catch (_: Exception) {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (locationPermissionState.status.isGranted) {
            key("map_view") {
                AndroidView(
                    factory = { mapView },
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (isMapLoading) {
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)).clickable(enabled = false) { }, contentAlignment = Alignment.Center) {
                    Card(Modifier.widthIn(min = 200.dp, max = 300.dp).padding(32.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)) {
                        Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            Surface(Modifier.size(80.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Map, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary) } }
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Загрузка карты", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface); Text("Поиск пользователей и событий...", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center) }
                            LinearProgressIndicator(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)), color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceVariant)
                            Text("Пожалуйста, подождите", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            if (!isMapLoading) {
                IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.align(Alignment.TopStart).padding(16.dp).shadow(4.dp, CircleShape).background(MaterialTheme.colorScheme.surface, CircleShape)) { Icon(Icons.Default.ArrowBack, "Назад", tint = MaterialTheme.colorScheme.onSurface) }
                if (cameraAddress != null || isAddressLoading) { Text(text = if(isAddressLoading) "Определение..." else cameraAddress ?: "", modifier = Modifier.align(Alignment.TopStart).padding(start = 80.dp, top = 24.dp, end = 80.dp).widthIn(max = 280.dp).shadow(1.dp, RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, fontSize = 17.sp, lineHeight = 20.sp), textAlign = TextAlign.Start, maxLines = 3, softWrap = true) }
                Column(modifier = Modifier.align(Alignment.TopEnd).padding(end = 16.dp, top = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconButton(onClick = { showSettingsDialog = true }, modifier = Modifier.size(48.dp).shadow(4.dp, CircleShape).background(MaterialTheme.colorScheme.surface, CircleShape)) { Icon(Icons.Outlined.Settings, "Настройки", tint = MaterialTheme.colorScheme.onSurface) }
                    IconButton(onClick = { currentLocation?.let { loc -> mapView.map.move(CameraPosition(Point(loc.latitude, loc.longitude), 16.0f, 0.0f, 0.0f), Animation(Animation.Type.SMOOTH, 1.0f), null) } }, modifier = Modifier.size(48.dp).shadow(4.dp, CircleShape).background(MaterialTheme.colorScheme.primary, CircleShape)) { Icon(Icons.Outlined.MyLocation, "Моё местоположение", tint = MaterialTheme.colorScheme.onPrimary) }
                    FloatingActionButton(onClick = { Toast.makeText(context, "Зажмите палец на карте, чтобы создать метку", Toast.LENGTH_LONG).show() }, modifier = Modifier.shadow(4.dp, CircleShape), containerColor = MaterialTheme.colorScheme.tertiaryContainer, contentColor = MaterialTheme.colorScheme.onTertiaryContainer) { Icon(Icons.Outlined.AddLocation, "Создать метку") }
                }

                // ========================================================================
                // ДИАЛОГ НАСТРОЕК
                // ========================================================================
                if (showSettingsDialog) {
                    AlertDialog(
                        onDismissRequest = { showSettingsDialog = false },
                        title = {
                            Column {
                                Text("Кто видит ваше местоположение?", fontWeight = FontWeight.Bold)
                                Text("Вы видите всех, кто дал разрешение", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        text = {
                            Column(Modifier.fillMaxWidth()) {
                                Spacer(Modifier.height(8.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(selected = locationSettings.visibility == "all", onClick = { viewModel.updateLocationSettings(locationSettings.copy(visibility = "all"), currentUserId) }, label = { Text("Все") })
                                    FilterChip(selected = locationSettings.visibility == "friends", onClick = { viewModel.updateLocationSettings(locationSettings.copy(visibility = "friends", selectedFriends = friends.map { it.userId }), currentUserId) }, label = { Text("Друзья") })
                                    FilterChip(selected = locationSettings.visibility == "nobody", onClick = { viewModel.updateLocationSettings(locationSettings.copy(visibility = "nobody"), currentUserId) }, label = { Text("Никто") })
                                }
                                if (locationSettings.visibility == "friends") {
                                    Spacer(Modifier.height(16.dp))
                                    friends.forEach { friend ->
                                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(checked = locationSettings.selectedFriends.contains(friend.userId), onCheckedChange = { checked ->
                                                val newList = if (checked) locationSettings.selectedFriends + friend.userId else locationSettings.selectedFriends - friend.userId
                                                viewModel.updateLocationSettings(locationSettings.copy(selectedFriends = newList), currentUserId)
                                            })
                                            Text(friend.name, style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = { TextButton(onClick = { showSettingsDialog = false }) { Text("Готово") } },
                        shape = RoundedCornerShape(20.dp)
                    )
                }

                // ========================================================================
                // ДИАЛОГ ПОЛЬЗОВАТЕЛЯ
                // ========================================================================
                if (showUserDialog && selectedUser != null) {
                    val user = selectedUser!!
                    val isCurrentUser = user.userId == currentUserId
                    val displayName = if (isCurrentUser) currentUserProfile?.name ?: user.name else user.name
                    val displayAvatarUrl = if (isCurrentUser) currentUserProfile?.avatarUrl else user.avatarUrl
                    val isFriend = friends.any { it.userId == user.userId }
                    var friendRequestStatus by remember { mutableStateOf<FriendRequestStatus?>(null) }

                    AlertDialog(
                        onDismissRequest = { showUserDialog = false; selectedUser = null },
                        title = null,
                        text = {
                            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                UserAvatar(avatarUrl = displayAvatarUrl, name = displayName, size = 80, showOnlineIndicator = true, isOnline = user.isOnline)
                                Spacer(Modifier.height(16.dp))
                                Text(displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                Text(if (user.isOnline) "В сети" else "Был(а) недавно", style = MaterialTheme.typography.bodyMedium, color = if (user.isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        confirmButton = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isCurrentUser) Arrangement.Center else Arrangement.spacedBy(8.dp)
                            ) {
                                if (isFriend && !isCurrentUser) {
                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                val chatId = viewModel.getOrCreatePersonalChat(currentUserId, user.userId)
                                                navController.navigate("chat_detail/$chatId/${user.userId}")
                                                showUserDialog = false
                                                selectedUser = null
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Написать") }
                                }
                                if (!isFriend && !isCurrentUser) {
                                    Button(
                                        onClick = {
                                            viewModel.sendFriendRequest(currentUserId, user.userId)
                                            friendRequestStatus = FriendRequestStatus.ALREADY_SENT
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) { Text("В друзья") }
                                }
                                TextButton(onClick = { showUserDialog = false; selectedUser = null }) { Text("Закрыть") }
                            }
                        },
                        shape = RoundedCornerShape(20.dp)
                    )
                }

                // ========================================================================
                // ДИАЛОГ МАРКЕРА
                // ========================================================================
                if (selectedEventMarker != null) {
                    MarkerDetailDialog(
                        marker = selectedEventMarker!!, currentUserId = currentUserId,
                        onDismiss = { selectedEventMarker = null },
                        onJoin = { coroutineScope.launch { viewModel.joinMarker(selectedEventMarker!!.id) } },
                        onLeave = { coroutineScope.launch { viewModel.leaveMarker(selectedEventMarker!!.id) } },
                        onOpenChat = { selectedEventMarker?.chatId?.let { navController.navigate("chat_detail/$it/${selectedEventMarker!!.id}") }; selectedEventMarker = null },
                        onReport = { showMarkerReportDialog = true }
                    )
                }

                // ========================================================================
                // ДИАЛОГ РЕПОРТА
                // ========================================================================
                if (showMarkerReportDialog && selectedEventMarker != null) {
                    AlertDialog(
                        onDismissRequest = { showMarkerReportDialog = false }, title = { Text("Пожаловаться") },
                        confirmButton = { Button(onClick = { coroutineScope.launch { viewModel.reportMarker(selectedEventMarker!!.id, "Жалоба") }; showMarkerReportDialog = false; selectedEventMarker = null }) { Text("Отправить") } },
                        dismissButton = { TextButton(onClick = { showMarkerReportDialog = false }) { Text("Отмена") } }
                    )
                }

                // ========================================================================
                // ДИАЛОГ СОЗДАНИЯ МАРКЕРА
                // ========================================================================
                if (showCreateMarkerDialog && createMarkerLocation != null) {
                    CreateMarkerDialog(
                        initialLocation = createMarkerLocation!!, currentUserId = currentUserId,
                        onDismiss = { showCreateMarkerDialog = false; createMarkerLocation = null },
                        onCreate = { marker -> coroutineScope.launch { viewModel.createMarker(marker); showCreateMarkerDialog = false; createMarkerLocation = null } }
                    )
                }

                // Уведомление
                if (friendRequestMessage != null) {
                    Surface(Modifier.align(Alignment.BottomCenter).padding(16.dp), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.inverseSurface) {
                        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(friendRequestMessage!!, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}