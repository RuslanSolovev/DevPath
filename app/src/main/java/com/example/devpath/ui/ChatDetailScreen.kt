package com.example.devpath.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.devpath.data.repository.YdbRepository
import com.example.devpath.domain.models.Message
import com.example.devpath.ui.components.*
import com.example.devpath.ui.viewmodel.ChatsViewModel
import com.example.devpath.utils.Config
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

val availableReactions = listOf(
    "👍", "❤️", "😂", "😮", "😢", "😡",
    "👏", "🔥", "🎉", "💯", "😍", "🤔",
    "😭", "🤣", "😤", "🥳", "🙏", "💪",
    "😱", "🤯", "😴", "🥱", "🤮", "👻",
    "💩", "👽", "🤖", "🎃", "😺", "🐶",
    "🦄", "🌈", "⭐", "💎", "🍕", "🎸",
    "⚡", "🌟", "🍺", "🚀", "💀", "🗿",
    "🤡", "👹", "💋", "🧠", "👀",
    "🦾", "🧸", "💸", "📚"
)

@Composable
fun ChatDetailScreen(
    chatId: String,
    friendId: String,
    currentUserId: String,
    ydbRepository: YdbRepository,
    navController: NavHostController
) {
    val context = LocalContext.current
    val viewModel: ChatsViewModel = hiltViewModel()
    val messages by viewModel.messages.collectAsState()
    val chatName by viewModel.currentChatName.collectAsState()
    val isUserOnline by viewModel.isUserOnline.collectAsState()
    val replyingTo by viewModel.replyingTo.collectAsState()
    val editingMessage by viewModel.editingMessage.collectAsState()
    val isLoadingMessages by viewModel.isLoadingMessages.collectAsState()
    val totalMessagesCount by viewModel.totalMessagesCount.collectAsState()
    val userLastActive by viewModel.userLastActive.collectAsState()
    val chats by viewModel.chats.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var showMenuForMessage by remember { mutableStateOf<Message?>(null) }
    var showForwardDialog by remember { mutableStateOf<Message?>(null) }
    var showForwardSuccess by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var shouldScrollToBottom by remember { mutableStateOf(true) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var imageCaption by remember { mutableStateOf("") }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    var showReactionPickerFor by remember { mutableStateOf<String?>(null) }

    // Данные друга для верхней панели
    var friendAvatarUrl by remember { mutableStateOf<String?>(null) }
    var friendDisplayName by remember { mutableStateOf("") }

// Кэш аватаров отправителей
    var senderAvatars by remember { mutableStateOf<Map<String, String?>>(emptyMap()) }

    // Загружаем аватары отправителей
    LaunchedEffect(messages.size) {
        val avatars = mutableMapOf<String, String?>()
        messages.forEach { message ->
            if (message.senderId != currentUserId && !avatars.containsKey(message.senderId)) {
                val user = ydbRepository.getUser(message.senderId)
                avatars[message.senderId] = user?.optJSONObject("avatar_url")?.optString("S", "")?.ifEmpty { null }
            }
        }
        senderAvatars = avatars
    }

    // Загружаем данные друга
    LaunchedEffect(friendId) {
        if (friendId.isNotEmpty() && friendId != "null") {
            val friend = ydbRepository.getUser(friendId)
            friendAvatarUrl = friend?.optJSONObject("avatar_url")?.optString("S", "")?.ifEmpty { null }
            friendDisplayName = friend?.optJSONObject("name")?.optString("S", "") ?: chatName
        }
    }

    // Поиск — скролл к сообщению
    val scrollToMessageIdFromSearch =
        navController.currentBackStackEntry?.savedStateHandle?.get<String>("scrollToMessageId")
    LaunchedEffect(scrollToMessageIdFromSearch, messages) {
        scrollToMessageIdFromSearch?.let { msgId ->
            delay(300)
            val index = messages.indexOfFirst { it.messageId == msgId }
            if (index != -1) {
                listState.scrollToItem(index)
                showReactionPickerFor = msgId
                delay(2000)
                showReactionPickerFor = null
            }
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("scrollToMessageId")
        }
    }

    LaunchedEffect(chatId) {
        viewModel.updateUserLastActive(currentUserId)
        viewModel.loadMessages(chatId)
        viewModel.loadChatName(chatId, currentUserId)
        viewModel.loadChats(currentUserId)
        viewModel.startPolling(chatId)
        shouldScrollToBottom = true
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.stopPolling() }
    }

    LaunchedEffect(friendId) {
        if (friendId.isNotEmpty() && friendId != "null") viewModel.observeFriendOnlineStatus(friendId)
    }

    LaunchedEffect(messages.size) {
        if (shouldScrollToBottom && messages.isNotEmpty() && !isLoadingMessages) {
            try {
                listState.animateScrollToItem(messages.size - 1)
            } catch (_: Exception) {}
        }
    }

    // Отслеживаем успешную пересылку и показываем Snackbar
    LaunchedEffect(showForwardSuccess) {
        showForwardSuccess?.let { name ->
            snackbarHostState.showSnackbar(
                message = "✅ Переслано в \"$name\"",
                actionLabel = "OK",
                duration = SnackbarDuration.Short
            )
            showForwardSuccess = null
        }
    }

    fun createImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", context.cacheDir).apply {
            cameraImageUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", this)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            cameraImageUri?.let { selectedImageUri = it; imageCaption = "" }
        }
        cameraImageUri = null
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { selectedImageUri = it; imageCaption = "" }
    }

    fun uploadAndSendImage(uri: Uri) {
        coroutineScope.launch {
            try {
                val storageClient = com.example.devpath.data.storage.YandexStorageClient(
                    context, Config.YC_ACCESS_KEY, Config.YC_SECRET_KEY, Config.YC_BUCKET_NAME
                )
                val imageUrl = storageClient.uploadImage(uri, context.contentResolver)
                val user = ydbRepository.getUser(currentUserId)
                val senderName = user?.optJSONObject("name")?.optString("S", "Вы") ?: "Вы"
                val messageId = UUID.randomUUID().toString()
                ydbRepository.sendMessage(
                    messageId, chatId, currentUserId, senderName,
                    imageCaption, imageUrl,
                    replyingTo?.messageId ?: "", replyingTo?.text ?: "", replyingTo?.senderName ?: ""
                )
                ydbRepository.updateChatLastMessage(chatId, "📷 Изображение", senderName)
                viewModel.setReplyingTo(null); viewModel.loadMessages(chatId)
                selectedImageUri = null; imageCaption = ""; shouldScrollToBottom = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Диалог выбора источника изображения
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Выберите источник") },
            confirmButton = {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    TextButton({ showImageSourceDialog = false; galleryLauncher.launch("image/*") }, Modifier.fillMaxWidth()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Outlined.PhotoLibrary, null); Text("Галерея")
                        }
                    }
                    TextButton({
                        showImageSourceDialog = false
                        cameraLauncher.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                            putExtra(MediaStore.EXTRA_OUTPUT, createImageFile(context).let {
                                cameraImageUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", it)
                                cameraImageUri
                            })
                        })
                    }, Modifier.fillMaxWidth()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Outlined.CameraAlt, null); Text("Камера")
                        }
                    }
                }
            },
            dismissButton = { TextButton({ showImageSourceDialog = false }) { Text("Отмена") } }
        )
    }

    // Предпросмотр изображения
    if (selectedImageUri != null) {
        AlertDialog(
            onDismissRequest = { selectedImageUri = null; imageCaption = "" },
            title = { Text("Отправить изображение") },
            text = {
                Column(Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                    Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(8.dp)) {
                        AsyncImage(
                            model = selectedImageUri, contentDescription = null,
                            modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = imageCaption, onValueChange = { imageCaption = it },
                        placeholder = { Text("Подпись") }, modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = { Button({ uploadAndSendImage(selectedImageUri!!) }) { Text("Отправить") } },
            dismissButton = { TextButton({ selectedImageUri = null; imageCaption = "" }) { Text("Отмена") } }
        )
    }

    // Пересылка
    if (showForwardDialog != null) {
        val chatsForForward = chats.filter { it.chatId != chatId }
        var chatNames by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

        LaunchedEffect(showForwardDialog) {
            val names = mutableMapOf<String, String>()
            chatsForForward.forEach { chat ->
                if (chat.type == "personal") {
                    val fid = chat.participants.firstOrNull { it != currentUserId }
                    if (fid != null) {
                        val user = ydbRepository.getUser(fid)
                        names[chat.chatId] = user?.optJSONObject("name")?.optString("S") ?: "Пользователь"
                    }
                } else {
                    names[chat.chatId] = chat.name.ifEmpty { "Групповой чат" }
                }
            }
            chatNames = names
        }

        AlertDialog(
            onDismissRequest = { showForwardDialog = null },
            title = { Text("Переслать в...") },
            text = {
                if (chatsForForward.isEmpty()) Text("Нет других чатов")
                else LazyColumn {
                    items(chatsForForward) { chat ->
                        val chatDisplayName = chatNames[chat.chatId] ?: "Загрузка..."
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable {
                                val msg = showForwardDialog!!
                                coroutineScope.launch {
                                    val user = ydbRepository.getUser(currentUserId)
                                    val senderName = user?.optJSONObject("name")?.optString("S", "Вы") ?: "Вы"
                                    val messagesFromDb = ydbRepository.getChatMessages(chatId)
                                    val originalMessageJson = messagesFromDb.find {
                                        it.optJSONObject("message_id")?.optString("S") == msg.messageId
                                    }
                                    if (originalMessageJson != null) {
                                        ydbRepository.forwardMessageWithImage(
                                            originalMessage = originalMessageJson,
                                            targetChatId = chat.chatId,
                                            senderId = currentUserId,
                                            senderName = senderName
                                        )
                                        ydbRepository.updateChatLastMessage(chat.chatId, "📎 Пересланное сообщение", senderName)
                                        showForwardSuccess = chatDisplayName
                                    }
                                }
                                showForwardDialog = null
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(40.dp), shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            if (chat.type == "personal") Icons.Outlined.Person else Icons.Outlined.Group,
                                            null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(chatDisplayName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                    Text(if (chat.type == "personal") "Личный чат" else "Группа", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        if (chat != chatsForForward.last()) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton({ showForwardDialog = null }) { Text("Отмена") } },
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    modifier = Modifier.padding(16.dp), shape = RoundedCornerShape(12.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    action = {
                        TextButton(onClick = { data.dismiss() }) {
                            Text(data.visuals.actionLabel ?: "OK", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Text(data.visuals.message, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                }
            }
        },

        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 1.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 2.dp, vertical = 2.dp), // ← Уменьшены отступы
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.size(36.dp) // ← Уменьшен размер
                    ) {
                        Icon(
                            Icons.Default.ArrowBack, "Назад",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Аватар + Имя
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserAvatar(
                            avatarUrl = friendAvatarUrl,
                            name = friendDisplayName.ifEmpty { chatName.ifEmpty { "Чат" } },
                            size = 32, // ← Уменьшен аватар
                            showOnlineIndicator = true,
                            isOnline = isUserOnline
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = friendDisplayName.ifEmpty { chatName.ifEmpty { "Чат" } },
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                text = if (isUserOnline) "В сети" else if (userLastActive.isNotEmpty()) "Был(а) $userLastActive" else "",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isUserOnline) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Кнопки
                    Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                        IconButton(
                            onClick = { navController.navigate("search_messages/$chatId") },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Outlined.Search, "Поиск", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = { }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Outlined.MoreVert, "Ещё", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

    ) { padding ->
        Column(Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {
            Box(Modifier.weight(1f)) {
                when {
                    isLoadingMessages && messages.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    messages.filter { !it.deleted }.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Нет сообщений", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    else -> LazyColumn(
                        state = listState, modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        val filteredMessages = messages.filter { !it.deleted }

                        items(
                            items = filteredMessages,
                            key = { msg -> "${msg.messageId}_${msg.readBy.size}_${msg.deliveredTo.size}_${msg.reactions.size}_${msg.edited}_${msg.text.length}" }
                        ) { message ->
                            val currentIndex = filteredMessages.indexOf(message)
                            val prevMessage = if (currentIndex > 0) filteredMessages[currentIndex - 1] else null
                            val nextMessage = if (currentIndex < filteredMessages.size - 1) filteredMessages[currentIndex + 1] else null

                            // Определяем, является ли сообщение частью группы
                            val isFirstInGroup = prevMessage == null || prevMessage.senderId != message.senderId
                            val isLastInGroup = nextMessage == null || nextMessage.senderId != message.senderId

                            key("msg_${message.messageId}") {
                                MessageBubbleGrouped(
                                    message = message,
                                    isMine = message.senderId == currentUserId,
                                    currentUserId = currentUserId,
                                    isFirstInGroup = isFirstInGroup,
                                    senderAvatarUrl = senderAvatars[message.senderId],
                                    isLastInGroup = isLastInGroup,
                                    onLongClick = { showMenuForMessage = message },
                                    onAddReaction = { reaction -> viewModel.addReaction(message.messageId, currentUserId, reaction) },
                                    onRemoveReaction = { viewModel.removeReaction(message.messageId, currentUserId) },
                                    onShowReactionPicker = {
                                        showReactionPickerFor = message.messageId
                                        coroutineScope.launch {
                                            delay(100)
                                            val index = filteredMessages.indexOfFirst { it.messageId == message.messageId }
                                            if (index != -1) listState.animateScrollToItem(index)
                                        }
                                    },
                                    showReactionPicker = showReactionPickerFor == message.messageId,
                                    availableReactions = availableReactions,
                                    onReactionSelected = { reaction ->
                                        viewModel.addReaction(message.messageId, currentUserId, reaction)
                                        showReactionPickerFor = null
                                    },
                                    navController = navController
                                )
                            }
                        }

                    }
                }
            }

            // Отметка о прочтении
            LaunchedEffect(messages.hashCode(), messages.size) {
                messages.forEach { message ->
                    if (message.senderId != currentUserId) {
                        if (!message.deliveredTo.contains(currentUserId)) {
                            viewModel.markMessageAsDelivered(message.messageId, currentUserId)
                        }
                        if (!message.readBy.contains(currentUserId)) {
                            viewModel.markMessageAsRead(message.messageId, currentUserId)
                        }
                    }
                }
            }

            // Индикатор ответа
            AnimatedVisibility(visible = replyingTo != null) {
                Surface(
                    Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Reply, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Ответ для ${replyingTo?.senderName ?: ""}", style = MaterialTheme.typography.labelSmall)
                            Text(replyingTo?.text?.take(50) ?: "", style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton({ viewModel.setReplyingTo(null) }, Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, null, Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Индикатор редактирования
            AnimatedVisibility(visible = editingMessage != null) {
                Surface(
                    Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Edit, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text("✏️ Редактирование сообщения", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
                            Text(editingMessage?.text?.take(50) ?: "", style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton({ viewModel.setEditingMessage(null) }, Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, null, Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Поле ввода
            Surface(
                Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton({ showImageSourceDialog = true }, Modifier.size(44.dp)) {
                        Icon(Icons.Default.AddPhotoAlternate, null, Modifier.size(24.dp))
                    }
                    OutlinedTextField(
                        value = inputText, onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Сообщение") },
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        enabled = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                    IconButton({
                        if (inputText.isNotBlank()) {
                            if (editingMessage != null) {
                                viewModel.editMessage(editingMessage!!.messageId, inputText)
                                viewModel.setEditingMessage(null)
                            } else {
                                viewModel.sendMessage(
                                    chatId, currentUserId, inputText,
                                    replyingTo?.messageId ?: "", replyingTo?.text ?: "", replyingTo?.senderName ?: ""
                                )
                            }
                            inputText = ""
                            viewModel.setReplyingTo(null)
                        }
                    }, Modifier.size(44.dp)) {
                        Icon(Icons.Default.Send, null, tint = if (inputText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    // Меню сообщения
    if (showMenuForMessage != null) {
        val message = showMenuForMessage!!
        var showDeleteConfirm by remember { mutableStateOf(false) }

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Удалить сообщение?") },
                text = { Text("Это действие нельзя отменить.") },
                confirmButton = {
                    TextButton({
                        viewModel.deleteMessage(message.messageId)
                        showDeleteConfirm = false
                        showMenuForMessage = null
                    }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                        Text("Удалить")
                    }
                },
                dismissButton = { TextButton({ showDeleteConfirm = false; showMenuForMessage = null }) { Text("Отмена") } }
            )
        }

        AlertDialog(
            onDismissRequest = { showMenuForMessage = null },
            title = { Text("Действия") },
            confirmButton = {
                Column {
                    if (message.senderId == currentUserId) {
                        TextButton({ viewModel.setEditingMessage(message); inputText = message.text; showMenuForMessage = null }) {
                            Text("Редактировать")
                        }
                        TextButton({ showDeleteConfirm = true }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                            Text("Удалить")
                        }
                    }
                    TextButton({ viewModel.setReplyingTo(message); showMenuForMessage = null }) { Text("Ответить") }
                    TextButton({ showForwardDialog = message; showMenuForMessage = null }) { Text("Переслать") }
                    TextButton({ showMenuForMessage = null }) { Text("Отмена") }
                }
            }
        )
    }
}

@Composable
fun MessageBubbleGrouped(
    message: Message,
    isMine: Boolean,
    currentUserId: String,
    isFirstInGroup: Boolean,
    isLastInGroup: Boolean,
    onLongClick: () -> Unit,
    senderAvatarUrl: String? = null,
    onAddReaction: (String) -> Unit,
    onRemoveReaction: () -> Unit,
    onShowReactionPicker: () -> Unit,
    showReactionPicker: Boolean = false,
    availableReactions: List<String> = emptyList(),
    onReactionSelected: (String) -> Unit = {},
    navController: NavHostController
) {
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val messageTime = if (message.timestamp > 0) Date(message.timestamp) else Date()
    val timeString = timeFormatter.format(messageTime)

    val showAvatar = !isMine && isFirstInGroup
    val showName = !isMine && isFirstInGroup

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) { detectTapGestures(onLongPress = { onLongClick() }) },
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        // Отступ между группами (только перед первой в группе)
        if (isFirstInGroup) {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Аватар и имя НАД сообщением (для первого в группе)
        if (showAvatar) {
            Row(
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserAvatar(
                    avatarUrl = senderAvatarUrl,
                    name = message.senderName,
                    size = 32, // ← Увеличен с 28 до 32
                    showOnlineIndicator = false,
                    isOnline = false
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    message.senderName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
            // Пустое место для сообщений без аватара (чтобы выровнять по левому краю)
            if (!isMine && !showAvatar) {
                Spacer(modifier = Modifier.width(40.dp)) // 32dp аватар + 8dp отступ
            }

            Column(
                horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
                modifier = Modifier.widthIn(max = 300.dp) // ← Увеличена ширина
            ) {
                // Сообщение
                Surface(
                    shape = RoundedCornerShape(
                        topStart = 20.dp, // ← Увеличены скругления
                        topEnd = 20.dp,
                        bottomStart = if (isLastInGroup) if (isMine) 20.dp else 6.dp else if (isMine) 20.dp else 6.dp,
                        bottomEnd = if (isLastInGroup) if (isMine) 6.dp else 20.dp else if (isMine) 6.dp else 20.dp
                    ),
                    color = if (isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                    shadowElevation = 0.5.dp
                ) {
                    Column(Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                        // Ответ
                        if (message.replyToId.isNotEmpty() && message.replyToText.isNotEmpty()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = if (isMine) Color.Black.copy(alpha = 0.2f) else Color(0xFF1E88E5).copy(alpha = 0.15f)
                            ) {
                                Column(Modifier.padding(8.dp)) {
                                    Text(
                                        message.replyToSenderName.ifEmpty { "Пользователь" },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isMine) Color.White.copy(alpha = 0.9f) else Color(0xFF1565C0)
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        message.replyToText.ifEmpty { "📷 Изображение" },
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 2,
                                        color = if (isMine) Color.White.copy(alpha = 0.8f) else Color(0xFF424242)
                                    )
                                }
                            }
                        }

                        // Изображение
                        if (message.imageUrl.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 120.dp, max = 280.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.Black.copy(alpha = 0.05f))
                            ) {
                                AsyncImage(
                                    model = message.imageUrl,
                                    contentDescription = "Изображение",
                                    modifier = Modifier.fillMaxSize().clickable {
                                        navController.navigate("fullscreen_image/${Uri.encode(message.imageUrl)}")
                                    },
                                    contentScale = ContentScale.Crop
                                )
                            }
                            if (message.text.isNotEmpty()) Spacer(Modifier.height(6.dp))
                        }

                        // Текст
                        if (message.text.isNotEmpty()) {
                            Text(
                                text = message.text,
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                                color = if (isMine) Color.White else Color.Black
                            )
                        }

                        // Время и статус
                        Row(
                            Modifier.padding(top = 2.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (message.edited) {
                                Text("(ред.)", style = MaterialTheme.typography.labelSmall, color = if (isMine) Color.White.copy(alpha = 0.6f) else Color.Gray, fontSize = 10.sp)
                                Spacer(Modifier.width(4.dp))
                            }
                            Text(timeString, style = MaterialTheme.typography.labelSmall, color = if (isMine) Color.White.copy(alpha = 0.6f) else Color.Gray, fontSize = 10.sp)
                            if (isMine) {
                                Spacer(Modifier.width(3.dp))
                                when {
                                    message.readBy.isNotEmpty() -> Icon(Icons.Outlined.DoneAll, "Прочитано", modifier = Modifier.size(14.dp), tint = Color(0xFF34B7F1))
                                    message.deliveredTo.isNotEmpty() -> Icon(Icons.Outlined.DoneAll, "Доставлено", modifier = Modifier.size(14.dp), tint = Color.White.copy(alpha = 0.6f))
                                    else -> Icon(Icons.Outlined.Done, "Отправлено", modifier = Modifier.size(14.dp), tint = Color.White.copy(alpha = 0.6f))
                                }
                            }
                        }
                    }
                }

                // Реакции
                if (isLastInGroup) {
                    Spacer(Modifier.height(2.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(start = if (isMine) 0.dp else 40.dp)
                    ) {
                        if (message.reactions.isNotEmpty()) {
                            message.reactions.groupBy { it.reaction }.forEach { (emoji, list) ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    modifier = Modifier.clickable { onAddReaction(emoji) }
                                ) {
                                    Text("$emoji ${list.size}", modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), style = MaterialTheme.typography.labelLarge, fontSize = 13.sp)
                                }
                            }
                        }
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(24.dp).clickable { onShowReactionPicker() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.EmojiEmotions, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Пикер реакций
                    if (showReactionPicker) {
                        Spacer(Modifier.height(4.dp))
                        Column(
                            modifier = Modifier.widthIn(max = 250.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(16.dp)).padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            availableReactions.chunked(6).forEach { row ->
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(vertical = 2.dp)) {
                                    row.forEach { emoji ->
                                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), modifier = Modifier.size(36.dp).clickable { onReactionSelected(emoji) }) {
                                            Box(contentAlignment = Alignment.Center) { Text(emoji, fontSize = 20.sp) }
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
}

private fun formatMessagesCount(count: Long): String = when {
    count == 0L -> "Нет сообщений"
    count == 1L -> "1 сообщение"
    count in 2L..4L -> "$count сообщения"
    else -> "$count сообщений"
}