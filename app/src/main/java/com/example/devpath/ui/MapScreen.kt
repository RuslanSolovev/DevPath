package com.example.devpath.ui

import android.Manifest
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.Color as AndroidColor
import android.location.Geocoder
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Scale
import com.example.devpath.domain.models.MapMarker
import com.example.devpath.domain.models.MarkerType
import com.example.devpath.ui.components.UserAvatar
import com.example.devpath.ui.viewmodel.MapViewModel
import com.google.accompanist.permissions.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yandex.mapkit.Animation
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

// ============================================================================
// 🎨 ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ ОТРИСОВКИ МАРКЕРОВ ПОЛЬЗОВАТЕЛЕЙ
// ============================================================================

private fun drawUserName(
    canvas: Canvas,
    name: String,
    totalWidth: Int,
    avatarSize: Int,
    avatarY: Float
) {
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = AndroidColor.BLACK
        textSize = 30f
        typeface = Typeface.defaultFromStyle(Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        setShadowLayer(4f, 2f, 2f, AndroidColor.WHITE)
    }
    val textY = avatarY + avatarSize + 40f
    val displayName = if (name.length > 18) name.take(16) + "…" else name
    canvas.drawText(displayName, totalWidth / 2f, textY, textPaint)
}

private fun drawPointer(
    canvas: Canvas,
    totalWidth: Int,
    totalHeight: Int,
    markerColor: Int
) {
    val pointerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = markerColor
        style = Paint.Style.FILL
    }
    val pointerPath = Path().apply {
        moveTo((totalWidth / 2 - 18).toFloat(), (totalHeight - 35).toFloat())
        lineTo((totalWidth / 2 + 18).toFloat(), (totalHeight - 35).toFloat())
        lineTo((totalWidth / 2).toFloat(), (totalHeight - 12).toFloat())
        close()
    }
    canvas.drawPath(pointerPath, pointerPaint)

    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = AndroidColor.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    canvas.drawPath(pointerPath, strokePaint)
}

private fun drawFallbackMarker(
    canvas: Canvas,
    name: String,
    markerColor: Int,
    totalWidth: Int,
    totalHeight: Int,
    avatarSize: Int
) {
    val centerX = totalWidth / 2f
    val avatarY = 22f
    val radius = avatarSize / 2f

    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = markerColor
        style = Paint.Style.FILL
    }
    canvas.drawCircle(centerX, avatarY + radius, radius + 6, bgPaint)

    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = AndroidColor.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }
    canvas.drawCircle(centerX, avatarY + radius, radius + 6, strokePaint)

    val letterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = AndroidColor.WHITE
        textSize = 60f
        typeface = Typeface.defaultFromStyle(Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    val initial = name.take(1).uppercase()
    val textY = avatarY + radius - (letterPaint.descent() + letterPaint.ascent()) / 2
    canvas.drawText(initial, centerX, textY, letterPaint)

    drawUserName(canvas, name, totalWidth, avatarSize, avatarY)
    drawPointer(canvas, totalWidth, totalHeight, markerColor)
}

private fun drawMarkerContent(
    canvas: Canvas,
    avatarBitmap: Bitmap,
    name: String,
    markerColor: Int,
    totalWidth: Int,
    totalHeight: Int,
    avatarSize: Int
) {
    val centerX = totalWidth / 2f
    val avatarY = 22f
    val radius = avatarSize / 2f

    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = markerColor
        style = Paint.Style.FILL
    }
    canvas.drawCircle(centerX, avatarY + radius, radius + 6, bgPaint)

    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = AndroidColor.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }
    canvas.drawCircle(centerX, avatarY + radius, radius + 6, strokePaint)

    canvas.save()
    val clipPath = Path().apply {
        addCircle(centerX, avatarY + radius, radius, Path.Direction.CW)
    }
    canvas.clipPath(clipPath)
    canvas.drawBitmap(avatarBitmap, centerX - radius - 2, avatarY - 2, null)
    canvas.restore()

    drawUserName(canvas, name, totalWidth, avatarSize, avatarY)
    drawPointer(canvas, totalWidth, totalHeight, markerColor)
}

// ============================================================================
// 🌍 ФУНКЦИЯ ОБРАТНОГО ГЕОКОДИРОВАНИЯ (ИСПРАВЛЕНА)
// ============================================================================

private suspend fun reverseGeocode(
    context: android.content.Context,
    latitude: Double,
    longitude: Double
): String = withContext(Dispatchers.IO) {
    try {
        if (!Geocoder.isPresent()) {
            println("DEBUG: Geocoder not present")
            "${latitude.format(3)}, ${longitude.format(3)}"
        } else {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)

            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                val city = addr.locality ?: addr.adminArea ?: ""
                val street = addr.thoroughfare ?: ""

                when {
                    city.isNotEmpty() && street.isNotEmpty() -> "$city, $street"
                    city.isNotEmpty() -> city
                    street.isNotEmpty() -> street
                    else -> "${latitude.format(3)}, ${longitude.format(3)}"
                }
            } else {
                "${latitude.format(3)}, ${longitude.format(3)}"
            }
        }
    } catch (e: Exception) {
        println("DEBUG: Geocoder error: ${e.message}")
        "${latitude.format(3)}, ${longitude.format(3)}"
    }
}

private fun Double.format(digits: Int) = "%.${digits}f".format(this)

// ============================================================================
// 🔹 ПРОВЕРКА СТАТУСА ЗАЯВКИ В ДРУЗЬЯ
// ============================================================================

private suspend fun checkFriendRequestStatus(
    currentUserId: String,
    targetUserId: String
): FriendRequestStatus {
    return try {
        val db = FirebaseFirestore.getInstance()

        val outgoing = db.collection("friend_requests")
            .whereEqualTo("fromUserId", currentUserId)
            .whereEqualTo("toUserId", targetUserId)
            .whereEqualTo("status", "pending")
            .get()
            .await()

        if (!outgoing.isEmpty) {
            return FriendRequestStatus.ALREADY_SENT
        }

        val incoming = db.collection("friend_requests")
            .whereEqualTo("fromUserId", targetUserId)
            .whereEqualTo("toUserId", currentUserId)
            .whereEqualTo("status", "pending")
            .get()
            .await()

        if (!incoming.isEmpty) {
            return FriendRequestStatus.INCOMING_PENDING
        }

        val friends = db.collection("friends")
            .whereEqualTo("userId", currentUserId)
            .whereArrayContains("friendIds", targetUserId)
            .get()
            .await()

        if (!friends.isEmpty) {
            return FriendRequestStatus.ALREADY_FRIENDS
        }

        FriendRequestStatus.NONE
    } catch (e: Exception) {
        println("DEBUG: Error checking friend request: ${e.message}")
        FriendRequestStatus.NONE
    }
}

private enum class FriendRequestStatus {
    NONE,
    ALREADY_SENT,
    INCOMING_PENDING,
    ALREADY_FRIENDS
}

// ============================================================================
// 🎨 UI: ДИАЛОГ ДЕТАЛЕЙ МАРКЕРА-СОБЫТИЯ
// ============================================================================

@Composable
fun MarkerDetailDialog(
    marker: MapMarker,
    currentUserId: String,
    onDismiss: () -> Unit,
    onJoin: () -> Unit,
    onLeave: () -> Unit,
    onOpenChat: () -> Unit,
    onReport: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = when (marker.type) {
                        MarkerType.ANNOUNCEMENT -> Icons.Outlined.Campaign
                        MarkerType.EVENT -> Icons.Outlined.Event
                        MarkerType.COMMUNITY -> Icons.Outlined.Groups
                        MarkerType.DISCUSSION -> Icons.Outlined.Chat
                    },
                    contentDescription = null,
                    tint = when (marker.type) {
                        MarkerType.ANNOUNCEMENT -> MaterialTheme.colorScheme.primary
                        MarkerType.EVENT -> MaterialTheme.colorScheme.secondary
                        MarkerType.COMMUNITY -> MaterialTheme.colorScheme.tertiary
                        MarkerType.DISCUSSION -> MaterialTheme.colorScheme.tertiary
                    }
                )
                Text(
                    text = marker.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = marker.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                marker.endsAt?.let { endsAt ->
                    val timeLeft = getTimeLeftString(endsAt)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (marker.isExpired) Color.Gray else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (marker.isExpired) "Завершено" else "До конца: $timeLeft",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (marker.isExpired) Color.Gray else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (marker.type != MarkerType.ANNOUNCEMENT && marker.type != MarkerType.DISCUSSION) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Outlined.People,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Участников: ${marker.participantCount}" +
                                    (marker.participantLimit?.let { " / $it" } ?: ""),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    if (marker.participants.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy((-8).dp)
                        ) {
                            marker.participants.take(5).forEach { participantId ->
                                Surface(
                                    modifier = Modifier.size(28.dp),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = participantId.take(1).uppercase(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            if (marker.participants.size > 5) {
                                Text(
                                    text = "+${marker.participants.size - 5}",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }
                    }
                }

                if (marker.type != MarkerType.ANNOUNCEMENT) {
                    val isParticipant = marker.isParticipant(currentUserId)
                    val statusText = when {
                        isParticipant -> "Вы участвуете ✅"
                        marker.isFull -> "Мест нет ❌"
                        marker.isExpired -> "Событие завершено ⏰"
                        else -> "Можно присоединиться"
                    }
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            isParticipant -> MaterialTheme.colorScheme.primary
                            marker.isFull || marker.isExpired -> Color.Gray
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = onReport,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Outlined.Flag,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Пожаловаться")
                }

                Spacer(modifier = Modifier.weight(1f))

                when (marker.type) {
                    MarkerType.ANNOUNCEMENT -> {
                        TextButton(onClick = onDismiss) { Text("Закрыть") }
                    }

                    MarkerType.EVENT -> {
                        if (marker.isParticipant(currentUserId)) {
                            OutlinedButton(
                                onClick = onLeave,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Выйти")
                            }
                        } else if (marker.isFull || marker.isExpired) {
                            OutlinedButton(
                                onClick = {},
                                enabled = false
                            ) {
                                Text(if (marker.isExpired) "Завершено" else "Нет мест")
                            }
                        } else {
                            Button(
                                onClick = onJoin,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Icon(
                                    Icons.Outlined.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Участвую")
                            }
                        }
                        TextButton(onClick = onDismiss) { Text("Закрыть") }
                    }

                    MarkerType.COMMUNITY, MarkerType.DISCUSSION -> {
                        if (marker.isParticipant(currentUserId)) {
                            Button(
                                onClick = onOpenChat,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary
                                )
                            ) {
                                Icon(
                                    Icons.Outlined.Chat,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("В чат")
                            }
                            OutlinedButton(
                                onClick = onLeave,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Выйти")
                            }
                        } else {
                            Button(
                                onClick = onJoin,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary
                                )
                            ) {
                                Text("Присоединиться")
                            }
                        }
                        TextButton(onClick = onDismiss) { Text("Закрыть") }
                    }
                }
            }
        },
        dismissButton = null,
        shape = RoundedCornerShape(20.dp)
    )
}

private fun getTimeLeftString(endsAt: com.google.firebase.Timestamp): String {
    val now = java.util.Date()
    val end = endsAt.toDate()
    val diffMillis = end.time - now.time

    return when {
        diffMillis <= 0 -> "0 мин"
        diffMillis < 60_000 -> "менее 1 мин"
        diffMillis < 3_600_000 -> "${diffMillis / 60_000} мин"
        diffMillis < 86_400_000 -> "${diffMillis / 3_600_000} ч"
        else -> "${diffMillis / 86_400_000} дн"
    }
}

// ============================================================================
// 🎨 UI: ДИАЛОГ СОЗДАНИЯ МАРКЕРА
// ============================================================================

@Composable
fun CreateMarkerDialog(
    initialLocation: Point,
    onDismiss: () -> Unit,
    onCreate: (MapMarker) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(MarkerType.ANNOUNCEMENT) }
    var endsAt by remember { mutableStateOf<java.util.Date?>(null) }
    var visibility by remember { mutableStateOf("public") }
    var participantLimit by remember { mutableStateOf<Int?>(null) }

    println("DEBUG: CreateMarkerDialog - selectedType: $selectedType")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Создать метку", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Тип метки:", fontWeight = FontWeight.Medium)

                // 🔹 ВСЕ 4 ТИПА МАРКЕРОВ
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Первый ряд: ANNOUNCEMENT и EVENT
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedType == MarkerType.ANNOUNCEMENT,
                            onClick = {
                                selectedType = MarkerType.ANNOUNCEMENT
                                println("DEBUG: Selected type: ANNOUNCEMENT")
                            },
                            label = { Text("📢 Объявление") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedType == MarkerType.EVENT,
                            onClick = {
                                selectedType = MarkerType.EVENT
                                println("DEBUG: Selected type: EVENT")
                            },
                            label = { Text("🎉 Событие") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Второй ряд: COMMUNITY и DISCUSSION
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedType == MarkerType.COMMUNITY,
                            onClick = {
                                selectedType = MarkerType.COMMUNITY
                                println("DEBUG: Selected type: COMMUNITY")
                            },
                            label = { Text("👥 Сообщество") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedType == MarkerType.DISCUSSION,
                            onClick = {
                                selectedType = MarkerType.DISCUSSION
                                println("DEBUG: Selected type: DISCUSSION")
                            },
                            label = { Text("💬 Обсуждение") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название *") },
                    placeholder = { Text("Например: Потерялся щенок") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание") },
                    placeholder = { Text("Подробности...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 4
                )

                // Время завершения (только для EVENT и COMMUNITY)
                if (selectedType == MarkerType.EVENT || selectedType == MarkerType.COMMUNITY) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Завершится:", modifier = Modifier.weight(1f))

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(
                                "1ч" to 1,
                                "6ч" to 6,
                                "24ч" to 24,
                                "3д" to 72,
                                "7д" to 168,
                                "∞" to -1
                            ).forEach { (label, hours) ->
                                FilterChip(
                                    selected = when {
                                        hours == -1 && endsAt == null -> true
                                        hours != -1 && endsAt != null -> {
                                            val expectedTime = System.currentTimeMillis() + hours * 3_600_000L
                                            Math.abs(endsAt!!.time - expectedTime) < 60_000
                                        }
                                        else -> false
                                    },
                                    onClick = {
                                        endsAt = if (hours == -1) null
                                        else java.util.Date(System.currentTimeMillis() + hours * 3_600_000L)
                                    },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }
                }

                Text("Кто видит:", fontWeight = FontWeight.Medium)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "public" to "Все",
                        "friends" to "Друзья",
                        "private" to "Только я"
                    ).forEach { (value, label) ->
                        FilterChip(
                            selected = visibility == value,
                            onClick = { visibility = value },
                            label = { Text(label) }
                        )
                    }
                }

                if (selectedType == MarkerType.EVENT || selectedType == MarkerType.COMMUNITY) {
                    OutlinedTextField(
                        value = participantLimit?.toString() ?: "",
                        onValueChange = { participantLimit = it.toIntOrNull() },
                        label = { Text("Лимит участников (необязательно)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    println("DEBUG: Creating marker of type: ${selectedType.name}")
                    val marker = MapMarker(
                        id = "",
                        type = selectedType,
                        title = title.ifEmpty { "Без названия" },
                        description = description,
                        createdBy = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                        createdAt = com.google.firebase.Timestamp.now(),
                        endsAt = endsAt?.let { com.google.firebase.Timestamp(it) },
                        latitude = initialLocation.latitude,
                        longitude = initialLocation.longitude,
                        visibility = visibility,
                        participantLimit = if (selectedType == MarkerType.EVENT || selectedType == MarkerType.COMMUNITY) participantLimit else null,
                        chatId = if (selectedType == MarkerType.COMMUNITY || selectedType == MarkerType.DISCUSSION) "auto" else null
                    )
                    onCreate(marker)
                },
                enabled = title.isNotBlank()
            ) {
                Text("Создать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

// ============================================================================
// 🗺️ COMPOSABLE: MapScreen
// ============================================================================

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    val viewModel: MapViewModel = hiltViewModel()

    val currentLocation by viewModel.currentLocation.collectAsState(initial = null)
    val nearbyUsers by viewModel.nearbyUsers.collectAsState(initial = emptyList())
    val locationSettings by viewModel.locationSettings.collectAsState(
        initial = com.example.devpath.data.repository.LocationSettings()
    )
    val friends by viewModel.friends.collectAsState(initial = emptyList())
    val currentUserProfile by viewModel.currentUserProfile.collectAsState(initial = null)
    val nearbyMarkers by viewModel.nearbyMarkers.collectAsState(initial = emptyList())

    val coroutineScope = rememberCoroutineScope()
    val geocodeScope = rememberCoroutineScope()

    var showSettingsDialog by remember { mutableStateOf(false) }
    var selectedUser by remember {
        mutableStateOf<com.example.devpath.data.repository.UserLocation?>(
            null
        )
    }
    var showUserDialog by remember { mutableStateOf(false) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    var hasInitialCameraMove by remember { mutableStateOf(false) }
    var cameraAddress by remember { mutableStateOf<String?>(null) }
    var isAddressLoading by remember { mutableStateOf(false) }
    var geocodeJob by remember { mutableStateOf<Job?>(null) }
    var friendRequestMessage by remember { mutableStateOf<String?>(null) }

    // 🔹 Состояния для создания маркеров
    var showCreateMarkerDialog by remember { mutableStateOf(false) }
    var createMarkerLocation by remember { mutableStateOf<Point?>(null) }
    var selectedEventMarker by remember { mutableStateOf<MapMarker?>(null) }
    var showMarkerReportDialog by remember { mutableStateOf(false) }

    val userPlacemarks = remember { mutableMapOf<String, PlacemarkMapObject>() }
    val markerPlacemarks = remember { mutableMapOf<String, PlacemarkMapObject>() }
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .crossfade(true)
            .build()
    }

    val colorCurrentUser = AndroidColor.rgb(33, 150, 243)
    val colorOtherUser = AndroidColor.rgb(244, 67, 54)
    val markerBitmapCache = remember { mutableMapOf<String, ImageProvider>() }
    val eventIconCache = remember { mutableMapOf<String, ImageProvider>() }

    // ========================================================================
    // 🎨 СОЗДАНИЕ МАРКЕРА ПОЛЬЗОВАТЕЛЯ
    // ========================================================================


    suspend fun createMarkerBitmap(
        name: String,
        avatarUrl: String?,
        isCurrentUser: Boolean
    ): ImageProvider = suspendCoroutine { continuation ->
        val avatarSize = 140
        val totalWidth = 245
        val totalHeight = 294
        val markerColor = if (isCurrentUser) colorCurrentUser else colorOtherUser

        val cacheKey = "${if (isCurrentUser) "me" else name}_${avatarUrl ?: "noavatar"}"

        markerBitmapCache[cacheKey]?.let {
            println("DEBUG: Using cached user marker for: $name")
            continuation.resume(it)
            return@suspendCoroutine
        }

        println("DEBUG: Creating new user marker bitmap for: $name")
        val bitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(AndroidColor.TRANSPARENT, PorterDuff.Mode.CLEAR)

        if (!avatarUrl.isNullOrEmpty()) {
            coroutineScope.launch {
                try {
                    val request = ImageRequest.Builder(context)
                        .data(avatarUrl)
                        .size(avatarSize * 2)
                        .scale(Scale.FILL)
                        .crossfade(true)
                        .allowHardware(false)
                        .build()
                    val result = imageLoader.execute(request)

                    if (result is SuccessResult) {
                        println("DEBUG: Avatar loaded for user marker: $name")
                        val avatarBitmap = result.drawable.toBitmap()
                        drawMarkerContent(
                            canvas = canvas,
                            avatarBitmap = avatarBitmap,
                            name = name,
                            markerColor = markerColor,
                            totalWidth = totalWidth,
                            totalHeight = totalHeight,
                            avatarSize = avatarSize
                        )
                    } else {
                        println("DEBUG: Failed to load avatar for: $name, using fallback")
                        drawFallbackMarker(
                            canvas, name, markerColor, totalWidth, totalHeight, avatarSize
                        )
                    }
                    val imageProvider = ImageProvider.fromBitmap(bitmap)
                    markerBitmapCache[cacheKey] = imageProvider
                    continuation.resume(imageProvider)
                } catch (e: Exception) {
                    println("DEBUG: Error loading avatar for: $name: ${e.message}")
                    drawFallbackMarker(
                        canvas, name, markerColor, totalWidth, totalHeight, avatarSize
                    )
                    val imageProvider = ImageProvider.fromBitmap(bitmap)
                    markerBitmapCache[cacheKey] = imageProvider
                    continuation.resume(imageProvider)
                }
            }
        } else {
            println("DEBUG: No avatar for: $name, using fallback")
            drawFallbackMarker(canvas, name, markerColor, totalWidth, totalHeight, avatarSize)
            val imageProvider = ImageProvider.fromBitmap(bitmap)
            markerBitmapCache[cacheKey] = imageProvider
            continuation.resume(imageProvider)
        }
    }


    // ========================================================================
    // 🎨 ИКОНКА ДЛЯ МАРКЕРА-СОБЫТИЯ
    // ========================================================================
    fun createEventMarkerIcon(type: MarkerType, participantCount: Int): ImageProvider {
        val cacheKey = "${type.name}_$participantCount"
        eventIconCache[cacheKey]?.let { return it }

        val color = when (type) {
            MarkerType.ANNOUNCEMENT -> AndroidColor.rgb(33, 150, 243)
            MarkerType.EVENT -> AndroidColor.rgb(76, 175, 80)
            MarkerType.COMMUNITY -> AndroidColor.rgb(156, 39, 176)
            MarkerType.DISCUSSION -> AndroidColor.rgb(255, 152, 0)
        }

        val size = 100
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            style = Paint.Style.FILL
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 10, paint)

        paint.apply {
            this.color = AndroidColor.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 10, paint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = AndroidColor.WHITE
            textSize = 40f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.defaultFromStyle(Typeface.BOLD)
        }
        val iconChar = when (type) {
            MarkerType.ANNOUNCEMENT -> "📢"
            MarkerType.EVENT -> "🎉"
            MarkerType.COMMUNITY -> "👥"
            MarkerType.DISCUSSION -> "💬"
        }
        canvas.drawText(iconChar, size / 2f, size / 2f + 15, textPaint)

        if (participantCount > 0 && type != MarkerType.ANNOUNCEMENT) {
            val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = AndroidColor.RED
                style = Paint.Style.FILL
            }
            canvas.drawCircle(size - 25f, 25f, 18f, badgePaint)

            val badgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = AndroidColor.WHITE
                textSize = 14f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.defaultFromStyle(Typeface.BOLD)
            }
            canvas.drawText(
                if (participantCount < 100) participantCount.toString() else "99+",
                size - 25f,
                25f + 5,
                badgeTextPaint
            )
        }

        val imageProvider = ImageProvider.fromBitmap(bitmap)
        eventIconCache[cacheKey] = imageProvider
        return imageProvider
    }

    suspend fun updateUserMarkers(mapView: MapView) {
        println("DEBUG: Updating user markers, nearby users: ${nearbyUsers.size}")

        // Удаляем старые маркеры
        userPlacemarks.values.forEach { placemark ->
            mapView.map.mapObjects.remove(placemark)
        }
        userPlacemarks.clear()

        // 🔹 Маркер текущего пользователя
        currentLocation?.let { loc ->
            val userPoint = Point(loc.latitude, loc.longitude)
            val myName = currentUserProfile?.name ?: "Я"
            val myAvatar = currentUserProfile?.avatarUrl

            println("DEBUG: Creating marker for CURRENT user: $myName at ${loc.latitude}, ${loc.longitude}")

            val icon = createMarkerBitmap(myName, myAvatar, isCurrentUser = true)
            val placemark = mapView.map.mapObjects.addPlacemark(userPoint, icon)

            placemark.isDraggable = false
            placemark.setZIndex(10f)
            placemark.userData = "current_user"

            // 🔥 Добавляем TapListener
            placemark.addTapListener(MapObjectTapListener { _, point ->
                println("DEBUG: Current user marker tapped at ${point?.latitude}, ${point?.longitude}")
                selectedUser = com.example.devpath.data.repository.UserLocation(
                    userId = currentUserId,
                    name = myName,
                    avatarUrl = myAvatar,
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    isOnline = true,
                    lastUpdated = System.currentTimeMillis()
                )
                showUserDialog = true
                true
            })

            // Небольшая пауза для гарантии добавления слушателя
            kotlinx.coroutines.delay(50)

            userPlacemarks["current"] = placemark

            if (!hasInitialCameraMove) {
                mapView.map.move(
                    CameraPosition(userPoint, 16.0f, 0.0f, 0.0f),
                    Animation(Animation.Type.SMOOTH, 1.5f),
                    null
                )
                hasInitialCameraMove = true
                println("DEBUG: Initial camera move completed")
            }
        }

        // 🔹 Маркеры других пользователей
        nearbyUsers.forEach { user ->
            val userPoint = Point(user.latitude, user.longitude)
            println("DEBUG: Creating marker for NEARBY user: ${user.name} at ${user.latitude}, ${user.longitude}")

            val icon = createMarkerBitmap(user.name, user.avatarUrl, isCurrentUser = false)
            val placemark = mapView.map.mapObjects.addPlacemark(userPoint, icon)

            placemark.isDraggable = false
            placemark.setZIndex(5f)
            placemark.userData = user.userId

            // 🔥 Добавляем TapListener
            placemark.addTapListener(MapObjectTapListener { mapObject, point ->
                println("DEBUG: Nearby user marker tapped: ${user.name} at ${point?.latitude}, ${point?.longitude}")
                selectedUser = user
                showUserDialog = true
                true
            })

            // Небольшая пауза для гарантии добавления слушателя
            kotlinx.coroutines.delay(30)

            userPlacemarks[user.userId] = placemark
        }

        println("DEBUG: Added ${userPlacemarks.size} user markers with tap listeners (including current user)")
    }

    suspend fun updateEventMarkers(mapView: MapView) {
        println("DEBUG: Updating event markers, count: ${nearbyMarkers.size}")

        // Удаляем старые маркеры
        markerPlacemarks.values.forEach { placemark ->
            mapView.map.mapObjects.remove(placemark)
        }
        markerPlacemarks.clear()

        // Добавляем новые
        nearbyMarkers.forEach { marker ->
            if (marker.status != "active" || marker.isExpired) {
                println("DEBUG: Skipping marker ${marker.title} - inactive or expired")
                return@forEach
            }

            val markerPoint = Point(marker.latitude, marker.longitude)
            println("DEBUG: Adding marker at ${marker.latitude}, ${marker.longitude}: ${marker.title}")

            val icon = createEventMarkerIcon(marker.type, marker.participantCount)
            val placemark = mapView.map.mapObjects.addPlacemark(markerPoint, icon)

            placemark.isDraggable = false
            placemark.setZIndex(3f)
            placemark.userData = marker.id  // сохраняем ID

            // 🔥 Добавляем TapListener (addTapListener возвращает Unit, не Boolean)
            placemark.addTapListener(MapObjectTapListener { mapObject, point ->
                println("DEBUG: Event marker tapped! Marker: ${marker.title}, ID: ${marker.id}")
                selectedEventMarker = marker
                true
            })

            // Чтобы гарантировать добавление слушателя, делаем небольшую паузу
            kotlinx.coroutines.delay(50)

            markerPlacemarks[marker.id] = placemark
        }

        println("DEBUG: Added ${markerPlacemarks.size} event markers with tap listeners")
    }


    // ========================================================================
    // 🔹 ОБНОВЛЕНИЕ АДРЕСА
    // ========================================================================
    fun updateCameraAddress(latitude: Double, longitude: Double) {
        geocodeJob?.cancel()
        geocodeJob = geocodeScope.launch {
            delay(800L)
            isAddressLoading = true
            cameraAddress = null
            try {
                val address = reverseGeocode(context, latitude, longitude)
                cameraAddress = address
            } catch (e: Exception) {
                println("DEBUG: Geocoding error: ${e.message}")
            } finally {
                isAddressLoading = false
            }
        }
    }

    // ========================================================================
    // 🚀 ИНИЦИАЛИЗАЦИЯ
    // ========================================================================
    LaunchedEffect(Unit) {
        locationPermissionState.launchPermissionRequest()
        if (locationPermissionState.status.isGranted) {
            viewModel.startLocationUpdates()
        }
        viewModel.loadLocationSettings()
        viewModel.loadFriends()
    }

    LaunchedEffect(nearbyUsers, currentUserProfile, currentLocation) {
        println("DEBUG: LaunchedEffect triggered for user markers")
        println("DEBUG: currentLocation = ${currentLocation != null}")
        println("DEBUG: nearbyUsers count = ${nearbyUsers.size}")
        println("DEBUG: currentUserProfile = ${currentUserProfile?.name}")

        if (currentLocation != null) {
            // Даём карте время на полную инициализацию
            delay(2000)

            mapViewRef?.let { mapView ->
                try {
                    println("DEBUG: Updating user markers on map...")
                    updateUserMarkers(mapView)
                    println("DEBUG: User markers updated successfully")
                } catch (e: Exception) {
                    println("DEBUG: Error updating user markers: ${e.message}")
                    e.printStackTrace()
                    // Пробуем ещё раз
                    kotlinx.coroutines.delay(1000)
                    try {
                        updateUserMarkers(mapView)
                        println("DEBUG: User markers updated on second attempt")
                    } catch (e2: Exception) {
                        println("DEBUG: Second attempt failed: ${e2.message}")
                    }
                }
            } ?: run {
                println("DEBUG: MapView is null, cannot update user markers")
            }

            // Обновляем координаты для загрузки событий
            try {
                viewModel.updateMarkerLocation(
                    currentLocation!!.latitude,
                    currentLocation!!.longitude
                )
                println("DEBUG: Marker location updated")
            } catch (e: Exception) {
                println("DEBUG: Error updating marker location: ${e.message}")
            }
        } else {
            println("DEBUG: Current location is null, waiting...")
        }
    }

    LaunchedEffect(nearbyMarkers) {
        if (nearbyMarkers.isNotEmpty()) {
            println("DEBUG: Nearby markers received, count: ${nearbyMarkers.size}")
            println("DEBUG: First marker: ${nearbyMarkers.firstOrNull()?.title} at ${nearbyMarkers.firstOrNull()?.latitude}, ${nearbyMarkers.firstOrNull()?.longitude}")

            // Ждём дополнительное время после обновления пользовательских маркеров
            delay(3000) // Увеличено для гарантии

            mapViewRef?.let { mapView ->
                try {
                    println("DEBUG: Updating event markers on map...")
                    updateEventMarkers(mapView)
                    println("DEBUG: Event markers updated successfully")
                } catch (e: Exception) {
                    println("DEBUG: Error updating event markers: ${e.message}")
                    e.printStackTrace()
                    // Пробуем ещё раз
                    kotlinx.coroutines.delay(1000)
                    try {
                        updateEventMarkers(mapView)
                        println("DEBUG: Event markers updated on second attempt")
                    } catch (e2: Exception) {
                        println("DEBUG: Second attempt for events failed: ${e2.message}")
                    }
                }
            } ?: run {
                println("DEBUG: MapView is null, cannot update event markers")
            }
        }
    }

    // 🔹 Создаем InputListener один раз
    val inputListener = remember {
        object : InputListener {
            override fun onMapTap(map: Map, point: Point) {
                println("DEBUG: Map TAP at ${point.latitude}, ${point.longitude}")
            }

            override fun onMapLongTap(map: Map, point: Point) {
                println("DEBUG: Map LONG TAP at ${point.latitude}, ${point.longitude}")
                createMarkerLocation = point
                showCreateMarkerDialog = true
            }
        }
    }

    val cameraListener = remember {
        object : CameraListener {
            override fun onCameraPositionChanged(
                map: com.yandex.mapkit.map.Map,
                cameraPosition: CameraPosition,
                cameraUpdateReason: com.yandex.mapkit.map.CameraUpdateReason,
                finished: Boolean
            ) {
                if (finished) {
                    updateCameraAddress(
                        cameraPosition.target.latitude,
                        cameraPosition.target.longitude
                    )
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            println("DEBUG: Disposing MapScreen, cleaning up...")
            geocodeJob?.cancel()
            geocodeScope.cancel()
            try {
                mapViewRef?.map?.removeCameraListener(cameraListener)
                mapViewRef?.map?.removeInputListener(inputListener)
                mapViewRef?.onStop()
                println("DEBUG: MapView stopped and listeners removed")
            } catch (e: Exception) {
                println("DEBUG: Error during map cleanup: ${e.message}")
            }
            markerBitmapCache.clear()
            eventIconCache.clear()
            userPlacemarks.clear()
            markerPlacemarks.clear()
        }
    }

    // ========================================================================
    // ОСНОВНОЙ UI
    // ========================================================================
    Box(modifier = Modifier.fillMaxSize()) {
        if (locationPermissionState.status.isGranted) {
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        mapViewRef = this

                        // 🔹 Явно включаем все жесты
                        map.isRotateGesturesEnabled = true
                        map.isScrollGesturesEnabled = true
                        map.isTiltGesturesEnabled = true
                        map.isZoomGesturesEnabled = true

                        // 🔹 Включаем обработку тапов для объектов на карте
                        map.isFastTapEnabled = true  // Ускоряет обработку тапов

                        map.move(
                            CameraPosition(Point(55.751574, 37.573856), 10.0f, 0.0f, 0.0f),
                            Animation(Animation.Type.SMOOTH, 0.5f),
                            null
                        )

                        map.addCameraListener(cameraListener)
                        map.addInputListener(inputListener)

                        println("DEBUG: MapView created, gestures enabled")
                        println("DEBUG: Map dimensions: ${this.width}x${this.height}")

                        onStart()
                        println("DEBUG: MapView onStart() completed")

                        // 🔹 Дополнительная задержка для полной инициализации
                        postDelayed({
                            println("DEBUG: MapView fully initialized delay completed")
                        }, 500)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // 🔹 Кнопка "Назад"
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .shadow(4.dp, CircleShape)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Назад",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // 🔹 Адрес
            if (cameraAddress != null || isAddressLoading) {
                Text(
                    text = if (isAddressLoading) "Определение..." else cameraAddress ?: "",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 80.dp, top = 24.dp, end = 80.dp)
                        .widthIn(max = 280.dp)
                        .shadow(1.dp, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 17.sp,
                        lineHeight = 20.sp
                    ),
                    textAlign = TextAlign.Start,
                    maxLines = 3,
                    softWrap = true
                )
            }

            // 🔹 Кнопки справа
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 16.dp, top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = { showSettingsDialog = true },
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(4.dp, CircleShape)
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                ) {
                    Icon(
                        Icons.Outlined.Settings,
                        contentDescription = "Настройки видимости",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = {
                        currentLocation?.let { loc ->
                            mapViewRef?.map?.move(
                                CameraPosition(
                                    Point(loc.latitude, loc.longitude),
                                    16.0f,
                                    0.0f,
                                    0.0f
                                ),
                                Animation(Animation.Type.SMOOTH, 1.0f),
                                null
                            )
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(4.dp, CircleShape)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Icon(
                        Icons.Outlined.MyLocation,
                        contentDescription = "Моё местоположение",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                // 🔹 Кнопка создания метки через долгое нажатие
                FloatingActionButton(
                    onClick = {
                        Toast.makeText(
                            context,
                            "Зажмите палец на карте в любом месте, чтобы создать метку",
                            Toast.LENGTH_LONG
                        ).show()
                    },
                    modifier = Modifier.shadow(4.dp, CircleShape),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ) {
                    Icon(
                        Icons.Outlined.AddLocation,
                        contentDescription = "Инструкция по созданию метки"
                    )
                }
            }

            // ========================================================================
            // ⚙️ ДИАЛОГ НАСТРОЕК
            // ========================================================================
            if (showSettingsDialog) {
                AlertDialog(
                    onDismissRequest = { showSettingsDialog = false },
                    title = {
                        Column {
                            Text("Кто видит ваше местоположение?", fontWeight = FontWeight.Bold)
                            Text(
                                "Вы видите всех, кто дал разрешение",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    text = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = locationSettings.visibility == "all",
                                    onClick = {
                                        viewModel.updateLocationSettings(
                                            locationSettings.copy(visibility = "all")
                                        )
                                    },
                                    label = { Text("Все") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Outlined.Public,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )

                                FilterChip(
                                    selected = locationSettings.visibility == "friends",
                                    onClick = {
                                        val newSelectedFriends =
                                            if (locationSettings.visibility != "friends") {
                                                friends.map { it.userId }
                                            } else {
                                                locationSettings.selectedFriends
                                            }
                                        viewModel.updateLocationSettings(
                                            locationSettings.copy(
                                                visibility = "friends",
                                                selectedFriends = newSelectedFriends
                                            )
                                        )
                                    },
                                    label = { Text("Друзья") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Outlined.People,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )

                                FilterChip(
                                    selected = locationSettings.visibility == "nobody",
                                    onClick = {
                                        viewModel.updateLocationSettings(
                                            locationSettings.copy(visibility = "nobody")
                                        )
                                    },
                                    label = { Text("Никто") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Outlined.VisibilityOff,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            }

                            if (locationSettings.visibility == "friends") {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Выберите друзей, которым будет видно ваше местоположение:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                if (friends.isEmpty()) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                                alpha = 0.3f
                                            )
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                Icons.Outlined.PersonOff,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                "У вас пока нет друзей",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                } else {
                                    friends.forEach { friend ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = locationSettings.selectedFriends.contains(
                                                    friend.userId
                                                ),
                                                onCheckedChange = { checked ->
                                                    val newList = if (checked) {
                                                        locationSettings.selectedFriends + friend.userId
                                                    } else {
                                                        locationSettings.selectedFriends - friend.userId
                                                    }
                                                    viewModel.updateLocationSettings(
                                                        locationSettings.copy(selectedFriends = newList)
                                                    )
                                                }
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    friend.name,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                Text(
                                                    "@${friend.name.lowercase().replace(" ", "_")}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            val isFriendOnline = nearbyUsers.find {
                                                it.userId == friend.userId
                                            }?.isOnline ?: false

                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.size(14.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(10.dp)
                                                        .background(Color.White, CircleShape)
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .background(
                                                            if (isFriendOnline) Color(0xFF10B981) else Color.Gray,
                                                            CircleShape
                                                        )
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        "Вы всегда видите всех пользователей, которые разрешили показывать своё местоположение",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showSettingsDialog = false }) {
                            Text("Готово")
                        }
                    },
                    shape = RoundedCornerShape(20.dp)
                )
            }

            // ========================================================================
            // 👤 ДИАЛОГ ПОЛЬЗОВАТЕЛЯ
            // ========================================================================
            if (showUserDialog && selectedUser != null) {
                val user = selectedUser!!
                val isCurrentUser = user.userId == currentUserId
                val displayAvatarUrl = if (isCurrentUser) {
                    currentUserProfile?.avatarUrl ?: user.avatarUrl
                } else {
                    user.avatarUrl
                }
                val displayName = if (isCurrentUser) {
                    currentUserProfile?.name ?: user.name
                } else {
                    user.name
                }
                val isFriend = friends.any { it.userId == user.userId }

                var friendRequestStatus by remember { mutableStateOf<FriendRequestStatus?>(null) }
                var isCheckingRequest by remember { mutableStateOf(false) }

                LaunchedEffect(user.userId) {
                    if (!isCurrentUser && !isFriend) {
                        isCheckingRequest = true
                        friendRequestStatus = checkFriendRequestStatus(currentUserId, user.userId)
                        isCheckingRequest = false
                    }
                }

                AlertDialog(
                    onDismissRequest = {
                        showUserDialog = false
                        selectedUser = null
                        friendRequestStatus = null
                    },
                    title = null,
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            UserAvatar(
                                avatarUrl = displayAvatarUrl,
                                name = displayName,
                                size = 80,
                                showOnlineIndicator = true,
                                isOnline = user.isOnline
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (user.isOnline) "В сети" else "Был(а) недавно",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (user.isOnline) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Обновлено: ${
                                    SimpleDateFormat("HH:mm", Locale.getDefault())
                                        .format(user.lastUpdated)
                                }",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            if (isCurrentUser) {
                                Spacer(modifier = Modifier.height(20.dp))
                                Surface(
                                    modifier = Modifier.fillMaxWidth(0.8f),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            Icons.Filled.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "Это вы",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = when {
                                isCurrentUser -> Arrangement.Center
                                isFriend -> Arrangement.spacedBy(8.dp)
                                else -> Arrangement.Center
                            }
                        ) {
                            if (isFriend && !isCurrentUser) {
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            try {
                                                val chatId = viewModel.getOrCreatePersonalChat(
                                                    currentUserId,
                                                    user.userId
                                                )
                                                navController.navigate("chat_detail/$chatId/${user.userId}")
                                                delay(100)
                                                showUserDialog = false
                                                selectedUser = null
                                            } catch (e: Exception) {
                                                Toast.makeText(
                                                    context,
                                                    "Не удалось открыть чат: ${e.localizedMessage}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(
                                        Icons.Outlined.Chat,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Написать")
                                }
                            }

                            if (!isFriend && !isCurrentUser) {
                                when (friendRequestStatus) {
                                    FriendRequestStatus.ALREADY_SENT,
                                    FriendRequestStatus.INCOMING_PENDING -> {
                                        OutlinedButton(
                                            onClick = {},
                                            modifier = Modifier.weight(1f),
                                            enabled = false,
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                    alpha = 0.5f
                                                )
                                            )
                                        ) {
                                            Icon(
                                                Icons.Outlined.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Заявка отправлена")
                                        }
                                    }

                                    else -> {
                                        OutlinedButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    if (isCheckingRequest) return@launch
                                                    val status = checkFriendRequestStatus(
                                                        currentUserId,
                                                        user.userId
                                                    )
                                                    when (status) {
                                                        FriendRequestStatus.ALREADY_SENT,
                                                        FriendRequestStatus.INCOMING_PENDING -> {
                                                            friendRequestMessage =
                                                                "Заявка уже отправлена"
                                                        }

                                                        FriendRequestStatus.ALREADY_FRIENDS -> {
                                                            friendRequestMessage = "Вы уже друзья"
                                                        }

                                                        else -> {
                                                            viewModel.sendFriendRequest(user.userId)
                                                            friendRequestStatus =
                                                                FriendRequestStatus.ALREADY_SENT
                                                            friendRequestMessage =
                                                                "Заявка отправлена"
                                                        }
                                                    }
                                                    delay(2000)
                                                    friendRequestMessage = null
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            enabled = !isCheckingRequest
                                        ) {
                                            if (isCheckingRequest) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(18.dp),
                                                    strokeWidth = 2.dp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            } else {
                                                Icon(
                                                    Icons.Outlined.PersonAdd,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("В друзья")
                                            }
                                        }
                                    }
                                }
                            }

                            TextButton(
                                onClick = {
                                    showUserDialog = false
                                    selectedUser = null
                                    friendRequestStatus = null
                                }
                            ) {
                                Text("Закрыть")
                            }
                        }
                    },
                    dismissButton = null,
                    shape = RoundedCornerShape(20.dp)
                )
            }

            // ========================================================================
            // 🎯 ДИАЛОГ ДЕТАЛЕЙ МАРКЕРА-СОБЫТИЯ
            // ========================================================================
            if (selectedEventMarker != null) {
                MarkerDetailDialog(
                    marker = selectedEventMarker!!,
                    currentUserId = currentUserId,
                    onDismiss = { selectedEventMarker = null },
                    onJoin = {
                        coroutineScope.launch {
                            try {
                                viewModel.joinMarker(selectedEventMarker!!.id)
                                selectedEventMarker = selectedEventMarker?.copy(
                                    participants = selectedEventMarker!!.participants + currentUserId
                                )
                                friendRequestMessage = "Вы участвуете! ✅"
                                delay(2000)
                                friendRequestMessage = null
                            } catch (e: Exception) {
                                friendRequestMessage = "Ошибка присоединения"
                            }
                        }
                    },
                    onLeave = {
                        coroutineScope.launch {
                            try {
                                viewModel.leaveMarker(selectedEventMarker!!.id)
                                selectedEventMarker = selectedEventMarker?.copy(
                                    participants = selectedEventMarker!!.participants - currentUserId
                                )
                                friendRequestMessage = "Вы вышли"
                                delay(2000)
                                friendRequestMessage = null
                            } catch (e: Exception) {
                                println("DEBUG: Error leaving marker: ${e.message}")
                            }
                        }
                    },
                    onOpenChat = {
                        selectedEventMarker?.chatId?.let { chatId ->
                            navController.navigate("chat_detail/$chatId/${selectedEventMarker!!.id}")
                        }
                        selectedEventMarker = null
                    },
                    onReport = { showMarkerReportDialog = true }
                )
            }

            // ========================================================================
            // 🚩 ДИАЛОГ РЕПОРТА
            // ========================================================================
            if (showMarkerReportDialog && selectedEventMarker != null) {
                AlertDialog(
                    onDismissRequest = { showMarkerReportDialog = false },
                    title = { Text("Пожаловаться", color = MaterialTheme.colorScheme.error) },
                    text = { Text("Почему вы жалуетесь на эту метку?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.reportMarker(
                                        selectedEventMarker!!.id,
                                        "Пользователь пожаловался"
                                    )
                                    showMarkerReportDialog = false
                                    selectedEventMarker = null
                                    friendRequestMessage = "Жалоба отправлена"
                                    delay(2000)
                                    friendRequestMessage = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Отправить")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showMarkerReportDialog = false }) {
                            Text("Отмена")
                        }
                    }
                )
            }

            // ========================================================================
            // 🎨 ДИАЛОГ СОЗДАНИЯ МАРКЕРА (появляется при долгом нажатии)
            // ========================================================================
            if (showCreateMarkerDialog && createMarkerLocation != null) {
                println("DEBUG: Showing CreateMarkerDialog at ${createMarkerLocation!!.latitude}, ${createMarkerLocation!!.longitude}")
                CreateMarkerDialog(
                    initialLocation = createMarkerLocation!!,
                    onDismiss = {
                        println("DEBUG: CreateMarkerDialog dismissed")
                        showCreateMarkerDialog = false
                        createMarkerLocation = null
                    },
                    onCreate = { marker ->
                        println("DEBUG: ========================================")
                        println("DEBUG: MapScreen - СОЗДАНИЕ МАРКЕРА")
                        println("DEBUG: Тип: ${marker.type}")
                        println("DEBUG: Название: ${marker.title}")
                        println("DEBUG: Координаты: ${marker.latitude}, ${marker.longitude}")
                        println("DEBUG: Видимость: ${marker.visibility}")
                        println("DEBUG: createdBy: ${marker.createdBy}")
                        println("DEBUG: chatId: ${marker.chatId}")
                        println("DEBUG: ========================================")

                        coroutineScope.launch {
                            try {
                                val newId = viewModel.createMarker(marker)
                                println("DEBUG: MapScreen - Маркер создан успешно! ID: $newId")
                                showCreateMarkerDialog = false
                                createMarkerLocation = null
                                friendRequestMessage = "Метка создана! ✅ (ID: ${newId.take(8)}...)"
                                delay(3000)
                                friendRequestMessage = null
                            } catch (e: Exception) {
                                println("DEBUG: MapScreen - ОШИБКА создания маркера:")
                                println("DEBUG: ${e.javaClass.name}: ${e.message}")
                                e.printStackTrace()
                                friendRequestMessage = "Ошибка: ${e.message}"
                                delay(3000)
                                friendRequestMessage = null
                            }
                        }
                    }
                )
            }

            // ========================================================================
            // 🔔 УВЕДОМЛЕНИЕ
            // ========================================================================
            if (friendRequestMessage != null) {
                LaunchedEffect(friendRequestMessage) {
                    delay(3000)
                    friendRequestMessage = null
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .shadow(6.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.inverseOnSurface
                        )
                        Text(
                            text = friendRequestMessage!!,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        IconButton(
                            onClick = { friendRequestMessage = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Закрыть",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }

}