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

import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.first

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.graphicsLayer

// ============================================================================
// 🎨 ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ ОТРИСОВКИ МАРКЕРОВ ПОЛЬЗОВАТЕЛЕЙ
// ============================================================================

private fun drawUserName(
    canvas: Canvas, name: String, totalWidth: Int, avatarSize: Int, avatarY: Float
) {
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = AndroidColor.BLACK
        textSize = 30f
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
        setShadowLayer(4f, 2f, 2f, AndroidColor.WHITE)
    }
    val textY = avatarY + avatarSize + 40f
    val displayName = if (name.length > 18) name.take(16) + "…" else name
    canvas.drawText(displayName, totalWidth / 2f, textY, textPaint)
}

private fun drawPointer(canvas: Canvas, totalWidth: Int, totalHeight: Int, markerColor: Int) {
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
    canvas: Canvas, name: String, markerColor: Int, totalWidth: Int, totalHeight: Int, avatarSize: Int
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
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }
    val initial = name.take(1).uppercase()
    val textY = avatarY + radius - (letterPaint.descent() + letterPaint.ascent()) / 2
    canvas.drawText(initial, centerX, textY, letterPaint)
    drawUserName(canvas, name, totalWidth, avatarSize, avatarY)
    drawPointer(canvas, totalWidth, totalHeight, markerColor)
}

private fun drawMarkerContent(
    canvas: Canvas, avatarBitmap: Bitmap, name: String, markerColor: Int,
    totalWidth: Int, totalHeight: Int, avatarSize: Int
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
    val clipPath = Path().apply { addCircle(centerX, avatarY + radius, radius, Path.Direction.CW) }
    canvas.clipPath(clipPath)
    canvas.drawBitmap(avatarBitmap, centerX - radius - 2, avatarY - 2, null)
    canvas.restore()
    drawUserName(canvas, name, totalWidth, avatarSize, avatarY)
    drawPointer(canvas, totalWidth, totalHeight, markerColor)
}

// ============================================================================
// 🌍 УЛУЧШЕННОЕ ОБРАТНОЕ ГЕОКОДИРОВАНИЕ
// ============================================================================

private suspend fun reverseGeocode(
    context: android.content.Context, latitude: Double, longitude: Double
): String = withContext(Dispatchers.IO) {
    try {
        if (!Geocoder.isPresent()) {
            return@withContext formatCoordinates(latitude, longitude)
        }
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        if (addresses.isNullOrEmpty()) {
            return@withContext formatCoordinates(latitude, longitude)
        }
        val addr = addresses[0]
        val parts = mutableListOf<String>()
        val street = addr.thoroughfare
        val house = addr.subThoroughfare
        if (!street.isNullOrEmpty()) {
            val streetWithHouse = if (!house.isNullOrEmpty()) "$street, $house" else street
            parts.add(streetWithHouse)
        }
        val district = addr.subLocality ?: addr.locality
        if (!district.isNullOrEmpty() && district != street) {
            parts.add(district)
        }
        val city = addr.adminArea ?: addr.countryName
        if (!city.isNullOrEmpty() && city != district) {
            parts.add(city)
        }
        if (parts.isNotEmpty()) {
            parts.joinToString(", ")
        } else {
            formatCoordinates(latitude, longitude)
        }
    } catch (e: Exception) {
        formatCoordinates(latitude, longitude)
    }
}

private fun formatCoordinates(latitude: Double, longitude: Double): String {
    return "${"%.4f".format(latitude)}, ${"%.4f".format(longitude)}"
}

// ============================================================================
// 🔹 ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ
// ============================================================================

private enum class FriendRequestStatus { NONE, ALREADY_SENT, INCOMING_PENDING, ALREADY_FRIENDS }

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
// 🎨 UI: ДИАЛОГ СПИСКА УЧАСТНИКОВ
// ============================================================================

@Composable
fun ParticipantsDialog(
    marker: MapMarker,
    ydbRepository: com.example.devpath.data.repository.YdbRepository,
    onDismiss: () -> Unit
) {
    var participants by remember { mutableStateOf<List<com.example.devpath.domain.models.UserProfile>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(marker) {
        val profiles = mutableListOf<com.example.devpath.domain.models.UserProfile>()
        marker.participants.forEach { userId ->
            val user = ydbRepository.getUser(userId)
            if (user != null) {
                profiles.add(
                    com.example.devpath.domain.models.UserProfile(
                        userId = userId,
                        name = user.optJSONObject("name")?.optString("S", "") ?: "",
                        email = user.optJSONObject("email")?.optString("S", "") ?: "",
                        avatarUrl = user.optJSONObject("avatar_url")?.optString("S", "")?.ifEmpty { null }
                    )
                )
            }
        }
        participants = profiles
        isLoading = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Outlined.People,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        "Участники",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${participants.size} человек",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Загрузка участников...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (participants.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.PeopleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            "Пока нет участников",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(participants) { participant ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                UserAvatar(
                                    avatarUrl = participant.avatarUrl,
                                    name = participant.name,
                                    size = 44,
                                    showOnlineIndicator = false,
                                    isOnline = false
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        participant.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (participant.userId == marker.createdBy) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                Icons.Outlined.Star,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                "Создатель",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        },
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 6.dp
    )
}

// ============================================================================
// 🎨 UI: УЛУЧШЕННЫЙ ДИАЛОГ ДЕТАЛЕЙ МАРКЕРА
// ============================================================================

@Composable
fun MarkerDetailDialog(
    marker: MapMarker,
    currentUserId: String,
    ydbRepository: com.example.devpath.data.repository.YdbRepository,
    onDismiss: () -> Unit,
    onJoin: () -> Unit,
    onLeave: () -> Unit,
    onDelete: () -> Unit,
    onOpenChat: () -> Unit,
    onReport: () -> Unit,
    onShowParticipants: () -> Unit
) {
    val isCreator = marker.createdBy == currentUserId
    val isParticipant = marker.isParticipant(currentUserId)

    // Состояния для анимации присоединения
    var isJoining by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Сброс состояния при изменении маркера
    LaunchedEffect(marker.id) {
        isJoining = false
        showSuccess = false
    }

    AlertDialog(
        onDismissRequest = {
            if (!isJoining) onDismiss()
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = when (marker.type) {
                        MarkerType.ANNOUNCEMENT -> MaterialTheme.colorScheme.primary
                        MarkerType.EVENT -> MaterialTheme.colorScheme.secondary
                        MarkerType.DISCUSSION -> MaterialTheme.colorScheme.tertiary
                    }.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isJoining) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = when (marker.type) {
                                    MarkerType.EVENT -> MaterialTheme.colorScheme.secondary
                                    MarkerType.DISCUSSION -> MaterialTheme.colorScheme.tertiary
                                    else -> MaterialTheme.colorScheme.primary
                                }
                            )
                        } else if (showSuccess) {
                            Icon(
                                Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = Color(0xFF4CAF50)
                            )
                        } else {
                            Icon(
                                imageVector = when (marker.type) {
                                    MarkerType.ANNOUNCEMENT -> Icons.Outlined.Campaign
                                    MarkerType.EVENT -> Icons.Outlined.Event
                                    MarkerType.DISCUSSION -> Icons.Outlined.Chat
                                },
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = when (marker.type) {
                                    MarkerType.ANNOUNCEMENT -> MaterialTheme.colorScheme.primary
                                    MarkerType.EVENT -> MaterialTheme.colorScheme.secondary
                                    MarkerType.DISCUSSION -> MaterialTheme.colorScheme.tertiary
                                }
                            )
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        marker.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        when (marker.type) {
                            MarkerType.ANNOUNCEMENT -> "📢 Объявление"
                            MarkerType.EVENT -> "🎉 Событие"
                            MarkerType.DISCUSSION -> "💬 Обсуждение"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Прогресс-бар присоединения
                if (isJoining) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Присоединяемся...",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = MaterialTheme.colorScheme.secondary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }

                // Сообщение об успехе
                if (showSuccess && !isJoining) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF4CAF50).copy(alpha = 0.15f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = Color(0xFF4CAF50)
                            )
                            Column {
                                Text(
                                    "Вы присоединились! 🎉",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                                Text(
                                    "Теперь вы можете перейти в чат",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF2E7D32).copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }

                // Описание
                if (marker.description.isNotEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        Text(
                            marker.description,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Таймер
                marker.endsAt?.let { endsAt ->
                    val timeLeft = getTimeLeftString(endsAt)
                    val isExpired = marker.isExpired
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isExpired)
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        else
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Timer,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                                tint = if (isExpired)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    if (isExpired) "Завершено" else "Осталось времени",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    timeLeft,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isExpired)
                                        MaterialTheme.colorScheme.error
                                    else
                                        MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                } ?: run {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Outlined.AllInclusive,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Бессрочно",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Участники (для EVENT)
                if (marker.type == MarkerType.EVENT) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onShowParticipants() },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Outlined.People,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Участники",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "${marker.participantCount}" + (marker.participantLimit?.let { " / $it" } ?: ""),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Icon(
                                Icons.Outlined.ChevronRight,
                                contentDescription = "Посмотреть список",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Статус участия
                if (marker.type != MarkerType.ANNOUNCEMENT && !isJoining && !showSuccess) {
                    val statusText = when {
                        isParticipant -> "✅ Вы участвуете"
                        marker.type == MarkerType.EVENT && marker.isFull -> "❌ Мест нет"
                        marker.type == MarkerType.EVENT && marker.isExpired -> "⏰ Событие завершено"
                        marker.isExpired -> "⏰ Завершено"
                        else -> "Можно присоединиться"
                    }
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = when {
                            isParticipant -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            marker.isFull || marker.isExpired -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                            else -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                        }
                    ) {
                        Text(
                            statusText,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = when {
                                isParticipant -> MaterialTheme.colorScheme.primary
                                marker.isFull || marker.isExpired -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.secondary
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (marker.type) {
                    MarkerType.ANNOUNCEMENT -> {
                        if (isCreator) {
                            Button(
                                onClick = onDelete,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Outlined.Delete, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Удалить метку")
                            }
                        }
                        TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                            Text("Закрыть")
                        }
                    }

                    MarkerType.EVENT, MarkerType.DISCUSSION -> {
                        // Кнопка "Перейти в чат" — показывается сразу после успешного присоединения
                        if (isParticipant || showSuccess) {
                            Button(
                                onClick = onOpenChat,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary
                                )
                            ) {
                                Icon(Icons.Outlined.Chat, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Перейти в чат")
                            }
                            OutlinedButton(
                                onClick = onLeave,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Outlined.ExitToApp, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Выйти")
                            }
                        } else if (!isJoining && !showSuccess) {
                            val canJoin = when (marker.type) {
                                MarkerType.EVENT -> !marker.isFull && !marker.isExpired
                                else -> !marker.isExpired
                            }
                            if (canJoin) {
                                Button(
                                    onClick = {
                                        isJoining = true
                                        coroutineScope.launch {
                                            // Вызываем присоединение
                                            onJoin()
                                            // Имитируем задержку для анимации
                                            delay(1500)
                                            isJoining = false
                                            showSuccess = true
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !isJoining,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    )
                                ) {
                                    if (isJoining) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.onSecondary
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text("Присоединение...")
                                    } else {
                                        Icon(Icons.Outlined.Add, null, Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Присоединиться")
                                    }
                                }
                            }
                        }

                        // Кнопка удаления для создателя
                        if (isCreator) {
                            OutlinedButton(
                                onClick = onDelete,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Outlined.Delete, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Удалить метку")
                            }
                        }

                        TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                            Text("Закрыть")
                        }
                    }
                }
            }
        },
        dismissButton = null,
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 8.dp
    )
}

// ============================================================================
// 🎨 UI: ДИАЛОГ ПОДТВЕРЖДЕНИЯ УДАЛЕНИЯ
// ============================================================================

@Composable
fun DeleteMarkerConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Outlined.Warning,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                "Удалить метку?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                "Это действие нельзя отменить. Все данные метки будут потеряны.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Outlined.Delete, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Удалить")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Отмена")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

// ============================================================================
// 🎨 UI: ДИАЛОГ СОЗДАНИЯ МАРКЕРА
// ============================================================================

@Composable
fun CreateMarkerDialog(
    initialLocation: Point,
    currentUserId: String,
    onDismiss: () -> Unit,
    onCreate: (MapMarker) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(MarkerType.ANNOUNCEMENT) }
    var endsAt by remember { mutableStateOf<java.util.Date?>(null) }
    var visibility by remember { mutableStateOf("public") }
    var participantLimit by remember { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Outlined.AddLocation,
                            null,
                            Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Text("Создать метку", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Тип метки
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            "Тип метки",
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = selectedType == MarkerType.ANNOUNCEMENT,
                                onClick = { selectedType = MarkerType.ANNOUNCEMENT },
                                label = { Text("📢 Объявление") },
                                modifier = Modifier.weight(1f),
                                leadingIcon = if (selectedType == MarkerType.ANNOUNCEMENT) {
                                    { Icon(Icons.Outlined.Check, null, Modifier.size(16.dp)) }
                                } else null
                            )
                            FilterChip(
                                selected = selectedType == MarkerType.EVENT,
                                onClick = { selectedType = MarkerType.EVENT },
                                label = { Text("🎉 Событие") },
                                modifier = Modifier.weight(1f),
                                leadingIcon = if (selectedType == MarkerType.EVENT) {
                                    { Icon(Icons.Outlined.Check, null, Modifier.size(16.dp)) }
                                } else null
                            )
                            FilterChip(
                                selected = selectedType == MarkerType.DISCUSSION,
                                onClick = { selectedType = MarkerType.DISCUSSION },
                                label = { Text("💬 Обсуждение") },
                                modifier = Modifier.weight(1f),
                                leadingIcon = if (selectedType == MarkerType.DISCUSSION) {
                                    { Icon(Icons.Outlined.Check, null, Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }

                // Название
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название *") },
                    placeholder = { Text("Введите название метки") },
                    leadingIcon = { Icon(Icons.Outlined.Title, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Описание
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание") },
                    placeholder = { Text("Опишите вашу метку") },
                    leadingIcon = { Icon(Icons.Outlined.Description, null) },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp)
                )

                // Таймер
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Outlined.Timer, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                            Text(
                                "Время активности",
                                fontWeight = FontWeight.Medium,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "1ч" to 1, "5ч" to 5, "12ч" to 12,
                                "24ч" to 24, "7д" to 168, "∞" to -1
                            ).forEach { (label, hours) ->
                                FilterChip(
                                    selected = when {
                                        hours == -1 && endsAt == null -> true
                                        hours != -1 && endsAt != null ->
                                            Math.abs(endsAt!!.time - (System.currentTimeMillis() + hours * 3_600_000L)) < 60_000
                                        else -> false
                                    },
                                    onClick = {
                                        endsAt = if (hours == -1) null
                                        else java.util.Date(System.currentTimeMillis() + hours * 3_600_000L)
                                    },
                                    label = { Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Видимость
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Outlined.Visibility, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                            Text(
                                "Кто видит",
                                fontWeight = FontWeight.Medium,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("public" to "🌍 Все", "friends" to "👥 Друзья", "private" to "🔒 Только я").forEach { (value, label) ->
                                FilterChip(
                                    selected = visibility == value,
                                    onClick = { visibility = value },
                                    label = { Text(label) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Лимит участников
                if (selectedType == MarkerType.EVENT) {
                    OutlinedTextField(
                        value = participantLimit?.toString() ?: "",
                        onValueChange = { participantLimit = it.toIntOrNull() },
                        label = { Text("Лимит участников") },
                        placeholder = { Text("Без ограничений") },
                        leadingIcon = { Icon(Icons.Outlined.People, null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val marker = MapMarker(
                        id = "",
                        type = selectedType,
                        title = title.ifEmpty { "Без названия" },
                        description = description,
                        createdBy = currentUserId,
                        createdAt = com.google.firebase.Timestamp.now(),
                        endsAt = endsAt?.let { com.google.firebase.Timestamp(it) },
                        latitude = initialLocation.latitude,
                        longitude = initialLocation.longitude,
                        visibility = visibility,
                        participantLimit = if (selectedType == MarkerType.EVENT) participantLimit else null,
                        chatId = if (selectedType == MarkerType.EVENT || selectedType == MarkerType.DISCUSSION) "auto" else null
                    )
                    onCreate(marker)
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Outlined.Check, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Создать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Отмена")
            }
        },
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 8.dp
    )
}

// ============================================================================
// 🗺️ COMPOSABLE: MapScreen
// ============================================================================

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
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


    var selectedUser by remember { mutableStateOf<UserLocation?>(null) }
    var showUserDialog by remember { mutableStateOf(false) }

    var cameraAddress by remember { mutableStateOf<String?>(null) }
    var isAddressLoading by remember { mutableStateOf(false) }
    var geocodeJob by remember { mutableStateOf<Job?>(null) }

    var showCreateMarkerDialog by remember { mutableStateOf(false) }
    var createMarkerLocation by remember { mutableStateOf<Point?>(null) }
    var selectedEventMarker by remember { mutableStateOf<MapMarker?>(null) }
    var showMarkerReportDialog by remember { mutableStateOf(false) }
    var showParticipantsDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    val prefs = context.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
    val currentUserId = prefs.getString("user_id", "") ?: ""
    val currentUserName = prefs.getString("user_name", "Пользователь") ?: "Пользователь"

    val ydbRepository = remember { com.example.devpath.data.repository.YdbRepository() }

    val iconFactory = remember { MarkerIconFactory(context) }
    val markerManager = remember { mutableStateOf<MarkerManager?>(null) }
    var isMapReady by remember { mutableStateOf(false) }
    var isMapLoading by remember { mutableStateOf(true) }
    var cameraSetForCurrentLocation by remember { mutableStateOf(false) }

    var showSettingsDialog by remember { mutableStateOf(false) }
    var localVisibility by remember { mutableStateOf("all") }
    var localSelectedFriends by remember { mutableStateOf<List<String>>(emptyList()) }

    var animationStartTime by remember { mutableStateOf(0L) }

    fun updateCameraAddress(latitude: Double, longitude: Double) {
        geocodeJob?.cancel()
        geocodeJob = geocodeScope.launch {
            delay(300L)
            isAddressLoading = true
            cameraAddress = null
            try {
                cameraAddress = reverseGeocode(context, latitude, longitude)
            } catch (e: Exception) {
                cameraAddress = formatCoordinates(latitude, longitude)
            } finally {
                isAddressLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionState.launchPermissionRequest()
        if (locationPermissionState.status.isGranted) {
            viewModel.startLocationUpdates(currentUserId, currentUserName, null)
            viewModel.loadNearbyUsers(currentUserId)
        }

        // Запоминаем время старта анимации
        animationStartTime = System.currentTimeMillis()
    }

    LaunchedEffect(currentLocation) {
        currentLocation?.let { loc ->
            viewModel.loadNearbyMarkers(currentUserId, loc.latitude, loc.longitude)
        }
    }

    val inputListener = remember {
        object : InputListener {
            override fun onMapTap(map: Map, point: Point) {
                selectedEventMarker = null
                selectedUser = null
                showUserDialog = false
            }
            override fun onMapLongTap(map: Map, point: Point) {
                createMarkerLocation = point
                showCreateMarkerDialog = true
            }
        }
    }

    val cameraListener = remember {
        object : CameraListener {
            override fun onCameraPositionChanged(
                map: Map,
                cameraPosition: CameraPosition,
                cameraUpdateReason: com.yandex.mapkit.map.CameraUpdateReason,
                finished: Boolean
            ) {
                if (finished) {
                    updateCameraAddress(cameraPosition.target.latitude, cameraPosition.target.longitude)
                }
            }
        }
    }

    // Инициализация карты
    LaunchedEffect(mapView) {
        mapView.apply {
            map.isRotateGesturesEnabled = true
            map.isScrollGesturesEnabled = true
            map.isTiltGesturesEnabled = true
            map.isZoomGesturesEnabled = true
            map.addCameraListener(cameraListener)
            map.addInputListener(inputListener)
        }

        markerManager.value = MarkerManager(mapView, iconFactory,
            onUserTap = { userId, name, avatarUrl, lat, lon, isOnline ->
                selectedUser = UserLocation(
                    userId = userId, name = name, avatarUrl = avatarUrl,
                    latitude = lat, longitude = lon, isOnline = isOnline,
                    lastUpdated = System.currentTimeMillis()
                )
                showUserDialog = true
            },
            onEventTap = { markerId ->
                selectedEventMarker = nearbyMarkers.find { it.id == markerId }
            }
        )

        // ЖДЁМ ЛОКАЦИЮ
        if (currentLocation != null) {
            val loc = currentLocation!!
            mapView.map.move(
                CameraPosition(Point(loc.latitude, loc.longitude), 16.0f, 0.0f, 0.0f),
                Animation(Animation.Type.SMOOTH, 1.0f),
                null
            )
            markerManager.value?.setInitialCamera(loc.latitude, loc.longitude)
            updateCameraAddress(loc.latitude, loc.longitude)
            cameraSetForCurrentLocation = true
            isMapReady = true
        } else {
            snapshotFlow { currentLocation }
                .first { it != null }
                .let { loc ->
                    if (loc != null) {
                        delay(300)
                        mapView.map.move(
                            CameraPosition(Point(loc.latitude, loc.longitude), 16.0f, 0.0f, 0.0f),
                            Animation(Animation.Type.SMOOTH, 1.0f),
                            null
                        )
                        markerManager.value?.setInitialCamera(loc.latitude, loc.longitude)
                        updateCameraAddress(loc.latitude, loc.longitude)
                        cameraSetForCurrentLocation = true
                    }
                }
            isMapReady = true
        }

        // Гарантируем минимум 5 секунд анимации
        val elapsedSinceStart = System.currentTimeMillis() - animationStartTime
        val minAnimationTime = 2500L
        val remainingTime = minAnimationTime - elapsedSinceStart

        if (remainingTime > 0) {
            delay(remainingTime)
        }

        isMapLoading = false
    }

    // Периодическая подгрузка маркеров (каждые 30 секунд)
    LaunchedEffect(isMapReady) {
        if (!isMapReady) return@LaunchedEffect
        while (true) {
            delay(30_000)
            currentLocation?.let { loc ->
                viewModel.loadNearbyMarkers(currentUserId, loc.latitude, loc.longitude)
            }
        }
    }

    // Обновление маркеров на карте
    LaunchedEffect(isMapReady) {
        if (!isMapReady || markerManager.value == null) return@LaunchedEffect
        snapshotFlow { Triple(nearbyUsers, nearbyMarkers, currentLocation) }
            .debounce(300)
            .collect { (users, markers, location) ->
                val userList = users.map { user ->
                    UserLocationData(
                        user.userId, user.name, user.avatarUrl,
                        user.latitude, user.longitude, user.isOnline
                    )
                }.toMutableList()
                location?.let { loc ->
                    userList.add(
                        UserLocationData(
                            currentUserId,
                            currentUserProfile?.name ?: "Я",
                            currentUserProfile?.avatarUrl,
                            loc.latitude, loc.longitude, true
                        )
                    )
                }
                markerManager.value?.updateUsers(userList, currentUserId)
                val eventList = markers
                    .filter { it.status == "active" && !it.isExpired }
                    .map { marker ->
                        EventMarkerData(
                            marker.id, marker.type.name, marker.title,
                            marker.latitude, marker.longitude
                        )
                    }
                markerManager.value?.updateEvents(eventList)
            }
    }


    LaunchedEffect(showSettingsDialog) {
        if (showSettingsDialog) {
            // Даем время на получение актуальных данных из ViewModel
            delay(100)

            // Получаем актуальные настройки через snapshotFlow
            val currentSettings = snapshotFlow { locationSettings }.first()

            println("DEBUG: SettingsDialog - открыт. visibility=${currentSettings.visibility}, friends=${currentSettings.selectedFriends}")

            localVisibility = currentSettings.visibility

            // Если выбран режим "друзья" и список пустой, автоматически выбираем всех друзей
            if (currentSettings.visibility == "friends" && currentSettings.selectedFriends.isEmpty()) {
                localSelectedFriends = friends.map { it.userId }
            } else {
                localSelectedFriends = currentSettings.selectedFriends.toList()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            geocodeJob?.cancel()
            geocodeScope.cancel()
            markerManager.value?.clear()
            iconFactory.clearCache()
            try {
                mapView.map.removeCameraListener(cameraListener)
                mapView.map.removeInputListener(inputListener)
            } catch (_: Exception) {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (locationPermissionState.status.isGranted) {
            key("map_view") {
                AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
            }

            if (isMapLoading) {
                // Анимированные состояния
                var animationProgress by remember { mutableStateOf(0f) }
                var currentMessageIndex by remember { mutableStateOf(0) }

                val loadingMessages = listOf(
                    "Сканируем горизонт..." to "🔭",
                    "Прокладываем маршруты..." to "🧭",
                    "Ищем попутчиков..." to "👥",
                    "Загружаем карту..." to "🗺️",
                    "Настраиваем локацию..." to "📍",
                    "Почти готово..." to "✨"
                )

                // Анимация прогресса
                LaunchedEffect(Unit) {
                    val startTime = System.currentTimeMillis()
                    val duration = 3000L

                    while (isMapLoading) {
                        val elapsed = System.currentTimeMillis() - startTime
                        animationProgress = (elapsed.toFloat() / duration).coerceIn(0f, 1f)
                        currentMessageIndex = ((elapsed / 500) % loadingMessages.size).toInt()
                        delay(16)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFF8F9FA),
                                    Color(0xFFE3F2FD),
                                    Color(0xFFF3E5F5),
                                    Color(0xFFF8F9FA)
                                )
                            )
                        )
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    // Декоративные круги на фоне
                    repeat(4) { index ->
                        val size = (100 + index * 80).dp
                        val offsetX = when (index % 2) {
                            0 -> -80.dp
                            else -> 80.dp
                        }
                        val offsetY = when (index / 2) {
                            0 -> -100.dp
                            else -> 100.dp
                        }

                        Box(
                            modifier = Modifier
                                .offset(x = offsetX, y = offsetY)
                                .size(size)
                                .graphicsLayer {
                                    alpha = 0.04f + (animationProgress * 0.04f)
                                    scaleX = 0.9f + (animationProgress * 0.2f)
                                    scaleY = 0.9f + (animationProgress * 0.2f)
                                }
                                .background(
                                    if (index % 2 == 0) Color(0xFF42A5F5)
                                    else Color(0xFFAB47BC),
                                    CircleShape
                                )
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        // Анимированная иконка
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .graphicsLayer {
                                    rotationZ = animationProgress * 360f
                                    scaleX = 0.85f + (animationProgress * 0.15f)
                                    scaleY = 0.85f + (animationProgress * 0.15f)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            // Внешнее кольцо
                            Surface(
                                modifier = Modifier.size(130.dp),
                                shape = CircleShape,
                                color = Color.White,
                                shadowElevation = 12.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    // Среднее кольцо
                                    Surface(
                                        modifier = Modifier.size(95.dp),
                                        shape = CircleShape,
                                        color = Color(0xFFF0F4FF),
                                        border = BorderStroke(2.dp, Color(0xFF42A5F5).copy(alpha = 0.3f))
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            // Внутренний круг с иконкой
                                            Surface(
                                                modifier = Modifier.size(65.dp),
                                                shape = CircleShape,
                                                color = Color(0xFFE3F2FD),
                                                border = BorderStroke(2.dp, Color(0xFF42A5F5))
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = loadingMessages[currentMessageIndex].second,
                                                        fontSize = 30.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Декоративные точки по кругу
                            repeat(8) { index ->
                                val angle = Math.toRadians((index * 45.0 + (animationProgress * 360.0)))
                                val radius = 60f
                                val x = (Math.cos(angle) * radius).toFloat()
                                val y = (Math.sin(angle) * radius).toFloat()

                                Box(
                                    modifier = Modifier
                                        .offset(x.dp, y.dp)
                                        .size(8.dp)
                                        .background(
                                            Color(0xFF42A5F5).copy(alpha = 0.8f),
                                            CircleShape
                                        )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(40.dp))

                        // Анимированный текст
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.animateContentSize()
                        ) {
                            Text(
                                text = loadingMessages[currentMessageIndex].first,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                color = Color(0xFF1565C0),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "DevPath Map",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = 3.sp
                                ),
                                color = Color(0xFF7B1FA2).copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Светлый прогресс-бар
                        Box(
                            modifier = Modifier
                                .width(220.dp)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE0E0E0))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(fraction = animationProgress)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFF42A5F5),
                                                Color(0xFF7E57C2),
                                                Color(0xFFEC407A)
                                            )
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Процент загрузки
                        Text(
                            text = "${(animationProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = Color(0xFF1565C0),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Анимированные точки в конце
                        if (animationProgress > 0.6f) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(3) { index ->
                                    val active = ((System.currentTimeMillis() / 250 + index) % 3 == 0L)

                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .graphicsLayer {
                                                alpha = if (active) 1f else 0.3f
                                                translationY = if (active) -6.dp.toPx() else 0f
                                            }
                                            .background(
                                                if (active) Color(0xFF42A5F5)
                                                else Color(0xFFB0BEC5),
                                                CircleShape
                                            )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Загружаем карту...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF78909C),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            if (!isMapLoading) {
                // Кнопка назад
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.TopStart).padding(16.dp).shadow(4.dp, CircleShape).background(MaterialTheme.colorScheme.surface, CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, "Назад", tint = MaterialTheme.colorScheme.onSurface)
                }

                // Адрес
                if (cameraAddress != null || isAddressLoading) {
                    Card(
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 20.dp).padding(horizontal = 80.dp).shadow(4.dp, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (isAddressLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                            } else {
                                Icon(Icons.Outlined.LocationOn, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                            Text(
                                text = if (isAddressLoading) "Определение..." else cameraAddress ?: "",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface),
                                maxLines = 3, overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Кнопки управления
                Column(
                    modifier = Modifier.align(Alignment.TopEnd).padding(end = 16.dp, top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.size(48.dp).shadow(4.dp, CircleShape).background(MaterialTheme.colorScheme.surface, CircleShape)
                    ) { Icon(Icons.Outlined.Settings, "Настройки", tint = MaterialTheme.colorScheme.onSurface) }

                    IconButton(
                        onClick = {
                            currentLocation?.let { loc ->
                                mapView.map.move(
                                    CameraPosition(Point(loc.latitude, loc.longitude), 16.0f, 0.0f, 0.0f),
                                    Animation(Animation.Type.SMOOTH, 1.0f), null
                                )
                            }
                        },
                        modifier = Modifier.size(48.dp).shadow(4.dp, CircleShape).background(MaterialTheme.colorScheme.primary, CircleShape)
                    ) { Icon(Icons.Outlined.MyLocation, "Моё местоположение", tint = MaterialTheme.colorScheme.onPrimary) }

                    FloatingActionButton(
                        onClick = {
                            Toast.makeText(context, "Зажмите палец на карте, чтобы создать метку", Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier.shadow(4.dp, CircleShape),
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    ) { Icon(Icons.Outlined.AddLocation, "Создать метку") }
                }

                // Диалог настроек
                // Диалог настроек
                if (showSettingsDialog) {
                    AlertDialog(
                        onDismissRequest = {
                            // Сохраняем изменения при закрытии (свайп назад или клик вне диалога)
                            viewModel.updateLocationSettings(
                                LocationSettings(
                                    visibility = localVisibility,
                                    selectedFriends = if (localVisibility == "friends") localSelectedFriends else emptyList()
                                ),
                                currentUserId
                            )
                            showSettingsDialog = false
                        },
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
                            Column(Modifier.fillMaxWidth()) {
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = localVisibility == "all",
                                        onClick = {
                                            localVisibility = "all"
                                            localSelectedFriends = emptyList() // Очищаем список
                                        },
                                        label = { Text("Все") }
                                    )
                                    FilterChip(
                                        selected = localVisibility == "friends",
                                        onClick = {
                                            localVisibility = "friends"
                                            // Автоматически выбираем ВСЕХ друзей при переключении
                                            if (localSelectedFriends.isEmpty()) {
                                                localSelectedFriends = friends.map { it.userId }
                                            }
                                        },
                                        label = { Text("Друзья") }
                                    )
                                    FilterChip(
                                        selected = localVisibility == "nobody",
                                        onClick = {
                                            localVisibility = "nobody"
                                            localSelectedFriends = emptyList() // Очищаем список
                                        },
                                        label = { Text("Никто") }
                                    )
                                }

                                // Показываем список друзей только если выбрано "Друзья"
                                if (localVisibility == "friends") {
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        "Выберите друзей, которые будут видеть вас:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )

                                    if (friends.isEmpty()) {
                                        Text(
                                            "У вас пока нет друзей",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    } else {
                                        // Показываем всех друзей с возможностью отключить
                                        friends.forEach { friend ->
                                            Row(
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(
                                                    checked = localSelectedFriends.contains(friend.userId),
                                                    onCheckedChange = { checked ->
                                                        localSelectedFriends = if (checked) {
                                                            localSelectedFriends + friend.userId
                                                        } else {
                                                            localSelectedFriends - friend.userId
                                                        }
                                                    }
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Text(
                                                    friend.name,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                        }

                                        // Добавляем кнопку "Выбрать всех" / "Снять всех"
                                        Spacer(Modifier.height(8.dp))
                                        Row(
                                            Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            TextButton(
                                                onClick = {
                                                    localSelectedFriends = friends.map { it.userId }
                                                },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Выбрать всех", fontSize = 13.sp)
                                            }
                                            TextButton(
                                                onClick = {
                                                    localSelectedFriends = emptyList()
                                                },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Снять всех", fontSize = 13.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                // Сохраняем изменения и закрываем
                                viewModel.updateLocationSettings(
                                    LocationSettings(
                                        visibility = localVisibility,
                                        selectedFriends = if (localVisibility == "friends") localSelectedFriends else emptyList()
                                    ),
                                    currentUserId
                                )
                                showSettingsDialog = false
                            }) {
                                Text("Готово")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                // Закрываем без сохранения
                                showSettingsDialog = false
                            }) {
                                Text("Отмена")
                            }
                        },
                        shape = RoundedCornerShape(20.dp)
                    )
                }

                // Диалог пользователя
                if (showUserDialog && selectedUser != null) {
                    val user = selectedUser!!
                    val isCurrentUser = user.userId == currentUserId
                    val displayName = if (isCurrentUser) currentUserProfile?.name ?: user.name else user.name
                    val displayAvatarUrl = if (isCurrentUser) currentUserProfile?.avatarUrl else user.avatarUrl
                    val isFriend = friends.any { it.userId == user.userId }

                    AlertDialog(
                        onDismissRequest = { showUserDialog = false; selectedUser = null },
                        title = null,
                        text = {
                            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                UserAvatar(avatarUrl = displayAvatarUrl, name = displayName, size = 80, showOnlineIndicator = true, isOnline = user.isOnline)
                                Spacer(Modifier.height(16.dp))
                                Text(displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                Text(
                                    if (user.isOnline) "В сети" else "Был(а) недавно",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (user.isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        confirmButton = {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (isCurrentUser) Arrangement.Center else Arrangement.spacedBy(8.dp)) {
                                if (isFriend && !isCurrentUser) {
                                    Button(onClick = {
                                        coroutineScope.launch {
                                            val chatId = viewModel.getOrCreatePersonalChat(currentUserId, user.userId)
                                            navController.navigate("chat_detail/$chatId/${user.userId}")
                                            showUserDialog = false; selectedUser = null
                                        }
                                    }, modifier = Modifier.weight(1f)) { Text("Написать") }
                                }
                                if (!isFriend && !isCurrentUser) {
                                    Button(onClick = {
                                        viewModel.sendFriendRequest(currentUserId, user.userId)
                                        Toast.makeText(context, "Заявка отправлена", Toast.LENGTH_SHORT).show()
                                        showUserDialog = false; selectedUser = null
                                    }, modifier = Modifier.weight(1f)) { Text("В друзья") }
                                }
                                TextButton(onClick = { showUserDialog = false; selectedUser = null }) { Text("Закрыть") }
                            }
                        },
                        shape = RoundedCornerShape(20.dp)
                    )
                }

// Диалог маркера
                if (selectedEventMarker != null && !showParticipantsDialog && !showDeleteConfirmDialog) {
                    MarkerDetailDialog(
                        marker = selectedEventMarker!!,
                        currentUserId = currentUserId,
                        ydbRepository = ydbRepository,
                        onDismiss = { selectedEventMarker = null },
                        onJoin = {
                            coroutineScope.launch {
                                val markerId = selectedEventMarker!!.id
                                // Присоединяемся к маркеру
                                viewModel.joinMarker(markerId)

                                // Даём время на обновление данных в БД
                                delay(2000)

                                // Загружаем обновлённый маркер (теперь пользователь в participants)
                                val updatedMarker = viewModel.getMarker(markerId)
                                if (updatedMarker != null) {
                                    selectedEventMarker = updatedMarker

                                    // Гарантируем, что пользователь добавлен в чат
                                    if (updatedMarker.chatId != null && updatedMarker.chatId.isNotEmpty() && updatedMarker.chatId != "auto") {
                                        val chat = ydbRepository.getChat(updatedMarker.chatId)
                                        val participants = chat?.optJSONObject("participants")?.optJSONArray("SS")
                                        val isInChat = participants != null &&
                                                (0 until participants.length()).any { participants.getString(it) == currentUserId }

                                        if (!isInChat) {
                                            ydbRepository.addUserToChat(updatedMarker.chatId, currentUserId)
                                        }
                                    }
                                }
                            }
                        },
                        onLeave = {
                            coroutineScope.launch {
                                viewModel.leaveMarker(selectedEventMarker!!.id)
                                selectedEventMarker = null
                            }
                        },
                        onDelete = { showDeleteConfirmDialog = true },
                        onOpenChat = {
                            selectedEventMarker?.let { marker ->
                                coroutineScope.launch {
                                    val chatId = marker.chatId
                                    if (chatId != null && chatId.isNotEmpty() && chatId != "auto") {
                                        // Проверяем и добавляем пользователя в чат если нужно
                                        val chat = ydbRepository.getChat(chatId)
                                        val participants = chat?.optJSONObject("participants")?.optJSONArray("SS")
                                        val isInChat = participants != null &&
                                                (0 until participants.length()).any { participants.getString(it) == currentUserId }

                                        if (!isInChat) {
                                            ydbRepository.addUserToChat(chatId, currentUserId)
                                        }

                                        navController.navigate("chat_detail/$chatId/${marker.id}")
                                    }
                                    selectedEventMarker = null
                                }
                            }
                        },
                        onReport = { showMarkerReportDialog = true },
                        onShowParticipants = { showParticipantsDialog = true }
                    )
                }

// Диалог списка участников
                if (showParticipantsDialog && selectedEventMarker != null) {
                    ParticipantsDialog(
                        marker = selectedEventMarker!!,
                        ydbRepository = ydbRepository,
                        onDismiss = { showParticipantsDialog = false }
                    )
                }

// Диалог подтверждения удаления
                if (showDeleteConfirmDialog && selectedEventMarker != null) {
                    DeleteMarkerConfirmDialog(
                        onDismiss = { showDeleteConfirmDialog = false },
                        onConfirm = {
                            coroutineScope.launch {
                                val marker = selectedEventMarker
                                if (marker != null) {
                                    try {
                                        viewModel.deleteMarker(marker.id, marker.chatId)
                                        Toast.makeText(context, "✅ Метка и чат удалены", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "❌ Ошибка удаления: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                showDeleteConfirmDialog = false
                                selectedEventMarker = null
                            }
                        }
                    )
                }

// Диалог создания маркера
                if (showCreateMarkerDialog && createMarkerLocation != null) {
                    CreateMarkerDialog(
                        initialLocation = createMarkerLocation!!,
                        currentUserId = currentUserId,
                        onDismiss = { showCreateMarkerDialog = false; createMarkerLocation = null },
                        onCreate = { marker ->
                            coroutineScope.launch {
                                val createdMarkerId = viewModel.createMarker(marker)
                                // Обновляем список маркеров
                                currentLocation?.let { loc ->
                                    viewModel.loadNearbyMarkers(currentUserId, loc.latitude, loc.longitude)
                                }
                                Toast.makeText(context, "Метка создана! 🎉", Toast.LENGTH_SHORT).show()
                                showCreateMarkerDialog = false
                                createMarkerLocation = null
                            }
                        }
                    )
                }
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Icon(Icons.Outlined.LocationOff, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
                    Text("Доступ к местоположению необходим", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Button(onClick = { locationPermissionState.launchPermissionRequest() }) { Text("Предоставить доступ") }
                }
            }
        }
    }
}