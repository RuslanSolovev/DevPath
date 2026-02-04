@file:OptIn(ExperimentalFoundationApi::class)

package com.example.devpath.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.devpath.data.repository.FavoritesRepository
import com.example.devpath.data.repository.InterviewRepository
import com.example.devpath.data.repository.ProgressRepository
import com.example.devpath.domain.models.InterviewQuestion
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterviewScreen() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "question_list"
    ) {
        composable("question_list") {
            InterviewQuestionListScreen(navController)
        }

        composable("question/{questionId}") { backStackEntry ->
            InterviewQuestionDetailScreen(
                backStackEntry = backStackEntry,
                navController = navController
            )
        }
    }
}

@Composable
fun InterviewQuestionDetailScreen(
    backStackEntry: androidx.navigation.NavBackStackEntry,
    navController: NavHostController
) {
    val questionId = backStackEntry.arguments?.getString("questionId") ?: "iq1"
    val question = InterviewRepository.getQuestionById(questionId)
        ?: InterviewRepository.getInterviewQuestions().first()

    val currentUser = Firebase.auth.currentUser
    val coroutineScope = rememberCoroutineScope()
    val progressRepo = remember { ProgressRepository() }

    InterviewQuestionScreen(
        question = question,
        onBack = { navController.popBackStack() },
        onToggleFavorite = { id ->
            if (currentUser != null) {
                val isNowFavorite = !FavoritesRepository.isFavorite(id)
                FavoritesRepository.toggleFavorite(id)
                coroutineScope.launch {
                    progressRepo.toggleFavoriteInterviewQuestion(
                        currentUser.uid,
                        id,
                        isNowFavorite
                    )
                }
            } else {
                FavoritesRepository.toggleFavorite(id)
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InterviewQuestionListScreen(navController: NavHostController) {
    val allQuestions = InterviewRepository.getInterviewQuestions()
    val favoriteQuestions by remember {
        derivedStateOf {
            FavoritesRepository.getFavoriteQuestions(allQuestions)
        }
    }
    val currentUser = Firebase.auth.currentUser
    val coroutineScope = rememberCoroutineScope()
    val progressRepo = remember { ProgressRepository() }

    var selectedTab by remember { mutableStateOf(InterviewTab.ALL) }
    val questionsToShow = remember(selectedTab) {
        if (selectedTab == InterviewTab.ALL) allQuestions else favoriteQuestions
    }

    // ✅ ПРОСТОЙ STATE ДЛЯ СКРОЛЛА
    val listState = rememberLazyListState()

    // ✅ ПРЯМОЙ РАСЧЁТ ВЫСОТЫ HEADER
    val headerHeight by remember(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        derivedStateOf {
            if (listState.firstVisibleItemIndex == 0) {
                // Полная высота когда первый элемент виден
                (260 - listState.firstVisibleItemScrollOffset).coerceAtLeast(0)
            } else {
                // Полностью скрыт когда скроллим дальше
                0
            }
        }
    }

    // ✅ ПРОСТАЯ АЛЬФА ДЛЯ ПЛАВНОГО ИСЧЕЗНОВЕНИЯ
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
            item {
                Column {
                    // ✅ ПРОСТОЙ COLLAPSING HEADER
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(headerHeight.dp)
                            .alpha(alpha)
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
                                    // Top section - icon and title
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
                                                Icons.Rounded.QuestionAnswer,
                                                contentDescription = "Собеседование",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(if (headerHeight > 130) 32.dp else 24.dp)
                                            )
                                        }

                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "Подготовка к собеседованию",
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
                                                text = "Ответы на популярные вопросы",
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

                                    // Статистика (показываем только когда достаточно места)
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

                                            // Избранные
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
                                                        Icons.Rounded.Favorite,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Column(
                                                        horizontalAlignment = Alignment.Start
                                                    ) {
                                                        Text(
                                                            text = favoriteQuestions.size.toString(),
                                                            style = MaterialTheme.typography.titleMedium.copy(
                                                                fontWeight = FontWeight.Bold
                                                            ),
                                                            color = MaterialTheme.colorScheme.error,
                                                            fontSize = 16.sp
                                                        )
                                                        Text(
                                                            text = "Избранных",
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

                    // Вкладки
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
                            InterviewTab.entries.forEach { tab ->
                                Tab(
                                    selected = selectedTab == tab,
                                    onClick = {
                                        selectedTab = tab
                                        coroutineScope.launch {
                                            listState.animateScrollToItem(0)
                                        }
                                    },
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = when (tab) {
                                                InterviewTab.ALL -> Icons.Rounded.List
                                                InterviewTab.FAVORITES -> Icons.Rounded.Favorite
                                            },
                                            contentDescription = null,
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

            // Список вопросов
            if (questionsToShow.isEmpty()) {
                item {
                    EmptyState(
                        icon = if (selectedTab == InterviewTab.ALL)
                            Icons.Rounded.QuestionMark
                        else
                            Icons.Rounded.FavoriteBorder,
                        title = if (selectedTab == InterviewTab.ALL)
                            "Вопросы не найдены"
                        else
                            "Нет избранных вопросов",
                        subtitle = if (selectedTab == InterviewTab.ALL)
                            "Попробуйте позже"
                        else
                            "Нажимайте ❤️ на вопросах, чтобы добавить их сюда"
                    )
                }
            } else {
                items(items = questionsToShow, key = { it.id }) { question ->
                    InterviewQuestionItem(
                        question = question,
                        onClick = { questionId ->
                            navController.navigate("question/$questionId")
                        },
                        onToggleFavorite = { questionId ->
                            if (currentUser != null) {
                                val isNowFavorite = !FavoritesRepository.isFavorite(questionId)
                                FavoritesRepository.toggleFavorite(questionId)
                                coroutineScope.launch {
                                    progressRepo.toggleFavoriteInterviewQuestion(
                                        currentUser.uid,
                                        questionId,
                                        isNowFavorite
                                    )
                                }
                            } else {
                                FavoritesRepository.toggleFavorite(questionId)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun InterviewQuestionItem(
    question: InterviewQuestion,
    onClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    // Цвета для категории и сложности
    val categoryColor = getCategoryColor(question.category)
    val difficultyColor = getDifficultyColor(question.difficulty)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = { onClick(question.id) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Иконка вопроса
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(categoryColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.HelpOutline,
                    contentDescription = "Вопрос",
                    tint = categoryColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Контент
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Вопрос
                Text(
                    text = question.question,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Метки
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Категория
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(categoryColor.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = question.category.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = categoryColor
                        )
                    }

                    // Сложность
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(difficultyColor.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = when (question.difficulty.lowercase()) {
                                "beginner" -> "Начальный"
                                "intermediate" -> "Средний"
                                "advanced" -> "Продвинутый"
                                else -> "Начальный"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = difficultyColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Кнопка избранного
            IconButton(
                onClick = { onToggleFavorite(question.id) },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (question.isFavorite) MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
            ) {
                Icon(
                    imageVector = if (question.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (question.isFavorite) "Удалить из избранного" else "Добавить в избранное",
                    tint = if (question.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// Вспомогательные функции для цветов
@Composable
fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "kotlin" -> Color(0xFF7C3AED) // Фиолетовый
        "android" -> Color(0xFF3B82F6) // Синий
        "algorithms" -> Color(0xFF8B5CF6) // Индиго
        "general" -> Color(0xFF059669) // Изумрудный
        else -> MaterialTheme.colorScheme.primary
    }
}

@Composable
fun getDifficultyColor(difficulty: String): Color {
    return when (difficulty.lowercase()) {
        "beginner" -> Color(0xFF10B981) // Зеленый
        "intermediate" -> Color(0xFFF59E0B) // Оранжевый
        "advanced" -> Color(0xFFEF4444) // Красный
        else -> MaterialTheme.colorScheme.primary
    }
}

enum class InterviewTab(val title: String) {
    ALL("Все вопросы"),
    FAVORITES("Избранное")
}