// ui/InterviewSimulationScreen.kt
package com.example.devpath.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.devpath.ui.viewmodel.InterviewViewModel
import com.example.devpath.ui.viewmodel.VoiceInputViewModel
import com.example.devpath.ui.viewmodel.VoiceOutputViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

// Импортируем ResumeData из другого файла
import com.example.devpath.ui.ResumeData

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun InterviewSimulationScreen(
    navController: NavHostController,
    viewModel: InterviewViewModel,
    voiceInputViewModel: VoiceInputViewModel,
    voiceOutputViewModel: VoiceOutputViewModel,
    resumeData: ResumeData? = null
) {
    var currentStep by remember { mutableIntStateOf(0) }
    var isRecording by remember { mutableStateOf(false) }
    var userAnswer by remember { mutableStateOf("") }
    var interviewCompleted by remember { mutableStateOf(false) }
    var showAnalysisDialog by remember { mutableStateOf(false) }
    var timeRemaining by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    val isTimerRunning = remember { mutableStateOf(true) }

    // Для голосового ввода
    val recognizedText by voiceInputViewModel.recognizedText.collectAsState()
    val isProcessing by voiceInputViewModel.isProcessing.collectAsState()
    val audioLevel by voiceInputViewModel.audioLevel.collectAsState()

    // Для скролла
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val configuration = LocalConfiguration.current

    val answers by viewModel.answers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val analysisResult by viewModel.analysisResult.collectAsState()

    val interviewSteps = listOf(
        EnhancedInterviewStep(
            id = 1,
            title = "Введение",
            question = "Расскажите о себе и своем опыте в разработке на Kotlin.",
            timeLimit = 180,
            difficulty = 2,
            category = "Soft Skills",
            tips = listOf(
                "Расскажите о вашем образовании и опыте",
                "Упомяните ключевые проекты",
                "Расскажите о ваших сильных сторонах",
                "Объясните почему выбрали Kotlin"
            )
        ),
        EnhancedInterviewStep(
            id = 2,
            title = "Базовые концепции",
            question = "Объясните разницу между val и var, а также между class и data class в Kotlin.",
            timeLimit = 120,
            difficulty = 3,
            category = "Kotlin Basics",
            tips = listOf(
                "val - неизменяемая ссылка, var - изменяемая",
                "data class автоматически генерирует методы",
                "Приведите примеры использования"
            )
        ),
        EnhancedInterviewStep(
            id = 3,
            title = "Корутины",
            question = "Что такое корутины и как они отличаются от потоков? Объясните разницу между launch и async.",
            timeLimit = 180,
            difficulty = 4,
            category = "Concurrency",
            tips = listOf(
                "Корутины легковеснее потоков",
                "launch - для fire-and-forget операций",
                "async - для получения результата",
                "Объясните suspend функции"
            )
        ),
        EnhancedInterviewStep(
            id = 4,
            title = "Архитектура",
            question = "Опишите ваш опыт работы с MVVM/MVI архитектурой в Android. Как вы реализуете ViewModel?",
            timeLimit = 150,
            difficulty = 4,
            category = "Architecture",
            tips = listOf(
                "Объясните паттерн Observer",
                "Расскажите про LiveData/StateFlow",
                "Упомяните lifecycle-aware компоненты"
            )
        ),
        EnhancedInterviewStep(
            id = 5,
            title = "Практическая задача",
            question = "Напишите функцию, которая находит все пары чисел в массиве, сумма которых равна заданному числу. Оцените сложность алгоритма.",
            timeLimit = 300,
            difficulty = 5,
            category = "Algorithms",
            tips = listOf(
                "Можно использовать HashMap для O(n) решения",
                "Обсудите trade-offs разных подходов",
                "Подумайте о граничных случаях"
            )
        ),
        EnhancedInterviewStep(
            id = 6,
            title = "Вопросы к компании",
            question = "Есть ли у вас вопросы к нам о компании или процессе работы?",
            timeLimit = 120,
            difficulty = 1,
            category = "Culture Fit",
            tips = listOf(
                "Спросите о стеке технологий",
                "Узнайте о процессе разработки",
                "Спросите о возможностях роста",
                "Узнайте о корпоративной культуре"
            )
        )
    )

    // Инициализируем таймер при смене шага
    LaunchedEffect(currentStep) {
        userAnswer = answers[currentStep] ?: ""
        timeRemaining = interviewSteps[currentStep].timeLimit
        isTimerRunning.value = true
        scrollState.scrollTo(0)
    }

    // Таймер обратного отсчёта для каждого вопроса
    LaunchedEffect(currentStep, isTimerRunning.value) {
        if (isTimerRunning.value && !interviewCompleted) {
            while (timeRemaining > 0 && isTimerRunning.value) {
                delay(1000L)
                timeRemaining--
            }
            if (timeRemaining == 0) {
                isTimerRunning.value = false
                // Автоматически сохраняем пустой ответ и переходим дальше
                if (!interviewCompleted) {
                    viewModel.saveAnswer(currentStep, "")
                    if (currentStep < interviewSteps.size - 1) {
                        currentStep++
                        userAnswer = answers[currentStep] ?: ""
                    } else {
                        interviewCompleted = true
                    }
                }
            }
        }
    }

    // Обработка распознанного текста
    LaunchedEffect(recognizedText) {
        if (recognizedText.isNotBlank()) {
            userAnswer = recognizedText
            voiceInputViewModel.clearRecognizedText()

            // Автоскролл к полю ввода
            coroutineScope.launch {
                delay(100)
                scrollState.animateScrollTo(scrollState.maxValue)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadAnswers()
    }

    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            println("Interview error: $errorMessage")
        }
    }

    if (showAnalysisDialog) {
        AlertDialog(
            onDismissRequest = {
                showAnalysisDialog = false
                viewModel.clearAnalysis()
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Analytics,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Анализ собеседования от GigaChat",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(4.dp)
                ) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(48.dp),
                                    strokeWidth = 4.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "GigaChat анализирует ваши ответы...",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                )
                            }
                        }
                    } else {
                        AnalysisResultContent(analysisResult)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showAnalysisDialog = false
                        viewModel.clearAnalysis()
                    }
                ) {
                    Text("Закрыть")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Анимированный фон
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    val infiniteTransition = rememberInfiniteTransition()
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val width = size.width
                val height = size.height
                val brush = Brush.linearGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.05f),
                        secondaryColor.copy(alpha = 0.05f),
                        tertiaryColor.copy(alpha = 0.05f)
                    ),
                    start = Offset(gradientOffset % width, 0f),
                    end = Offset((gradientOffset + width) % width, height)
                )
                drawRect(brush = brush)
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            TopBarWithProgress(
                currentStep = currentStep,
                totalSteps = interviewSteps.size,
                timeRemaining = timeRemaining,
                timeLimit = interviewSteps[currentStep].timeLimit,
                onBackClick = {
                    focusManager.clearFocus()
                    navController.popBackStack()
                }
            )

            if (!interviewCompleted) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp)
                ) {
                    EnhancedInterviewStepContent(
                        step = interviewSteps[currentStep],
                        userAnswer = userAnswer,
                        onAnswerChange = { userAnswer = it },
                        isRecording = isRecording,
                        isProcessing = isProcessing,
                        audioLevel = audioLevel,
                        onRecordingClick = {
                            if (isRecording) {
                                voiceInputViewModel.stopRecordingAndRecognize()
                            } else {
                                voiceInputViewModel.startRecording()
                            }
                            isRecording = !isRecording
                        },
                        answersCount = answers.size,
                        totalSteps = interviewSteps.size,
                        interviewerAvatar = getInterviewerAvatar(currentStep)
                    )

                    Spacer(modifier = Modifier.height(80.dp))
                }

                // Нижняя панель навигации (только кнопки "Назад" и "Далее/Завершить")
                BottomNavigationPanel(
                    currentStep = currentStep,
                    totalSteps = interviewSteps.size,
                    onPrevious = {
                        focusManager.clearFocus()
                        if (currentStep > 0) {
                            if (userAnswer.isNotBlank()) {
                                viewModel.saveAnswer(currentStep, userAnswer)
                            }
                            currentStep--
                            userAnswer = answers[currentStep] ?: ""
                        }
                    },
                    onNext = {
                        focusManager.clearFocus()
                        if (userAnswer.isNotBlank()) {
                            viewModel.saveAnswer(currentStep, userAnswer)
                        }
                        if (currentStep < interviewSteps.size - 1) {
                            currentStep++
                            userAnswer = answers[currentStep] ?: ""
                        } else {
                            interviewCompleted = true
                        }
                    },
                    isLoading = isLoading
                )
            } else {
                EnhancedInterviewCompletedContent(
                    answers = answers,
                    interviewSteps = interviewSteps,
                    resumeData = resumeData,
                    onRestart = {
                        focusManager.clearFocus()
                        currentStep = 0
                        userAnswer = ""
                        viewModel.clearAnswers()
                        viewModel.clearAnalysis()
                        interviewCompleted = false
                    },
                    onAnalyze = {
                        focusManager.clearFocus()
                        viewModel.analyzeFullInterview(
                            interviewSteps = interviewSteps,
                            answers = answers,
                            resumeData = resumeData
                        )
                        showAnalysisDialog = true
                    },
                    onBackToMain = {
                        focusManager.clearFocus()
                        navController.popBackStack()
                    },
                    isLoading = isLoading
                )
            }
        }
    }
}

@Composable
private fun TopBarWithProgress(
    currentStep: Int,
    totalSteps: Int,
    timeRemaining: Int,
    timeLimit: Int,
    onBackClick: () -> Unit
) {
    val progress = (currentStep + 1f) / totalSteps
    val timeProgress = timeRemaining.toFloat() / timeLimit.toFloat()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = Color.Transparent,
        tonalElevation = 0.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    "Вопрос ${currentStep + 1}/$totalSteps",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 4.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                // Индикатор времени
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            progress = { timeProgress },
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = when {
                                timeRemaining < 30 -> MaterialTheme.colorScheme.error
                                timeRemaining < 60 -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formatTime(timeRemaining),
                            style = MaterialTheme.typography.labelMedium,
                            color = when {
                                timeRemaining < 30 -> MaterialTheme.colorScheme.error
                                timeRemaining < 60 -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.primary
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
            }

            // Прогресс-бар вопросов
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
private fun EnhancedInterviewStepContent(
    step: EnhancedInterviewStep,
    userAnswer: String,
    onAnswerChange: (String) -> Unit,
    isRecording: Boolean,
    isProcessing: Boolean,
    audioLevel: Float,
    onRecordingClick: () -> Unit,
    answersCount: Int,
    totalSteps: Int,
    interviewerAvatar: InterviewerAvatar
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        InterviewerAvatarView(avatar = interviewerAvatar)

        Spacer(modifier = Modifier.height(12.dp))

        // Категория и сложность
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = step.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Сложность:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 4.dp)
                )
                repeat(5) { starIndex ->
                    Icon(
                        imageVector = if (starIndex < step.difficulty) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = null,
                        tint = if (starIndex < step.difficulty) Color(0xFFFFB800) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Статус ответов
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Отвечено: $answersCount/$totalSteps",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Карточка вопроса с эффектом печатной машинки
        TypewriterQuestionCard(step = step)

        Spacer(modifier = Modifier.height(16.dp))

        // Поле ввода ответа
        OutlinedTextField(
            value = userAnswer,
            onValueChange = onAnswerChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp, max = screenHeight * 0.3f),
            placeholder = {
                Text(
                    text = "Введите ваш ответ здесь...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            },
            maxLines = 8,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            enabled = !isProcessing
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Кнопка голосового ответа
        VoiceRecordingButton(
            isRecording = isRecording,
            isProcessing = isProcessing,
            audioLevel = audioLevel,
            onClick = onRecordingClick
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Советы
        TipsCard(tips = step.tips)
    }
}

@Composable
private fun VoiceRecordingButton(
    isRecording: Boolean,
    isProcessing: Boolean,
    audioLevel: Float,
    onClick: () -> Unit
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

    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isRecording)
                MaterialTheme.colorScheme.error
            else if (isProcessing)
                MaterialTheme.colorScheme.tertiary
            else
                MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(12.dp),
        enabled = !isProcessing
    ) {
        if (isProcessing) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Обработка голоса...")
        } else {
            Icon(
                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                contentDescription = null,
                modifier = Modifier
                    .size(18.dp)
                    .scale(if (isRecording) scale else 1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isRecording) "Остановить запись" else "Голосовой ответ"
            )
        }
    }
}

@Composable
private fun TypewriterQuestionCard(step: EnhancedInterviewStep) {
    var displayedText by remember { mutableStateOf("") }

    LaunchedEffect(step.id) {
        displayedText = ""
        val chars = step.question.toCharArray()
        for (i in chars.indices) {
            delay(30)
            displayedText += chars[i]
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = step.id.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = displayedText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
private fun InterviewerAvatarView(avatar: InterviewerAvatar) {
    val infiniteTransition = rememberInfiniteTransition()
    val floatAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .offset(y = floatAnimation.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = avatar.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = avatar.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = avatar.role,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TipsCard(tips: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f)
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Советы для ответа:",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            tips.forEach { tip ->
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavigationPanel(
    currentStep: Int,
    totalSteps: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    isLoading: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        shape = RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = 0.dp,
            bottomEnd = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Кнопка назад
            Button(
                onClick = onPrevious,
                enabled = currentStep > 0 && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Назад")
            }

            // Кнопка далее/завершить
            Button(
                onClick = onNext,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = if (currentStep == totalSteps - 1) "Завершить" else "Далее"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (currentStep == totalSteps - 1)
                            Icons.Default.Check
                        else
                            Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EnhancedInterviewCompletedContent(
    answers: Map<Int, String>,
    interviewSteps: List<EnhancedInterviewStep>,
    resumeData: ResumeData?,
    onRestart: () -> Unit,
    onAnalyze: () -> Unit,
    onBackToMain: () -> Unit,
    isLoading: Boolean
) {
    val scrollState = rememberScrollState()
    val answeredCount = answers.count { (_, value) -> value.isNotBlank() }
    val averageDifficulty = if (interviewSteps.isNotEmpty()) {
        interviewSteps.sumOf { it.difficulty } / interviewSteps.size
    } else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var scale by remember { mutableStateOf(1f) }
        LaunchedEffect(Unit) {
            scale = 1.2f
            delay(200)
            scale = 1f
        }

        Icon(
            Icons.Default.CheckCircle,
            contentDescription = "Завершено",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(80.dp)
                .scale(scale)
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Собеседование завершено!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Информация из резюме
        if (resumeData?.hasData() == true) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Кандидат: ${resumeData.fullName.take(30)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    if (resumeData.skills.isNotBlank()) {
                        Text(
                            "Навыки: ${resumeData.skills.take(50)}...",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Статистика
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = "$answeredCount/${interviewSteps.size}",
                    label = "Отвечено",
                    icon = Icons.Default.CheckCircle
                )
                StatItem(
                    value = "$averageDifficulty/5",
                    label = "Ср. сложность",
                    icon = Icons.Default.Star
                )
                StatItem(
                    value = "${answers.values.sumOf { it.length }}",
                    label = "Всего символов",
                    icon = Icons.Default.TextFields
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Детали по вопросам
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Детали по вопросам",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                interviewSteps.forEachIndexed { index, step ->
                    val answer = answers[index]
                    val isAnswered = answer != null && answer.isNotBlank()
                    AnswerSummaryItem(
                        step = step,
                        isAnswered = isAnswered,
                        answerLength = answer?.length ?: 0
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Кнопки действий
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onAnalyze,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        Icons.Default.Analytics,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Получить анализ от GigaChat")
                }
            }

            OutlinedButton(
                onClick = onRestart,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Пройти заново")
            }

            TextButton(
                onClick = onBackToMain,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Вернуться на главную")
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AnswerSummaryItem(
    step: EnhancedInterviewStep,
    isAnswered: Boolean,
    answerLength: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (isAnswered)
            Color(0xFF10B981).copy(alpha = 0.1f)
        else
            MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Индикатор сложности
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .fillMaxHeight(0.7f)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        when (step.difficulty) {
                            5 -> Color(0xFFFF0000)
                            4 -> Color(0xFFFF8C00)
                            3 -> Color(0xFFFFFF00)
                            2 -> Color(0xFF90EE90)
                            else -> Color(0xFF00FF00)
                        }
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Вопрос ${step.id}: ${step.title}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    step.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Icon(
                    if (isAnswered) Icons.Default.CheckCircle else Icons.Default.Info,
                    contentDescription = null,
                    tint = if (isAnswered)
                        Color(0xFF10B981)
                    else
                        MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
                if (isAnswered) {
                    Text(
                        "$answerLength симв.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalysisResultContent(analysisResult: String?) {
    if (analysisResult.isNullOrBlank()) {
        Text(
            "Нет данных для анализа",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
        return
    }

    val sections = analysisResult.split("\n\n")

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        sections.forEach { section ->
            when {
                section.contains("ОЦЕНКА:") || section.contains("БАЛЛЫ:") -> {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = section,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                section.contains("СОВЕТЫ:") || section.contains("РЕКОМЕНДАЦИИ:") -> {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = section,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                section.contains("СИЛЬНЫЕ СТОРОНЫ:") -> {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = section,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                section.contains("ЧТО УЛУЧШИТЬ:") -> {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = section,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                else -> {
                    Text(
                        text = section,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", minutes, secs)
}

private fun getInterviewerAvatar(step: Int): InterviewerAvatar {
    return when (step % 3) {
        0 -> InterviewerAvatar(Icons.Filled.SmartToy, "Анна", "Senior Android Developer")
        1 -> InterviewerAvatar(Icons.Filled.Person, "Дмитрий", "Tech Lead")
        else -> InterviewerAvatar(Icons.Filled.Star, "Елена", "HR Specialist")
    }
}

data class InterviewerAvatar(
    val icon: ImageVector,
    val name: String,
    val role: String
)

data class EnhancedInterviewStep(
    val id: Int,
    val title: String,
    val question: String,
    val timeLimit: Int,
    val difficulty: Int = 3,
    val category: String = "General",
    val tips: List<String> = emptyList()
)