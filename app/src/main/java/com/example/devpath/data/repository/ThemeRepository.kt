package com.example.devpath.data.repository

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import com.example.devpath.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // Добавьте эту аннотацию
class ThemeRepository @Inject constructor(
    context: Context
) {
    private val preferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    private val _currentTheme = MutableStateFlow(loadThemeFromPreferences())
    val currentTheme: StateFlow<AppTheme> = _currentTheme.asStateFlow()

    fun setTheme(theme: AppTheme) {
        _currentTheme.value = theme
        saveThemeToPreferences(theme)
    }

    private fun loadThemeFromPreferences(): AppTheme {
        val savedTheme = preferences.getString("app_theme", "SYSTEM") ?: "SYSTEM"
        return AppTheme.valueOf(savedTheme)
    }

    private fun saveThemeToPreferences(theme: AppTheme) {
        preferences.edit().putString("app_theme", theme.name).apply()
    }
}

// CompositionLocal для доступа к репозиторию из Compose
val LocalThemeRepository = compositionLocalOf<ThemeRepository> { error("No ThemeRepository provided") }