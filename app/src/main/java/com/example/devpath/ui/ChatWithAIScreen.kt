// ui/ChatWithAIScreen.kt
package com.example.devpath.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.devpath.domain.models.AIMessage
import com.example.devpath.ui.components.VoiceSettingsDialog
import com.example.devpath.ui.viewmodel.ChatViewModel
import com.example.devpath.ui.viewmodel.VoiceInputViewModel
import com.example.devpath.ui.viewmodel.VoiceOutputViewModel
import kotlinx.coroutines.delay

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

    // Состояния UI
    var showVoiceSettings by remember { mutableStateOf(false) }
    var showEmotionPicker by remember { mutableStateOf(false) }
    val isVoiceEnabled by voiceOutputViewModel.isVoiceEnabled.collectAsState()
    val selectedVoice by voiceOutputViewModel.selectedVoice.collectAsState()
    val isSpeaking by voiceOutputViewModel.isSpeaking.collectAsState()
    val isRecording by voiceInputViewModel.isRecording.collectAsState()
    val isProcessing by voiceInputViewModel.isProcessing.collectAsState()
    val voiceSpeed by voiceOutputViewModel.voiceSpeed.collectAsState()

    // Автопрокрутка при новых сообщениях
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    // Обработка ошибок
    LaunchedEffect(error) {
        error?.let {
            println("❌ Chat error: $it")
            viewModel.clearError()
        }
    }

    // Диалог настроек голоса
    if (showVoiceSettings) {
        VoiceSettingsDialog(
            showDialog = showVoiceSettings,
            currentVoice = selectedVoice,
            currentSpeed = voiceSpeed,
            onDismiss = { showVoiceSettings = false },
            onVoiceSelected = { voiceId ->
                voiceOutputViewModel.setVoice(voiceId)
            },
            onSpeedSelected = { speed ->
                voiceOutputViewModel.setVoiceSpeed(speed)
            }
        )
    }

    // Диалог выбора эмоции
    if (showEmotionPicker) {
        EmotionPickerDialog(
            onDismiss = { showEmotionPicker = false },
            onEmotionSelected = { emotion ->
                voiceOutputViewModel.setEmotion(emotion)
                showEmotionPicker = false
            }
        )
    }

    // ✅ КОРНЕВОЙ КОНТЕЙНЕР - НИКАКИХ ОТСТУПОВ, ВСЁ ВПЛОТНУЮ!
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding(), // Только для клавиатуры
        verticalArrangement = Arrangement.Top
    ) {
        ChatTopAppBar(
            onBackClick = onBackToHome,
            onClearChat = { viewModel.clearChat() },
            onSaveChat = { viewModel.saveCurrentChat() },
            onVoiceSettingsClick = { showVoiceSettings = true },
            isVoiceEnabled = isVoiceEnabled,
            isSpeaking = isSpeaking,
            isChatEmpty = messages.isEmpty(),
            isLoading = isLoading
        )

        // ✅ 2. КОНТЕНТ – ЗАНИМАЕТ ВСЁ ОСТАВШЕЕСЯ МЕСТО
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
                        voiceOutputViewModel = voiceOutputViewModel
                    )
                }
            }
        }

        // ✅ 3. ПАНЕЛЬ ВВОДА – ПРИЖАТА К НИЗУ ВПЛОТНУЮ, БЕЗ ОТСТУПОВ!
        InputPanel(
            modifier = Modifier.fillMaxWidth(),
            message = message,
            onMessageChange = { message = it },
            onSendClick = {
                if (message.isNotBlank() && !isLoading) {
                    viewModel.sendMessage(message)
                    message = ""
                    keyboardController?.hide()
                }
            },
            isLoading = isLoading,
            isRecording = isRecording,
            isProcessing = isProcessing,
            onVoiceClick = {
                if (isRecording) {
                    voiceInputViewModel.stopRecordingAndRecognize { recognizedText ->
                        message = recognizedText
                        if (recognizedText.isNotBlank()) {
                            viewModel.sendMessage(recognizedText)
                            message = ""
                        }
                    }
                } else {
                    voiceInputViewModel.startRecording()
                }
            },
            voiceInputViewModel = voiceInputViewModel
        )
    }
}

// ==================== ВЕРХНЯЯ ПАНЕЛЬ – ПОЛНОСТЬЮ ГОТОВАЯ ====================
@Composable
private fun ChatTopAppBar(
    onBackClick: () -> Unit,
    onClearChat: () -> Unit,
    onSaveChat: () -> Unit,
    onVoiceSettingsClick: () -> Unit,
    isVoiceEnabled: Boolean,
    isSpeaking: Boolean,
    isChatEmpty: Boolean,
    isLoading: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp), // Стандартная высота AppBar
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 0.dp), // НОЛЬ отступов по бокам!
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // --- ЛЕВАЯ ЧАСТЬ - БЕЗ ОТСТУПОВ ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp) // НОЛЬ отступов между элементами!
            ) {
                // Кнопка назад – полный размер 56x56, без padding
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

                // Логотип – без отступов
                Icon(
                    Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(0.dp)
                )

                // Название – без отступа слева
                Text(
                    "GigaChat",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 0.dp)
                )
            }

            // --- ПРАВАЯ ЧАСТЬ - ВСЕ КНОПКИ 56x56, БЕЗ ОТСТУПОВ ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp) // НОЛЬ отступов между кнопками!
            ) {
                // 1. ИНДИКАТОР РЕЧИ – всегда занимает место
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .padding(0.dp),
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
                            SpeakingAnimation()
                        }
                    }
                }

                // 2. КНОПКА НАСТРОЕК ГОЛОСА
                IconButton(
                    onClick = onVoiceSettingsClick,
                    enabled = !isLoading,
                    modifier = Modifier
                        .size(56.dp)
                        .padding(0.dp)
                ) {
                    BadgedBox(
                        badge = {
                            if (isVoiceEnabled) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(8.dp)
                                )
                            }
                        }
                    ) {
                        Icon(
                            if (isVoiceEnabled) Icons.Filled.VoiceChat else Icons.Outlined.VoiceChat,
                            contentDescription = "Настройки голоса",
                            tint = if (isVoiceEnabled)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // 3. КНОПКА СОХРАНЕНИЯ ЧАТА
                if (!isChatEmpty) {
                    IconButton(
                        onClick = onSaveChat,
                        enabled = !isLoading,
                        modifier = Modifier
                            .size(56.dp)
                            .padding(0.dp)
                    ) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = "Сохранить диалог",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    // Резервируем место, чтобы панель не прыгала
                    Spacer(modifier = Modifier.size(56.dp))
                }

                // 4. КНОПКА ОЧИСТКИ ЧАТА
                if (!isChatEmpty) {
                    IconButton(
                        onClick = onClearChat,
                        enabled = !isLoading,
                        modifier = Modifier
                            .size(56.dp)
                            .padding(0.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Очистить чат",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    // Резервируем место
                    Spacer(modifier = Modifier.size(56.dp))
                }
            }
        }
    }
}

// ==================== АНИМАЦИЯ РЕЧИ ====================
@Composable
private fun SpeakingAnimation() {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        )
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        )
    )
    Icon(
        Icons.Default.VolumeUp,
        contentDescription = "Говорит",
        tint = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
        modifier = Modifier
            .scale(scale)
            .size(20.dp)
    )
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
    voiceOutputViewModel: VoiceOutputViewModel
) {
    var isPlaying by remember { mutableStateOf(false) }
    val isSpeaking by voiceOutputViewModel.isSpeaking.collectAsState()
    val currentMessageId by voiceOutputViewModel.currentMessageId.collectAsState()

    LaunchedEffect(message.text, message.isUser, isVoiceEnabled) {
        if (!message.isUser && isVoiceEnabled && currentMessageId != message.timestamp) {
            delay(500)
            isPlaying = true
            voiceOutputViewModel.speakText(message.text, message.timestamp)
            isPlaying = false
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

// ==================== ПАНЕЛЬ ВВОДА – БЕЗ ОТСТУПОВ, ВПЛОТНУЮ! ====================
@Composable
private fun InputPanel(
    modifier: Modifier = Modifier,
    message: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isLoading: Boolean,
    isRecording: Boolean,
    isProcessing: Boolean,
    onVoiceClick: () -> Unit,
    voiceInputViewModel: VoiceInputViewModel
) {
    val recognizedText by voiceInputViewModel.recognizedText.collectAsState()

    LaunchedEffect(recognizedText) {
        if (recognizedText.isNotBlank()) {
            onMessageChange(recognizedText)
            voiceInputViewModel.clearRecognizedText()
        }
    }

    Card(
        modifier = modifier, // 👈 НИКАКИХ PADDING! ВПЛОТНУЮ!
        shape = RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 28.dp,
            bottomStart = 0.dp, // 👈 ПРЯМОЙ КРАЙ СНИЗУ!
            bottomEnd = 0.dp     // 👈 ПРЯМОЙ КРАЙ СНИЗУ!
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
                onClick = onVoiceClick,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

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
                enabled = !isLoading && !isRecording && !isProcessing
            )

            Spacer(modifier = Modifier.width(8.dp))

            SendButton(
                enabled = message.isNotBlank() && !isLoading,
                onClick = onSendClick,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

// ==================== КНОПКА ГОЛОСОВОЙ ЗАПИСИ ====================
@Composable
private fun VoiceRecordingButton(
    isRecording: Boolean,
    isProcessing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
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

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                when {
                    isProcessing -> MaterialTheme.colorScheme.tertiaryContainer
                    isRecording -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.primaryContainer
                }
            )
            .scale(if (isRecording) scale else 1f)
            .alpha(if (isRecording) alpha else 1f)
            .clickable(enabled = !isProcessing, onClick = onClick),
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
            isRecording -> {
                Icon(
                    Icons.Default.Stop,
                    "Остановить запись",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                SoundWaves()
            }
            else -> {
                Icon(
                    Icons.Default.Mic,
                    "Голосовой ввод",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun SoundWaves() {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val infiniteTransition = rememberInfiniteTransition()
            val height by infiniteTransition.animateFloat(
                initialValue = 4f,
                targetValue = 12f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500 + index * 100),
                    repeatMode = RepeatMode.Reverse
                )
            )
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(height.dp)
                    .background(
                        MaterialTheme.colorScheme.error,
                        CircleShape
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
    val time = java.util.Date(timestamp)
    val format = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
    return format.format(time)
}