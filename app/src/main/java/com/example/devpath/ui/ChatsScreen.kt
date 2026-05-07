package com.example.devpath.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.devpath.data.repository.YdbRepository
import com.example.devpath.ui.components.UserAvatar
import com.example.devpath.ui.viewmodel.ChatsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
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

    // Загрузка чатов при первом запуске
    LaunchedEffect(currentUserId) {
        viewModel.loadChats(currentUserId)
    }

    // Умный поллинг — запускаем и останавливаем при уходе с экрана
    DisposableEffect(currentUserId) {
        viewModel.startSmartPolling(currentUserId)
        onDispose {
            viewModel.stopChatListPolling()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(
                            Brush.linearGradient(colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)))
                        ), contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Chat, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                        }
                        Text("Чаты", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold))
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("friends") }, modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))) {
                        Icon(Icons.Outlined.People, contentDescription = "Друзья", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues).background(
            Brush.verticalGradient(colors = listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)))
        )) {
            when {
                isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(48.dp), strokeWidth = 3.dp, color = MaterialTheme.colorScheme.primary)
                        Text("Загрузка чатов...", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                chats.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Surface(modifier = Modifier.size(80.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) {
                            Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Chat, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) }
                        }
                        Text("У вас пока нет чатов", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                        Text("Добавьте друзей, чтобы начать общение", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(onClick = { navController.navigate("friends") }, modifier = Modifier.padding(top = 8.dp), shape = RoundedCornerShape(24.dp)) {
                            Icon(Icons.Outlined.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Найти друзей")
                        }
                    }
                }
                else -> LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(chats.sortedByDescending { it.lastMessageTime }, key = { it.chatId }) { chat ->
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
                            onLongClick = { chatToDelete = chat; showDeleteDialog = true }
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog && chatToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false; chatToDelete = null },
            title = { Text("Удалить чат", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) },
            text = { Text("Вы уверены, что хотите удалить этот чат? Все сообщения будут потеряны.", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = {
                    chatToDelete?.let { viewModel.deleteChat(it.chatId, currentUserId) }
                    showDeleteDialog = false; chatToDelete = null
                }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false; chatToDelete = null }) { Text("Отмена") }
            },
            shape = RoundedCornerShape(16.dp)
        )
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

    // Загружаем данные друга только при изменении chatId
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
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
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
                    else -> {
                        Box(
                            modifier = Modifier.fillMaxSize().clip(CircleShape).background(
                                Brush.linearGradient(colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)))
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Group, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(28.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = when {
                            chat.type == "personal" && friendName.isNotEmpty() -> friendName
                            chat.name.isNotEmpty() -> chat.name
                            else -> "Личный чат"
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
                    Icon(Icons.Outlined.ChevronRight, "Открыть", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
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