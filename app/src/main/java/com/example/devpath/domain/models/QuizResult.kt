package com.example.devpath.domain.models

data class QuizResult(
    val totalQuestions: Int,
    val correctAnswers: Int,
    val topicResults: Map<String, TopicResult>
)

data class TopicResult(
    val topicName: String,
    val totalQuestions: Int,
    val correctAnswers: Int
)