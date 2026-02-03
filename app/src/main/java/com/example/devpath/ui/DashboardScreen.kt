package com.example.devpath.ui

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.devpath.data.repository.ProgressRepository
import com.example.devpath.ui.theme.AppTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.style.TextOverflow
import com.example.devpath.data.repository.LocalThemeRepository
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    onSignOut: () -> Unit,
    onNavigateToTabs: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToPractice: () -> Unit = {},
    onNavigateToQuiz: () -> Unit = {},
    onNavigateToInterview: () -> Unit = {}
) {
    val currentUser = Firebase.auth.currentUser
    val progressRepo = remember { ProgressRepository() }

    var displayName by remember { mutableStateOf("") }
    var totalXP by remember { mutableStateOf(0) }
    var level by remember { mutableStateOf(1) }
    var shouldShowProfile by remember { mutableStateOf(false) }
    var userPhotoUrl by remember { mutableStateOf<String?>(null) }

    // Анимация прогресса
    val progressAnimation = remember { Animatable(0f) }
    val animatedLevel by animateFloatAsState(
        targetValue = level.toFloat(),
        animationSpec = tween(1000)
    )

    // Состояние для темы через репозиторий
    val themeRepository = LocalThemeRepository.current
    val currentTheme by themeRepository.currentTheme.collectAsState()

    // Состояние меню
    var showSettingsMenu by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Мотивационные фразы для колокольчика
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

    LaunchedEffect(Unit) {
        if (currentUser != null) {
            try {
                val progress = progressRepo.loadProgress(currentUser.uid)
                if (progress != null) {
                    displayName = progress.displayName ?: ""
                    totalXP = progress.totalXP

                    // 🔥 ВОТ ГЛАВНОЕ ИЗМЕНЕНИЕ:
                    val calculatedLevel = calculateLevel(totalXP)
                    level = calculatedLevel

                    // Если уровень изменился, сохраняем его в Firestore
                    if (calculatedLevel != progress.level) {
                        coroutineScope.launch {
                            val updatedProgress = progress.copy(level = calculatedLevel)
                            progressRepo.saveProgress(updatedProgress)
                        }
                    }

                    userPhotoUrl = currentUser.photoUrl?.toString()

                    // Запускаем анимацию прогресса
                    val progressPercent = (totalXP % 100) / 100f
                    progressAnimation.animateTo(
                        targetValue = progressPercent,
                        animationSpec = tween(1000, easing = LinearEasing)
                    )

                    if (displayName.isBlank()) {
                        shouldShowProfile = true
                    }
                } else {
                    shouldShowProfile = true
                }
            } catch (e: Exception) {
                println("DEBUG: Ошибка в Dashboard: ${e.message}")
            }
        }
    }

    // Автоматический переход к профилю
    if (shouldShowProfile) {
        onNavigateToProfile()
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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            // Используем SmallTopAppBar для правильного отступа от системной панели
            SmallTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Приветствие пользователя
                        Column {
                            Text(
                                "Привет,",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                if (displayName.isNotBlank()) displayName else "Гость",
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
                    // Аватар пользователя
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
                            .clickable { showSettingsMenu = true },
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
                        } else {
                            Text(
                                displayName.take(2).uppercase(),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                },
                actions = {
                    // Кнопка мотивации с колокольчиком
                    IconButton(
                        onClick = {
                            currentMotivationalPhrase = motivationalPhrases.random()
                            showMotivationalToast = true
                        },
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
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Мотивационная фраза (появляется после нажатия на колокольчик)
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
                                    onClick = { showMotivationalToast = false },
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
                            // Круглый прогресс с градиентом
                            Box(
                                modifier = Modifier.size(160.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                // Фон круга
                                CircularProgressIndicator(
                                    progress = 1f,
                                    modifier = Modifier.size(160.dp),
                                    strokeWidth = 12.dp,
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                )

                                // Анимированный прогресс
                                CircularProgressIndicator(
                                    progress = progressAnimation.value,
                                    modifier = Modifier.size(160.dp),
                                    strokeWidth = 12.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = Color.Transparent
                                )

                                // Внутренний круг
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
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
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

                            // XP и прогресс
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
                                    value = "${totalXP % 100}/100",
                                    label = "До след. уровня",
                                    icon = Icons.Filled.TrendingUp,
                                    gradient = secondaryGradient
                                )
                            }
                        }
                    }
                }

                // Рекомендуемые модули (вместо быстрых действий)
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
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
                                            "practice" -> onNavigateToPractice() // Открываем вкладку практики
                                            "quiz" -> onNavigateToQuiz()        // Открываем вкладку тестов
                                            "interview" -> onNavigateToInterview() // Открываем вкладку собеседований
                                            "full_course" -> onNavigateToTabs()   // Открываем все вкладки
                                        }
                                    }


                                )
                            }
                        }
                    }
                }

                // Ежедневная цель
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(
                            "Ежедневная цель",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        DailyGoalCard(
                            completed = 3,
                            total = 5,
                            onComplete = { /* TODO: Отметить выполнение */ }
                        )
                    }
                }

                // Путь обучения
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Ваш путь обучения",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            TextButton(onClick = onNavigateToTabs) {
                                Text("Смотреть все")
                            }
                        }

                        LearningPathCard(
                            title = "Kotlin для начинающих",
                            description = "Основы программирования на Kotlin",
                            progress = 0.65f,
                            duration = "8 часов",
                            lessonsCompleted = 15,
                            totalLessons = 25,
                            onClick = onNavigateToTabs
                        )
                    }
                }

                // Достижения
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(
                            "Ваши достижения",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(achievements) { achievement ->
                                AchievementBadge(
                                    achievement = achievement,
                                    onClick = { /* TODO: Открыть детали достижения */ }
                                )
                            }
                        }
                    }
                }

                // Статистика
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
                                "Статистика за неделю",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StatCard(
                                    value = "12",
                                    label = "Пройдено уроков",
                                    icon = Icons.Rounded.MenuBook,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                StatCard(
                                    value = "8",
                                    label = "Правильных ответов",
                                    icon = Icons.Rounded.CheckCircle,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                StatCard(
                                    value = "5",
                                    label = "Новых навыков",
                                    icon = Icons.Rounded.AutoAwesome,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // Настройки меню
        if (showSettingsMenu) {
            AlertDialog(
                onDismissRequest = { showSettingsMenu = false },
                title = { Text("Настройки") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Профиль
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

                        // Тема
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

// Новые компоненты

data class RecommendedModule(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

val recommendedModules = listOf(
    RecommendedModule(
        id = "practice",
        title = "Практика",
        description = "Решайте задачи",
        icon = Icons.Rounded.Code,
        color = Color(0xFF6366F1) // Индиго
    ),
    RecommendedModule(
        id = "quiz",
        title = "Тесты",
        description = "Проверьте знания",
        icon = Icons.Rounded.Quiz,
        color = Color(0xFF10B981) // Изумрудный
    ),
    RecommendedModule(
        id = "interview",
        title = "Собеседование",
        description = "Подготовка к собесу",
        icon = Icons.Rounded.WorkspacePremium,
        color = Color(0xFFF59E0B) // Янтарный
    ),
    RecommendedModule(
        id = "full_course",
        title = "Полный курс",
        description = "Все уроки",
        icon = Icons.Rounded.School,
        color = Color(0xFF8B5CF6) // Фиолетовый
    )
)

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

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
fun DailyGoalCard(
    completed: Int,
    total: Int,
    onComplete: () -> Unit
) {
    val progress = completed.toFloat() / total.toFloat()

    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        onClick = onComplete,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Ежедневная цель",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Выполните $total заданий за день",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "$completed/$total",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Прогресс бар
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer
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
        color = MaterialTheme.colorScheme.surfaceContainerHigh, // 👈 Специальный цвет для карточек
        onClick = onClick,
        tonalElevation = 3.dp, // 👈 Увеличиваем тень
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

                Column(
                    modifier = Modifier.weight(1f)
                ) {
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

            // Информация о курсе
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

            // Прогресс бар
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

data class Achievement(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val achieved: Boolean,
    val xpReward: Int
)

val achievements = listOf(
    Achievement(
        title = "Первый код",
        description = "Напишите первую программу",
        icon = Icons.Rounded.Code,
        achieved = true,
        xpReward = 50
    ),
    Achievement(
        title = "Ученик",
        description = "Пройдите 10 уроков",
        icon = Icons.Rounded.School,
        achieved = true,
        xpReward = 100
    ),
    Achievement(
        title = "Тестировщик",
        description = "Ответьте на 20 вопросов",
        icon = Icons.Rounded.Quiz,
        achieved = false,
        xpReward = 150
    ),
    Achievement(
        title = "Профессионал",
        description = "Завершите все уроки курса",
        icon = Icons.Rounded.WorkspacePremium,
        achieved = false,
        xpReward = 500
    )
)

@Composable
fun AchievementBadge(
    achievement: Achievement,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(160.dp),
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
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        if (achievement.achieved)
                            MaterialTheme.colorScheme.secondary
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
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    achievement.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (achievement.achieved)
                        MaterialTheme.colorScheme.onSecondaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Text(
                    "+${achievement.xpReward} XP",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (achievement.achieved)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
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

// Функция расчёта уровня по XP
fun calculateLevel(totalXP: Int): Int {
    if (totalXP < 100) return 1
    if (totalXP < 250) return 2
    if (totalXP < 450) return 3
    if (totalXP < 700) return 4
    if (totalXP < 1000) return 5

    // Общая формула для больших значений
    var xp = totalXP
    var level = 1
    var xpForNextLevel = 100

    while (xp >= xpForNextLevel) {
        xp -= xpForNextLevel
        level++
        xpForNextLevel += 50 // Каждый уровень требует на 50 XP больше
    }

    return level
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

// Вспомогательная функция для определения системной темы
@Composable
fun isSystemInDarkTheme(): Boolean {
    return LocalConfiguration.current.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}