package com.example.devpath.ui

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.devpath.data.repository.FavoritesRepository
import com.example.devpath.data.repository.LessonRepository
import com.example.devpath.data.repository.PracticeRepository
import com.example.devpath.data.repository.QuizRepository
import com.example.devpath.ui.navigation.BottomNavigationScreen
import com.example.devpath.ui.navigation.NavigationHandler
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlin.random.Random
import com.example.devpath.ui.viewmodel.ProgressViewModel
import com.example.devpath.domain.models.GeneralTestResult
import androidx.navigation.compose.navigation
import com.example.devpath.data.repository.YdbRepository
import com.example.devpath.utils.SessionManager
import com.google.firebase.auth.FirebaseAuth

@Composable
fun DevPathNavGraph(
    navController: NavHostController = rememberNavController(),
    onNavigationVisibilityChanged: (Boolean) -> Unit = {}
) {

    val isLoggedIn = SessionManager.isLoggedIn()
    val startDestination = if (isLoggedIn) "dashboard" else "auth"

    val viewModel: ProgressViewModel = hiltViewModel()
    val progressRepo = viewModel.progressRepository

    // Состояние для управления возвратом на dashboard
    var shouldReturnToDashboard by remember { mutableStateOf(false) }

    // Состояние для отображения навигационных кнопок в DashboardScreen
    var showNavButtons by remember { mutableStateOf(true) }

    // Синхронизация избранного при запуске
    LaunchedEffect(Unit) {
        val userId = SessionManager.getUserId()
        if (userId != null) {
            val progress = progressRepo.loadProgress(userId)
            progress?.favoriteInterviewQuestions?.let { favoriteIds ->
                FavoritesRepository.syncWithRemote(favoriteIds)
            }
        }
    }

    // Обработчик возврата на dashboard
    LaunchedEffect(shouldReturnToDashboard) {
        if (shouldReturnToDashboard) {
            println("DEBUG: Возвращаемся на dashboard")
            navController.popBackStack("dashboard", false)
            shouldReturnToDashboard = false
        }
    }

    // Внутри DevPathNavGraph - используем DisposableEffect для правильной очистки
    DisposableEffect(navController) {
        // Создаём листенер
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            val route = destination.route ?: ""

            // ✅ Исправленные проверки маршрутов
            val shouldShow = when {
                // Скрываем навигацию внутри учебного процесса
                route.startsWith("lesson/") -> false
                route.startsWith("practice/") -> false
                route.startsWith("quiz/") -> false
                route.startsWith("interview/") -> false
                route.startsWith("tabs_main/") -> false  // ← КЛЮЧЕВОЕ: был "tabs/", а надо "tabs_main/"

                // Показываем на главных экранах учебника
                route == "dashboard" -> true
                route == "auth" -> true

                // По умолчанию показываем (для безопасности)
                else -> {
                    println("DEBUG: Неизвестный маршрут '$route', показываем навигацию")
                    true
                }
            }

            println("DEBUG: 🧭 Route: '$route' → showNavigation: $shouldShow")
            onNavigationVisibilityChanged(shouldShow)
        }

        // Добавляем листенер при входе в эффект
        navController.addOnDestinationChangedListener(listener)
        println("DEBUG: 📡 Listener added to inner navController")

        // ✅ onDispose вызывается автоматически при выходе из композиции
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
            println("DEBUG: 📡 Listener removed")
        }
    }

    // Глобальный обработчик навигации
    NavigationHandler(
        navController = navController,
        onBackToDashboard = {
            shouldReturnToDashboard = true
        }
    )

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("auth") {
            val ydbRepository = remember { YdbRepository() }
            AuthScreen(
                ydbRepository = ydbRepository,
                onSuccess = { userId ->
                    navController.navigate("dashboard") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }

        composable("dashboard") {
            DashboardScreen(
                onNavigateToTabs = { initialTab ->
                    navController.navigate("tabs/$initialTab")
                },
                onNavigateToPractice = {
                    navController.navigate("tabs/practice")
                },
                onNavigateToQuiz = {
                    navController.navigate("tabs/quiz")
                },
                onNavigateToInterview = {
                    navController.navigate("tabs/interview")
                },
                parentNavController = navController,
                showNavigationButtons = showNavButtons
            )
        }

        composable("profile") {
            val context = LocalContext.current
            val ydbRepository = remember { YdbRepository() }
            val currentUserId = remember {
                val prefs = context.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
                prefs.getString("user_id", "") ?: ""
            }
            ProfileScreen(
                ydbRepository = ydbRepository,
                currentUserId = currentUserId,
                navController = navController,
                onNavigateToTabs = {
                    navController.popBackStack("dashboard", false)
                }
            )
        }

        // Вложенная навигация для вкладок
        navigation(
            startDestination = "tabs_main/{initialTab}",
            route = "tabs/{initialTab}"
        ) {
            composable(
                "tabs_main/{initialTab}",
                arguments = listOf(navArgument("initialTab") { type = NavType.StringType })
            ) { backStackEntry ->
                val initialTab = backStackEntry.arguments?.getString("initialTab") ?: "learning"

                BottomNavigationScreen(
                    initialTab = initialTab,
                    onSignOut = {
                        Firebase.auth.signOut()
                        navController.navigate("auth") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    parentNavController = navController,
                    onNavigateBack = {
                        println("DEBUG: BottomNavigationScreen - запрос возврата на dashboard")
                        shouldReturnToDashboard = true
                    }
                )
            }

            // Уроки внутри вкладок
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
                    onBack = {
                        println("DEBUG: LessonScreen - запрос возврата")
                        if (!navController.popBackStack()) {
                            println("DEBUG: Нет предыдущего экрана, возвращаемся на dashboard")
                            shouldReturnToDashboard = true
                        }
                    },
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

            // Практические задания внутри вкладок
            composable(
                route = "practice/{taskId}",
                arguments = listOf(navArgument("taskId") { type = NavType.StringType })
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId") ?: "hello_world"
                val task = PracticeRepository.getTaskById(taskId)
                    ?: PracticeRepository.getPracticeTasks().first()

                PracticeTaskScreen(
                    task = task,
                    onBack = {
                        println("DEBUG: PracticeTaskScreen - запрос возврата")
                        if (!navController.popBackStack()) {
                            shouldReturnToDashboard = true
                        }
                    }
                )
            }

            // Вопросы тестов внутри вкладок
            composable(
                route = "quiz/question/{questionId}",
                arguments = listOf(navArgument("questionId") { type = NavType.StringType })
            ) { backStackEntry ->
                val questionId = backStackEntry.arguments?.getString("questionId") ?: "q1"
                val question = QuizRepository.getQuestionById(questionId)
                    ?: QuizRepository.getQuizQuestions().first()

                QuizQuestionScreen(
                    question = question,
                    onBack = {
                        println("DEBUG: QuizQuestionScreen - запрос возврата")
                        if (!navController.popBackStack()) {
                            shouldReturnToDashboard = true
                        }
                    }
                )
            }

            composable("quiz/general_test") {
                GeneralTestScreenContent(
                    navController = navController,
                    onBackToDashboard = { shouldReturnToDashboard = true }
                )
            }

            // Результаты теста внутри вкладок (обновленный маршрут с attemptId)
            composable(
                route = "quiz/test_results/{attemptId}",
                arguments = listOf(navArgument("attemptId") { type = NavType.LongType })
            ) { backStackEntry ->
                val attemptId = backStackEntry.arguments?.getLong("attemptId") ?: -1L

                TestResultsScreen(
                    attemptId = attemptId,
                    navController = navController,
                    onRetry = {
                        navController.navigate("quiz/general_test") {
                            popUpTo("quiz/test_results/{attemptId}") { inclusive = true }
                        }
                    },
                    onBackToMain = {
                        println("DEBUG: TestResultsScreen - возврат на dashboard")
                        navController.navigate("dashboard") {
                            popUpTo("tabs/{initialTab}") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onBack = {
                        println("DEBUG: TestResultsScreen - запрос возврата")
                        if (!navController.popBackStack()) {
                            shouldReturnToDashboard = true
                        }
                    }
                )
            }

            // Экран детального разбора теста
            composable(
                route = "quiz/test_detail/{attemptId}",
                arguments = listOf(navArgument("attemptId") { type = NavType.LongType })
            ) { backStackEntry ->
                val attemptId = backStackEntry.arguments?.getLong("attemptId") ?: -1L
                TestDetailScreen(
                    attemptId = attemptId,
                    onBack = {
                        if (!navController.popBackStack()) {
                            shouldReturnToDashboard = true
                        }
                    }
                )
            }
        }

        // Старые маршруты для обратной совместимости
        composable("tabs") {
            BottomNavigationScreen(
                initialTab = "learning",
                onSignOut = {
                    Firebase.auth.signOut()
                    navController.navigate("auth") {
                        popUpTo("tabs") { inclusive = true }
                    }
                },
                parentNavController = navController,
                onNavigateBack = {
                    println("DEBUG: Старый tabs - возврат на dashboard")
                    shouldReturnToDashboard = true
                }
            )
        }

        composable("lessons") {
            LessonListScreen(
                onLessonClick = { lessonId ->
                    navController.navigate("lesson/$lessonId")
                }
            )
        }
    }
}

@Composable
fun GeneralTestScreenContent(
    navController: NavHostController,
    onBackToDashboard: () -> Unit
) {
    val context = LocalContext.current
    val currentUserId = remember { SessionManager.getUserId() }

    val allQuestions = QuizRepository.getQuizQuestions()
    val randomQuestions = remember(allQuestions) {
        allQuestions.shuffled(Random(System.currentTimeMillis())).take(10)
    }

    val testViewModel: ProgressViewModel = hiltViewModel()
    var shouldNavigateToResults by remember { mutableStateOf(false) }
    var attemptId by remember { mutableStateOf<Long?>(null) }
    val coroutineScope = rememberCoroutineScope()

    BackHandler {
        if (!navController.popBackStack()) {
            onBackToDashboard()
        }
    }

    // Если пользователь не авторизован – показываем индикатор или сообщение
    if (currentUserId == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator()
                Text(
                    "Пожалуйста, войдите в аккаунт",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(onClick = { onBackToDashboard() }) {
                    Text("Вернуться на главную")
                }
            }
        }
        return
    }

    // Навигация к результатам после сохранения попытки
    if (shouldNavigateToResults && attemptId != null) {
        LaunchedEffect(Unit) {
            navController.navigate("quiz/test_results/${attemptId}") {
                popUpTo("quiz/general_test") { inclusive = true }
            }
        }
    }

    GeneralTestScreen(
        questions = randomQuestions,
        onTestComplete = { quizResult, userAnswers ->
            coroutineScope.launch {
                try {
                    // Сохраняем детальную попытку
                    val id = testViewModel.progressRepository.saveTestAttempt(
                        currentUserId,
                        randomQuestions,
                        userAnswers
                    )
                    attemptId = id

                    // Сохраняем результат в историю с attemptId
                    val testResult = GeneralTestResult(
                        correctAnswers = quizResult.correctAnswers,
                        totalQuestions = quizResult.totalQuestions,
                        percentage = if (quizResult.totalQuestions > 0)
                            (quizResult.correctAnswers * 100 / quizResult.totalQuestions)
                        else 0,
                        attemptId = id
                    )
                    testViewModel.progressRepository.saveGeneralTestResult(
                        currentUserId,
                        testResult
                    )

                    Log.d("GeneralTest", "✅ Тест сохранён: ${quizResult.correctAnswers}/${quizResult.totalQuestions}")
                    shouldNavigateToResults = true
                } catch (e: Exception) {
                    Log.e("GeneralTest", "❌ Ошибка сохранения теста: ${e.message}", e)
                    // Можно показать ошибку пользователю
                }
            }
        },
        onBack = {
            if (!navController.popBackStack()) {
                onBackToDashboard()
            }
        }
    )
}