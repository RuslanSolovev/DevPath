package com.example.devpath.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.devpath.data.repository.FavoritesRepository
import com.example.devpath.data.repository.LessonRepository
import com.example.devpath.data.repository.PracticeRepository
import com.example.devpath.data.repository.ProgressRepository
import com.example.devpath.ui.navigation.BottomNavigationScreen
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

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
                // ДОБАВЬ ЭТИ ОБРАБОТЧИКИ:
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
                    // Возвращаемся на dashboard, а не напрямую в tabs
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

        // В DevPathNavGraph.kt
        composable(
            route = "lesson/{lessonId}",
            arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: "kotlin_basics"
            val lesson =
                LessonRepository.getLessonById(lessonId) ?: LessonRepository.getLessons().first()

            LessonScreen(
                lessonTitle = lesson.title,
                lessonContent = lesson.theory,
                lessonId = lessonId, // ← ПЕРЕДАЁМ ID
                onBack = { navController.popBackStack() }
            )
        }

        // В DevPathNavGraph.kt
        composable(
            route = "practice/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: "hello_world"
            val task =
                PracticeRepository.getTaskById(taskId) ?: PracticeRepository.getPracticeTasks()
                    .first()

            PracticeTaskScreen(
                task = task,
                onBack = { navController.popBackStack() }
            )
        }
    }

}
