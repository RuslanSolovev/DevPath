package com.example.devpath.domain.models

import com.google.errorprone.annotations.Keep

@Keep
data class UserProgress(
    val userId: String = "",
    val displayName: String = "",
    val completedLessons: List<String> = emptyList(),
    val completedPracticeTasks: List<String> = emptyList(),
    val quizResults: Map<String, Boolean> = emptyMap(),
    val favoriteInterviewQuestions: List<String> = emptyList(),
    val totalXP: Int = 0,
    val level: Int = 1,
    val generalTestHistory: List<GeneralTestResult> = emptyList(),
    // 🔥 НОВЫЕ ПОЛЯ ДЛЯ МИКРО-ПРИВЫЧЕК:
    val dailyStreak: Int = 0,                    // Ежедневный стрик
    val lastActivityDate: String = "",          // Последняя активность "2024-02-06"
    val weeklyGoals: Map<String, Boolean> = emptyMap(), // "monday_lessons" -> true
    val achievementsUnlocked: Set<String> = emptySet()   // ID разблокированных достижений
) {
    companion object {
        fun createEmpty(userId: String): UserProgress {
            return UserProgress(userId = userId)
        }
    }

    // Умные геттеры для микро-привычек
    fun getCurrentDailyGoal(): DailyGoal {
        return DailyGoal(
            target = 3, // 3 действия в день для формирования привычки
            completed = countTodayActions(),
            streak = dailyStreak
        )
    }

    private fun countTodayActions(): Int {
        // Считаем действия за сегодня из локальной базы
        return completedLessons.size + completedPracticeTasks.size + quizResults.size
    }
}

// Модель ежедневной цели
data class DailyGoal(
    val target: Int,
    val completed: Int,
    val streak: Int
) {
    val isCompleted: Boolean = completed >= target
    val progress: Float = completed.coerceAtMost(target).toFloat() / target
}

@Keep
data class GeneralTestResult(
    val timestamp: Long = System.currentTimeMillis(),
    val correctAnswers: Int = 0,
    val totalQuestions: Int = 0,
    val percentage: Int = 0
)