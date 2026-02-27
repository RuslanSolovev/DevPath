package com.example.devpath.data.repository.lessons

import com.example.devpath.domain.models.Lesson

object Lesson12Functional {
    fun get(): Lesson = Lesson(
        id = "functional_programming",
        title = "🧪 Функциональное программирование",
        description = "Мощные функциональные концепции: монады, композиция, чистые функции и работа с эффектами",
        difficulty = "advanced",
        duration = 45,
        topic = "functional",
        theory = """
            # 🧪 Функциональное программирование в Kotlin

            Kotlin — это **мультипарадигмальный** язык, который отлично поддерживает как объектно-ориентированный, так и **функциональный** стиль программирования. Функциональный подход делает код более предсказуемым, тестируемым и безопасным.

            ## 🎯 Основные принципы

            ### 1. Чистые функции (Pure Functions)

            **Чистая функция** — это функция, которая:
            - Всегда возвращает один и тот же результат для одних и тех же аргументов
            - Не имеет побочных эффектов (не меняет состояние, не взаимодействует с внешним миром)

            ```kotlin
            // ✅ Чистая функция
            fun add(a: Int, b: Int): Int = a + b
            
            // ❌ Нечистая функция (зависит от внешнего состояния)
            var counter = 0
            fun increment(): Int = ++counter
            
            // ❌ Нечистая функция (побочный эффект - печать)
            fun printAndAdd(a: Int, b: Int): Int {
                println("Складываю ${'$'}a и ${'$'}b")
                return a + b
            }
            ```

            ### 2. Неизменяемость (Immutability)

            Предпочитайте неизменяемые данные (`val`) изменяемым (`var`). Это исключает целый класс ошибок.

            ```kotlin
            val list = listOf(1, 2, 3)        // Неизменяемый список
            // list.add(4)                     // ❌ Ошибка!
            
            val newList = list + 4             // ✅ Создаётся новый список [1, 2, 3, 4]
            val filtered = list.filter { it > 1 } // [2, 3]
            ```

            ### 3. Функции как значения (First-class functions)

            Функции могут быть:
            - Присвоены переменным
            - Переданы как параметры
            - Возвращены из других функций

            ```kotlin
            // Функция как переменная
            val square: (Int) -> Int = { x -> x * x }
            
            // Функция как параметр
            fun operate(x: Int, y: Int, op: (Int, Int) -> Int): Int = op(x, y)
            
            val result = operate(5, 3) { a, b -> a + b } // 8
            ```

            ## 🔄 Лямбда-выражения

            Лямбда — это анонимная функция, которая может быть передана как выражение.

            ### Синтаксис

            ```kotlin
            // Полная форма
            val sum: (Int, Int) -> Int = { a: Int, b: Int -> a + b }
            
            // Сокращённая (типы выведены)
            val multiply = { a: Int, b: Int -> a * b }
            
            // С одним параметром (можно использовать it)
            val double = { x: Int -> x * 2 }
            val doubleShorter: (Int) -> Int = { it * 2 }
            
            // Без параметров
            val greet = { println("Hello!") }
            ```

            ### Замыкания (Closures)

            Лямбда может захватывать переменные из внешней области видимости:

            ```kotlin
            fun makeCounter(): () -> Int {
                var count = 0
                return {
                    count++ // Захватывает count из внешней функции
                }
            }
            
            val counter = makeCounter()
            println(counter()) // 0
            println(counter()) // 1
            println(counter()) // 2
            ```

            ## 📊 Функции высшего порядка для коллекций

            Kotlin предоставляет богатый набор функций для работы с коллекциями в функциональном стиле.

            ### Основные операции

            | Функция | Описание | Пример |
            |---------|----------|--------|
            | `map` | Преобразует каждый элемент | `list.map { it * 2 }` |
            | `filter` | Оставляет элементы, удовлетворяющие условию | `list.filter { it > 0 }` |
            | `reduce` | Свёртка без начального значения | `list.reduce { acc, i -> acc + i }` |
            | `fold` | Свёртка с начальным значением | `list.fold(0) { acc, i -> acc + i }` |
            | `flatMap` | Преобразует и сглаживает | `list.flatMap { listOf(it, it*10) }` |
            | `groupBy` | Группирует по ключу | `list.groupBy { it % 2 }` |
            | `take` | Берёт первые n элементов | `list.take(3)` |
            | `drop` | Пропускает первые n элементов | `list.drop(2)` |

            ```kotlin
            val numbers = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
            
            // Цепочка операций
            val result = numbers
                .filter { it % 2 == 0 }      // [2, 4, 6, 8, 10]
                .map { it * it }              // [4, 16, 36, 64, 100]
                .take(3)                       // [4, 16, 36]
                .sum()                         // 56
            
            println(result) // 56
            ```

            ### Группировка и агрегация

            ```kotlin
            data class Person(val name: String, val age: Int)
            
            val people = listOf(
                Person("Анна", 25),
                Person("Борис", 30),
                Person("Виктор", 25),
                Person("Дарья", 30)
            )
            
            // Группировка по возрасту
            val byAge = people.groupBy { it.age }
            // {25=[Анна, Виктор], 30=[Борис, Дарья]}
            
            // Подсчёт количества по возрасту
            val countByAge = people.groupingBy { it.age }.eachCount()
            // {25=2, 30=2}
            
            // Средний возраст
            val averageAge = people.map { it.age }.average()
            ```

            ## 🔗 Композиция функций

            Композиция позволяет объединять маленькие функции в большие.

            ```kotlin
            // Функции-кирпичики
            val addOne = { x: Int -> x + 1 }
            val double = { x: Int -> x * 2 }
            val square = { x: Int -> x * x }
            
            // Ручная композиция
            fun <A, B, C> compose(f: (B) -> C, g: (A) -> B): (A) -> C = { x -> f(g(x)) }
            
            val addOneThenDouble = compose(double, addOne)
            println(addOneThenDouble(5)) // (5 + 1) * 2 = 12
            
            // Инфиксная композиция (более читаемо)
            infix fun <A, B, C> ((B) -> C).compose(g: (A) -> B): (A) -> C = { x -> this(g(x)) }
            
            val pipeline = square compose addOne compose double
            println(pipeline(3)) // ((3 * 2) + 1)² = 49
            ```

            ## 🎭 Функциональные структуры данных

            ### Option (Maybe) — для безопасной работы с null

            Option представляет значение, которое может отсутствовать, но в отличие от null, это типобезопасно.

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
                
                fun orElse(alternative: () -> Option<T>): Option<T> = when (this) {
                    is Some -> this
                    is None -> alternative()
                }
            }
            
            // Создание Option
            fun findUser(id: Int): Option<String> = when (id) {
                1 -> Option.Some("Алексей")
                2 -> Option.Some("Елена")
                else -> Option.None
            }
            
            // Использование
            val user = findUser(1)
                .map { it.uppercase() }
                .flatMap { name -> findUser(2) }
                .getOrElse { "Не найден" }
            ```

            ### Either (Result) — для обработки ошибок

            Either представляет значение одного из двух возможных типов: успех (Right) или ошибка (Left).

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
                
                fun getOrElse(default: () -> R): R = when (this) {
                    is Right -> value
                    is Left -> default()
                }
            }
            
            // Безопасное деление
            fun divide(a: Int, b: Int): Either<String, Int> = when {
                b == 0 -> Either.Left("Деление на ноль!")
                else -> Either.Right(a / b)
            }
            
            // Цепочка операций
            val result = divide(10, 2)
                .map { it * 3 }
                .flatMap { x -> divide(x, 2) }
                .fold(
                    { error -> "Ошибка: ${'$'}error" },
                    { value -> "Результат: ${'$'}value" }
                )
            ```

            ## 🔁 Каррирование (Currying)

            Каррирование — преобразование функции от нескольких аргументов в последовательность функций от одного аргумента.

            ```kotlin
            // Обычная функция
            fun add(a: Int, b: Int): Int = a + b
            
            // Каррированная версия
            fun curriedAdd(a: Int): (Int) -> Int = { b -> a + b }
            
            val addFive = curriedAdd(5)
            println(addFive(3)) // 8
            
            // Универсальное каррирование
            fun <A, B, C> curry(f: (A, B) -> C): (A) -> (B) -> C = { a -> { b -> f(a, b) } }
            
            val multiply = { a: Int, b: Int -> a * b }
            val curriedMultiply = curry(multiply)
            val double = curriedMultiply(2)
            println(double(10)) // 20
            ```

            ## 🔁 Хвостовая рекурсия (Tail Recursion)

            Обычная рекурсия может привести к переполнению стека. Хвостовая рекурсия оптимизируется компилятором в цикл.

            ```kotlin
            // ❌ Обычная рекурсия (опасно для больших чисел)
            fun factorial(n: Int): Long = when (n) {
                0, 1 -> 1
                else -> n * factorial(n - 1)
            }
            
            // ✅ Хвостовая рекурсия (безопасно)
            tailrec fun factorialTail(n: Int, acc: Long = 1): Long = when (n) {
                0, 1 -> acc
                else -> factorialTail(n - 1, n * acc)
            }
            
            // Хвостовая рекурсия для списков
            tailrec fun <T> find(
                predicate: (T) -> Boolean,
                list: List<T>
            ): T? = when {
                list.isEmpty() -> null
                predicate(list.first()) -> list.first()
                else -> find(predicate, list.drop(1))
            }
            ```

            ## ⚡ Мемоизация (Memoization)

            Кэширование результатов дорогостоящих вычислений.

            ```kotlin
            fun <T, R> ((T) -> R).memoize(): (T) -> R {
                val cache = mutableMapOf<T, R>()
                return { key ->
                    cache.getOrPut(key) {
                        println("Вычисляю для ${'$'}key")
                        this(key)
                    }
                }
            }
            
            val expensive: (Int) -> Int = { n ->
                Thread.sleep(1000) // Имитация долгого вычисления
                n * n
            }
            
            val memoized = expensive.memoize()
            
            println(memoized(5)) // Вычисляет (1 сек)
            println(memoized(5)) // Из кэша (мгновенно)
            ```

            ## 📈 Функциональные паттерны

            ### Railway Oriented Programming

            Программа представляется как железная дорога с двумя путями: успех и ошибка.

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
            fun processUser(input: String): Result<String, String> =
                validate(input)
                    .flatMap { parse(it) }
                    .flatMap { save(it) }
            ```

            ### Typealias для читаемости

            ```kotlin
            typealias Transformer<T, R> = (T) -> R
            typealias Predicate<T> = (T) -> Boolean
            typealias Effect = () -> Unit
            typealias Async<T> = suspend () -> T
            
            fun <T> List<T>.filterValid(predicate: Predicate<T>): List<T> = filter(predicate)
            ```

            ## 🎯 Практические примеры

            ### 1. Обработка данных

            ```kotlin
            data class Order(val id: Int, val amount: Double, val customerId: Int)
            data class Customer(val id: Int, val name: String, val email: String)
            
            fun processOrders(orders: List<Order>, customers: List<Customer>): Map<String, Double> =
                orders
                    .filter { it.amount > 100 }                  // Только крупные заказы
                    .groupBy { order ->                           // Группируем по клиентам
                        customers.find { it.id == order.customerId }?.name ?: "Unknown"
                    }
                    .mapValues { (_, orders) -> orders.sumOf { it.amount } } // Сумма по клиенту
            ```

            ### 2. Валидация форм

            ```kotlin
            data class ValidationError(val field: String, val message: String)
            typealias ValidationResult = Either<List<ValidationError>, Unit>
            
            fun validateEmail(email: String): ValidationResult = when {
                email.isBlank() -> Either.Left(listOf(ValidationError("email", "Email не может быть пустым")))
                !email.contains("@") -> Either.Left(listOf(ValidationError("email", "Некорректный email")))
                else -> Either.Right(Unit)
            }
            
            fun validatePassword(password: String): ValidationResult = when {
                password.length < 8 -> Either.Left(listOf(ValidationError("password", "Минимум 8 символов")))
                !password.any { it.isDigit() } -> Either.Left(listOf(ValidationError("password", "Нужна хотя бы одна цифра")))
                else -> Either.Right(Unit)
            }
            
            fun validateForm(email: String, password: String): ValidationResult =
                validateEmail(email).flatMap { validatePassword(password) }
            ```

            ### 3. Пайплайн обработки

            ```kotlin
            val pipeline = { data: String ->
                data
                    .takeIf { it.isNotBlank() }
                    ?.let { it.uppercase() }
                    ?.split(" ")
                    ?.map { it.length }
                    ?.filter { it > 3 }
                    ?.sum()
                    ?: 0
            }
            ```

            ## 📚 Лучшие практики

            1. **Используйте чистые функции** где возможно — они легче тестируются и отлаживаются
            2. **Предпочитайте неизменяемость** — изменяемое состояние — источник ошибок
            3. **Комбинируйте маленькие функции** в большие через композицию
            4. **Используйте Option вместо null** — это типобезопасно и явно
            5. **Обрабатывайте ошибки через Either/Result**, а не исключения
            6. **Используйте хвостовую рекурсию** для рекурсивных алгоритмов
            7. **Мемоизируйте** дорогостоящие чистые функции
            8. **Думайте в терминах преобразований данных**, а не инструкций

            ## 🎓 Заключение

            Функциональное программирование в Kotlin — это не "всё или ничего". Вы можете постепенно внедрять функциональные концепции в свой код: начать с использования `map` и `filter`, затем перейти к композиции функций, а позже — к монадам и функциональным эффектам.

            Главное преимущество ФП — **предсказуемость**. Когда функция чиста и данные неизменяемы, вы точно знаете, что произойдёт, и можете легко тестировать и отлаживать код.
        """.trimIndent(),
        codeExample = """
            import kotlin.system.measureTimeMillis
            
            fun main() {
                println("🎯 Функциональное программирование в Kotlin\n")
                
                // 1️⃣ Чистые и нечистые функции
                println("1️⃣ Чистые vs Нечистые функции")
                println("   Чистая функция add(2, 3) = ${'$'}{add(2, 3)}")
                println("   Чистая функция add(2, 3) = ${'$'}{add(2, 3)} (всегда одинаково)")
                
                var counter = 0
                fun increment(): Int = ++counter
                println("   Нечистая функция increment() = ${'$'}{increment()}")
                println("   Нечистая функция increment() = ${'$'}{increment()} (разные значения)")
                
                // 2️⃣ Функции высшего порядка
                println("\n2️⃣ Функции высшего порядка")
                val numbers = (1..10).toList()
                println("   Исходный список: ${'$'}numbers")
                
                val result = numbers
                    .filter { it % 2 == 0 }    // чётные
                    .map { it * it }            // квадраты
                    .take(3)                     // первые 3
                    .sum()                       // сумма
                
                println("   Результат пайплайна: ${'$'}result")
                
                // 3️⃣ Замыкания
                println("\n3️⃣ Замыкания")
                fun makeCounter(): () -> Int {
                    var count = 0
                    return { ++count }
                }
                
                val counter1 = makeCounter()
                val counter2 = makeCounter()
                
                println("   Счётчик 1: ${'$'}{counter1()}, ${'$'}{counter1()}, ${'$'}{counter1()}")
                println("   Счётчик 2: ${'$'}{counter2()}, ${'$'}{counter2()}")
                
                // 4️⃣ Композиция функций
                println("\n4️⃣ Композиция функций")
                val addTwo = { x: Int -> x + 2 }
                val multiplyByThree = { x: Int -> x * 3 }
                val square = { x: Int -> x * x }
                
                infix fun <A, B, C> ((B) -> C).compose(g: (A) -> B): (A) -> C = { x -> this(g(x)) }
                
                val pipeline = square compose addTwo compose multiplyByThree
                println("   (x*3 + 2)² для x=5: ${'$'}{pipeline(5)}") // ((5*3)+2)² = 289
                
                // 5️⃣ Каррирование
                println("\n5️⃣ Каррирование")
                fun <A, B, C> curry(f: (A, B) -> C): (A) -> (B) -> C = { a -> { b -> f(a, b) } }
                
                val multiply = { a: Int, b: Int -> a * b }
                val curriedMultiply = curry(multiply)
                val double = curriedMultiply(2)
                val triple = curriedMultiply(3)
                
                println("   double(10) = ${'$'}{double(10)}")
                println("   triple(10) = ${'$'}{triple(10)}")
                
                // 6️⃣ Option Monad
                println("\n6️⃣ Option Monad (Maybe)")
                
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
                
                fun findUser(id: Int): Option<String> = when (id) {
                    1 -> Option.Some("Алексей")
                    2 -> Option.Some("Елена")
                    else -> Option.None
                }
                
                val user = findUser(1)
                    .map { it.uppercase() }
                    .getOrElse { "Не найден" }
                
                println("   Найден пользователь: ${'$'}user")
                
                val notFound = findUser(99)
                    .map { it.uppercase() }
                    .getOrElse { "Не найден" }
                
                println("   Пользователь 99: ${'$'}notFound")
                
                // 7️⃣ Either Monad
                println("\n7️⃣ Either Monad (Result)")
                
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
                
                fun divide(a: Int, b: Int): Either<String, Int> = when {
                    b == 0 -> Either.Left("Деление на ноль!")
                    else -> Either.Right(a / b)
                }
                
                val calc = divide(10, 2)
                    .map { it * 3 }
                    .flatMap { x -> divide(x, 2) }
                    .fold(
                        { error -> "Ошибка: ${'$'}error" },
                        { value -> "Результат: ${'$'}value" }
                    )
                
                println("   Результат вычислений: ${'$'}calc")
                
                val calcError = divide(10, 0)
                    .map { it * 3 }
                    .fold(
                        { error -> "Ошибка: ${'$'}error" },
                        { value -> "Результат: ${'$'}value" }
                    )
                
                println("   Результат с ошибкой: ${'$'}calcError")
                
                // 8️⃣ Хвостовая рекурсия
                println("\n8️⃣ Хвостовая рекурсия")
                
                tailrec fun factorialTail(n: Int, acc: Long = 1): Long = when (n) {
                    0, 1 -> acc
                    else -> factorialTail(n - 1, n * acc)
                }
                
                println("   factorialTail(10) = ${'$'}{factorialTail(10)}")
                println("   factorialTail(20) = ${'$'}{factorialTail(20)}")
                
                val time = measureTimeMillis {
                    val fact = factorialTail(10000)
                    println("   factorialTail(10000) вычислен (первые 10 цифр: ${'$'}{fact.toString().take(10)}...)")
                }
                println("   Время вычисления: ${'$'}time мс")
                
                // 9️⃣ Мемоизация
                println("\n9️⃣ Мемоизация")
                
                fun <T, R> ((T) -> R).memoize(): (T) -> R {
                    val cache = mutableMapOf<T, R>()
                    return { key ->
                        cache.getOrPut(key) {
                            println("   ➤ Вычисляю для ${'$'}key")
                            this(key)
                        }
                    }
                }
                
                val fibonacci: (Int) -> Long = { n ->
                    when (n) {
                        0 -> 0
                        1 -> 1
                        else -> fibonacci(n - 1) + fibonacci(n - 2)
                    }
                }
                
                val fastFibonacci = fibonacci.memoize()
                
                println("   fastFibonacci(10) = ${'$'}{fastFibonacci(10)}")
                println("   fastFibonacci(10) = ${'$'}{fastFibonacci(10)} (из кэша)")
                
                // 🔟 Практический пример: обработка данных
                println("\n🔟 Практический пример: аналитика заказов")
                
                data class Order(val id: Int, val amount: Double, val customerId: Int)
                data class Customer(val id: Int, val name: String)
                
                val orders = listOf(
                    Order(1, 150.0, 1),
                    Order(2, 75.0, 2),
                    Order(3, 200.0, 1),
                    Order(4, 50.0, 3),
                    Order(5, 300.0, 2),
                    Order(6, 120.0, 1)
                )
                
                val customers = listOf(
                    Customer(1, "Анна"),
                    Customer(2, "Борис"),
                    Customer(3, "Виктор")
                )
                
                val analytics = orders
                    .filter { it.amount > 100 }
                    .groupBy { order ->
                        customers.find { it.id == order.customerId }?.name ?: "Unknown"
                    }
                    .mapValues { (_, custOrders) ->
                        custOrders.map { it.amount }.run {
                            "сумма=${'$'}{sum()}, среднее=${'$'}{average()}, кол-во=${'$'}size"
                        }
                    }
                
                println("   Аналитика по клиентам:")
                analytics.forEach { (name, stats) ->
                    println("     ${'$'}name: ${'$'}stats")
                }
            }
            
            // Чистая функция
            fun add(a: Int, b: Int): Int = a + b
        """.trimIndent()
    )
}