package com.example.devpath.ui

import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.devpath.data.repository.*
import com.example.devpath.ui.navigation.BottomNavigationScreen
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun DevPathNavGraph() {
    val navController = rememberNavController()
    val currentUser = Firebase.auth.currentUser
    val startDestination = if (currentUser != null) "dashboard" else "auth"

    // Синхронизация избранного при запуске
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            val progressRepo = ProgressRepository()
            val progress = progressRepo.loadProgress(currentUser.uid)
            progress?.favoriteInterviewQuestions?.let { favoriteIds ->
                FavoritesRepository.syncWithRemote(favoriteIds)
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("auth") {
            AuthScreen(onSuccess = {
                navController.navigate("dashboard") {
                    popUpTo("auth") { inclusive = true }
                }
            })
        }

        composable("dashboard") {
            DashboardScreen(
                onSignOut = {
                    Firebase.auth.signOut()
                    navController.navigate("auth") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                },
                onNavigateToTabs = {
                    navController.navigate("tabs")
                },
                onNavigateToProfile = {
                    navController.navigate("profile")
                },
                onNavigateToPractice = {
                    navController.navigate("tabs")
                },
                onNavigateToQuiz = {
                    navController.navigate("tabs")
                },
                onNavigateToInterview = {
                    navController.navigate("tabs")
                }
            )
        }

        composable("profile") {
            ProfileScreen(
                navController = navController,
                onNavigateToTabs = {
                    navController.popBackStack("dashboard", false)
                }
            )
        }

        composable("tabs") {
            BottomNavigationScreen(
                onSignOut = {
                    Firebase.auth.signOut()
                    navController.navigate("auth") {
                        popUpTo("tabs") { inclusive = true }
                    }
                },
                parentNavController = navController
            )
        }

        composable("lessons") {
            LessonListScreen(onLessonClick = { lessonId ->
                navController.navigate("lesson/$lessonId")
            })
        }

        composable(
            route = "lesson/{lessonId}",
            arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: "kotlin_basics"
            val lesson = LessonRepository.getLessonById(lessonId)
                ?: LessonRepository.getLessons().first()

            LessonScreen(
                lessonTitle = lesson.title,
                lessonContent = lesson.theory,
                lessonId = lessonId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "practice/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: "hello_world"
            val task = PracticeRepository.getTaskById(taskId)
                ?: PracticeRepository.getPracticeTasks().first()

            PracticeTaskScreen(
                task = task,
                onBack = { navController.popBackStack() }
            )
        }

        // МАРШРУТЫ ДЛЯ ТЕСТОВ
        composable(
            route = "quiz/question/{questionId}",
            arguments = listOf(navArgument("questionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val questionId = backStackEntry.arguments?.getString("questionId") ?: "q1"
            val question = QuizRepository.getQuestionById(questionId)
                ?: QuizRepository.getQuizQuestions().first()

            QuizQuestionScreen(
                question = question,
                onBack = { navController.popBackStack() }
            )
        }

        composable("quiz/general_test") {
            val allQuestions = QuizRepository.getQuizQuestions()
            val randomQuestions = remember(allQuestions) {
                allQuestions.shuffled(Random(System.currentTimeMillis())).take(10)
            }

            GeneralTestScreenWithResultSaver(
                questions = randomQuestions,
                currentUserId = currentUser?.uid,
                onTestComplete = { quizResult ->
                    // Переходим на экран результатов
                    navController.navigate("quiz/test_results/${quizResult.correctAnswers}/${quizResult.totalQuestions}") {
                        popUpTo("quiz/general_test") { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "quiz/test_results/{correct}/{total}",
            arguments = listOf(
                navArgument("correct") { type = NavType.StringType },
                navArgument("total") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val correct = backStackEntry.arguments?.getString("correct")?.toIntOrNull() ?: 0
            val total = backStackEntry.arguments?.getString("total")?.toIntOrNull() ?: 10

            TestResultsScreen(
                correctAnswers = correct,
                totalQuestions = total,
                onRetry = {
                    navController.navigate("quiz/general_test") {
                        popUpTo("quiz/test_results/{correct}/{total}") { inclusive = true }
                    }
                },
                onBackToMain = {
                    // Возвращаемся на экран с вкладками
                    navController.navigate("tabs") {
                        popUpTo("quiz/test_results/{correct}/{total}") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun GeneralTestScreenWithResultSaver(
    questions: List<com.example.devpath.domain.models.QuizQuestion>,
    currentUserId: String?,
    onTestComplete: (com.example.devpath.domain.models.QuizResult) -> Unit,
    onBack: () -> Unit
) {
    val progressRepo = remember { ProgressRepository() }
    val coroutineScope = rememberCoroutineScope()

    GeneralTestScreen(
        questions = questions,
        onTestComplete = { quizResult ->
            // Сохраняем результат теста асинхронно
            if (currentUserId != null) {
                val testResult = com.example.devpath.domain.models.GeneralTestResult(
                    correctAnswers = quizResult.correctAnswers,
                    totalQuestions = quizResult.totalQuestions,
                    percentage = (quizResult.correctAnswers * 100 / quizResult.totalQuestions)
                )

                // Запускаем корутину для сохранения
                coroutineScope.launch {
                    progressRepo.saveGeneralTestResult(currentUserId, testResult)
                }
            }

            // Передаем результат дальше
            onTestComplete(quizResult)
        },
        onBack = onBack
    )
}