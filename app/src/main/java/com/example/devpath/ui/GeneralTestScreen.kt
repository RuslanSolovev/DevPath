package com.example.devpath.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.devpath.domain.models.QuizQuestion
import com.example.devpath.domain.models.QuizResult
import com.example.devpath.domain.models.TopicResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralTestScreen(
    questions: List<QuizQuestion>,
    onTestComplete: (QuizResult) -> Unit
) {
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var answers = remember { mutableStateOf(mutableMapOf<Int, Int>()) }
    var isTestCompleted by remember { mutableStateOf(false) }

    if (isTestCompleted) {
        // Собираем результаты
        val correctAnswers = answers.value.count { (questionIndex, selectedAnswer) ->
            questions[questionIndex].correctAnswerIndex == selectedAnswer
        }

        // Анализ по темам
        val topicResults = mutableMapOf<String, TopicResult>()
        questions.forEachIndexed { index, question ->
            val topic = question.topic
            val isCorrect = answers.value[index] == question.correctAnswerIndex

            // ПРАВИЛЬНЫЙ КОД:
            val currentTopicResult = topicResults[topic] ?: TopicResult(topic, 0, 0)
            topicResults[topic] = currentTopicResult.copy(
                totalQuestions = currentTopicResult.totalQuestions + 1,
                correctAnswers = if (isCorrect) {
                    currentTopicResult.correctAnswers + 1
                } else {
                    currentTopicResult.correctAnswers
                }
            )
        }

        val result = QuizResult(
            totalQuestions = questions.size,
            correctAnswers = correctAnswers,
            topicResults = topicResults.toMap()
        )

        onTestComplete(result)
        return
    }

    val currentQuestion = questions[currentQuestionIndex]
    var selectedOption by remember { mutableStateOf<Int?>(answers.value[currentQuestionIndex]) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Вопрос ${currentQuestionIndex + 1} из ${questions.size}")
                },
                navigationIcon = {
                    IconButton(onClick = { /* Назад нельзя в тесте */ }) {
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
                text = currentQuestion.question,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Варианты ответов
            currentQuestion.options.forEachIndexed { index, option ->
                val isSelected = selectedOption == index

                RadioButtonWithText(
                    text = option,
                    isSelected = isSelected,
                    isCorrect = null, // Не показываем результат пока не завершен тест
                    onClick = {
                        selectedOption = index
                        answers.value[currentQuestionIndex] = index
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            // Кнопки навигации
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentQuestionIndex > 0) {
                    OutlinedButton(
                        onClick = { currentQuestionIndex-- },
                        enabled = currentQuestionIndex > 0
                    ) {
                        Text("Назад")
                    }
                } else {
                    Spacer(modifier = Modifier.width(88.dp)) // Заполнитель для выравнивания
                }

                if (currentQuestionIndex < questions.lastIndex) {
                    Button(
                        onClick = {
                            if (selectedOption != null) {
                                currentQuestionIndex++
                                // Сохраняем ответ при переходе
                                answers.value[currentQuestionIndex - 1] = selectedOption!!
                            }
                        },
                        enabled = selectedOption != null
                    ) {
                        Text("Далее")
                    }
                } else {
                    Button(
                        onClick = {
                            if (selectedOption != null) {
                                answers.value[currentQuestionIndex] = selectedOption!!
                                isTestCompleted = true
                            }
                        },
                        enabled = selectedOption != null
                    ) {
                        Text("Завершить тест")
                    }
                }
            }
        }
    }
}