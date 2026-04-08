package com.example.devpath.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.devpath.ui.components.UserAvatar
import com.example.devpath.ui.viewmodel.ChatsViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ChatDetailScreen(
    chatId: String,
    friendId: String,
    navController: NavHostController
) {
    val context = LocalContext.current
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
    var isInitialLoad by remember { mutableStateOf(true) }
    val userLastActive by viewModel.userLastActive.collectAsState()

    // Состояния для улучшенной отправки фото
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var imageCaption by remember { mutableStateOf("") }

    // Лаунчер для выбора изображения из галереи
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            imageCaption = ""
        }
    }

    // Диалог предпросмотра фото
    if (selectedImageUri != null) {
        AlertDialog(
            onDismissRequest = {
                selectedImageUri = null
                imageCaption = ""
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Отправить изображение", style = MaterialTheme.typography.titleMedium)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 500.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Превью",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                            alignment = Alignment.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = imageCaption,
                        onValueChange = { imageCaption = it },
                        placeholder = { Text("Добавить подпись к фото (необязательно)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedImageUri?.let { uri ->
                            isUploading = true
                            coroutineScope.launch {
                                viewModel.sendImageMessageWithText(
                                    chatId = chatId,
                                    senderId = currentUserId,
                                    imageUri = uri,
                                    text = imageCaption,
                                    contentResolver = context.contentResolver,
                                    replyToId = replyingTo?.messageId ?: "",
                                    replyToText = replyingTo?.text ?: "",
                                    replyToSenderName = replyingTo?.senderName ?: ""
                                )
                                isUploading = false
                                shouldScrollToBottom = true
                                selectedImageUri = null
                                imageCaption = ""
                                viewModel.setReplyingTo(null)
                            }
                        }
                    },
                    enabled = !isUploading
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Загрузка...")
                    } else {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Отправить")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    selectedImageUri = null
                    imageCaption = ""
                }) {
                    Text("Отмена")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Обновляем активность при входе в чат
    LaunchedEffect(Unit) {
        viewModel.updateUserLastActive(currentUserId)
    }

    LaunchedEffect(chatId) {
        viewModel.updateUserLastActive(currentUserId)
        viewModel.loadMessages(chatId, reset = true)
        viewModel.loadChatName(chatId, currentUserId)
        viewModel.loadTotalMessagesCount(chatId)
        shouldScrollToBottom = true
        isInitialLoad = false
    }

    LaunchedEffect(Unit) {
        if (friendId.isNotEmpty() && friendId != "null") {
            viewModel.observeFriendOnlineStatus(friendId)
            viewModel.observeUserLastActive(friendId)
        }
    }

    LaunchedEffect(chatId) {
        viewModel.observeTypingStatus(chatId, currentUserId).collect { typing ->
            isSomeoneTyping = typing
        }
    }

    // Пагинация при скролле вверх
    LaunchedEffect(listState.firstVisibleItemIndex) {
        if (!isLoadingMore && hasMoreMessages && listState.firstVisibleItemIndex == 0 && messages.isNotEmpty() && !isInitialLoad) {
            val currentScrollPosition = listState.firstVisibleItemScrollOffset
            previousMessagesCount = messages.size
            viewModel.loadMoreMessages(chatId)
            coroutineScope.launch {
                delay(100)
                val addedMessages = messages.size - previousMessagesCount
                if (addedMessages > 0) {
                    try {
                        listState.scrollToItem(addedMessages, currentScrollPosition)
                    } catch (e: Exception) {
                        println("DEBUG: Ошибка скролла при загрузке: ${e.message}")
                    }
                }
                shouldScrollToBottom = false
            }
        }
    }

    // Автоскролл вниз при новых сообщениях
    LaunchedEffect(messages.size) {
        if (shouldScrollToBottom && messages.isNotEmpty() && !isLoadingMessages && !isLoadingMore && !isInitialLoad) {
            try {
                val lastIndex = messages.size - 1
                if (lastIndex >= 0) {
                    coroutineScope.launch {
                        listState.animateScrollToItem(lastIndex)
                    }
                }
            } catch (e: Exception) {
                println("DEBUG: Ошибка анимации скролла: ${e.message}")
            }
        }
    }

    // Отметка о прочтении
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
                            Text(
                                text = chatName.ifEmpty { "Чат" },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            if (isUserOnline) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF4CAF50))
                                )
                            }
                        }
                        Text(
                            text = if (isUserOnline) "В сети"
                            else if (userLastActive.isNotEmpty()) "Был(а) $userLastActive"
                            else "Был(а) недавно",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Назад",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                ),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Прогресс-бар при начальной загрузке
                if (isLoadingMessages && messages.isEmpty()) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                // Блок ответа на сообщение
                AnimatedVisibility(
                    visible = replyingTo != null,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(32.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Reply,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                                Text(
                                    text = "Ответ для ${replyingTo?.senderName ?: "пользователя"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = replyingTo?.text?.take(50) ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                            IconButton(
                                onClick = { viewModel.setReplyingTo(null) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Отмена",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Список сообщений
                Box(modifier = Modifier.weight(1f)) {
                    if (isLoadingMessages && messages.isEmpty()) {
                        // Центральный индикатор загрузки (на случай если прогресс-бар не достаточен)
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
                                    strokeWidth = 3.dp,
                                    color = MaterialTheme.colorScheme.primary
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
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            reverseLayout = false
                        ) {
                            if (isLoadingMore) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            items(messages, key = { it.messageId }) { message ->
                                AnimatedContent(
                                    targetState = message,
                                    transitionSpec = {
                                        fadeIn(animationSpec = tween(300)) +
                                                slideInVertically(
                                                    initialOffsetY = { it / 2 },
                                                    animationSpec = tween(300)
                                                ) togetherWith
                                                fadeOut(animationSpec = tween(100))
                                    }
                                ) { msg ->
                                    MessageBubbleModern(
                                        message = msg,
                                        isMine = msg.senderId == currentUserId,
                                        currentUserId = currentUserId,
                                        onLongClick = { showMenuForMessage = msg },
                                        navController = navController
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }

                // Индикатор "печатает..."
                AnimatedVisibility(
                    visible = isSomeoneTyping,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.8f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(32.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Outlined.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Собеседник печатает...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
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
                                                .size(6.dp)
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
                AnimatedVisibility(
                    visible = editingMessage != null,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(32.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                                Text(
                                    text = "Редактирование сообщения",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            IconButton(
                                onClick = { viewModel.setEditingMessage(null) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Отмена",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Поле ввода с кнопкой для выбора фото (улучшенный дизайн)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    tonalElevation = 3.dp,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Column {
                        // Строка ввода
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Кнопка выбора фото с анимацией
                            IconButton(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primaryContainer,
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                            )
                                        )
                                    )
                            ) {
                                Icon(
                                    Icons.Default.Image,
                                    contentDescription = "Выбрать фото",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Поле ввода текста
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
                                    Text(
                                        if (replyingTo != null) "Введите ответ..." else "Сообщение",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                shape = RoundedCornerShape(24.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                textStyle = MaterialTheme.typography.bodyLarge
                            )

                            // Кнопка отправки/сохранения
                            FloatingActionButton(
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
                                },
                                modifier = Modifier.size(40.dp),
                                containerColor = if (inputText.isNotBlank())
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape,
                                elevation = FloatingActionButtonDefaults.elevation(0.dp)
                            ) {
                                Icon(
                                    if (editingMessage != null) Icons.Default.Edit else Icons.Default.Send,
                                    contentDescription = if (editingMessage != null) "Сохранить" else "Отправить",
                                    modifier = Modifier.size(20.dp),
                                    tint = if (inputText.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Меню для сообщения (оставлено без изменений, только улучшен внешний вид)
    if (showMenuForMessage != null) {
        val message = showMenuForMessage!!
        AlertDialog(
            onDismissRequest = { showMenuForMessage = null },
            title = { Text("Действия", style = MaterialTheme.typography.titleMedium) },
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
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
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
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
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
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Reply, contentDescription = null, modifier = Modifier.size(18.dp))
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
fun MessageBubbleModern(
    message: com.example.devpath.domain.models.Message,
    isMine: Boolean,
    currentUserId: String,
    onLongClick: () -> Unit,
    navController: NavHostController
) {
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFormatter = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }
    val messageTime = message.timestamp?.toDate() ?: Date()
    val timeString = timeFormatter.format(messageTime)
    val dateString = dateFormatter.format(messageTime)
    val isToday = dateString == dateFormatter.format(Date())

    val isRead = message.readBy.contains(currentUserId)
    val isDelivered = message.deliveredTo.contains(currentUserId)

    // Состояние для аватара собеседника
    var friendAvatarUrl by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // Создаем репозиторий напрямую
    val chatRepository = remember {
        com.example.devpath.data.repository.ChatRepository(
            yandexStorageClient = com.example.devpath.data.storage.YandexStorageClient(
                context = context,
                accessKey = com.example.devpath.BuildConfig.YC_ACCESS_KEY,
                secretKey = com.example.devpath.BuildConfig.YC_SECRET_KEY,
                bucketName = com.example.devpath.BuildConfig.YC_BUCKET_NAME
            )
        )
    }

    LaunchedEffect(message.senderId) {
        if (!isMine) {
            val userProfile = chatRepository.getUser(message.senderId)
            friendAvatarUrl = userProfile?.avatarUrl
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = { onLongClick() })
            },
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        // Аватар для сообщений собеседника
        if (!isMine) {
            UserAvatar(
                avatarUrl = friendAvatarUrl,
                name = message.senderName,
                size = 36,
                modifier = Modifier.padding(end = 8.dp) // Вместо margin используем padding
            )
        }

        Column(
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            if (!isMine && message.senderName.isNotEmpty()) {
                Text(
                    text = message.senderName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 4.dp)
                )
            }

            // Блок цитируемого сообщения
            if (message.replyToId.isNotEmpty() && message.replyToText.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Outlined.Reply, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(message.replyToSenderName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                        }
                        Text(message.replyToText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            // Основной пузырь сообщения
            Surface(
                shape = RoundedCornerShape(if (isMine) 20.dp else 20.dp, 4.dp, 20.dp, 20.dp),
                color = if (isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    if (message.imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = message.imageUrl,
                            contentDescription = "Изображение",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp, max = 300.dp)
                                .wrapContentHeight()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    val encodedUrl = Uri.encode(message.imageUrl)
                                    navController.navigate("fullscreen_image/$encodedUrl")
                                },
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (message.text.isNotEmpty()) {
                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        modifier = Modifier.padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (isToday) timeString else "$dateString $timeString",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isMine) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (message.edited && !message.deleted) {
                            Text(
                                "(ред.)",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isMine) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (isMine) {
                            when {
                                isRead && message.readBy.size > 1 -> {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Icon(Icons.Outlined.DoneAll, "Прочитано", modifier = Modifier.size(16.dp), tint = Color(0xFF34B7F1))
                                    }
                                }
                                isDelivered && message.deliveredTo.size > 1 -> {
                                    Icon(Icons.Outlined.DoneAll, "Доставлено", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                message.deliveredTo.isNotEmpty() -> {
                                    Icon(Icons.Outlined.Done, "Отправлено", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                else -> {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 1.5.dp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Вспомогательная функция (оставлена для совместимости)
private fun formatMessagesCount(count: Long): String {
    return when {
        count == 0L -> "Нет сообщений"
        count == 1L -> "1 сообщение"
        count in 2L..4L -> "$count сообщения"
        else -> "$count сообщений"
    }
}