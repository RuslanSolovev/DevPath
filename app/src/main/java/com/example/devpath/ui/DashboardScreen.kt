@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
package com.example.devpath.ui

import android.content.res.Configuration
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
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.devpath.data.repository.LocalThemeRepository
import com.example.devpath.domain.models.UserProgress
import com.example.devpath.ui.theme.AppTheme
import com.example.devpath.ui.viewmodel.ProgressViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

// Enum для главных вкладок
enum class MainTab(
    val title: String,
    val icon: ImageVector,
    val route: String
) {
    HOME("Главная", Icons.Default.Home, "main_home"),
    CHAT("Чат с ИИ", Icons.Default.SmartToy, "main_chat"),
    INTERVIEW_SIM("Собеседование", Icons.Default.Work, "main_interview_sim")
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

// Исправленная система достижений (15 достижений без XP)
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
    onSignOut: () -> Unit,
    onNavigateToTabs: (initialTab: String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToPractice: () -> Unit = {},
    onNavigateToQuiz: () -> Unit = {},
    onNavigateToInterview: () -> Unit = {},
    parentNavController: NavHostController
) {
    var currentTab by remember { mutableStateOf(MainTab.HOME) }

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
    var currentMotivationalPhrase by remember { mutableStateOf(motivationalPhrases.random()) }
    var showMotivationalToast by remember { mutableStateOf(false) }

    // Состояние меню
    var showSettingsMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (currentTab == MainTab.HOME) {
                // TopAppBar будет отображаться только на главной вкладке
                HomeTopAppBar(
                    onSettingsClick = { showSettingsMenu = true },
                    onMotivationalPhraseClick = {
                        currentMotivationalPhrase = motivationalPhrases.random()
                        showMotivationalToast = true
                    },
                    onNavigateToTabs = onNavigateToTabs,
                    onNavigateToProfile = onNavigateToProfile,
                    onNavigateToPractice = onNavigateToPractice,
                    onNavigateToQuiz = onNavigateToQuiz,
                    onNavigateToInterview = onNavigateToInterview
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar {
                MainTab.entries.forEach { tab ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null
                            )
                        },
                        label = { Text(tab.title) },
                        selected = currentTab == tab,
                        onClick = {
                            currentTab = tab
                            if (tab == MainTab.HOME) {
                                // Обновляем мотивационную фразу при переходе на главную
                                currentMotivationalPhrase = motivationalPhrases.random()
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            when (currentTab) {
                MainTab.HOME -> {
                    HomeDashboardContent(
                        onSignOut = onSignOut,
                        onNavigateToProfile = onNavigateToProfile,
                        onNavigateToPractice = onNavigateToPractice,
                        onNavigateToQuiz = onNavigateToQuiz,
                        onNavigateToInterview = onNavigateToInterview,
                        onNavigateToTabs = onNavigateToTabs,
                        currentMotivationalPhrase = currentMotivationalPhrase,
                        showMotivationalToast = showMotivationalToast,
                        onMotivationalPhraseClick = {
                            currentMotivationalPhrase = motivationalPhrases.random()
                            showMotivationalToast = true
                        },
                        onHideMotivationalToast = { showMotivationalToast = false }
                    )
                }


                MainTab.CHAT -> {
                    ChatWithAIScreen(
                        onBackToHome = { currentTab = MainTab.HOME } // ← стрелка переключит на главную
                    )
                }

                MainTab.INTERVIEW_SIM -> {
                    InterviewSimulationScreen(
                        navController = parentNavController
                    )
                }
            }
        }
    }

    // Настройки меню
    if (showSettingsMenu) {
        val themeRepository = LocalThemeRepository.current
        val currentTheme by themeRepository.currentTheme.collectAsState()

        AlertDialog(
            onDismissRequest = { showSettingsMenu = false },
            title = { Text("Настройки") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ListItem(
                        headlineContent = { Text("Профиль") },
                        leadingContent = {
                            Icon(
                                Icons.Outlined.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.clickable {
                            showSettingsMenu = false
                            onNavigateToProfile()
                        }
                    )
                    Divider()
                    Text(
                        "Тема приложения",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                    ThemeOption(
                        title = "Системная",
                        selected = currentTheme == AppTheme.SYSTEM,
                        icon = if (isSystemInDarkTheme()) Icons.Default.DarkMode else Icons.Default.LightMode,
                        onClick = {
                            themeRepository.setTheme(AppTheme.SYSTEM)
                        }
                    )
                    ThemeOption(
                        title = "Светлая",
                        selected = currentTheme == AppTheme.LIGHT,
                        icon = Icons.Default.LightMode,
                        onClick = {
                            themeRepository.setTheme(AppTheme.LIGHT)
                        }
                    )
                    ThemeOption(
                        title = "Тёмная",
                        selected = currentTheme == AppTheme.DARK,
                        icon = Icons.Default.DarkMode,
                        onClick = {
                            themeRepository.setTheme(AppTheme.DARK)
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSettingsMenu = false
                        onSignOut()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Outlined.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Выйти")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSettingsMenu = false }
                ) {
                    Text("Закрыть")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(
    onSettingsClick: () -> Unit,
    onMotivationalPhraseClick: () -> Unit,
    onNavigateToTabs: (initialTab: String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToPractice: () -> Unit,
    onNavigateToQuiz: () -> Unit,
    onNavigateToInterview: () -> Unit
) {
    val currentUser = Firebase.auth.currentUser
    val viewModel: ProgressViewModel = hiltViewModel()
    val progressRepo = viewModel.progressRepository
    var displayName by remember { mutableStateOf("") }
    var userPhotoUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var userProgress by remember { mutableStateOf<UserProgress?>(null) }

    LaunchedEffect(Unit) {
        if (currentUser != null) {
            isLoading = true
            try {
                // Загружаем локальные данные
                val localProgress = progressRepo.loadLocalProgress(currentUser.uid)
                if (localProgress != null) {
                    userProgress = localProgress
                    displayName = localProgress.displayName.ifEmpty {
                        currentUser.displayName ?: "Гость"
                    }
                    userPhotoUrl = currentUser.photoUrl?.toString()
                } else {
                    displayName = currentUser.displayName ?: "Гость"
                }
            } catch (e: Exception) {
                displayName = currentUser.displayName ?: "Гость"
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
            displayName = "Гость"
        }
    }

    SmallTopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column {
                    Text(
                        "Привет,",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        displayName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        navigationIcon = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                        )
                    )
                    .clickable { onSettingsClick() },
                contentAlignment = Alignment.Center
            ) {
                if (userPhotoUrl != null) {
                    AsyncImage(
                        model = userPhotoUrl,
                        contentDescription = "Аватар",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else if (!isLoading) {
                    Text(
                        displayName.take(2).uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        },
        actions = {
            IconButton(
                onClick = onMotivationalPhraseClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
            ) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.offset(x = 8.dp, y = (-8).dp)
                ) {
                    Text("🎯", fontSize = 10.sp)
                }
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = "Мотивация",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDashboardContent(
    onSignOut: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToPractice: () -> Unit,
    onNavigateToQuiz: () -> Unit,
    onNavigateToInterview: () -> Unit,
    onNavigateToTabs: (initialTab: String) -> Unit,
    currentMotivationalPhrase: String,
    showMotivationalToast: Boolean,
    onMotivationalPhraseClick: () -> Unit,
    onHideMotivationalToast: () -> Unit
) {
    val currentUser = Firebase.auth.currentUser
    val viewModel: ProgressViewModel = hiltViewModel()
    val progressRepo = viewModel.progressRepository
    var displayName by remember { mutableStateOf("") }
    var totalXP by remember { mutableIntStateOf(0) }
    var level by remember { mutableIntStateOf(1) }
    var userPhotoUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Добавляем состояние для хранения полного прогресса
    var userProgress by remember { mutableStateOf<UserProgress?>(null) }

    // Состояние для общего количества уроков
    var totalLessonsCount by remember { mutableIntStateOf(12) }

    // Анимация прогресса
    val progressAnimation = remember { Animatable(0f) }
    val animatedLevel by animateFloatAsState(
        targetValue = level.toFloat(),
        animationSpec = tween(1000)
    )

    val coroutineScope = rememberCoroutineScope()
    var shouldShowProfile by remember { mutableStateOf(false) }

    // Функция для расчета XP для уровня
    fun calculateXPForLevel(level: Int): Int {
        return when (level) {
            1 -> 0
            2 -> 100
            3 -> 250
            4 -> 450
            5 -> 700
            6 -> 1000
            else -> {
                var xp = 1000
                var currentLevel = 6
                var xpForNextLevel = 150
                while (currentLevel < level) {
                    xp += xpForNextLevel
                    xpForNextLevel += 50
                    currentLevel++
                }
                xp
            }
        }
    }

    // Функция для расчета прогресса до следующего уровня
    fun calculateProgressToNextLevel(totalXP: Int): Float {
        val currentLevel = calculateLevelFromXP(totalXP)
        val xpForCurrentLevel = calculateXPForLevel(currentLevel)
        val xpForNextLevel = calculateXPForLevel(currentLevel + 1)
        val xpInCurrentLevel = totalXP - xpForCurrentLevel
        val xpNeededForNextLevel = xpForNextLevel - xpForCurrentLevel

        return if (xpNeededForNextLevel > 0) {
            (xpInCurrentLevel.toFloat() / xpNeededForNextLevel).coerceIn(0f, 1f)
        } else {
            0f
        }
    }

    // Функции для получения реальных данных
    fun getCompletedLessonsCount(): Int {
        return userProgress?.completedLessons?.size ?: 0
    }

    fun getCompletedTestsCount(): Int {
        return userProgress?.quizResults?.size ?: 0
    }

    fun getCompletedPracticeCount(): Int {
        return userProgress?.completedPracticeTasks?.size ?: 0
    }

    fun calculateLearningProgress(): Float {
        val completed = getCompletedLessonsCount()
        return if (totalLessonsCount > 0) completed.toFloat() / totalLessonsCount else 0f
    }

    fun getUserAchievements(): List<Achievement> {
        val progress = userProgress ?: return achievementsList

        val completedLessons = getCompletedLessonsCount()
        val completedTests = getCompletedTestsCount()
        val completedPractice = getCompletedPracticeCount()
        val dailyStreak = progress.dailyStreak

        return achievementsList.map { achievement ->
            val achieved = when (achievement.id) {
                // Уроки
                "first_lesson" -> completedLessons >= 1
                "lesson_3" -> completedLessons >= 3
                "lesson_5" -> completedLessons >= 5
                "lesson_10" -> completedLessons >= 10
                "all_lessons" -> completedLessons >= totalLessonsCount

                // Тесты
                "first_test" -> completedTests >= 1
                "test_3" -> completedTests >= 3
                "test_5" -> completedTests >= 5
                "test_10" -> completedTests >= 10

                // Практика
                "first_practice" -> completedPractice >= 1
                "practice_3" -> completedPractice >= 3
                "practice_5" -> completedPractice >= 5
                "practice_10" -> completedPractice >= 10

                // Специальные
                "streak_3" -> dailyStreak >= 3
                "complete_all" -> completedLessons >= totalLessonsCount &&
                        completedTests >= 10 &&
                        completedPractice >= 10

                else -> false
            }

            achievement.copy(achieved = achieved)
        }
    }

    // Загрузка прогресса
    LaunchedEffect(Unit) {
        if (currentUser != null) {
            isLoading = true
            try {
                // Загружаем локальные данные
                val localProgress = progressRepo.loadLocalProgress(currentUser.uid)
                if (localProgress != null) {
                    userProgress = localProgress
                    displayName = localProgress.displayName.ifEmpty {
                        currentUser.displayName ?: "Гость"
                    }
                    totalXP = localProgress.totalXP
                    // Рассчитываем уровень из XP
                    level = calculateLevelFromXP(totalXP)
                    userPhotoUrl = currentUser.photoUrl?.toString()

                    // Анимация прогресса
                    val progressPercent = calculateProgressToNextLevel(localProgress.totalXP)
                    progressAnimation.animateTo(
                        targetValue = progressPercent,
                        animationSpec = tween(800, easing = LinearEasing)
                    )

                    if (localProgress.displayName.isBlank()) {
                        shouldShowProfile = true
                    }
                } else {
                    displayName = currentUser.displayName ?: "Гость"
                    shouldShowProfile = true
                }

                // Фоновая загрузка полных данных
                coroutineScope.launch {
                    val fullProgress = progressRepo.loadProgress(currentUser.uid)
                    fullProgress?.let { progress ->
                        userProgress = progress
                        displayName = progress.displayName.ifEmpty {
                            currentUser.displayName ?: "Гость"
                        }
                        totalXP = progress.totalXP
                        level = calculateLevelFromXP(progress.totalXP)

                        val progressPercent = calculateProgressToNextLevel(progress.totalXP)
                        progressAnimation.animateTo(
                            targetValue = progressPercent,
                            animationSpec = tween(800, easing = LinearEasing)
                        )

                        if (progress.displayName.isBlank()) {
                            shouldShowProfile = true
                        }
                    }
                }

            } catch (e: Exception) {
                println("DEBUG: Ошибка в Dashboard: ${e.message}")
                displayName = currentUser.displayName ?: "Гость"
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
            displayName = "Гость"
        }
    }

    // Автоматический переход к профилю
    if (shouldShowProfile) {
        LaunchedEffect(Unit) {
            onNavigateToProfile()
        }
        return
    }

    // Градиенты
    val primaryGradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        )
    )
    val secondaryGradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
        )
    )

    // Расчет XP для отображения
    val xpForCurrentLevel = calculateXPForLevel(level)
    val xpForNextLevel = calculateXPForLevel(level + 1)
    val xpInCurrentLevel = (totalXP - xpForCurrentLevel).coerceAtLeast(0)
    val xpNeededForNextLevel = (xpForNextLevel - xpForCurrentLevel).coerceAtLeast(1)

    // Рассчитываем прогресс достижений
    val userAchievements = getUserAchievements()
    val unlockedAchievementsCount = userAchievements.count { it.achieved }
    val totalAchievementsCount = userAchievements.size

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 3.dp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Загружаем ваш прогресс...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                // Мотивационная фраза
                if (showMotivationalToast) {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    currentMotivationalPhrase,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = onHideMotivationalToast,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Закрыть",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Карточка прогресса
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier.size(160.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    progress = 1f,
                                    modifier = Modifier.size(160.dp),
                                    strokeWidth = 12.dp,
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                )
                                CircularProgressIndicator(
                                    progress = progressAnimation.value,
                                    modifier = Modifier.size(160.dp),
                                    strokeWidth = 12.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = Color.Transparent
                                )
                                Box(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.primaryContainer,
                                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            animatedLevel.toInt().toString(),
                                            style = MaterialTheme.typography.displayLarge.copy(
                                                fontSize = 48.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            ),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            "Уровень",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatItem(
                                    value = "$totalXP",
                                    label = "Всего XP",
                                    icon = Icons.Filled.Star,
                                    gradient = primaryGradient
                                )
                                StatItem(
                                    value = "$xpInCurrentLevel/$xpNeededForNextLevel",
                                    label = "До след. уровня",
                                    icon = Icons.Filled.TrendingUp,
                                    gradient = secondaryGradient
                                )
                                StatItem(
                                    value = "$unlockedAchievementsCount/$totalAchievementsCount",
                                    label = "Достижения",
                                    icon = Icons.Filled.EmojiEvents,
                                    gradient = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFFF59E0B),
                                            Color(0xFFF59E0B).copy(alpha = 0.8f)
                                        )
                                    )
                                )
                            }
                        }
                    }
                }

                // Рекомендуемые модули
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            "Рекомендуемые модули",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(recommendedModules) { module ->
                                RecommendedModuleCard(
                                    module = module,
                                    onClick = {
                                        when (module.id) {
                                            "practice" -> onNavigateToPractice()
                                            "quiz" -> onNavigateToQuiz()
                                            "interview" -> onNavigateToInterview()
                                            "full_course" -> onNavigateToTabs("learning")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Путь обучения
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Ваш путь обучения",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            TextButton(onClick = { onNavigateToTabs("learning") }) {
                                Text("Смотреть все")
                            }
                        }
                        LearningPathCard(
                            title = "Kotlin для начинающих",
                            description = "Основы программирования на Kotlin",
                            progress = calculateLearningProgress(),
                            duration = "",
                            lessonsCompleted = getCompletedLessonsCount(),
                            totalLessons = totalLessonsCount,
                            onClick = { onNavigateToTabs("learning") }
                        )
                    }
                }

                // Достижения с прогрессом
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Ваши достижения",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                "$unlockedAchievementsCount/$totalAchievementsCount",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        // Прогресс-бар достижений
                        LinearProgressIndicator(
                            progress = if (totalAchievementsCount > 0)
                                unlockedAchievementsCount.toFloat() / totalAchievementsCount
                            else 0f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFFF59E0B),
                            trackColor = Color(0xFFF59E0B).copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(userAchievements) { achievement ->
                                AchievementBadge(
                                    achievement = achievement,
                                    onClick = { /* TODO: Подробности */ }
                                )
                            }
                        }
                    }
                }

                // Статистика за неделю
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                "Ваша активность",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StatCard(
                                    value = getCompletedLessonsCount().toString(),
                                    label = "Пройдено уроков",
                                    icon = Icons.Rounded.MenuBook,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                StatCard(
                                    value = getCompletedTestsCount().toString(),
                                    label = "Пройдено тестов",
                                    icon = Icons.Rounded.CheckCircle,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                StatCard(
                                    value = getCompletedPracticeCount().toString(),
                                    label = "Выполнено заданий",
                                    icon = Icons.Rounded.Code,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun StatItem(
    value: String,
    label: String,
    icon: ImageVector,
    gradient: Brush
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(120.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(gradient),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun RecommendedModuleCard(
    module: RecommendedModule,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(140.dp),
        shape = RoundedCornerShape(20.dp),
        color = module.color.copy(alpha = 0.1f),
        onClick = onClick,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(module.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    module.icon,
                    contentDescription = module.title,
                    tint = module.color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    module.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    module.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
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
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        onClick = onClick,
        tonalElevation = 3.dp,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
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
                    Icon(
                        Icons.Rounded.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "Начать",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CourseInfoItem(
                    icon = Icons.Rounded.Schedule,
                    text = duration,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                )
                CourseInfoItem(
                    icon = Icons.Rounded.MenuBook,
                    text = "$lessonsCompleted/$totalLessons уроков",
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Прогресс",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun CourseInfoItem(
    icon: ImageVector,
    text: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AchievementBadge(
    achievement: Achievement,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(140.dp),
        shape = RoundedCornerShape(20.dp),
        color = if (achievement.achieved)
            MaterialTheme.colorScheme.secondaryContainer
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        onClick = onClick,
        tonalElevation = if (achievement.achieved) 2.dp else 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (achievement.achieved)
                            Color(0xFFF59E0B)
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    achievement.icon,
                    contentDescription = achievement.title,
                    tint = if (achievement.achieved)
                        MaterialTheme.colorScheme.onSecondary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    achievement.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (achievement.achieved)
                        MaterialTheme.colorScheme.onSecondaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                if (!achievement.achieved) {
                    Text(
                        achievement.description,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Получено",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    value: String,
    label: String,
    icon: ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ThemeOption(
    title: String,
    selected: Boolean,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (selected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surface,
        onClick = onClick,
        tonalElevation = if (selected) 1.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
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
                    tint = if (selected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            }
            if (selected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Выбрано",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun isSystemInDarkTheme(): Boolean {
    return LocalConfiguration.current.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
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

data class AIMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class InterviewStep(
    val id: Int,
    val title: String,
    val question: String,
    val timeLimit: Int, // в секундах
    val tips: List<String> = emptyList()
)