package com.example.devpath.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.google.firebase.auth.FirebaseAuth

@Composable
fun DevPathNavGraph(
    navController: NavHostController = rememberNavController(),
    onNavigationVisibilityChanged: (Boolean) -> Unit = {}
) {

    val currentUser = Firebase.auth.currentUser
    val startDestination = if (currentUser != null) "dashboard" else "auth"

    val viewModel: ProgressViewModel = hiltViewModel()
    val progressRepo = viewModel.progressRepository

    // Состояние для управления возвратом на dashboard
    var shouldReturnToDashboard by remember { mutableStateOf(false) }

    // Состояние для отображения навигационных кнопок в DashboardScreen
    var showNavButtons by remember { mutableStateOf(true) }

    // Синхронизация избранного при запуске
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            val progress = progressRepo.loadProgress(currentUser.uid)
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

    LaunchedEffect(navController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val route = destination.route ?: ""
            val shouldShow = when {
                route.startsWith("tabs/") -> false
                route.startsWith("lesson/") -> false
                route.startsWith("practice/") -> false
                route.startsWith("quiz/question/") -> false
                route.startsWith("quiz/general_test") -> false
                route.startsWith("quiz/test_results/") -> false
                route.startsWith("quiz/test_detail/") -> false
                else -> true
            }
            onNavigationVisibilityChanged(shouldShow)
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
            AuthScreen(
                onSuccess = {
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
            ProfileScreen(
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
    // Состояние для хранения ID пользователя
    var currentUserId by remember { mutableStateOf<String?>(null) }
    val auth = Firebase.auth

    // Подписываемся на изменения аутентификации
    DisposableEffect(Unit) {
        currentUserId = auth.currentUser?.uid
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUserId = auth.currentUser?.uid
        }
        auth.addAuthStateListener(listener)
        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

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

    // Если пользователь ещё не загружен – показываем индикатор
    if (currentUserId == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
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
                // Сохраняем детальную попытку
                val id = testViewModel.progressRepository.saveTestAttempt(
                    currentUserId!!,
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
                testViewModel.progressRepository.saveGeneralTestResult(currentUserId!!, testResult)

                shouldNavigateToResults = true
            }
        },
        onBack = {
            if (!navController.popBackStack()) {
                onBackToDashboard()
            }
        }
    )
}