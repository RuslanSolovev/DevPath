package com.example.devpath.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.devpath.ui.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavigationScreen(
    initialTab: String = "learning",
    onSignOut: () -> Unit,
    parentNavController: NavHostController
) {
    // Конвертируем строку в BottomTab
    val initialBottomTab = when (initialTab.lowercase()) {
        "practice" -> BottomTab.PRACTICE
        "quiz" -> BottomTab.QUIZ
        "interview" -> BottomTab.INTERVIEW
        else -> BottomTab.LEARNING
    }

    var currentTab by remember { mutableStateOf(initialBottomTab) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                BottomTab.entries.forEach { tab ->
                    NavigationBarItem(
                        icon = { Icon(imageVector = tab.icon, contentDescription = null) },
                        label = { Text(tab.title) },
                        selected = currentTab == tab,
                        onClick = {
                            currentTab = tab
                        }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
        ) {
            when (currentTab) {
                BottomTab.LEARNING -> {
                    LessonListScreen(onLessonClick = { lessonId ->
                        parentNavController.navigate("lesson/$lessonId")
                    })
                }
                BottomTab.PRACTICE -> PracticeScreen(parentNavController = parentNavController)
                BottomTab.QUIZ -> QuizScreen(parentNavController = parentNavController)
                BottomTab.INTERVIEW -> InterviewScreen(parentNavController = parentNavController)
            }
        }
    }
}

enum class BottomTab(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
) {
    LEARNING("Обучение", Icons.Default.MenuBook, "learning"),
    PRACTICE("Практика", Icons.Default.Code, "practice"),
    QUIZ("Тесты", Icons.Default.Quiz, "quiz"),
    INTERVIEW("Собеседование", Icons.Default.QuestionAnswer, "interview")
}