package com.example.devpath.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.devpath.data.repository.LessonRepository
import com.example.devpath.data.repository.PracticeRepository
import com.example.devpath.data.repository.QuizRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.devpath.ui.viewmodel.ProgressViewModel

@OptIn(ExperimentalMaterial3Api::class)
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

    val lesson = LessonRepository.getLessonById(lessonId) ?: LessonRepository.getLessons().first()

    // Получаем практику и тесты по теме урока
    val practiceTasks = PracticeRepository.getTasksByTopic(lesson.topic)
    val quizQuestions = QuizRepository.getQuestionsByTopic(lesson.topic)

    // Состояние для отслеживания завершения урока
    var isMarkedAsCompleted by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Загружаем состояние урока при открытии
    LaunchedEffect(currentUser, lessonId) {
        if (currentUser != null) {
            isLoading = true
            try {
                val progress = progressRepo.loadProgress(currentUser.uid)
                val isCompleted = progress?.completedLessons?.contains(lessonId) ?: false
                isMarkedAsCompleted = isCompleted
            } catch (e: Exception) {
                println("Ошибка загрузки состояния урока: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        lessonTitle,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Назад",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (currentUser != null && !isMarkedAsCompleted && !isLoading) {
                        coroutineScope.launch {
                            isLoading = true
                            try {
                                progressRepo.markLessonCompleted(currentUser.uid, lessonId)
                                isMarkedAsCompleted = true
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
                            strokeWidth = 2.dp
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
                            else -> "Отметить как пройденный"
                        }
                    )
                },
                containerColor = if (isMarkedAsCompleted)
                    MaterialTheme.colorScheme.secondary
                else
                    MaterialTheme.colorScheme.primary,
                contentColor = if (isMarkedAsCompleted)
                    MaterialTheme.colorScheme.onSecondary
                else
                    MaterialTheme.colorScheme.onPrimary,
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Баннер урока
            item {
                LessonHeader(lesson = lesson)
            }

            // Теория урока
            item {
                LessonTheoryContent(lessonContent = lessonContent)
            }

            // Пример кода
            item {
                LessonCodeExample(codeExample = lesson.codeExample)
            }

            // Советы
            item {
                LessonTips()
            }

            // Рекомендации по теме
            if (practiceTasks.isNotEmpty() || quizQuestions.isNotEmpty()) {
                item {
                    SequentialLearningPath(
                        lessonTopic = lesson.topic,
                        practiceTasks = practiceTasks,
                        quizQuestions = quizQuestions,
                        onPracticeClick = { taskId ->
                            if (practiceTasks.isNotEmpty()) {
                                onNavigateToPractice(taskId)
                            }
                        },
                        onQuizClick = { questionId ->
                            if (quizQuestions.isNotEmpty()) {
                                onNavigateToQuiz(questionId)
                            }
                        },
                        onGeneralTestClick = onNavigateToGeneralTest
                    )
                }
            }
        }
    }
}

@Composable
private fun LessonHeader(lesson: com.example.devpath.domain.models.Lesson) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Урок",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    lesson.title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    lesson.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Информация о сложности и продолжительности
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Сложность
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = when (lesson.difficulty) {
                                "beginner" -> "Начальный"
                                "intermediate" -> "Средний"
                                "advanced" -> "Продвинутый"
                                else -> "Начальный"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Продолжительность
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Schedule,
                            contentDescription = "Продолжительность",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${lesson.duration} мин",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LessonTheoryContent(lessonContent: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Теория",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Счётчик символов
                Text(
                    "${lessonContent.length} символов",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Используем новый FormattedLessonContent вместо старой функции
            FormattedLessonContent(
                content = lessonContent,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun LessonCodeExample(codeExample: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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

            // Используем CodeBlock из вашего файла
            CodeBlock(
                code = codeExample,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 400.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "💡 Скопируйте и запустите код в своей среде разработки",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}

@Composable
private fun LessonTips() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    "Совет",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            Text(
                "Прочитайте материал внимательно и попробуйте повторить примеры кода. " +
                        "Практика - ключ к успеху в программировании! Не стесняйтесь экспериментировать " +
                        "с кодом и изменять его, чтобы лучше понять как он работает.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                lineHeight = 22.sp
            )
        }
    }
}

// Добавьте в конец LessonScreen.kt (для отладки и предпросмотра)
@Preview(showBackground = true)
@Composable
fun LessonScreenPreview() {
    MaterialTheme {
        LessonScreen(
            lessonTitle = "Основы Kotlin",
            lessonContent = """
                # Основы Kotlin
                
                Kotlin — современный язык программирования, разработанный компанией JetBrains.
                Он полностью совместим с Java и работает на JVM, Android, браузерах и нативных платформах.
                
                ## Почему Kotlin?
                
                1. **Краткость** — на 40% меньше кода по сравнению с Java
                2. **Безопасность** — защита от NullPointerException
                3. **Совместимость** — полная совместимость с Java
                4. **Функциональный подход** — поддержка функций высшего порядка
                5. **Мультиплатформенность** — одна кодовая база для разных платформ
                
                ## Переменные
                
                В Kotlin есть два типа переменных:
                - `val` — неизменяемая переменная (аналог final в Java)
                - `var` — изменяемая переменная
                
                ```kotlin
                // Примеры объявления переменных
                val name = "Kotlin"           // нельзя изменить
                var age = 5                   // можно изменить
                val pi: Double = 3.14159      // явное указание типа
                var isReady = false           // логический тип
                ```
            """.trimIndent(),
            lessonId = "kotlin_basics",
            onBack = {},
            onNavigateToPractice = {},
            onNavigateToQuiz = {},
            onNavigateToGeneralTest = {}
        )
    }
}