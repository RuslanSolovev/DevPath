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
                """.trimIndent()
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
                """.trimIndent()
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
                """.trimIndent()
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
                """.trimIndent()
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
                """.trimIndent()
            )
        )
    }

    fun getLessonById(id: String): Lesson? {
        return getLessons().find { it.id == id }
    }
}