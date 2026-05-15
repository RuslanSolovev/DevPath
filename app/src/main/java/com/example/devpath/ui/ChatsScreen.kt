package com.example.devpath.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.devpath.data.repository.YdbRepository
import com.example.devpath.ui.components.UserAvatar
import com.example.devpath.ui.viewmodel.ChatsViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatsScreen(
    ydbRepository: YdbRepository,
    currentUserId: String,
    navController: NavHostController
) {
    val viewModel: ChatsViewModel = hiltViewModel()
    val chats by viewModel.chats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var chatToDelete by remember { mutableStateOf<com.example.devpath.domain.models.Chat?>(null) }

    // Гарантируем минимум 3 секунды анимации
    var minimumLoadingTimeReached by remember { mutableStateOf(false) }
    var dataLoaded by remember { mutableStateOf(false) }

    // Загрузка чатов при первом запуске
    LaunchedEffect(currentUserId) {
        viewModel.loadChats(currentUserId)
        dataLoaded = true
    }

    // Гарантируем минимум 3 секунды анимации
    LaunchedEffect(currentUserId) {
        delay(1500)
        minimumLoadingTimeReached = true
    }

    // Умный поллинг — запускаем и останавливаем при уходе с экрана
    DisposableEffect(currentUserId) {
        viewModel.startSmartPolling(currentUserId)
        onDispose {
            viewModel.stopChatListPolling()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        // Верхняя панель
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Chat,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    "Чаты",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            IconButton(
                onClick = { navController.navigate("friends") },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                Icon(
                    Icons.Outlined.People,
                    contentDescription = "Друзья",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Контент
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                // Показываем анимацию загрузки минимум 3 секунды
                !minimumLoadingTimeReached || (isLoading && !dataLoaded) -> {
                    LightLoadingScreen(minimumLoadingTimeReached)
                }

                // Пустой список чатов
                chats.isEmpty() && minimumLoadingTimeReached -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(80.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Outlined.Chat,
                                        contentDescription = null,
                                        modifier = Modifier.size(40.dp),
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                    )
                                }
                            }
                            Text(
                                "У вас пока нет чатов",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Добавьте друзей, чтобы начать общение",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = { navController.navigate("friends") },
                                modifier = Modifier.padding(top = 8.dp),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.PersonAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Найти друзей")
                            }
                        }
                    }
                }

                // Список чатов
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = chats.sortedByDescending { it.lastMessageTime },
                            key = { it.chatId }
                        ) { chat ->
                            var itemLoaded by remember { mutableStateOf(false) }

                            LaunchedEffect(chat.chatId) {
                                delay((50..200).random().toLong())
                                itemLoaded = true
                            }

                            if (itemLoaded) {
                                ChatItemModern(
                                    ydbRepository = ydbRepository,
                                    chat = chat,
                                    currentUserId = currentUserId,
                                    onClick = {
                                        val friendId = chat.participants.firstOrNull { it != currentUserId }
                                        if (friendId != null && chat.chatId.isNotBlank()) {
                                            navController.navigate("chat_detail/${chat.chatId}/$friendId")
                                        }
                                    },
                                    onLongClick = {
                                        chatToDelete = chat
                                        showDeleteDialog = true
                                    }
                                )
                            } else {
                                // Скелетон загрузки
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(72.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // Скелетон аватара
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    Brush.linearGradient(
                                                        colors = listOf(
                                                            MaterialTheme.colorScheme.surfaceVariant,
                                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                        )
                                                    )
                                                )
                                        )

                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // Скелетон имени
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(0.6f)
                                                    .height(16.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(
                                                        Brush.linearGradient(
                                                            colors = listOf(
                                                                MaterialTheme.colorScheme.surfaceVariant,
                                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                            )
                                                        )
                                                    )
                                            )
                                            // Скелетон сообщения
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(0.9f)
                                                    .height(12.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(
                                                        Brush.linearGradient(
                                                            colors = listOf(
                                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                                            )
                                                        )
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog && chatToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                chatToDelete = null
            },
            title = {
                Text(
                    "Удалить чат",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    "Вы уверены, что хотите удалить этот чат? Все сообщения будут потеряны.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        chatToDelete?.let { viewModel.deleteChat(it.chatId, currentUserId) }
                        showDeleteDialog = false
                        chatToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        chatToDelete = null
                    }
                ) {
                    Text("Отмена")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun LightLoadingScreen(minimumLoadingTimeReached: Boolean) {
    var animationProgress by remember { mutableStateOf(0f) }
    var currentEmojiIndex by remember { mutableStateOf(0) }
    var showContent by remember { mutableStateOf(false) }

    val chatMessages = listOf(
        "Загружаем сообщения..." to "💬",
        "Синхронизируем диалоги..." to "🔄",
        "Подгружаем контакты..." to "👥",
        "Расшифровываем секреты..." to "🔐",
        "Почти готово..." to "✨"
    )

    LaunchedEffect(Unit) {
        delay(200)
        showContent = true

        val startTime = System.currentTimeMillis()
        val duration = 1500L

        while (!minimumLoadingTimeReached) {
            val elapsed = System.currentTimeMillis() - startTime
            animationProgress = (elapsed.toFloat() / duration).coerceIn(0f, 1f)
            currentEmojiIndex = ((elapsed / 600) % chatMessages.size).toInt()
            delay(16)
        }

        animationProgress = 1f
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
            ),
        contentAlignment = Alignment.Center
    ) {
        // Декоративные круги на фоне
        repeat(4) { index ->
            val size = (80 + index * 60).dp
            val offsetX = when (index % 2) {
                0 -> -60.dp
                else -> 60.dp
            }
            val offsetY = when (index / 2) {
                0 -> -80.dp
                else -> 80.dp
            }

            Box(
                modifier = Modifier
                    .offset(x = offsetX, y = offsetY)
                    .size(size)
                    .graphicsLayer {
                        alpha = 0.05f + (animationProgress * 0.05f)
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

        if (showContent) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                // Анимированная иконка чата
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .graphicsLayer {
                            rotationY = animationProgress * 360f
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Внешнее кольцо
                    Surface(
                        modifier = Modifier.size(120.dp),
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 8.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            // Точки по кругу
                            repeat(3) { dotIndex ->
                                val angle = Math.toRadians((dotIndex * 120.0 + (animationProgress * 180.0)))
                                val radius = 42f
                                val x = (Math.cos(angle) * radius).toFloat()
                                val y = (Math.sin(angle) * radius).toFloat()

                                Box(
                                    modifier = Modifier
                                        .offset(x.dp, y.dp)
                                        .size(8.dp)
                                        .background(
                                            Color(0xFF42A5F5),
                                            CircleShape
                                        )
                                )
                            }

                            // Внутренний круг
                            Surface(
                                modifier = Modifier.size(70.dp),
                                shape = CircleShape,
                                color = Color(0xFFF0F4FF)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = chatMessages[currentEmojiIndex].second,
                                        fontSize = 32.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                // Текст сообщения
                Text(
                    text = chatMessages[currentEmojiIndex].first,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = Color(0xFF1565C0),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "DevPath Messenger",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 3.sp
                    ),
                    color = Color(0xFF7B1FA2).copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

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

                Spacer(modifier = Modifier.height(12.dp))

                // Процент
                Text(
                    text = "${(animationProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = Color(0xFF1565C0),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Анимированные точки
                if (animationProgress > 0.6f) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(3) { index ->
                            val active = ((System.currentTimeMillis() / 300 + index) % 4 != 0L)

                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .graphicsLayer {
                                        alpha = if (active) 1f else 0.3f
                                        translationY = if (active) -4.dp.toPx() else 0f
                                    }
                                    .background(
                                        if (active) Color(0xFF42A5F5)
                                        else Color(0xFFB0BEC5),
                                        CircleShape
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Загружаем...",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(0xFF78909C),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatItemModern(
    ydbRepository: YdbRepository,
    chat: com.example.devpath.domain.models.Chat,
    currentUserId: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    var friendAvatarUrl by remember { mutableStateOf<String?>(null) }
    var friendName by remember { mutableStateOf("") }
    var isFriendOnline by remember { mutableStateOf(false) }

    var chatAvatarJson by remember { mutableStateOf<String?>(null) }
    var markerType by remember { mutableStateOf<String?>(null) }
    var markerColor by remember { mutableStateOf<String?>(null) }
    var markerEmoji by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(chat.chatId) {
        if (chat.type == "personal") {
            val friendId = chat.participants.firstOrNull { it != currentUserId }
            if (friendId != null) {
                val friend = ydbRepository.getUser(friendId)
                friendAvatarUrl = friend?.optJSONObject("avatar_url")?.optString("S", "")?.ifEmpty { null }
                friendName = friend?.optJSONObject("name")?.optString("S", "") ?: chat.name
                val lastSeen = friend?.optJSONObject("last_seen")?.optString("S")?.toLongOrNull() ?: 0
                isFriendOnline = System.currentTimeMillis() - lastSeen < 120_000
            }
        } else {
            val chatData = ydbRepository.getChat(chat.chatId)
            chatAvatarJson = chatData?.optJSONObject("chat_avatar")?.optString("S")

            if (chatAvatarJson != null) {
                try {
                    val avatarObj = org.json.JSONObject(chatAvatarJson!!)
                    markerType = avatarObj.optString("marker_type", "")
                    markerColor = avatarObj.optString("color", "")
                    markerEmoji = avatarObj.optString("emoji", "")
                } catch (e: Exception) {
                    // ignore
                }
            }

            if (chat.name.isEmpty() && chatData != null) {
                friendName = chatData.optJSONObject("name")?.optString("S", "") ?: "Групповой чат"
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(56.dp)) {
                when {
                    chat.type == "personal" -> {
                        UserAvatar(
                            avatarUrl = friendAvatarUrl,
                            name = friendName.ifEmpty { chat.name },
                            size = 56,
                            showOnlineIndicator = true,
                            isOnline = isFriendOnline
                        )
                    }
                    markerColor != null -> {
                        val color = try {
                            android.graphics.Color.parseColor(markerColor)
                        } catch (e: Exception) {
                            android.graphics.Color.parseColor("#FF9800")
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color(color)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = markerEmoji ?: "🎉",
                                fontSize = 28.sp
                            )
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.Group,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = when {
                            chat.type == "personal" && friendName.isNotEmpty() -> friendName
                            chat.name.isNotEmpty() -> chat.name
                            else -> "Групповой чат"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = formatChatTime(chat.lastMessageTime),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (chat.lastMessageSender.isNotEmpty()) {
                        Text(
                            chat.lastMessageSender,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                        Text(
                            ": ",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        chat.lastMessage.ifEmpty { "Нет сообщений" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.ChevronRight,
                        "Открыть",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

private fun formatChatTime(timestamp: Long): String {
    if (timestamp <= 0) return ""
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val date = Date(timestamp)
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("dd.MM", Locale.getDefault())
    return when {
        diff < 60_000 -> "сейчас"
        diff < 3600_000 -> "${diff / 60_000} мин"
        diff < 86400000 -> timeFormat.format(date)
        diff < 604800000 -> dateFormat.format(date)
        else -> SimpleDateFormat("dd.MM.yy", Locale.getDefault()).format(date)
    }
}