package com.example.devpath.ui

import android.app.Activity
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.devpath.domain.models.UserProgress
import com.example.devpath.ui.viewmodel.ChatViewModel
import com.example.devpath.ui.viewmodel.InterviewViewModel
import com.example.devpath.ui.viewmodel.ProgressViewModel
import com.example.devpath.ui.viewmodel.VoiceInputViewModel
import com.example.devpath.ui.viewmodel.VoiceOutputViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding

// Enum для главных вкладок учебника
enum class TextbookTab(
    val title: String,
    val icon: ImageVector,
    val route: String
) {
    HOME("Главная", Icons.Default.Home, "textbook_home"),
    CHAT("Чат с ИИ", Icons.Default.SmartToy, "textbook_chat"),
    INTERVIEW_SIM("Собеседование", Icons.Default.Work, "textbook_interview")
}

// Функция расчета уровня из XP
private fun calculateLevelFromXP(totalXP: Int): Int {
    if (totalXP < 100) return 1
    if (totalXP < 250) return 2
    if (totalXP < 450) return 3
    if (totalXP < 700) return 4
    if (totalXP < 1000) return 5
    var xp = totalXP
    var level = 1
    var xpForNextLevel = 100
    while (xp >= xpForNextLevel) {
        xp -= xpForNextLevel
        level++
        xpForNextLevel += 50
    }
    return level
}

// Система достижений (15 достижений)
private val achievementsList = listOf(
    Achievement(
        id = "first_lesson",
        title = "Первый шаг",
        description = "Пройдите первый урок",
        icon = Icons.Rounded.Assignment,
        achieved = false
    ),
    Achievement(
        id = "first_test",
        title = "Эрудит",
        description = "Пройдите первый тест",
        icon = Icons.Rounded.Quiz,
        achieved = false
    ),
    Achievement(
        id = "first_practice",
        title = "Практик",
        description = "Выполните первое задание",
        icon = Icons.Rounded.Code,
        achieved = false
    ),
    Achievement(
        id = "lesson_3",
        title = "Любознательный",
        description = "Пройдите 3 урока",
        icon = Icons.Rounded.MenuBook,
        achieved = false
    ),
    Achievement(
        id = "test_3",
        title = "Знаток",
        description = "Пройдите 3 теста",
        icon = Icons.Rounded.Assessment,
        achieved = false
    ),
    Achievement(
        id = "practice_3",
        title = "Трудолюбивый",
        description = "Выполните 3 задания",
        icon = Icons.Rounded.Build,
        achieved = false
    ),
    Achievement(
        id = "lesson_5",
        title = "Усердный ученик",
        description = "Пройдите 5 уроков",
        icon = Icons.Rounded.School,
        achieved = false
    ),
    Achievement(
        id = "test_5",
        title = "Тестировщик",
        description = "Пройдите 5 теста",
        icon = Icons.Rounded.CheckCircle,
        achieved = false
    ),
    Achievement(
        id = "practice_5",
        title = "Мастер практики",
        description = "Выполните 5 заданий",
        icon = Icons.Rounded.Handyman,
        achieved = false
    ),
    Achievement(
        id = "lesson_10",
        title = "Продвинутый",
        description = "Пройдите 10 уроков",
        icon = Icons.Rounded.AutoAwesome,
        achieved = false
    ),
    Achievement(
        id = "test_10",
        title = "Профессионал",
        description = "Пройдите 10 тестов",
        icon = Icons.Rounded.Verified,
        achieved = false
    ),
    Achievement(
        id = "practice_10",
        title = "Виртуоз кода",
        description = "Выполните 10 заданий",
        icon = Icons.Rounded.Computer,
        achieved = false
    ),
    Achievement(
        id = "all_lessons",
        title = "Всезнайка",
        description = "Пройдите все уроки курса",
        icon = Icons.Rounded.EmojiEvents,
        achieved = false
    ),
    Achievement(
        id = "streak_3",
        title = "Последовательный",
        description = "Занимайтесь 3 дня подряд",
        icon = Icons.Rounded.TrendingUp,
        achieved = false
    ),
    Achievement(
        id = "complete_all",
        title = "Абсолютный чемпион",
        description = "Пройдите все уроки, тесты и задания",
        icon = Icons.Rounded.WorkspacePremium,
        achieved = false
    )
)

// Список рекомендуемых модулей
private val recommendedModules = listOf(
    RecommendedModule(
        id = "practice",
        title = "Практика",
        description = "Решайте задачи",
        icon = Icons.Rounded.Code,
        color = Color(0xFF6366F1)
    ),
    RecommendedModule(
        id = "quiz",
        title = "Тесты",
        description = "Проверьте знания",
        icon = Icons.Rounded.Quiz,
        color = Color(0xFF10B981)
    ),
    RecommendedModule(
        id = "interview",
        title = "Собеседование",
        description = "Подготовка к собесу",
        icon = Icons.Rounded.WorkspacePremium,
        color = Color(0xFFF59E0B)
    ),
    RecommendedModule(
        id = "full_course",
        title = "Полный курс",
        description = "Все уроки",
        icon = Icons.Rounded.School,
        color = Color(0xFF8B5CF6)
    )
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    onNavigateToTabs: (initialTab: String) -> Unit,
    onNavigateToPractice: () -> Unit = {},
    onNavigateToQuiz: () -> Unit = {},
    onNavigateToInterview: () -> Unit = {},
    parentNavController: NavHostController,
    showNavigationButtons: Boolean = true  // новый параметр для скрытия кнопок
) {
    val activity = LocalContext.current as? Activity
    val currentUser = Firebase.auth.currentUser
    val viewModel: ProgressViewModel = hiltViewModel()
    val progressRepo = viewModel.progressRepository

    val voiceInputViewModel: VoiceInputViewModel = hiltViewModel()
    val voiceOutputViewModel: VoiceOutputViewModel = hiltViewModel()
    val chatViewModel: ChatViewModel = hiltViewModel()
    val interviewViewModel: InterviewViewModel = hiltViewModel()

    var currentTab by rememberSaveable { mutableStateOf(TextbookTab.HOME) }

    // Мотивационные фразы
    val motivationalPhrases = remember {
        listOf(
            "Каждый день - шаг к мастерству! 🚀",
            "Код, который ты пишешь сегодня, создаёт твоё завтра! 💻",
            "Ошибки - это ступеньки к успеху! 📈",
            "Учиться программировать - как учить новый язык! 🌍",
            "Терпение и труд всё перетрут! ⚡",
            "Каждая строка кода делает тебя сильнее! 💪",
            "Не бойся сложных задач - они делают тебя экспертом! 🧠",
            "Программирование - это суперсилка 21 века! 🦸",
            "Сегодняшний код - завтрашний успех! 🎯",
            "Учись, пробуй, ошибайся, побеждай! 🏆",
            "Код - это магия, которую может творить каждый! ✨",
            "Каждая решённая задача приближает к цели! 🎯",
            "Программист не тот, кто не ошибается, а тот, кто исправляет! 🔧",
            "Твоё упорство сегодня - твой успех завтра! 🌟",
            "Код пишется не пальцами, а головой! 🤔"
        )
    }
    var currentMotivationalPhrase by rememberSaveable { mutableStateOf(motivationalPhrases.random()) }
    var showMotivationalToast by rememberSaveable { mutableStateOf(false) }

    // Данные пользователя
    var userDisplayName by rememberSaveable { mutableStateOf("") }
    var userPhotoUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var userIsLoading by rememberSaveable { mutableStateOf(true) }
    var userTotalXP by rememberSaveable { mutableIntStateOf(0) }
    var userLevel by rememberSaveable { mutableIntStateOf(1) }
    var userProgress by remember { mutableStateOf<UserProgress?>(null) }
    var dataLoaded by rememberSaveable { mutableStateOf(false) }
    var refreshTrigger by rememberSaveable { mutableIntStateOf(0) }

    var completedLessonsCount by rememberSaveable { mutableIntStateOf(0) }
    var completedTestsCount by rememberSaveable { mutableIntStateOf(0) }
    var completedPracticeCount by rememberSaveable { mutableIntStateOf(0) }
    val totalLessonsCount = 12
    var userAchievements by remember { mutableStateOf(achievementsList) }

    val userIdKey = remember(currentUser?.uid) { currentUser?.uid ?: "guest" }

    // Системная кнопка "Назад" сворачивает приложение только на главной вкладке учебника
    BackHandler(enabled = currentTab == TextbookTab.HOME) {
        activity?.moveTaskToBack(true)
    }

    fun updateStatsFromProgress(progress: UserProgress) {
        completedLessonsCount = progress.completedLessons.size
        completedTestsCount = progress.quizResults.size
        completedPracticeCount = progress.completedPracticeTasks.size
        userTotalXP = progress.totalXP
        userLevel = calculateLevelFromXP(userTotalXP)
        userDisplayName = progress.displayName.ifEmpty { currentUser?.displayName ?: "Гость" }

        userAchievements = achievementsList.map { achievement ->
            val achieved = when (achievement.id) {
                "first_lesson" -> completedLessonsCount >= 1
                "lesson_3" -> completedLessonsCount >= 3
                "lesson_5" -> completedLessonsCount >= 5
                "lesson_10" -> completedLessonsCount >= 10
                "all_lessons" -> completedLessonsCount >= totalLessonsCount
                "first_test" -> completedTestsCount >= 1
                "test_3" -> completedTestsCount >= 3
                "test_5" -> completedTestsCount >= 5
                "test_10" -> completedTestsCount >= 10
                "first_practice" -> completedPracticeCount >= 1
                "practice_3" -> completedPracticeCount >= 3
                "practice_5" -> completedPracticeCount >= 5
                "practice_10" -> completedPracticeCount >= 10
                "streak_3" -> progress.dailyStreak >= 3
                "complete_all" -> completedLessonsCount >= totalLessonsCount &&
                        completedTestsCount >= 10 &&
                        completedPracticeCount >= 10
                else -> false
            }
            achievement.copy(achieved = achieved)
        }
    }

    fun refreshData() {
        if (currentUser != null) {
            viewModel.viewModelScope.launch {
                try {
                    val freshProgress = progressRepo.loadProgress(currentUser.uid)
                    freshProgress?.let { progress ->
                        userProgress = progress
                        updateStatsFromProgress(progress)
                    }
                } catch (e: Exception) {
                    println("DEBUG: Ошибка обновления: ${e.message}")
                }
            }
        }
    }

    // Загрузка данных пользователя
    LaunchedEffect(userIdKey) {
        if (!dataLoaded && currentUser != null) {
            userIsLoading = true
            try {
                val localProgress = progressRepo.loadLocalProgress(currentUser.uid)
                if (localProgress != null) {
                    userProgress = localProgress
                    updateStatsFromProgress(localProgress)
                    userPhotoUrl = currentUser.photoUrl?.toString()
                } else {
                    userDisplayName = currentUser.displayName ?: "Гость"
                }
                launch {
                    val fullProgress = progressRepo.loadProgress(currentUser.uid)
                    fullProgress?.let { progress ->
                        userProgress = progress
                        updateStatsFromProgress(progress)
                    }
                }
                dataLoaded = true
            } catch (e: Exception) {
                userDisplayName = currentUser.displayName ?: "Гость"
            } finally {
                userIsLoading = false
            }
        } else if (currentUser == null) {
            userDisplayName = "Гость"
            userIsLoading = false
            dataLoaded = true
        }
    }

    LaunchedEffect(currentTab, refreshTrigger) {
        if (currentTab == TextbookTab.HOME && dataLoaded && currentUser != null) {
            delay(300)
            refreshData()
        }
    }

    LaunchedEffect(userProgress) {
        userProgress?.let { updateStatsFromProgress(it) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Основной контент
            when (currentTab) {
                TextbookTab.HOME -> {
                    TextbookHomeContent(
                        displayName = userDisplayName,
                        totalXP = userTotalXP,
                        level = userLevel,
                        userPhotoUrl = userPhotoUrl,
                        isLoading = userIsLoading && !dataLoaded,
                        dataLoaded = dataLoaded,
                        motivationalPhrase = currentMotivationalPhrase,
                        showMotivationalToast = showMotivationalToast,
                        onHideMotivationalToast = { showMotivationalToast = false },
                        completedLessonsCount = completedLessonsCount,
                        completedTestsCount = completedTestsCount,
                        completedPracticeCount = completedPracticeCount,
                        totalLessonsCount = totalLessonsCount,
                        userAchievements = userAchievements,
                        onNavigateToPractice = onNavigateToPractice,
                        onNavigateToQuiz = onNavigateToQuiz,
                        onNavigateToInterview = onNavigateToInterview,
                        onNavigateToTabs = onNavigateToTabs
                    )
                }
                TextbookTab.CHAT -> {
                    ChatWithAIScreen(
                        onBackToHome = {
                            currentTab = TextbookTab.HOME
                            refreshTrigger++
                        },
                        viewModel = chatViewModel,
                        voiceInputViewModel = voiceInputViewModel,
                        voiceOutputViewModel = voiceOutputViewModel
                    )
                }
                TextbookTab.INTERVIEW_SIM -> {
                    InterviewSimulationScreen(
                        navController = parentNavController,
                        viewModel = interviewViewModel,
                        voiceInputViewModel = voiceInputViewModel,
                        voiceOutputViewModel = voiceOutputViewModel
                    )
                }
            }

            // Кнопка мотивации (жёлтая) в правом верхнем углу с отступом от статус-бара
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars) // отступ от статус-бара
                    .padding(16.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable {
                            currentMotivationalPhrase = motivationalPhrases.random()
                            showMotivationalToast = true
                        },
                    color = Color(0xFFFFC107),
                    contentColor = Color.Black,
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Lightbulb,
                            contentDescription = "Мотивация",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Вертикальная навигация (3 круглые кнопки) - показываем только если флаг true
            if (showNavigationButtons) {
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    NavigationCircleButton(
                        icon = Icons.Default.Home,
                        isSelected = currentTab == TextbookTab.HOME,
                        onClick = {
                            currentTab = TextbookTab.HOME
                            currentMotivationalPhrase = motivationalPhrases.random()
                            refreshTrigger++
                        },
                        size = 44.dp
                    )
                    NavigationCircleButton(
                        icon = Icons.Default.SmartToy,
                        isSelected = currentTab == TextbookTab.CHAT,
                        onClick = { currentTab = TextbookTab.CHAT },
                        size = 44.dp
                    )
                    NavigationCircleButton(
                        icon = Icons.Default.Work,
                        isSelected = currentTab == TextbookTab.INTERVIEW_SIM,
                        onClick = { currentTab = TextbookTab.INTERVIEW_SIM },
                        size = 44.dp
                    )
                }
            }
        }
    }

    // Тост с мотивационной фразой (появляется сверху)
    if (showMotivationalToast) {
        LaunchedEffect(Unit) {
            delay(3000)
            showMotivationalToast = false
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(top = 80.dp, start = 16.dp, end = 16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        currentMotivationalPhrase,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun NavigationCircleButton(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    size: Dp = 48.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                if (isSelected)
                    Color(0xFF4CAF50)  // ярко-зелёный фон для выбранной кнопки
                else
                    Color(0xFF1A1A1A)  // тёмно-серый/чёрный фон для невыбранной
            )
            .border(
                width = if (isSelected) 0.dp else 1.dp,
                color = Color.White.copy(alpha = 0.3f),
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (isSelected) Color.White else Color(0xFFB0B0B0), // белая иконка для выбранной, светло-серая для невыбранной
            modifier = Modifier.size(size * 0.5f)
        )
    }
}

@Composable
fun TextbookHomeContent(
    displayName: String,
    totalXP: Int,
    level: Int,
    userPhotoUrl: String?,
    isLoading: Boolean,
    dataLoaded: Boolean,
    motivationalPhrase: String,
    showMotivationalToast: Boolean,
    onHideMotivationalToast: () -> Unit,
    completedLessonsCount: Int,
    completedTestsCount: Int,
    completedPracticeCount: Int,
    totalLessonsCount: Int,
    userAchievements: List<Achievement>,
    onNavigateToPractice: () -> Unit,
    onNavigateToQuiz: () -> Unit,
    onNavigateToInterview: () -> Unit,
    onNavigateToTabs: (initialTab: String) -> Unit
) {
    val progressAnimation = remember { Animatable(0f) }
    val animatedLevel by animateFloatAsState(
        targetValue = level.toFloat(),
        animationSpec = tween(1000)
    )

    fun calculateXPForLevel(lvl: Int): Int = when (lvl) {
        1 -> 0; 2 -> 100; 3 -> 250; 4 -> 450; 5 -> 700; 6 -> 1000
        else -> {
            var xp = 1000
            var current = 6
            var xpNext = 150
            while (current < lvl) {
                xp += xpNext
                xpNext += 50
                current++
            }
            xp
        }
    }

    fun calculateProgressToNextLevel(totalXP: Int): Float {
        val currentLevel = calculateLevelFromXP(totalXP)
        val xpCurrent = calculateXPForLevel(currentLevel)
        val xpNext = calculateXPForLevel(currentLevel + 1)
        val xpIn = totalXP - xpCurrent
        val need = xpNext - xpCurrent
        return if (need > 0) (xpIn.toFloat() / need).coerceIn(0f, 1f) else 0f
    }

    LaunchedEffect(totalXP) {
        if (dataLoaded) {
            progressAnimation.animateTo(
                targetValue = calculateProgressToNextLevel(totalXP),
                animationSpec = tween(800, easing = LinearEasing)
            )
        }
    }

    fun calculateLearningProgress(): Float = if (totalLessonsCount > 0) completedLessonsCount.toFloat() / totalLessonsCount else 0f

    val primaryGradient = Brush.linearGradient(
        colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
    )
    val secondaryGradient = Brush.linearGradient(
        colors = listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f))
    )

    val xpForCurrentLevel = calculateXPForLevel(level)
    val xpForNextLevel = calculateXPForLevel(level + 1)
    val xpInCurrentLevel = (totalXP - xpForCurrentLevel).coerceAtLeast(0)
    val xpNeededForNextLevel = (xpForNextLevel - xpForCurrentLevel).coerceAtLeast(1)
    val unlockedCount = userAchievements.count { it.achieved }
    val totalAchievements = userAchievements.size

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading && !dataLoaded) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp), strokeWidth = 3.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Загружаем ваш прогресс...", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(top = 0.dp, bottom = 80.dp)
            ) {
                // Карточка прогресса
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        shadowElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            val primaryColor = MaterialTheme.colorScheme.primary
                            val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
                            val onPrimaryContainerColor = MaterialTheme.colorScheme.onPrimaryContainer
                            val onSurfaceColor = MaterialTheme.colorScheme.onSurface
                            val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

                            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).padding(8.dp), contentAlignment = Alignment.Center) {
                                Canvas(modifier = Modifier.matchParentSize()) {
                                    val size = size.minDimension
                                    drawCircle(color = surfaceVariantColor, radius = size / 2, center = center)
                                }
                                Canvas(modifier = Modifier.matchParentSize()) {
                                    val size = size.minDimension
                                    drawArc(
                                        color = primaryColor,
                                        startAngle = -90f,
                                        sweepAngle = progressAnimation.value * 360f,
                                        useCenter = false,
                                        style = Stroke(width = size * 0.075f, cap = StrokeCap.Round),
                                        size = Size(size, size)
                                    )
                                }
                                Box(
                                    modifier = Modifier.fillMaxWidth(0.75f).aspectRatio(1f).clip(CircleShape)
                                        .background(Brush.radialGradient(listOf(primaryContainerColor, primaryContainerColor.copy(alpha = 0.5f)))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            animatedLevel.toInt().toString(),
                                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 48.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 48.sp),
                                            color = onPrimaryContainerColor,
                                            maxLines = 1
                                        )
                                        Text("Уровень", style = MaterialTheme.typography.labelLarge, color = onPrimaryContainerColor.copy(alpha = 0.8f))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(primaryGradient), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Filled.Star, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(28.dp))
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("$totalXP", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp), color = onSurfaceColor)
                                    Text("Всего XP", style = MaterialTheme.typography.bodySmall, color = onSurfaceVariantColor, textAlign = TextAlign.Center)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(secondaryGradient), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Filled.TrendingUp, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(28.dp))
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("$xpInCurrentLevel/$xpNeededForNextLevel", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp), color = onSurfaceColor)
                                    Text("До след. уровня", style = MaterialTheme.typography.bodySmall, color = onSurfaceVariantColor, textAlign = TextAlign.Center)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFF59E0B).copy(alpha = 0.8f)))), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Filled.EmojiEvents, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(28.dp))
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("$unlockedCount/$totalAchievements", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp), color = onSurfaceColor)
                                    Text("Достижения", style = MaterialTheme.typography.bodySmall, color = onSurfaceVariantColor, textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }
                }

                // Рекомендуемые модули
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text("Рекомендуемые модули", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(bottom = 12.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(recommendedModules) { module ->
                                RecommendedModuleCard(module = module, onClick = {
                                    when (module.id) {
                                        "practice" -> onNavigateToPractice()
                                        "quiz" -> onNavigateToQuiz()
                                        "interview" -> onNavigateToInterview()
                                        "full_course" -> onNavigateToTabs("learning")
                                    }
                                })
                            }
                        }
                    }
                }

                // Путь обучения
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Ваш путь обучения", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                            TextButton(onClick = { onNavigateToTabs("learning") }) { Text("Смотреть все") }
                        }
                        LearningPathCard(
                            title = "Kotlin для начинающих",
                            description = "Основы программирования на Kotlin",
                            progress = calculateLearningProgress(),
                            duration = "",
                            lessonsCompleted = completedLessonsCount,
                            totalLessons = totalLessonsCount,
                            onClick = { onNavigateToTabs("learning") }
                        )
                    }
                }

                // Достижения с прогрессом
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Ваши достижения", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                            Text("$unlockedCount/$totalAchievements", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = if (totalAchievements > 0) unlockedCount.toFloat() / totalAchievements else 0f,
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFFF59E0B),
                            trackColor = Color(0xFFF59E0B).copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(userAchievements) { achievement ->
                                AchievementBadge(achievement = achievement, onClick = {})
                            }
                        }
                    }
                }

                // Статистика за неделю
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                            Text("Ваша активность", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(bottom = 16.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Rounded.MenuBook, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("$completedLessonsCount", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp), color = MaterialTheme.colorScheme.onSurface)
                                    Text("Пройдено уроков", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Rounded.CheckCircle, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("$completedTestsCount", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp), color = MaterialTheme.colorScheme.onSurface)
                                    Text("Пройдено тестов", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Rounded.Code, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(24.dp))
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("$completedPracticeCount", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp), color = MaterialTheme.colorScheme.onSurface)
                                    Text("Выполнено заданий", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Вспомогательные компоненты (оставлены без изменений)
@Composable
fun RecommendedModuleCard(module: RecommendedModule, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.width(140.dp),
        shape = RoundedCornerShape(20.dp),
        color = module.color.copy(alpha = 0.1f),
        onClick = onClick,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(module.color.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                Icon(module.icon, module.title, tint = module.color, modifier = Modifier.size(24.dp))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(module.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
                Text(module.description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun LearningPathCard(
    title: String,
    description: String,
    progress: Float,
    duration: String,
    lessonsCompleted: Int,
    totalLessons: Int,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        onClick = onClick,
        tonalElevation = 3.dp,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.size(60.dp).clip(CircleShape).background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)))), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.School, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(28.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                    Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.Default.ArrowForward, "Начать", tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                CourseInfoItem(icon = Icons.Rounded.Schedule, text = duration, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                CourseInfoItem(icon = Icons.Rounded.MenuBook, text = "$lessonsCompleted/$totalLessons уроков", color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
            }
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)), color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Прогресс", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun CourseInfoItem(icon: ImageVector, text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(color), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
        }
        Text(text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun AchievementBadge(achievement: Achievement, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.width(140.dp),
        shape = RoundedCornerShape(20.dp),
        color = if (achievement.achieved) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        onClick = onClick,
        tonalElevation = if (achievement.achieved) 2.dp else 1.dp
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(if (achievement.achieved) Color(0xFFF59E0B) else MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                Icon(achievement.icon, achievement.title, tint = if (achievement.achieved) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(24.dp))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(achievement.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = if (achievement.achieved) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                if (!achievement.achieved) {
                    Text(achievement.description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp))
                } else {
                    Icon(Icons.Default.Check, "Получено", tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// Классы данных
data class RecommendedModule(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val achieved: Boolean
)