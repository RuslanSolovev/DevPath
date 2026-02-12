// ui/InterviewSimulationScreen.kt
package com.example.devpath.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
                    "Анализ GigaChat",
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
                                Text("GigaChat анализирует ваш ответ...")
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

    // ✅ УБИРАЕМ SCAFFOLD, ИСПОЛЬЗУЕМ ЧИСТУЮ КОЛОНКУ!
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding() // Только для клавиатуры
    ) {
        // ✅ 1. ВЕРХНЯЯ ПАНЕЛЬ – КАСТОМНАЯ, ПРИЖАТА К ВЕРХУ
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            color = Color.Transparent,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                // Кнопка назад – полный размер 56x56, без padding
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .size(56.dp)
                        .padding(0.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = if (interviewCompleted) "Результаты собеседования" else "Симуляция собеседования",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 0.dp)
                )
            }
        }

        if (!interviewCompleted) {
            // ✅ 2. ОСНОВНОЙ КОНТЕНТ – С WEIGHT, СЖИМАЕТСЯ ПРИ КЛАВИАТУРЕ
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                val step = interviewSteps[currentStep]

                Spacer(modifier = Modifier.height(8.dp))

                // Прогресс
                LinearProgressIndicator(
                    progress = { (currentStep + 1f) / interviewSteps.size },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Информация о текущем шаге
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "Шаг ${currentStep + 1}/${interviewSteps.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = "Вопрос ${step.id}/6",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Вопрос
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Заголовок
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = step.id.toString(),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = step.title,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Вопрос
                        Text(
                            text = step.question,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 24.sp
                        )

                        // Подсказки
                        if (step.tips.isNotEmpty()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Lightbulb,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Подсказки:",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }

                                    step.tips.forEach { tip ->
                                        Row(
                                            verticalAlignment = Alignment.Top,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "•",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                            Text(
                                                text = tip,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Лимит времени
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Лимит времени: ${step.timeLimit / 60} мин",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Ответ пользователя
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Ваш ответ:",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = userAnswer,
                        onValueChange = { userAnswer = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "Введите ваш ответ здесь...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        },
                        maxLines = 8,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            cursorColor = MaterialTheme.colorScheme.primary
                        ),
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Кнопки действий
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Кнопка голосового ответа
                        OutlinedButton(
                            onClick = { isRecording = !isRecording },
                            enabled = !isLoading,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (isRecording)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.primary
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isRecording)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isRecording) "Остановить" else "Голосовой ответ",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        // Кнопка анализа
                        Button(
                            onClick = {
                                if (userAnswer.isNotBlank()) {
                                    viewModel.saveAnswer(currentStep, userAnswer)
                                    viewModel.analyzeAnswer(
                                        question = step.question,
                                        userAnswer = userAnswer,
                                        tips = step.tips
                                    )
                                    showAnalysisDialog = true
                                }
                            },
                            enabled = userAnswer.isNotBlank() && !isLoading,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Analytics,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Проверить ответ",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Советы
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "📝 Советы для успешного ответа:",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(
                                "Будьте конкретны и структурированы",
                                "Приводите примеры из реального опыта",
                                "Не бойтесь говорить о сложностях и как вы их преодолели",
                                "Задавайте уточняющие вопросы если нужно",
                                "Используйте кнопку 'Проверить ответ' для ИИ-анализа от GigaChat"
                            ).forEach { tip ->
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "•",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Text(
                                        text = tip,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }

            // ✅ 3. НИЖНЯЯ ПАНЕЛЬ – ПРИЖАТА К НИЗУ, БЕЗ ОТСТУПОВ!
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Кнопка назад
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
                        enabled = currentStep > 0 && !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Назад")
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Кнопка далее/завершить
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
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(
                                text = if (currentStep == interviewSteps.size - 1) "Завершить" else "Далее"
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = if (currentStep == interviewSteps.size - 1)
                                Icons.Default.Check
                            else
                                Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        } else {
            // ✅ ЭКРАН ЗАВЕРШЕНИЯ – БЕЗ ИЗМЕНЕНИЙ
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // ... код без изменений ...
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

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Ваши ответы сохранены",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        LazyColumn(
                            modifier = Modifier.heightIn(max = 300.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(interviewSteps) { step ->
                                val answer = answers[step.id - 1]
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            "Вопрос ${step.id}: ${step.title}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
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
                                        if (answer != null) Icons.Default.CheckCircle else Icons.Default.Info,
                                        contentDescription = null,
                                        tint = if (answer != null)
                                            Color(0xFF10B981)
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
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
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
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
                        enabled = answers.isNotEmpty(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Analytics,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Полный анализ собеседования")
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