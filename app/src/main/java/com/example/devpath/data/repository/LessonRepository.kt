package com.example.devpath.data.repository

import com.example.devpath.domain.models.Lesson

object LessonRepository {
    fun getLessons(): List<Lesson> {
        return listOf(
            Lesson(
                id = "kotlin_basics",
                title = "Основы Kotlin",
                description = "Изучение базового синтаксиса Kotlin",
                theory = """
                    # Основы Kotlin
                    
                    Kotlin — современный язык программирования, который полностью совместим с Java.
                    
                    ## Переменные
                    
                    В Kotlin есть два типа переменных:
                    - \`val\` — неизменяемая переменная (аналог final в Java)
                    - \`var\` — изменяемая переменная
                    
                    ```kotlin
                    val name = "Kotlin"  // нельзя изменить
                    var age = 5          // можно изменить
                    ```
                    
                    ## Типы данных
                    
                    Kotlin имеет следующие основные типы:
                    - \`String\` — строки
                    - \`Int\` — целые числа
                    - \`Double\` — числа с плавающей точкой
                    - \`Boolean\` — логические значения
                """.trimIndent()
            ),
            Lesson(
                id = "variables_types",
                title = "Переменные и типы",
                description = "Глубокое погружение в переменные и типы данных",
                theory = """
                    # Переменные и типы в Kotlin
                    
                    ## Объявление переменных
                    
                    ```kotlin
                    // Неявная типизация
                    val message = "Hello"
                    var count = 42
                    
                    // Явная типизация
                    val pi: Double = 3.14
                    var isActive: Boolean = true
                    ```
                    
                    ## Null Safety
                    
                    Kotlin защищает от NullPointerException:
                    - \`String\` — не может быть null
                    - \`String?\` — может быть null
                    
                    ```kotlin
                    val name: String = "Kotlin"  // всегда не null
                    val nullableName: String? = null  // может быть null
                    ```
                """.trimIndent(),
                codeExample = """
                    fun main() {
                        val language = "Kotlin"
                        var version = 1.9
                        
                        println("Я изучаю " + language + " версии " + version)
                        
                        // Попробуй изменить val - будет ошибка!
                        // language = "Java" // Compilation error!
                    }
                """.trimIndent()
            ),
            Lesson(
                id = "functions",
                title = "Функции",
                description = "Создание и использование функций",
                theory = """
                    # Функции в Kotlin
                    
                    Функции объявляются с помощью ключевого слова \`fun\`.
                    
                    ## Синтаксис
                    
                    ```kotlin
                    fun имяФункции(параметры): ТипВозврата {
                        // тело функции
                        return значение
                    }
                    ```
                    
                    ## Примеры
                    
                    ```kotlin
                    // Функция без параметров и возвращаемого значения
                    fun greet() {
                        println("Привет!")
                    }
                    
                    // Функция с параметрами и возвращаемым значением
                    fun add(a: Int, b: Int): Int {
                        return a + b
                    }
                    
                    // Сокращённый синтаксис для однострочных функций
                    fun multiply(a: Int, b: Int) = a * b
                    ```
                """.trimIndent()
            )
        )
    }

    fun getLessonById(id: String): Lesson? {
        return getLessons().find { it.id == id }
    }
}