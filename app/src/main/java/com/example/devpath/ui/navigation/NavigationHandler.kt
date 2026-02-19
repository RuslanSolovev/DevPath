package com.example.devpath.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController

@Composable
fun NavigationHandler(
    navController: NavHostController,
    onBackToDashboard: () -> Unit
) {
    val context = LocalContext.current
    var backPressedTime by remember { mutableStateOf(0L) }

    BackHandler {
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        val previousRoute = navController.previousBackStackEntry?.destination?.route

        println("DEBUG: NavigationHandler - текущий маршрут: $currentRoute")
        println("DEBUG: NavigationHandler - предыдущий маршрут: $previousRoute")

        when {
            // Если есть предыдущий экран в стеке - возвращаемся назад
            previousRoute != null -> {
                println("DEBUG: NavigationHandler - переход назад по стеку")
                navController.popBackStack()
            }

            // Если мы на экране tabs и не в корне - возвращаемся на dashboard
            currentRoute?.startsWith("tabs/") == true -> {
                println("DEBUG: NavigationHandler - возврат на dashboard с tabs")
                onBackToDashboard()
            }

            // Если мы на dashboard - сворачиваем приложение с защитой от случайного выхода
            currentRoute == "dashboard" -> {
                val currentTime = System.currentTimeMillis()
                if (currentTime - backPressedTime > 2000) {
                    backPressedTime = currentTime
                    android.widget.Toast.makeText(
                        context,
                        "Нажмите ещё раз для выхода",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                } else {
                    println("DEBUG: NavigationHandler - сворачиваем приложение")
                    (context as? android.app.Activity)?.moveTaskToBack(true)
                }
            }

            // Если мы на auth экране - выходим из приложения
            currentRoute == "auth" -> {
                println("DEBUG: NavigationHandler - выход из приложения с auth")
                (context as? android.app.Activity)?.finish()
            }

            // В остальных случаях - ничего не делаем
            else -> {
                println("DEBUG: NavigationHandler - нет действия для маршрута: $currentRoute")
            }
        }
    }
}