package com.example.devpath.data.repository.lessons

import com.example.devpath.domain.models.Lesson

object Lesson04ControlFlow {
    fun get(): Lesson = Lesson(
        id = "control_flow",
        title = "🔄 Управляющие конструкции",
        description = "Условные операторы, циклы, обработка исключений и многое другое",
        difficulty = "beginner",
        duration = 35,
        topic = "control_flow",
        theory = """
            # 🔄 Управляющие конструкции в Kotlin

            Управляющие конструкции — это основа любого языка программирования. Они позволяют управлять потоком выполнения программы: принимать решения, повторять действия и обрабатывать ошибки.

            ## 📋 Содержание урока
            - Условные операторы (`if`, `when`)
            - Циклы (`for`, `while`, `do-while`)
            - Операторы перехода (`break`, `continue`, `return`)
            - Исключения (`try-catch-finally`)
            - Проверки (`require`, `check`, `assert`)

            ---

            ## 🎯 Условные операторы

            ### 1️⃣ Классический `if-else`

            В Kotlin `if` — это **выражение**, а не просто инструкция. Он возвращает значение!

            ```kotlin
            // Как инструкция
            if (temperature > 25) {
                println("Жарко!")
            } else {
                println("Прохладно")
            }
            
            // Как выражение (возвращает значение)
            val weather = if (temperature > 25) "Жарко" else "Прохладно"
            println(weather)
            
            // С блоками кода (последнее выражение - результат)
            val max = if (a > b) {
                println("a больше")
                a  // последнее выражение
            } else {
                println("b больше или равно")
                b  // последнее выражение
            }
            ```

            ### 2️⃣ Мощный `when` (замена `switch`)

            `when` — это бриллиант Kotlin. Он заменяет `switch` из Java и делает гораздо больше!

            **Простое использование:**
            ```kotlin
            val x = 3
            when (x) {
                1 -> println("x == 1")
                2, 3 -> println("x == 2 или 3")
                in 4..10 -> println("x между 4 и 10")
                !in 11..20 -> println("x меньше 11 или больше 20")
                else -> println("другое значение")
            }
            ```

            **`when` как выражение:**
            ```kotlin
            val day = 2
            val dayName = when (day) {
                1 -> "Понедельник"
                2 -> "Вторник"
                3 -> "Среда"
                4 -> "Четверг"
                5 -> "Пятница"
                6, 7 -> "Выходной"
                else -> "Неизвестный день"
            }
            println("Сегодня ${'$'}dayName")
            ```

            **`when` без аргумента (замена if-else if):**
            ```kotlin
            val score = 85
            val grade = when {
                score >= 90 -> "Отлично"
                score >= 75 -> "Хорошо"
                score >= 60 -> "Удовлетворительно"
                else -> "Надо подтянуть"
            }
            ```

            **Проверка типа:**
            ```kotlin
            fun describe(obj: Any): String = when (obj) {
                is String -> "Строка длиной ${'$'}{obj.length}"
                is Int -> "Целое число: ${'$'}obj"
                is List<*> -> "Список из ${'$'}{obj.size} элементов"
                else -> "Неизвестный тип"
            }
            ```

            ---

            ## 🔁 Циклы

            ### 1️⃣ `for` — итерация по диапазонам и коллекциям

            **Диапазоны (ranges):**
            ```kotlin
            for (i in 1..5) print(i)        // 12345 (включительно)
            for (i in 1 until 5) print(i)    // 1234 (исключая 5)
            for (i in 5 downTo 1) print(i)    // 54321
            for (i in 1..10 step 2) print(i)  // 13579
            ```

            **По коллекциям:**
            ```kotlin
            val fruits = listOf("Яблоко", "Банан", "Апельсин")
            
            for (fruit in fruits) {
                println(fruit)
            }
            
            // С индексом
            for ((index, value) in fruits.withIndex()) {
                println("${'$'}index: ${'$'}value")
            }
            
            // По индексам
            for (i in fruits.indices) {
                println("${'$'}i -> ${'$'}{fruits[i]}")
            }
            ```

            ### 2️⃣ `while` и `do-while`

            ```kotlin
            var x = 5
            while (x > 0) {
                println("x = ${'$'}x")
                x--
            }
            
            var y = 0
            do {
                println("y = ${'$'}y")
                y++
            } while (y < 3)  // Выполнится хотя бы один раз
            ```

            ---

            ## 🚦 Операторы перехода

            ### `break` и `continue`

            ```kotlin
            for (i in 1..10) {
                if (i % 2 == 0) continue  // пропускаем чётные
                if (i > 7) break          // останавливаем после 7
                println(i)  // 1, 3, 5, 7
            }
            ```

            ### Метки (labels) для вложенных циклов

            ```kotlin
            outer@ for (i in 1..3) {
                for (j in 1..3) {
                    if (i * j > 4) {
                        println("break на i=${'$'}i, j=${'$'}j")
                        break@outer  // прерывает оба цикла
                    }
                    println("i=${'$'}i, j=${'$'}j")
                }
            }
            ```

            ### `return` и `return@label`

            ```kotlin
            fun findFirstPositive(numbers: List<Int>): Int? {
                for (num in numbers) {
                    if (num > 0) return num  // выход из функции
                }
                return null
            }
            
            fun processList(list: List<Int>) {
                list.forEach {
                    if (it < 0) return@forEach  // только выход из forEach
                    println("Обработка: ${'$'}it")
                }
                println("Цикл завершён")
            }
            ```

            ---

            ## ⚠️ Исключения (Exceptions)

            ### Базовый `try-catch-finally`

            ```kotlin
            try {
                val result = 10 / 0
                println("Результат: ${'$'}result")
            } catch (e: ArithmeticException) {
                println("Ошибка: деление на ноль!")
            } finally {
                println("Этот блок выполнится всегда")
            }
            ```

            ### `try` как выражение

            ```kotlin
            val number = try {
                "123".toInt()
            } catch (e: NumberFormatException) {
                0  // значение по умолчанию
            }
            println("Число: ${'$'}number")
            ```

            ### Несколько `catch` блоков

            ```kotlin
            try {
                val list = listOf(1, 2, 3)
                val value = list[5]
                val result = 10 / 0
            } catch (e: IndexOutOfBoundsException) {
                println("Индекс вне диапазона!")
            } catch (e: ArithmeticException) {
                println("Арифметическая ошибка!")
            } catch (e: Exception) {
                println("Другая ошибка: ${'$'}{e.message}")
            }
            ```

            ---

            ## ✅ Проверки (Checks)

            Kotlin предоставляет удобные функции для валидации:

            ```kotlin
            fun processUser(user: User?) {
                // require - для проверки параметров (IllegalArgumentException)
                require(user != null) { "Пользователь не может быть null" }
                require(user.age >= 0) { "Возраст не может быть отрицательным" }
                
                // check - для проверки состояния (IllegalStateException)
                check(user.isActive) { "Пользователь должен быть активным" }
                
                // assert - только для отладки (работает с -ea)
                assert(user.name.isNotEmpty()) { "Имя не может быть пустым" }
                
                println("Пользователь ${'$'}{user.name} валиден")
            }
            ```

            ---

            ## 🎨 Продвинутые примеры

            ### Комбинация `when` с типами
            ```kotlin
            fun processValue(value: Any) = when (value) {
                is String -> "Строка: ${'$'}value"
                is Int -> when {
                    value > 0 -> "Положительное число: ${'$'}value"
                    value < 0 -> "Отрицательное число: ${'$'}value"
                    else -> "Ноль"
                }
                is List<*> -> "Список из ${'$'}{value.size} элементов"
                else -> "Неизвестный тип"
            }
            ```

            ### Циклы с условиями
            ```kotlin
            fun generateSequence(start: Int, limit: Int) {
                var current = start
                while (current <= limit) {
                    when {
                        current % 15 == 0 -> println("FizzBuzz")
                        current % 3 == 0 -> println("Fizz")
                        current % 5 == 0 -> println("Buzz")
                        else -> println(current)
                    }
                    current++
                }
            }
            ```

            ---

            ## 📊 Сравнение с Java

            | Операция | Java | Kotlin |
            |----------|------|--------|
            | Switch | `switch` (только константы) | `when` (любые выражения, типы) |
            | Тернарный оператор | `condition ? a : b` | `if (condition) a else b` |
            | For-each | `for (Item item : list)` | `for (item in list)` |
            | Break с меткой | `break label;` | `break@label` |
            | Try-catch | только инструкция | может быть выражением |

            ---

            ## 💡 Советы

            ✅ Используйте `when` вместо длинных цепочек `if-else if`  
            ✅ Применяйте `if` как выражение для присваивания  
            ✅ Для диапазонов используйте `until`, `downTo`, `step`  
            ✅ Не забывайте про `require` и `check` для валидации  
            ✅ Используйте метки только когда действительно нужно  
            ✅ Помните, что `try` может возвращать значение

            ---

            ## 🚀 Что дальше?

            В следующем уроке мы изучим коллекции — мощные структуры данных для работы с группами объектов.
        """.trimIndent(),
        codeExample = """
            fun main() {
                println("=== УПРАВЛЯЮЩИЕ КОНСТРУКЦИИ В KOTLIN ===\n")
                
                // 1️⃣ IF КАК ВЫРАЖЕНИЕ
                println("1️⃣ IF КАК ВЫРАЖЕНИЕ:")
                val temperature = 28
                val weather = if (temperature > 25) "☀️ Жарко" else "☁️ Прохладно"
                println("Температура ${'$'}temperature°: ${'$'}weather")
                
                val a = 10
                val b = 20
                val max = if (a > b) {
                    println("a больше b")
                    a
                } else {
                    println("b больше или равно a")
                    b
                }
                println("Максимум: ${'$'}max\n")
                
                // 2️⃣ WHEN ВО ВСЕЙ КРАСЕ
                println("2️⃣ WHEN ВО ВСЕЙ КРАСЕ:")
                
                val day = 3
                val dayName = when (day) {
                    1 -> "Понедельник"
                    2 -> "Вторник"
                    3 -> "Среда"
                    4 -> "Четверг"
                    5 -> "Пятница"
                    6, 7 -> "Выходной"
                    else -> "Неизвестный день"
                }
                println("День ${'$'}day: ${'$'}dayName")
                
                val score = 82
                val grade = when {
                    score >= 90 -> "Отлично (A)"
                    score >= 75 -> "Хорошо (B)"
                    score >= 60 -> "Удовлетворительно (C)"
                    else -> "Нужно подтянуть (D)"
                }
                println("Оценка за ${'$'}score баллов: ${'$'}grade")
                
                fun describe(obj: Any): String = when (obj) {
                    is String -> "Строка длиной ${'$'}{obj.length}"
                    is Int -> when {
                        obj > 0 -> "Положительное число ${'$'}obj"
                        obj < 0 -> "Отрицательное число ${'$'}obj"
                        else -> "Ноль"
                    }
                    is List<*> -> "Список из ${'$'}{obj.size} элементов"
                    else -> "Неизвестный тип"
                }
                
                println("describe(\"Kotlin\"): ${'$'}{describe("Kotlin")}")
                println("describe(42): ${'$'}{describe(42)}")
                println("describe(-5): ${'$'}{describe(-5)}")
                println("describe(listOf(1,2,3)): ${'$'}{describe(listOf(1,2,3))}\n")
                
                // 3️⃣ ЦИКЛЫ
                println("3️⃣ ЦИКЛЫ:")
                
                println("Диапазоны:")
                print("1..5: ")
                for (i in 1..5) print("${'$'}i ")
                println()
                
                print("5 downTo 1: ")
                for (i in 5 downTo 1) print("${'$'}i ")
                println()
                
                print("1..10 step 2: ")
                for (i in 1..10 step 2) print("${'$'}i ")
                println()
                
                println("\nКоллекции:")
                val fruits = listOf("Яблоко", "Банан", "Апельсин", "Киви")
                
                println("Фрукты:")
                for (fruit in fruits) {
                    println("  • ${'$'}fruit")
                }
                
                println("С индексами:")
                for ((index, fruit) in fruits.withIndex()) {
                    println("  ${'$'}index: ${'$'}fruit")
                }
                
                println("\nwhile и do-while:")
                var counter = 3
                while (counter > 0) {
                    println("  while: ${'$'}counter")
                    counter--
                }
                
                var counter2 = 0
                do {
                    println("  do-while: ${'$'}counter2")
                    counter2++
                } while (counter2 < 3)
                
                // 4️⃣ BREAK И CONTINUE С МЕТКАМИ
                println("\n4️⃣ BREAK И CONTINUE С МЕТКАМИ:")
                
                println("continue - пропуск чётных:")
                for (i in 1..8) {
                    if (i % 2 == 0) continue
                    print("${'$'}i ")
                }
                println()
                
                println("break с меткой:")
                outer@ for (i in 1..3) {
                    for (j in 1..3) {
                        if (i * j > 4) {
                            println("  Прерываем на i=${'$'}i, j=${'$'}j")
                            break@outer
                        }
                        println("  i=${'$'}i, j=${'$'}j")
                    }
                }
                
                // 5️⃣ ОБРАБОТКА ИСКЛЮЧЕНИЙ
                println("\n5️⃣ ОБРАБОТКА ИСКЛЮЧЕНИЙ:")
                
                println("Деление на ноль:")
                try {
                    val result = 10 / 0
                    println("Результат: ${'$'}result")
                } catch (e: ArithmeticException) {
                    println("  Поймано исключение: ${'$'}{e.message}")
                } finally {
                    println("  Блок finally выполнен")
                }
                
                println("try как выражение:")
                val number = try {
                    "123abc".toInt()
                } catch (e: NumberFormatException) {
                    -1  // значение по умолчанию
                }
                println("  Результат парсинга: ${'$'}number")
                
                // 6️⃣ ПРОВЕРКИ (REQUIRE, CHECK)
                println("\n6️⃣ ПРОВЕРКИ:")
                
                fun validateAge(age: Int) {
                    try {
                        require(age >= 0) { "Возраст не может быть отрицательным" }
                        require(age <= 150) { "Возраст не может быть больше 150" }
                        println("  Возраст ${'$'}age валиден")
                    } catch (e: IllegalArgumentException) {
                        println("  Ошибка валидации: ${'$'}{e.message}")
                    }
                }
                
                validateAge(25)
                validateAge(-5)
                validateAge(200)
                
                // 7️⃣ ПРАКТИЧЕСКИЙ ПРИМЕР: FIZZBUZZ
                println("\n7️⃣ ПРАКТИЧЕСКИЙ ПРИМЕР: FIZZBUZZ")
                println("Числа от 1 до 15:")
                for (i in 1..15) {
                    when {
                        i % 15 == 0 -> print("FizzBuzz ")
                        i % 3 == 0 -> print("Fizz ")
                        i % 5 == 0 -> print("Buzz ")
                        else -> print("${'$'}i ")
                    }
                }
                println()
                
                // 8️⃣ ФУНКЦИЯ С RETURN@LABEL
                println("\n8️⃣ RETURN@LABEL:")
                
                fun processNumbers(numbers: List<Int>) {
                    println("Обработка списка ${'$'}numbers:")
                    numbers.forEach {
                        if (it < 0) {
                            println("  Отрицательное число ${'$'}it, пропускаем")
                            return@forEach  // только выход из forEach
                        }
                        println("  Обработка: ${'$'}it")
                    }
                    println("  Цикл forEach завершён")
                }
                
                processNumbers(listOf(1, -2, 3, -4, 5))
                
                println("\n=== КОНЕЦ ДЕМОНСТРАЦИИ ===")
            }
        """.trimIndent()
    )
}