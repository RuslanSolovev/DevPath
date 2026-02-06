package com.example.devpath.data.repository

import com.example.devpath.data.local.AppDatabase
import com.example.devpath.data.local.entity.toDomain
import com.example.devpath.data.local.entity.toEntity
import com.example.devpath.domain.models.GeneralTestResult
import com.example.devpath.domain.models.UserProgress
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val localDb: AppDatabase
) {
    // Флаг для отключения Firebase (поставьте false для тестирования в авиарежиме)
    private val useFirebase = true

    // Для фоновой синхронизации
    private val syncScope = CoroutineScope(Dispatchers.IO)

    suspend fun saveProgress(progress: UserProgress) {
        try {
            // 🔄 Сохраняем в Firebase асинхронно в фоне
            if (useFirebase) {
                syncScope.launch {
                    try {
                        db.collection("users")
                            .document(progress.userId)
                            .set(progress)
                            .await()
                        println("DEBUG: Прогресс сохранен в Firestore для ${progress.userId}")
                    } catch (e: Exception) {
                        println("DEBUG: Ошибка сохранения в Firebase: ${e.message}")
                        // Игнорируем ошибки Firebase, главное - локальное сохранение
                    }
                }
            }

            // 🚀 Сначала мгновенно сохраняем локально
            localDb.userProgressDao().insertProgress(progress.toEntity())
            println("DEBUG: Прогресс сохранен локально для ${progress.userId}")

        } catch (e: Exception) {
            println("DEBUG: Ошибка сохранения прогресса: ${e.message}")
            // Всегда пытаемся сохранить локально
            try {
                localDb.userProgressDao().insertProgress(progress.toEntity())
                println("DEBUG: Прогресс сохранен только локально")
            } catch (localError: Exception) {
                println("DEBUG: Ошибка локального сохранения: ${localError.message}")
            }
        }
    }

    // 🚀 БЫСТРАЯ ЗАГРУЗКА: сначала локальные данные, потом синхронизация в фоне
    suspend fun loadProgress(userId: String): UserProgress? = withContext(Dispatchers.IO) {
        try {
            // 1. 🚀 МГНОВЕННО: загружаем локальные данные
            val localProgress = localDb.userProgressDao().getProgress(userId)

            // 2. 🔄 В ФОНЕ: запускаем синхронизацию с Firebase
            syncScope.launch {
                syncWithFirebase(userId)
            }

            // 3. Возвращаем локальные данные (если есть)
            if (localProgress != null) {
                println("DEBUG: Используем локальный прогресс для $userId")
                return@withContext localProgress.toDomain()
            }

            // 4. Если нет локальных данных, пробуем Firebase
            if (useFirebase) {
                try {
                    val document = db.collection("users").document(userId).get().await()
                    if (document.exists()) {
                        val progress = document.toObject(UserProgress::class.java)
                        if (progress != null) {
                            // Сохраняем локальную копию
                            localDb.userProgressDao().insertProgress(progress.toEntity())
                            println("DEBUG: Загружен прогресс из Firestore для $userId")
                            return@withContext progress
                        }
                    }
                } catch (e: Exception) {
                    println("DEBUG: Ошибка загрузки из Firebase: ${e.message}")
                }
            }

            // 5. Создаем новый прогресс
            println("DEBUG: Прогресс не найден, создаем новый")
            val initialProgress = UserProgress.createEmpty(userId)
            saveProgress(initialProgress)
            initialProgress

        } catch (e: Exception) {
            println("DEBUG: Ошибка загрузки прогресса: ${e.message}")
            // В случае ошибки возвращаем локальные данные
            val localProgress = localDb.userProgressDao().getProgress(userId)
            localProgress?.toDomain()
        }
    }

    // 🔄 Синхронизация с Firebase в фоне
    private suspend fun syncWithFirebase(userId: String) {
        if (!useFirebase) return

        try {
            val document = db.collection("users").document(userId).get().await()
            if (document.exists()) {
                val firebaseProgress = document.toObject(UserProgress::class.java)
                if (firebaseProgress != null) {
                    // Сохраняем свежие данные из Firebase локально
                    localDb.userProgressDao().insertProgress(firebaseProgress.toEntity())
                    println("DEBUG: Синхронизировано с Firebase для $userId")
                }
            }
        } catch (e: Exception) {
            println("DEBUG: Ошибка синхронизации с Firebase: ${e.message}")
        }
    }

    // 🚀 УЛЬТРА-БЫСТРАЯ ЗАГРУЗКА: только локальные данные (для мгновенного отображения UI)
    suspend fun loadLocalProgress(userId: String): UserProgress? = withContext(Dispatchers.IO) {
        try {
            localDb.userProgressDao().getProgress(userId)?.toDomain()
        } catch (e: Exception) {
            println("DEBUG: Ошибка загрузки локального прогресса: ${e.message}")
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
            val progress = loadLocalProgress(userId) // Используем быструю локальную загрузку
            progress?.completedLessons?.contains(lessonId) ?: false
        } catch (e: Exception) {
            println("DEBUG: Ошибка проверки урока: ${e.message}")
            false
        }
    }

    suspend fun saveGeneralTestResult(userId: String, result: GeneralTestResult) {
        val currentProgress = loadLocalProgress(userId) ?: UserProgress.createEmpty(userId)

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
        val currentProgress = loadLocalProgress(userId) ?: UserProgress.createEmpty(userId)

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
        val currentProgress = loadLocalProgress(userId) ?: UserProgress.createEmpty(userId)
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
        val currentProgress = loadLocalProgress(userId) ?: UserProgress.createEmpty(userId)
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