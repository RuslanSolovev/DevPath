package com.example.devpath.ui

import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.devpath.data.repository.FavoritesRepository
import com.example.devpath.data.repository.LessonRepository
import com.example.devpath.data.repository.PracticeRepository
import com.example.devpath.data.repository.QuizRepository
import com.example.devpath.ui.navigation.BottomNavigationScreen
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlin.random.Random
import com.example.devpath.ui.viewmodel.ProgressViewModel

@Composable
fun DevPathNavGraph() {
    val navController = rememberNavController()
    val currentUser = Firebase.auth.currentUser
    val startDestination = if (currentUser != null) "dashboard" else "auth"

    // ✅ ИСПРАВЛЯЕМ: Используем hiltViewModel() вместо viewModel()
    val viewModel: ProgressViewModel = hiltViewModel()
    val progressRepo = viewModel.progressRepository

    // Синхронизация избранного при запуске
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            // ✅ ИСПОЛЬЗУЕМ УЖЕ ПОЛУЧЕННЫЙ РЕПОЗИТОРИЙ
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
                    // Переход к вкладкам на экране tabs
                    navController.navigate("tabs")
                },
                onNavigateToQuiz = {
                    // Переход к вкладкам на экране tabs
                    navController.navigate("tabs")
                },
                onNavigateToInterview = {
                    // Переход к вкладкам на экране tabs
                    navController.navigate("tabs")
                },
                parentNavController = navController // Передаем навигатор для вкладок
            )
        }

        composable("profile") {
            ProfileScreen(
                navController = navController,
                onNavigateToTabs = {
                    // Возврат на главную вкладку dashboard
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

        // Экран уроков (можно открыть из вкладок или с главной)
        composable("lessons") {
            LessonListScreen(onLessonClick = { lessonId ->
                navController.navigate("lesson/$lessonId")
            })
        }

        // Экран урока
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
                onBack = { navController.popBackStack() },
                onNavigateToPractice = { taskId ->
                    navController.navigate("practice/$taskId")
                },
                onNavigateToQuiz = { questionId ->
                    navController.navigate("quiz/question/$questionId")
                },
                onNavigateToGeneralTest = {
                    navController.navigate("quiz/general_test")
                }
            )
        }

        // Экран практических заданий
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

        // Экран отдельного вопроса теста
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

        // Общий тест
        composable("quiz/general_test") {
            val allQuestions = QuizRepository.getQuizQuestions()
            val randomQuestions = remember(allQuestions) {
                allQuestions.shuffled(Random(System.currentTimeMillis())).take(10)
            }

            // ✅ ИСПРАВЛЯЕМ: Используем hiltViewModel() здесь тоже
            val testViewModel: ProgressViewModel = hiltViewModel()

            GeneralTestScreenWithResultSaver(
                viewModel = testViewModel,
                questions = randomQuestions,
                currentUserId = currentUser?.uid,
                onTestComplete = { quizResult ->
                    navController.navigate("quiz/test_results/${quizResult.correctAnswers}/${quizResult.totalQuestions}") {
                        popUpTo("quiz/general_test") { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // Результаты теста
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
                    // Возвращаемся на главную вкладку dashboard
                    navController.navigate("dashboard") {
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
    viewModel: ProgressViewModel,
    questions: List<com.example.devpath.domain.models.QuizQuestion>,
    currentUserId: String?,
    onTestComplete: (com.example.devpath.domain.models.QuizResult) -> Unit,
    onBack: () -> Unit
) {
    val progressRepo = viewModel.progressRepository
    val coroutineScope = rememberCoroutineScope()

    GeneralTestScreen(
        questions = questions,
        onTestComplete = { quizResult ->
            if (currentUserId != null) {
                val testResult = com.example.devpath.domain.models.GeneralTestResult(
                    correctAnswers = quizResult.correctAnswers,
                    totalQuestions = quizResult.totalQuestions,
                    percentage = (quizResult.correctAnswers * 100 / quizResult.totalQuestions)
                )

                coroutineScope.launch {
                    progressRepo.saveGeneralTestResult(currentUserId, testResult)
                }
            }
            onTestComplete(quizResult)
        },
        onBack = onBack
    )
}