package com.example.devpath.domain.models


data class PracticeTask (
    val id: String,
    val title: String,
    val description: String,
    val starterCode: String,
    val solution: String,
    val hint: String = "",
    val difficulty: String = "beginner" // beginner, intermediate, advanced
)
