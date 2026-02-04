// com.example.devpath.domain.models.Lesson
package com.example.devpath.domain.models

data class Lesson(
    val id: String,
    val title: String,
    val description: String,
    val theory: String,
    val codeExample: String,
    val duration: Int = 15, // Дефолтное значение
    val difficulty: String = "beginner", // Дефолтное значение
    val isCompleted: Boolean = false, // Это поле теперь будет вычисляться из прогресса
    val order: Int = 0
) {
    companion object {
        // Можно добавить вспомогательные методы
        fun getDifficultyText(difficulty: String): String {
            return when (difficulty) {
                "beginner" -> "Начальный"
                "intermediate" -> "Средний"
                "advanced" -> "Продвинутый"
                else -> "Начальный"
            }
        }
    }
}