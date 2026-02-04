package com.example.devpath.domain.models

import androidx.compose.ui.graphics.Color
import com.example.devpath.data.repository.FavoritesRepository
import com.example.devpath.ui.getCategoryColor
import com.example.devpath.ui.getDifficultyColor

data class InterviewQuestion(
    val id: String,
    val question: String,
    val answer: String,
    val category: String = "kotlin",
    val difficulty: String = "beginner"
) {
    // Динамическое свойство - не хранится в данных
    val isFavorite: Boolean
        get() = FavoritesRepository.isFavorite(id)



    // Отображаемое название сложности
    val displayDifficulty: String
        get() = when (difficulty.lowercase()) {
            "beginner" -> "Начальный"
            "intermediate" -> "Средний"
            "advanced" -> "Продвинутый"
            else -> "Начальный"
        }

    // Отображаемая категория с заглавной буквой
    val displayCategory: String
        get() = category.replaceFirstChar { it.uppercase() }
}