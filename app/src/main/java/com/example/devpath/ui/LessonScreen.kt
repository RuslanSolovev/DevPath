package com.example.devpath.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.devpath.data.repository.LessonRepository
import com.example.devpath.data.repository.PracticeRepository
import com.example.devpath.data.repository.QuizRepository
import com.example.devpath.ui.viewmodel.ProgressViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

// Импортируем ваши компоненты
import com.example.devpath.ui.CodeBlock
import com.example.devpath.ui.FormattedLessonContent

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalAnimationApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun LessonScreen(
    lessonTitle: String,
    lessonContent: String,
    lessonId: String,
    onBack: () -> Unit,
    onNavigateToPractice: (String) -> Unit = {},
    onNavigateToQuiz: (String) -> Unit = {},
    onNavigateToGeneralTest: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val viewModel: ProgressViewModel = hiltViewModel()
    val progressRepo = viewModel.progressRepository
    val currentUser = Firebase.auth.currentUser
    val listState = rememberLazyListState()
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    val lesson = remember(lessonId) {
        LessonRepository.getLessonById(lessonId) ?: LessonRepository.getLessons().first()
    }

    val practiceTasks = remember(lesson.topic) {
        PracticeRepository.getTasksByTopic(lesson.topic)
    }

    val quizQuestions = remember(lesson.topic) {
        QuizRepository.getQuestionsByTopic(lesson.topic)
    }

    var isMarkedAsCompleted by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isCodeFullScreen by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0: теория, 1: код, 2: практика

    val showScrollToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }

    LaunchedEffect(currentUser, lessonId) {
        if (currentUser != null) {
            isLoading = true
            try {
                val progress = progressRepo.loadProgress(currentUser.uid)
                isMarkedAsCompleted = progress?.completedLessons?.contains(lessonId) ?: false
            } catch (e: Exception) {
                println("Ошибка загрузки состояния урока: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    val fabScale by animateFloatAsState(
        targetValue = if (isMarkedAsCompleted) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = 0.5f,
            stiffness = Spring.StiffnessLow
        ),
        label = "fab_scale_animation"
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                ExtendedFloatingActionButton(
                    onClick = {
                        if (currentUser != null && !isMarkedAsCompleted && !isLoading) {
                            coroutineScope.launch {
                                isLoading = true
                                try {
                                    progressRepo.markLessonCompleted(currentUser.uid, lessonId)
                                    isMarkedAsCompleted = true
                                    snackbarHostState.showSnackbar(
                                        message = "🎉 Урок отмечен как пройденный!",
                                        duration = SnackbarDuration.Short
                                    )
                                } catch (e: Exception) {
                                    println("Ошибка сохранения урока: ${e.message}")
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    },
                    icon = {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                if (isMarkedAsCompleted) Icons.Default.DoneAll else Icons.Default.CheckCircle,
                                contentDescription = "Завершить урок"
                            )
                        }
                    },
                    text = {
                        Text(
                            when {
                                isLoading -> "Сохранение..."
                                isMarkedAsCompleted -> "Урок пройден!"
                                else -> "Отметить пройденным"
                            }
                        )
                    },
                    containerColor = if (isMarkedAsCompleted)
                        MaterialTheme.colorScheme.secondaryContainer
                    else
                        MaterialTheme.colorScheme.primaryContainer,
                    contentColor = if (isMarkedAsCompleted)
                        MaterialTheme.colorScheme.onSecondaryContainer
                    else
                        MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.scale(fabScale)
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = paddingValues.calculateTopPadding() + 16.dp,
                    bottom = 100.dp
                )
            ) {
                item {
                    LessonHeader(
                        lesson = lesson,
                        isCompleted = isMarkedAsCompleted,
                        onBack = onBack
                    )
                }

                // Вкладки для навигации по контенту
                item {
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        edgePadding = 0.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("Теория", "Примеры кода", "Практика").forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                            )
                        }
                    }
                }

                // Контент в зависимости от выбранной вкладки
                when (selectedTab) {
                    0 -> {
                        item {
                            LessonTheoryContent(
                                lessonContent = lessonContent
                            )
                        }
                        // Добавляем секцию с реальными примерами из разработки
                        item {
                            RealWorldExamples()
                        }
                        // Секция с частыми ошибками
                        item {
                            CommonPitfalls()
                        }
                    }
                    1 -> {
                        if (lesson.codeExample.isNotBlank()) {
                            item {
                                LessonCodeExample(
                                    code = lesson.codeExample,
                                    onCopy = {
                                        clipboardManager.setText(AnnotatedString(lesson.codeExample))
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "📋 Код скопирован!",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    },
                                    onExpand = { isCodeFullScreen = true }
                                )
                            }
                        }
                    }
                    2 -> {
                        if (practiceTasks.isNotEmpty() || quizQuestions.isNotEmpty()) {
                            item {
                                SequentialLearningPath(
                                    lessonTopic = lesson.topic,
                                    practiceTasks = practiceTasks,
                                    quizQuestions = quizQuestions,
                                    onPracticeClick = onNavigateToPractice,
                                    onQuizClick = onNavigateToQuiz,
                                    onGeneralTestClick = onNavigateToGeneralTest
                                )
                            }
                        } else {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Text(
                                        "Практические задания по этой теме появятся позже.",
                                        modifier = Modifier.padding(16.dp),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }

            // Улучшенный индикатор прогресса чтения
            ReadingProgressIndicator(
                listState = listState,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = paddingValues.calculateTopPadding() + 16.dp, end = 16.dp)
            )

            // Кнопка "Наверх"
            AnimatedVisibility(
                visible = showScrollToTop,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 160.dp, end = 16.dp)
            ) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(0)
                        }
                    },
                    modifier = Modifier.size(48.dp),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = "Наверх")
                }
            }

            // Полноэкранный режим для кода
            if (isCodeFullScreen) {
                FullScreenCodeDialog(
                    code = lesson.codeExample,
                    onDismiss = { isCodeFullScreen = false },
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(lesson.codeExample))
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = "📋 Код скопирован!",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ReadingProgressIndicator(
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val itemHeights = remember { mutableStateMapOf<Int, Int>() }

    LaunchedEffect(listState.layoutInfo) {
        val visibleItems = listState.layoutInfo.visibleItemsInfo
        visibleItems.forEach { item ->
            if (!itemHeights.containsKey(item.index)) {
                itemHeights[item.index] = item.size
            }
        }
    }

    val progress by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val viewportHeight = layoutInfo.viewportSize.height
            val totalItemsCount = layoutInfo.totalItemsCount
            val visibleItemsInfo = layoutInfo.visibleItemsInfo

            if (totalItemsCount == 0 || visibleItemsInfo.isEmpty()) return@derivedStateOf 0f

            val firstVisibleItem = visibleItemsInfo.first()
            val firstVisibleIndex = firstVisibleItem.index
            val firstVisibleOffset = firstVisibleItem.offset.toFloat()

            val scrolledKnownHeight = itemHeights
                .filterKeys { it < firstVisibleIndex }
                .values
                .sum()

            val currentVisiblePart = (-firstVisibleOffset).coerceAtLeast(0f)
            val totalScrolledHeight = scrolledKnownHeight + currentVisiblePart

            val knownHeightSum = itemHeights.values.sum()
            val knownItemsCount = itemHeights.size

            val averageKnownHeight = if (knownItemsCount > 0) {
                knownHeightSum.toFloat() / knownItemsCount
            } else {
                500f
            }

            val remainingItemsCount = (totalItemsCount - knownItemsCount).coerceAtLeast(0)
            val estimatedRemainingHeight = remainingItemsCount * averageKnownHeight

            val estimatedTotalHeight = knownHeightSum + estimatedRemainingHeight
            val maxScroll = (estimatedTotalHeight - viewportHeight).coerceAtLeast(1f)

            (totalScrolledHeight / maxScroll).coerceIn(0f, 1f)
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 100, easing = LinearEasing),
        label = "progress_animation"
    )

    val percentage = (animatedProgress * 100).toInt().coerceIn(0, 100)

    val progressColor = when {
        percentage < 25 -> MaterialTheme.colorScheme.primary
        percentage < 50 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        percentage < 75 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.tertiary
    }

    if (listState.layoutInfo.totalItemsCount == 0) return

    Box(
        modifier = modifier
            .padding(8.dp)
            .size(56.dp)
            .clip(CircleShape)
            .background(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                CircleShape
            )
            .shadow(16.dp, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.size(46.dp),
            strokeWidth = 3.5.dp,
            color = progressColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Text(
            text = "$percentage%",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FullScreenCodeDialog(
    code: String,
    onDismiss: () -> Unit,
    onCopy: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Полный код",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row {
                        IconButton(onClick = onCopy) {
                            Icon(
                                Icons.Outlined.ContentCopy,
                                contentDescription = "Копировать",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Закрыть",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                Divider()
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    CodeBlock(
                        code = code,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// ==================== LessonHeader ====================
@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun LessonHeader(
    lesson: com.example.devpath.domain.models.Lesson,
    isCompleted: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isCompleted)
            MaterialTheme.colorScheme.secondaryContainer
        else
            MaterialTheme.colorScheme.primaryContainer,
        label = "header_color_animation",
        animationSpec = tween(durationMillis = 300)
    )

    val contentColor = if (isCompleted)
        MaterialTheme.colorScheme.onSecondaryContainer
    else
        MaterialTheme.colorScheme.onPrimaryContainer

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(contentColor.copy(alpha = 0.15f))
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        tint = contentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                )
                            )
                        )
                        .shadow(6.dp, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isCompleted) Icons.Rounded.DoneAll else Icons.Rounded.MenuBook,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "📘 Урок",
                        style = MaterialTheme.typography.labelLarge,
                        color = contentColor.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        lesson.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 24.sp
                    )
                    Text(
                        lesson.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val difficultyColor = when (lesson.difficulty) {
                    "beginner" -> MaterialTheme.colorScheme.tertiary
                    "intermediate" -> MaterialTheme.colorScheme.secondary
                    "advanced" -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.primary
                }

                @Composable
                fun InfoChip(
                    text: String,
                    icon: androidx.compose.ui.graphics.vector.ImageVector,
                    chipColor: Color
                ) {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = text,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                icon,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color.Transparent,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = AssistChipDefaults.assistChipBorder(
                            enabled = true,
                            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            borderWidth = 1.dp
                        ),
                        modifier = Modifier.heightIn(min = 36.dp)
                    )
                }

                InfoChip(
                    text = when (lesson.difficulty) {
                        "beginner" -> "🌱 Начальный"
                        "intermediate" -> "📊 Средний"
                        "advanced" -> "🔥 Продвинутый"
                        else -> "🌱 Начальный"
                    },
                    icon = Icons.Outlined.Star,
                    chipColor = difficultyColor
                )

                InfoChip(
                    text = "${lesson.duration} мин",
                    icon = Icons.Outlined.Schedule,
                    chipColor = MaterialTheme.colorScheme.primary
                )

                InfoChip(
                    text = lesson.topic.replace("_", " ").replaceFirstChar { it.uppercase() },
                    icon = Icons.Outlined.Folder,
                    chipColor = MaterialTheme.colorScheme.tertiary
                )
            }

            AnimatedVisibility(visible = isCompleted) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                "Урок успешно пройден",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LessonTheoryContent(
    lessonContent: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Rounded.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        "Теория",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Divider(
                modifier = Modifier.padding(horizontal = 20.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            FormattedLessonContent(
                content = lessonContent,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "${lessonContent.length} символов",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LessonCodeExample(
    code: String,
    onCopy: () -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Rounded.Code,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        "Пример кода",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row {
                    IconButton(onClick = onCopy) {
                        Icon(
                            Icons.Outlined.ContentCopy,
                            contentDescription = "Копировать код",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onExpand) {
                        Icon(
                            Icons.Outlined.Fullscreen,
                            contentDescription = "На весь экран",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Divider(
                modifier = Modifier.padding(horizontal = 20.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (expanded) {
                            Modifier.wrapContentHeight(unbounded = true)
                        } else {
                            Modifier.heightIn(min = 120.dp, max = 240.dp)
                        }
                    )
                    .animateContentSize()
            ) {
                CodeBlock(
                    code = code,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(
                    onClick = { expanded = !expanded },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        if (expanded) "Свернуть" else "Развернуть код",
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.Lightbulb,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        "Скопируйте и запустите код в своей среде разработки",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun RealWorldExamples(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Work, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Реальные примеры из разработки",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            Text(
                "1. **Android**: безопасное чтение из Bundle: `bundle.getString(\"key\") ?: \"\"`",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                "2. **Retrofit**: использование `@Nullable` и `@NotNull` аннотаций для API-моделей",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                "3. **Room**: обработка nullable полей в базе данных с помощью `@TypeConverters`",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                "4. **Kotlin Coroutines**: безопасная обработка nullable результатов асинхронных вызовов",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@Composable
fun CommonPitfalls(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Частые ошибки и как их избежать",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Text(
                "❌ Использование `!!` без проверки → замените на `?.` или `?:`",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                "❌ Игнорирование платформенных типов из Java → всегда явно указывайте тип (String или String?)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                "❌ Возврат `null` из функций, когда можно вернуть пустую коллекцию или `Result`",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                "✅ Вместо этого используйте `Result.success(emptyList())` или `null` только в особых случаях",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
fun SequentialLearningPath(
    lessonTopic: String,
    practiceTasks: List<Any>,
    quizQuestions: List<Any>,
    onPracticeClick: (String) -> Unit,
    onQuizClick: (String) -> Unit,
    onGeneralTestClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
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
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Rounded.Timeline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    "Путь обучения",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                "Закрепите материал на практике:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (practiceTasks.isNotEmpty()) {
                    LearningCard(
                        title = "Практика",
                        count = practiceTasks.size,
                        icon = Icons.Rounded.Code,
                        color = MaterialTheme.colorScheme.primary,
                        onClick = { onPracticeClick(practiceTasks.firstOrNull()?.toString() ?: "") },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (quizQuestions.isNotEmpty()) {
                    LearningCard(
                        title = "Тесты",
                        count = quizQuestions.size,
                        icon = Icons.Rounded.Quiz,
                        color = MaterialTheme.colorScheme.secondary,
                        onClick = { onQuizClick(quizQuestions.firstOrNull()?.toString() ?: "") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            OutlinedButton(
                onClick = onGeneralTestClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Пройти общий тест по теме")
            }
        }
    }
}

@Composable
fun LearningCard(
    title: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.12f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = color
            )
            Text(
                "$count заданий",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

