package com.example.devpath.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.devpath.ui.viewmodel.ChatsViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chatId: String,
    navController: NavHostController
) {
    val currentUserId = Firebase.auth.currentUser?.uid ?: ""
    val viewModel: ChatsViewModel = hiltViewModel()
    val messages by viewModel.messages.collectAsState()
    val chatName by viewModel.currentChatName.collectAsState()
    val isUserOnline by viewModel.isUserOnline.collectAsState()
    val replyingTo by viewModel.replyingTo.collectAsState()
    val editingMessage by viewModel.editingMessage.collectAsState()
    val isLoadingMessages by viewModel.isLoadingMessages.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val hasMoreMessages by viewModel.hasMoreMessages.collectAsState()
    val totalMessagesCount by viewModel.totalMessagesCount.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var showMenuForMessage by remember { mutableStateOf<com.example.devpath.domain.models.Message?>(null) }
    var isSomeoneTyping by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var shouldScrollToBottom by remember { mutableStateOf(true) }
    var previousMessagesCount by remember { mutableStateOf(0) }

    // Флаг для отслеживания, была ли уже загрузка
    var isInitialLoad by remember { mutableStateOf(true) }

    // Загружаем первые сообщения и общее количество
    LaunchedEffect(chatId) {
        viewModel.loadMessages(chatId, reset = true)
        viewModel.loadChatName(chatId, currentUserId)
        viewModel.loadTotalMessagesCount(chatId)
        shouldScrollToBottom = true
        isInitialLoad = false
    }

    // Наблюдаем за статусом онлайн собеседника
    LaunchedEffect(chatId) {
        val friendId = viewModel.getOtherParticipantId(chatId, currentUserId)
        if (friendId != null) {
            viewModel.observeFriendOnlineStatus(friendId)
        }
    }

    // Наблюдаем за статусом "печатает"
    LaunchedEffect(chatId) {
        viewModel.observeTypingStatus(chatId, currentUserId).collect { typing ->
            isSomeoneTyping = typing
        }
    }

    // Обновляем статус онлайн при входе/выходе
    DisposableEffect(Unit) {
        viewModel.updateUserOnlineStatus(currentUserId, true)
        onDispose {
            viewModel.updateUserOnlineStatus(currentUserId, false)
        }
    }

    // Загружаем старые сообщения при скролле вверх
    LaunchedEffect(listState.firstVisibleItemIndex) {
        if (!isLoadingMore && hasMoreMessages && listState.firstVisibleItemIndex == 0 && messages.isNotEmpty() && !isInitialLoad) {
            val currentScrollPosition = listState.firstVisibleItemScrollOffset
            previousMessagesCount = messages.size
            viewModel.loadMoreMessages(chatId)
            coroutineScope.launch {
                delay(100)
                val newMessageCount = messages.size
                val addedMessages = newMessageCount - previousMessagesCount
                if (addedMessages > 0) {
                    listState.scrollToItem(addedMessages, currentScrollPosition)
                }
                shouldScrollToBottom = false
            }
        }
    }

    // Новые сообщения – скроллим к ним (только если сообщение отправили мы)
    LaunchedEffect(messages.size) {
        if (shouldScrollToBottom && messages.isNotEmpty() && !isLoadingMessages && !isLoadingMore && !isInitialLoad) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    // Отмечаем сообщения как доставленные и прочитанные при прокрутке
    LaunchedEffect(messages, listState.layoutInfo.visibleItemsInfo) {
        if (messages.isNotEmpty()) {
            val visibleMessages = listState.layoutInfo.visibleItemsInfo.mapNotNull { it.key as? String }
            visibleMessages.forEach { messageId ->
                val message = messages.find { it.messageId == messageId }
                if (message != null && message.senderId != currentUserId) {
                    if (!message.deliveredTo.contains(currentUserId)) {
                        viewModel.markMessageAsDelivered(messageId, currentUserId)
                    }
                    if (!message.readBy.contains(currentUserId)) {
                        viewModel.markMessageAsRead(messageId, currentUserId)
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(chatName.ifEmpty { "Чат" })
                            if (isUserOnline) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF4CAF50))
                                )
                            }
                        }
                        if (!isLoadingMessages && totalMessagesCount > 0) {
                            Text(
                                text = formatMessagesCount(totalMessagesCount),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            // Блок ответа на сообщение
            if (replyingTo != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Reply,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Ответ для ${replyingTo?.senderName ?: "пользователя"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = replyingTo?.text?.take(50) ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(8.dp),
                                    maxLines = 2,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                        IconButton(onClick = { viewModel.setReplyingTo(null) }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Отмена",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Список сообщений
            Box(modifier = Modifier.weight(1f)) {
                if (isLoadingMessages && messages.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                strokeWidth = 3.dp
                            )
                            Text(
                                text = "Загрузка сообщений...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        contentPadding = PaddingValues(16.dp),
                        reverseLayout = false
                    ) {
                        if (isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        }

                        items(messages, key = { it.messageId }) { message ->
                            MessageBubble(
                                message = message,
                                isMine = message.senderId == currentUserId,
                                currentUserId = currentUserId,
                                onLongClick = { showMenuForMessage = message }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            // Индикатор "печатает..."
            if (isSomeoneTyping) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column {
                            Text(
                                text = "Собеседник печатает...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                repeat(3) { index ->
                                    val animation by animateFloatAsState(
                                        targetValue = 1f,
                                        animationSpec = repeatable(
                                            iterations = Int.MAX_VALUE,
                                            animation = tween(400, easing = LinearEasing),
                                            repeatMode = RepeatMode.Reverse
                                        ),
                                        label = "typing"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(
                                                MaterialTheme.colorScheme.primary.copy(
                                                    alpha = animation
                                                )
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Блок редактирования сообщения
            if (editingMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Редактирование сообщения",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { viewModel.setEditingMessage(null) }) {
                            Icon(Icons.Default.Close, contentDescription = "Отмена")
                        }
                    }
                }
            }

            // Поле ввода
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { newText ->
                        inputText = newText
                        if (newText.isNotBlank()) {
                            viewModel.startTyping(chatId, currentUserId)
                        } else {
                            viewModel.stopTyping(chatId, currentUserId)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(if (replyingTo != null) "Введите ответ..." else "Сообщение")
                    },
                    shape = MaterialTheme.shapes.large,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            if (editingMessage != null) {
                                viewModel.editMessage(editingMessage!!.messageId, inputText)
                                inputText = ""
                                viewModel.setEditingMessage(null)
                            } else {
                                shouldScrollToBottom = true
                                viewModel.sendMessage(
                                    chatId = chatId,
                                    senderId = currentUserId,
                                    text = inputText,
                                    replyToId = replyingTo?.messageId ?: "",
                                    replyToText = replyingTo?.text ?: "",
                                    replyToSenderName = replyingTo?.senderName ?: ""
                                )
                                inputText = ""
                                viewModel.stopTyping(chatId, currentUserId)
                            }
                        }
                    }
                ) {
                    Icon(
                        if (editingMessage != null) Icons.Default.Edit else Icons.Default.Send,
                        contentDescription = if (editingMessage != null) "Сохранить" else "Отправить",
                        tint = if (inputText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Меню для сообщения
    if (showMenuForMessage != null) {
        val message = showMenuForMessage!!
        AlertDialog(
            onDismissRequest = { showMenuForMessage = null },
            title = { Text("Действия") },
            text = { Text("Выберите действие для сообщения") },
            confirmButton = {
                Column {
                    if (message.senderId == currentUserId) {
                        TextButton(
                            onClick = {
                                showMenuForMessage = null
                                viewModel.setEditingMessage(message)
                                inputText = message.text
                            }
                        ) {
                            Row {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Редактировать")
                            }
                        }
                        TextButton(
                            onClick = {
                                showMenuForMessage = null
                                viewModel.deleteMessage(message.messageId)
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Row {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Удалить")
                            }
                        }
                    }
                    TextButton(
                        onClick = {
                            showMenuForMessage = null
                            viewModel.setReplyingTo(message)
                        }
                    ) {
                        Row {
                            Icon(Icons.Default.Reply, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ответить")
                        }
                    }
                    TextButton(
                        onClick = { showMenuForMessage = null }
                    ) {
                        Text("Отмена")
                    }
                }
            },
            dismissButton = {}
        )
    }
}

@Composable
fun MessageBubble(
    message: com.example.devpath.domain.models.Message,
    isMine: Boolean,
    currentUserId: String,
    onLongClick: () -> Unit
) {
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFormatter = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }
    val messageTime = message.timestamp?.toDate() ?: Date()
    val timeString = timeFormatter.format(messageTime)
    val dateString = dateFormatter.format(messageTime)
    val isToday = dateString == dateFormatter.format(Date())

    val isRead = message.readBy.contains(currentUserId)
    val isDelivered = message.deliveredTo.contains(currentUserId)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = { onLongClick() })
            },
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            // Имя отправителя
            if (!isMine && message.senderName.isNotEmpty()) {
                Text(
                    text = message.senderName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
                )
            }

            // Блок цитируемого сообщения
            if (message.replyToId.isNotEmpty() && message.replyToText.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Reply,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = message.replyToSenderName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = message.replyToText,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Основное сообщение
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = if (isMine)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = if (isMine) 0.dp else 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isToday) timeString else "$dateString $timeString",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (message.edited && !message.deleted) {
                            Text(
                                text = "(ред.)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isMine) {
                            when {
                                isRead && message.readBy.size > 1 -> {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.DoneAll,
                                            contentDescription = "Прочитано",
                                            modifier = Modifier.size(14.dp),
                                            tint = Color(0xFF34B7F1)
                                        )
                                    }
                                }
                                isDelivered && message.deliveredTo.size > 1 -> {
                                    Icon(
                                        Icons.Default.DoneAll,
                                        contentDescription = "Доставлено",
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                message.deliveredTo.isNotEmpty() -> {
                                    Icon(
                                        Icons.Default.Done,
                                        contentDescription = "Отправлено",
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                else -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(12.dp),
                                        strokeWidth = 1.dp
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

// Вспомогательная функция для форматирования количества сообщений
private fun formatMessagesCount(count: Long): String {
    return when {
        count == 0L -> "Нет сообщений"
        count == 1L -> "1 сообщение"
        count in 2L..4L -> "$count сообщения"
        else -> "$count сообщений"
    }
}