package com.example.devpath.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
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
import androidx.navigation.NavHostController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.devpath.data.local.entity.TestAttemptEntity
import com.example.devpath.ui.fffff.Green40
import com.example.devpath.ui.viewmodel.ProgressViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestResultsScreen(
    attemptId: Long,
    navController: NavHostController,
    onRetry: () -> Unit,
    onBackToMain: () -> Unit,
    onBack: () -> Unit
) {
    val viewModel: ProgressViewModel = hiltViewModel()
    var attempt by remember { mutableStateOf<TestAttemptEntity?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Загружаем попытку теста
    LaunchedEffect(attemptId) {
        if (attemptId != -1L) {
            attempt = viewModel.progressRepository.getTestAttempt(attemptId)
        }
        isLoading = false
    }

    val correctAnswers = attempt?.correctAnswers ?: 0
    val totalQuestions = attempt?.totalQuestions ?: 0
    val percentage = if (totalQuestions > 0) (correctAnswers.toFloat() / totalQuestions) * 100 else 0f
    val isPassed = percentage >= 70

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Результаты теста") },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                item {
                    // Иконка результата
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(MaterialTheme.shapes.extraLarge)
                            .background(
                                if (isPassed) Green40.copy(alpha = 0.1f)
                                else MaterialTheme.colorScheme.errorContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPassed) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (isPassed) Green40 else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

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
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isPassed)
                                Green40.copy(alpha = 0.05f)
                            else
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                if (isPassed) Icons.Rounded.Celebration else Icons.Rounded.School,
                                contentDescription = null,
                                tint = if (isPassed) Green40 else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(32.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = if (isPassed) {
                                    "Отличный результат! 🎉\nВы хорошо знаете материал."
                                } else {
                                    "Нужно повторить! 📚\nНе расстраивайтесь, практика решает всё."
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }

                // Кнопки
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Первый ряд кнопок
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = onRetry,
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.large
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.Refresh,
                                        contentDescription = "Повторить",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text("Пройти снова")
                                }
                            }

                            OutlinedButton(
                                onClick = onBackToMain,
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.large
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.Home,
                                        contentDescription = "Главная",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text("В меню")
                                }
                            }
                        }

                        // Кнопка "Показать разбор" (только если есть детальная информация)
                        if (attemptId != -1L && !isLoading && attempt != null) {
                            OutlinedButton(
                                onClick = {
                                    navController.navigate("quiz/test_detail/$attemptId")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.large
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.ListAlt,
                                        contentDescription = "Разбор",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text("Показать разбор")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}