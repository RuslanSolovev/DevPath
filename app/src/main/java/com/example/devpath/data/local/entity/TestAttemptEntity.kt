package com.example.devpath.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "test_attempts")
data class TestAttemptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val timestamp: Long,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val detailsJson: String // JSON с вопросами и ответами
)