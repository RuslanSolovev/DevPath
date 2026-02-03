package com.example.devpath.domain.models

import androidx.annotation.Keep
import com.google.firebase.firestore.IgnoreExtraProperties

@Keep
@IgnoreExtraProperties
data class UserProgress(
    val userId: String = "",
    val displayName: String = "",
    val completedLessons: List<String> = emptyList(),
    val completedPracticeTasks: List<String> = emptyList(),
    val quizResults: Map<String, Boolean> = emptyMap(),
    val favoriteInterviewQuestions: List<String> = emptyList(),
    val totalXP: Int = 0,
    val level: Int = 1
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