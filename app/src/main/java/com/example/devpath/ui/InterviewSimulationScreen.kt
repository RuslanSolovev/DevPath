package com.example.devpath.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.devpath.ui.viewmodel.InterviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterviewSimulationScreen(
    navController: NavHostController,
    viewModel: InterviewViewModel = hiltViewModel()
) {
    var currentStep by remember { mutableIntStateOf(0) }
    var isRecording by remember { mutableStateOf(false) }
    var userAnswer by remember { mutableStateOf("") }
    var interviewCompleted by remember { mutableStateOf(false) }
    var showAnalysisDialog by remember { mutableStateOf(false) }

    val answers by viewModel.answers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val analysisResult by viewModel.analysisResult.collectAsState()

    val interviewSteps = listOf(
        InterviewStep(
            id = 1,
            title = "Введение",
            question = "Расскажите о себе и своем опыте в разработке на Kotlin.",
            timeLimit = 180,
            tips = listOf(
                "Расскажите о вашем образовании и опыте",
                "Упомяните ключевые проекты",
                "Расскажите о ваших сильных сторонах",
                "Объясните почему выбрали Kotlin"
            )
        ),
        InterviewStep(
            id = 2,
            title = "Базовые концепции",
            question = "Объясните разницу между val и var, а также между class и data class в Kotlin.",
            timeLimit = 120,
            tips = listOf(
                "val - неизменяемая ссылка, var - изменяемая",
                "data class автоматически генерирует методы",
                "Приведите примеры использования"
            )
        ),
        InterviewStep(
            id = 3,
            title = "Корутины",
            question = "Что такое корутины и как они отличаются от потоков? Объясните разницу между launch и async.",
            timeLimit = 180,
            tips = listOf(
                "Корутины легковеснее потоков",
                "launch - для fire-and-forget операций",
                "async - для получения результата",
                "Объясните suspend функции"
            )
        ),
        InterviewStep(
            id = 4,
            title = "Архитектура",
            question = "Опишите ваш опыт работы с MVVM/MVI архитектурой в Android. Как вы реализуете ViewModel?",
            timeLimit = 150,
            tips = listOf(
                "Объясните паттерн Observer",
                "Расскажите про LiveData/StateFlow",
                "Упомяните lifecycle-aware компоненты"
            )
        ),
        InterviewStep(
            id = 5,
            title = "Практическая задача",
            question = "Напишите функцию, которая находит все пары чисел в массиве, сумма которых равна заданному числу. Оцените сложность алгоритма.",
            timeLimit = 300,
            tips = listOf(
                "Можно использовать HashMap для O(n) решения",
                "Обсудите trade-offs разных подходов",
                "Подумайте о граничных случаях"
            )
        ),
        InterviewStep(
            id = 6,
            title = "Вопросы к компании",
            question = "Есть ли у вас вопросы к нам о компании или процессе работы?",
            timeLimit = 120,
            tips = listOf(
                "Спросите о стеке технологий",
                "Узнайте о процессе разработки",
                "Спросите о возможностях роста",
                "Узнайте о корпоративной культуре"
            )
        )
    )

    // Загрузка сохраненных ответов
    LaunchedEffect(Unit) {
        viewModel.loadAnswers()
    }

    // Обработка ошибок
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            println("Interview error: $errorMessage")
        }
    }

    // Диалог анализа ответа
    if (showAnalysisDialog) {
        AlertDialog(
            onDismissRequest = {
                showAnalysisDialog = false
                viewModel.clearAnalysis()
            },
            title = {
                Text(
                    "Анализ GigaChat", // ← Изменено
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(4.dp)
                ) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("GigaChat анализирует ваш ответ...") // ← Изменено
                            }
                        }
                    } else {
                        Text(
                            text = analysisResult ?: "Нет данных для анализа",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showAnalysisDialog = false
                        viewModel.clearAnalysis()
                    }
                ) {
                    Text("Закрыть")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = {
                    Text(
                        if (interviewCompleted) "Результаты собеседования"
                        else "Симуляция собеседования"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        bottomBar = {
            if (!interviewCompleted) {
                BottomAppBar {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = {
                                if (currentStep > 0) {
                                    if (userAnswer.isNotBlank()) {
                                        viewModel.saveAnswer(currentStep, userAnswer)
                                    }
                                    currentStep--
                                    userAnswer = answers[currentStep] ?: ""
                                }
                            },
                            enabled = currentStep > 0 && !isLoading
                        ) {
                            Text("Назад")
                        }

                        Button(
                            onClick = {
                                if (userAnswer.isNotBlank()) {
                                    viewModel.saveAnswer(currentStep, userAnswer)
                                }

                                if (currentStep < interviewSteps.size - 1) {
                                    currentStep++
                                    userAnswer = answers[currentStep] ?: ""
                                } else {
                                    interviewCompleted = true
                                }
                            },
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    if (currentStep == interviewSteps.size - 1) "Завершить"
                                    else "Далее"
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (!interviewCompleted) {
                val step = interviewSteps[currentStep]

                // Прогресс
                LinearProgressIndicator(
                    progress = { (currentStep + 1f) / interviewSteps.size },
                    modifier = Modifier.fillMaxWidth()
                )

                // Информация о текущем шаге
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Шаг ${currentStep + 1}/${interviewSteps.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Вопрос ${step.id}/6",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Вопрос
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            step.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            step.question,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Подсказки
                        if (step.tips.isNotEmpty()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        "💡 Подсказки:",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    step.tips.forEach { tip ->
                                        Text(
                                            "• $tip",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Лимит времени: ${step.timeLimit / 60} мин",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Ответ пользователя
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        "Ваш ответ:",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = userAnswer,
                        onValueChange = { userAnswer = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Введите ваш ответ здесь...") },
                        maxLines = 10,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        enabled = !isLoading
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { isRecording = !isRecording },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRecording) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary
                            ),
                            enabled = !isLoading
                        ) {
                            Icon(
                                if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = if (isRecording) "Остановить запись" else "Начать запись"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isRecording) "Остановить" else "Голосовой ответ")
                        }

                        Button(
                            onClick = {
                                if (userAnswer.isNotBlank()) {
                                    // Сохраняем ответ
                                    viewModel.saveAnswer(currentStep, userAnswer)
                                    // Анализируем через GigaChat
                                    viewModel.analyzeAnswer(
                                        question = step.question,
                                        userAnswer = userAnswer,
                                        tips = step.tips
                                    )
                                    showAnalysisDialog = true
                                }
                            },
                            enabled = userAnswer.isNotBlank() && !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Analytics, contentDescription = "Анализ")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Проверить ответ")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Советы
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "📝 Советы для успешного ответа:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            "• Будьте конкретны и структурированы\n" +
                                    "• Приводите примеры из реального опыта\n" +
                                    "• Не бойтесь говорить о сложностях и как вы их преодолели\n" +
                                    "• Задавайте уточняющие вопросы если нужно\n" +
                                    "• Используйте кнопку 'Проверить ответ' для ИИ-анализа от GigaChat", // ← Изменено
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                // Экран завершения (без изменений, только текст про GigaChat)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Завершено",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Собеседование завершено!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Вы ответили на ${answers.size} из ${interviewSteps.size} вопросов. " +
                                "Это отличная практика перед реальным собеседованием!",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Статистика
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Ваши ответы сохранены",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            LazyColumn {
                                items(interviewSteps) { step ->
                                    val answer = answers[step.id - 1]
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                "Вопрос ${step.id}: ${step.title}",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                if (answer != null) {
                                                    "✓ Ответ записан (${answer.length} символов)"
                                                } else {
                                                    "✗ Без ответа"
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (answer != null)
                                                    Color(0xFF10B981)
                                                else
                                                    MaterialTheme.colorScheme.error
                                            )
                                        }
                                        Icon(
                                            if (answer != null) Icons.Default.Check else Icons.Default.Info,
                                            contentDescription = null,
                                            tint = if (answer != null)
                                                Color(0xFF10B981)
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    if (step.id < interviewSteps.size) {
                                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                currentStep = 0
                                userAnswer = ""
                                viewModel.clearAnswers()
                                viewModel.clearAnalysis()
                                interviewCompleted = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Пройти заново")
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.analyzeFullInterview(
                                    interviewSteps = interviewSteps,
                                    answers = answers
                                )
                                showAnalysisDialog = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = answers.isNotEmpty()
                        ) {
                            Text("Полный анализ собеседования от GigaChat") // ← Изменено
                        }

                        TextButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Вернуться на главную")
                        }
                    }
                }
            }
        }
    }
}