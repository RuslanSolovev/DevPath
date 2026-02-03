package com.example.devpath.domain.models

data class QuizQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int, // Индекс правильного ответа (0-based)
    val explanation: String = "",
    val topic: String = "kotlin_basics" // Тема вопроса
)