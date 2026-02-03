package com.example.devpath.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.devpath.ui.fffff.Green40

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestResultsScreen(
    correctAnswers: Int,
    totalQuestions: Int,
    onRetry: () -> Unit,
    onBackToMain: () -> Unit
) {
    val percentage = (correctAnswers.toFloat() / totalQuestions.toFloat()) * 100
    val isPassed = percentage >= 70 // Проходной балл 70%

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Результаты теста") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                // Иконка результата
                Icon(
                    imageVector = if (isPassed) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (isPassed) Green40 else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Основной результат
                Text(
                    text = "$correctAnswers / $totalQuestions",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isPassed) Green40 else MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${percentage.toInt()}% правильных ответов",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Сообщение
                Text(
                    text = if (isPassed) {
                        "Отличный результат! 🎉\nВы хорошо знаете материал."
                    } else {
                        "Нужно повторить! 📚\nНе расстраивайтесь, практика решает всё."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))
            }

            // Анализ по темам (заглушка - можно расширить позже)
            item {
                Text(
                    "Анализ по темам",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // Здесь будет анализ по темам когда добавим данные
                Text(
                    "• Kotlin Basics: 80% ✅\n• Null Safety: 60% ⚠️\n• Collections: 90% ✅",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Кнопки
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onRetry,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Пройти снова")
                    }

                    OutlinedButton(
                        onClick = onBackToMain,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("В меню")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}