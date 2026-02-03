package com.example.devpath.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.devpath.data.repository.ProgressRepository
import com.example.devpath.domain.models.QuizQuestion
import com.example.devpath.ui.fffff.Green40
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizQuestionScreen(question: QuizQuestion, onBack: () -> Unit) {
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var isAnswered by remember { mutableStateOf(false) }
    var showExplanation by remember { mutableStateOf(false) }
    val currentUser = Firebase.auth.currentUser
    val coroutineScope = rememberCoroutineScope()
    val progressRepo = remember { ProgressRepository() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Вопрос") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Вопрос
            Text(
                text = question.question,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Варианты ответов
            question.options.forEachIndexed { index, option ->
                val isSelected = selectedOption == index
                val isCorrect = index == question.correctAnswerIndex
                val showResult = isAnswered

                RadioButtonWithText(
                    text = option,
                    isSelected = isSelected,
                    isCorrect = if (showResult) isCorrect else null,
                    onClick = {
                        if (!isAnswered) {
                            selectedOption = index
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            // Кнопка проверки
            if (!isAnswered && selectedOption != null) {
                // В QuizQuestionScreen.kt
                Button(
                    onClick = {
                        isAnswered = true
                        showExplanation = true

                        // Сохраняем результат
                        if (currentUser != null) {
                            coroutineScope.launch {
                                progressRepo.saveQuizResult(
                                    currentUser.uid,
                                    question.id,
                                    selectedOption == question.correctAnswerIndex
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Проверить ответ")
                }
            }

            // Объяснение
            if (showExplanation) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedOption == question.correctAnswerIndex)
                            Green40.copy(alpha = 0.1f)
                        else
                            MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (selectedOption == question.correctAnswerIndex) {
                                "✅ Правильно!"
                            } else {
                                "❌ Неправильно"
                            },
                            color = if (selectedOption == question.correctAnswerIndex) {
                                Green40
                            } else {
                                MaterialTheme.colorScheme.error
                            },
                            style = MaterialTheme.typography.titleSmall
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = question.explanation,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RadioButtonWithText(
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Цвет фона при показе результата
        val backgroundColor = when {
            isCorrect == true -> Green40.copy(alpha = 0.2f)
            isCorrect == false -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
            else -> MaterialTheme.colorScheme.surface
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .background(backgroundColor)
                .padding(8.dp)
        ) {
            Text(text = text, style = MaterialTheme.typography.bodyLarge)
        }

        // Радиокнопка
        RadioButton(
            selected = isSelected,
            onClick = null, // Обработка клика на весь Row
            enabled = isCorrect == null // Отключаем после ответа
        )
    }
}