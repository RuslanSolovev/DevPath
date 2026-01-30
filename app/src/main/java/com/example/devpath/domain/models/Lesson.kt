package com.example.devpath.domain.models

data class Lesson (
    val id: String,
    val title: String,
    val description: String,
    val theory: String,     // Может быть Markdown позже
    val codeExample: String? = null,
    val difficulty: String = "beginner"   // beginner, intermediate, advanced
)