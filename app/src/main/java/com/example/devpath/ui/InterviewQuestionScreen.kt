package com.example.devpath.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Собеседование") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onToggleFavorite(question.id) }
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Удалить из избранного" else "Добавить в избранное",
                            tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Категория и сложность
            Row(
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                FilledTonalButton(
                    onClick = { /* Фильтр по категории */ },
                    enabled = false
                ) {
                    Text(question.category.capitalize())
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = { /* Фильтр по сложности */ },
                    enabled = false
                ) {
                    Text(question.difficulty.capitalize())
                }
            }

            // Вопрос
            Text(
                text = question.question,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Ответ
            Text(
                text = question.answer,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Советы по ответу
            Card(
                modifier = Modifier.fillMaxSize(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💡 Советы по ответу:",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = when (question.category) {
                            "kotlin" -> "• Начните с определения ключевого понятия\n• Приведите пример кода\n• Объясните преимущества использования"
                            "android" -> "• Упомяните жизненный цикл компонентов\n• Сравните с альтернативными подходами\n• Приведите пример из практики"
                            "algorithms" -> "• Объясните временную и пространственную сложность\n• Предложите несколько решений\n• Обсудите edge cases"
                            else -> "• Структурируйте ответ логично\n• Используйте примеры\n• Покажите глубину понимания"
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

// Расширение для capitalize
private fun String.capitalize(): String {
    return replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}