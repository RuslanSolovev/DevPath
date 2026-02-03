package com.example.devpath.data.repository

import com.example.devpath.domain.models.UserProgress
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProgressRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    suspend fun saveProgress(progress: UserProgress) {
        db.collection("users")
            .document(progress.userId)
            .set(progress)
            .await()
    }

    suspend fun loadProgress(userId: String): UserProgress? {
        return try {
            val document = db.collection("users").document(userId).get().await()
            if (document.exists()) {
                val progress = document.toObject(UserProgress::class.java)
                // Добавь логирование для отладки
                println("DEBUG: Загружен прогресс для $userId: ${progress?.displayName}")
                return progress
            } else {
                println("DEBUG: Прогресс не найден для $userId")
                return null
            }
        } catch (e: Exception) {
            println("DEBUG: Ошибка загрузки прогресса: ${e.message}")
            return null
        }
    }

    suspend fun markLessonCompleted(userId: String, lessonId: String) {
        val currentProgress = loadProgress(userId) ?: UserProgress.createEmpty(userId)

        // Избегаем дубликатов
        val updatedLessons = if (lessonId !in currentProgress.completedLessons) {
            currentProgress.completedLessons + lessonId
        } else {
            currentProgress.completedLessons
        }

        val updatedProgress = currentProgress.copy(
            completedLessons = updatedLessons,
            totalXP = currentProgress.totalXP + 10
        )
        saveProgress(updatedProgress)
    }

    suspend fun markPracticeTaskCompleted(userId: String, taskId: String) {
        val currentProgress = loadProgress(userId) ?: UserProgress.createEmpty(userId)

        val updatedTasks = if (taskId !in currentProgress.completedPracticeTasks) {
            currentProgress.completedPracticeTasks + taskId
        } else {
            currentProgress.completedPracticeTasks
        }

        val updatedProgress = currentProgress.copy(
            completedPracticeTasks = updatedTasks,
            totalXP = currentProgress.totalXP + 20
        )
        saveProgress(updatedProgress)
    }

    suspend fun saveQuizResult(userId: String, questionId: String, isCorrect: Boolean) {
        val currentProgress = loadProgress(userId) ?: UserProgress.createEmpty(userId)
        val updatedQuizResults = currentProgress.quizResults.toMutableMap()
        updatedQuizResults[questionId] = isCorrect

        val xpBonus = if (isCorrect) 5 else 0
        val updatedProgress = currentProgress.copy(
            quizResults = updatedQuizResults,
            totalXP = currentProgress.totalXP + xpBonus
        )
        saveProgress(updatedProgress)
    }

    suspend fun toggleFavoriteInterviewQuestion(userId: String, questionId: String, isFavorite: Boolean) {
        val currentProgress = loadProgress(userId) ?: UserProgress.createEmpty(userId)
        val currentFavorites = currentProgress.favoriteInterviewQuestions.toMutableList()

        if (isFavorite) {
            if (questionId !in currentFavorites) {
                currentFavorites.add(questionId)
            }
        } else {
            currentFavorites.remove(questionId)
        }

        val updatedProgress = currentProgress.copy(
            favoriteInterviewQuestions = currentFavorites
        )
        saveProgress(updatedProgress)
    }
}