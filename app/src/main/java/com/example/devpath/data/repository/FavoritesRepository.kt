package com.example.devpath.data.repository

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

object FavoritesRepository {
    private var _favoriteQuestionIds = mutableStateOf(setOf<String>())
    val favoriteQuestionIds = _favoriteQuestionIds

    fun toggleFavorite(questionId: String) {
        val currentFavorites = _favoriteQuestionIds.value
        val newFavorites = if (questionId in currentFavorites) {
            currentFavorites - questionId
        } else {
            currentFavorites + questionId
        }
        _favoriteQuestionIds.value = newFavorites
    }

    fun isFavorite(questionId: String): Boolean {
        return questionId in _favoriteQuestionIds.value
    }

    fun getFavoriteQuestions(allQuestions: List<com.example.devpath.domain.models.InterviewQuestion>): List<com.example.devpath.domain.models.InterviewQuestion> {
        return allQuestions.filter { isFavorite(it.id) }
    }

    // НОВАЯ ФУНКЦИЯ: синхронизация с Firestore
    fun syncWithRemote(favoriteQuestionIds: List<String>) {
        _favoriteQuestionIds.value = favoriteQuestionIds.toSet()
    }
}