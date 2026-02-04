package com.example.devpath.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.devpath.data.repository.LessonRepository
import com.example.devpath.data.repository.ProgressRepository
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import com.example.devpath.domain.models.Lesson
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonListScreen(
    onLessonClick: (String) -> Unit
) {
    val lessons = LessonRepository.getLessons()
    val currentUser = Firebase.auth.currentUser
    val progressRepo = remember { ProgressRepository() }

    // Состояние для загрузки завершенных уроков
    var completedLessons by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isLoading by remember { mutableStateOf(true) }

    // Загружаем завершенные уроки
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            isLoading = true
            try {
                val progress = progressRepo.loadProgress(currentUser.uid)
                completedLessons = progress?.completedLessons?.toSet() ?: emptySet()
            } catch (e: Exception) {
                println("Ошибка загрузки прогресса уроков: ${e.message}")
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    // Цвета для карточек
    val colors = listOf(
        Color(0xFF6366F1), // Индиго
        Color(0xFF10B981), // Изумрудный
        Color(0xFFF59E0B), // Янтарный
        Color(0xFFEF4444), // Красный
        Color(0xFF8B5CF6), // Фиолетовый
        Color(0xFF06B6D4), // Голубой
    )

    // Иконки для уроков
    val icons = listOf(
        Icons.Rounded.Code,
        Icons.Rounded.Functions,
        Icons.Rounded.DataArray,
        Icons.Rounded.Category,
        Icons.Rounded.Sync,
        Icons.Rounded.WorkspacePremium,
    )

    // Создаем список уроков с актуальным состоянием завершенности
    val lessonsWithCompletion = remember(lessons, completedLessons, isLoading) {
        lessons.map { lesson ->
            lesson.copy(isCompleted = completedLessons.contains(lesson.id))
        }.sortedBy { it.order }
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = {
                    Column {
                        Text(
                            "Уроки Kotlin",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        if (!isLoading) {
                            val completedCount = lessonsWithCompletion.count { it.isCompleted }
                            Text(
                                "$completedCount из ${lessons.size} уроков пройдено",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                "Загрузка прогресса...",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                ),
                actions = {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Группируем уроки по завершенности
                val (completed, notCompleted) = lessonsWithCompletion.partition { it.isCompleted }

                if (completed.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Outlined.CheckCircle,
                                contentDescription = "Завершенные",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Завершенные уроки",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                "${completed.size}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    items(completed.chunked(2), key = { chunk -> chunk.hashCode() }) { chunk ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            chunk.forEach { lesson ->
                                val colorIndex = (lessonsWithCompletion.indexOf(lesson) % colors.size)
                                val iconIndex = (lessonsWithCompletion.indexOf(lesson) % icons.size)

                                LessonCard(
                                    lesson = lesson,
                                    color = colors[colorIndex],
                                    icon = icons[iconIndex],
                                    onClick = { onLessonClick(lesson.id) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(220.dp)
                                )
                            }

                            if (chunk.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                if (notCompleted.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Rounded.MenuBook,
                                contentDescription = "Для изучения",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                "Для изучения",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                "${notCompleted.size}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    items(notCompleted.chunked(2), key = { chunk -> chunk.hashCode() }) { chunk ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            chunk.forEach { lesson ->
                                val colorIndex = (lessonsWithCompletion.indexOf(lesson) % colors.size)
                                val iconIndex = (lessonsWithCompletion.indexOf(lesson) % icons.size)

                                LessonCard(
                                    lesson = lesson,
                                    color = colors[colorIndex],
                                    icon = icons[iconIndex],
                                    onClick = { onLessonClick(lesson.id) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(220.dp)
                                )
                            }

                            if (chunk.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun LessonCard(
    lesson: Lesson,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            color,
            color.copy(alpha = 0.8f)
        )
    )

    val animatedElevation by animateFloatAsState(
        targetValue = 2f,
        animationSpec = tween(300),
        label = "lessonCardElevation"
    )

    Card(
        modifier = modifier
            .shadow(
                elevation = animatedElevation.dp,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Иконка урока
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(gradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = lesson.title,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Название урока - всегда 2 строки
            Text(
                text = lesson.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )

            // Описание урока - всегда 2 строки
            Text(
                text = lesson.description,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Информация об уроке
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Сложность
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(color.copy(alpha = 0.1f))
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
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp
                        ),
                        color = color
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
                    )
                    Text(
                        text = "${lesson.duration} мин",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Индикатор прогресса (если есть)
            if (lesson.isCompleted) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Outlined.CheckCircle,
                        contentDescription = "Завершено",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Пройдено",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}