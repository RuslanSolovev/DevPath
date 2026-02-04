package com.example.devpath.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.devpath.domain.models.QuizQuestion
import com.example.devpath.domain.models.QuizResult
import com.example.devpath.domain.models.TopicResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralTestScreen(
    questions: List<QuizQuestion>,
    onTestComplete: (QuizResult) -> Unit,
    onBack: () -> Unit // Добавлен параметр onBack
) {
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var answers = remember { mutableStateMapOf<Int, Int>() }
    var isTestCompleted by remember { mutableStateOf(false) }

    if (isTestCompleted) {
        // Собираем результаты
        val correctAnswers = answers.count { (questionIndex, selectedAnswer) ->
            questions[questionIndex].correctAnswerIndex == selectedAnswer
        }

        // Анализ по темам
        val topicResults = mutableMapOf<String, TopicResult>()
        questions.forEachIndexed { index, question ->
            val topic = question.topic
            val isCorrect = answers[index] == question.correctAnswerIndex

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

        LaunchedEffect(Unit) {
            onTestComplete(result)
        }
        return
    }

    val currentQuestion = questions[currentQuestionIndex]
    var selectedOption by remember { mutableStateOf<Int?>(answers[currentQuestionIndex]) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Вопрос ${currentQuestionIndex + 1} из ${questions.size}")
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
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Вопрос
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = currentQuestion.question,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Варианты ответов
            currentQuestion.options.forEachIndexed { index, option ->
                val isSelected = selectedOption == index

                TestRadioButtonWithText(
                    text = option,
                    index = index,
                    isSelected = isSelected,
                    onClick = {
                        selectedOption = index
                        answers[currentQuestionIndex] = index
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
                        onClick = {
                            currentQuestionIndex--
                            selectedOption = answers[currentQuestionIndex]
                        },
                        enabled = currentQuestionIndex > 0
                    ) {
                        Text("Назад")
                    }
                } else {
                    Spacer(modifier = Modifier.width(88.dp))
                }

                if (currentQuestionIndex < questions.lastIndex) {
                    Button(
                        onClick = {
                            if (selectedOption != null) {
                                currentQuestionIndex++
                                selectedOption = answers[currentQuestionIndex]
                            }
                        },
                        enabled = selectedOption != null,
                        modifier = Modifier.width(120.dp)
                    ) {
                        Text("Далее")
                    }
                } else {
                    Button(
                        onClick = {
                            if (selectedOption != null) {
                                answers[currentQuestionIndex] = selectedOption!!
                                isTestCompleted = true
                            }
                        },
                        enabled = selectedOption != null,
                        modifier = Modifier.width(150.dp)
                    ) {
                        Text("Завершить тест")
                    }
                }
            }
        }
    }
}

@Composable
fun TestRadioButtonWithText(
    text: String,
    index: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val letters = listOf("A", "B", "C", "D")

    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }

    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = BorderStroke(
            width = 1.dp,
            color = borderColor
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(
                        if (isSelected)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    letters.getOrElse(index) { (index + 1).toString() },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            if (isSelected) {
                Icon(
                    Icons.Rounded.RadioButtonChecked,
                    contentDescription = "Выбрано",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}