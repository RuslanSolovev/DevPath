package com.example.devpath.data.repository

import com.example.devpath.domain.models.Lesson
import com.example.devpath.data.repository.lessons.*

object LessonRepository {

    private val lessons = listOf(
        Lesson01KotlinBasics.get(),
        Lesson02VariablesTypes.get(),
        Lesson03Functions.get(),
        Lesson04ControlFlow.get(),
        Lesson05Collections.get(),
        Lesson06OOP.get(),
        Lesson07NullSafety.get(),
        Lesson08Extensions.get(),
        Lesson09Coroutines.get(),
        Lesson10DSL.get(),
        Lesson11Delegation.get(),
        Lesson12Functional.get()
    )

    fun getLessons(): List<Lesson> = lessons

    fun getLessonById(id: String): Lesson? = lessons.find { it.id == id }
}
