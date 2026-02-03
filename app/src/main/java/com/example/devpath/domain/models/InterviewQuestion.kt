package com.example.devpath.domain.models

import com.example.devpath.data.repository.FavoritesRepository

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
}