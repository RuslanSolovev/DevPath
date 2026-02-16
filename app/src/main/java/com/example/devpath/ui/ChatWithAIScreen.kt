// ui/ChatWithAIScreen.kt
package com.example.devpath.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.VoiceChat
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.devpath.domain.models.AIMessage
import com.example.devpath.domain.models.ChatSession
import com.example.devpath.ui.components.VoiceSettingsDialog
import com.example.devpath.ui.viewmodel.ChatViewModel
import com.example.devpath.ui.viewmodel.ChatHistoryViewModel
import com.example.devpath.ui.viewmodel.VoiceInputViewModel
import com.example.devpath.ui.viewmodel.VoiceOutputViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatWithAIScreen(
    onBackToHome: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel(),
    voiceInputViewModel: VoiceInputViewModel = hiltViewModel(),
    voiceOutputViewModel: VoiceOutputViewModel = hiltViewModel()
) {
    var message by remember { mutableStateOf("") }
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    var showVoiceSettings by remember { mutableStateOf(false) }
    var showEmotionPicker by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }
    var isHistoryLoading by remember { mutableStateOf(false) }

    // Состояние для диалога сохранения
    var showSaveDialog by remember { mutableStateOf(false) }
    var saveDialogTitle by remember { mutableStateOf("") }

    val isVoiceEnabled by voiceOutputViewModel.isVoiceEnabled.collectAsState()
    val selectedVoice by voiceOutputViewModel.selectedVoice.collectAsState()
    val isSpeaking by voiceOutputViewModel.isSpeaking.collectAsState()
    val isRecording by voiceInputViewModel.isRecording.collectAsState()
    val isProcessing by voiceInputViewModel.isProcessing.collectAsState()
    val isListening by voiceInputViewModel.isListening.collectAsState()
    val audioLevel by voiceInputViewModel.audioLevel.collectAsState()
    val voiceSpeed by voiceOutputViewModel.voiceSpeed.collectAsState()

    // Состояния для диалога разрешений
    val showPermissionDialog by voiceInputViewModel.showPermissionDialog.collectAsState()
    val permissionPermanentlyDenied by voiceInputViewModel.permissionPermanentlyDenied.collectAsState()

    val chatHistoryViewModel: ChatHistoryViewModel = hiltViewModel()
    val coroutineScope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }

    val successMessage by viewModel.success.collectAsState()
    val errorMessage by viewModel.error.collectAsState()

    var pendingVoiceMessage by remember { mutableStateOf<String?>(null) }
    var isVoiceModeActive by remember { mutableStateOf(false) }
    var lastMessageId by remember { mutableStateOf<Long?>(null) }

    // Автоматическая отправка голосовых сообщений и ОЧИСТКА ПОЛЯ ВВОДА
    LaunchedEffect(pendingVoiceMessage) {
        pendingVoiceMessage?.let { text ->
            if (text.isNotBlank()) {
                viewModel.sendMessage(text)
                // Важно: очищаем поле ввода после отправки
                message = ""
                pendingVoiceMessage = null
                println("📝 Голосовое сообщение отправлено, поле очищено")
            }
        }
    }

    // Основная логика голосового диалога:
    // 1. Когда ИИ заканчивает говорить, автоматически включаем микрофон
    // 2. Когда пользователь заканчивает говорить, отправляем сообщение
    LaunchedEffect(messages, isSpeaking, isVoiceModeActive) {
        if (!isVoiceModeActive) return@LaunchedEffect

        val lastMessage = messages.lastOrNull()

        // Если последнее сообщение от ИИ и он закончил говорить
        if (lastMessage != null && !lastMessage.isUser && !isSpeaking && isVoiceEnabled) {
            // Ждем небольшую паузу после окончания озвучки
            delay(800)

            // Включаем микрофон для следующего вопроса
            if (!isListening && !isRecording && !isProcessing) {
                println("🎤 Автоматическое включение микрофона после ответа ИИ")
                voiceInputViewModel.startListening { recognizedText ->
                    pendingVoiceMessage = recognizedText
                }
            }
        }
    }

    // Обработка окончания записи голоса
    LaunchedEffect(isRecording, isVoiceModeActive) {
        if (isVoiceModeActive && !isRecording && lastMessageId != null) {
            // Если запись закончилась и есть последнее сообщение от ИИ
            // значит пользователь только что говорил и запись остановилась
            val lastMessage = messages.lastOrNull()
            if (lastMessage != null && lastMessage.isUser) {
                // Ждем ответ ИИ
                println("🎤 Ожидание ответа ИИ...")
            }
        }
    }

    // Отслеживание новых сообщений для озвучки
    LaunchedEffect(messages) {
        if (messages.isNotEmpty()) {
            val lastMessage = messages.last()
            if (!lastMessage.isUser && isVoiceEnabled && lastMessage.timestamp != lastMessageId) {
                lastMessageId = lastMessage.timestamp
                // Озвучиваем ответ ИИ
                voiceOutputViewModel.speakText(lastMessage.text, lastMessage.timestamp)
            }
        }
    }

    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearSuccess()
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short,
                actionLabel = "OK"
            )
            viewModel.clearError()
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    if (showVoiceSettings) {
        VoiceSettingsDialog(
            showDialog = showVoiceSettings,
            currentVoice = selectedVoice,
            currentSpeed = voiceSpeed,
            isVoiceEnabled = isVoiceEnabled,
            onDismiss = { showVoiceSettings = false },
            onVoiceSelected = { voiceId ->
                voiceOutputViewModel.setVoice(voiceId)
            },
            onSpeedSelected = { speed ->
                voiceOutputViewModel.setVoiceSpeed(speed)
            },
            onToggleVoiceEnabled = {
                voiceOutputViewModel.toggleVoiceEnabled()
                if (!isVoiceEnabled) {
                    // Если голос отключен, выключаем голосовой режим
                    isVoiceModeActive = false
                    voiceInputViewModel.stopListening()
                }
            }
        )
    }

    // Диалог сохранения чата
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Сохранить диалог") },
            text = {
                Column {
                    Text("Введите название для диалога:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = saveDialogTitle,
                        onValueChange = { saveDialogTitle = it },
                        placeholder = { Text("Например: Вопрос про корутины") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (saveDialogTitle.isNotBlank()) {
                            viewModel.saveCurrentChat(saveDialogTitle)
                        } else {
                            viewModel.saveCurrentChat()
                        }
                        showSaveDialog = false
                        saveDialogTitle = ""
                    }
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Отмена")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showEmotionPicker) {
        EmotionPickerDialog(
            onDismiss = { showEmotionPicker = false },
            onEmotionSelected = { emotion ->
                voiceOutputViewModel.setEmotion(emotion)
                showEmotionPicker = false
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.Top
        ) {
            ChatTopAppBar(
                onBackClick = onBackToHome,
                onClearChat = {
                    viewModel.clearChat()
                    isVoiceModeActive = false
                    voiceInputViewModel.stopListening()
                    message = "" // Очищаем поле ввода
                },
                onSaveChat = {
                    // Открываем диалог сохранения, а не настройки голоса
                    if (messages.isNotEmpty()) {
                        saveDialogTitle = ""
                        showSaveDialog = true
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Нет сообщений для сохранения",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                },
                onHistoryClick = {
                    chatHistoryViewModel.loadSessions()
                    showHistorySheet = true
                },
                onVoiceSettingsClick = { showVoiceSettings = true },
                onStopSpeakingClick = {
                    voiceOutputViewModel.stopSpeaking()
                    isVoiceModeActive = false
                    voiceInputViewModel.stopListening()
                },
                onClearCacheClick = { voiceOutputViewModel.clearCache() },
                onVoiceModeToggle = {
                    isVoiceModeActive = !isVoiceModeActive
                    if (isVoiceModeActive && isVoiceEnabled) {
                        // При включении голосового режима сразу запускаем микрофон
                        voiceInputViewModel.startListening { recognizedText ->
                            pendingVoiceMessage = recognizedText
                        }
                    } else {
                        voiceInputViewModel.stopListening()
                    }
                },
                isVoiceEnabled = isVoiceEnabled,
                isSpeaking = isSpeaking,
                isVoiceModeActive = isVoiceModeActive,
                isChatEmpty = messages.isEmpty(),
                isLoading = isLoading
            )

            // ИНДИКАТОР РЕЖИМА ДИАЛОГА
            if (isVoiceModeActive) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Filled.VoiceChat,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                "Режим непрерывного диалога",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        TextButton(
                            onClick = {
                                isVoiceModeActive = false
                                voiceInputViewModel.stopListening()
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "🎤 Голосовой режим выключен",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        ) {
                            Text("Выключить")
                        }
                    }
                }
            }

            if (messages.isEmpty()) {
                EmptyChatContent(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    onExampleClick = { question ->
                        viewModel.handleExampleQuestion(question)
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    reverseLayout = true,
                    state = listState,
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isLoading) {
                        item { LoadingIndicator() }
                    }

                    items(
                        items = messages.reversed(),
                        key = { it.timestamp }
                    ) { message ->
                        AnimatedMessageItem(
                            message = message,
                            isVoiceEnabled = isVoiceEnabled,
                            voiceOutputViewModel = voiceOutputViewModel,
                            isHistoryLoading = isHistoryLoading
                        )
                    }
                }
            }

            InputPanel(
                modifier = Modifier.fillMaxWidth(),
                message = message,
                onMessageChange = { message = it },
                onSendClick = {
                    if (message.isNotBlank() && !isLoading) {
                        viewModel.sendMessage(message)
                        message = "" // ОЧИЩАЕМ ПОЛЕ ВВОДА
                        keyboardController?.hide()
                    }
                },
                isLoading = isLoading,
                isRecording = isRecording,
                isProcessing = isProcessing,
                isListening = isListening,
                audioLevel = audioLevel,
                onVoiceClick = {
                    if (isRecording) {
                        voiceInputViewModel.stopRecordingAndRecognize { recognizedText ->
                            pendingVoiceMessage = recognizedText
                        }
                    } else if (isListening) {
                        voiceInputViewModel.stopListening()
                    } else {
                        voiceInputViewModel.startRecording()
                    }
                },
                onVoiceLongClick = {
                    // Долгое нажатие включает непрерывный голосовой режим
                    isVoiceModeActive = !isVoiceModeActive
                    if (isVoiceModeActive && isVoiceEnabled) {
                        voiceInputViewModel.startListening { recognizedText ->
                            pendingVoiceMessage = recognizedText
                        }
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = "🎤 Голосовой режим включен",
                                duration = SnackbarDuration.Short
                            )
                        }
                    } else {
                        voiceInputViewModel.stopListening()
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = "🎤 Голосовой режим выключен",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                },
                voiceInputViewModel = voiceInputViewModel,
                isVoiceModeActive = isVoiceModeActive
            )
        }

        // Диалог запроса разрешения
        if (showPermissionDialog) {
            PermissionDialog(
                onDismiss = { voiceInputViewModel.dismissPermissionDialog() },
                onConfirm = {
                    if (permissionPermanentlyDenied) {
                        voiceInputViewModel.openAppSettings()
                    } else {
                        // Запрашиваем разрешение через ActivityResult API
                        // Это должно быть реализовано в MainActivity
                        // Пока показываем сообщение
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Пожалуйста, разрешите доступ к микрофону в настройках",
                                duration = SnackbarDuration.Long
                            )
                        }
                    }
                    voiceInputViewModel.dismissPermissionDialog()
                },
                isPermanentlyDenied = permissionPermanentlyDenied
            )
        }

        ChatHistoryBottomSheet(
            showSheet = showHistorySheet,
            onDismiss = { showHistorySheet = false },
            onSessionSelected = { sessionId ->
                isHistoryLoading = true
                viewModel.loadChatSession(sessionId)
                showHistorySheet = false
                isVoiceModeActive = false
                voiceInputViewModel.stopListening()
                message = "" // Очищаем поле ввода при загрузке истории
                coroutineScope.launch {
                    delay(1000)
                    viewModel.forceResetLoading()
                    isHistoryLoading = false
                }
            },
            onDeleteSession = { sessionId ->
                chatHistoryViewModel.deleteSession(sessionId)
            },
            chatHistoryViewModel = chatHistoryViewModel
        )
    }

    LaunchedEffect(isHistoryLoading) {
        if (isHistoryLoading) {
            delay(1000)
            isHistoryLoading = false
        }
    }
}

// ==================== ДИАЛОГ РАЗРЕШЕНИЯ ====================
@Composable
fun PermissionDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isPermanentlyDenied: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isPermanentlyDenied) "Доступ к микрофону заблокирован" else "Требуется разрешение",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = if (isPermanentlyDenied) {
                    "Вы навсегда заблокировали доступ к микрофону. " +
                            "Пожалуйста, разрешите доступ в настройках приложения для использования голосового ввода."
                } else {
                    "Для голосового ввода необходимо разрешение на запись аудио.\n\n" +
                            "Это позволит вам общаться с ИИ голосом, как с голосовым ассистентом."
                },
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(if (isPermanentlyDenied) "Открыть настройки" else "Понятно")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

// ==================== ПАНЕЛЬ ВВОДА ====================
@Composable
private fun InputPanel(
    modifier: Modifier = Modifier,
    message: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isLoading: Boolean,
    isRecording: Boolean,
    isProcessing: Boolean,
    isListening: Boolean,
    audioLevel: Float,
    onVoiceClick: () -> Unit,
    onVoiceLongClick: () -> Unit,
    voiceInputViewModel: VoiceInputViewModel,
    isVoiceModeActive: Boolean
) {
    val recognizedText by voiceInputViewModel.recognizedText.collectAsState()

    LaunchedEffect(recognizedText) {
        if (recognizedText.isNotBlank()) {
            onMessageChange(recognizedText)
            voiceInputViewModel.clearRecognizedText()
        }
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 28.dp,
            bottomStart = 0.dp,
            bottomEnd = 0.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            VoiceRecordingButton(
                isRecording = isRecording,
                isProcessing = isProcessing,
                isListening = isListening,
                audioLevel = audioLevel,
                onClick = onVoiceClick,
                onLongClick = onVoiceLongClick,
                modifier = Modifier.size(48.dp),
                isVoiceModeActive = isVoiceModeActive
            )

            Spacer(modifier = Modifier.width(8.dp))

            if (isListening || isVoiceModeActive) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            if (isVoiceModeActive)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            if (isVoiceModeActive) "🎤 Режим диалога" else "🎤 Слушаю...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isVoiceModeActive)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.secondary
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            repeat(3) { index ->
                                val infiniteTransition = rememberInfiniteTransition()
                                val alpha by infiniteTransition.animateFloat(
                                    initialValue = 0.3f,
                                    targetValue = 1f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(500 + index * 200),
                                        repeatMode = RepeatMode.Reverse
                                    )
                                )
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(
                                            (if (isVoiceModeActive)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.secondary)
                                                .copy(alpha = alpha)
                                        )
                                )
                            }
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = message,
                    onValueChange = onMessageChange,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            if (isRecording) "Говорите..." else "Введите вопрос...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                    maxLines = 5,
                    enabled = !isLoading && !isRecording && !isProcessing && !isVoiceModeActive
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            SendButton(
                enabled = (message.isNotBlank() || isListening) && !isLoading && !isVoiceModeActive,
                onClick = onSendClick,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VoiceRecordingButton(
    isRecording: Boolean,
    isProcessing: Boolean,
    isListening: Boolean,
    audioLevel: Float,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    isVoiceModeActive: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        )
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        )
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(
                when {
                    isProcessing -> MaterialTheme.colorScheme.tertiaryContainer
                    isListening || isVoiceModeActive ->
                        if (isVoiceModeActive)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.secondaryContainer
                    isRecording -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.primaryContainer
                }
            )
            .scale(
                when {
                    isListening || isVoiceModeActive -> pulse
                    isRecording -> scale
                    else -> 1f
                }
            )
            .alpha(
                when {
                    isListening || isVoiceModeActive -> alpha
                    isRecording -> alpha
                    else -> 1f
                }
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                enabled = !isProcessing
            ),
        contentAlignment = Alignment.Center
    ) {
        when {
            isProcessing -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            isListening || isVoiceModeActive -> {
                ListeningIndicator(isVoiceModeActive)
            }
            isRecording -> {
                RecordingIndicator(audioLevel)
            }
            else -> {
                Icon(
                    Icons.Default.Mic,
                    contentDescription = "Голосовой ввод",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun ListeningIndicator(isVoiceModeActive: Boolean) {
    val color = if (isVoiceModeActive)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.secondary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.3f))
        )
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Mic,
                contentDescription = if (isVoiceModeActive) "Режим диалога" else "Слушаю...",
                tint = if (isVoiceModeActive)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun RecordingIndicator(audioLevel: Float) {
    val errorColor = MaterialTheme.colorScheme.error

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Stop,
            contentDescription = "Остановить запись",
            tint = errorColor,
            modifier = Modifier.size(24.dp)
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            val barWidth = size.width / 8
            val barHeight = size.height * audioLevel * 0.8f
            drawRect(
                color = errorColor,
                size = Size(barWidth, barHeight),
                topLeft = Offset(
                    (size.width - barWidth) / 2,
                    (size.height - barHeight) / 2
                )
            )
        }
    }
}

// ==================== КНОПКА ОТПРАВКИ ====================
@Composable
private fun SendButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                if (enabled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Send,
            contentDescription = "Отправить",
            tint = if (enabled) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            modifier = Modifier.size(20.dp)
        )
    }
}

// ==================== ВЕРХНЯЯ ПАНЕЛЬ ====================
@Composable
private fun ChatTopAppBar(
    onBackClick: () -> Unit,
    onClearChat: () -> Unit,
    onSaveChat: () -> Unit,
    onHistoryClick: () -> Unit,
    onVoiceSettingsClick: () -> Unit,
    onStopSpeakingClick: () -> Unit,
    onClearCacheClick: () -> Unit,
    onVoiceModeToggle: () -> Unit,
    isVoiceEnabled: Boolean,
    isSpeaking: Boolean,
    isVoiceModeActive: Boolean,
    isChatEmpty: Boolean,
    isLoading: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(56.dp)
                        .padding(0.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Icon(
                    Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(0.dp)
                )

                Text(
                    "GigaChat",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 0.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Кнопка голосового режима
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .padding(0.dp)
                        .clickable(enabled = isVoiceEnabled) {
                            onVoiceModeToggle()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    BadgedBox(
                        badge = {
                            if (isVoiceModeActive) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(8.dp)
                                )
                            }
                        }
                    ) {
                        Icon(
                            if (isVoiceModeActive) Icons.Filled.VoiceChat else Icons.Outlined.VoiceChat,
                            contentDescription = "Голосовой режим",
                            tint = if (isVoiceModeActive)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Кнопка остановки речи
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .padding(0.dp)
                        .clickable(enabled = isSpeaking) {
                            onStopSpeakingClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSpeaking) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Stop,
                                contentDescription = "Остановить",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Кнопка истории
                IconButton(
                    onClick = onHistoryClick,
                    enabled = !isLoading,
                    modifier = Modifier
                        .size(56.dp)
                        .padding(0.dp)
                ) {
                    Icon(
                        Icons.Outlined.History,
                        contentDescription = "История диалогов",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Кнопка меню
                IconButton(
                    onClick = { expanded = true },
                    enabled = !isLoading,
                    modifier = Modifier
                        .size(56.dp)
                        .padding(0.dp)
                ) {
                    BadgedBox(
                        badge = {
                            if (isVoiceEnabled || !isChatEmpty) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(8.dp)
                                )
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Меню",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .width(240.dp)
                        .background(
                            MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    // Настройки голоса
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    if (isVoiceEnabled) Icons.Filled.VoiceChat else Icons.Outlined.VoiceChat,
                                    contentDescription = null,
                                    tint = if (isVoiceEnabled)
                                        MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text("Настройки голоса")
                                if (isVoiceEnabled) {
                                    Spacer(modifier = Modifier.weight(1f))
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(8.dp)
                                    )
                                }
                            }
                        },
                        onClick = {
                            expanded = false
                            onVoiceSettingsClick()
                        },
                        modifier = Modifier.height(48.dp)
                    )

                    if (!isChatEmpty) {
                        Divider(modifier = Modifier.padding(horizontal = 8.dp))

                        // Сохранить диалог - ИСПРАВЛЕНО
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Save,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text("Сохранить диалог")
                                }
                            },
                            onClick = {
                                expanded = false
                                onSaveChat() // Теперь вызывает правильную функцию
                            },
                            modifier = Modifier.height(48.dp)
                        )
                    }

                    Divider(modifier = Modifier.padding(horizontal = 8.dp))

                    // Очистить кэш озвучки
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text("Очистить кэш озвучки")
                            }
                        },
                        onClick = {
                            expanded = false
                            onClearCacheClick()
                        },
                        modifier = Modifier.height(48.dp)
                    )

                    if (!isChatEmpty) {
                        Divider(modifier = Modifier.padding(horizontal = 8.dp))

                        // Очистить чат
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        "Очистить чат",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            onClick = {
                                expanded = false
                                onClearChat()
                            },
                            modifier = Modifier.height(48.dp)
                        )
                    }
                }
            }
        }
    }
}

// ==================== BOTTOM SHEET ИСТОРИИ ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatHistoryBottomSheet(
    showSheet: Boolean,
    onDismiss: () -> Unit,
    onSessionSelected: (Long) -> Unit,
    onDeleteSession: (Long) -> Unit,
    chatHistoryViewModel: ChatHistoryViewModel = hiltViewModel()
) {
    val sessions by chatHistoryViewModel.sessions.collectAsState()
    val isLoading by chatHistoryViewModel.isLoading.collectAsState()
    val error by chatHistoryViewModel.error.collectAsState()

    LaunchedEffect(showSheet) {
        if (showSheet) {
            chatHistoryViewModel.loadSessions()
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "История диалогов",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        if (sessions.isNotEmpty()) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 4.dp)
                            ) {
                                Text(
                                    text = "${sessions.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (error != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = error!!,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                key(sessions.size) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (sessions.isNotEmpty()) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.heightIn(max = 400.dp)
                        ) {
                            items(
                                items = sessions,
                                key = { "session_${it.id}" }
                            ) { session ->
                                SessionCard(
                                    session = session,
                                    onSelect = {
                                        onSessionSelected(session.id)
                                        onDismiss()
                                    },
                                    onDelete = {
                                        onDeleteSession(session.id)
                                    }
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Outlined.History,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Нет сохранённых диалогов",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Нажмите ⋮ → Сохранить диалог",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// ==================== КАРТОЧКА СЕССИИ ====================
@Composable
fun SessionCard(
    session: ChatSession,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Chat,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = session.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = session.preview,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                        Text(
                            text = formatDate(session.timestamp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            Icons.Default.Forum,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "${session.messageCount} ${pluralize(session.messageCount, "сообщение", "сообщения", "сообщений")}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            IconButton(
                onClick = { showDeleteConfirmation = true },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Удалить",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Удаление диалога") },
            text = { Text("Вы уверены, что хотите удалить этот диалог?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

// ==================== ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ ====================
private fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val now = Date()
    val diff = now.time - timestamp
    return when {
        diff < 24 * 60 * 60 * 1000 -> "Сегодня"
        diff < 48 * 60 * 60 * 1000 -> "Вчера"
        else -> {
            val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            format.format(date)
        }
    }
}

private fun pluralize(count: Int, one: String, few: String, many: String): String {
    return when {
        count % 10 == 1 && count % 100 != 11 -> one
        count % 10 in 2..4 && (count % 100 !in 12..14) -> few
        else -> many
    }
}

// ==================== ПУСТОЙ ЧАТ ====================
@Composable
private fun EmptyChatContent(
    modifier: Modifier = Modifier,
    onExampleClick: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val infiniteTransition = rememberInfiniteTransition()
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000),
                repeatMode = RepeatMode.Reverse
            )
        )

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
                .scale(scale),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.SmartToy,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "GigaChat помощник",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold
            ),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Задайте вопрос по Kotlin/Android разработке",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Text(
            "Я помогу с кодом, объяснениями и советами",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    "📋 Быстрые вопросы",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                QuickQuestionChip(
                    text = "val vs var",
                    icon = Icons.Default.Code,
                    onClick = { onExampleClick("val_var") }
                )
                QuickQuestionChip(
                    text = "Функции высшего порядка",
                    icon = Icons.Default.Functions,
                    onClick = { onExampleClick("higher_order") }
                )
                QuickQuestionChip(
                    text = "Корутины",
                    icon = Icons.Default.Sync,
                    onClick = { onExampleClick("coroutines") }
                )
                QuickQuestionChip(
                    text = "Собеседование",
                    icon = Icons.Default.Work,
                    onClick = { onExampleClick("interview_tips") }
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun QuickQuestionChip(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        onClick = onClick
    ) {
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
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            Icon(
                Icons.Default.Send,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ==================== ИНДИКАТОР ЗАГРУЗКИ ====================
@Composable
private fun LoadingIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 1.dp,
            modifier = Modifier.widthIn(max = 120.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Печатает...",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// ==================== СООБЩЕНИЕ ====================
@Composable
private fun AnimatedMessageItem(
    message: AIMessage,
    isVoiceEnabled: Boolean,
    voiceOutputViewModel: VoiceOutputViewModel,
    isHistoryLoading: Boolean
) {
    var isPlaying by remember { mutableStateOf(false) }
    val isSpeaking by voiceOutputViewModel.isSpeaking.collectAsState()
    val currentMessageId by voiceOutputViewModel.currentMessageId.collectAsState()

    LaunchedEffect(message.text, message.isUser, isVoiceEnabled, isHistoryLoading) {
        if (!message.isUser && isVoiceEnabled && !isHistoryLoading) {
            if (currentMessageId == message.timestamp) {
                println("⏭️ Сообщение уже озвучивается, пропускаем")
            } else {
                delay(500)
                isPlaying = true
                voiceOutputViewModel.speakText(message.text, message.timestamp)
                isPlaying = false
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
        ) {
            MessageBubble(
                message = message,
                isPlaying = isPlaying || (currentMessageId == message.timestamp && isSpeaking),
                onPlayClick = {
                    voiceOutputViewModel.speakText(message.text, message.timestamp)
                }
            )
            Text(
                text = formatTime(message.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(
                    top = 4.dp,
                    start = if (message.isUser) 0.dp else 8.dp,
                    end = if (message.isUser) 8.dp else 0.dp
                )
            )
        }
    }
}

@Composable
private fun MessageBubble(
    message: AIMessage,
    isPlaying: Boolean,
    onPlayClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = if (message.isUser) 16.dp else 4.dp,
            bottomEnd = if (message.isUser) 4.dp else 16.dp
        ),
        color = if (message.isUser)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = if (message.isUser) 0.dp else 1.dp,
        shadowElevation = 2.dp,
        modifier = Modifier
            .widthIn(max = 280.dp)
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            if (!message.isUser) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.SmartToy,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "GigaChat",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    IconButton(
                        onClick = onPlayClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        if (isPlaying) {
                            val infiniteTransition = rememberInfiniteTransition()
                            val alpha by infiniteTransition.animateFloat(
                                initialValue = 0.5f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(500),
                                    repeatMode = RepeatMode.Reverse
                                )
                            )
                            Icon(
                                Icons.Default.VolumeUp,
                                contentDescription = "Воспроизводится",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Icon(
                                Icons.Default.VolumeUp,
                                contentDescription = "Озвучить",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (message.isUser)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )
        }
    }
}

// ==================== ДИАЛОГ ВЫБОРА ЭМОЦИИ ====================
@Composable
private fun EmotionPickerDialog(
    onDismiss: () -> Unit,
    onEmotionSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Выберите эмоцию",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EmotionChip("neutral", "Нейтральный", onEmotionSelected)
                EmotionChip("good", "Добрый", onEmotionSelected)
                EmotionChip("evil", "Злой", onEmotionSelected)
                EmotionChip("sad", "Грустный", onEmotionSelected)
                EmotionChip("whisper", "Шёпот", onEmotionSelected)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
private fun EmotionChip(
    emotionId: String,
    emotionName: String,
    onSelect: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        onClick = { onSelect(emotionId) }
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                emotionName,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// ==================== ФОРМАТИРОВАНИЕ ВРЕМЕНИ ====================
private fun formatTime(timestamp: Long): String {
    val time = Date(timestamp)
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
    return format.format(time)
}