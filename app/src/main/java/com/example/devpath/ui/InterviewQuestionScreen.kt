package com.example.devpath.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.devpath.data.repository.FavoritesRepository
import com.example.devpath.domain.models.InterviewQuestion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterviewQuestionScreen(
    question: InterviewQuestion,
    onBack: () -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    // Используем актуальное состояние из репозитория
    val isFavorite = FavoritesRepository.isFavorite(question.id)

    // Цвет для сложности
    val difficultyColor = when (question.difficulty.lowercase()) {
        "beginner" -> Color(0xFF10B981)
        "intermediate" -> Color(0xFFF59E0B)
        "advanced" -> Color(0xFFEF4444)
        else -> MaterialTheme.colorScheme.primary
    }

    // Цвет для категории
    val categoryColor = when (question.category.lowercase()) {
        "kotlin" -> Color(0xFF7C3AED)
        "android" -> Color(0xFF3B82F6)
        "algorithms" -> Color(0xFF8B5CF6)
        "general" -> Color(0xFF059669)
        else -> MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // TopAppBar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Кнопка назад
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Назад",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Заголовок
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "Собеседование",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    "Подготовка к вопросам",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Кнопка избранного
            IconButton(
                onClick = { onToggleFavorite(question.id) },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isFavorite) MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (isFavorite) "Удалить из избранного" else "Добавить в избранное",
                    tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Содержимое вопроса
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Заголовок вопроса
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Вопрос
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                "❓ Вопрос",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = question.question,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 28.sp
                        )
                    }

                    // Метки категории и сложности
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Категория
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(categoryColor.copy(alpha = 0.1f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Category,
                                    contentDescription = "Категория",
                                    tint = categoryColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = question.category.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = categoryColor
                                )
                            }
                        }

                        // Сложность
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(difficultyColor.copy(alpha = 0.1f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.TrendingUp,
                                    contentDescription = "Сложность",
                                    tint = difficultyColor,
                                    modifier = Modifier.size(14.dp)
                                )
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
                }
            }

            // Ответ
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Lightbulb,
                                contentDescription = "Ответ",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Text(
                            "💡 Ответ",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Используем FormattedLessonContent для форматированного ответа с кодом
                    FormattedLessonContent(
                        content = question.answer,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Советы по ответу
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.TipsAndUpdates,
                                contentDescription = "Советы",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Text(
                            "💎 Советы по ответу",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }

                    Text(
                        text = getAnswerTips(question.category),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        lineHeight = 22.sp
                    )
                }
            }

            // Ключевые моменты
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                            Icons.Rounded.Key,
                            contentDescription = "Ключевые моменты",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            "🎯 Ключевые моменты",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        getKeyPoints(question.category).forEach { point ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "✓",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = point,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// Функция для получения советов по ответу
private fun getAnswerTips(category: String): String {
    return when (category.lowercase()) {
        "kotlin" -> "• Начните с определения ключевого понятия\n• Приведите пример кода с пояснениями\n• Объясните преимущества использования\n• Упомяните альтернативы\n• Расскажите о лучших практиках"
        "android" -> "• Упомяните жизненный цикл компонентов\n• Сравните с альтернативными подходами\n• Приведите пример из реальной практики\n• Обсудите ограничения\n• Расскажите о лучших практиках Android"
        "algorithms" -> "• Объясните временную и пространственную сложность\n• Предложите несколько решений\n• Обсудите edge cases\n• Приведите псевдокод или реальный код\n• Объясните, где можно применить"
        "general" -> "• Структурируйте ответ логично и последовательно\n• Используйте конкретные примеры\n• Покажите глубину понимания предмета\n• Упомяните связанные концепции\n• Будьте готовы к уточняющим вопросам"
        else -> "• Структурируйте ответ логично\n• Используйте примеры\n• Покажите глубину понимания\n• Будьте кратки, но информативны\n• Готовьтесь к follow-up вопросам"
    }
}

// Функция для получения ключевых моментов
private fun getKeyPoints(category: String): List<String> {
    return when (category.lowercase()) {
        "kotlin" -> listOf(
            "Назовите основные преимущества перед Java",
            "Приведите примеры синтаксиса",
            "Упомяните null safety систему",
            "Расскажите о корутинах",
            "Объясните data class и sealed class"
        )
        "android" -> listOf(
            "Упомяните жизненные циклы компонентов",
            "Расскажите о современных подходах (Jetpack)",
            "Объясните работу с памятью",
            "Упомяните лучшие практики",
            "Расскажите о тестировании"
        )
        "algorithms" -> listOf(
            "Объясните временную сложность O()",
            "Предложите несколько решений",
            "Обсудите ограничения подхода",
            "Приведите пример использования",
            "Упомяните оптимизации"
        )
        "general" -> listOf(
            "Структурируйте ответ по принципу STAR",
            "Приводите конкретные примеры",
            "Покажите системное мышление",
            "Будьте готовы к диалогу",
            "Задавайте уточняющие вопросы"
        )
        else -> listOf(
            "Будьте структурированы",
            "Используйте примеры",
            "Покажите понимание",
            "Будьте уверены в ответах",
            "Демонстрируйте опыт"
        )
    }
}