package com.example.devpath.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.devpath.domain.models.GeneralTestResult
import com.example.devpath.domain.models.UserProgress
import com.google.gson.Gson

@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey val userId: String,
    val displayName: String,
    val completedLessons: String,
    val completedPracticeTasks: String,
    val quizResults: String,
    val favoriteInterviewQuestions: String,
    val totalXP: Int,
    val level: Int,
    val generalTestHistory: String
)

fun UserProgress.toEntity(): UserProgressEntity {
    val gson = Gson()
    return UserProgressEntity(
        userId = userId,
        displayName = displayName,
        completedLessons = this.completedLessons.joinToString(","),
        completedPracticeTasks = this.completedPracticeTasks.joinToString(","),
        quizResults = this.quizResults.entries.joinToString(",") { "${it.key}:${it.value}" },
        favoriteInterviewQuestions = this.favoriteInterviewQuestions.joinToString(","),
        totalXP = totalXP,
        level = level,
        generalTestHistory = gson.toJson(this.generalTestHistory)
    )
}

fun UserProgressEntity.toDomain(): UserProgress {
    val gson = Gson()
    return UserProgress(
        userId = userId,
        displayName = displayName,
        completedLessons = if (completedLessons.isEmpty()) emptyList() else completedLessons.split(","),
        completedPracticeTasks = if (completedPracticeTasks.isEmpty()) emptyList() else completedPracticeTasks.split(","),
        quizResults = if (quizResults.isEmpty()) emptyMap() else quizResults.split(",").associate { entry ->
            val parts = entry.split(":")
            parts[0] to parts[1].toBoolean()
        },
        favoriteInterviewQuestions = if (favoriteInterviewQuestions.isEmpty()) emptyList() else favoriteInterviewQuestions.split(","),
        totalXP = totalXP,
        level = level,
        generalTestHistory = gson.fromJson(generalTestHistory, Array<GeneralTestResult>::class.java)?.toList() ?: emptyList()
    )
}