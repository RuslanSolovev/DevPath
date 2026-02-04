// domain/models/UserProgress.kt
package com.example.devpath.domain.models

import com.google.errorprone.annotations.Keep

data class UserProgress(
    val userId: String = "",
    val displayName: String = "",
    val completedLessons: List<String> = emptyList(),
    val completedPracticeTasks: List<String> = emptyList(),
    val quizResults: Map<String, Boolean> = emptyMap(),
    val favoriteInterviewQuestions: List<String> = emptyList(),
    val totalXP: Int = 0,
    val level: Int = 1,
    val generalTestHistory: List<GeneralTestResult> = emptyList()
) {
    companion object {
        fun createEmpty(userId: String): UserProgress {
            return UserProgress(userId = userId)
        }
    }

    fun hasCompletedLesson(lessonId: String): Boolean = lessonId in completedLessons
    fun hasCompletedPracticeTask(taskId: String): Boolean = taskId in completedPracticeTasks
    fun isFavoriteInterviewQuestion(questionId: String): Boolean = questionId in favoriteInterviewQuestions
}


@Keep
data class GeneralTestResult(
    val timestamp: Long = System.currentTimeMillis(),
    val correctAnswers: Int = 0,
    val totalQuestions: Int = 0,
    val percentage: Int = 0
)