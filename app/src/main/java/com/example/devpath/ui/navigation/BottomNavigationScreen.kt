package com.example.devpath.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.devpath.ui.LessonListScreen
import com.example.devpath.ui.PracticeScreen
import com.example.devpath.ui.QuizScreen
import com.example.devpath.ui.InterviewScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavigationScreen(
    onSignOut: () -> Unit,
    parentNavController: NavHostController
) {
    var currentTab by remember { mutableStateOf(BottomTab.LEARNING) }

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
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (currentTab) {
                BottomTab.LEARNING -> {
                    LessonListScreen(onLessonClick = { lessonId ->
                        parentNavController.navigate("lesson/$lessonId")
                    })
                }
                BottomTab.PRACTICE -> PracticeScreen()
                BottomTab.QUIZ -> QuizScreen()
                BottomTab.INTERVIEW -> InterviewScreen()
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