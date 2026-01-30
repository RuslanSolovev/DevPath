package com.example.devpath.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.devpath.data.repository.LessonRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun DevPathNavGraph() {
    val navController = rememberNavController()
    val currentUser = Firebase.auth.currentUser
    val startDestination = if (currentUser != null) "home" else "auth"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("auth") {
            AuthScreen(onSuccess = {
                navController.navigate("home") {
                    popUpTo("auth") { inclusive = true }
                }
            })
        }

        composable("home") {
            HomeScreen(
                onSignOut = {
                    Firebase.auth.signOut()
                    navController.navigate("auth") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onLessonsClick = { navController.navigate("lessons") }
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
            val lesson = LessonRepository.getLessonById(lessonId) ?: LessonRepository.getLessons().first()

            LessonScreen(
                lessonTitle = lesson.title,
                lessonContent = lesson.theory,
                onBack = { navController.popBackStack() }
            )
        }
    }
}