package com.example.devpath.data.repository

import com.example.devpath.domain.models.GeneralTestResult
import com.example.devpath.domain.models.UserProgress
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProgressRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    suspend fun saveProgress(progress: UserProgress) {
        try {
            db.collection("users")
                .document(progress.userId)
                .set(progress)
                .await()
            println("DEBUG: Прогресс сохранен для ${progress.userId}")
        } catch (e: Exception) {
            println("DEBUG: Ошибка сохранения прогресса: ${e.message}")
            throw e
        }
    }

    suspend fun loadProgress(userId: String): UserProgress? {
        return try {
            val document = db.collection("users").document(userId).get().await()
            if (document.exists()) {
                val progress = document.toObject(UserProgress::class.java)
                println("DEBUG: Загружен прогресс для $userId: ${progress?.completedLessons?.size} уроков")
                progress
            } else {
                println("DEBUG: Прогресс не найден для $userId, создаем новый")
                // Создаем начальный прогресс
                val initialProgress = UserProgress.createEmpty(userId)
                saveProgress(initialProgress)
                initialProgress
            }
        } catch (e: Exception) {
            println("DEBUG: Ошибка загрузки прогресса: ${e.message}")
            null
        }
    }

    suspend fun markLessonCompleted(userId: String, lessonId: String): Boolean {
        return try {
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
            println("DEBUG: Урок $lessonId отмечен как пройденный для $userId")
            true
        } catch (e: Exception) {
            println("DEBUG: Ошибка отметки урока: ${e.message}")
            false
        }
    }

    suspend fun isLessonCompleted(userId: String, lessonId: String): Boolean {
        return try {
            val progress = loadProgress(userId)
            progress?.completedLessons?.contains(lessonId) ?: false
        } catch (e: Exception) {
            println("DEBUG: Ошибка проверки урока: ${e.message}")
            false
        }
    }

    suspend fun saveGeneralTestResult(userId: String, result: GeneralTestResult) {
        val currentProgress = loadProgress(userId) ?: UserProgress.createEmpty(userId)

        // Ограничиваем историю 10 последними результатами
        val updatedHistory = (currentProgress.generalTestHistory + result)
            .sortedByDescending { it.timestamp }
            .take(10)

        val updatedProgress = currentProgress.copy(generalTestHistory = updatedHistory)
        saveProgress(updatedProgress)
    }

    fun getBestGeneralTestResult(history: List<GeneralTestResult>): GeneralTestResult? {
        return history.maxByOrNull { it.percentage }
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