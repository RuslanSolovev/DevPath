package com.example.devpath.data.repository.lessons

import com.example.devpath.domain.models.Lesson

object Lesson01KotlinBasics {
    fun get(): Lesson = Lesson(
        id = "kotlin_basics",
        title = "Основы Kotlin",
        description = "Погружение в базовый синтаксис, переменные, типы и основы функционального стиля",
        difficulty = "beginner",
        duration = 25,
        topic = "kotlin_basics",
        theory = """
            # 🚀 Введение в Kotlin

            Kotlin — это современный, статически типизированный язык программирования, который работает на платформе Java Virtual Machine (JVM), а также может компилироваться в JavaScript и нативный код (через Kotlin/Native). Он разработан компанией JetBrains (создатели IntelliJ IDEA) и с 2017 года официально поддерживается Google для разработки под Android.

            ## ✨ Почему Kotlin?

            - **Лаконичность** — код на Kotlin в среднем на 40% короче, чем эквивалентный код на Java.
            - **Безопасность** — встроенная защита от `NullPointerException` (null safety).
            - **Интероперабельность** — 100% совместимость с Java, можно использовать все существующие Java-библиотеки.
            - **Функциональный подход** — поддержка функций высшего порядка, лямбд, неизменяемости.
            - **Мультиплатформенность** — один и тот же код может работать на разных платформах (сервер, Android, iOS, веб).

            ## 📦 Переменные и неизменяемость

            В Kotlin два основных типа переменных:

            - `val` — **неизменяемая** ссылка (как `final` в Java). После инициализации значение нельзя изменить.
            - `var` — **изменяемая** ссылка.

            ```kotlin
            val language = "Kotlin"   // неизменяемая
            var version = 1.9          // изменяемая
            version = 2.0              // OK
            // language = "Java"       // Ошибка компиляции!
            ```

            Kotlin поддерживает **вывод типов** — обычно тип можно не указывать, компилятор сам его определит.

            ```kotlin
            val name = "Kotlin"        // тип String
            val year = 2024             // тип Int
            val pi = 3.14159            // тип Double
            val isAwesome = true        // тип Boolean
            ```

            При необходимости тип можно указать явно:

            ```kotlin
            val message: String = "Hello"
            val count: Int = 42
            ```

            ## 🔤 Основные типы данных

            | Категория       | Типы                                                                 |
            |-----------------|----------------------------------------------------------------------|
            | Целые числа     | `Byte`, `Short`, `Int`, `Long`                                       |
            | Числа с плавающей точкой | `Float`, `Double`                                             |
            | Логические      | `Boolean` (значения `true` / `false`)                                |
            | Символы         | `Char` (одиночный символ в кавычках)                                 |
            | Строки          | `String` (последовательность символов в двойных кавычках)            |

            ### Числовые литералы

            ```kotlin
            val byte: Byte = 127
            val int = 100_000_000      // можно использовать подчёркивания для читаемости
            val long = 100L            // суффикс L
            val float = 3.14f           // суффикс f
            val double = 3.14159
            ```

            ### Строки

            Строки могут быть как однострочными, так и многострочными (с сохранением форматирования):

            ```kotlin
            val singleLine = "Обычная строка"
            val multiLine = \"\"\"
                Это пример
                многострочной
                строки
            \"\"\".trimIndent()
            ```

            **Шаблоны строк** (string templates) позволяют встраивать выражения прямо в строку:

            ```kotlin
            val name = "Мир"
            val greeting = "Привет, \${'$'}name!"                // Привет, Мир!
            val sum = "2 + 2 = \${'$'}{2 + 2}"                    // 2 + 2 = 4
            ```

            ### Массивы

            ```kotlin
            val numbers = arrayOf(1, 2, 3, 4, 5)
            val strings = arrayOf("A", "B", "C")
            val intArray = intArrayOf(1, 2, 3)              // примитивный массив Int
            ```

            ## 💬 Комментарии

            ```kotlin
            // Однострочный комментарий

            /*
              Многострочный
              комментарий
            */

            /**
             * KDoc-комментарий (используется для документации)
             * @param name имя пользователя
             * @return приветствие
             */
            fun greet(name: String): String = "Hello, \${'$'}name"
            ```

            ## 🧩 Базовые операторы

            Арифметические: `+`, `-`, `*`, `/`, `%`
            Сравнения: `==`, `!=`, `<`, `>`, `<=`, `>=`
            Логические: `&&`, `||`, `!`

            ```kotlin
            val a = 10
            val b = 20
            val sum = a + b
            val isGreater = a > b   // false
            ```

            ## 🔄 Управляющие конструкции (краткий обзор)

            ### Условный оператор `if`

            В Kotlin `if` — это **выражение**, возвращающее значение:

            ```kotlin
            val max = if (a > b) a else b
            ```

            ### `when` — замена `switch`

            ```kotlin
            val day = 2
            val dayName = when (day) {
                1 -> "Понедельник"
                2 -> "Вторник"
                else -> "Неизвестный день"
            }
            ```

            ### Циклы

            ```kotlin
            for (i in 1..5) println(i)            // от 1 до 5 включительно
            for (i in 5 downTo 1) println(i)       // от 5 до 1
            for (i in 1..10 step 2) println(i)     // 1,3,5,7,9

            var x = 0
            while (x < 5) {
                println(x)
                x++
            }
            ```

            ## 🎯 Функции

            Функции объявляются ключевым словом `fun`:

            ```kotlin
            // Простая функция
            fun greet() {
                println("Привет!")
            }

            // Функция с параметрами и возвращаемым значением
            fun sum(a: Int, b: Int): Int {
                return a + b
            }

            // Однострочная функция (сокращённый синтаксис)
            fun multiply(a: Int, b: Int) = a * b

            // Функция с параметром по умолчанию
            fun greetUser(name: String = "Гость") {
                println("Привет, \${'$'}name!")
            }
            ```

            ## 🧪 Null Safety (безопасность null)

            Одна из главных фич Kotlin — возможность явно указать, может ли переменная быть `null`:

            - `String` — **не может** быть null
            - `String?` — **может** быть null

            ```kotlin
            var notNull: String = "Всегда значение"
            // notNull = null          // Ошибка!

            var nullable: String? = "Может быть null"
            nullable = null              // OK
            ```

            **Безопасный вызов** (`?.`): если объект null, то выражение вернёт null, не вызывая метод.

            ```kotlin
            val length = nullable?.length   // если nullable == null, то length == null
            ```

            **Оператор Элвиса** (`?:`): позволяет задать значение по умолчанию.

            ```kotlin
            val safeLength = nullable?.length ?: 0   // если nullable == null, вернёт 0
            ```

            **Оператор `!!`** (не рекомендуется, но иногда нужен): утверждает, что значение не null; если это не так, выбрасывается исключение.

            ```kotlin
            val forcedLength = nullable!!.length   // бросит NPE, если nullable == null
            ```

            ## 📚 Вывод

            Kotlin сочетает простоту и мощь, позволяя писать выразительный, безопасный и лаконичный код. Это отличный выбор как для новичков, так и для профессионалов.

            В следующих уроках мы углубимся в более сложные темы: коллекции, функции высшего порядка, классы и многое другое.
        """.trimIndent(),
        codeExample = """
            fun main() {
                // Демонстрация основ Kotlin

                // --- Переменные ---
                val language = "Kotlin"
                var version = 1.9
                println("Изучаю \${'$'}language версии \${'$'}version")

                version = 2.0
                println("Новая версия: \${'$'}version")

                // --- Типы данных ---
                val age: Int = 5
                val pi: Double = 3.14159
                val isFun = true
                val firstChar = 'K'

                println("Возраст языка: \${'$'}age лет")
                println("Число Пи: \${'$'}pi")
                println("Это весело? \${'$'}isFun")
                println("Первый символ: \${'$'}firstChar")

                // --- Арифметика ---
                val a = 10
                val b = 20
                val sum = a + b
                val product = a * b
                println("Сумма: \${'$'}sum, Произведение: \${'$'}product")

                // --- Null Safety ---
                var nullableString: String? = "Hello"
                println("Длина: " + (nullableString?.length ?: "null"))

                nullableString = null
                println("Длина после null: " + (nullableString?.length ?: "null"))

                // --- Условные выражения ---
                val max = if (a > b) a else b
                println("Максимум: \${'$'}max")

                // --- Циклы ---
                println("Числа от 1 до 5:")
                for (i in 1..5) {
                    print("\${'$'}i ")
                }
                println()

                // --- Функции ---
                greet()
                println("5 + 3 = \${'$'}{sum(5, 3)}")
                println("5 * 3 = \${'$'}{multiply(5, 3)}")
                greetUser("Алексей")
                greetUser() // использует значение по умолчанию
            }

            fun greet() {
                println("Привет из функции!")
            }

            fun sum(a: Int, b: Int): Int {
                return a + b
            }

            fun multiply(a: Int, b: Int) = a * b

            fun greetUser(name: String = "Гость") {
                println("Привет, \${'$'}name!")
            }
        """.trimIndent()
    )
}