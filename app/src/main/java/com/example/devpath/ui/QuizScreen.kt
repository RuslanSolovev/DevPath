package com.example.devpath.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.devpath.data.repository.QuizRepository
import com.example.devpath.domain.models.QuizQuestion
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen() {
    val navController = rememberNavController()
    val allQuestions = QuizRepository.getQuizQuestions()

    // Генерируем 10 случайных вопросов
    val randomQuestions = remember(allQuestions) {
        allQuestions.shuffled(Random.Default).take(10)
    }

    var selectedTab by remember { mutableStateOf(QuizTab.ALL_QUESTIONS) }

    NavHost(
        navController = navController,
        startDestination = "quiz_main"
    ) {
        composable("quiz_main") {
            Column(modifier = Modifier.fillMaxSize()) {
                // Табы
                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    QuizTab.entries.forEach { tab ->
                        Tab(
                            text = { Text(tab.title) },
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab }
                        )
                    }
                }

                // Контент
                when (selectedTab) {
                    QuizTab.ALL_QUESTIONS -> {
                        QuizQuestionList(
                            questions = allQuestions,
                            onItemClick = { questionId ->
                                navController.navigate("question/$questionId")
                            }
                        )
                    }
                    QuizTab.GENERAL_TEST -> {
                        GeneralTestCard(
                            onStartTest = {
                                navController.navigate("general_test")
                            }
                        )
                    }
                }
            }
        }

        // Отдельный вопрос
        composable("question/{questionId}") { backStackEntry ->
            val questionId = backStackEntry.arguments?.getString("questionId") ?: "q1"
            val question = QuizRepository.getQuestionById(questionId)
                ?: QuizRepository.getQuizQuestions().first()

            QuizQuestionScreen(
                question = question,
                onBack = { navController.popBackStack() }
            )
        }

        // Общий тест
        composable("general_test") {
            GeneralTestScreen(
                questions = randomQuestions,
                onTestComplete = { results ->
                    navController.navigate("test_results/${results.correctAnswers}/${results.totalQuestions}") {
                        popUpTo("quiz_main") { inclusive = false }
                    }
                }
            )
        }

        // Результаты теста
        composable("test_results/{correct}/{total}") { backStackEntry ->
            val correct = backStackEntry.arguments?.getString("correct")?.toIntOrNull() ?: 0
            val total = backStackEntry.arguments?.getString("total")?.toIntOrNull() ?: 10

            TestResultsScreen(
                correctAnswers = correct,
                totalQuestions = total,
                onRetry = {
                    navController.popBackStack("quiz_main", false)
                },
                onBackToMain = {
                    navController.popBackStack("quiz_main", true)
                }
            )
        }
    }
}

enum class QuizTab(val title: String) {
    ALL_QUESTIONS("Все вопросы"),
    GENERAL_TEST("Общий тест")
}

@Composable
private fun QuizQuestionList(
    questions: List<QuizQuestion>,
    onItemClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(questions, key = { it.id }) { question ->
            QuizQuestionItem(
                question = question,
                onClick = { onItemClick(question.id) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun GeneralTestCard(onStartTest: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        onClick = onStartTest
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Проверьте свои знания!",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "10 случайных вопросов из всех тем",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onStartTest) {
                Text("Начать тест")
            }
        }
    }
}

@Composable
private fun QuizQuestionItem(
    question: QuizQuestion,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.QuestionAnswer,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = question.question,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}