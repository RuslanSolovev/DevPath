package com.example.devpath.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.devpath.domain.models.GeneralTestResult
import com.example.devpath.domain.models.UserProgress
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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
    val favorites = this.favoriteInterviewQuestions.joinToString(",")
    android.util.Log.d("ENTITY_DEBUG", "toEntity: favorites = '$favorites', size = ${this.favoriteInterviewQuestions.size}")

    return UserProgressEntity(
        userId = userId,
        displayName = displayName,
        completedLessons = this.completedLessons.joinToString(","),
        completedPracticeTasks = this.completedPracticeTasks.joinToString(","),
        quizResults = this.quizResults.entries.joinToString(",") { "${it.key}:${it.value}" },
        favoriteInterviewQuestions = favorites,
        totalXP = totalXP,
        level = level,
        generalTestHistory = gson.toJson(this.generalTestHistory)
    )
}

fun UserProgressEntity.toDomain(): UserProgress {
    val gson = Gson()
    val historyType = object : TypeToken<List<GeneralTestResult>>() {}.type

    // Логируем сырые данные из БД
    android.util.Log.d("ENTITY_DEBUG", "toDomain: raw favoriteInterviewQuestions = '$favoriteInterviewQuestions'")

    val favoritesList = if (favoriteInterviewQuestions.isEmpty()) {
        android.util.Log.d("ENTITY_DEBUG", "toDomain: favorites is EMPTY")
        emptyList()
    } else {
        val list = favoriteInterviewQuestions.split(",").filter { it.isNotBlank() }
        android.util.Log.d("ENTITY_DEBUG", "toDomain: parsed favorites = $list")
        list
    }

    return UserProgress(
        userId = userId,
        displayName = displayName,
        completedLessons = if (completedLessons.isEmpty()) emptyList() else completedLessons.split(","),
        completedPracticeTasks = if (completedPracticeTasks.isEmpty()) emptyList() else completedPracticeTasks.split(","),
        quizResults = if (quizResults.isEmpty()) emptyMap() else quizResults.split(",").associate { entry ->
            val parts = entry.split(":")
            parts[0] to (parts.getOrNull(1)?.toBoolean() ?: false)
        },
        favoriteInterviewQuestions = favoritesList,
        totalXP = totalXP,
        level = level,
        generalTestHistory = try {
            gson.fromJson(generalTestHistory, historyType) ?: emptyList()
        } catch (e: Exception) {
            println("DEBUG: Ошибка парсинга generalTestHistory: ${e.message}")
            emptyList()
        }
    )
}