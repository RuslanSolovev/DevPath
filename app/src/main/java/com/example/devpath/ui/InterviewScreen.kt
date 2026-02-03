package com.example.devpath.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.devpath.data.repository.FavoritesRepository
import com.example.devpath.data.repository.InterviewRepository
import com.example.devpath.data.repository.ProgressRepository
import com.example.devpath.domain.models.InterviewQuestion
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterviewScreen() {
    val navController = rememberNavController()
    val allQuestions = InterviewRepository.getInterviewQuestions()
    val favoriteQuestions by remember {
        derivedStateOf {
            FavoritesRepository.getFavoriteQuestions(allQuestions)
        }
    }
    val currentUser = Firebase.auth.currentUser
    val coroutineScope = rememberCoroutineScope()
    val progressRepo = remember { ProgressRepository() }

    var selectedTab by remember { mutableStateOf(InterviewTab.ALL) }

    NavHost(
        navController = navController,
        startDestination = "question_list"
    ) {
        composable("question_list") {
            Column(modifier = Modifier.fillMaxSize()) {
                // Вкладки
                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    InterviewTab.entries.forEach { tab ->
                        Tab(
                            text = { Text(tab.title) },
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab }
                        )
                    }
                }

                // Список вопросов
                when (selectedTab) {
                    InterviewTab.ALL -> {
                        QuestionList(
                            questions = allQuestions,
                            onItemClick = { questionId ->
                                navController.navigate("question/$questionId")
                            },
                            onToggleFavorite = { questionId ->
                                if (currentUser != null) {
                                    val isNowFavorite = !FavoritesRepository.isFavorite(questionId)
                                    FavoritesRepository.toggleFavorite(questionId)
                                    coroutineScope.launch {
                                        progressRepo.toggleFavoriteInterviewQuestion(
                                            currentUser.uid,
                                            questionId,
                                            isNowFavorite
                                        )
                                    }
                                } else {
                                    FavoritesRepository.toggleFavorite(questionId)
                                }
                            }
                        )
                    }
                    InterviewTab.FAVORITES -> {
                        if (favoriteQuestions.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Нет избранных вопросов\nНажмите ❤️ чтобы добавить",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        } else {
                            QuestionList(
                                questions = favoriteQuestions,
                                onItemClick = { questionId ->
                                    navController.navigate("question/$questionId")
                                },
                                onToggleFavorite = { questionId ->
                                    if (currentUser != null) {
                                        val isNowFavorite = !FavoritesRepository.isFavorite(questionId)
                                        FavoritesRepository.toggleFavorite(questionId)
                                        coroutineScope.launch {
                                            progressRepo.toggleFavoriteInterviewQuestion(
                                                currentUser.uid,
                                                questionId,
                                                isNowFavorite
                                            )
                                        }
                                    } else {
                                        FavoritesRepository.toggleFavorite(questionId)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        composable("question/{questionId}") { backStackEntry ->
            val questionId = backStackEntry.arguments?.getString("questionId") ?: "iq1"
            val question = InterviewRepository.getQuestionById(questionId)
                ?: InterviewRepository.getInterviewQuestions().first()

            InterviewQuestionScreen(
                question = question,
                onBack = { navController.popBackStack() },
                onToggleFavorite = { questionId ->
                    if (currentUser != null) {
                        val isNowFavorite = !FavoritesRepository.isFavorite(questionId)
                        FavoritesRepository.toggleFavorite(questionId)
                        coroutineScope.launch {
                            progressRepo.toggleFavoriteInterviewQuestion(
                                currentUser.uid,
                                questionId,
                                isNowFavorite
                            )
                        }
                    } else {
                        FavoritesRepository.toggleFavorite(questionId)
                    }
                }
            )
        }
    }
}

@Composable
fun QuestionList(
    questions: List<InterviewQuestion>,
    onItemClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(questions, key = { it.id }) { question ->
            InterviewQuestionItem(
                question = question,
                onClick = { onItemClick(question.id) },
                onToggleFavorite = { onToggleFavorite(question.id) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun InterviewQuestionItem(
    question: InterviewQuestion,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = question.question,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${question.category.capitalize()} • ${question.difficulty.capitalize()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(
                onClick = { onToggleFavorite() }
            ) {
                Icon(
                    imageVector = if (question.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (question.isFavorite) "Удалить из избранного" else "Добавить в избранное",
                    tint = if (question.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

enum class InterviewTab(val title: String) {
    ALL("Все"),
    FAVORITES("Избранное")
}

// Расширение для capitalize
private fun String.capitalize(): String {
    return replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}