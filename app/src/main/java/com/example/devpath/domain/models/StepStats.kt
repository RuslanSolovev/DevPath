package com.example.devpath.domain.models

import com.google.firebase.Timestamp

data class StepStats(
    val userId: String = "",
    val userName: String = "",
    val userAvatar: String? = null,
    val steps: Int = 0,
    val date: String = "", // Формат: yyyy-MM-dd
    val timestamp: Timestamp = Timestamp.now(),
    val weeklySteps: Map<String, Int> = emptyMap(), // День недели -> шаги
    val monthlySteps: Map<String, Int> = emptyMap(), // День месяца -> шаги
    val totalSteps: Int = 0,
    val dailyGoal: Int = 10000,
    val streak: Int = 0,
    val bestDay: Int = 0,
    val caloriesBurned: Int = 0,
    val distanceKm: Double = 0.0,
    val activeMinutes: Int = 0
)

data class LeaderboardEntry(
    val userId: String,
    val userName: String,
    val userAvatar: String?,
    val totalSteps: Int,
    val dailyAverage: Int,
    val streak: Int,
    val rank: Int
)