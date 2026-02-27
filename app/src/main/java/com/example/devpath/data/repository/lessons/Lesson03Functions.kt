package com.example.devpath.data.repository.lessons

import com.example.devpath.domain.models.Lesson

object Lesson03Functions {
    fun get(): Lesson = Lesson(
        id = "functions",
        title = "🎯 Функции",
        description = "Всё о функциях: от основ до функционального программирования",
        difficulty = "beginner",
        duration = 35,
        topic = "functions",
        theory = """
            # 🎯 Функции в Kotlin

            Функции — это основа любого Kotlin-приложения. Они позволяют структурировать код, избегать повторений и создавать абстракции.

            ## 📝 Базовый синтаксис

            Функции объявляются ключевым словом `fun`:

            ```kotlin
            fun имяФункции(параметр1: Тип1, параметр2: Тип2): ТипВозврата {
                // тело функции
                return значение
            }
            ```

            ### Простые примеры

            ```kotlin
            // Функция без параметров и без возвращаемого значения (возвращает Unit)
            fun greet() {
                println("Привет, Kotlin!")
            }
            
            // Функция с параметрами и возвращаемым значением
            fun add(a: Int, b: Int): Int {
                return a + b
            }
            
            // Однострочная функция (single-expression function)
            fun multiply(a: Int, b: Int) = a * b
            
            // Функция с возвращаемым типом Unit (можно опустить)
            fun log(message: String): Unit {
                println("[LOG]: ${'$'}message")
            }
            ```

            ## 🔄 Функции как граждане первого класса

            В Kotlin функции могут:
            - Храниться в переменных
            - Передаваться как параметры
            - Возвращаться из других функций

            ```kotlin
            // Функция в переменной
            val sum: (Int, Int) -> Int = { x, y -> x + y }
            val result = sum(5, 3)  // 8
            
            // Тип функции можно не указывать
            val product = { x: Int, y: Int -> x * y }
            ```

            ## 🎨 Различные виды функций

            ### 1. Функции-члены (Member functions)

            ```kotlin
            class Calculator {
                fun add(a: Int, b: Int): Int = a + b
                fun subtract(a: Int, b: Int): Int = a - b
            }
            
            val calc = Calculator()
            println(calc.add(5, 3))  // 8
            ```

            ### 2. Локальные функции (вложенные)

            Функции могут быть объявлены внутри других функций:

            ```kotlin
            fun processNumbers(numbers: List<Int>) {
                fun isEven(n: Int) = n % 2 == 0
                fun isOdd(n: Int) = !isEven(n)
                
                val evens = numbers.filter(::isEven)
                val odds = numbers.filter(::isOdd)
                
                println("Четные: ${'$'}evens")
                println("Нечетные: ${'$'}odds")
            }
            ```

            ### 3. Функции высшего порядка (Higher-order functions)

            Функции, которые принимают другие функции как параметры или возвращают их:

            ```kotlin
            fun calculate(a: Int, b: Int, operation: (Int, Int) -> Int): Int {
                return operation(a, b)
            }
            
            // Использование
            val sum = calculate(10, 5) { x, y -> x + y }      // 15
            val product = calculate(10, 5) { x, y -> x * y }  // 50
            ```

            ### 4. Расширяющие функции (Extension functions)

            Позволяют добавлять новые функции к существующим классам:

            ```kotlin
            // Добавляем функцию к String
            fun String.addExclamation(): String = this + "!"
            
            // Добавляем функцию к Int
            fun Int.isEven(): Boolean = this % 2 == 0
            fun Int.isOdd(): Boolean = !this.isEven()
            
            // Использование
            println("Hello".addExclamation())  // Hello!
            println(4.isEven())                 // true
            println(7.isOdd())                  // true
            ```

            ### 5. Инфиксные функции (Infix functions)

            Функции, которые можно вызывать без точки и скобок:

            ```kotlin
            infix fun Int.times(str: String) = str.repeat(this)
            infix fun String.plus(other: String) = this + other
            
            val result = 3 times "Hello"  // HelloHelloHello
            val combined = "Hello" plus " World"  // Hello World
            ```

            ### 6. Хвостовая рекурсия (Tailrec functions)

            Для рекурсивных функций, которые компилятор может оптимизировать в цикл:

            ```kotlin
            tailrec fun factorial(n: Int, accumulator: Long = 1): Long {
                return if (n <= 1) accumulator
                else factorial(n - 1, n * accumulator)
            }
            
            // Обычная рекурсия может вызвать StackOverflow
            // Хвостовая рекурсия безопасна для больших чисел
            println(factorial(5))    // 120
            println(factorial(100))  // Большое число без переполнения стека
            ```

            ## 📦 Параметры функций

            ### Именованные аргументы (Named arguments)

            ```kotlin
            fun createUser(name: String, age: Int, email: String, isActive: Boolean = true) {
                println("Создан пользователь: ${'$'}name, возраст: ${'$'}age, email: ${'$'}email")
            }
            
            // Можно указывать параметры в любом порядке
            createUser(
                email = "john@example.com",
                name = "John",
                age = 25
            )
            ```

            ### Значения по умолчанию (Default arguments)

            ```kotlin
            fun sendMessage(
                text: String,
                recipient: String = "all",
                priority: Int = 1,
                isUrgent: Boolean = false
            ) {
                println("Отправка '${'$'}text' к ${'$'}recipient (приоритет: ${'$'}priority)")
            }
            
            sendMessage("Hello")                    // recipient = "all", priority = 1
            sendMessage("Важно!", priority = 5)     // recipient = "all", priority = 5
            sendMessage("Срочно!", isUrgent = true) // recipient = "all", priority = 1
            ```

            ### Переменное количество аргументов (vararg)

            ```kotlin
            fun sum(vararg numbers: Int): Int {
                return numbers.sum()
            }
            
            fun <T> join(vararg elements: T, separator: String = ", "): String {
                return elements.joinToString(separator)
            }
            
            println(sum(1, 2, 3, 4, 5))        // 15
            println(sum(10, 20))                // 30
            
            println(join("a", "b", "c"))        // a, b, c
            println(join(1, 2, 3, separator = "-"))  // 1-2-3
            
            // Spread operator (*) для передачи массива
            val numbers = intArrayOf(1, 2, 3)
            println(sum(*numbers))               // 6
            ```

            ## 🎭 Лямбда-выражения

            Лямбды — это анонимные функции, которые можно передавать как значения:

            ```kotlin
            // Полная запись
            val sum: (Int, Int) -> Int = { a: Int, b: Int -> a + b }
            
            // Сокращенная (типы выведены)
            val multiply = { a: Int, b: Int -> a * b }
            
            // С одним параметром (используем it)
            val square: (Int) -> Int = { it * it }
            
            // Без параметров
            val greet = { println("Hello!") }
            ```

            ### Лямбды с коллекциями

            ```kotlin
            val numbers = listOf(1, 2, 3, 4, 5, 6)
            
            // Фильтрация
            val evens = numbers.filter { it % 2 == 0 }        // [2, 4, 6]
            
            // Трансформация
            val squares = numbers.map { it * it }             // [1, 4, 9, 16, 25, 36]
            
            // Сортировка
            val sorted = numbers.sortedBy { -it }             // [6, 5, 4, 3, 2, 1]
            
            // Сворачивание (fold/reduce)
            val sum = numbers.reduce { acc, i -> acc + i }    // 21
            val product = numbers.fold(1) { acc, i -> acc * i } // 720
            ```

            ## 🔗 Композиция функций

            ```kotlin
            // Композиция функций вручную
            fun <A, B, C> compose(f: (B) -> C, g: (A) -> B): (A) -> C {
                return { x -> f(g(x)) }
            }
            
            val addOne = { x: Int -> x + 1 }
            val double = { x: Int -> x * 2 }
            val addOneThenDouble = compose(double, addOne)
            
            println(addOneThenDouble(5))  // (5 + 1) * 2 = 12
            
            // Инфиксная композиция
            infix fun <A, B, C> ((B) -> C).after(g: (A) -> B): (A) -> C {
                return { x -> this(g(x)) }
            }
            
            val doubleThenAddOne = (addOne) after double
            println(doubleThenAddOne(5))  // (5 * 2) + 1 = 11
            ```

            ## ⚡ Встроенные функции (inline)

            `inline` функции уменьшают накладные расходы на лямбды:

            ```kotlin
            inline fun measureTime(block: () -> Unit) {
                val start = System.currentTimeMillis()
                block()
                val end = System.currentTimeMillis()
                println("Время выполнения: ${'$'}{end - start}ms")
            }
            
            // Использование
            measureTime {
                Thread.sleep(100)
                println("Работа выполнена")
            }
            ```

            ### noinline и crossinline

            ```kotlin
            inline fun process(
                crossinline operation: () -> Unit,  // Нельзя использовать return
                noinline callback: () -> Unit       // Не будет встроена
            ) {
                operation()
                // callback можно сохранить в переменную
                val savedCallback = callback
            }
            ```

            ## 🎯 Функции с ресивером (Function literals with receiver)

            Позволяют вызывать методы ресивера внутри лямбды без явной квалификации:

            ```kotlin
            // buildString — функция с ресивером StringBuilder
            val string = buildString {
                append("Hello")
                append(" ")
                append("World")
            }
            println(string)  // Hello World
            
            // Свой DSL
            fun html(init: HTML.() -> Unit): HTML {
                val html = HTML()
                html.init()
                return html
            }
            
            class HTML {
                private val elements = mutableListOf<String>()
                fun body(init: Body.() -> Unit) {
                    val body = Body()
                    body.init()
                    elements.add(body.toString())
                }
                override fun toString() = elements.joinToString("")
            }
            ```

            ## 📚 Операторные функции

            В Kotlin можно переопределять операторы:

            ```kotlin
            data class Point(val x: Int, val y: Int) {
                operator fun plus(other: Point) = Point(x + other.x, y + other.y)
                operator fun times(scale: Int) = Point(x * scale, y * scale)
                operator fun compareTo(other: Point): Int = (x + y) - (other.x + other.y)
            }
            
            val p1 = Point(1, 2)
            val p2 = Point(3, 4)
            
            println(p1 + p2)  // Point(4, 6)
            println(p1 * 3)   // Point(3, 6)
            println(p1 < p2)  // true
            ```

            ## 🏆 Практические примеры

            ### Функции для валидации

            ```kotlin
            fun <T> validate(value: T, validator: (T) -> Boolean, errorMessage: String): T {
                if (!validator(value)) {
                    throw IllegalArgumentException(errorMessage)
                }
                return value
            }
            
            val email = validate("test@example.com",
                validator = { it.contains('@') },
                errorMessage = "Некорректный email"
            )
            ```

            ### Функции с мемоизацией (кэшированием)

            ```kotlin
            fun <T, R> ((T) -> R).memoize(): (T) -> R {
                val cache = mutableMapOf<T, R>()
                return { key ->
                    cache.getOrPut(key) { this(key) }
                }
            }
            
            val expensiveOperation = { n: Int ->
                println("Вычисление для ${'$'}n")
                n * n
            }
            
            val cached = expensiveOperation.memoize()
            println(cached(5))  // Вычисляет
            println(cached(5))  // Берёт из кэша
            ```

            ## 📋 Резюме

            ✅ Функции объявляются с `fun` и могут возвращать значение или `Unit`
            ✅ Поддерживаются функции высшего порядка и лямбды
            ✅ Расширения добавляют функциональность существующим классам
            ✅ `vararg` для переменного числа аргументов
            ✅ Именованные аргументы и значения по умолчанию
            ✅ `tailrec` для оптимизации рекурсии
            ✅ `inline` для уменьшения накладных расходов
            ✅ Функции с ресивером для создания DSL

            Функции — это мощный инструмент, который делает Kotlin одновременно простым и выразительным языком!
        """.trimIndent(),
        codeExample = """
            fun main() {
                println("=== Функции в Kotlin ===\n")
                
                // 1️⃣ Базовые функции
                println("1️⃣ Базовые функции:")
                println("5 + 3 = ${'$'}{add(5, 3)}")
                println("5 * 3 = ${'$'}{multiply(5, 3)}")
                
                // 2️⃣ Функции высшего порядка
                println("\n2️⃣ Функции высшего порядка:")
                val numbers = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                
                val evenNumbers = numbers.filter { it % 2 == 0 }
                println("Четные числа: ${'$'}evenNumbers")
                
                val doubled = numbers.map { it * 2 }
                println("Удвоенные числа: ${'$'}doubled")
                
                val sum = numbers.reduce { acc, i -> acc + i }
                println("Сумма всех чисел: ${'$'}sum")
                
                // 3️⃣ Функции с параметрами по умолчанию
                println("\n3️⃣ Параметры по умолчанию:")
                createUser("Алексей", 30)
                createUser("Мария", 25, "maria@example.com")
                
                // 4️⃣ Именованные аргументы
                println("\n4️⃣ Именованные аргументы:")
                sendMessage(
                    text = "Важное сообщение",
                    priority = 5,
                    recipient = "admin"
                )
                
                // 5️⃣ vararg (переменное количество аргументов)
                println("\n5️⃣ Переменное количество аргументов:")
                println("Сумма 1-5: ${'$'}{sumNumbers(1, 2, 3, 4, 5)}")
                println("Сумма 10,20,30: ${'$'}{sumNumbers(10, 20, 30)}")
                
                val list = listOf(1, 2, 3, 4, 5)
                println("Объединение: ${'$'}{joinElements("a", "b", "c", separator = "-")}")
                
                // 6️⃣ Расширяющие функции
                println("\n6️⃣ Расширяющие функции:")
                println("Привет".addExclamation())
                println("Kotlin".addExclamation().addExclamation())
                println("Число 42 четное? ${'$'}{42.isEven()}")
                println("Число 43 четное? ${'$'}{43.isEven()}")
                
                // 7️⃣ Инфиксные функции
                println("\n7️⃣ Инфиксные функции:")
                val repeated = 3 times "Kotlin "
                println("Повтор 3 раза: ${'$'}repeated")
                
                // 8️⃣ Лямбда-выражения
                println("\n8️⃣ Лямбда-выражения:")
                val square = { x: Int -> x * x }
                println("Квадрат 5: ${'$'}{square(5)}")
                
                val numbers2 = listOf(1, 2, 3, 4, 5)
                val squares = numbers2.map { it * it }
                println("Квадраты чисел: ${'$'}squares")
                
                // 9️⃣ Хвостовая рекурсия
                println("\n9️⃣ Хвостовая рекурсия:")
                println("Факториал 5: ${'$'}{factorial(5)}")
                println("Факториал 10: ${'$'}{factorial(10)}")
                
                // 🔟 Композиция функций
                println("\n🔟 Композиция функций:")
                val addTwo = { x: Int -> x + 2 }
                val multiplyByThree = { x: Int -> x * 3 }
                
                val composed = addTwo.andThen(multiplyByThree)
                println("(5 + 2) * 3 = ${'$'}{composed(5)}")
                
                // 1️⃣1️⃣ Функции с ресивером
                println("\n1️⃣1️⃣ Функции с ресивером:")
                val message = buildString {
                    append("Это ")
                    append("пример ")
                    append("работы ")
                    append("с ресивером")
                }
                println(message)
                
                // 1️⃣2️⃣ Встроенные функции (inline) для измерения времени
                println("\n1️⃣2️⃣ Измерение времени:")
                measureTime {
                    Thread.sleep(100)
                    println("Операция выполнена")
                }
                
                // 1️⃣3️⃣ Операторные функции
                println("\n1️⃣3️⃣ Операторные функции:")
                val p1 = Point(2, 3)
                val p2 = Point(4, 5)
                println("${'$'}p1 + ${'$'}p2 = ${'$'}{p1 + p2}")
                println("${'$'}p1 * 3 = ${'$'}{p1 * 3}")
            }
            
            // Базовые функции
            fun add(a: Int, b: Int): Int = a + b
            fun multiply(a: Int, b: Int): Int = a * b
            
            // Функция с параметрами по умолчанию
            fun createUser(name: String, age: Int, email: String = "не указан") {
                println("Пользователь: ${'$'}name, возраст: ${'$'}age, email: ${'$'}email")
            }
            
            // Именованные аргументы
            fun sendMessage(text: String, recipient: String = "all", priority: Int = 1) {
                println("Отправка '${'$'}text' к ${'$'}recipient (приоритет: ${'$'}priority)")
            }
            
            // vararg
            fun sumNumbers(vararg numbers: Int): Int {
                return numbers.sum()
            }
            
            fun joinElements(vararg elements: String, separator: String = ", "): String {
                return elements.joinToString(separator)
            }
            
            // Расширяющие функции
            fun String.addExclamation(): String = this + "!"
            fun Int.isEven(): Boolean = this % 2 == 0
            
            // Инфиксная функция
            infix fun Int.times(str: String): String = str.repeat(this)
            
            // Хвостовая рекурсия
            tailrec fun factorial(n: Int, accumulator: Long = 1): Long {
                return if (n <= 1) accumulator
                else factorial(n - 1, n * accumulator)
            }
            
            // Композиция функций
            fun <A, B, C> ((A) -> B).andThen(f: (B) -> C): (A) -> C {
                return { x -> f(this(x)) }
            }
            
            // Встроенная функция
            inline fun measureTime(block: () -> Unit) {
                val start = System.currentTimeMillis()
                block()
                val end = System.currentTimeMillis()
                println("Время: ${'$'}{end - start}мс")
            }
            
            // Операторные функции
            data class Point(val x: Int, val y: Int) {
                operator fun plus(other: Point) = Point(x + other.x, y + other.y)
                operator fun times(scale: Int) = Point(x * scale, y * scale)
                override fun toString(): String = "(${'$'}x, ${'$'}y)"
            }
        """.trimIndent()
    )
}