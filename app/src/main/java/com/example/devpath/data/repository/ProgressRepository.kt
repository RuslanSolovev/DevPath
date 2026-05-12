package com.example.devpath.data.repository

import android.util.Log
import com.example.devpath.data.local.AppDatabase
import com.example.devpath.data.local.entity.TestAttemptEntity
import com.example.devpath.data.local.entity.toDomain
import com.example.devpath.data.local.entity.toEntity
import com.example.devpath.domain.models.GeneralTestResult
import com.example.devpath.domain.models.QuizQuestion
import com.example.devpath.domain.models.UserProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressRepository @Inject constructor(
    private val localDb: AppDatabase
) {

    // ==================== СОХРАНЕНИЕ ====================

    suspend fun saveProgress(progress: UserProgress) {
        withContext(Dispatchers.IO) {
            try {
                val entity = progress.toEntity()
                localDb.userProgressDao().insertProgress(entity)
                println("DEBUG: ✅ Прогресс сохранён локально: userId=${progress.userId}, XP=${progress.totalXP}, уроков=${progress.completedLessons.size}")
            } catch (e: Exception) {
                println("DEBUG: ❌ Ошибка сохранения прогресса: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    // ==================== ЗАГРУЗКА ====================

    suspend fun loadProgress(userId: String): UserProgress? = withContext(Dispatchers.IO) {
        try {
            val localProgress = localDb.userProgressDao().getProgress(userId)
            if (localProgress != null) {
                println("DEBUG: 📂 Загружен локальный прогресс для $userId")
                return@withContext localProgress.toDomain()
            }

            // Создаём новый прогресс
            println("DEBUG: 🆕 Прогресс не найден, создаём новый для $userId")
            val initialProgress = UserProgress.createEmpty(userId)
            saveProgress(initialProgress)
            initialProgress
        } catch (e: Exception) {
            println("DEBUG: ❌ Ошибка загрузки прогресса: ${e.message}")
            UserProgress.createEmpty(userId)
        }
    }

    suspend fun loadLocalProgress(userId: String): UserProgress? = withContext(Dispatchers.IO) {
        try {
            localDb.userProgressDao().getProgress(userId)?.toDomain()
        } catch (e: Exception) {
            println("DEBUG: ❌ Ошибка загрузки локального прогресса: ${e.message}")
            null
        }
    }

    // ==================== УРОКИ ====================

    suspend fun markLessonCompleted(userId: String, lessonId: String): Boolean {
        return try {
            val currentProgress = loadProgress(userId) ?: UserProgress.createEmpty(userId)

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
            println("DEBUG: 📚 Урок $lessonId пройден, XP=${updatedProgress.totalXP}")
            true
        } catch (e: Exception) {
            println("DEBUG: ❌ Ошибка отметки урока: ${e.message}")
            false
        }
    }

    suspend fun isLessonCompleted(userId: String, lessonId: String): Boolean {
        return try {
            val progress = loadLocalProgress(userId)
            progress?.completedLessons?.contains(lessonId) ?: false
        } catch (e: Exception) {
            false
        }
    }

    // ==================== ПРАКТИКА ====================

    suspend fun markPracticeTaskCompleted(userId: String, taskId: String) {
        try {
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
            println("DEBUG: 🛠️ Задача $taskId выполнена, XP=${updatedProgress.totalXP}")
        } catch (e: Exception) {
            println("DEBUG: ❌ Ошибка отметки задачи: ${e.message}")
        }
    }

    // ==================== ТЕСТЫ ====================

    suspend fun saveQuizResult(userId: String, questionId: String, isCorrect: Boolean) {
        try {
            val currentProgress = loadProgress(userId) ?: UserProgress.createEmpty(userId)
            val updatedQuizResults = currentProgress.quizResults.toMutableMap()
            updatedQuizResults[questionId] = isCorrect

            val xpBonus = if (isCorrect) 5 else 0
            val updatedProgress = currentProgress.copy(
                quizResults = updatedQuizResults,
                totalXP = currentProgress.totalXP + xpBonus
            )
            saveProgress(updatedProgress)
        } catch (e: Exception) {
            println("DEBUG: ❌ Ошибка сохранения результата теста: ${e.message}")
        }
    }

    suspend fun saveGeneralTestResult(userId: String, result: GeneralTestResult) {
        try {
            val currentProgress = loadProgress(userId) ?: UserProgress.createEmpty(userId)

            val updatedHistory = (currentProgress.generalTestHistory + result)
                .sortedByDescending { it.timestamp }
                .take(10)

            val updatedProgress = currentProgress.copy(
                generalTestHistory = updatedHistory,
                totalXP = currentProgress.totalXP + (result.correctAnswers * 5)
            )
            saveProgress(updatedProgress)
            println("DEBUG: 📊 Общий тест сохранён: ${result.correctAnswers}/${result.totalQuestions}")
        } catch (e: Exception) {
            println("DEBUG: ❌ Ошибка сохранения общего теста: ${e.message}")
        }
    }

    fun getBestGeneralTestResult(history: List<GeneralTestResult>): GeneralTestResult? {
        return history.maxByOrNull { it.percentage }
    }

    // ==================== ПОПЫТКИ ТЕСТОВ ====================

    suspend fun saveTestAttempt(
        userId: String,
        questions: List<QuizQuestion>,
        userAnswers: Map<Int, Int>
    ): Long {
        val correctCount = userAnswers.count { (index, answer) ->
            questions.getOrNull(index)?.correctAnswerIndex == answer
        }

        val details = buildTestAttemptDetails(questions, userAnswers)
        val attempt = TestAttemptEntity(
            userId = userId,
            timestamp = System.currentTimeMillis(),
            totalQuestions = questions.size,
            correctAnswers = correctCount,
            detailsJson = details
        )
        val id = localDb.testAttemptDao().insertAttempt(attempt)
        println("DEBUG: 📝 Попытка теста сохранена: id=$id, правильно=$correctCount/${questions.size}")
        return id
    }

    suspend fun getTestAttempt(attemptId: Long): TestAttemptEntity? {
        return localDb.testAttemptDao().getAttemptById(attemptId)
    }

    suspend fun getLastTestAttempt(userId: String): TestAttemptEntity? {
        return localDb.testAttemptDao().getAttemptsByUserId(userId).firstOrNull()
    }

    suspend fun getUserTestAttempts(userId: String): List<TestAttemptEntity> {
        return localDb.testAttemptDao().getAttemptsByUserId(userId)
    }

    private fun buildTestAttemptDetails(questions: List<QuizQuestion>, userAnswers: Map<Int, Int>): String {
        val sb = StringBuilder("[")
        questions.forEachIndexed { idx, q ->
            val userAnswer = userAnswers[idx] ?: -1
            sb.append("""{"question":"${escapeJson(q.question)}","options":[${q.options.joinToString(",") { "\"${escapeJson(it)}\"" }}],"correct":${q.correctAnswerIndex},"userAnswer":$userAnswer,"explanation":"${escapeJson(q.explanation)}","topic":"${q.topic}"}""")
            if (idx < questions.size - 1) sb.append(",")
        }
        sb.append("]")
        return sb.toString()
    }

    private fun escapeJson(s: String): String {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
    }

    // ==================== ИЗБРАННОЕ ====================

    suspend fun toggleFavoriteInterviewQuestion(userId: String, questionId: String, isFavorite: Boolean) {
        try {
            android.util.Log.d("ENTITY_DEBUG", "toggleFavorite: userId=$userId, question=$questionId, isFavorite=$isFavorite")

            val currentProgress = loadProgress(userId) ?: UserProgress.createEmpty(userId)
            android.util.Log.d("ENTITY_DEBUG", "toggleFavorite: текущий список избранного = ${currentProgress.favoriteInterviewQuestions}")

            val currentFavorites = currentProgress.favoriteInterviewQuestions.toMutableList()

            if (isFavorite) {
                if (questionId !in currentFavorites) {
                    currentFavorites.add(questionId)
                    android.util.Log.d("ENTITY_DEBUG", "toggleFavorite: + добавлен $questionId")
                }
            } else {
                currentFavorites.remove(questionId)
                android.util.Log.d("ENTITY_DEBUG", "toggleFavorite: - удален $questionId")
            }

            val updatedProgress = currentProgress.copy(
                favoriteInterviewQuestions = currentFavorites
            )

            android.util.Log.d("ENTITY_DEBUG", "toggleFavorite: сохраняем список = ${updatedProgress.favoriteInterviewQuestions}")
            saveProgress(updatedProgress)
            android.util.Log.d("ENTITY_DEBUG", "toggleFavorite: ✅ сохранено успешно")
        } catch (e: Exception) {
            android.util.Log.e("ENTITY_DEBUG", "❌ Ошибка избранного: ${e.message}", e)
        }
    }

    // ==================== ДОСТИЖЕНИЯ ====================

    suspend fun checkAndUnlockAchievements(userId: String): Set<String> {
        val progress = loadProgress(userId) ?: return emptySet()
        val unlocked = progress.achievementsUnlocked.toMutableSet()
        var changed = false

        if (progress.completedLessons.isNotEmpty() && !unlocked.contains("first_step")) {
            unlocked.add("first_step")
            changed = true
        }

        if (progress.dailyStreak >= 3 && !unlocked.contains("streak_3")) {
            unlocked.add("streak_3")
            changed = true
        }

        if (progress.completedPracticeTasks.size >= 5 && !unlocked.contains("practicer")) {
            unlocked.add("practicer")
            changed = true
        }

        if (changed) {
            val updatedProgress = progress.copy(achievementsUnlocked = unlocked)
            saveProgress(updatedProgress)
        }

        return unlocked
    }

    // ==================== ЕЖЕДНЕВНАЯ АКТИВНОСТЬ ====================

    suspend fun trackDailyActivity(userId: String) {
        val progress = loadProgress(userId) ?: UserProgress.createEmpty(userId)
        val today = getTodayDateString()

        val newStreak = when {
            progress.lastActivityDate == today -> progress.dailyStreak
            progress.lastActivityDate == getYesterdayDateString() -> progress.dailyStreak + 1
            else -> 1
        }

        val updatedProgress = progress.copy(
            dailyStreak = newStreak,
            lastActivityDate = today
        )
        saveProgress(updatedProgress)
    }

    // ==================== ВСПОМОГАТЕЛЬНОЕ ====================

    private fun calculateLevel(totalXP: Int): Int {
        if (totalXP < 100) return 1
        if (totalXP < 250) return 2
        if (totalXP < 450) return 3
        if (totalXP < 700) return 4
        if (totalXP < 1000) return 5
        var xp = totalXP
        var level = 1
        var xpForNextLevel = 100
        while (xp >= xpForNextLevel) {
            xp -= xpForNextLevel
            level++
            xpForNextLevel += 50
        }
        return level
    }

    private fun getTodayDateString(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())
    }

    private fun getYesterdayDateString(): String {
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
        return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(calendar.time)
    }
}