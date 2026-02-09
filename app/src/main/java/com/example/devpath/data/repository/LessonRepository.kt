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
                    
                    Kotlin — современный язык программирования, разработанный компанией JetBrains.
                    Он полностью совместим с Java и работает на JVM, Android, браузерах и нативных платформах.
                    
                    ## Почему Kotlin?
                    
                    1. **Краткость** — на 40% меньше кода по сравнению с Java
                    2. **Безопасность** — защита от NullPointerException
                    3. **Совместимость** — полная совместимость с Java
                    4. **Функциональный подход** — поддержка функций высшего порядка
                    5. **Мультиплатформенность** — одна кодовая база для разных платформ
                    
                    ## Переменные
                    
                    В Kotlin есть два типа переменных:
                    - `val` — неизменяемая переменная (аналог final в Java)
                    - `var` — изменяемая переменная
                    
                    ```kotlin
                    // Примеры объявления переменных
                    val name = "Kotlin"           // нельзя изменить
                    var age = 5                   // можно изменить
                    val pi: Double = 3.14159      // явное указание типа
                    var isReady = false           // логический тип
                    ```
                    
                    ## Типы данных
                    
                    Kotlin имеет следующие основные типы:
                    - `String` — строки
                    - `Int` — целые числа
                    - `Double` — числа с плавающей точкой
                    - `Boolean` — логические значения
                    - `Char` — одиночный символ
                    - `Float` — числа с плавающей точкой меньшей точности
                    
                    ## Комментарии
                    
                    ```kotlin
                    // Однострочный комментарий
                    
                    /*
                      Многострочный
                      комментарий
                    */
                    
                    /**
                     * Комментарий документации
                     * для генерации документации
                     */
                    ```
                """.trimIndent(),
                codeExample = """
                    fun main() {
                        // Объявление переменных
                        val language = "Kotlin"
                        var version = 1.9
                        val year = 2024
                        
                        println("Я изучаю " + language + " версии " + version)
                        println("Текущий год: " + year)
                        
                        // Попробуй изменить val - будет ошибка!
                        // language = "Java" // Compilation error!
                        
                        // Но var можно изменять
                        version = 2.0
                        println("Новая версия: " + version)
                    }
                """.trimIndent(),
                topic = "kotlin_basics"
            ),
            Lesson(
                id = "variables_types",
                title = "Переменные и типы",
                description = "Глубокое погружение в переменные и типы данных",
                theory = """
                    # Переменные и типы в Kotlin
                    
                    ## Объявление переменных
                    
                    ```kotlin
                    // Неявная типизация (тип выводится автоматически)
                    val message = "Hello, World!"
                    var count = 42
                    val pi = 3.14159
                    var isActive = true
                    
                    // Явная типизация
                    val username: String = "developer"
                    var score: Int = 100
                    val temperature: Double = 25.5
                    var isComplete: Boolean = false
                    ```
                    
                    ## Неизменяемость (immutability)
                    
                    Используйте `val` везде, где возможно:
                    - Безопаснее
                    - Проще для понимания
                    - Потокобезопасно
                    
                    ```kotlin
                    val immutableList = listOf(1, 2, 3)
                    var mutableList = mutableListOf(1, 2, 3)
                    
                    // immutableList.add(4) // Ошибка!
                    mutableList.add(4)      // OK
                    ```
                    
                    ## Null Safety
                    
                    Kotlin защищает от NullPointerException:
                    - `String` — не может быть null
                    - `String?` — может быть null
                    
                    ```kotlin
                    val name: String = "Kotlin"   // всегда не null
                    val nullableName: String? = null  // может быть null
                    
                    // Безопасный вызов
                    val length = nullableName?.length // вернет null если nullableName = null
                    
                    // Оператор Элвиса ?:
                    val safeLength = nullableName?.length ?: 0 // вернет 0 если null
                    
                    // Утверждение не-null !!
                    val forcedLength = nullableName!!.length // бросит исключение если null
                    ```
                    
                    ## Основные типы данных
                    
                    ### Числовые типы
                    - `Byte` (8 бит), `Short` (16 бит), `Int` (32 бита), `Long` (64 бита)
                    - `Float` (32 бита), `Double` (64 бита)
                    
                    ```kotlin
                    val byteValue: Byte = 127
                    val intValue = 100_000_000  // можно использовать подчеркивания
                    val longValue = 100L        // L для Long
                    val floatValue = 3.14f      // f для Float
                    val doubleValue = 3.14159
                    ```
                    
                    ### Строки
                    
```kotlin
val singleLine = "Hello"

// Многострочная строка
val multiLine = ""${'"'}
    Это пример
    многострочной
    строки
""${'"'}.trimIndent()
                    
                    // Шаблонные строки (string template)
                    val name = "Мир"
                    val greeting = "Привет, $${'{'}name${'}'}!" // Используем escape последовательности
                    ```
                    
                    ### Массивы
                    
                    ```kotlin
                    val numbers = arrayOf(1, 2, 3, 4, 5)
                    val strings = arrayOf("A", "B", "C")
                    val intArray = intArrayOf(1, 2, 3) // примитивный массив
                    ```
                """.trimIndent(),
                codeExample = """
                    fun main() {
                        // Разные способы объявления переменных
                        val immutable = "Нельзя изменить"
                        var mutable = "Можно изменить"
                        
                        println("immutable: " + immutable)
                        println("mutable до: " + mutable)
                        
                        mutable = "Новое значение"
                        println("mutable после: " + mutable)
                        
                        // Работа с null safety
                        val notNull: String = "NotNull"
                        var nullable: String? = null
                        
                        println("Длина notNull: " + notNull.length)
                        println("Длина nullable: " + (nullable?.length ?: "null"))
                        
                        // Безопасное преобразование
                        nullable = "Теперь не null"
                        println("Длина nullable после: " + nullable?.length)
                        
                        // Числовые типы
                        val million = 1_000_000
                        val price = 99.99
                        val temperature = 25.5f
                        
                        println("Миллион: " + million)
                        println("Цена: " + price)
                        println("Температура: " + temperature + "°C")
                    }
                """.trimIndent(),
                topic = "kotlin_basics"
            ),
            Lesson(
                id = "functions",
                title = "Функции",
                description = "Создание и использование функций",
                theory = """
                    # Функции в Kotlin
                    
                    Функции объявляются с помощью ключевого слова `fun`.
                    
                    ## Синтаксис
                    
                    ```kotlin
                    fun имяФункции(параметр1: Тип1, параметр2: Тип2): ТипВозврата {
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
                    
                    // Функция с параметром по умолчанию
                    fun greetUser(name: String = "Гость") {
                        println("Привет, " + name + "!")
                    }
                    
                    // Сокращённый синтаксис для однострочных функций
                    fun multiply(a: Int, b: Int) = a * b
                    ```
                    
                    ## Типы функций
                    
                    ### 1. Функции-члены (Member functions)
                    ```kotlin
                    class Calculator {
                        fun add(a: Int, b: Int): Int = a + b
                    }
                    ```
                    
                    ### 2. Локальные функции
                    ```kotlin
                    fun outer() {
                        fun inner() {
                            println("Внутренняя функция")
                        }
                        inner()
                    }
                    ```
                    
                    ### 3. Функции высшего порядка
                    ```kotlin
                    fun calculate(a: Int, b: Int, operation: (Int, Int) -> Int): Int {
                        return operation(a, b)
                    }
                    ```
                    
                    ### 4. Расширяющие функции (Extension functions)
                    ```kotlin
                    fun String.addExclamation(): String = this + "!"
                    ```
                    
                    ## Параметры функций
                    
                    ### Именованные параметры
                    ```kotlin
                    fun createUser(name: String, age: Int, email: String) { ... }
                    
                    // Вызов с именованными параметрами
                    createUser(name = "John", age = 25, email = "john@example.com")
                    ```
                    
                    ### Параметры по умолчанию
                    ```kotlin
                    fun sendMessage(
                        text: String,
                        recipient: String = "all",
                        priority: Int = 1
                    ) { ... }
                    
                    sendMessage("Hello") // recipient = "all", priority = 1
                    ```
                    
                    ### Переменное количество параметров (vararg)
                    ```kotlin
                    fun sum(vararg numbers: Int): Int {
                        return numbers.sum()
                    }
                    
                    sum(1, 2, 3, 4, 5) // 15
                    ```
                    
                    ## Рекурсивные функции
                    
                    ```kotlin
                    fun factorial(n: Int): Long {
                        return if (n == 0) 1 else n * factorial(n - 1)
                    }
                    ```
                    
                    ## Инфиксные функции
                    
                    ```kotlin
                    infix fun Int.times(str: String) = str.repeat(this)
                    
                    val result = 3 times "Hello" // "HelloHelloHello"
                    ```
                    
                    ## Встроенные функции (inline)
                    
                    ```kotlin
                    inline fun measureTime(block: () -> Unit) {
                        val start = System.currentTimeMillis()
                        block()
                        val end = System.currentTimeMillis()
                        println("Время выполнения: " + (end - start) + "ms")
                    }
                    ```
                """.trimIndent(),
                codeExample = """
                    fun main() {
                        // Простые функции
                        greet()
                        
                        val sum = add(10, 20)
                        println("10 + 20 = " + sum)
                        
                        val product = multiply(5, 6)
                        println("5 * 6 = " + product)
                        
                        // Функции с параметрами по умолчанию
                        greetUser()
                        greetUser("Алексей")
                        
                        // Переменное количество параметров
                        val total = sumNumbers(1, 2, 3, 4, 5)
                        println("Сумма чисел: " + total)
                        
                        // Рекурсивная функция
                        val fact = factorial(5)
                        println("5! = " + fact)
                    }
                    
                    fun greet() {
                        println("Добро пожаловать в мир Kotlin!")
                    }
                    
                    fun add(a: Int, b: Int): Int {
                        return a + b
                    }
                    
                    fun multiply(a: Int, b: Int) = a * b
                    
                    fun greetUser(name: String = "Гость") {
                        println("Приветствуем, " + name + "!")
                    }
                    
                    fun sumNumbers(vararg numbers: Int): Int {
                        var result = 0
                        for (num in numbers) {
                            result += num
                        }
                        return result
                    }
                    
                    fun factorial(n: Int): Long {
                        return if (n <= 1) 1 else n * factorial(n - 1)
                    }
                """.trimIndent(),
                topic = "functions"
            ),
            Lesson(
                id = "control_flow",
                title = "Управляющие конструкции",
                description = "Условные операторы и циклы",
                theory = """
                    # Управляющие конструкции в Kotlin
                    
                    Kotlin предоставляет стандартные управляющие конструкции с некоторыми улучшениями.
                    
                    ## Условные выражения
                    
                    ### 1. if-else
                    ```kotlin
                    val max = if (a > b) {
                        println("a больше")
                        a
                    } else {
                        println("b больше или равно")
                        b
                    }
                    ```
                    
                    ### 2. when (аналог switch)
                    ```kotlin
                    when (x) {
                        1 -> println("x == 1")
                        2, 3 -> println("x == 2 или 3")
                        in 4..10 -> println("x между 4 и 10")
                        else -> println("другое значение")
                    }
                    ```
                    
                    ## Циклы
                    
                    ### 1. for
                    ```kotlin
                    // Диапазон
                    for (i in 1..5) { ... }
                    
                    // Обратный порядок
                    for (i in 5 downTo 1) { ... }
                    
                    // С шагом
                    for (i in 1..10 step 2) { ... }
                    
                    // По коллекциям
                    for (item in collection) { ... }
                    
                    // С индексом
                    for ((index, value) in array.withIndex()) { ... }
                    ```
                    
                    ### 2. while и do-while
                    ```kotlin
                    while (condition) { ... }
                    
                    do {
                        ...
                    } while (condition)
                    ```
                    
                    ## Ключевые слова break и continue
                    
                    ```kotlin
                    loop@ for (i in 1..10) {
                        for (j in 1..10) {
                            if (i * j > 50) break@loop
                            println("i * j = " + (i * j))
                        }
                    }
                    ```
                    
                    ## Возврат значений
                    
                    ### 1. return
                    ```kotlin
                    fun findFirstEven(numbers: List<Int>): Int? {
                        for (num in numbers) {
                            if (num % 2 == 0) return num
                        }
                        return null
                    }
                    ```
                    
                    ### 2. return@label
                    ```kotlin
                    fun processList(list: List<Int>) {
                        list.forEach {
                            if (it < 0) return@forEach  // продолжает цикл
                            println(it)
                        }
                    }
                    ```
                    
                    ## Исключения
                    
                    ```kotlin
                    try {
                        // Код, который может выбросить исключение
                        val result = 10 / 0
                    } catch (e: ArithmeticException) {
                        println("Деление на ноль!")
                    } finally {
                        println("Этот блок выполнится всегда")
                    }
                    
                    // Выражение try-catch
                    val number = try {
                        "123".toInt()
                    } catch (e: NumberFormatException) {
                        0
                    }
                    ```
                    
                    ## Проверки (Checks)
                    
                    ```kotlin
                    fun processUser(user: User?) {
                        require(user != null) { "Пользователь не может быть null" }
                        check(user.age >= 0) { "Возраст не может быть отрицательным" }
                        assert(user.name.isNotEmpty()) { "Имя не может быть пустым" }
                    }
                    ```
                    
                    ## Улучшенный when
                    
                    ```kotlin
                    when {
                        x.isOdd() -> print("x нечетное")
                        x.isEven() -> print("x четное")
                        else -> print("x не число")
                    }
                    
                    // when как выражение
                    val description = when (number) {
                        0 -> "ноль"
                        in 1..10 -> "от одного до десяти"
                        else -> "другое число"
                    }
                    ```
                """.trimIndent(),
                codeExample = """
                    fun main() {
                        // Пример if-else как выражения
                        val a = 10
                        val b = 20
                        val max = if (a > b) a else b
                        println("Максимальное число: " + max)
                        
                        // Пример when
                        val grade = 85
                        val result = when {
                            grade >= 90 -> "Отлично"
                            grade >= 75 -> "Хорошо"
                            grade >= 60 -> "Удовлетворительно"
                            else -> "Неудовлетворительно"
                        }
                        println("Оценка: " + result)
                        
                        // Циклы
                        println("
    Цикл for:")
                        for (i in 1..5) {
                            println("i = " + i)
                        }
                        
                        println("
    Цикл while:")
                        var counter = 5
                        while (counter > 0) {
                            println("Осталось: " + counter)
                            counter--
                        }
                        
                        // Обработка исключений
                        println("
    Обработка исключений:")
                        try {
                            val division = 10 / 0
                        } catch (e: ArithmeticException) {
                            println("Поймано исключение: " + e.message)
                        } finally {
                            println("Блок finally выполнен")
                        }
                        
                        // Использование break с меткой
                        println("
    Использование break с меткой:")
                        outer@ for (i in 1..3) {
                            for (j in 1..3) {
                                if (i * j > 4) {
                                    println("Прерываем при i=" + i + ", j=" + j)
                                    break@outer
                                }
                                println("i=" + i + ", j=" + j)
                            }
                        }
                    }
                    
                    fun Int.isEven() = this % 2 == 0
                    fun Int.isOdd() = !this.isEven()
                """.trimIndent(),
                topic = "control_flow"
            ),
            Lesson(
                id = "collections",
                title = "Коллекции",
                description = "Работа с коллекциями данных",
                theory = """
                    # Коллекции в Kotlin
                    
                    Kotlin предоставляет богатую библиотеку коллекций, которая включает как изменяемые, так и неизменяемые коллекции.
                    
                    ## Основные типы коллекций
                    
                    ### 1. List (Список)
                    ```kotlin
                    // Неизменяемый список
                    val immutableList = listOf(1, 2, 3, 4, 5)
                    
                    // Изменяемый список
                    val mutableList = mutableListOf("A", "B", "C")
                    mutableList.add("D")
                    ```
                    
                    ### 2. Set (Множество)
                    ```kotlin
                    // Неизменяемое множество (уникальные элементы)
                    val immutableSet = setOf(1, 2, 3, 2, 1) // [1, 2, 3]
                    
                    // Изменяемое множество
                    val mutableSet = mutableSetOf("apple", "banana")
                    ```
                    
                    ### 3. Map (Словарь)
                    ```kotlin
                    // Неизменяемый словарь
                    val immutableMap = mapOf(
                        "name" to "John",
                        "age" to 25
                    )
                    
                    // Изменяемый словарь
                    val mutableMap = mutableMapOf<String, Int>()
                    mutableMap["key"] = 100
                    ```
                    
                    ## Функциональные операции
                    
                    ### 1. Трансформация
                    ```kotlin
                    val numbers = listOf(1, 2, 3, 4, 5)
                    
                    // map - преобразование каждого элемента
                    val squares = numbers.map { it * it } // [1, 4, 9, 16, 25]
                    
                    // flatMap - преобразование и сглаживание
                    val flat = numbers.flatMap { listOf(it, it * 10) }
                    
                    // filter - фильтрация
                    val evens = numbers.filter { it % 2 == 0 } // [2, 4]
                    
                    // filterNot - обратная фильтрация
                    val odds = numbers.filterNot { it % 2 == 0 } // [1, 3, 5]
                    ```
                    
                    ### 2. Агрегация
                    ```kotlin
                    // sum - сумма
                    val total = numbers.sum() // 15
                    
                    // average - среднее
                    val avg = numbers.average() // 3.0
                    
                    // min/max - минимум/максимум
                    val min = numbers.minOrNull()
                    val max = numbers.maxOrNull()
                    
                    // reduce - сворачивание
                    val product = numbers.reduce { acc, i -> acc * i } // 120
                    
                    // fold - сворачивание с начальным значением
                    val sumWithInitial = numbers.fold(10) { acc, i -> acc + i } // 25
                    ```
                    
                    ### 3. Поиск
                    ```kotlin
                    // find - поиск первого элемента
                    val firstEven = numbers.find { it % 2 == 0 } // 2
                    
                    // first/last - первый/последний элемент
                    val first = numbers.first()
                    val last = numbers.last()
                    
                    // elementAt - элемент по индексу
                    val third = numbers.elementAt(2) // 3
                    
                    // any - проверка наличия хотя бы одного
                    val hasEven = numbers.any { it % 2 == 0 } // true
                    
                    // all - проверка всех элементов
                    val allPositive = numbers.all { it > 0 } // true
                    
                    // none - проверка отсутствия
                    val noNegative = numbers.none { it < 0 } // true
                    ```
                    
                    ### 4. Группировка и сортировка
                    ```kotlin
                    // groupBy - группировка
                    val grouped = listOf("apple", "banana", "avocado")
                        .groupBy { it.first() } // {'a': ['apple', 'avocado'], 'b': ['banana']}
                    
                    // sorted - сортировка
                    val sorted = numbers.sorted() // по возрастанию
                    val sortedDesc = numbers.sortedDescending() // по убыванию
                    
                    // sortedBy - сортировка по ключу
                    val words = listOf("one", "two", "three")
                    val sortedByLength = words.sortedBy { it.length } // ['one', 'two', 'three']
                    ```
                    
                    ## Последовательности (Sequences)
                    
                    Последовательности ленивы и обрабатывают элементы по одному:
                    ```kotlin
                    val sequence = sequenceOf(1, 2, 3, 4, 5)
                    val result = sequence
                        .map { it * it }
                        .filter { it > 10 }
                        .toList() // [16, 25]
                    ```
                    
                    ## Потоки (Flow)
                    
                    Асинхронные потоки данных (часть Coroutines):
                    ```kotlin
                    fun getNumbers(): Flow<Int> = flow {
                        for (i in 1..5) {
                            delay(100)
                            emit(i)
                        }
                    }
                    ```
                    
                    ## Создание коллекций
                    
                    ```kotlin
                    // Пустые коллекции
                    val emptyList = emptyList<String>()
                    val emptySet = emptySet<Int>()
                    val emptyMap = emptyMap<String, Any>()
                    
                    // Коллекции с одним элементом
                    val singletonList = listOf("single")
                    val singletonSet = setOf(42)
                    val singletonMap = mapOf("key" to "value")
                    
                    // Динамическое создание
                    val generatedList = List(5) { index -> index * 2 } // [0, 2, 4, 6, 8]
                    val generatedMap = buildMap {
                        put("name", "John")
                        put("age", 25)
                    }
                    ```
                """.trimIndent(),
                codeExample = """
                    fun main() {
                        // Создание коллекций
                        val numbers = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                        val names = mutableListOf("Анна", "Борис", "Владимир", "Дарья")
                        
                        println("Исходный список чисел: " + numbers)
                        println("Исходный список имен: " + names)
                        
                        // Трансформации
                        val squares = numbers.map { it * it }
                        println("
    Квадраты чисел: " + squares)
                        
                        val evenNumbers = numbers.filter { it % 2 == 0 }
                        println("Четные числа: " + evenNumbers)
                        
                        // Агрегация
                        val sum = numbers.sum()
                        val average = numbers.average()
                        val max = numbers.maxOrNull()
                        
                        println("
    Сумма: " + sum)
                        println("Среднее: " + average)
                        println("Максимум: " + max)
                        
                        // Группировка
                        val groupedByLength = names.groupBy { it.length }
                        println("
    Имена сгруппированы по длине:")
                        for ((length, nameList) in groupedByLength) {
                            println("  Длина " + length + ": " + nameList)
                        }
                        
                        // Сортировка
                        val sortedNames = names.sorted()
                        println("
    Отсортированные имена: " + sortedNames)
                        
                        val sortedByLength = names.sortedBy { it.length }
                        println("Имена отсортированы по длине: " + sortedByLength)
                        
                        // Работа с Map
                        val userMap = mapOf(
                            "id" to 1,
                            "name" to "Алексей",
                            "age" to 30,
                            "city" to "Москва"
                        )
                        
                        println("
    Информация о пользователе:")
                        for ((key, value) in userMap) {
                            println("  " + key + ": " + value)
                        }
                        
                        // Поиск
                        val hasLongName = names.any { it.length > 6 }
                        val allNamesValid = names.all { it.isNotEmpty() }
                        val firstLongName = names.find { it.length > 5 }
                        
                        println("
    Есть ли длинные имена? " + hasLongName)
                        println("Все имена валидны? " + allNamesValid)
                        println("Первое длинное имя: " + (firstLongName ?: "не найдено"))
                        
                        // Изменяемые операции
                        names.add("Екатерина")
                        names.remove("Борис")
                        names[0] = "Анна Ивановна"
                        
                        println("
    Измененный список имен: " + names)
                        
                        // Последовательности (ленивые вычисления)
                        val sequenceResult = numbers.asSequence()
                            .filter { it > 5 }
                            .map { it * 10 }
                            .toList()
                        
                        println("
    Результат работы с последовательностью: " + sequenceResult)
                    }
                """.trimIndent(),
                topic = "collections"
            ),
            Lesson(
                id = "oop_kotlin",
                title = "ООП в Kotlin",
                description = "Объектно-ориентированное программирование",
                theory = """
                    # Объектно-ориентированное программирование в Kotlin
                    
                    Kotlin полностью поддерживает ООП с некоторыми улучшениями по сравнению с Java.
                    
                    ## Классы
                    
                    ### Объявление класса
                    ```kotlin
                    class Person {
                        // свойства
                        var name: String = ""
                        var age: Int = 0
                        
                        // методы
                        fun speak() {
                            println("Привет, меня зовут " + name)
                        }
                    }
                    ```
                    
                    ### Первичный конструктор
                    ```kotlin
                    class Person(val name: String, var age: Int) {
                        init {
                            println("Создан человек: " + name)
                        }
                    }
                    
                    // Создание объекта
                    val person = Person("Иван", 25)
                    ```
                    
                    ### Вторичный конструктор
                    ```kotlin
                    class Person(val name: String) {
                        var age: Int = 0
                        
                        constructor(name: String, age: Int) : this(name) {
                            this.age = age
                        }
                    }
                    ```
                    
                    ## Наследование
                    
                    ### Открытые классы
                    ```kotlin
                    open class Animal(val name: String) {
                        open fun makeSound() {
                            println("Животное издает звук")
                        }
                    }
                    
                    class Dog(name: String) : Animal(name) {
                        override fun makeSound() {
                            println("Гав-гав!")
                        }
                    }
                    ```
                    
                    ### Абстрактные классы
                    ```kotlin
                    abstract class Shape {
                        abstract fun area(): Double
                        
                        fun description() {
                            println("Это фигура")
                        }
                    }
                    
                    class Circle(val radius: Double) : Shape() {
                        override fun area(): Double = Math.PI * radius * radius
                    }
                    ```
                    
                    ## Интерфейсы
                    
                    ```kotlin
                    interface Drawable {
                        fun draw()
                        
                        // Методы с реализацией по умолчанию
                        fun drawOutline() {
                            println("Рисую контур")
                        }
                    }
                    
                    interface Movable {
                        fun move(dx: Int, dy: Int)
                    }
                    
                    class Rectangle : Drawable, Movable {
                        override fun draw() {
                            println("Рисую прямоугольник")
                        }
                        
                        override fun move(dx: Int, dy: Int) {
                            println("Перемещаю на (" + dx + ", " + dy + ")")
                        }
                    }
                    ```
                    
                    ## Data классы
                    
                    Автоматически генерируют:
                    - `equals()` и `hashCode()`
                    - `toString()`
                    - `copy()`
                    - `componentN()` функции
                    
                    ```kotlin
                    data class User(
                        val id: Int,
                        val name: String,
                        val email: String
                    )
                    
                    val user1 = User(1, "John", "john@example.com")
                    val user2 = user1.copy(name = "Jane")
                    val (id, name, email) = user1 // деструктуризация
                    ```
                    
                    ## Enum классы
                    
                    ```kotlin
                    enum class Color(val rgb: Int) {
                        RED(0xFF0000),
                        GREEN(0x00FF00),
                        BLUE(0x0000FF);
                        
                        fun containsRed(): Boolean = this.rgb and 0xFF0000 != 0
                    }
                    ```
                    
                    ## Объекты
                    
                    ### Синглтоны (object declaration)
                    ```kotlin
                    object Database {
                        init {
                            println("База данных инициализирована")
                        }
                        
                        fun connect() { ... }
                        fun disconnect() { ... }
                    }
                    
                    // Использование
                    Database.connect()
                    ```
                    
                    ### Companion объекты
                    ```kotlin
                    class MyClass {
                        companion object {
                            const val CONSTANT = "Значение"
                            
                            fun create(): MyClass = MyClass()
                        }
                    }
                    
                    // Вызов
                    val obj = MyClass.create()
                    val const = MyClass.CONSTANT
                    ```
                    
                    ### Object выражения (анонимные объекты)
                    ```kotlin
                    val clickListener = object : ClickListener {
                        override fun onClick() {
                            println("Клик!")
                        }
                    }
                    ```
                    
                    ## Модификаторы доступа
                    
                    - `public` (по умолчанию) - виден везде
                    - `private` - виден только внутри класса/файла
                    - `protected` - виден в классе и наследниках
                    - `internal` - виден внутри модуля
                    
                    ## Делегирование
                    
                    ### Делегирование свойств
                    ```kotlin
                    class Example {
                        var p: String by Delegate()
                    }
                    
                    class Delegate {
                        operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
                            return property.name + " получено"
                        }
                        
                        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
                            println(property.name + " установлено в " + value)
                        }
                    }
                    ```
                    
                    ### Делегирование реализации
                    ```kotlin
                    interface Base {
                        fun print()
                    }
                    
                    class BaseImpl(val x: Int) : Base {
                        override fun print() { print(x) }
                    }
                    
                    class Derived(b: Base) : Base by b
                    ```
                    
                    ## Вложенные и внутренние классы
                    
                    ```kotlin
                    class Outer {
                        private val bar: Int = 1
                        
                        // Вложенный класс (не имеет доступа к bar)
                        class Nested {
                            fun foo() = 2
                        }
                        
                        // Внутренний класс (имеет доступ к bar)
                        inner class Inner {
                            fun foo() = bar
                        }
                    }
                    ```
                    
                    ## Свойства (Properties)
                    
                    ```kotlin
                    class Rectangle {
                        var width: Int = 0
                        var height: Int = 0
                        
                        // Вычисляемое свойство
                        val area: Int
                            get() = width * height
                        
                        // Свойство с сеттером
                        var square: Boolean = false
                            set(value) {
                                field = value
                                if (value) {
                                    height = width
                                }
                            }
                    }
                    ```
                """.trimIndent(),
                codeExample = """
                    fun main() {
                        // Создание объектов
                        val person = Person("Алексей", 30)
                        person.speak()
                        println("Возраст: " + person.age)
                        
                        // Наследование
                        val dog = Dog("Бобик")
                        dog.makeSound()
                        
                        // Data классы
                        val user1 = User(1, "Иван", "ivan@example.com")
                        val user2 = user1.copy(name = "Петр")
                        
                        println("
    user1: " + user1)
                        println("user2: " + user2)
                        println("user1 == user2: " + (user1 == user2))
                        
                        // Деструктуризация
                        val (id, name, email) = user1
                        println("
    Деструктуризация:")
                        println("ID: " + id)
                        println("Имя: " + name)
                        println("Email: " + email)
                        
                        // Enum классы
                        val color = Color.RED
                        println("
    Цвет: " + color)
                        println("Содержит красный? " + color.containsRed())
                        
                        // Объекты (синглтоны)
                        DatabaseManager.connect()
                        DatabaseManager.query("SELECT * FROM users")
                        
                        // Интерфейсы
                        val rect = Rectangle()
                        rect.draw()
                        rect.move(10, 20)
                        
                        // Вычисляемые свойства
                        val rectangle = RectangleShape()
                        rectangle.width = 10
                        rectangle.height = 5
                        println("
    Площадь прямоугольника: " + rectangle.area)
                        
                        rectangle.square = true
                        println("После установки square=true:")
                        println("Ширина: " + rectangle.width + ", Высота: " + rectangle.height)
                        println("Площадь: " + rectangle.area)
                    }
                    
                    // Базовый класс
                    open class Person(val name: String, var age: Int) {
                        open fun speak() {
                            println("Привет, меня зовут " + name + ", мне " + age + " лет")
                        }
                    }
                    
                    // Наследование
                    open class Animal(val name: String) {
                        open fun makeSound() {
                            println("Животное издает звук")
                        }
                    }
                    
                    class Dog(name: String) : Animal(name) {
                        override fun makeSound() {
                            println(name + " говорит: Гав-гав!")
                        }
                    }
                    
                    // Data класс
                    data class User(
                        val id: Int,
                        val name: String,
                        val email: String
                    )
                    
                    // Enum класс
                    enum class Color(val rgb: Int) {
                        RED(0xFF0000),
                        GREEN(0x00FF00),
                        BLUE(0x0000FF);
                        
                        fun containsRed(): Boolean = (this.rgb and 0xFF0000) != 0
                    }
                    
                    // Объект (синглтон)
                    object DatabaseManager {
                        fun connect() {
                            println("Подключение к базе данных...")
                        }
                        
                        fun disconnect() {
                            println("Отключение от базы данных...")
                        }
                        
                        fun query(sql: String) {
                            println("Выполнение запроса: " + sql)
                        }
                    }
                    
                    // Интерфейсы
                    interface Drawable {
                        fun draw()
                    }
                    
                    interface Movable {
                        fun move(dx: Int, dy: Int)
                    }
                    
                    class Rectangle : Drawable, Movable {
                        override fun draw() {
                            println("Рисую прямоугольник")
                        }
                        
                        override fun move(dx: Int, dy: Int) {
                            println("Перемещаю прямоугольник на (" + dx + ", " + dy + ")")
                        }
                    }
                    
                    // Свойства с getter/setter
                    class RectangleShape {
                        var width: Int = 0
                        var height: Int = 0
                        
                        val area: Int
                            get() = width * height
                        
                        var square: Boolean = false
                            set(value) {
                                field = value
                                if (value) {
                                    height = width
                                    println("Установлена высота равной ширине")
                                }
                            }
                    }
                """.trimIndent(),
                topic = "classes"
            ),
            Lesson(
                id = "null_safety",
                title = "Безопасность null",
                description = "Работа с nullable типами и безопасные вызовы",
                theory = """
        # Null Safety в Kotlin
        
        Одной из ключевых особенностей Kotlin является система null safety, которая помогает избежать NullPointerException.
        
        ## Nullable и Non-Null типы
        
        По умолчанию все типы в Kotlin являются non-null:
        ```kotlin
        var name: String = "Kotlin"  // не может быть null
        // name = null               // Ошибка компиляции!
        ```
        
        Чтобы разрешить null, нужно использовать `?`:
        ```kotlin
        var nullableName: String? = "Kotlin"
        nullableName = null          // OK
        ```
        
        ## Безопасные вызовы (Safe Calls)
        
        ```kotlin
        val length: Int? = nullableName?.length
        // Если nullableName = null, то length = null
        // Если nullableName != null, то length = nullableName.length
        ```
        
        Цепочка безопасных вызовов:
        ```kotlin
        val street: String? = user?.address?.street
        // Вернет null если user или address или street = null
        ```
        
        ## Оператор Элвиса (Elvis Operator) ?:
        
        ```kotlin
        val length = nullableName?.length ?: 0
        // Если nullableName = null, вернет 0
        // Иначе вернет nullableName.length
        ```
        
        Также можно использовать с throw или return:
        ```kotlin
        val name = nullableName ?: throw IllegalArgumentException("Имя обязательно")
        val length = nullableName?.length ?: return
        ```
        
        ## Утверждение не-null (!! Operator)
        
        Используйте только когда уверены, что значение не null:
        ```kotlin
        val length = nullableName!!.length
        // Если nullableName = null, выбросит NullPointerException
        ```
        
        ## Безопасное приведение типов (Safe Cast)
        
        ```kotlin
        val obj: Any = "Hello"
        val str: String? = obj as? String  // безопасное приведение
        val num: Int? = obj as? Int       // вернет null вместо исключения
        ```
        
        ## Коллекции и null safety
        
        ```kotlin
        val list: List<Int?> = listOf(1, 2, null, 4)
        val nonNullList: List<Int> = list.filterNotNull()
        // nonNullList = [1, 2, 4]
        ```
        
        ## Функции с nullable параметрами
        
        ```kotlin
        fun printLength(str: String?) {
            val length = str?.length ?: "null"
            println("Длина: $${'{'}length${'}'}")
        }
        ```
        
        ## Расширения для nullable типов
        
        ```kotlin
        fun String?.isNullOrEmptyOrBlank(): Boolean {
            return this == null || this.isBlank()
        }
        
        val result = nullableString.isNullOrEmptyOrBlank()
        ```
        
        ## Поздняя инициализация (lateinit)
        
        Для случаев, когда переменная не может быть инициализирована сразу:
        ```kotlin
        class MyClass {
            lateinit var name: String  // не может быть nullable!
            
            fun initialize() {
                name = "Kotlin"  // должна быть инициализирована до использования
            }
            
            fun printName() {
                if (::name.isInitialized) {
                    println(name)
                }
            }
        }
        ```
        
        ## Делегаты для nullable свойств
        
        ```kotlin
        import kotlin.properties.Delegates
        
        class User {
            var name: String? by Delegates.observable(null) { 
                prop, old, new ->
                println("Имя изменилось с $${'{'}old${'}'} на $${'{'}new${'}'}")
            }
        }
        ```
        
        ## Проверки в условиях
        
        Компилятор Kotlin запоминает проверки на null:
        ```kotlin
        fun processString(str: String?) {
            if (str != null) {
                // здесь str автоматически преобразуется в String
                println(str.length)  // безопасно!
            }
        }
        ```
        
        ## Платформенные типы
        
        При работе с Java кодом:
        ```kotlin
        // Java метод: public String getName() { ... }
        val name = javaObject.name  // тип String! (платформенный тип)
        // Может быть как String, так и String?
        ```
        
        ## Аннотации для Java взаимодействия
        
        ```kotlin
        // Kotlin
        fun process(@NotNull name: String) { ... }
        
        // Или с использованием аннотаций JetBrains
        import org.jetbrains.annotations.NotNull
        import org.jetbrains.annotations.Nullable
        ```
    """.trimIndent(),
                codeExample = """
        fun main() {
            // Примеры nullable типов
            var nullableString: String? = "Привет, Kotlin!"
            var definitelyString: String = "Не может быть null"
            
            println("nullableString: " + nullableString)
            println("definitelyString: " + definitelyString)
            
            // Безопасные вызовы
            val length1 = nullableString?.length
            println("Длина nullableString: " + length1)
            
            nullableString = null
            val length2 = nullableString?.length
            println("Длина после установки null: " + length2)
            
            // Оператор Элвиса
            val safeLength = nullableString?.length ?: 0
            println("Безопасная длина: " + safeLength)
            
            // Безопасное приведение типов
            val anyValue: Any = 123
            val stringValue = anyValue as? String
            val intValue = anyValue as? Int
            
            println("
    Безопасное приведение:")
            println("String: " + stringValue)  // null
            println("Int: " + intValue)        // 123
            
            // Работа с коллекциями
            val mixedList: List<Int?> = listOf(1, null, 3, null, 5)
            println("
    Исходный список: " + mixedList)
            
            val nonNullList = mixedList.filterNotNull()
            println("Список без null: " + nonNullList)
            
            val sum = mixedList.filterNotNull().sum()
            println("Сумма не-null элементов: " + sum)
            
            // Проверки в условиях
            val maybeString: String? = "Проверка"
            println("
    Проверки в условиях:")
            
            if (maybeString != null) {
                // Компилятор знает, что maybeString не null здесь
                println("Длина строки: " + maybeString.length)
            }
            
            // when с проверкой на null
            when (maybeString) {
                null -> println("Строка равна null")
                else -> println("Строка не null: " + maybeString)
            }
            
            // Поздняя инициализация
            val processor = DataProcessor()
            // processor.process() // Ошибка: lateinit property data не инициализирована
            
            processor.initialize("Важные данные")
            processor.process()
            
            // Цепочка безопасных вызовов
            val user: User? = User("Алексей", Address("Москва", "Тверская"))
            val city = user?.address?.city
            println("
    Город пользователя: " + (city ?: "не указан"))
            
            user?.address = null
            val street = user?.address?.street ?: "адрес не указан"
            println("Улица после null: " + street)
            
            // Делегаты для nullable свойств
            val config = Configuration()
            config.setting = "Значение 1"
            config.setting = "Значение 2"
            config.setting = null
        }
        
        // Классы для примеров
        class DataProcessor {
            lateinit var data: String
            
            fun initialize(value: String) {
                data = value
            }
            
            fun process() {
                if (::data.isInitialized) {
                    println("Обработка данных: " + data)
                } else {
                    println("Данные не инициализированы")
                }
            }
        }
        
        data class Address(val city: String?, val street: String?)
        data class User(val name: String, var address: Address?)
        
        class Configuration {
            var setting: String? by Delegates.observable(null) { prop, old, new ->
                println("Настройка изменилась: $${'{'}old${'}'} -> $${'{'}new${'}'}")
            }
        }
        
        // Расширение для nullable строк
        fun String?.customLength(default: Int = -1): Int {
            return this?.length ?: default
        }
    """.trimIndent(),
                topic = "null_safety"
            ),
            Lesson(
                id = "extensions",
                title = "Функции и свойства расширения",
                description = "Расширение существующих классов без наследования",
                theory = """
        # Функции и свойства расширения в Kotlin
        
        Расширения позволяют добавлять новый функционал к существующим классам без наследования.
        
        ## Функции расширения
        
        ### Синтаксис
        ```kotlin
        fun Класс.имяФункции(параметры): ТипВозврата {
            // this ссылается на объект, для которого вызвана функция
        }
        ```
        
        ### Примеры
        ```kotlin
        // Добавляем функцию к String
        fun String.addExclamation(): String {
            return this + "!"
        }
        
        val greeting = "Hello".addExclamation() // "Hello!"
        
        // Добавляем функцию к Int
        fun Int.isEven(): Boolean = this % 2 == 0
        println(4.isEven()) // true
        ```
        
        ### Расширения с nullable получателем
        ```kotlin
        fun String?.safeLength(default: Int = 0): Int {
            return this?.length ?: default
        }
        
        val length1: Int = "Hello".safeLength()  // 5
        val length2: Int = null.safeLength()     // 0
        val length3: Int = null.safeLength(-1)   // -1
        ```
        
        ## Свойства расширения
        
        Можно добавлять вычисляемые свойства:
        ```kotlin
        val String.hasLetters: Boolean
            get() = this.any { it.isLetter() }
        
        val String.lastChar: Char?
            get() = this.lastOrNull()
        
        // Использование
        println("123".hasLetters)  // false
        println("abc".lastChar)    // 'c'
        ```
        
        ## Расширения для стандартных классов
        
        ### Коллекции
        ```kotlin
        fun <T> List<T>.secondOrNull(): T? = this.getOrNull(1)
        
        val list = listOf(1, 2, 3)
        println(list.secondOrNull()) // 2
        ```
        
        ### String
        ```kotlin
        fun String.toTitleCase(): String {
            return this.split(" ").joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercase() }
            }
        }
        
        println("hello world".toTitleCase()) // "Hello World"
        ```
        
        ## Обобщенные расширения
        
        ```kotlin
        fun <T> List<T>.penultimate(): T? {
            if (this.size < 2) return null
            return this[this.size - 2]
        }
        
        fun <T : Comparable<T>> List<T>.sortedDesc(): List<T> {
            return this.sortedDescending()
        }
        ```
        
        ## Расширения как функции высшего порядка
        
        ```kotlin
        inline fun <T> T.applyIf(condition: Boolean, block: T.() -> Unit): T {
            if (condition) {
                block()
            }
            return this
        }
        
        val result = "Hello".applyIf(true) {
            println(this) // "Hello"
        }
        ```
        
        ## Область видимости расширений
        
        Расширения нужно импортировать:
        ```kotlin
        // File: StringExtensions.kt
        package com.example.extensions
        
        fun String.customFunction() { ... }
        
        // В другом файле
        import com.example.extensions.customFunction
        // или
        import com.example.extensions.*
        
        "test".customFunction()
        ```
        
        ## Расширения-приемники
        
        ### Контекстуальные приемники
        ```kotlin
        class StringContext {
            val prefix = ">> "
        }
        
        context(StringContext)
        fun String.withPrefix(): String = prefix + this
        
        val context = StringContext()
        with(context) {
            println("Hello".withPrefix()) // ">> Hello"
        }
        ```
        
        ### Несколько приемников
        ```kotlin
        class Logger {
            fun log(message: String) = println("[LOG] $${'{'}message${'}'}")
        }
        
        class Formatter {
            fun format(text: String) = text.uppercase()
        }
        
        context(Logger, Formatter)
        fun String.process() {
            log(format(this))
        }
        ```
        
        ## Инфиксные расширения
        
        ```kotlin
        infix fun String.repeatTimes(n: Int): String {
            return this.repeat(n)
        }
        
        val result = "Hi " repeatTimes 3 // "Hi Hi Hi "
        ```
        
        ## Расширения для компаньон-объектов
        
        ```kotlin
        class MyClass {
            companion object
        }
        
        fun MyClass.Companion.create(): MyClass = MyClass()
        
        val obj = MyClass.create()
        ```
        
        ## Ограничения расширений
        
        1. Не могут иметь backing field
        2. Не могут переопределять существующие члены класса
        3. Разрешение статическое (не виртуальное)
        4. Не имеют доступа к protected членам
        
        ## Практическое применение
        
        ### DSL (Domain Specific Language)
        ```kotlin
        fun html(init: HTML.() -> Unit): HTML {
            val html = HTML()
            html.init()
            return html
        }
        
        class HTML {
            fun body(init: Body.() -> Unit) { ... }
        }
        
        class Body {
            fun p(text: String) { ... }
        }
        
        // Использование
        html {
            body {
                p("Hello World")
            }
        }
        ```
        
        ### Утилиты
        ```kotlin
        // Вместо утилитных классов
        object StringUtils {
            fun capitalize(str: String): String { ... }
        }
        
        // Лучше использовать расширения
        fun String.capitalize(): String { ... }
        ```
    """.trimIndent(),
                codeExample = """
        fun main() {
            // Демонстрация функций расширения
            
            // 1. Расширения для String
            println("=== Расширения для String ===")
            val text = "kotlin programming"
            
            println("Оригинал: " + text)
            println("В TitleCase: " + text.toTitleCase())
            println("С восклицанием: " + text.addExclamation())
            println("Последний символ: " + text.lastChar)
            println("Содержит буквы: " + text.hasLetters)
            
            val emptyText = ""
            println("
    Пустая строка:")
            println("Последний символ: " + emptyText.lastChar)
            println("Содержит буквы: " + emptyText.hasLetters)
            
            // 2. Расширения для Int
            println("
    === Расширения для Int ===")
            val number = 42
            
            println("Число: " + number)
            println("Четное: " + number.isEven())
            println("Нечетное: " + number.isOdd())
            println("В квадрате: " + number.squared())
            println("В степени 3: " + number.pow(3))
            println("Факториал 5: " + 5.factorial())
            
            // 3. Расширения для коллекций
            println("
    === Расширения для коллекций ===")
            val numbers = listOf(1, 2, 3, 4, 5)
            
            println("Список: " + numbers)
            println("Второй элемент: " + numbers.secondOrNull())
            println("Предпоследний элемент: " + numbers.penultimate())
            println("Сумма четных: " + numbers.sumOfEven())
            println("Перевернутый: " + numbers.reversedString())
            
            val emptyList = emptyList<Int>()
            println("
    Пустой список:")
            println("Второй элемент: " + emptyList.secondOrNull())
            println("Предпоследний элемент: " + emptyList.penultimate())
            
            // 4. Обобщенные расширения
            println("
    === Обобщенные расширения ===")
            val strings = listOf("apple", "banana", "cherry")
            println("Второй элемент строк: " + strings.secondOrNull())
            
            // 5. Расширения с nullable получателем
            println("
    === Расширения с nullable ===")
            var nullableString: String? = "Hello"
            println("Длина: " + nullableString.safeLength())
            
            nullableString = null
            println("Длина null: " + nullableString.safeLength())
            println("Длина с дефолтом -1: " + nullableString.safeLength(-1))
            
            // 6. Инфиксные расширения
            println("
    === Инфиксные расширения ===")
            val repeated = "Hi " repeatTimes 3
            println("Повтор 3 раза: " + repeated)
            
            val spaced = "A" spacedWith "B"
            println("С пробелом: " + spaced)
            
            // 7. Расширения как функции высшего порядка
            println("
    === Расширения высшего порядка ===")
            val processed = "Test".applyIf(true) {
                println("Применяем к: " + this)
            }
            
            val notProcessed = "Test".applyIf(false) {
                println("Это не напечатается")
            }
            
            // 8. Практический пример
            println("
    === Практический пример ===")
            val products = listOf(100, 200, 150, 300)
            println("Товары: " + products)
            println("Общая сумма: " + products.sum())
            println("Средняя цена: " + products.average())
            println("Скидка 10%: " + products.withDiscount(10))
            println("Только дорогие (>150): " + products.filterExpensive(150))
        }
        
        // Определения расширений
        
        // String расширения
        fun String.addExclamation(): String = this + "!"
        
        fun String.toTitleCase(): String {
            return this.split(" ").joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercase() }
            }
        }
        
        val String.lastChar: Char?
            get() = this.lastOrNull()
        
        val String.hasLetters: Boolean
            get() = this.any { it.isLetter() }
        
        // Int расширения
        fun Int.isEven(): Boolean = this % 2 == 0
        
        fun Int.isOdd(): Boolean = !this.isEven()
        
        fun Int.squared(): Int = this * this
        
        fun Int.pow(exponent: Int): Int {
            var result = 1
            repeat(exponent) {
                result *= this
            }
            return result
        }
        
        fun Int.factorial(): Long {
            return if (this <= 1) 1 else this * (this - 1).factorial()
        }
        
        // List расширения
        fun <T> List<T>.secondOrNull(): T? = this.getOrNull(1)
        
        fun <T> List<T>.penultimate(): T? {
            if (this.size < 2) return null
            return this[this.size - 2]
        }
        
        fun List<Int>.sumOfEven(): Int = this.filter { it.isEven() }.sum()
        
        fun List<Int>.reversedString(): String = this.reversed().joinToString(", ")
        
        // Nullable расширения
        fun String?.safeLength(default: Int = 0): Int = this?.length ?: default
        
        // Инфиксные расширения
        infix fun String.repeatTimes(n: Int): String = this.repeat(n)
        
        infix fun String.spacedWith(other: String): String = "$this $(other)"
        
        // Функции высшего порядка как расширения
        fun <T> T.applyIf(condition: Boolean, block: T.() -> Unit): T {
            if (condition) {
                block()
            }
            return this
        }
        
        // Практические расширения для e-commerce
        fun List<Int>.withDiscount(percent: Int): List<Int> {
            return this.map { price ->
                (price * (100 - percent) / 100)
            }
        }
        
        fun List<Int>.filterExpensive(threshold: Int): List<Int> {
            return this.filter { it > threshold }
        }
    """.trimIndent(),
                topic = "extensions"
            ),
            Lesson(
                id = "coroutines_basics",
                title = "Корутины: основы",
                description = "Асинхронное программирование с корутинами",
                theory = """
        # Корутины в Kotlin
        
        Корутины — это легковесные потоки для асинхронного программирования.
        
        ## Что такое корутины?
        
        Корутины — это:
        - Легковесные потоки (дешевле потоков)
        - Могут приостанавливаться и возобновляться
        - Позволяют писать асинхронный код синхронно
        
        ## Основные понятия
        
        ### CoroutineScope
        Контекст выполнения корутин:
        ```kotlin
        val scope = CoroutineScope(Dispatchers.Default)
        ```
        
        ### Dispatcher
        Определяет, на каком потоке выполняется корутина:
        - `Dispatchers.Default` — для CPU-интенсивных операций
        - `Dispatchers.IO` — для операций ввода/вывода
        - `Dispatchers.Main` — главный поток UI (Android)
        - `Dispatchers.Unconfined` — не ограничен
        
        ### Job
        Контролирует жизненный цикл корутины:
        ```kotlin
        val job = scope.launch { ... }
        job.cancel() // отмена корутины
        ```
        
        ## Запуск корутин
        
        ### launch
        Запускает корутину, которая не возвращает результат:
        ```kotlin
        scope.launch {
            // асинхронный код
        }
        ```
        
        ### async/await
        Запускает корутину, которая возвращает результат:
        ```kotlin
        val deferred: Deferred<Int> = scope.async {
            // вычисление
            42
        }
        val result = deferred.await() // 42
        ```
        
        ### runBlocking
        Блокирует текущий поток до завершения корутины:
        ```kotlin
        runBlocking {
            // блокирующий вызов
            delay(1000)
        }
        ```
        
        ## Приостановка корутин
        
        ### suspend функции
        Функции, которые могут приостанавливаться:
        ```kotlin
        suspend fun fetchData(): String {
            delay(1000) // приостановка без блокировки потока
            return "Данные"
        }
        ```
        
        ### delay
        Приостанавливает корутину на указанное время:
        ```kotlin
        suspend fun process() {
            delay(1000) // 1 секунда
        }
        ```
        
        ## Обработка исключений
        
        ### try-catch в корутинах
        ```kotlin
        scope.launch {
            try {
                fetchData()
            } catch (e: Exception) {
                println("Ошибка: " + e.message)
            }
        }
        ```
        
        ### CoroutineExceptionHandler
        Глобальный обработчик исключений:
        ```kotlin
        val handler = CoroutineExceptionHandler { _, exception ->
            println("Поймано исключение: " + exception)
        }
        
        val scope = CoroutineScope(Dispatchers.Default + handler)
        ```
        
        ## Структурная конкуренция
        
        Корутины следуют иерархии родитель-потомок:
        ```kotlin
        scope.launch {
            // родительская корутина
            launch {
                // дочерняя корутина
            }
        }
        ```
        
        Отмена родителя отменяет всех детей:
        ```kotlin
        val parentJob = scope.launch {
            launch { /* child 1 */ }
            launch { /* child 2 */ }
        }
        
        parentJob.cancel() // отменяет все дочерние корутины
        ```
        
        ## Потоковые диспетчеры
        
        ### withContext
        Переключение контекста выполнения:
        ```kotlin
        suspend fun loadData() {
            // В фоновом потоке
            val data = withContext(Dispatchers.IO) {
                fetchFromNetwork()
            }
            
            // В главном потоке
            withContext(Dispatchers.Main) {
                updateUI(data)
            }
        }
        ```
        
        ## Отмена корутин
        
        ### Проверка на отмену
        ```kotlin
        scope.launch {
            while (isActive) { // проверка активна ли корутина
                // работа
                yield() // приостановка для проверки отмены
            }
        }
        ```
        
        ### ensureActive
        ```kotlin
        scope.launch {
            repeat(1000) { i ->
                ensureActive() // выбросит исключение если корутина отменена
                // работа
            }
        }
        ```
        
        ## Таймауты
        
        ### withTimeout
        ```kotlin
        try {
            val result = withTimeout(5000) { // 5 секунд
                fetchData()
            }
        } catch (e: TimeoutCancellationException) {
            println("Таймаут!")
        }
        ```
        
        ### withTimeoutOrNull
        ```kotlin
        val result = withTimeoutOrNull(5000) {
            fetchData()
        } ?: "Таймаут"
        ```
        
        ## Параллельное выполнение
        
        ```kotlin
        suspend fun fetchMultiple() {
            val deferred1 = async { fetchData1() }
            val deferred2 = async { fetchData2() }
            
            val result1 = deferred1.await()
            val result2 = deferred2.await()
            
            // или
            val results = awaitAll(deferred1, deferred2)
        }
        ```
        
        ## Flow — асинхронные потоки
        
        ### Создание Flow
        ```kotlin
        fun getNumbers(): Flow<Int> = flow {
            for (i in 1..5) {
                delay(100)
                emit(i) // отправка значения
            }
        }
        ```
        
        ### Коллектинг Flow
        ```kotlin
        scope.launch {
            getNumbers()
                .collect { value ->
                    println(value)
                }
        }
        ```
        
        ### Операторы Flow
        ```kotlin
        getNumbers()
            .filter { it % 2 == 0 }
            .map { it * it }
            .collect { println(it) }
        ```
        
        ## Практические паттерны
        
        ### Паттерн MVVM с корутинами
        ```kotlin
        class ViewModel : ViewModel() {
            private val _data = MutableStateFlow<String?>(null)
            val data: StateFlow<String?> = _data.asStateFlow()
            
            fun loadData() {
                viewModelScope.launch {
                    _data.value = repository.fetchData()
                }
            }
        }
        ```
        
        ### Ретри и повторы
        ```kotlin
        suspend fun <T> retry(
            times: Int = 3,
            delay: Long = 1000,
            block: suspend () -> T
        ): T {
            repeat(times - 1) { attempt ->
                try {
                    return block()
                } catch (e: Exception) {
                    delay(delay * (attempt + 1))
                }
            }
            return block() // последняя попытка
        }
        ```
    """.trimIndent(),
                codeExample = """
        import kotlinx.coroutines.*
        import kotlin.system.measureTimeMillis
        
        fun main() = runBlocking {
            println("=== Основы корутин ===")
            
            // 1. Простой запуск корутины
            println("
    1. Простой запуск:")
            launchExample()
            
            // 2. async/await для параллельных вычислений
            println("
    2. Параллельные вычисления:")
            val time = measureTimeMillis {
                val result = parallelComputation()
                println("Результат: " + result)
            }
            println("Время выполнения: " + time + "мс")
            
            // 3. Переключение контекстов
            println("
    3. Переключение контекстов:")
            contextSwitching()
            
            // 4. Обработка исключений
            println("
    4. Обработка исключений:")
            exceptionHandling()
            
            // 5. Таймауты
            println("
    5. Таймауты:")
            timeoutExample()
            
            // 6. Структурная конкуренция
            println("
    6. Структурная конкуренция:")
            structuredConcurrency()
            
            // 7. Отмена корутин
            println("
    7. Отмена корутин:")
            cancellationExample()
            
            // 8. Flow - асинхронные потоки
            println("
    8. Flow - асинхронные потоки:")
            flowExample()
            
            println("
    === Демонстрация завершена ===")
        }
        
        suspend fun launchExample() {
            println("Запуск корутины...")
            
            // launch - для операций без результата
            val job = GlobalScope.launch {
                delay(1000)
                println("Корутина выполнена после задержки")
            }
            
            println("Основной поток продолжает работу...")
            job.join() // ждем завершения корутины
            println("Корутина завершена")
        }
        
        suspend fun parallelComputation(): Int = coroutineScope {
            println("Начало параллельных вычислений...")
            
            // async для параллельного выполнения с результатом
            val deferred1 = async {
                delay(1000)
                println("Вычисление 1 завершено")
                10
            }
            
            val deferred2 = async {
                delay(1500)
                println("Вычисление 2 завершено")
                20
            }
            
            val deferred3 = async {
                delay(800)
                println("Вычисление 3 завершено")
                30
            }
            
            // awaitAll ждет завершения всех
            val results = awaitAll(deferred1, deferred2, deferred3)
            results.sum()
        }
        
        suspend fun contextSwitching() {
            println("Старт в Default диспетчере")
            
            withContext(Dispatchers.IO) {
                println("Переключились на IO диспетчер")
                // Имитация IO операции
                delay(500)
            }
            
            println("Вернулись в предыдущий контекст")
            
            // Несколько переключений
            val result = withContext(Dispatchers.Default) {
                "Результат из Default"
            }
            
            println("Получен результат: " + result)
        }
        
        suspend fun exceptionHandling() {
            val handler = CoroutineExceptionHandler { _, exception ->
                println("Глобальный обработчик поймал: " + exception.message)
            }
            
            val scope = CoroutineScope(Dispatchers.Default + handler)
            
            // Исключение в корутине
            scope.launch {
                try {
                    throw RuntimeException("Тестовое исключение")
                } catch (e: Exception) {
                    println("Локальный catch: " + e.message)
                }
            }
            
            delay(100) // даем время на выполнение
        }
        
        suspend fun timeoutExample() {
            println("Попытка выполнения с таймаутом...")
            
            try {
                val result = withTimeout(1300) {
                    repeat(3) { i ->
                        println("Итерация " + (i + 1))
                        delay(500) // 3 * 500 = 1500мс > 1300мс
                    }
                    "Успех"
                }
                println("Результат: " + result)
            } catch (e: TimeoutCancellationException) {
                println("Таймаут! " + e.message)
            }
            
            // withTimeoutOrNull
            println("
    withTimeoutOrNull:")
            val result = withTimeoutOrNull(1000) {
                delay(2000)
                "Это не выполнится"
            }
            println("Результат: " + (result ?: "null (таймаут)"))
        }
        
        suspend fun structuredConcurrency() = coroutineScope {
            println("Родительская корутина запущена")
            
            // Дочерние корутины
            val child1 = launch {
                delay(1000)
                println("Дочерняя 1 завершена")
            }
            
            val child2 = launch {
                delay(1500)
                println("Дочерняя 2 завершена")
            }
            
            // Если родитель отменяется - отменяются все дети
            launch {
                delay(500)
                println("Быстрая дочерняя корутина")
            }
            
            // Ждем завершения детей
            joinAll(child1, child2)
            println("Все дочерние корутины завершены")
        }
        
        suspend fun cancellationExample() = coroutineScope {
            println("Демонстрация отмены...")
            
            val job = launch {
                repeat(1000) { i ->
                    if (!isActive) {
                        println("Корутина отменена")
                        return@launch
                    }
                    
                    try {
                        ensureActive() // проверка отмены
                        println("Работа " + i)
                        delay(100)
                    } catch (e: CancellationException) {
                        println("Поймана отмена на итерации " + i)
                        throw e // перебрасываем исключение
                    }
                }
            }
            
            delay(250) // даем поработать
            println("Отправляем запрос на отмену...")
            job.cancelAndJoin() // отменяем и ждем
            println("Корутина отменена")
        }
        
        suspend fun flowExample() {
            println("Создание Flow...")
            
            // Создание простого Flow
            fun simpleFlow(): kotlinx.coroutines.flow.Flow<Int> = kotlinx.coroutines.flow.flow {
                for (i in 1..5) {
                    delay(200)
                    emit(i) // отправляем значение
                }
            }
            
            // Коллектинг Flow
            simpleFlow()
                .onEach { value ->
                    println("Получено: " + value)
                }
                .collect() // запускаем сбор данных
            
            // Flow с операторами
            println("
    Flow с операторами:")
            kotlinx.coroutines.flow.flowOf(1, 2, 3, 4, 5)
                .filter { it % 2 == 0 }
                .map { it * 10 }
                .collect { value ->
                    println("Обработанное значение: " + value)
                }
            
            // StateFlow для UI состояния
            println("
    Имитация StateFlow:")
            val state = kotlinx.coroutines.flow.MutableStateFlow("Начальное состояние")
            
            // Запускаем сборщик состояния
            val collectorJob = launch {
                state.collect { newState ->
                    println("Состояние изменилось: " + newState)
                }
            }
            
            // Меняем состояние
            state.value = "Обновленное состояние 1"
            delay(100)
            state.value = "Обновленное состояние 2"
            
            collectorJob.cancel() // останавливаем сбор
        }
        
        // Вспомогательные suspend функции
        suspend fun simulateNetworkCall(delay: Long, result: String): String {
            delay(delay)
            return result
        }
        
        suspend fun simulateCPUTask(): Int {
            delay(300)
            return (1..100).random()
        }
    """.trimIndent(),
                topic = "coroutines"
            ),
            Lesson(
                id = "dsl_builders",
                title = "DSL и билдеры",
                description = "Создание предметно-ориентированных языков",
                theory = """
        # DSL (Domain Specific Language) в Kotlin
        
        DSL — это предметно-ориентированный язык, встроенный в Kotlin.
        
        ## Что такое DSL?
        
        DSL позволяет создавать декларативный код для конкретной области:
        - HTML/XML построение
        - SQL запросы
        - Конфигурации
        - Тестирование
        
        ## Основные концепции
        
        ### Функции с получателем (Function with Receiver)
        
        ```kotlin
        fun html(init: HTML.() -> Unit): HTML {
            val html = HTML()
            html.init()
            return html
        }
        
        // Использование
        html {
            head { }
            body { }
        }
        ```
        
        ### Лямбда с получателем
        
        ```kotlin
        class StringBuilder {
            fun append(text: String) { ... }
        }
        
        fun buildString(builder: StringBuilder.() -> Unit): String {
            val stringBuilder = StringBuilder()
            stringBuilder.builder()
            return stringBuilder.toString()
        }
        ```
        
        ## Создание простого DSL
        
        ### Шаг 1: Определение классов
        ```kotlin
        class HTML {
            private val children = mutableListOf<Tag>()
            
            fun head(init: Head.() -> Unit) {
                val head = Head()
                head.init()
                children.add(head)
            }
            
            override fun toString() = children.joinToString("
    ")
        }
        
        class Head {
            private val children = mutableListOf<Element>()
            
            fun title(text: String) {
                children.add(Title(text))
            }
            
            override fun toString() = "<head>${'$'}{children.joinToString("")}</head>"
        }
        ```
        
        ### Шаг 2: Использование
        ```kotlin
        val html = html {
            head {
                title("Моя страница")
            }
        }
        ```
        
        ## Типичные DSL паттерны
        
        ### Билдер паттерн
        ```kotlin
        class PersonBuilder {
            var name: String = ""
            var age: Int = 0
            var city: String = ""
            
            fun build() = Person(name, age, city)
        }
        
        fun person(block: PersonBuilder.() -> Unit): Person {
            val builder = PersonBuilder()
            builder.block()
            return builder.build()
        }
        ```
        
        ### Цепочка вызовов (Fluent Interface)
        ```kotlin
        class Query {
            fun select(vararg columns: String) = this
            fun from(table: String) = this
            fun where(condition: String) = this
        }
        
        val query = Query()
            .select("name", "age")
            .from("users")
            .where("age > 18")
        ```
        
        ## Расширения для DSL
        
        ```kotlin
        fun TABLE.tr(init: TR.() -> Unit) {
            val tr = TR()
            tr.init()
            this.children.add(tr)
        }
        
        fun TR.td(text: String) {
            this.children.add(TD(text))
        }
        ```
        
        ## DSL для конфигураций
        
        ```kotlin
        class DatabaseConfig {
            var url: String = ""
            var username: String = ""
            var password: String = ""
            var poolSize: Int = 10
            
            override fun toString() = "url: $${'{'}url${'}'}, user: $${'{'}username${'}'}"
        }
        
        fun database(block: DatabaseConfig.() -> Unit): DatabaseConfig {
            val config = DatabaseConfig()
            config.block()
            return config
        }
        
        // Использование
        val config = database {
            url = "jdbc:mysql://localhost:3306/mydb"
            username = "admin"
            password = "secret"
            poolSize = 20
        }
        ```
        
        ## DSL для тестирования
        
        ```kotlin
        class TestSuite {
            private val tests = mutableListOf<Test>()
            
            fun test(name: String, block: () -> Unit) {
                tests.add(Test(name, block))
            }
            
            fun run() {
                tests.forEach { it.run() }
            }
        }
        
        fun suite(block: TestSuite.() -> Unit): TestSuite {
            val suite = TestSuite()
            suite.block()
            return suite
        }
        ```
        
        ## Аннотации для DSL
        
        ### @DslMarker
        Ограничивает область видимости в DSL:
        ```kotlin
        @DslMarker
        annotation class HtmlDsl
        
        @HtmlDsl
        class HTML { ... }
        
        @HtmlDsl
        class Body { ... }
        
        // Теперь нельзя смешивать вызовы
        html {
            head {
                // body { } // Ошибка! body не в scope
            }
        }
        ```
        
        ## Практические примеры
        
        ### SQL DSL
        ```kotlin
        sql {
            select("name", "age")
            from("users")
            where {
                "age" greater 18
                and { "city" equals "Moscow" }
            }
            orderBy("name")
        }
        ```
        
        ### Gradle Kotlin DSL
        ```kotlin
        plugins {
            kotlin("jvm") version "1.9.0"
            application
        }
        
        dependencies {
            implementation(kotlin("stdlib"))
            testImplementation(kotlin("test"))
        }
        ```
        
        ### Android View DSL
        ```kotlin
        verticalLayout {
            padding = dip(16)
            
            textView {
                text = "Hello"
                textSize = 20f
            }
            
            button {
                text = "Click me"
                onClick { showToast("Clicked!") }
            }
        }
        ```
        
        ## Оптимизация DSL
        
        ### inline функции
        ```kotlin
        inline fun html(crossinline init: HTML.() -> Unit): HTML {
            val html = HTML()
            html.init()
            return html
        }
        ```
        
        ### reified типы
        ```kotlin
        inline fun <reified T : Tag> createTag(): T {
            return T::class.java.newInstance()
        }
        ```
        
        ## Обработка ошибок в DSL
        
        ```kotlin
        fun require(condition: Boolean, lazyMessage: () -> String) {
            if (!condition) {
                throw IllegalArgumentException(lazyMessage())
            }
        }
        
        fun html(init: HTML.() -> Unit): HTML {
            val html = HTML()
            try {
                html.init()
            } catch (e: Exception) {
                throw IllegalStateException("Ошибка в DSL: " + e.message, e)
            }
            return html
        }
        ```
        
        ## Расширенные возможности
        
        ### Инфиксные функции в DSL
        ```kotlin
        infix fun String.eq(value: Any): Condition {
            return Condition(this, "=", value.toString())
        }
        
        infix fun String.gt(value: Int): Condition {
            return Condition(this, ">", value.toString())
        }
        ```
        
        ### Операторы перегрузки
        ```kotlin
        operator fun String.unaryPlus() {
            addText(this)
        }
        
        // Использование
        +"Текст"
        ```
        
        ### Делегаты в DSL
        ```kotlin
        class Config {
            var host by RequiredProperty()
            var port by DefaultProperty(8080)
        }
        
        fun config(block: Config.() -> Unit): Config {
            val config = Config()
            config.block()
            config.validate()
            return config
        }
        ```
        
        ## Best Practices
        
        1. Делайте DSL типобезопасным
        2. Используйте @DslMarker для предотвращения путаницы
        3. Предоставляйте понятные сообщения об ошибках
        4. Документируйте DSL структуру
        5. Тестируйте DSL как обычный код
    """.trimIndent(),
                codeExample = """
        fun main() {
            println("=== DSL и билдеры в Kotlin ===")
            
            // 1. HTML DSL
            println("
    1. HTML DSL:")
            val htmlDocument = createHtmlPage()
            println(htmlDocument)
            
            // 2. Person Builder DSL
            println("
    2. Person Builder DSL:")
            val person = createPerson()
            println(person)
            
            // 3. Database Config DSL
            println("
    3. Database Config DSL:")
            val dbConfig = configureDatabase()
            println(dbConfig)
            
            // 4. SQL Query DSL
            println("
    4. SQL Query DSL:")
            val query = buildQuery()
            println(query)
            
            // 5. UI Layout DSL
            println("
    5. UI Layout DSL:")
            val layout = createLayout()
            println(layout)
            
            // 6. Test Suite DSL
            println("
    6. Test Suite DSL:")
            runTests()
            
            // 7. Gradle-like DSL
            println("
    7. Gradle-like DSL:")
            val buildScript = configureBuild()
            println(buildScript)
            
            println("
    === Все DSL примеры выполнены ===")
        }
        
        // 1. HTML DSL
        fun createHtmlPage(): String {
            return html {
                head {
                    title("Моя страница")
                    meta("charset" to "UTF-8")
                    style {
                        "body { font-family: Arial; }"
                        "h1 { color: blue; }"
                    }
                }
                body {
                    h1("Добро пожаловать в Kotlin DSL!")
                    p("Это пример HTML, созданного с помощью DSL")
                    div(classes = "content") {
                        p("Содержимое блока")
                        ul {
                            for (i in 1..3) {
                                li("Элемент " + i)
                            }
                        }
                    }
                    footer {
                        p("© 2024 Kotlin DSL Example")
                    }
                }
            }.toString()
        }
        
        // 2. Person Builder DSL
        fun createPerson(): Person {
            return person {
                name = "Иван Петров"
                age = 30
                address {
                    city = "Москва"
                    street = "Тверская"
                    house = "15"
                    apartment = "42"
                }
                contacts {
                    email = "ivan@example.com"
                    phone = "+7 999 123-45-67"
                }
                job {
                    position = "Разработчик"
                    company = "TechCorp"
                    experience = 5 // лет
                }
            }
        }
        
        // 3. Database Config DSL
        fun configureDatabase(): DatabaseConfig {
            return database {
                url = "jdbc:postgresql://localhost:5432/mydatabase"
                credentials {
                    username = "admin"
                    password = "secret123"
                }
                pool {
                    maxSize = 20
                    minSize = 5
                    timeout = 30000 // ms
                }
                options {
                    "ssl" to true
                    "charset" to "UTF-8"
                    "connectTimeout" to 5000
                }
            }
        }
        
        // 4. SQL Query DSL
        fun buildQuery(): String {
            return sql {
                select("u.name", "u.age", "d.name as department")
                from("users u")
                join("departments d") {
                    "u.department_id" eq "d.id"
                }
                where {
                    "u.age" greaterEq 18
                    and {
                        "u.active" eq true
                        or {
                            "u.registration_date" greater "2024-01-01"
                        }
                    }
                }
                orderBy("u.name", SortOrder.ASC)
                limit(100)
                offset(0)
            }.build()
        }
        
        // 5. UI Layout DSL
        fun createLayout(): String {
            return compose {
                verticalLayout(
                    padding = 16.dp,
                    spacing = 8.dp
                ) {
                    text(
                        text = "Привет, мир!",
                        textSize = 24.sp,
                        color = Color.BLUE
                    )
                    
                    spacer(height = 16.dp)
                    
                    card(elevation = 4.dp) {
                        column {
                            text("Заголовок карточки", style = TextStyle.BOLD)
                            text("Описание карточки с некоторым текстом")
                        }
                    }
                    
                    button(
                        text = "Нажми меня",
                        onClick = { println("Кнопка нажата!") }
                    ) {
                        icon = Icon.STAR
                        enabled = true
                    }
                }
            }.render()
        }
        
        // 6. Test Suite DSL
        fun runTests() {
            val testResults = testSuite("Мои тесты") {
                beforeAll {
                    println("Инициализация перед всеми тестами")
                }
                
                afterAll {
                    println("Очистка после всех тестов")
                }
                
                test("Тест сложения") {
                    val result = 2 + 2
                    assert(result == 4) { "2 + 2 должно быть 4" }
                    println("✓ Тест сложения пройден")
                }
                
                test("Тест строк") {
                    val text = "Kotlin"
                    assert(text.length == 6)
                    assert(text.startsWith("K"))
                    println("✓ Тест строк пройден")
                }
                
                group("Группа тестов коллекций") {
                    beforeEach {
                        println("  Перед каждым тестом в группе")
                    }
                    
                    test("Тест списка") {
                        val list = listOf(1, 2, 3)
                        assert(list.size == 3)
                        assert(list.contains(2))
                    }
                    
                    test("Тест множества") {
                        val set = setOf(1, 2, 2, 3)
                        assert(set.size == 3) // дубликаты удаляются
                    }
                }
            }
            
            testResults.run()
            println("Всего тестов: " + testResults.total)
            println("Пройдено: " + testResults.passed)
            println("Провалено: " + testResults.failed)
        }
        
        // 7. Gradle-like DSL
        fun configureBuild(): String {
            return buildGradle {
                plugins {
                    kotlin("jvm") version "1.9.0"
                    id("application")
                    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0"
                }
                
                dependencies {
                    implementation(kotlin("stdlib"))
                    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                    implementation("com.squareup.retrofit2:retrofit:2.9.0")
                    
                    testImplementation(kotlin("test"))
                    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
                }
                
                application {
                    mainClass = "com.example.MainKt"
                }
                
                tasks {
                    compileKotlin {
                        kotlinOptions.jvmTarget = "17"
                    }
                    
                    test {
                        useJUnitPlatform()
                    }
                }
            }.generate()
        }
        
        // DSL определения (упрощенные)
        
        // HTML DSL
        @DslMarker
        annotation class HtmlDsl
        
        @HtmlDsl
        class HtmlBuilder {
            private val children = mutableListOf<Any>()
            
            fun head(block: HeadBuilder.() -> Unit) {
                val head = HeadBuilder()
                head.block()
                children.add(head)
            }
            
            fun body(block: BodyBuilder.() -> Unit) {
                val body = BodyBuilder()
                body.block()
                children.add(body)
            }
            
            override fun toString(): String {
                return "<!DOCTYPE html>
    <html>
    " + children.joinToString("
    ") + "
    </html>"
            }
        }
        
        @HtmlDsl
        class HeadBuilder {
            private val children = mutableListOf<String>()
            
            fun title(text: String) {
                children.add("<title>" + text + "</title>")
            }
            
            fun meta(vararg attributes: Pair<String, String>) {
                val attrs = attributes.joinToString(" ") { "${'$'}{it.first}=\"${'$'}{it.second}\"" }
                children.add("<meta " + attrs + ">")
            }
            
            fun style(block: StyleBuilder.() -> Unit) {
                val style = StyleBuilder()
                style.block()
                children.add("<style>" + style + "</style>")
            }
            
            override fun toString() = "<head>
    " + children.joinToString("
    ") + "
    </head>"
        }
        
        class StyleBuilder {
            private val rules = mutableListOf<String>()
            
            operator fun String.unaryPlus() {
                rules.add(this)
            }
            
            override fun toString() = rules.joinToString("
    ")
        }
        
        @HtmlDsl
        class BodyBuilder {
            private val children = mutableListOf<String>()
            
            fun h1(text: String) {
                children.add("<h1>" + text + "</h1>")
            }
            
            fun p(text: String) {
                children.add("<p>" + text + "</p>")
            }
            
            fun div(classes: String = "", block: BodyBuilder.() -> Unit) {
                val classAttr = if (classes.isNotEmpty()) " class=\"${'$'}classes\"" else ""
                val divContent = BodyBuilder().apply(block).toString().replace("\n", "\n  ")
                children.add("<div" + classAttr + ">
      " + divContent + "
    </div>")
            }
            
            fun ul(block: UlBuilder.() -> Unit) {
                val ul = UlBuilder()
                ul.block()
                children.add(ul.toString())
            }
            
            fun footer(block: BodyBuilder.() -> Unit) {
                val footer = BodyBuilder().apply(block)
                children.add("<footer>" + footer + "</footer>")
            }
            
            override fun toString() = "<body>
    " + children.joinToString("
    ") + "
    </body>"
        }
        
        class UlBuilder {
            private val items = mutableListOf<String>()
            
            fun li(text: String) {
                items.add("<li>" + text + "</li>")
            }
            
            override fun toString() = "<ul>
    " + items.joinToString("
    ") + "
    </ul>"
        }
        
        fun html(block: HtmlBuilder.() -> Unit): HtmlBuilder {
            return HtmlBuilder().apply(block)
        }
        
        // Person Builder DSL
        data class Person(
            val name: String,
            val age: Int,
            val address: Address,
            val contacts: Contacts,
            val job: Job
        )
        
        data class Address(
            val city: String,
            val street: String,
            val house: String,
            val apartment: String
        )
        
        data class Contacts(
            val email: String,
            val phone: String
        )
        
        data class Job(
            val position: String,
            val company: String,
            val experience: Int
        )
        
        class PersonBuilder {
            var name: String = ""
            var age: Int = 0
            private var addressBuilder = AddressBuilder()
            private var contactsBuilder = ContactsBuilder()
            private var jobBuilder = JobBuilder()
            
            fun address(block: AddressBuilder.() -> Unit) {
                addressBuilder.block()
            }
            
            fun contacts(block: ContactsBuilder.() -> Unit) {
                contactsBuilder.block()
            }
            
            fun job(block: JobBuilder.() -> Unit) {
                jobBuilder.block()
            }
            
            fun build(): Person {
                return Person(
                    name,
                    age,
                    addressBuilder.build(),
                    contactsBuilder.build(),
                    jobBuilder.build()
                )
            }
        }
        
        class AddressBuilder {
            var city: String = ""
            var street: String = ""
            var house: String = ""
            var apartment: String = ""
            
            fun build() = Address(city, street, house, apartment)
        }
        
        class ContactsBuilder {
            var email: String = ""
            var phone: String = ""
            
            fun build() = Contacts(email, phone)
        }
        
        class JobBuilder {
            var position: String = ""
            var company: String = ""
            var experience: Int = 0
            
            fun build() = Job(position, company, experience)
        }
        
        fun person(block: PersonBuilder.() -> Unit): Person {
            return PersonBuilder().apply(block).build()
        }
        
        // Остальные DSL определения были бы аналогичными, но для краткости опущены
        // В реальном коде они были бы полноценно реализованы
        
        // Заглушки для остальных DSL
        class DatabaseConfig {
            override fun toString() = "DatabaseConfig"
        }
        
        fun database(block: DatabaseConfig.() -> Unit) = DatabaseConfig().apply(block)
        
        class SqlQuery {
            fun build() = "SELECT * FROM users"
        }
        
        fun sql(block: SqlQuery.() -> Unit) = SqlQuery().apply(block)
        
        class Composer {
            fun render() = "UI Layout"
        }
        
        fun compose(block: Composer.() -> Unit) = Composer().apply(block)
        
        class TestSuite(val name: String) {
            val total = 4
            val passed = 4
            val failed = 0
            fun run() { println("Запуск тестов...") }
        }
        
        fun testSuite(name: String, block: TestSuite.() -> Unit) = TestSuite(name).apply(block)
        
        class BuildGradle {
            fun generate() = "build.gradle.kts content"
        }
        
        fun buildGradle(block: BuildGradle.() -> Unit) = BuildGradle().apply(block)
        
        // Extension properties для DSL
        val Int.dp get() = this
        val Int.sp get() = this
        
        enum class Color { BLUE }
        enum class Icon { STAR }
        
        class TextStyle {
            companion object {
                val BOLD = TextStyle()
            }
        }
    """.trimIndent(),
                topic = "dsl"
            ),

            Lesson(
                id = "delegation",
                title = "Делегирование в Kotlin",
                description = "Шаблон делегирования и делегированные свойства",
                theory = """
        # Делегирование в Kotlin
        
        Kotlin поддерживает шаблон делегирования на уровне языка, что позволяет легко реализовывать композицию вместо наследования.
        
        ## Делегирование реализации (Implementation by Delegation)
        
        ### Ключевое слово `by`
        ```kotlin
        interface Base {
            fun print()
        }
        
        class BaseImpl(val x: Int) : Base {
            override fun print() { println(x) }
        }
        
        // Derived делегирует реализацию Base объекту b
        class Derived(b: Base) : Base by b
        ```
        
        ### Множественное делегирование
        ```kotlin
        interface A {
            fun foo() { println("A") }
        }
        
        interface B {
            fun foo() { println("B") }
        }
        
        class C(a: A, b: B) : A by a, B by b {
            override fun foo() {
                super<A>.foo()
                super<B>.foo()
            }
        }
        ```
        
        ## Делегированные свойства (Delegated Properties)
        
        ### Стандартные делегаты
        
        #### lazy
        ```kotlin
        val lazyValue: String by lazy {
            println("Вычисляем значение")
            "Hello"
        }
        
        // При первом обращении
        println(lazyValue) // "Вычисляем значение" и "Hello"
        println(lazyValue) // только "Hello"
        ```
        
        #### observable
        ```kotlin
        import kotlin.properties.Delegates
        
        var observedValue: String by Delegates.observable("начальное") {
            prop, old, new ->
            println("${'$'}{prop.name} изменилось с ${'$'}old на ${'$'}new")
        }
        
        observedValue = "первое"  // печатает: observedValue изменилось с начальное на первое
        observedValue = "второе"  // печатает: observedValue изменилось с первое на второе
        ```
        
        #### vetoable
        ```kotlin
        var positiveNumber: Int by Delegates.vetoable(0) {
            prop, old, new ->
            new >= 0 // разрешает изменение только если новое значение >= 0
        }
        
        positiveNumber = 10  // OK
        positiveNumber = -5  // Не изменится, останется 10
        ```
        
        #### notNull
        ```kotlin
        var notNullValue: String by Delegates.notNull<String>()
        
        // notNullValue // Ошибка: property not initialized
        notNullValue = "значение" // OK
        println(notNullValue) // "значение"
        ```
        
        ## Создание пользовательских делегатов
        
        ### Базовый делегат
        ```kotlin
        class Example {
            var p: String by Delegate()
        }
        
        class Delegate {
            operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
                return "${'$'}{property.name} получено"
            }
            
            operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
                println("${'$'}{property.name} установлено в ${'$'}value")
            }
        }
        ```
        
        ### Делегат с параметрами
        ```kotlin
        class ValidatedDelegate<T>(private val validator: (T) -> Boolean) {
            private var value: T? = null
            
            operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
                return value ?: throw IllegalStateException("Property not initialized")
            }
            
            operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
                if (validator(value)) {
                    this.value = value
                } else {
                    throw IllegalArgumentException("Invalid value: ${'$'}value")
                }
            }
        }
        
        class User {
            var age: Int by ValidatedDelegate { it in 0..150 }
        }
        ```
        
        ## Делегирование в коллекциях
        
        ### Map делегат
        ```kotlin
        class Config(map: Map<String, Any?>) {
            val name: String by map
            val version: Int by map
            val enabled: Boolean by map
        }
        
        val config = Config(mapOf(
            "name" to "App",
            "version" to 1,
            "enabled" to true
        ))
        
        println(config.name) // "App"
        ```
        
        ### MutableMap делегат
        ```kotlin
        class MutableConfig(map: MutableMap<String, Any?>) {
            var name: String by map
            var version: Int by map
            var enabled: Boolean by map
        }
        
        val mutableMap = mutableMapOf(
            "name" to "App",
            "version" to 1
        )
        val config = MutableConfig(mutableMap)
        config.enabled = true
        println(mutableMap["enabled"]) // true
        ```
        
        ## Практические примеры делегатов
        
        ### Кэширующий делегат
        ```kotlin
        class CacheDelegate<T>(private val compute: () -> T) {
            private var cachedValue: T? = null
            private var computed = false
            
            operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
                if (!computed) {
                    cachedValue = compute()
                    computed = true
                }
                return cachedValue!!
            }
        }
        
        val expensiveValue: String by CacheDelegate {
            // Дорогостоящее вычисление
            Thread.sleep(1000)
            "Результат"
        }
        ```
        
        ### Логирующий делегат
        ```kotlin
        class LoggingDelegate<T>(private var value: T) {
            operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
                println("${'$'}{property.name} прочитано: ${'$'}value")
                return value
            }
            
            operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
                println("${'$'}{property.name} изменено с ${'$'}{this.value} на ${'$'}value")
                this.value = value
            }
        }
        ```
        
        ## Делегирование в Android/Kotlin Multiplatform
        
        ### View Binding делегат
        ```kotlin
        class ActivityDelegate<T : ViewBinding>(
            private val inflate: (LayoutInflater) -> T
        ) : ReadOnlyProperty<Activity, T> {
            private var binding: T? = null
            
            override fun getValue(thisRef: Activity, property: KProperty<*>): T {
                return binding ?: inflate(thisRef.layoutInflater).also { binding = it }
            }
        }
        
        // Использование
        class MainActivity : AppCompatActivity() {
            private val binding by ActivityDelegate(MainBinding::inflate)
        }
        ```
        
        ### SharedPreferences делегат
        ```kotlin
        class PreferenceDelegate<T>(
            private val preferences: SharedPreferences,
            private val key: String,
            private val defaultValue: T,
            private val serializer: (T) -> String,
            private val deserializer: (String) -> T
        ) {
            operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
                val value = preferences.getString(key, null)
                return if (value != null) deserializer(value) else defaultValue
            }
            
            operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
                preferences.edit().putString(key, serializer(value)).apply()
            }
        }
        ```
        
        ## Расширенные возможности
        
        ### Делегаты с зависимостями
        ```kotlin
        class DependantDelegate(
            private val dependency: () -> String
        ) {
            operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
                return "Зависит от: ${'$'}{dependency()}"
            }
        }
        ```
        
        ### Делегаты с контекстом
        ```kotlin
        class ContextAwareDelegate {
            operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
                return when (thisRef) {
                    is Activity -> "Контекст Activity"
                    is Fragment -> "Контекст Fragment"
                    else -> "Неизвестный контекст"
                }
            }
        }
        ```
        
        ## Производительность делегатов
        
        ### inline делегаты
        ```kotlin
        inline fun <T> loggingDelegate(crossinline initializer: () -> T): ReadWriteProperty<Any?, T> {
            return object : ReadWriteProperty<Any?, T> {
                private var value: T? = null
                
                override fun getValue(thisRef: Any?, property: KProperty<*>): T {
                    println("Чтение ${'$'}{property.name}")
                    return value ?: initializer().also { value = it }
                }
                
                override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
                    println("Запись ${'$'}{property.name} = ${'$'}value")
                    this.value = value
                }
            }
        }
        ```
        
        ### Кэширование делегатов
        ```kotlin
        class CachedDelegateProvider {
            private val delegates = mutableMapOf<String, Any>()
            
            fun <T> provide(key: String, factory: () -> T): T {
                @Suppress("UNCHECKED_CAST")
                return delegates.getOrPut(key) { factory() } as T
            }
        }
        ```
        
        ## Лучшие практики
        
        1. Используйте делегирование вместо наследования, когда нужна композиция
        2. Создавайте делегаты для повторяющейся логики
        3. Документируйте поведение пользовательских делегатов
        4. Тестируйте делегаты как отдельные компоненты
        5. Используйте стандартные делегаты когда возможно
    """.trimIndent(),
                codeExample = """
        import kotlin.properties.Delegates
        import kotlin.reflect.KProperty
        import java.util.concurrent.ConcurrentHashMap
        
        fun main() {
            println("=== Делегирование в Kotlin ===")
            
            // 1. Делегирование реализации
            println("
    1. Делегирование реализации:")
            val baseImpl = BaseImpl(42)
            val derived = Derived(baseImpl)
            derived.print() // 42
            
            // 2. Множественное делегирование
            println("
    2. Множественное делегирование:")
            val aImpl = AImpl()
            val bImpl = BImpl()
            val c = C(aImpl, bImpl)
            c.foo()
            
            // 3. Стандартные делегаты свойств
            println("
    3. Стандартные делегаты свойств:")
            
            // lazy
            println("- Lazy делегат:")
            val config = Configuration()
            println("Первое обращение: " + config.lazyValue)
            println("Второе обращение: " + config.lazyValue)
            
            // observable
            println("- Observable делегат:")
            config.observedValue = "Первое значение"
            config.observedValue = "Второе значение"
            
            // vetoable
            println("- Vetoable делегат:")
            config.positiveNumber = 10
            println("После 10: " + config.positiveNumber)
            config.positiveNumber = -5
            println("После -5: " + config.positiveNumber) // осталось 10
            
            // notNull
            println("- NotNull делегат:")
            // config.notNullValue // Ошибка!
            config.notNullValue = "Установлено"
            println("NotNull значение: " + config.notNullValue)
            
            // 4. Пользовательские делегаты
            println("
    4. Пользовательские делегаты:")
            
            val example = Example()
            println("Чтение свойства: " + example.p)
            example.p = "Новое значение"
            println("После установки: " + example.p)
            
            // 5. Валидирующий делегат
            println("
    5. Валидирующий делегат:")
            val user = User()
            user.age = 25
            println("Возраст: " + user.age)
            
            try {
                user.age = 200 // Ошибка!
            } catch (e: IllegalArgumentException) {
                println("Ошибка валидации: " + e.message)
            }
            
            // 6. Map делегат
            println("
    6. Map делегат:")
            val appConfig = AppConfig(mapOf(
                "name" to "MyApp",
                "version" to 2,
                "debug" to true,
                "timeout" to 5000L
            ))
            
            println("Конфигурация:")
            println("  Имя: " + appConfig.name)
            println("  Версия: " + appConfig.version)
            println("  Отладка: " + appConfig.debug)
            println("  Таймаут: " + appConfig.timeout)
            
            // 7. Кэширующий делегат
            println("
    7. Кэширующий делегат:")
            val calculator = ExpensiveCalculator()
            println("Первое вычисление:")
            println("Результат: " + calculator.expensiveResult)
            println("
    Второе вычисление (должно быть из кэша):")
            println("Результат: " + calculator.expensiveResult)
            
            // 8. Логирующий делегат
            println("
    8. Логирующий делегат:")
            val logger = PropertyLogger()
            logger.message = "Первое сообщение"
            logger.message = "Второе сообщение"
            println("Текущее значение: " + logger.message)
            
            // 9. Делегат с зависимостями
            println("
    9. Делегат с зависимостями:")
            val container = DependencyContainer()
            println("Зависимое значение: " + container.dependantValue)
            
            // 10. Inline делегат
            println("
    10. Inline делегат:")
            val withLogging = WithLogging()
            withLogging.value = "Тест"
            println("Значение: " + withLogging.value)
            
            println("
    === Демонстрация завершена ===")
        }
        
        // 1. Делегирование реализации
        interface Base {
            fun print()
        }
        
        class BaseImpl(val x: Int) : Base {
            override fun print() { println("BaseImpl.print(): " + x) }
        }
        
        class Derived(b: Base) : Base by b
        
        // 2. Множественное делегирование
        interface A {
            fun foo() { println("A.foo()") }
        }
        
        interface B {
            fun foo() { println("B.foo()") }
        }
        
        class AImpl : A
        class BImpl : B
        
        class C(a: A, b: B) : A by a, B by b {
            override fun foo() {
                println("C.foo() вызывает:")
                super<A>.foo()
                super<B>.foo()
            }
        }
        
        // 3. Стандартные делегаты
        class Configuration {
            // lazy
            val lazyValue: String by lazy {
                println("Вычисление lazy значения...")
                "Результат lazy"
            }
            
            // observable
            var observedValue: String by Delegates.observable("начальное") { prop, old, new ->
                println("${'$'}{prop.name}: ${'$'}old -> ${'$'}new")
            }
            
            // vetoable
            var positiveNumber: Int by Delegates.vetoable(0) { _, _, new ->
                new >= 0
            }
            
            // notNull
            var notNullValue: String by Delegates.notNull<String>()
        }
        
        // 4. Пользовательский делегат
        class Example {
            var p: String by CustomDelegate()
        }
        
        class CustomDelegate {
            private var storedValue: String = "default"
            
            operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
                println("${'$'}{property.name} получено: ${'$'}storedValue")
                return storedValue
            }
            
            operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
                println("${'$'}{property.name} устанавливается: ${'$'}value")
                storedValue = value
            }
        }
        
        // 5. Валидирующий делегат
        class ValidatedDelegate<T>(private val validator: (T) -> Boolean) {
            private var value: T? = null
            
            operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
                return value ?: throw IllegalStateException("Свойство не инициализировано")
            }
            
            operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
                if (validator(value)) {
                    this.value = value
                } else {
                    throw IllegalArgumentException("Недопустимое значение: ${'$'}value")
                }
            }
        }
        
        class User {
            var age: Int by ValidatedDelegate { it in 0..150 }
        }
        
        // 6. Map делегат
        class AppConfig(val map: Map<String, Any?>) {
            val name: String by map
            val version: Int by map
            val debug: Boolean by map
            val timeout: Long by map
        }
        
        // 7. Кэширующий делегат
        class CacheDelegate<T>(private val compute: () -> T) {
            private var cachedValue: T? = null
            private var computed = false
            
            operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
                if (!computed) {
                    println("Вычисление значения для ${'$'}{property.name}...")
                    cachedValue = compute()
                    computed = true
                    println("Значение вычислено и закэшировано")
                } else {
                    println("Использование кэшированного значения для ${'$'}{property.name}")
                }
                return cachedValue!!
            }
        }
        
        class ExpensiveCalculator {
            val expensiveResult: String by CacheDelegate {
                // Имитация дорогостоящей операции
                Thread.sleep(1000)
                "Результат вычисления ${System.currentTimeMillis()}"
            }
        }
        
        // 8. Логирующий делегат
        class PropertyLogger {
            var message: String by LoggingDelegate("начальное")
        }
        
        class LoggingDelegate<T>(private var value: T) {
            operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
                println("[LOG] Чтение ${'$'}{property.name}: ${'$'}value")
                return value
            }
            
            operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
                println("[LOG] Запись ${'$'}{property.name}: ${'$'}{this.value} -> ${'$'}value")
                this.value = value
            }
        }
        
        // 9. Делегат с зависимостями
        class DependencyContainer {
            private val dependency: String by lazy {
                println("Инициализация зависимости...")
                "Зависимость"
            }
            
            val dependantValue: String by DependantDelegate { dependency }
        }
        
        class DependantDelegate(private val dependency: () -> String) {
            operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
                return "Значение зависит от: ${'$'}{dependency()}"
            }
        }
        
        // 10. Inline делегат
        inline fun <T> loggingDelegate(crossinline initializer: () -> T): ReadWriteProperty<Any?, T> {
            return object : ReadWriteProperty<Any?, T> {
                private var value: T? = null
                
                override fun getValue(thisRef: Any?, property: KProperty<*>): T {
                    println("[INLINE] Чтение ${'$'}{property.name}")
                    return value ?: initializer().also { value = it }
                }
                
                override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
                    println("[INLINE] Запись ${'$'}{property.name} = ${'$'}value")
                    this.value = value
                }
            }
        }
        
        class WithLogging {
            var value: String by loggingDelegate { "default" }
        }
        
        // Интерфейсы для делегатов
        interface ReadWriteProperty<in R, T> {
            operator fun getValue(thisRef: R, property: KProperty<*>): T
            operator fun setValue(thisRef: R, property: KProperty<*>, value: T)
        }
        
        interface ReadOnlyProperty<in R, out T> {
            operator fun getValue(thisRef: R, property: KProperty<*>): T
        }
    """.trimIndent(),
                topic = "delegation"
            ),
            Lesson(
                id = "functional_programming",
                title = "Функциональное программирование",
                description = "Функциональные концепции и монады в Kotlin",
                theory = """
        # Функциональное программирование в Kotlin
        
        Kotlin поддерживает функциональное программирование, предоставляя функции высшего порядка, лямбда-выражения и другие функциональные концепции.
        
        ## Основные концепции
        
        ### Чистые функции (Pure Functions)
        ```kotlin
        // Чистая функция - всегда один результат для одних аргументов
        fun add(a: Int, b: Int): Int = a + b
        
        // Нечистая функция - зависит от внешнего состояния
        var counter = 0
        fun increment(): Int = ++counter
        ```
        
        ### Функции высшего порядка (Higher-Order Functions)
        ```kotlin
        fun <T, R> List<T>.map(transform: (T) -> R): List<R> {
            val result = mutableListOf<R>()
            for (item in this) {
                result.add(transform(item))
            }
            return result
        }
        
        // Использование
        val numbers = listOf(1, 2, 3)
        val squared = numbers.map { it * it } // [1, 4, 9]
        ```
        
        ### Неизменяемость (Immutability)
        ```kotlin
        // Используйте val вместо var
        val immutableList = listOf(1, 2, 3)
        // immutableList.add(4) // Ошибка!
        
        val newList = immutableList + 4 // Создается новый список
        ```
        
        ## Лямбда-выражения
        
        ### Синтаксис
        ```kotlin
        // Полная форма
        val sum: (Int, Int) -> Int = { a: Int, b: Int -> a + b }
        
        // Сокращенная форма
        val multiply = { a: Int, b: Int -> a * b }
        
        // С одним параметром (it)
        val square: (Int) -> Int = { it * it }
        
        // Без параметров
        val greet = { println("Hello!") }
        ```
        
        ### Замыкания (Closures)
        ```kotlin
        fun makeCounter(): () -> Int {
            var count = 0
            return { ++count } // Замыкание захватывает count
        }
        
        val counter = makeCounter()
        println(counter()) // 1
        println(counter()) // 2
        ```
        
        ## Стандартные функции высшего порядка
        
        ### Основные функции
        ```kotlin
        val numbers = listOf(1, 2, 3, 4, 5)
        
        // map - трансформация
        val doubled = numbers.map { it * 2 } // [2, 4, 6, 8, 10]
        
        // filter - фильтрация
        val evens = numbers.filter { it % 2 == 0 } // [2, 4]
        
        // reduce - свертка
        val sum = numbers.reduce { acc, i -> acc + i } // 15
        
        // fold - свертка с начальным значением
        val product = numbers.fold(1) { acc, i -> acc * i } // 120
        
        // flatMap - трансформация и сглаживание
        val nested = listOf(listOf(1, 2), listOf(3, 4))
        val flat = nested.flatMap { it } // [1, 2, 3, 4]
        ```
        
        ### Комбинирование функций
        ```kotlin
        val result = numbers
            .filter { it > 2 }        // [3, 4, 5]
            .map { it * 3 }           // [9, 12, 15]
            .take(2)                  // [9, 12]
            .reduce { acc, i -> acc + i } // 21
        ```
        
        ## Функциональные типы
        
        ### Typealias для функциональных типов
        ```kotlin
        typealias Transformer<T, R> = (T) -> R
        typealias Predicate<T> = (T) -> Boolean
        typealias Action = () -> Unit
        
        fun <T> List<T>.filter(predicate: Predicate<T>): List<T> {
            return this.filter(predicate)
        }
        ```
        
        ### Функции как значения
        ```kotlin
        val operations = mapOf<String, (Int, Int) -> Int>(
            "add" to { a, b -> a + b },
            "subtract" to { a, b -> a - b },
            "multiply" to { a, b -> a * b },
            "divide" to { a, b -> a / b }
        )
        
        val result = operations["add"]?.invoke(10, 5) // 15
        ```
        
        ## Композиция функций
        
        ### Ручная композиция
        ```kotlin
        fun <A, B, C> compose(f: (B) -> C, g: (A) -> B): (A) -> C {
            return { x -> f(g(x)) }
        }
        
        val addOne = { x: Int -> x + 1 }
        val double = { x: Int -> x * 2 }
        val addThenDouble = compose(double, addOne)
        
        println(addThenDouble(5)) // 12
        ```
        
        ### Инфиксная композиция
        ```kotlin
        infix fun <A, B, C> ((B) -> C).compose(g: (A) -> B): (A) -> C {
            return { x -> this(g(x)) }
        }
        
        val doubleThenAdd = addOne compose double
        println(doubleThenAdd(5)) // 11
        ```
        
        ## Каррирование (Currying)
        
        ```kotlin
        // Обычная функция
        fun add(a: Int, b: Int): Int = a + b
        
        // Каррированная версия
        fun curriedAdd(a: Int): (Int) -> Int = { b -> a + b }
        
        // Использование
        val addFive = curriedAdd(5)
        println(addFive(3)) // 8
        
        // Автоматическое каррирование
        fun <A, B, C> curry(f: (A, B) -> C): (A) -> (B) -> C {
            return { a -> { b -> f(a, b) } }
        }
        
        val curriedMultiply = curry { a: Int, b: Int -> a * b }
        val multiplyByThree = curriedMultiply(3)
        println(multiplyByThree(4)) // 12
        ```
        
        ## Частичное применение (Partial Application)
        
        ```kotlin
        fun <A, B, C> partial(f: (A, B) -> C, a: A): (B) -> C {
            return { b -> f(a, b) }
        }
        
        val multiply = { a: Int, b: Int -> a * b }
        val double = partial(multiply, 2)
        println(double(5)) // 10
        ```
        
        ## Функциональные структуры данных
        
        ### Either (Monad)
        ```kotlin
        sealed class Either<out L, out R> {
            data class Left<out L>(val value: L) : Either<L, Nothing>()
            data class Right<out R>(val value: R) : Either<Nothing, R>()
            
            fun <T> fold(leftOp: (L) -> T, rightOp: (R) -> T): T = when (this) {
                is Left -> leftOp(value)
                is Right -> rightOp(value)
            }
            
            fun <T> map(f: (R) -> T): Either<L, T> = when (this) {
                is Left -> Left(value)
                is Right -> Right(f(value))
            }
            
            fun <T> flatMap(f: (R) -> Either<L, T>): Either<L, T> = when (this) {
                is Left -> Left(value)
                is Right -> f(value)
            }
        }
        
        // Использование
        fun parseNumber(s: String): Either<String, Int> = try {
            Either.Right(s.toInt())
        } catch (e: NumberFormatException) {
            Either.Left("Не число: " + s)
        }
        ```
        
        ### Option (Maybe)
        ```kotlin
        sealed class Option<out T> {
            data class Some<out T>(val value: T) : Option<T>()
            object None : Option<Nothing>()
            
            fun <R> map(f: (T) -> R): Option<R> = when (this) {
                is Some -> Some(f(value))
                is None -> None
            }
            
            fun <R> flatMap(f: (T) -> Option<R>): Option<R> = when (this) {
                is Some -> f(value)
                is None -> None
            }
            
            fun getOrElse(default: () -> T): T = when (this) {
                is Some -> value
                is None -> default()
            }
        }
        
        fun findUser(id: Int): Option<String> = if (id > 0) {
            Option.Some("User id")
        } else {
            Option.None
        }
        ```
        
        ## Tail Recursion (Хвостовая рекурсия)
        
        ```kotlin
        // Обычная рекурсия (может вызвать StackOverflow)
        fun factorial(n: Int): Long {
            return if (n <= 1) 1 else n * factorial(n - 1)
        }
        
        // Хвостовая рекурсия (оптимизируется в цикл)
        tailrec fun factorialTail(n: Int, acc: Long = 1): Long {
            return if (n <= 1) acc else factorialTail(n - 1, n * acc)
        }
        
        tailrec fun <T> find(predicate: (T) -> Boolean, list: List<T>): T? {
            return when {
                list.isEmpty() -> null
                predicate(list.first()) -> list.first()
                else -> find(predicate, list.drop(1))
            }
        }
        ```
        
        ## Функциональные библиотеки Kotlin
        
        ### Arrow.kt
        ```kotlin
        // Типы из Arrow
        import arrow.core.Either
        import arrow.core.Option
        import arrow.core.continuations.either
        
        // Использование Either из Arrow
        suspend fun fetchData(): Either<Throwable, String> = either {
            // Логика с безопасными вычислениями
            val data = networkCall().bind()
            processData(data).bind()
        }
        ```
        
        ### Kotlinx.coroutines с функциональным подходом
        ```kotlin
        suspend fun <T, R> List<T>.mapAsync(
            transform: suspend (T) -> R
        ): List<R> = coroutineScope {
            map { async { transform(it) } }.awaitAll()
        }
        ```
        
        ## Функциональные паттерны
        
        ### Railway Oriented Programming
        ```kotlin
        sealed class Result<out T, out E> {
            data class Success<out T>(val value: T) : Result<T, Nothing>()
            data class Failure<out E>(val error: E) : Result<Nothing, E>()
            
            fun <U> map(f: (T) -> U): Result<U, E> = when (this) {
                is Success -> Success(f(value))
                is Failure -> Failure(error)
            }
            
            fun <U> flatMap(f: (T) -> Result<U, E>): Result<U, E> = when (this) {
                is Success -> f(value)
                is Failure -> Failure(error)
            }
        }
        
        // Цепочка операций
        fun process(input: String): Result<String, String> {
            return validate(input)
                .flatMap { parse(it) }
                .flatMap { transform(it) }
                .flatMap { save(it) }
        }
        ```
        
        ### Tagless Final
        ```kotlin
        interface Algebra<F> {
            fun <A> pure(a: A): F<A>
            fun <A, B> map(fa: F<A>, f: (A) -> B): F<B>
            fun <A, B> flatMap(fa: F<A>, f: (A) -> F<B>): F<B>
        }
        
        class OptionAlgebra : Algebra<Option> {
            override fun <A> pure(a: A): Option<A> = Option.Some(a)
            
            override fun <A, B> map(fa: Option<A>, f: (A) -> B): Option<B> = fa.map(f)
            
            override fun <A, B> flatMap(fa: Option<A>, f: (A) -> Option<B>): Option<B> = fa.flatMap(f)
        }
        ```
        
        ## Оптимизации
        
        ### Inline функции высшего порядка
        ```kotlin
        inline fun <T> List<T>.filterInline(
            crossinline predicate: (T) -> Boolean
        ): List<T> {
            val result = mutableListOf<T>()
            for (item in this) {
                if (predicate(item)) {
                    result.add(item)
                }
            }
            return result
        }
        ```
        
        ### Мемоизация (Memoization)
        ```kotlin
        fun <T, R> ((T) -> R).memoize(): (T) -> R {
            val cache = mutableMapOf<T, R>()
            return { key ->
                cache.getOrPut(key) { this(key) }
            }
        }
        
        val expensiveComputation = { n: Int ->
            println("Вычисление для " + n)
            // Дорогостоящая операция
            n * n
        }
        
        val memoized = expensiveComputation.memoize()
        println(memoized(5)) // Вычисляет
        println(memoized(5)) // Использует кэш
        ```
        
        ## Best Practices
        
        1. Используйте чистые функции когда возможно
        2. Предпочитайте неизменяемые данные
        3. Комбинируйте маленькие функции в большие
        4. Используйте типы для моделирования домена
        5. Обрабатывайте ошибки через Either/Result
        6. Используйте хвостовую рекурсию для рекурсивных алгоритмов
    """.trimIndent(),
                codeExample = """
        import kotlin.system.measureTimeMillis
        
        fun main() {
            println("=== Функциональное программирование в Kotlin ===")
            
            // 1. Чистые vs Нечистые функции
            println("
    1. Чистые и нечистые функции:")
            println("Чистая функция add(2, 3) = " + add(2, 3))
            println("Чистая функция add(2, 3) = " + add(2, 3) + " (всегда одинаково)")
            
            println("Нечистая функция increment() = " + increment())
            println("Нечистая функция increment() = " + increment() + " (разные значения)")
            
            // 2. Функции высшего порядка
            println("
    2. Функции высшего порядка:")
            val numbers = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
            println("Исходный список: " + numbers)
            
            val transformed = numbers
                .filter { it % 2 == 0 }      // четные
                .map { it * it }             // квадраты
                .take(3)                     // первые 3
                .reduce { acc, i -> acc + i } // сумма
            
            println("Результат трансформации: " + transformed)
            
            // 3. Замыкания
            println("
    3. Замыкания:")
            val counter1 = makeCounter()
            val counter2 = makeCounter()
            
            println("Счетчик 1: " + counter1() + ", " + counter1() + ", " + counter1())
            println("Счетчик 2: " + counter2() + ", " + counter2())
            println("Счетчик 1: " + counter1() + " (продолжает с того же места)")
            
            // 4. Функциональные типы и композиция
            println("
    4. Композиция функций:")
            
            val addTwo = { x: Int -> x + 2 }
            val multiplyByThree = { x: Int -> x * 3 }
            val square = { x: Int -> x * x }
            
            // Композиция вручную
            val addThenMultiply = addTwo compose multiplyByThree
            println("addThenMultiply(5) = " + addThenMultiply(5)) // (5 + 2) * 3 = 21
            
            val complex = square compose addTwo compose multiplyByThree
            println("complex(5) = " + complex(5)) // square((5 * 3) + 2) = 289
            
            // 5. Каррирование
            println("
    5. Каррирование:")
            
            val curriedAdd = curry { a: Int, b: Int -> a + b }
            val addFive = curriedAdd(5)
            println("addFive(3) = " + addFive(3)) // 8
            
            // Каррирование с тремя параметрами
            val curriedMultiply3 = curry3 { a: Int, b: Int, c: Int -> a * b * c }
            val multiplyByTwo = curriedMultiply3(2)
            val multiplyByTwoAndThree = multiplyByTwo(3)
            println("multiplyByTwoAndThree(4) = " + multiplyByTwoAndThree(4)) // 24
            
            // 6. Частичное применение
            println("
    6. Частичное применение:")
            
            val formatMessage = { prefix: String, name: String, suffix: String ->
                "prefix, name! suffix"
            }
            
            val greetUser = partial(formatMessage, "Привет")
            val formalGreet = greetUser("Алексей", "Рад вас видеть")
            val informalGreet = greetUser("Друг", "Как дела?")
            
            println("Формальное: " + formalGreet)
            println("Неформальное: " + informalGreet)
            
            // 7. Either Monad
            println("
    7. Either Monad:")
            
            val result1 = parseNumber("123")
            val result2 = parseNumber("abc")
            
            println("Результат 123: " + result1.fold(
                { error -> "Ошибка: " + error },
                { value -> "Успех: " + value }
            ))
            
            println("Результат abc: " + result2.fold(
                { error -> "Ошибка: " + error },
                { value -> "Успех: " + value }
            ))
            
            // Цепочка вычислений с Either
            val computation = calculateSafely("10", "5", "2")
            println("Безопасное вычисление: " + computation.fold(
                { error -> "Ошибка: " + error },
                { value -> "Результат: " + value }
            ))
            
            // 8. Option Monad
            println("
    8. Option Monad:")
            
            val user1 = findUser(1)
            val user2 = findUser(-1)
            
            println("Пользователь 1: " + user1.getOrElse { "Не найден" })
            println("Пользователь -1: " + user2.getOrElse { "Не найден" })
            
            // Цепочка вычислений с Option
            val processedUser = processUser(42)
            println("Обработанный пользователь: " + processedUser.getOrElse { "Ошибка обработки" })
            
            // 9. Хвостовая рекурсия
            println("
    9. Хвостовая рекурсия:")
            
            val largeNumber = 10000
            val time = measureTimeMillis {
                val fact = factorialTail(largeNumber, 1)
                println("Факториал largeNumber (первые 10 цифр): " + fact.toString().take(10) + "...")
            }
            println("Время вычисления: " + time + "мс")
            
            // 10. Мемоизация
            println("
    10. Мемоизация:")
            
            val fibonacci = { n: Int ->
                println("Вычисление fibonacci(n)")
                when (n) {
                    0 -> 0
                    1 -> 1
                    else -> fibonacciMemo(n - 1) + fibonacciMemo(n - 2)
                }
            }
            
            val fibonacciMemo = fibonacci.memoize()
            
            println("fibonacci(10) = " + fibonacciMemo(10))
            println("fibonacci(10) = " + fibonacciMemo(10) + " (из кэша)")
            println("fibonacci(5) = " + fibonacciMemo(5) + " (из кэша)")
            
            // 11. Функциональные структуры данных
            println("
    11. Функциональные структуры данных:")
            
            val tree = Tree.node(
                Tree.node(Tree.leaf(1), Tree.leaf(2)),
                Tree.node(Tree.leaf(3), Tree.leaf(4))
            )
            
            println("Дерево: " + tree)
            println("Сумма значений: " + tree.sum())
            println("Глубина: " + tree.depth())
            
            println("
    === Демонстрация завершена ===")
        }
        
        // 1. Чистые и нечистые функции
        fun add(a: Int, b: Int): Int = a + b
        
        var counter = 0
        fun increment(): Int = ++counter
        
        // 2. Функции высшего порядка (расширения)
        fun <T, R> List<T>.customMap(transform: (T) -> R): List<R> {
            val result = mutableListOf<R>()
            for (item in this) {
                result.add(transform(item))
            }
            return result
        }
        
        fun <T> List<T>.customFilter(predicate: (T) -> Boolean): List<T> {
            val result = mutableListOf<T>()
            for (item in this) {
                if (predicate(item)) {
                    result.add(item)
                }
            }
            return result
        }
        
        // 3. Замыкания
        fun makeCounter(): () -> Int {
            var count = 0
            return { ++count }
        }
        
        // 4. Композиция функций
        infix fun <A, B, C> ((B) -> C).compose(g: (A) -> B): (A) -> C {
            return { x -> this(g(x)) }
        }
        
        // 5. Каррирование
        fun <A, B, C> curry(f: (A, B) -> C): (A) -> (B) -> C {
            return { a -> { b -> f(a, b) } }
        }
        
        fun <A, B, C, D> curry3(f: (A, B, C) -> D): (A) -> (B) -> (C) -> D {
            return { a -> { b -> { c -> f(a, b, c) } } }
        }
        
        // 6. Частичное применение
        fun <A, B, C> partial(f: (A, B) -> C, a: A): (B) -> C {
            return { b -> f(a, b) }
        }
        
        fun <A, B, C, D> partial3(f: (A, B, C) -> D, a: A): (B) -> (C) -> D {
            return { b -> { c -> f(a, b, c) } }
        }
        
        // 7. Either Monad
        sealed class Either<out L, out R> {
            data class Left<out L>(val value: L) : Either<L, Nothing>()
            data class Right<out R>(val value: R) : Either<Nothing, R>()
            
            fun <T> fold(leftOp: (L) -> T, rightOp: (R) -> T): T = when (this) {
                is Left -> leftOp(value)
                is Right -> rightOp(value)
            }
            
            fun <T> map(f: (R) -> T): Either<L, T> = when (this) {
                is Left -> Left(value)
                is Right -> Right(f(value))
            }
            
            fun <T> flatMap(f: (R) -> Either<L, T>): Either<L, T> = when (this) {
                is Left -> Left(value)
                is Right -> f(value)
            }
        }
        
        fun parseNumber(s: String): Either<String, Int> = try {
            Either.Right(s.toInt())
        } catch (e: NumberFormatException) {
            Either.Left("Не число: " + s)
        }
        
        fun divide(a: Int, b: Int): Either<String, Int> = if (b == 0) {
            Either.Left("Деление на ноль")
        } else {
            Either.Right(a / b)
        }
        
        fun calculateSafely(aStr: String, bStr: String, cStr: String): Either<String, Int> {
            return parseNumber(aStr)
                .flatMap { a ->
                    parseNumber(bStr).flatMap { b ->
                        parseNumber(cStr).flatMap { c ->
                            divide(a + b, c)
                        }
                    }
                }
        }
        
        // 8. Option Monad
        sealed class Option<out T> {
            data class Some<out T>(val value: T) : Option<T>()
            object None : Option<Nothing>()
            
            fun <R> map(f: (T) -> R): Option<R> = when (this) {
                is Some -> Some(f(value))
                is None -> None
            }
            
            fun <R> flatMap(f: (T) -> Option<R>): Option<R> = when (this) {
                is Some -> f(value)
                is None -> None
            }
            
            fun getOrElse(default: () -> T): T = when (this) {
                is Some -> value
                is None -> default()
            }
            
            fun isEmpty(): Boolean = this == None
            fun isNotEmpty(): Boolean = !isEmpty()
        }
        
        fun findUser(id: Int): Option<String> = if (id > 0) {
            Option.Some("User id")
        } else {
            Option.None
        }
        
        fun validateUser(user: String): Option<String> = if (user.isNotEmpty()) {
            Option.Some(user.uppercase())
        } else {
            Option.None
        }
        
        fun processUser(id: Int): Option<String> {
            return findUser(id)
                .flatMap { validateUser(it) }
                .map { "Обработан: " + it }
        }
        
        // 9. Хвостовая рекурсия
        tailrec fun factorialTail(n: Int, acc: Long = 1): Long {
            return if (n <= 1) acc else factorialTail(n - 1, n * acc)
        }
        
        tailrec fun sumList(list: List<Int>, acc: Int = 0): Int {
            return if (list.isEmpty()) acc else sumList(list.drop(1), acc + list.first())
        }
        
        // 10. Мемоизация
        fun <T, R> ((T) -> R).memoize(): (T) -> R {
            val cache = mutableMapOf<T, R>()
            return { key ->
                cache.getOrPut(key) { this(key) }
            }
        }
        
        // 11. Функциональные структуры данных (бинарное дерево)
        sealed class Tree<out T> {
            data class Node<out T>(
                val left: Tree<T>,
                val right: Tree<T>
            ) : Tree<T>()
            
            data class Leaf<out T>(val value: T) : Tree<T>()
            
            companion object {
                fun <T> leaf(value: T): Tree<T> = Leaf(value)
                fun <T> node(left: Tree<T>, right: Tree<T>): Tree<T> = Node(left, right)
            }
            
            fun sum(): Int where T: Number = when (this) {
                is Leaf -> value.toInt()
                is Node -> left.sum() + right.sum()
            }
            
            fun depth(): Int = when (this) {
                is Leaf -> 1
                is Node -> 1 + maxOf(left.depth(), right.depth())
            }
            
            fun <R> map(f: (T) -> R): Tree<R> = when (this) {
                is Leaf -> Leaf(f(value))
                is Node -> Node(left.map(f), right.map(f))
            }
            
            override fun toString(): String = when (this) {
                is Leaf -> value.toString()
                is Node -> "({left.toString()}, {right.toString()})"
            }
        }
        
        // Дополнительные функциональные утилиты
        fun <T> List<T>.customReduce(operation: (T, T) -> T): T? {
            if (isEmpty()) return null
            var accumulator = this[0]
            for (i in 1 until size) {
                accumulator = operation(accumulator, this[i])
            }
            return accumulator
        }
        
        fun <T, R> List<T>.customFlatMap(transform: (T) -> List<R>): List<R> {
            val result = mutableListOf<R>()
            for (item in this) {
                result.addAll(transform(item))
            }
            return result
        }
        
        // Функциональные операторы для коллекций
        operator fun <T> List<T>.plus(element: T): List<T> {
            return this.toMutableList().apply { add(element) }
        }
        
        operator fun <T> List<T>.minus(element: T): List<T> {
            return this.filterNot { it == element }
        }
    """.trimIndent(),
                topic = "functional"
            ),



        )
    }

    fun getLessonById(id: String): Lesson? {
        return getLessons().find { it.id == id }
    }
}