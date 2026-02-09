package com.example.devpath.data.repository

import com.example.devpath.domain.models.PracticeTask

object PracticeRepository {
    fun getPracticeTasks(): List<PracticeTask> {
        return listOf(
            PracticeTask(
                id = "hello_world",
                title = "Hello World",
                description = "Напишите функцию main(), которая выводит 'Hello, DevPath!' с помощью println()",
                starterCode = """fun main() {
    // Ваш код здесь
}""".trimIndent(),
                solution = "", // Не используется при гибкой проверке
                hint = "Используйте: println(\"Hello, DevPath!\")",
                topic = "kotlin_basics"
            ),
            PracticeTask(
                id = "variables_sum",
                title = "Сумма переменных",
                description = "Создайте две неизменяемые переменные: a = 5 и b = 10. Выведите их сумму с помощью println()",
                starterCode = """fun main() {
    // Объявите переменные и выведите сумму
}""".trimIndent(),
                solution = "",
                hint = "Используйте: val a = 5, val b = 10, println(a + b)",
                topic = "kotlin_basics"
            ),
            PracticeTask(
                id = "conditional_if",
                title = "Условный оператор if",
                description = "Создайте переменную number. Используя if-else, определите, является ли число чётным, и выведите результат",
                starterCode = """fun main() {
    // Создайте переменную number
    // Используйте if-else для проверки чётности
}""".trimIndent(),
                solution = "",
                hint = "Используйте: if (number % 2 == 0) { ... } else { ... }",
                topic = "control_flow"
            ),
            PracticeTask(
                id = "for_loop",
                title = "Цикл for",
                description = "Используя цикл for, выведите все числа от 1 до 5 включительно",
                starterCode = """fun main() {
    // Используйте цикл for для вывода чисел 1-5
}""".trimIndent(),
                solution = "",
                hint = "Используйте: for (i in 1..5) { println(i) }",
                topic = "loops"
            ),
            PracticeTask(
                id = "while_loop",
                title = "Цикл while",
                description = "Используя цикл while, выведите числа от 1 до 5. Создайте счётчик counter = 1",
                starterCode = """fun main() {
    // Используйте цикл while для вывода чисел 1-5
}""".trimIndent(),
                solution = "",
                hint = "Используйте: var counter = 1; while (counter <= 5) { println(counter); counter++ }",
                topic = "loops"
            ),
            PracticeTask(
                id = "function_basic",
                title = "Функции",
                description = "Создайте функцию multiply(a: Int, b: Int), которая возвращает произведение двух чисел. Вызовите её и выведите результат",
                starterCode = """fun main() {
    // Создайте функцию multiply и вызовите её
}

// Объявите функцию multiply здесь""".trimIndent(),
                solution = "",
                hint = "Используйте: fun multiply(a: Int, b: Int): Int { return a * b }",
                topic = "functions"
            ),
            PracticeTask(
                id = "list_operations",
                title = "Работа со списками",
                description = "Создайте список чисел (например, 1, 2, 3, 4, 5) и выведите каждый элемент с помощью forEach или цикла for",
                starterCode = """fun main() {
    // Создайте список и переберите его элементы
}""".trimIndent(),
                solution = "",
                hint = "Используйте: val list = listOf(1, 2, 3, 4, 5); list.forEach { println(it) }",
                topic = "collections"
            ),
            PracticeTask(
                id = "string_template",
                title = "Шаблоны строк",
                description = "Создайте переменные name и age, затем создайте строку \"Меня зовут \$name, мне \$age лет\" и выведите её",
                starterCode = """fun main() {
    // Создайте переменные name и age
    // Используйте шаблон строки для вывода
}""".trimIndent(),
                solution = "",
                hint = "Используйте: val name = \"Имя\"; val age = 25; println(\"Меня зовут \$name, мне \$age лет\")",
                topic = "strings"
            ),
            PracticeTask(
                id = "null_safety",
                title = "Null безопасность",
                description = "Создайте nullable переменную типа String?. Используя оператор ?:, задайте значение по умолчанию \"Гость\", если переменная равна null",
                starterCode = """fun main() {
    // Создайте nullable переменную и обработайте null
}""".trimIndent(),
                solution = "",
                hint = "Используйте: val name: String? = null; val result = name ?: \"Гость\"; println(result)",
                topic = "null_safety"
            ),
            PracticeTask(
                id = "when_expression",
                title = "When выражение",
                description = "Создайте переменную grade с буквенной оценкой (A, B, C). Используя when-выражение, определите качество оценки и выведите результат",
                starterCode = """fun main() {
    // Создайте переменную grade
    // Используйте when для определения качества оценки
}""".trimIndent(),
                solution = "",
                hint = "Используйте: val grade = \"A\"; when (grade) { \"A\" -> println(\"Отлично\") \"B\" -> println(\"Хорошо\") else -> println(\"Удовлетворительно\") }",
                topic = "control_flow"
            ),

            // === Дополнительные практические задания ===

// 1. variables_types (Переменные и типы)
            PracticeTask(
                id = "variables_types",
                title = "Переменные и типы данных",
                description = "Объявите разные типы переменных: String, Int, Double, Boolean. Используйте как val, так и var. Выведите их значения и типы",
                starterCode = """fun main() {
    // Объявите переменные разных типов
    // 1. Неизменяемую строку
    // 2. Изменяемое целое число
    // 3. Число с плавающей точкой
    // 4. Логическое значение
    // Выведите все значения с пояснениями
}""".trimIndent(),
                solution = "",
                hint = "Пример: val name: String = \"Kotlin\"; var count: Int = 42; val pi: Double = 3.14; var isReady: Boolean = true",
                topic = "variables_types"
            ),
            PracticeTask(
                id = "type_inference",
                title = "Вывод типов",
                description = "Объявите переменные без явного указания типа и позвольте Kotlin определить тип автоматически. Проверьте вывод типов для разных значений",
                starterCode = """fun main() {
    // Объявите переменные без указания типа
    // Пусть компилятор сам определит тип
    // Проверьте, какой тип определился
    
    val text = "Hello" // String
    // Добавьте другие типы...
    
    // Выведите значения с информацией о типах
}""".trimIndent(),
                solution = "",
                hint = "Kotlin сам определяет тип: val number = 42 (Int), val decimal = 3.14 (Double), val flag = true (Boolean)",
                topic = "variables_types"
            ),

// 2. delegation (Делегирование)
            PracticeTask(
                id = "lazy_delegation",
                title = "Отложенная инициализация (lazy)",
                description = "Создайте свойство с делегатом lazy, которое будет вычисляться только при первом обращении. Выведите значение дважды и убедитесь, что вычисление происходит один раз",
                starterCode = """fun main() {
    // Создайте свойство с делегатом lazy
    // Выведите значение дважды
    
    // Пример:
    // val expensiveValue: String by lazy {
    //     println("Вычисляю значение...")
    //     "Результат вычисления"
    // }
}""".trimIndent(),
                solution = "",
                hint = "Используйте синтаксис: val property by lazy { ... }",
                topic = "delegation"
            ),
            PracticeTask(
                id = "observable_delegation",
                title = "Наблюдаемое свойство (observable)",
                description = "Создайте свойство с делегатом observable, которое выводит сообщение при каждом изменении значения. Измените значение несколько раз и наблюдайте за выводами",
                starterCode = """import kotlin.properties.Delegates

fun main() {
    // Создайте свойство с делегатом observable
    // Изменяйте значение и наблюдайте за сообщениями
    
    var observedValue: String by Delegates.observable("начальное") {
        property, oldValue, newValue ->
        println("${'$'}{property.name} изменилось: ${'$'}oldValue -> ${'$'}newValue")
    }
    
    // Изменяйте значение здесь
}""".trimIndent(),
                solution = "",
                hint = "Используйте Delegates.observable(initialValue) { prop, old, new -> ... }",
                topic = "delegation"
            ),

// 3. extensions (Расширения)
            PracticeTask(
                id = "string_extension",
                title = "Расширения для String",
                description = "Создайте extension-функцию для класса String, которая добавляет восклицательный знак в конец. Создайте ещё одно расширение, которое проверяет, является ли строка палиндромом",
                starterCode = """fun main() {
    // Протестируйте созданные extension-функции
    
    val greeting = "Hello"
    println(greeting.addExclamation()) // Должно быть "Hello!"
    
    val palindrome = "level"
    println("'${'$'}palindrome' палиндром? ${'$'}{palindrome.isPalindrome()}")
    
    val notPalindrome = "kotlin"
    println("'${'$'}notPalindrome' палиндром? ${'$'}{notPalindrome.isPalindrome()}")
}

// Создайте extension-функцию addExclamation() для String
fun String.addExclamation(): String {
    // Добавьте восклицательный знак
}

// Создайте extension-функцию isPalindrome() для String
fun String.isPalindrome(): Boolean {
    // Проверьте, читается ли строка одинаково с начала и с конца
}""".trimIndent(),
                solution = "",
                hint = "Для проверки палиндрома: this.reversed() == this",
                topic = "extensions"
            ),
            PracticeTask(
                id = "int_extension",
                title = "Расширения для Int",
                description = "Создайте extension-свойство для Int, которое возвращает квадрат числа, и extension-функцию, которая проверяет, является ли число простым",
                starterCode = """fun main() {
    // Протестируйте созданные расширения
    
    val number = 5
    println("Квадрат ${'$'}number = ${'$'}{number.squared}")
    
    println("${'$'}number простое? ${'$'}{number.isPrime()}")
    println("6 простое? ${'$'}{6.isPrime()}")
    println("7 простое? ${'$'}{7.isPrime()}")
}

// Создайте extension-свойство squared для Int
val Int.squared: Int
    get() {
        // Верните квадрат числа
    }

// Создайте extension-функцию isPrime() для Int
fun Int.isPrime(): Boolean {
    // Проверьте, является ли число простым
    // Простое число делится только на 1 и на себя
}""".trimIndent(),
                solution = "",
                hint = "Для проверки простоты числа проверяйте деление на числа от 2 до sqrt(n)",
                topic = "extensions"
            ),

// 4. dsl (DSL и билдеры)
            PracticeTask(
                id = "html_dsl",
                title = "Простой HTML DSL",
                description = "Создайте простой DSL для построения HTML. Реализуйте функции html(), body(), h1(), p(). Используйте лямбды с получателем",
                starterCode = """fun main() {
    // Используйте созданный DSL для построения HTML
    
    val html = html {
        body {
            h1("Добро пожаловать в Kotlin!")
            p("Это пример DSL для HTML")
        }
    }
    
    println(html)
}

// Реализуйте DSL функции

fun html(init: HtmlBuilder.() -> Unit): HtmlBuilder {
    val html = HtmlBuilder()
    html.init()
    return html
}

class HtmlBuilder {
    private val children = mutableListOf<String>()
    
    fun body(init: BodyBuilder.() -> Unit) {
        val body = BodyBuilder()
        body.init()
        children.add(body.build())
    }
    
    fun build(): String {
        return "<html>
" + children.joinToString("
") + "
</html>"
    }
}

class BodyBuilder {
    private val children = mutableListOf<String>()
    
    fun h1(text: String) {
        children.add("<h1>${'$'}text</h1>")
    }
    
    fun p(text: String) {
        children.add("<p>${'$'}text</p>")
    }
    
    fun build(): String {
        return "<body>
" + children.joinToString("
") + "
</body>"
    }
}""".trimIndent(),
                solution = "",
                hint = "Используйте лямбды с получателем: fun html(init: HtmlBuilder.() -> Unit)",
                topic = "dsl"
            ),
            PracticeTask(
                id = "config_dsl",
                title = "DSL для конфигурации",
                description = "Создайте DSL для конфигурации приложения. Реализуйте функции database(), server(), logging() с параметрами",
                starterCode = """fun main() {
    // Используйте DSL для настройки конфигурации
    
    val config = appConfig {
        database {
            url = "localhost:5432"
            username = "admin"
            password = "secret"
        }
        server {
            port = 8080
            host = "0.0.0.0"
        }
        logging {
            level = "DEBUG"
            file = "app.log"
        }
    }
    
    println(config)
}

// Реализуйте DSL функции

fun appConfig(init: AppConfig.() -> Unit): AppConfig {
    val config = AppConfig()
    config.init()
    return config
}

class AppConfig {
    private val configs = mutableListOf<String>()
    
    fun database(init: DatabaseConfig.() -> Unit) {
        val db = DatabaseConfig()
        db.init()
        configs.add("Database: ${'$'}db")
    }
    
    fun server(init: ServerConfig.() -> Unit) {
        val server = ServerConfig()
        server.init()
        configs.add("Server: ${'$'}server")
    }
    
    fun logging(init: LoggingConfig.() -> Unit) {
        val logging = LoggingConfig()
        logging.init()
        configs.add("Logging: ${'$'}logging")
    }
    
    override fun toString(): String {
        return configs.joinToString("
")
    }
}

class DatabaseConfig {
    var url: String = ""
    var username: String = ""
    var password: String = ""
    
    override fun toString(): String {
        return "url='${'$'}url', username='${'$'}username'"
    }
}

class ServerConfig {
    var port: Int = 0
    var host: String = ""
    
    override fun toString(): String {
        return "${'$'}host:${'$'}port"
    }
}

class LoggingConfig {
    var level: String = ""
    var file: String = ""
    
    override fun toString(): String {
        return "level=${'$'}level, file=${'$'}file"
    }
}""".trimIndent(),
                solution = "",
                hint = "Каждый блок конфигурации должен возвращать объект-настройку",
                topic = "dsl"
            ),

// 5. functional (Функциональное программирование)
            PracticeTask(
                id = "higher_order",
                title = "Функции высшего порядка",
                description = "Создайте функцию calculate(), которая принимает два числа и операцию (лямбду), выполняет операцию и возвращает результат. Протестируйте с разными операциями",
                starterCode = """fun main() {
    // Протестируйте функцию calculate с разными операциями
    
    val sum = calculate(10, 5) { a, b -> a + b }
    println("10 + 5 = ${'$'}sum")
    
    val multiply = calculate(10, 5) { a, b -> a * b }
    println("10 * 5 = ${'$'}multiply")
    
    // Добавьте свои операции
}

// Реализуйте функцию calculate
fun calculate(a: Int, b: Int, operation: (Int, Int) -> Int): Int {
    // Примените операцию к a и b
}""".trimIndent(),
                solution = "",
                hint = "Просто вызовите operation(a, b) и верните результат",
                topic = "functional"
            ),
            PracticeTask(
                id = "functional_pipeline",
                title = "Функциональный конвейер",
                description = "Создайте список чисел, примените цепочку функциональных операций: filter (только чётные), map (умножить на 2), sorted (по убыванию). Выведите результат",
                starterCode = """fun main() {
    // Создайте список чисел
    val numbers = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    
    // Примените цепочку функциональных операций:
    // 1. filter - оставить только чётные
    // 2. map - умножить на 2
    // 3. sorted - отсортировать по убыванию
    
    val result = numbers
        // Добавьте операции здесь
    
    println("Исходный список: ${'$'}numbers")
    println("Результат: ${'$'}result")
}""".trimIndent(),
                solution = "",
                hint = "Используйте: .filter { it % 2 == 0 } .map { it * 2 } .sortedDescending()",
                topic = "functional"
            ),
            PracticeTask(
                id = "tail_recursion",
                title = "Хвостовая рекурсия",
                description = "Реализуйте вычисление факториала с помощью хвостовой рекурсии. Используйте tailrec для оптимизации",
                starterCode = """fun main() {
    // Протестируйте функцию factorial
    
    println("5! = ${'$'}{factorial(5)}")
    println("10! = ${'$'}{factorial(10)}")
    println("0! = ${'$'}{factorial(0)}")
    
    // Попробуйте большое число (оптимизация tailrec)
    println("20! = ${'$'}{factorial(20)}")
}

// Реализуйте хвостовую рекурсию для факториала
tailrec fun factorial(n: Int, accumulator: Long = 1): Long {
    // Реализуйте рекурсию
    // Базовый случай: если n <= 1, верните accumulator
    // Рекурсивный случай: вызовите factorial(n - 1, n * accumulator)
}""".trimIndent(),
                solution = "",
                hint = "Используйте: if (n <= 1) accumulator else factorial(n - 1, n * accumulator)",
                topic = "functional"
            ),

// 6. oop_kotlin (ООП в Kotlin) - из урока
            PracticeTask(
                id = "data_class",
                title = "Data классы",
                description = "Создайте data class User с полями id, name, email. Создайте несколько объектов, используйте автоматически сгенерированные методы (toString, equals, copy)",
                starterCode = """fun main() {
    // Создайте data class User
    // Протестируйте его возможности
    
    val user1 = User(1, "Иван", "ivan@example.com")
    println("user1: ${'$'}user1")
    
    val user2 = user1.copy(name = "Пётр")
    println("user2: ${'$'}user2")
    
    println("user1 == user2? ${'$'}{user1 == user2}")
    
    // Деструктуризация
    val (id, name, email) = user1
    println("ID: ${'$'}id, Имя: ${'$'}name, Email: ${'$'}email")
}

// Создайте data class User здесь""".trimIndent(),
                solution = "",
                hint = "Используйте: data class User(val id: Int, val name: String, val email: String)",
                topic = "oop_kotlin"
            ),
            PracticeTask(
                id = "inheritance",
                title = "Наследование и полиморфизм",
                description = "Создайте базовый класс Shape с методом area(). Создайте производные классы Circle и Rectangle, которые переопределяют этот метод",
                starterCode = """fun main() {
    // Создайте иерархию классов фигур
    
    val circle = Circle(5.0)
    println("Площадь круга (радиус=5): ${'$'}{circle.area()}")
    
    val rectangle = Rectangle(4.0, 6.0)
    println("Площадь прямоугольника (4x6): ${'$'}{rectangle.area()}")
    
    // Полиморфизм
    val shapes: List<Shape> = listOf(circle, rectangle)
    shapes.forEach { shape ->
        println("Площадь фигуры: ${'$'}{shape.area()}")
    }
}

// Создайте абстрактный класс Shape
abstract class Shape {
    // Объявите абстрактный метод area()
}

// Создайте класс Circle
class Circle(val radius: Double) : Shape() {
    // Переопределите метод area()
    // Площадь круга: π * r²
}

// Создайте класс Rectangle  
class Rectangle(val width: Double, val height: Double) : Shape() {
    // Переопределите метод area()
    // Площадь прямоугольника: width * height
}""".trimIndent(),
                solution = "",
                hint = "Используйте Math.PI для π, open/abstract для наследования",
                topic = "oop_kotlin"
            ),

// 7. coroutines_basics (Корутины) - из урока
            PracticeTask(
                id = "coroutines_launch",
                title = "Запуск корутин",
                description = "Создайте корутину с помощью launch. Имитируйте асинхронную задачу с delay. Выводите сообщения из основной функции и из корутины",
                starterCode = """import kotlinx.coroutines.*

fun main() = runBlocking {
    // Создайте корутину с launch
    
    println("Основной поток: начало")
    
    // Запустите корутину здесь
    
    delay(1000) // Дайте время корутине выполниться
    println("Основной поток: конец")
}""".trimIndent(),
                solution = "",
                hint = "Используйте: launch { delay(500); println(\"Корутина: выполнено\") }",
                topic = "coroutines_basics"
            ),
            PracticeTask(
                id = "coroutines_async",
                title = "Асинхронные вычисления",
                description = "Используйте async для параллельного выполнения двух задач. С помощью await дождитесь результатов и объедините их",
                starterCode = """import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

fun main() = runBlocking {
    // Используйте async для параллельных вычислений
    
    println("Начало параллельных вычислений...")
    
    // Запустите две async-корутины
    // Одна вычисляет 10 + 20 (с задержкой 500ms)
    // Другая вычисляет 30 * 2 (с задержкой 300ms)
    
    // Дождитесь результатов с await()
    
    // Выведите сумму результатов
}""".trimIndent(),
                solution = "",
                hint = "Используйте: val result1 = async { delay(500); 10 + 20 }, val result2 = async { delay(300); 30 * 2 }",
                topic = "coroutines_basics"
            ),

// 8. functional_programming (Функциональное программирование) - дополнение
            PracticeTask(
                id = "lambda_expressions",
                title = "Лямбда-выражения",
                description = "Создайте несколько лямбда-выражений и сохраните их в переменные. Вызовите их и передайте как параметры в функции",
                starterCode = """fun main() {
    // Создайте лямбда-выражения
    
    // 1. Лямбда для приветствия
    val greet = { name: String -> 
        // Верните приветствие
    }
    
    // 2. Лямбда для сложения двух чисел
    val add = { a: Int, b: Int ->
        // Верните сумму
    }
    
    // 3. Лямбда без параметров
    val sayHello = {
        // Верните приветствие
    }
    
    // Вызовите лямбды
    println(greet("Котлин"))
    println("5 + 3 = ${'$'}{add(5, 3)}")
    println(sayHello())
    
    // Передайте лямбду в функцию
    val numbers = listOf(1, 2, 3, 4, 5)
    val doubled = numbers.map { it * 2 }
    println("Удвоенные числа: ${'$'}doubled")
}""".trimIndent(),
                solution = "",
                hint = "Синтаксис лямбды: { параметры -> тело } или { it * 2 } для одного параметра",
                topic = "functional_programming"
            ),

// === Дополнительные практические задания (по 2 на каждую тему) ===

// 1. kotlin_basics - Основы Kotlin
            PracticeTask(
                id = "basic_types",
                title = "Работа с основными типами данных",
                description = "Создайте переменные всех основных типов данных в Kotlin (Int, Double, Boolean, Char, String). Выведите их значения и типы",
                starterCode = """fun main() {
    // Создайте переменные всех основных типов
    val intValue: Int = 42
    // Добавьте Double, Boolean, Char, String
    
    // Выведите значения и типы
    println("intValue: ${'$'}{intValue} (тип: ${'$'}{intValue::class.simpleName})")
    // Добавьте вывод для других типов
}""".trimIndent(),
                solution = "",
                hint = "Используйте: val doubleValue = 3.14, val boolValue = true, val charValue = 'A', val stringValue = \"Hello\"",
                topic = "kotlin_basics"
            ),
            PracticeTask(
                id = "arithmetic_operations",
                title = "Арифметические операции",
                description = "Выполните все основные арифметические операции (сложение, вычитание, умножение, деление) с числами 15 и 3. Выведите результаты",
                starterCode = """fun main() {
    val a = 15
    val b = 3
    
    // Выполните арифметические операции
    val sum = a + b
    // Вычитание, умножение, деление
    
    // Выведите результаты
    println("${'$'}a + ${'$'}b = ${'$'}{sum}")
    // Добавьте вывод для других операций
}""".trimIndent(),
                solution = "",
                hint = "Для деления целых чисел используйте /, результат будет целым: 15 / 3 = 5",
                topic = "kotlin_basics"
            ),

// 2. variables_types - Переменные и типы
            PracticeTask(
                id = "type_inference_practice",
                title = "Практика вывода типов",
                description = "Объявите переменные без указания типа и позвольте компилятору определить его. Затем проверьте, можно ли изменить тип переменной после объявления",
                starterCode = """fun main() {
    // Объявление с выводом типа
    val inferredString = "Kotlin" // String
    val inferredInt = 100         // Int
    val inferredDouble = 3.14     // Double
    
    // Попробуйте изменить значения
    // inferredString = 42 // Будет ли ошибка?
    
    println("Тип inferredString: ${'$'}{inferredString::class.simpleName}")
    println("Тип inferredInt: ${'$'}{inferredInt::class.simpleName}")
    println("Тип inferredDouble: ${'$'}{inferredDouble::class.simpleName}")
}""".trimIndent(),
                solution = "",
                hint = "Kotlin не позволит изменить тип переменной после объявления. Комментарий в коде вызовет ошибку компиляции",
                topic = "variables_types"
            ),
            PracticeTask(
                id = "explicit_type_conversion",
                title = "Явное преобразование типов",
                description = "Создайте переменные разных числовых типов и преобразуйте их явно в другие типы. Изучите особенности преобразования",
                starterCode = """fun main() {
    val intValue: Int = 100
    val doubleValue: Double = 3.14159
    
    // Явное преобразование типов
    val intToDouble: Double = intValue.toDouble()
    val doubleToInt: Int = doubleValue.toInt() // Потеря точности!
    
    println("intValue: ${'$'}{intValue}")
    println("intToDouble: ${'$'}{intToDouble}")
    println("doubleValue: ${'$'}{doubleValue}")
    println("doubleToInt: ${'$'}{doubleToInt} (потеряна дробная часть)")
    
    // Попробуйте другие преобразования
    val longValue: Long = 1000000000L
    val floatValue: Float = 2.71828f
}""".trimIndent(),
                solution = "",
                hint = "Используйте методы toInt(), toDouble(), toLong(), toFloat() для преобразования между числовыми типами",
                topic = "variables_types"
            ),

// 3. functions - Функции
            PracticeTask(
                id = "function_overloading",
                title = "Перегрузка функций",
                description = "Создайте несколько функций с одинаковым именем но разными параметрами (перегрузка). Протестируйте их вызов",
                starterCode = """fun main() {
    // Вызовите все перегруженные функции
    println("Приветствие без параметров: ${'$'}{greet()}")
    println("Приветствие с именем: ${'$'}{greet(\"Анна\")}")
    println("Приветствие с именем и языком: ${'$'}{greet(\"John\", \"English\")}")
}

// Создайте перегруженные функции greet
fun greet(): String {
    // Вернуть приветствие по умолчанию
}

fun greet(name: String): String {
    // Вернуть приветствие с именем
}

fun greet(name: String, language: String): String {
    // Вернуть приветствие с именем и языком
    // Если language == "English", вернуть "Hello, ${'$'}name!"
}""".trimIndent(),
                solution = "",
                hint = "Перегрузка функций позволяет иметь несколько функций с одинаковым именем но разными параметрами",
                topic = "functions"
            ),
            PracticeTask(
                id = "lambda_functions",
                title = "Лямбда-функции",
                description = "Создайте несколько лямбда-выражений и используйте их как параметры функций. Протестируйте разные синтаксисы лямбд",
                starterCode = """fun main() {
    // Создание лямбда-выражений
    val square: (Int) -> Int = { x -> x * x }
    val isEven: (Int) -> Boolean = { it % 2 == 0 }
    val greet: () -> String = { "Привет, мир!" }
    
    // Использование лямбд
    println("Квадрат 5: ${'$'}{square(5)}")
    println("5 четное? ${'$'}{isEven(5)}")
    println(greet())
    
    // Передача лямбды в функцию
    val numbers = listOf(1, 2, 3, 4, 5)
    val filtered = numbers.filter(isEven)
    println("Четные числа: ${'$'}{filtered}")
    
    // Сокращенный синтаксис
    val doubled = numbers.map { it * 2 }
    println("Удвоенные числа: ${'$'}{doubled}")
}""".trimIndent(),
                solution = "",
                hint = "Для лямбды с одним параметром можно использовать ключевое слово 'it' вместо явного объявления параметра",
                topic = "functions"
            ),

// 4. control_flow - Управляющие конструкции
            PracticeTask(
                id = "nested_conditionals",
                title = "Вложенные условные операторы",
                description = "Создайте вложенные if-else условия для классификации студентов по баллам. Добавьте несколько уровней вложенности",
                starterCode = """fun main() {
    val score = 85
    
    // Вложенные условия для классификации
    val grade = if (score >= 90) {
        "Отлично"
    } else if (score >= 75) {
        "Хорошо"
    } else if (score >= 60) {
        "Удовлетворительно"
    } else {
        "Неудовлетворительно"
    }
    
    println("Балл: ${'$'}{score}, Оценка: ${'$'}{grade}")
    
    // Дополнительная логика с вложенными условиями
    val hasHonors = if (score >= 90) {
        if (score == 100) {
            "С отличием (100 баллов!)"
        } else {
            "С отличием"
        }
    } else {
        "Без отличия"
    }
    
    println("Статус: ${'$'}{hasHonors}")
}""".trimIndent(),
                solution = "",
                hint = "Вложенные условия можно использовать для создания сложной логики. Будьте осторожны с читаемостью",
                topic = "control_flow"
            ),
            PracticeTask(
                id = "when_with_ranges",
                title = "When с диапазонами и условиями",
                description = "Используйте when с диапазонами чисел и дополнительными условиями для классификации температуры",
                starterCode = """fun main() {
    val temperature = 25
    
    // Используйте when с диапазонами
    val description = when (temperature) {
        in -50..0 -> "Мороз"
        in 1..15 -> "Прохладно"
        in 16..25 -> "Тепло"
        in 26..35 -> "Жарко"
        else -> "Экстремальная температура"
    }
    
    println("Температура: ${'$'}{temperature}°C - ${'$'}{description}")
    
    // When с дополнительными условиями
    val activity = when {
        temperature < 0 -> "Кататься на лыжах"
        temperature in 0..15 -> "Гулять в парке"
        temperature in 16..25 && !isRaining() -> "Играть в футбол"
        else -> "Оставаться дома"
    }
    
    println("Рекомендуемое занятие: ${'$'}{activity}")
}

fun isRaining(): Boolean = false // Заглушка""".trimIndent(),
                solution = "",
                hint = "В when можно использовать диапазоны (in a..b), множественные значения и сложные условия",
                topic = "control_flow"
            ),

// 5. loops - Циклы
            PracticeTask(
                id = "nested_loops",
                title = "Вложенные циклы",
                description = "Создайте вложенные циклы для вывода таблицы умножения от 1 до 10. Используйте разные типы циклов",
                starterCode = """fun main() {
    // Таблица умножения с вложенными циклами
    
    println("Таблица умножения:")
    
    // Внешний цикл
    for (i in 1..10) {
        // Внутренний цикл
        for (j in 1..10) {
            val product = i * j
            print("${'$'}product\t") // Табуляция для форматирования
        }
        println() // Новая строка после каждой строки таблицы
    }
    
    // Альтернатива с while
    println("\nТаблица умножения (while):")
    var x = 1
    while (x <= 10) {
        var y = 1
        while (y <= 10) {
            print("${'$'}{x * y}\t")
            y++
        }
        println()
        x++
    }
}""".trimIndent(),
                solution = "",
                hint = "Вложенные циклы позволяют обрабатывать многомерные структуры данных. Используйте println() для перехода на новую строку",
                topic = "loops"
            ),
            PracticeTask(
                id = "loop_control_statements",
                title = "Операторы управления циклами",
                description = "Используйте break, continue и метки для управления выполнением циклов. Создайте сложную логику с прерыванием",
                starterCode = """fun main() {
    // Пример использования break
    println("Поиск первого четного числа больше 10:")
    for (i in 1..20) {
        if (i > 10 && i % 2 == 0) {
            println("Найдено: ${'$'}{i}")
            break // Прерываем цикл при нахождении
        }
    }
    
    // Пример использования continue
    println("\nЧетные числа от 1 до 10:")
    for (i in 1..10) {
        if (i % 2 != 0) {
            continue // Пропускаем нечетные числа
        }
        print("${'$'}i ")
    }
    
    // Использование меток для вложенных циклов
    println("\n\nПример с метками:")
    outer@ for (i in 1..5) {
        inner@ for (j in 1..5) {
            if (i * j > 12) {
                println("Прерываем при i=${'$'}i, j=${'$'}j (произведение = ${'$'}{i * j})")
                break@outer // Прерываем внешний цикл
            }
            print("(${'$'}i,${'$'}j) ")
        }
        println()
    }
}""".trimIndent(),
                solution = "",
                hint = "Метки (labels) позволяют указать, какой именно цикл нужно прервать или продолжить во вложенных циклах",
                topic = "loops"
            ),

// 6. collections - Коллекции
            PracticeTask(
                id = "set_operations",
                title = "Операции с множествами",
                description = "Создайте несколько множеств (Set) и выполните операции объединения, пересечения и разности",
                starterCode = """fun main() {
    // Создание множеств
    val setA = setOf(1, 2, 3, 4, 5)
    val setB = setOf(4, 5, 6, 7, 8)
    
    println("Множество A: ${'$'}{setA}")
    println("Множество B: ${'$'}{setB}")
    
    // Операции с множествами
    val union = setA.union(setB) // Объединение
    val intersection = setA.intersect(setB) // Пересечение
    val difference = setA.subtract(setB) // Разность (A - B)
    
    println("Объединение: ${'$'}{union}")
    println("Пересечение: ${'$'}{intersection}")
    println("Разность (A - B): ${'$'}{difference}")
    
    // Проверка принадлежности
    println("\nПроверка принадлежности:")
    println("3 в множестве A? ${'$'}{3 in setA}")
    println("9 в множестве B? ${'$'}{9 in setB}")
    
    // Изменяемое множество
    val mutableSet = mutableSetOf(1, 2, 3)
    mutableSet.add(4)
    mutableSet.remove(2)
    println("Изменяемое множество: ${'$'}{mutableSet}")
}""".trimIndent(),
                solution = "",
                hint = "Set хранит только уникальные элементы. Операции union, intersect и subtract возвращают новые множества",
                topic = "collections"
            ),
            PracticeTask(
                id = "map_operations",
                title = "Работа с ассоциативными массивами (Map)",
                description = "Создайте и модифицируйте Map для хранения информации о студентах. Используйте различные операции с Map",
                starterCode = """fun main() {
    // Создание неизменяемого Map
    val studentGrades = mapOf(
        "Анна" to 85,
        "Борис" to 92,
        "Виктор" to 78
    )
    
    println("Оценки студентов: ${'$'}{studentGrades}")
    
    // Доступ к элементам
    println("Оценка Анны: ${'$'}{studentGrades[\"Анна\"]}")
    println("Оценка по умолчанию: ${'$'}{studentGrades.getOrDefault(\"Олег\", 0)}")
    
    // Создание изменяемого Map
    val mutableGrades = mutableMapOf(
        "Дарья" to 88,
        "Егор" to 95
    )
    
    // Модификация Map
    mutableGrades["Фёдор"] = 91 // Добавление
    mutableGrades["Егор"] = 96  // Изменение
    mutableGrades.remove("Дарья") // Удаление
    
    println("Измененные оценки: ${'$'}{mutableGrades}")
    
    // Итерация по Map
    println("\nВсе студенты и оценки:")
    for ((name, grade) in studentGrades) {
        println("${'$'}name: ${'$'}grade")
    }
    
    // Проверка существования ключа
    println("\nЕсть ли студент 'Борис'? ${'$'}{\"Борис\" in studentGrades}")
    
    // Получение всех ключей и значений
    println("Все имена: ${'$'}{studentGrades.keys}")
    println("Все оценки: ${'$'}{studentGrades.values}")
}""".trimIndent(),
                solution = "",
                hint = "Map хранит пары ключ-значение. Используйте оператор [] для доступа к значениям по ключу",
                topic = "collections"
            ),

// 7. strings - Строки
            PracticeTask(
                id = "string_manipulation",
                title = "Манипуляции со строками",
                description = "Используйте различные методы String для манипуляции текстом: обрезка, замена, разделение, объединение",
                starterCode = """fun main() {
    val text = "  Hello, Kotlin World!  "
    
    println("Оригинал: '${'$'}{text}'")
    
    // Базовые операции
    val trimmed = text.trim()
    val uppercase = text.uppercase()
    val lowercase = text.lowercase()
    
    println("Обрезано: '${'$'}{trimmed}'")
    println("Верхний регистр: '${'$'}{uppercase}'")
    println("Нижний регистр: '${'$'}{lowercase}'")
    
    // Замена и удаление
    val withoutSpaces = text.replace(" ", "")
    val withoutHello = text.replace("Hello", "Hi")
    
    println("Без пробелов: '${'$'}{withoutSpaces}'")
    println("Замена Hello: '${'$'}{withoutHello}'")
    
    // Разделение и объединение
    val words = text.trim().split(" ")
    println("Слова: ${'$'}{words}")
    println("Количество слов: ${'$'}{words.size}")
    
    val joined = words.joinToString("-")
    println("Объединено через '-': ${'$'}{joined}")
    
    // Подстроки
    val substring1 = text.substring(2, 7) // С 2 по 7 индекс
    val substring2 = text.substringAfter(",") // После запятой
    val substring3 = text.substringBefore("!") // До восклицательного знака
    
    println("Подстрока (2-7): '${'$'}{substring1}'")
    println("После запятой: '${'$'}{substring2}'")
    println("До '!': '${'$'}{substring3}'")
}""".trimIndent(),
                solution = "",
                hint = "Методы trim(), replace(), split(), joinToString() очень полезны для обработки строк",
                topic = "strings"
            ),
            PracticeTask(
                id = "string_builder",
                title = "StringBuilder для эффективной конкатенации",
                description = "Используйте StringBuilder для эффективного построения больших строк в циклах. Сравните с обычной конкатенацией",
                starterCode = """fun main() {
    // Обычная конкатенация (неэффективно в циклах)
    var result1 = ""
    for (i in 1..10) {
        result1 += "Число ${'$'}i, " // Создается новая строка на каждой итерации
    }
    println("Обычная конкатенация: ${'$'}{result1}")
    
    // Использование StringBuilder (эффективно)
    val builder = StringBuilder()
    for (i in 1..10) {
        builder.append("Число ${'$'}{i}, ")
    }
    val result2 = builder.toString()
    println("StringBuilder: ${'$'}{result2}")
    
    // Дополнительные методы StringBuilder
    val sb = StringBuilder("Начало")
    sb.append(" - середина")
    sb.insert(7, "ВСТАВКА ") // Вставка в позицию 7
    sb.delete(0, 7) // Удаление с 0 по 7 позицию
    sb.reverse() // Реверс строки
    
    println("StringBuilder операции: ${'$'}{sb}")
    
    // Сравнение производительности (концептуально)
    println("\nStringBuilder эффективнее при:")
    println("1. Многократной конкатенации в циклах")
    println("2. Больших объемах текста")
    println("3. Частых модификациях строки")
}""".trimIndent(),
                solution = "",
                hint = "StringBuilder изменяем и не создает новых объектов при каждой модификации, в отличие от обычных строк",
                topic = "strings"
            ),

// 8. null_safety - Null безопасность
            PracticeTask(
                id = "safe_calls_chain",
                title = "Цепочка безопасных вызовов",
                description = "Создайте сложную цепочку безопасных вызовов для работы с вложенными nullable объектами",
                starterCode = """data class Address(val city: String?, val street: String?)
data class User(val name: String, val address: Address?)

fun main() {
    // Создание объектов
    val user1 = User("Анна", Address("Москва", "Тверская"))
    val user2 = User("Борис", null)
    val user3: User? = null
    
    // Цепочка безопасных вызовов
    val city1 = user1.address?.city
    val city2 = user2.address?.city
    val city3 = user3?.address?.city
    
    println("Город user1: ${'$'}{city1}")
    println("Город user2: ${'$'}{city2}")
    println("Город user3: ${'$'}{city3}")
    
    // Более сложная цепочка с оператором Элвиса
    val safeCity1 = user1.address?.city ?: "Не указан"
    val safeCity2 = user2.address?.city ?: "Не указан"
    val safeCity3 = user3?.address?.city ?: "Не указан"
    
    println("\nБезопасные города:")
    println("user1: ${'$'}{safeCity1}")
    println("user2: ${'$'}{safeCity2}")
    println("user3: ${'$'}{safeCity3}")
    
    // Комбинирование с let
    user1.address?.city?.let { city ->
        println("Город user1 (через let): ${'$'}{city}")
    }
    
    user2.address?.city?.let { city ->
        println("Это не выполнится, так как address = null")
    }
    
    // Использование also для побочных эффектов
    user1.address?.also { address ->
        println("Адрес user1: город ${'$'}{address.city}, улица ${'$'}{address.street}")
    }
}""".trimIndent(),
                solution = "",
                hint = "Цепочка ?. останавливается и возвращает null при первом встреченном null. ?: предоставляет значение по умолчанию",
                topic = "null_safety"
            ),
            PracticeTask(
                id = "lateinit_property",
                title = "Поздняя инициализация (lateinit)",
                description = "Используйте lateinit для свойств, которые не могут быть инициализированы при создании объекта. Добавьте проверки",
                starterCode = """class DatabaseService {
    // lateinit позволяет отложить инициализацию
    lateinit var connection: String
    
    fun initialize(connectionString: String) {
        connection = connectionString
        println("База данных инициализирована: ${'$'}{connection}")
    }
    
    fun query(sql: String) {
        // Проверка инициализации перед использованием
        if (::connection.isInitialized) {
            println("Выполнение запроса '${'$'}sql' через соединение: ${'$'}{connection}")
        } else {
            println("Ошибка: соединение не инициализировано")
        }
    }
}

fun main() {
    val dbService = DatabaseService()
    
    // Попытка использовать до инициализации
    dbService.query("SELECT * FROM users") // Ошибка
    
    // Инициализация
    dbService.initialize("jdbc:mysql://localhost:3306/mydb")
    
    // Использование после инициализации
    dbService.query("SELECT * FROM products")
    
    // Пример с исключением
    try {
        val anotherService = DatabaseService()
        println("Соединение: ${'$'}{anotherService.connection}") // UninitializedPropertyAccessException
    } catch (e: UninitializedPropertyAccessException) {
        println("Поймано исключение: ${'$'}{e.message}")
    }
    
    // Когда использовать lateinit:
    println("\nLateinit полезен когда:")
    println("1. Свойство не может быть инициализировано в конструкторе")
    println("2. Инициализация происходит в отдельном методе (например, onCreate() в Android)")
    println("3. Гарантируется инициализация до первого использования")
}""".trimIndent(),
                solution = "",
                hint = "lateinit можно использовать только с var (не val) и только с non-null типами. Проверяйте isInitialized перед использованием",
                topic = "null_safety"
            ),

// 9. oop_kotlin - ООП в Kotlin
            PracticeTask(
                id = "constructor_overloading",
                title = "Перегрузка конструкторов",
                description = "Создайте класс с несколькими конструкторами (первичным и вторичными). Протестируйте создание объектов разными способами",
                starterCode = """// Класс с несколькими конструкторами
class Person {
    var name: String = ""
    var age: Int = 0
    var city: String = ""
    
    // Первичный конструктор
    constructor(name: String, age: Int, city: String) {
        this.name = name
        this.age = age
        this.city = city
        println("Создан Person: ${'$'}name, ${'$'}age лет, из ${'$'}city")
    }
    
    // Вторичный конструктор 1 (только имя и возраст)
    constructor(name: String, age: Int) : this(name, age, "Не указан") {
        println("Использован конструктор с именем и возрастом")
    }
    
    // Вторичный конструктор 2 (только имя)
    constructor(name: String) : this(name, 0, "Не указан") {
        println("Использован конструктор только с именем")
    }
    
    // Метод для вывода информации
    fun introduce() {
        println("Привет! Меня зовут ${'$'}{name}, мне ${'$'}{age} лет, я из ${'$'}{city}")
    }
}

fun main() {
    // Создание объектов разными конструкторами
    val person1 = Person("Анна", 25, "Москва")
    person1.introduce()
    
    println()
    
    val person2 = Person("Борис", 30)
    person2.introduce()
    
    println()
    
    val person3 = Person("Виктор")
    person3.introduce()
    
    // Изменение свойств после создания
    person3.age = 35
    person3.city = "Санкт-Петербург"
    println("\nПосле изменения:")
    person3.introduce()
}""".trimIndent(),
                solution = "",
                hint = "Вторичные конструкторы должны вызывать первичный конструктор через this(). Это обеспечивает единую точку инициализации",
                topic = "oop_kotlin"
            ),
            PracticeTask(
                id = "static_members_companion",
                title = "Статические члены и companion object",
                description = "Используйте companion object для создания статических методов и свойств. Сравните с объектами верхнего уровня",
                starterCode = """class MathUtils {
    companion object {
        // Статические свойства
        const val PI = 3.14159
        var count: Int = 0
        
        // Статические методы
        fun square(x: Int): Int = x * x
        
        fun circleArea(radius: Double): Double {
            count++
            return PI * radius * radius
        }
        
        fun factorial(n: Int): Long {
            return if (n <= 1) 1 else n * factorial(n - 1)
        }
    }
    
    // Обычные методы (требуют экземпляр)
    fun instanceMethod() {
        println("Этот метод требует создания объекта")
    }
}

// Объект верхнего уровня (альтернатива)
object StringUtils {
    fun capitalize(text: String): String {
        return text.replaceFirstChar { it.uppercase() }
    }
    
    fun isPalindrome(text: String): Boolean {
        return text == text.reversed()
    }
}

fun main() {
    // Использование companion object (без создания экземпляра)
    println("PI: ${'$'}{MathUtils.PI}")
    println("Квадрат 5: ${'$'}{MathUtils.square(5)}")
    println("Площадь круга (радиус=3): ${'$'}{MathUtils.circleArea(3.0)}")
    println("Факториал 5: ${'$'}{MathUtils.factorial(5)}")
    println("Количество вызовов: ${'$'}{MathUtils.count}")
    
    // Использование объекта верхнего уровня
    println("\nСтрока 'hello': ${'$'}{StringUtils.capitalize("hello")}")
    println("'level' палиндром? ${'$'}{StringUtils.isPalindrome("level")}")
    
    // Создание экземпляра для обычных методов
    val mathUtils = MathUtils()
    mathUtils.instanceMethod()
    
    // Разница между companion object и object:
    println("\nРазница:")
    println("1. companion object - часть класса, имеет доступ к private членам")
    println("2. object - самостоятельный синглтон")
    println("3. Оба могут реализовывать интерфейсы")
}""".trimIndent(),
                solution = "",
                hint = "companion object - это синглтон внутри класса. Можно обращаться к его членам через имя класса без создания экземпляра",
                topic = "oop_kotlin"
            ),

// 10. extensions - Расширения
            PracticeTask(
                id = "generic_extensions",
                title = "Обобщенные расширения",
                description = "Создайте обобщенные extension-функции, которые работают с любыми типами. Протестируйте с разными типами данных",
                starterCode = """// Обобщенное расширение для получения предпоследнего элемента
fun <T> List<T>.penultimate(): T? {
    if (size < 2) return null
    return this[size - 2]
}

// Обобщенное расширение для проверки наличия дубликатов
fun <T> List<T>.hasDuplicates(): Boolean {
    return this.size != this.toSet().size
}

// Обобщенное расширение для обмена элементов
fun <T> MutableList<T>.swap(index1: Int, index2: Int) {
    val temp = this[index1]
    this[index1] = this[index2]
    this[index2] = temp
}

// Обобщенное расширение для нахождения наиболее частого элемента
fun <T> List<T>.mostFrequent(): T? {
    return this.groupingBy { it }
        .eachCount()
        .maxByOrNull { it.value }
        ?.key
}

fun main() {
    // Тестирование с разными типами
    
    // Список чисел
    val numbers = listOf(1, 2, 3, 4, 5)
    println("Предпоследний элемент чисел: ${'$'}{numbers.penultimate()}")
    println("Есть дубликаты в числах? ${'$'}{numbers.hasDuplicates()}")
    
    // Список строк
    val words = listOf("apple", "banana", "apple", "orange", "banana", "banana")
    println("\nНаиболее частое слово: ${'$'}{words.mostFrequent()}")
    println("Есть дубликаты в словах? ${'$'}{words.hasDuplicates()}")
    
    // Изменяемый список
    val mutableList = mutableListOf("A", "B", "C", "D")
    println("\nДо обмена: ${'$'}{mutableList}")
    mutableList.swap(1, 2)
    println("После обмена (1 и 2): ${'$'}{mutableList}")
    
    // Список объектов
    data class Person(val name: String, val age: Int)
    val people = listOf(
        Person("Анна", 25),
        Person("Борис", 30),
        Person("Анна", 25) // Дубликат
    )
    println("\nЕсть дубликаты людей? ${'$'}{people.hasDuplicates()}")
    
    // Преимущества обобщенных расширений
    println("\nПреимущества обобщенных расширений:")
    println("1. Работают с любыми типами")
    println("2. Повышают переиспользуемость кода")
    println("3. Обеспечивают типобезопасность")
}""".trimIndent(),
                solution = "",
                hint = "Обобщенные расширения используют параметры типа <T>. Они могут работать с любым типом, удовлетворяющим ограничениям",
                topic = "extensions"
            ),
            PracticeTask(
                id = "extension_properties_custom",
                title = "Расширения свойств и пользовательские геттеры/сеттеры",
                description = "Создайте вычисляемые свойства-расширения и расширения с пользовательскими геттерами и сеттерами",
                starterCode = """// Вычисляемые свойства-расширения
val String.wordCount: Int
    get() = this.split("\\s+".toRegex()).size

val String.isPalindrome: Boolean
    get() = this == this.reversed()

val String.initials: String
    get() = this.split(" ").map { it.first().uppercase() }.joinToString("")

// Расширение с сеттером (только для mutable объектов)
var StringBuilder.lastChar: Char
    get() = this.last()
    set(value) {
        this.setCharAt(this.length - 1, value)
    }

// Расширение для Int с вычисляемым свойством
val Int.isPrime: Boolean
    get() {
        if (this <= 1) return false
        for (i in 2..this / 2) {
            if (this % i == 0) return false
        }
        return true
    }

val Int.squared: Int
    get() = this * this

fun main() {
    // Тестирование свойств-расширений
    
    val text = "Hello Kotlin World"
    println("Текст: '${'$'}text'")
    println("Количество слов: ${'$'}{text.wordCount}")
    println("Инициалы: ${'$'}{text.initials}")
    
    println("\nПроверка палиндромов:")
    println("'level' палиндром? ${'$'}{\"level\".isPalindrome}")
    println("'hello' палиндром? ${'$'}{\"hello\".isPalindrome}")
    
    println("\nРабота с StringBuilder:")
    val sb = StringBuilder("Hello")
    println("Исходный StringBuilder: ${'$'}{sb}")
    println("Последний символ: ${'$'}{sb.lastChar}")
    sb.lastChar = '!' // Изменение через сеттер
    println("После изменения: ${'$'}{sb}")
    
    println("\nСвойства для Int:")
    val number = 7
    println("Число: ${'$'}{number}")
    println("Простое? ${'$'}{number.isPrime}")
    println("В квадрате: ${'$'}{number.squared}")
    
    println("\nПроверка диапазона чисел на простоту:")
    for (i in 1..10) {
        println("${'$'}i - простое? ${'$'}{i.isPrime}")
    }
    
    // Ограничения свойств-расширений
    println("\nОграничения:")
    println("1. Не могут иметь backing field")
    println("2. Могут быть только вычисляемыми")
    println("3. Для сеттеров нужен изменяемый объект (как StringBuilder)")
}""".trimIndent(),
                solution = "",
                hint = "Свойства-расширения могут иметь только геттеры (val) или геттеры и сеттеры (var), но не могут иметь backing field",
                topic = "extensions"
            ),

// 11. delegation - Делегирование
            PracticeTask(
                id = "custom_delegate_validation",
                title = "Пользовательский делегат для валидации",
                description = "Создайте пользовательский делегат, который проверяет значения на соответствие определенным правилам",
                starterCode = """import kotlin.reflect.KProperty

// Делегат для валидации строк (не пустая, минимальная длина)
class ValidatedStringDelegate(
    private val minLength: Int = 1,
    private val maxLength: Int = 100
) {
    private var value: String = ""
    
    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return value
    }
    
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        // Валидация
        when {
            value.isEmpty() -> throw IllegalArgumentException("'${'$'}{property.name}' не может быть пустым")
            value.length < minLength -> throw IllegalArgumentException("'${'$'}{property.name}' должен быть не короче ${'$'}{minLength} символов")
            value.length > maxLength -> throw IllegalArgumentException("'${'$'}{property.name}' должен быть не длиннее ${'$'}{maxLength} символов")
            else -> this.value = value
        }
    }
}

// Делегат для валидации чисел в диапазоне
class RangeDelegate(
    private val min: Int,
    private val max: Int
) {
    private var value: Int = min
    
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        return value
    }
    
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        if (value !in min..max) {
            throw IllegalArgumentException("'${'$'}{property.name}' должен быть в диапазоне [${'$'}min..${'$'}max], получено: ${'$'}value")
        }
        this.value = value
    }
}

// Делегат для логирования изменений
class LoggingDelegate<T>(private val defaultValue: T) {
    private var value: T = defaultValue
    
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        println("[LOG] Чтение '${'$'}{property.name}': ${'$'}{value}")
        return value
    }
    
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        println("[LOG] Изменение '${'$'}{property.name}': ${'$'}{this.value} -> ${'$'}{value}")
        this.value = value
    }
}

class User {
    var name: String by ValidatedStringDelegate(minLength = 2, maxLength = 50)
    var age: Int by RangeDelegate(0, 150)
    var email: String by LoggingDelegate("")
}

fun main() {
    val user = User()
    
    // Валидные значения
    try {
        user.name = "Анна"
        user.age = 25
        user.email = "anna@example.com"
        
        println("\nВалидные значения установлены:")
        println("Имя: ${'$'}{user.name}")
        println("Возраст: ${'$'}{user.age}")
        println("Email: ${'$'}{user.email}")
    } catch (e: IllegalArgumentException) {
        println("Ошибка валидации: ${'$'}{e.message}")
    }
    
    // Невалидные значения
    println("\nПопытка установить невалидные значения:")
    try {
        user.name = "А" // Слишком короткое
    } catch (e: IllegalArgumentException) {
        println("Ошибка: ${'$'}{e.message}")
    }
    
    try {
        user.age = 200 // Вне диапазона
    } catch (e: IllegalArgumentException) {
        println("Ошибка: ${'$'}{e.message}")
    }
    
    try {
        user.name = "" // Пустая строка
    } catch (e: IllegalArgumentException) {
        println("Ошибка: ${'$'}{e.message}")
    }
    
    // Логирование изменений email
    println("\nЛогирование изменений email:")
    user.email = "new-email@example.com"
    val currentEmail = user.email // Вызовет логирование чтения
}""".trimIndent(),
                solution = "",
                hint = "Пользовательские делегаты должны реализовать операторы getValue и setValue. Можно добавлять любую логику валидации",
                topic = "delegation"
            ),
            PracticeTask(
                id = "map_delegate_pattern",
                title = "Делегирование свойств Map",
                description = "Используйте делегирование Map для создания динамических объектов конфигурации. Протестируйте с разными типами данных",
                starterCode = """class Configuration(val map: Map<String, Any?>) {
    // Делегирование свойств Map
    val appName: String by map
    val version: Int by map
    val isDebug: Boolean by map
    val port: Int by map
    val timeout: Long by map
    val features: List<String> by map
}

class DynamicConfig(val map: MutableMap<String, Any?> = mutableMapOf()) {
    // Делегирование изменяемым свойствам
    var username: String by map
    var isActive: Boolean by map
    var score: Double by map
    
    fun printAll() {
        println("Все свойства конфигурации:")
        for ((key, value) in map) {
            println("  ${'$'}key = ${'$'}value (тип: ${'$'}{value?.let { it::class.simpleName } ?: "null"})")
        }
    }
}

fun main() {
    // Конфигурация из Map
    val configMap = mapOf(
        "appName" to "MyApp",
        "version" to 2,
        "isDebug" to true,
        "port" to 8080,
        "timeout" to 5000L,
        "features" to listOf("auth", "logging", "caching")
    )
    
    val config = Configuration(configMap)
    
    println("Конфигурация приложения:")
    println("Название: ${'$'}{config.appName}")
    println("Версия: ${'$'}{config.version}")
    println("Отладка: ${'$'}{config.isDebug}")
    println("Порт: ${'$'}{config.port}")
    println("Таймаут: ${'$'}{config.timeout}мс")
    println("Фичи: ${'$'}{config.features}")
    
    // Динамическая конфигурация
    println("\nДинамическая конфигурация:")
    val dynamicConfig = DynamicConfig()
    
    // Установка значений (автоматически добавляет в Map)
    dynamicConfig.username = "user123"
    dynamicConfig.isActive = true
    dynamicConfig.score = 95.5
    
    dynamicConfig.printAll()
    
    // Изменение через Map
    println("\nИзменение через Map:")
    dynamicConfig.map["username"] = "newUser"
    dynamicConfig.map["isActive"] = false
    dynamicConfig.map["extraProperty"] = "Дополнительное свойство" // Динамическое добавление
    
    println("Имя пользователя: ${'$'}{dynamicConfig.username}")
    println("Активен: ${'$'}{dynamicConfig.isActive}")
    println("Дополнительное свойство: ${'$'}{dynamicConfig.map["extraProperty"]}")
    
    // Преимущества паттерна
    println("\nПреимущества делегирования Map:")
    println("1. Динамические свойства (можно добавлять во время выполнения)")
    println("2. Сериализация/десериализация (JSON → Map → Object)")
    println("3. Гибкая конфигурация (файлы настроек)")
    println("4. Минимальный код (автоматическое делегирование)")
}""".trimIndent(),
                solution = "",
                hint = "Делегирование Map позволяет создавать объекты, свойства которых хранятся в Map. Отлично подходит для конфигураций и динамических объектов",
                topic = "delegation"
            ),

// 12. dsl - DSL и билдеры
            PracticeTask(
                id = "fluent_builder_pattern",
                title = "Fluent Builder Pattern",
                description = "Создайте DSL с fluent interface для построения сложных объектов. Используйте цепочки вызовов методов",
                starterCode = """// Класс продукта
data class Computer(
    val cpu: String,
    val ram: Int, // GB
    val storage: Int, // GB
    val gpu: String?,
    val os: String
) {
    override fun toString(): String {
        return "Компьютер: CPU=${'$'}cpu, RAM=${'$'}ram GB, Storage=${'$'}storage GB, GPU=${'$'}{gpu ?: "Встроенная"}, OS=${'$'}os"
    }
}

// Builder с fluent interface
class ComputerBuilder {
    var cpu: String = "Intel i5"
    var ram: Int = 8
    var storage: Int = 256
    var gpu: String? = null
    var os: String = "Windows 10"
    
    fun cpu(cpu: String): ComputerBuilder {
        this.cpu = cpu
        return this
    }
    
    fun ram(gb: Int): ComputerBuilder {
        this.ram = gb
        return this
    }
    
    fun storage(gb: Int): ComputerBuilder {
        this.storage = gb
        return this
    }
    
    fun gpu(model: String): ComputerBuilder {
        this.gpu = model
        return this
    }
    
    fun os(os: String): ComputerBuilder {
        this.os = os
        return this
    }
    
    fun build(): Computer {
        return Computer(cpu, ram, storage, gpu, os)
    }
}

// DSL функция
fun computer(block: ComputerBuilder.() -> Unit): Computer {
    val builder = ComputerBuilder()
    builder.block()
    return builder.build()
}

// Расширенный DSL с валидацией
class AdvancedComputerBuilder {
    private var cpu: String = ""
    private var ram: Int = 0
    private var storage: Int = 0
    private var gpu: String? = null
    private var os: String = ""
    
    fun cpu(cpu: String) = apply { this.cpu = cpu }
    fun ram(gb: Int) = apply { 
        require(gb in 4..128) { "RAM должна быть в диапазоне 4-128 GB" }
        this.ram = gb 
    }
    fun storage(gb: Int) = apply { 
        require(gb in 128..4096) { "Storage должен быть в диапазоне 128-4096 GB" }
        this.storage = gb 
    }
    fun gpu(model: String) = apply { this.gpu = model }
    fun os(os: String) = apply { this.os = os }
    
    fun build(): Computer {
        require(cpu.isNotBlank()) { "CPU обязателен" }
        require(os.isNotBlank()) { "OS обязателен" }
        require(ram > 0) { "RAM обязательна" }
        require(storage > 0) { "Storage обязателен" }
        
        return Computer(cpu, ram, storage, gpu, os)
    }
}

fun advancedComputer(block: AdvancedComputerBuilder.() -> Unit): Computer {
    return AdvancedComputerBuilder().apply(block).build()
}

fun main() {
    // Использование fluent builder
    println("=== Fluent Builder Pattern ===")
    
    val gamingPc = ComputerBuilder()
        .cpu("Intel i9")
        .ram(32)
        .storage(1000)
        .gpu("NVIDIA RTX 4080")
        .os("Windows 11")
        .build()
    
    println("Игровой ПК: ${'$'}{gamingPc}")
    
    // Использование DSL
    println("\n=== DSL Style ===")
    
    val officePc = computer {
        cpu = "Intel i5"
        ram = 16
        storage = 512
        os = "Windows 10 Pro"
    }
    
    println("Офисный ПК: ${'$'}{officePc}")
    
    // Использование advanced DSL с apply
    println("\n=== Advanced DSL with Validation ===")
    
    try {
        val server = advancedComputer {
            cpu("AMD EPYC")
            ram(128)
            storage(2048)
            os("Ubuntu Server")
        }
        println("Сервер: ${'$'}{server}")
    } catch (e: IllegalArgumentException) {
        println("Ошибка сборки: ${'$'}{e.message}")
    }
    
    // Попытка создать невалидный компьютер
    try {
        val invalidPc = advancedComputer {
            cpu("")
            ram(2) // Слишком мало
            storage(50) // Слишком мало
            os("")
        }
    } catch (e: IllegalArgumentException) {
        println("\nОшибка валидации: ${'$'}{e.message}")
    }
    
    // Преимущества fluent builder
    println("\nПреимущества Fluent Builder:")
    println("1. Читаемость (похоже на естественный язык)")
    println("2. Гибкость (можно пропускать необязательные параметры)")
    println("3. Валидация (можно добавлять проверки в методы)")
    println("4. Неизменяемость (объект создается только в build())")
}""".trimIndent(),
                solution = "",
                hint = "Fluent builder возвращает this из каждого метода, позволяя строить цепочки вызовов. Метод apply в Kotlin упрощает этот паттерн",
                topic = "dsl"
            ),
            PracticeTask(
                id = "dsl_with_validation",
                title = "DSL с валидацией и ограничениями",
                description = "Создайте DSL с аннотациями @DslMarker и добавьте валидацию для предотвращения ошибок",
                starterCode = """// Маркер для ограничения области видимости в DSL
@DslMarker
annotation class FormDsl

// Классы формы
@FormDsl
class FormBuilder {
    private val fields = mutableListOf<Field>()
    
    fun text(block: TextFieldBuilder.() -> Unit) {
        val builder = TextFieldBuilder()
        builder.block()
        fields.add(builder.build())
    }
    
    fun select(block: SelectFieldBuilder.() -> Unit) {
        val builder = SelectFieldBuilder()
        builder.block()
        fields.add(builder.build())
    }
    
    fun build(): Form {
        require(fields.isNotEmpty()) { "Форма должна содержать хотя бы одно поле" }
        return Form(fields)
    }
}

@FormDsl
class TextFieldBuilder {
    var name: String = ""
    var label: String = ""
    var required: Boolean = false
    var maxLength: Int = 100
    
    fun build(): TextField {
        require(name.isNotBlank()) { "Имя поля обязательно" }
        require(label.isNotBlank()) { "Метка поля обязательна" }
        require(maxLength > 0) { "Максимальная длина должна быть положительной" }
        
        return TextField(name, label, required, maxLength)
    }
}

@FormDsl
class SelectFieldBuilder {
    var name: String = ""
    var label: String = ""
    var required: Boolean = false
    private val options = mutableListOf<String>()
    
    fun options(vararg values: String) {
        options.addAll(values)
    }
    
    fun build(): SelectField {
        require(name.isNotBlank()) { "Имя поля обязательно" }
        require(label.isNotBlank()) { "Метка поля обязательна" }
        require(options.isNotEmpty()) { "Должен быть хотя бы один вариант выбора" }
        
        return SelectField(name, label, required, options)
    }
}

// Модели данных
data class Form(val fields: List<Field>)
sealed class Field
data class TextField(
    val name: String,
    val label: String,
    val required: Boolean,
    val maxLength: Int
) : Field()

data class SelectField(
    val name: String,
    val label: String,
    val required: Boolean,
    val options: List<String>
) : Field()

// DSL функция
fun form(block: FormBuilder.() -> Unit): Form {
    return FormBuilder().apply(block).build()
}

fun main() {
    println("=== DSL для построения форм с валидацией ===")
    
    // Создание валидной формы
    try {
        val userForm = form {
            text {
                name = "username"
                label = "Имя пользователя"
                required = true
                maxLength = 50
            }
            
            text {
                name = "email"
                label = "Email"
                required = true
                maxLength = 100
            }
            
            select {
                name = "country"
                label = "Страна"
                required = true
                options("Россия", "США", "Германия", "Франция")
            }
            
            select {
                name = "language"
                label = "Язык"
                required = false
                options("Русский", "Английский", "Немецкий")
            }
        }
        
        println("Форма успешно создана!")
        println("Количество полей: ${'$'}{userForm.fields.size}")
        
        userForm.fields.forEach { field ->
            when (field) {
                is TextField -> println("Текстовое поле: ${'$'}{field.label} (обязательное: ${'$'}{field.required})")
                is SelectField -> println("Выпадающий список: ${'$'}{field.label} (вариантов: ${'$'}{field.options.size})")
            }
        }
    } catch (e: IllegalArgumentException) {
        println("Ошибка создания формы: ${'$'}{e.message}")
    }
    
    // Попытка создать невалидную форму
    println("\n=== Попытка создания невалидной формы ===")
    
    try {
        val invalidForm = form {
            text {
                name = "" // Ошибка: пустое имя
                label = "Тестовое поле"
                required = true
                maxLength = 50
            }
        }
    } catch (e: IllegalArgumentException) {
        println("Ошибка: ${'$'}{e.message}")
    }
    
    // Попытка создать форму без полей
    try {
        val emptyForm = form {
            // Нет полей
        }
    } catch (e: IllegalArgumentException) {
        println("Ошибка: ${'$'}{e.message}")
    }
    
    // Преимущества @DslMarker
    println("\nПреимущества @DslMarker:")
    println("1. Предотвращает смешивание контекстов")
    println("2. Улучшает читаемость DSL")
    println("3. Помогает избегать ошибок")
    println("4. Обеспечивает правильную область видимости")
    
    // Пример без @DslMarker (потенциальная проблема)
    println("\nБез @DslMarker можно было бы написать:")
    println(""form {
        text {
            name = "username"
            // select { ... } // Ошибка! Нельзя смешивать контексты
        }
    }"")
}""".trimIndent(),
                solution = "",
                hint = "@DslMarker предотвращает доступ к внешним scope в вложенных DSL блоках. Это помогает избежать ошибок и улучшает читаемость",
                topic = "dsl"
            ),

// 13. functional - Функциональное программирование
            PracticeTask(
                id = "monad_composition",
                title = "Композиция монад (Option, Either)",
                description = "Используйте монады Option и Either для обработки цепочек вычислений с возможными ошибками",
                starterCode = """// Определение Either монады
sealed class Either<out L, out R> {
    data class Left<out L>(val error: L) : Either<L, Nothing>()
    data class Right<out R>(val value: R) : Either<Nothing, R>()
    
    // Функтор: map
    fun <T> map(f: (R) -> T): Either<L, T> = when (this) {
        is Left -> Left(error)
        is Right -> Right(f(value))
    }
    
    // Монада: flatMap
    fun <T> flatMap(f: (R) -> Either<L, T>): Either<L, T> = when (this) {
        is Left -> Left(error)
        is Right -> f(value)
    }
    
    // Получение значения или выброс исключения
    fun getOrThrow(): R = when (this) {
        is Left -> throw IllegalArgumentException(error.toString())
        is Right -> value
    }
    
    // Получение значения или значение по умолчанию
    fun getOrElse(default: () -> R): R = when (this) {
        is Left -> default()
        is Right -> value
    }
}

// Определение Option монады
sealed class Option<out T> {
    data class Some<out T>(val value: T) : Option<T>()
    object None : Option<Nothing>()
    
    // Функтор: map
    fun <R> map(f: (T) -> R): Option<R> = when (this) {
        is Some -> Some(f(value))
        is None -> None
    }
    
    // Монада: flatMap
    fun <R> flatMap(f: (T) -> Option<R>): Option<R> = when (this) {
        is Some -> f(value)
        is None -> None
    }
    
    // Получение значения или значение по умолчанию
    fun getOrElse(default: () -> T): T = when (this) {
        is Some -> value
        is None -> default()
    }
    
    // Проверка наличия значения
    fun isDefined(): Boolean = this is Some
}

// Функции для работы с Either
fun parseNumber(str: String): Either<String, Int> = try {
    Either.Right(str.toInt())
} catch (e: NumberFormatException) {
    Either.Left("Не число: '${'$'}str'")
}

fun divide(a: Int, b: Int): Either<String, Int> = if (b == 0) {
    Either.Left("Деление на ноль: ${'$'}a / ${'$'}b")
} else {
    Either.Right(a / b)
}

fun sqrt(x: Int): Either<String, Double> = if (x < 0) {
    Either.Left("Корень из отрицательного числа: ${'$'}x")
} else {
    Either.Right(Math.sqrt(x.toDouble()))
}

// Функции для работы с Option
fun findUser(id: Int): Option<String> = if (id > 0) {
    Option.Some("User${'$'}id")
} else {
    Option.None
}

fun getUserEmail(user: String): Option<String> = when (user) {
    "User1" -> Option.Some("user1@example.com")
    "User2" -> Option.Some("user2@example.com")
    else -> Option.None
}

fun validateEmail(email: String): Option<String> = if (email.contains("@")) {
    Option.Some(email)
} else {
    Option.None
}

fun main() {
    println("=== Композиция монад Either ===")
    
    // Цепочка вычислений с Either
    val calculation1 = parseNumber("10")
        .flatMap { a -> parseNumber("2").flatMap { b -> divide(a, b) } }
        .flatMap(::sqrt)
    
    println("Результат 1: ${'$'}{calculation1}")
    
    val calculation2 = parseNumber("abc")
        .flatMap { a -> parseNumber("2").flatMap { b -> divide(a, b) } }
        .flatMap(::sqrt)
    
    println("Результат 2: ${'$'}{calculation2} (ошибка на первом шаге)")
    
    val calculation3 = parseNumber("16")
        .flatMap { a -> parseNumber("0").flatMap { b -> divide(a, b) } }
        .flatMap(::sqrt)
    
    println("Результат 3: ${'$'}{calculation3} (ошибка деления на ноль)")
    
    println("\n=== Композиция монад Option ===")
    
    // Цепочка вычислений с Option
    val userEmail1 = findUser(1)
        .flatMap(::getUserEmail)
        .flatMap(::validateEmail)
    
    println("Email пользователя 1: ${'$'}{userEmail1.getOrElse { "Не найден" }}")
    
    val userEmail2 = findUser(-1)
        .flatMap(::getUserEmail)
        .flatMap(::validateEmail)
    
    println("Email пользователя -1: ${'$'}{userEmail2.getOrElse { "Не найден" }}")
    
    val userEmail3 = findUser(3)
        .flatMap(::getUserEmail)
        .flatMap(::validateEmail)
    
    println("Email пользователя 3: ${'$'}{userEmail3.getOrElse { "Не найден" }}")
    
    // Использование map для Option
    val formattedEmail = findUser(1)
        .flatMap(::getUserEmail)
        .map { it.uppercase() }
    
    println("Форматированный email: ${'$'}{formattedEmail.getOrElse { "Не найден" }}")
    
    // Преимущества монадической композиции
    println("\nПреимущества монад:")
    println("1. Безопасная обработка ошибок (без исключений)")
    println("2. Композируемость (легко строить цепочки)")
    println("3. Явность (ошибки видны в типе)")
    println("4. Чистота (без побочных эффектов)")
    
    // Сравнение с императивным стилем
    println("\nСравнение с императивным стилем:")
    println("""
                // Императивный стиль (громоздкий)
                fun getUserEmailImperative(id: Int): String? {
                    val user = findUser(id)
                    if (user == null) return null
                    val email = getUserEmail(user)
                    if (email == null) return null
                    return if (validateEmail(email)) email else null
                }

                // Функциональный стиль (лаконичный)
                fun getUserEmailFunctional(id: Int) =
                    findUser(id)
                        .flatMap(::getUserEmail)
                        .flatMap(::validateEmail)
                """.trimIndent())
}""".trimIndent(),
                solution = "",
                hint = "Монады позволяют строить цепочки вычислений, где каждый шаг может завершиться неудачей. flatMap обрабатывает успех/неудачу предыдущего шага",
                topic = "functional"
            ),
            PracticeTask(
                id = "recursion_patterns",
                title = "Паттерны рекурсии и оптимизации",
                description = "Изучите разные паттерны рекурсии: прямая, хвостовая, взаимная. Используйте мемоизацию для оптимизации",
                starterCode = """import kotlin.system.measureTimeMillis

// Прямая рекурсия (может вызвать StackOverflow для больших n)
fun factorialDirect(n: Int): Long = if (n <= 1) 1 else n * factorialDirect(n - 1)

// Хвостовая рекурсия (оптимизируется компилятором)
tailrec fun factorialTail(n: Int, accumulator: Long = 1): Long = 
    if (n <= 1) accumulator else factorialTail(n - 1, n * accumulator)

// Взаимная рекурсия (две функции вызывают друг друга)
fun isEven(n: Int): Boolean = when (n) {
    0 -> true
    else -> isOdd(n - 1)
}

fun isOdd(n: Int): Boolean = when (n) {
    0 -> false
    else -> isEven(n - 1)
}

// Рекурсия с мемоизацией (кэширование результатов)
val fibonacciCache = mutableMapOf<Int, Long>()

fun fibonacciMemo(n: Int): Long = fibonacciCache.getOrPut(n) {
    when (n) {
        0 -> 0
        1 -> 1
        else -> fibonacciMemo(n - 1) + fibonacciMemo(n - 2)
    }
}

// Рекурсия для обхода дерева
sealed class Tree<out T> {
    data class Node<out T>(val value: T, val left: Tree<T>, val right: Tree<T>) : Tree<T>()
    object Empty : Tree<Nothing>()
    
    // Рекурсивный обход в глубину
    fun depth(): Int = when (this) {
        is Empty -> 0
        is Node -> 1 + maxOf(left.depth(), right.depth())
    }
    
    // Рекурсивный подсчет узлов
    fun count(): Int = when (this) {
        is Empty -> 0
        is Node -> 1 + left.count() + right.count()
    }
    
    // Рекурсивный поиск
    fun contains(value: @UnsafeVariance T): Boolean = when (this) {
        is Empty -> false
        is Node -> this.value == value || left.contains(value) || right.contains(value)
    }
    
    // Рекурсивное преобразование
    fun <R> map(f: (T) -> R): Tree<R> = when (this) {
        is Empty -> Empty
        is Node -> Node(f(value), left.map(f), right.map(f))
    }
}

// Создание дерева для тестирования
fun createTree(): Tree<Int> = Tree.Node(
    1,
    Tree.Node(2, Tree.Node(4, Tree.Empty, Tree.Empty), Tree.Node(5, Tree.Empty, Tree.Empty)),
    Tree.Node(3, Tree.Node(6, Tree.Empty, Tree.Empty), Tree.Empty)
)

fun main() {
    println("=== Сравнение типов рекурсии ===")
    
    // Тестирование факториала
    val n = 20
    
    val timeDirect = measureTimeMillis {
        val result = factorialDirect(n)
        println("Прямая рекурсия: ${'$'}n! = ${'$'}result")
    }
    println("Время (прямая): ${'$'}{timeDirect}мс")
    
    val timeTail = measureTimeMillis {
        val result = factorialTail(n)
        println("Хвостовая рекурсия: ${'$'}n! = ${'$'}result")
    }
    println("Время (хвостовая): ${'$'}{timeTail}мс")
    
    // Тестирование взаимной рекурсии
    println("\nВзаимная рекурсия:")
    println("10 четное? ${'$'}{isEven(10)}")
    println("7 нечетное? ${'$'}{isOdd(7)}")
    
    // Тестирование мемоизации
    println("\nМемоизация Фибоначчи:")
    
    val timeWithoutMemo = measureTimeMillis {
        // Без кэша (очень медленно для больших n)
        println("fibonacci(10) = ${'$'}{fibonacciMemo(10)}")
    }
    println("Первое вычисление: ${'$'}{timeWithoutMemo}мс")
    
    val timeWithMemo = measureTimeMillis {
        // С кэшем (быстро)
        println("fibonacci(30) = ${'$'}{fibonacciMemo(30)}")
    }
    println("Второе вычисление (с кэшем): ${'$'}{timeWithMemo}мс")
    
    println("Размер кэша: ${'$'}{fibonacciCache.size}")
    
    // Тестирование рекурсии с деревьями
    println("\nРекурсия с деревьями:")
    val tree = createTree()
    
    println("Дерево: ${'$'}{tree}")
    println("Глубина: ${'$'}{tree.depth()}")
    println("Количество узлов: ${'$'}{tree.count()}")
    println("Содержит 5? ${'$'}{tree.contains(5)}")
    println("Содержит 10? ${'$'}{tree.contains(10)}")
    
    val doubledTree = tree.map { it * 2 }
    println("Удвоенное дерево: ${'$'}{doubledTree}")
    
    // Советы по использованию рекурсии
    println("\nСоветы по использованию рекурсии:")
    println("1. Используйте хвостовую рекурсию (tailrec) когда возможно")
    println("2. Для повторяющихся вычислений используйте мемоизацию")
    println("3. Для древовидных структур рекурсия часто идеальна")
    println("4. Избегайте глубокой рекурсии (может вызвать StackOverflow)")
    println("5. Рассмотрите итеративные решения для простых случаев")
    
    // Ограничения рекурсии
    println("\nОграничения рекурсии в JVM:")
    println("1. Максимальная глубина стека ограничена")
    println("2. Каждый вызов функции использует память стека")
    println("3. Хвостовая рекурсия преобразуется в цикл (нет ограничений)")
    println("4. Для очень глубокой рекурсии используйте явный стек")
}""".trimIndent(),
                solution = "",
                hint = "Хвостовая рекурсия должна иметь рекурсивный вызов в самом конце функции. Компилятор Kotlin преобразует её в цикл для оптимизации",
                topic = "functional"
            ),

// 14. coroutines_basics - Корутины
            PracticeTask(
                id = "coroutine_cancellation_timeout",
                title = "Отмена корутин и таймауты",
                description = "Изучите механизмы отмены корутин и использование таймаутов для контроля времени выполнения",
                starterCode = """import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

suspend fun longRunningTask(name: String, delayMillis: Long): String {
    println("Задача '${'$'}name' начата")
    
    // Проверка активна ли корутина
    if (!currentCoroutineContext().isActive) {
        println("Задача '${'$'}name' была отменена до начала")
        return "Отменена"
    }
    
    try {
        delay(delayMillis)
        
        // Периодическая проверка отмены
        ensureActive()
        
        println("Задача '${'$'}name' завершена")
        return "Результат ${'$'}name"
    } catch (e: CancellationException) {
        println("Задача '${'$'}name' отменена: ${'$'}{e.message}")
        throw e // Перебрасываем исключение
    } finally {
        // Этот блок выполнится даже при отмене
        println("Задача '${'$'}name': блок finally")
    }
}

suspend fun taskWithTimeout(name: String, timeout: Long): String {
    return try {
        withTimeout(timeout) {
            println("Задача '${'$'}name' с таймаутом ${'$'}timeout мс")
            delay(timeout + 100) // Занимает больше времени чем таймаут
            "Успех ${'$'}name"
        }
    } catch (e: TimeoutCancellationException) {
        "Таймаут ${'$'}name: ${'$'}{e.message}"
    }
}

suspend fun cooperativeTask(name: String): String {
    println("Кооперативная задача '${'$'}name' начата")
    
    repeat(100) { i ->
        // Кооперативная проверка отмены
        yield() // Приостанавливает выполнение для проверки отмены
        
        // Или можно использовать ensureActive()
        // ensureActive()
        
        if (i % 10 == 0) {
            println("Задача '${'$'}name': шаг ${'$'}i")
        }
    }
    
    println("Кооперативная задача '${'$'}name' завершена")
    return "Результат ${'$'}name"
}

fun main() = runBlocking {
    println("=== Отмена корутин ===")
    
    // Пример 1: Отмена через Job
    println("\n1. Отмена через Job.cancel():")
    val job1 = launch {
        longRunningTask("Job1", 2000)
    }
    
    delay(500) // Даем поработать 500мс
    println("Отправляем запрос на отмену...")
    job1.cancelAndJoin()
    println("Job1 отменен")
    
    // Пример 2: Таймауты
    println("\n2. Таймауты:")
    val time2 = measureTimeMillis {
        val result = taskWithTimeout("Task2", 500)
        println("Результат: ${'$'}result")
    }
    println("Время выполнения: ${'$'}{time2}мс")
    
    // Пример 3: withTimeoutOrNull
    println("\n3. withTimeoutOrNull:")
    val result3 = withTimeoutOrNull(300) {
        delay(500)
        "Успех"
    }
    println("Результат: ${'$'}result3") // null, так как таймаут
    
    // Пример 4: Кооперативная отмена
    println("\n4. Кооперативная отмена:")
    val job4 = launch {
        cooperativeTask("Task4")
    }
    
    delay(50)
    println("Отменяем кооперативную задачу...")
    job4.cancel()
    job4.join()
    
    // Пример 5: Структурная отмена
    println("\n5. Структурная отмена (родитель отменяет детей):")
    val parentJob = launch {
        launch {
            try {
                delay(1000)
                println("Дочерняя 1 завершена")
            } catch (e: CancellationException) {
                println("Дочерняя 1 отменена")
            }
        }
        
        launch {
            try {
                delay(1500)
                println("Дочерняя 2 завершена")
            } catch (e: CancellationException) {
                println("Дочерняя 2 отменена")
            }
        }
        
        delay(500)
        println("Родительская корутина отменяет себя")
        cancel() // Отмена родителя отменяет всех детей
    }
    
    parentJob.join()
    
    // Пример 6: Некооперативная задача (нельзя отменить)
    println("\n6. Некооперативная задача (проблема):")
    val job6 = launch {
        try {
            // Этот цикл не проверяет отмену и не приостанавливается
            var i = 0
            while (i < 1_000_000_000) {
                i++
                if (i % 100_000_000 == 0) {
                    println("Некооперативная задача: ${'$'}i")
                }
            }
            println("Некооперативная задача завершена")
        } catch (e: CancellationException) {
            println("Это не напечатается - задача некооперативная")
        }
    }
    
    delay(10)
    println("Пытаемся отменить некооперативную задачу...")
    job6.cancel()
    
    // Даем немного времени для демонстрации
    delay(100)
    job6.cancel() // Повторная попытка отмены
    
    println("\nНекооперативная задача продолжает работать...")
    
    // Лучшие практики
    println("\nЛучшие практики отмены корутин:")
    println("1. Всегда делайте задачи кооперативными")
    println("2. Используйте yield() или ensureActive() в циклах")
    println("3. Обрабатывайте CancellationException")
    println("4. Используйте finally для очистки ресурсов")
    println("5. Используйте таймауты для ограничения времени выполнения")
    println("6. Не выполняйте блокирующие операции в корутинах")
}""".trimIndent(),
                solution = "",
                hint = "Корутины должны быть кооперативными для правильной отмены. Используйте yield(), delay() или ensureActive() для проверки отмены",
                topic = "coroutines_basics"
            ),
            PracticeTask(
                id = "coroutine_dispatchers_context",
                title = "Диспетчеры и контекст выполнения",
                description = "Изучите различные диспетчеры корутин и переключение контекстов выполнения",
                starterCode = """import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

suspend fun workOnDispatcher(name: String, dispatcher: CoroutineDispatcher) {
    withContext(dispatcher) {
        println("[${'$'}name] Запущено на: ${'$'}{Thread.currentThread().name}")
        delay(100)
        println("[${'$'}name] Завершено на: ${'$'}{Thread.currentThread().name}")
    }
}

suspend fun cpuIntensiveTask() = withContext(Dispatchers.Default) {
    println("[CPU] Запущено на: ${'$'}{Thread.currentThread().name}")
    var result = 0
    for (i in 0..1_000_000) {
        result += i
    }
    println("[CPU] Завершено на: ${'$'}{Thread.currentThread().name}, результат: ${'$'}result")
}

suspend fun ioIntensiveTask() = withContext(Dispatchers.IO) {
    println("[IO] Запущено на: ${'$'}{Thread.currentThread().name}")
    delay(500) // Имитация IO операции
    println("[IO] Завершено на: ${'$'}{Thread.currentThread().name}")
}

suspend fun uiTask() {
    // Имитация работы в UI потоке
    println("[UI] Имитация UI операции")
    delay(100)
}

// Создание пользовательского диспетчера
val customDispatcher = newSingleThreadContext("MyThread")

// Создание диспетчера с ограничением
val limitedDispatcher = Dispatchers.Default.limitedParallelism(2)

fun main() = runBlocking {
    println("=== Диспетчеры корутин ===")
    println("Основной поток: ${'$'}{Thread.currentThread().name}")
    
    // Пример 1: Различные диспетчеры
    println("\n1. Различные диспетчеры:")
    
    launch(Dispatchers.Default) {
        println("[Default] Поток: ${'$'}{Thread.currentThread().name}")
    }
    
    launch(Dispatchers.IO) {
        println("[IO] Поток: ${'$'}{Thread.currentThread().name}")
    }
    
    launch(Dispatchers.Unconfined) {
        println("[Unconfined] Начало на: ${'$'}{Thread.currentThread().name}")
        delay(100)
        println("[Unconfined] После delay на: ${'$'}{Thread.currentThread().name}")
    }
    
    launch(customDispatcher) {
        println("[Custom] Поток: ${'$'}{Thread.currentThread().name}")
    }
    
    delay(200)
    
    // Пример 2: withContext для переключения диспетчеров
    println("\n2. Переключение диспетчеров с withContext:")
    
    // Начинаем в Default
    println("Начало в Default: ${'$'}{Thread.currentThread().name}")
    
    // Переключаемся на IO
    withContext(Dispatchers.IO) {
        println("Переключились на IO: ${'$'}{Thread.currentThread().name}")
        delay(100)
    }
    
    // Возвращаемся в Default
    println("Вернулись в Default: ${'$'}{Thread.currentThread().name}")
    
    // Пример 3: Параллельное выполнение на разных диспетчерах
    println("\n3. Параллельное выполнение:")
    
    val time = measureTimeMillis {
        val deferred1 = async(Dispatchers.Default) { cpuIntensiveTask() }
        val deferred2 = async(Dispatchers.IO) { ioIntensiveTask() }
        
        deferred1.await()
        deferred2.await()
    }
    
    println("Общее время выполнения: ${'$'}{time}мс")
    
    // Пример 4: Ограниченный параллелизм
    println("\n4. Ограниченный параллелизм:")
    
    repeat(5) { i ->
        launch(limitedDispatcher) {
            println("[Limited-${'$'}i] Запущено на: ${'$'}{Thread.currentThread().name}")
            delay(100)
            println("[Limited-${'$'}i] Завершено на: ${'$'}{Thread.currentThread().name}")
        }
    }
    
    delay(500)
    
    // Пример 5: Контекст корутины
    println("\n5. Контекст корутины:")
    
    val job = launch(Dispatchers.Default + CoroutineName("MyCoroutine")) {
        println("Имя корутины: ${'$'}{coroutineContext[CoroutineName]?.name}")
        println("Диспетчер: ${'$'}{coroutineContext[CoroutineDispatcher]}")
        println("Job: ${'$'}{coroutineContext[Job]}")
    }
    
    job.join()
    
    // Пример 6: Наследование контекста
    println("\n6. Наследование контекста:")
    
    val parentJob = launch(Dispatchers.Default + CoroutineName("Parent")) {
        println("[Parent] Контекст: ${'$'}{coroutineContext[CoroutineName]?.name}")
        
        launch { // Наследует контекст родителя
            println("[Child1] Наследованный контекст: ${'$'}{coroutineContext[CoroutineName]?.name}")
        }
        
        launch(Dispatchers.IO + CoroutineName("Child2")) { // Новый контекст
            println("[Child2] Новый контекст: ${'$'}{coroutineContext[CoroutineName]?.name}")
        }
    }
    
    parentJob.join()
    
    // Очистка ресурсов
    customDispatcher.close()
    
    // Руководство по выбору диспетчера
    println("\nРуководство по выбору диспетчера:")
    println("1. Dispatchers.Default - CPU-интенсивные операции")
    println("2. Dispatchers.IO - операции ввода/вывода")
    println("3. Dispatchers.Main - UI операции (Android)")
    println("4. Dispatchers.Unconfined - не рекомендуется, только для тестов")
    println("5. newSingleThreadContext - для изоляции, не забывайте close()")
    println("6. limitedParallelism - для ограничения параллелизма")
    
    println("\nКогда использовать withContext:")
    println("1. Для переключения на другой диспетчер")
    println("2. Для выполнения блокирующих операций")
    println("3. Для изоляции критических секций")
    println("4. Для возврата значения из другого контекста")
}""".trimIndent(),
                solution = "",
                hint = "withContext приостанавливает текущую корутину и запускает блок кода в указанном контексте, затем возвращает результат в исходный контекст",
                topic = "coroutines_basics"
            )

        )
    }

    fun getTasksByTopic(topic: String): List<PracticeTask> {
        return getPracticeTasks().filter { it.topic == topic }
    }

    fun getTaskById(id: String): PracticeTask? {
        return getPracticeTasks().find { it.id == id }
    }
}