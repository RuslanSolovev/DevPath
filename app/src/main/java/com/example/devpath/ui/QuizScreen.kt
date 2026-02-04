package com.example.devpath.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.devpath.data.repository.ProgressRepository
import com.example.devpath.data.repository.QuizRepository
import com.example.devpath.domain.models.GeneralTestResult
import com.example.devpath.domain.models.QuizQuestion
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    parentNavController: NavHostController? = null
) {
    val allQuestions = QuizRepository.getQuizQuestions()
    val currentUser = Firebase.auth.currentUser
    val progressRepo = remember { ProgressRepository() }

    var selectedTab by remember { mutableStateOf(QuizTab.ALL_QUESTIONS) }

    // История тестов
    var testHistory by remember { mutableStateOf<List<GeneralTestResult>>(emptyList()) }
    var bestResult by remember { mutableStateOf<GeneralTestResult?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Ключ для обновления данных
    var refreshKey by remember { mutableStateOf(0) }

    LaunchedEffect(currentUser, refreshKey) {
        if (currentUser != null) {
            isLoading = true
            try {
                val progress = progressRepo.loadProgress(currentUser.uid)
                val history = progress?.generalTestHistory ?: emptyList()
                testHistory = history.sortedByDescending { it.timestamp }
                bestResult = getBestGeneralTestResult(history)
            } catch (e: Exception) {
                println("Ошибка загрузки истории тестов: ${e.message}")
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    // ✅ ДОБАВЛЕНО: LazyListState для collapsing header
    val listState = rememberLazyListState()

    // ✅ ДОБАВЛЕНО: Прямой расчёт высоты header
    val headerHeight by remember(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        derivedStateOf {
            if (listState.firstVisibleItemIndex == 0) {
                (260 - listState.firstVisibleItemScrollOffset).coerceAtLeast(0)
            } else {
                0
            }
        }
    }

    // ✅ ДОБАВЛЕНО: Альфа для плавного исчезновения
    val alpha by remember(headerHeight) {
        derivedStateOf {
            (headerHeight / 260f).coerceIn(0f, 1f)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // ✅ ДОБАВЛЕНО: Collapsing header
            item {
                Column {
                    // Header карточка
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(headerHeight.dp)
                            .graphicsLayer(alpha = alpha)
                    ) {
                        if (headerHeight > 0) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(headerHeight.dp)
                                    .padding(horizontal = 16.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(
                                            horizontal = 20.dp,
                                            vertical = if (headerHeight > 130) 20.dp else 24.dp
                                        ),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = if (headerHeight > 130)
                                        Arrangement.SpaceBetween
                                    else
                                        Arrangement.Center
                                ) {
                                    // Заголовок
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(if (headerHeight > 130) 60.dp else 48.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Rounded.AutoAwesome,
                                                contentDescription = "Тесты",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(if (headerHeight > 130) 32.dp else 24.dp)
                                            )
                                        }

                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "Тесты Kotlin",
                                                style = MaterialTheme.typography.headlineSmall.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                textAlign = TextAlign.Center,
                                                maxLines = if (headerHeight > 130) 2 else 1,
                                                overflow = TextOverflow.Ellipsis,
                                                fontSize = if (headerHeight > 130) 20.sp else 18.sp
                                            )

                                            Text(
                                                text = "${allQuestions.size} вопросов",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontSize = if (headerHeight > 130) 14.sp else 13.sp
                                                ),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center,
                                                maxLines = if (headerHeight > 130) 2 else 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    // Статистика (только при достаточной высоте)
                                    if (headerHeight > 130) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Все вопросы
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Rounded.List,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Column(
                                                        horizontalAlignment = Alignment.Start
                                                    ) {
                                                        Text(
                                                            text = allQuestions.size.toString(),
                                                            style = MaterialTheme.typography.titleMedium.copy(
                                                                fontWeight = FontWeight.Bold
                                                            ),
                                                            color = MaterialTheme.colorScheme.primary,
                                                            fontSize = 16.sp
                                                        )
                                                        Text(
                                                            text = "Всего",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            fontSize = 11.sp
                                                        )
                                                    }
                                                }
                                            }

                                            // Лучший результат
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Rounded.EmojiEvents,
                                                        contentDescription = null,
                                                        tint = if (bestResult?.percentage ?: 0 >= 70)
                                                            MaterialTheme.colorScheme.primary
                                                        else MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Column(
                                                        horizontalAlignment = Alignment.Start
                                                    ) {
                                                        Text(
                                                            text = "${bestResult?.percentage ?: 0}%",
                                                            style = MaterialTheme.typography.titleMedium.copy(
                                                                fontWeight = FontWeight.Bold
                                                            ),
                                                            color = if (bestResult?.percentage ?: 0 >= 70)
                                                                MaterialTheme.colorScheme.primary
                                                            else MaterialTheme.colorScheme.error,
                                                            fontSize = 16.sp
                                                        )
                                                        Text(
                                                            text = "Лучший",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            fontSize = 11.sp
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Табы
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .shadow(
                                elevation = if (headerHeight > 0) 4.dp else 0.dp,
                                shape = RoundedCornerShape(16.dp)
                            ),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        TabRow(
                            selectedTabIndex = selectedTab.ordinal,
                            modifier = Modifier.padding(horizontal = 8.dp),
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary,
                            indicator = { tabPositions ->
                                TabRowDefaults.Indicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                                    height = 3.dp,
                                    color = MaterialTheme.colorScheme.primary

                                )
                            }
                        ) {
                            QuizTab.entries.forEach { tab ->
                                Tab(
                                    selected = selectedTab == tab,
                                    onClick = { selectedTab = tab },
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = tab.title,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = tab.title,
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // ✅ ОБНОВЛЕНО: Контент без Scaffold
            when (selectedTab) {
                QuizTab.ALL_QUESTIONS -> {
                    items(allQuestions, key = { it.id }) { question ->
                        QuizQuestionCard(
                            question = question,
                            onClick = {
                                parentNavController?.navigate("quiz/question/${question.id}")
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                QuizTab.GENERAL_TEST -> {
                    item {
                        GeneralTestSectionContent(
                            isLoading = isLoading,
                            onStartTest = {
                                parentNavController?.navigate("quiz/general_test")
                            },
                            testHistory = testHistory,
                            bestResult = bestResult,
                            onHistoryItemClick = {
                                // Можно добавить просмотр деталей попытки
                            }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

enum class QuizTab(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    ALL_QUESTIONS("Все вопросы", Icons.Rounded.List),
    GENERAL_TEST("Общий тест", Icons.Rounded.AutoAwesome)
}

@Composable
private fun QuizQuestionCard(
    question: QuizQuestion,
    onClick: () -> Unit
) {
    val topicColors = mapOf(
        "kotlin_basics" to Color(0xFF6366F1),
        "null_safety" to Color(0xFF10B981),
        "strings" to Color(0xFFF59E0B),
        "collections" to Color(0xFFEF4444),
        "functions" to Color(0xFF8B5CF6),
        "control_flow" to Color(0xFF06B6D4),
        "operators" to Color(0xFFEC4899),
        "loops" to Color(0xFF14B8A6),
        "classes" to Color(0xFFF97316),
        "type_conversion" to Color(0xFF8B5CF6),
        "type_checking" to Color(0xFF84CC16)
    )

    val topicColor = topicColors[question.topic] ?: MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(140.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Иконка вопроса
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(topicColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.QuestionAnswer,
                    contentDescription = question.question,
                    tint = topicColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Текст вопроса
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    question.question,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )

                // Тема вопроса
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(topicColor.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        getTopicName(question.topic),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = topicColor
                    )
                }
            }

            // Стрелка перехода
            Icon(
                Icons.Rounded.ArrowForward,
                contentDescription = "Открыть",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun GeneralTestSectionContent(
    isLoading: Boolean,
    onStartTest: () -> Unit,
    testHistory: List<GeneralTestResult>,
    bestResult: GeneralTestResult?,
    onHistoryItemClick: (GeneralTestResult) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Баннер начала теста
        GeneralTestBanner(
            onStartTest = onStartTest,
            modifier = Modifier.fillMaxWidth()
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Лучший результат
            if (bestResult != null) {
                BestResultCard(bestResult = bestResult)
            } else {
                EmptyResultsCard()
            }

            // История тестов
            if (testHistory.isNotEmpty()) {
                TestHistorySection(
                    history = testHistory,
                    onItemClick = onHistoryItemClick
                )
            } else {
                EmptyHistoryCard()
            }
        }
    }
}

@Composable
private fun GeneralTestBanner(
    onStartTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(180.dp)
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onStartTest
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
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
                        Icons.Rounded.AutoAwesome,
                        contentDescription = "Тест",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Общий тест знаний",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "10 случайных вопросов\nПроверьте свои знания!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun BestResultCard(bestResult: GeneralTestResult) {
    val percentage = bestResult.percentage
    val color = when {
        percentage >= 90 -> Color(0xFF10B981) // Отлично - зелёный
        percentage >= 70 -> Color(0xFFF59E0B) // Хорошо - оранжевый
        else -> Color(0xFFEF4444) // Нужно подучить - красный
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.EmojiEvents,
                    contentDescription = "Лучший результат",
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "Лучший результат",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    "${bestResult.correctAnswers}/${bestResult.totalQuestions} правильных ответов",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.1f))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    "${percentage}%",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = color
                )
            }
        }
    }
}

@Composable
private fun TestHistorySection(
    history: List<GeneralTestResult>,
    onItemClick: (GeneralTestResult) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Последние попытки",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                "${history.size} попыток",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Отображаем последние 10 попыток
        history.take(10).forEachIndexed { index, result ->
            TestHistoryItem(
                result = result,
                index = index + 1,
                onClick = { onItemClick(result) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun TestHistoryItem(
    result: GeneralTestResult,
    index: Int,
    onClick: () -> Unit
) {
    val percentage = result.percentage
    val color = when {
        percentage >= 90 -> Color(0xFF10B981)
        percentage >= 70 -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }

    val dateFormatter = remember {
        SimpleDateFormat("dd.MM HH:mm", Locale.getDefault())
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Номер попытки
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    index.toString(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "${result.correctAnswers} из ${result.totalQuestions} правильных",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    dateFormatter.format(Date(result.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Процент
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.1f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    "${percentage}%",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = color
                )
            }
        }
    }
}

@Composable
private fun EmptyResultsCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Rounded.EmojiEvents,
                contentDescription = "Нет результатов",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )

            Text(
                "Пройдите тест первым!",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                "Здесь появится ваш лучший результат",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmptyHistoryCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Rounded.History,
                contentDescription = "Нет истории",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )

            Text(
                "История тестов пуста",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                "Пройдите общий тест,\nчтобы увидеть свою историю",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Вспомогательные функции
fun getBestGeneralTestResult(history: List<GeneralTestResult>): GeneralTestResult? {
    return history.maxByOrNull { it.percentage }
}

fun getTopicName(topic: String): String {
    return when (topic) {
        "kotlin_basics" -> "Основы Kotlin"
        "null_safety" -> "Null безопасность"
        "strings" -> "Строки"
        "collections" -> "Коллекции"
        "functions" -> "Функции"
        "control_flow" -> "Управление потоком"
        "operators" -> "Операторы"
        "loops" -> "Циклы"
        "classes" -> "Классы"
        "type_conversion" -> "Преобразование типов"
        "type_checking" -> "Проверка типов"
        else -> topic
    }
}