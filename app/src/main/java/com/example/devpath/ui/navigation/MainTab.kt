package com.example.devpath.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

enum class MainTab(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
) {
    DASHBOARD("Главная", Icons.Default.Home, "dashboard"),
    AI_CHAT("ИИ Помощник", Icons.Default.SmartToy, "ai_chat"),
    INTERVIEW("Собеседование", Icons.Default.WorkspacePremium, "interview_tab")
}