package com.example.devpath.data.repository.lessons

import com.example.devpath.domain.models.Lesson

object Lesson08Extensions {
    fun get(): Lesson = Lesson(
        id = "extensions",
        title = "🧩 Функции и свойства расширения",
        description = "Добавляем новые возможности к любым классам без наследования",
        difficulty = "intermediate",
        duration = 30,
        topic = "extensions",
        theory = """
            # 🧩 Функции и свойства расширения в Kotlin

            **Расширения (Extensions)** — одна из самых мощных и элегантных возможностей Kotlin. Они позволяют **добавлять новые функции и свойства к существующим классам** без наследования и без изменения их исходного кода.

            ## 📋 Содержание урока
            - Что такое расширения и зачем они нужны
            - Функции расширения (Extension Functions)
            - Свойства расширения (Extension Properties)
            - Расширения для nullable типов
            - Обобщенные расширения (Generic Extensions)
            - Инфиксные расширения
            - Расширения-приемники (Function Literals with Receiver)
            - Ограничения и лучшие практики

            ---

            ## 🤔 Зачем нужны расширения?

            Представьте, что вы работаете со стандартным классом `String` и хотите добавить метод, который проверяет, является ли строка email-адресом. В Java вам пришлось бы создать утилитный класс:

            ```java
            // Java — утилитный класс
            public class StringUtils {
                public static boolean isEmail(String str) {
                    return str.contains("@") && str.contains(".");
                }
            }
            
            // Использование
            if (StringUtils.isEmail(email)) { ... }
            ```

            В Kotlin с расширениями код становится намного элегантнее:

            ```kotlin
            // Kotlin — функция расширения
            fun String.isEmail(): Boolean {
                return this.contains("@") && this.contains(".")
            }
            
            // Использование — выглядит как метод самого класса!
            if (email.isEmail()) { ... }
            ```

            ---

            ## 📝 Функции расширения

            ### Синтаксис

            ```kotlin
            fun Класс.имяФункции(параметры): ТипВозврата {
                // this ссылается на объект, для которого вызвана функция
            }
            ```

            ### Простые примеры

            ```kotlin
            // Добавляем функцию к String
            fun String.addExclamation(): String = this + "!"
            
            // Добавляем функцию к Int
            fun Int.isEven(): Boolean = this % 2 == 0
            fun Int.isOdd(): Boolean = !this.isEven()
            
            fun main() {
                println("Hello".addExclamation())  // Hello!
                println(42.isEven())               // true
                println(43.isOdd())                 // true
            }
            ```

            ### Как это работает "под капотом"?

            Расширения не модифицируют исходный класс — они создают **статическую функцию**, которая принимает объект как первый параметр:

            ```kotlin
            // Ваш код
            fun String.addExclamation() = this + "!"
            "Hello".addExclamation()
            
            // Примерно так компилируется в байт-код
            public static final String addExclamation(String ${'$'}this) {
                return ${'$'}this + "!";
            }
            addExclamation("Hello");
            ```

            ---

            ## 🎯 Полезные расширения для стандартных классов

            ### Для String

            ```kotlin
            fun String.isEmail(): Boolean = 
                this.contains("@") && this.contains(".")
            
            fun String.isPhoneNumber(): Boolean = 
                this.matches(Regex(""${'"'}\+?\d{10,15}""${'"'}))
            
            fun String.truncate(maxLength: Int): String = 
                if (this.length <= maxLength) this 
                else this.substring(0, maxLength) + "..."
            
            fun String.reverse(): String = this.reversed()
            
            fun String.toSlug(): String = 
                this.lowercase()
                    .replace(""${'"'}[^a-zA-Z0-9\s]""${'"'}.toRegex(), "")
                    .replace(""${'"'}\s+""${'"'}.toRegex(), "-")
            
            fun String.isStrongPassword(): Boolean {
                return this.length >= 8 &&
                       this.any { it.isDigit() } &&
                       this.any { it.isLetter() } &&
                       this.any { !it.isLetterOrDigit() }
            }
            ```

            ### Для коллекций

            ```kotlin
            fun <T> List<T>.secondOrNull(): T? = this.getOrNull(1)
            
            fun <T> List<T>.penultimate(): T? = this.getOrNull(this.size - 2)
            
            fun List<Int>.sumOfEven(): Int = this.filter { it % 2 == 0 }.sum()
            
            fun <T> List<T>.containsAll(vararg elements: T): Boolean = 
                elements.all { it in this }
            
            fun <T> List<T>.middle(): T? = this.getOrNull(this.size / 2)
            
            fun <T> List<T>.secondHalf(): List<T> = 
                this.drop(this.size / 2)
            ```

            ### Для чисел

            ```kotlin
            fun Int.isPrime(): Boolean {
                if (this <= 1) return false
                for (i in 2..Math.sqrt(this.toDouble()).toInt()) {
                    if (this % i == 0) return false
                }
                return true
            }
            
            fun Int.factorial(): Long {
                var result = 1L
                for (i in 2..this) {
                    result *= i
                }
                return result
            }
            
            fun Int.toRoman(): String {
                val values = listOf(1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1)
                val symbols = listOf("M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I")
                
                var num = this
                val result = StringBuilder()
                
                for (i in values.indices) {
                    while (num >= values[i]) {
                        num -= values[i]
                        result.append(symbols[i])
                    }
                }
                return result.toString()
            }
            ```

            ---

            ## 🧬 Свойства расширения (Extension Properties)

            Можно добавлять не только функции, но и **вычисляемые свойства**:

            ```kotlin
            val String.isEmail: Boolean
                get() = this.contains("@") && this.contains(".")
            
            val String.wordCount: Int
                get() = this.split(""${'"'}\s+""${'"'}.toRegex()).size
            
            val String.firstChar: Char?
                get() = this.firstOrNull()
            
            val String.lastChar: Char?
                get() = this.lastOrNull()
            
            val String.isPalindrome: Boolean
                get() = this == this.reversed()
            
            fun main() {
                println("user@example.com".isEmail)        // true
                println("Hello world".wordCount)           // 2
                println("madam".isPalindrome)              // true
            }
            ```

            ### Важное ограничение
            Свойства расширения **не могут хранить состояние** (не имеют backing field):

            ```kotlin
            // ❌ Так нельзя — нет backing field
            var String.counter: Int
                get() = 0
                set(value) { field = value }  // Ошибка!
            ```

            ---

            ## 🎭 Расширения для nullable типов

            Можно создавать расширения, которые работают с `null`:

            ```kotlin
            fun String?.isNullOrEmptyOrBlank(): Boolean = 
                this == null || this.isBlank()
            
            fun String?.orElse(default: String): String = 
                this ?: default
            
            fun String?.getLengthOrDefault(default: Int = 0): Int = 
                this?.length ?: default
            
            fun main() {
                val s1: String? = "Hello"
                val s2: String? = null
                
                println(s1.isNullOrEmptyOrBlank())  // false
                println(s2.isNullOrEmptyOrBlank())  // true
                println(s2.orElse("Гость"))         // Гость
                println(s2.getLengthOrDefault(42))  // 42
            }
            ```

            ---

            ## 🔄 Обобщенные расширения (Generic Extensions)

            ```kotlin
            fun <T> List<T>.customJoin(separator: String = ", "): String = 
                this.joinToString(separator)
            
            fun <T : Number> List<T>.average(): Double {
                if (isEmpty()) return 0.0
                return this.sumOf { it.toDouble() } / size
            }
            
            fun <T> List<T>.secondHalf(): List<T> = 
                this.drop(this.size / 2)
            
            fun <T> List<T>.partitionBy(predicate: (T) -> Boolean): Pair<List<T>, List<T>> {
                val first = mutableListOf<T>()
                val second = mutableListOf<T>()
                for (item in this) {
                    if (predicate(item)) first.add(item)
                    else second.add(item)
                }
                return Pair(first, second)
            }
            ```

            ---

            ## 🤝 Инфиксные расширения

            С помощью `infix` можно создавать красивый синтаксис:

            ```kotlin
            infix fun String.repeatTimes(n: Int): String = this.repeat(n)
            
            infix fun Int.times(str: String): String = str.repeat(this)
            
            infix fun <T> List<T>.has(element: T): Boolean = this.contains(element)
            
            fun main() {
                println("Hi " repeatTimes 3)        // Hi Hi Hi 
                println(3 times "Kotlin ")          // Kotlin Kotlin Kotlin 
                println(listOf(1, 2, 3) has 2)      // true
            }
            ```

            ---

            ## 🏗️ Расширения-приемники (Function Literals with Receiver)

            Это основа для создания DSL (Domain Specific Language):

            ```kotlin
            class HTML {
                private val elements = mutableListOf<String>()
                
                fun body(init: Body.() -> Unit) {
                    val body = Body()
                    body.init()
                    elements.add(body.toString())
                }
                
                override fun toString() = "<html>${'$'}{'$'}{elements.joinToString("")}</html>"
            }
            
            class Body {
                private val elements = mutableListOf<String>()
                
                fun p(text: String) {
                    elements.add("<p>${'$'}{'$'}text</p>")
                }
                
                fun h1(text: String) {
                    elements.add("<h1>${'$'}{'$'}text</h1>")
                }
                
                override fun toString() = "<body>${'$'}{'$'}{elements.joinToString("")}</body>"
            }
            
            fun html(init: HTML.() -> Unit): HTML {
                val html = HTML()
                html.init()
                return html
            }
            
            // Использование DSL
            val page = html {
                body {
                    h1("Заголовок")
                    p("Первый параграф")
                    p("Второй параграф")
                }
            }
            ```

            ---

            ## 📍 Область видимости и импорт

            Расширения нужно импортировать как обычные функции:

            ```kotlin
            // File: StringExtensions.kt
            package com.example.extensions
            
            fun String.capitalizeWords(): String = 
                this.split(" ").joinToString(" ") { 
                    it.replaceFirstChar { c -> c.uppercase() } 
                }
            
            // File: Main.kt
            import com.example.extensions.capitalizeWords
            // или
            import com.example.extensions.*
            
            fun main() {
                println("hello world".capitalizeWords())  // Hello World
            }
            ```

            ---

            ## ⚠️ Ограничения расширений

            ### 1. Не могут переопределять существующие методы

            ```kotlin
            class MyClass {
                fun foo() = "original"
            }
            
            fun MyClass.foo() = "extension"  // Не переопределяет!
            
            fun main() {
                val obj = MyClass()
                println(obj.foo())  // "original" (метод класса, не расширение)
            }
            ```

            ### 2. Разрешение статическое

            Расширения выбираются **статически** на основе типа переменной, а не динамически на основе реального типа:

            ```kotlin
            open class Parent
            class Child : Parent()
            
            fun Parent.say() = "Parent"
            fun Child.say() = "Child"
            
            fun main() {
                val obj: Parent = Child()
                println(obj.say())  // "Parent" (вызывается Parent.say)
            }
            ```

            ### 3. Не имеют доступа к private членам

            ```kotlin
            class Person(private val name: String)
            
            fun Person.greet() {
                println("Hello, ${'$'}{'$'}name")  // ❌ Нельзя, name private
            }
            ```

            ---

            ## 📊 Сравнение подходов

            | Аспект | Расширения | Наследование | Утилитные функции |
            |--------|------------|--------------|-------------------|
            | **Синтаксис** | `obj.func()` | `obj.func()` | `Util.func(obj)` |
            | **Изменение класса** | Нет | Да (новый класс) | Нет |
            | **Доступ к private** | Нет | Да (protected) | Нет |
            | **Переиспользование** | Высокое | Среднее | Высокое |
            | **Производительность** | Статический вызов | Виртуальный вызов | Статический вызов |

            ---

            ## 🎨 Практические примеры

            ### Валидация форм

            ```kotlin
            fun String.isValidEmail(): Boolean {
                val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
                return this.matches(Regex(emailRegex))
            }
            
            fun String.isStrongPassword(): Boolean {
                return this.length >= 8 &&
                       this.any { it.isDigit() } &&
                       this.any { it.isLetter() } &&
                       this.any { !it.isLetterOrDigit() }
            }
            
            data class FormData(
                val email: String,
                val password: String
            ) {
                fun isValid(): Boolean = 
                    email.isValidEmail() && password.isStrongPassword()
            }
            ```

            ### Функциональные расширения

            ```kotlin
            fun <T, R> ((T) -> R).memoize(): (T) -> R {
                val cache = mutableMapOf<T, R>()
                return { key ->
                    cache.getOrPut(key) { this(key) }
                }
            }
            
            // Пример использования мемоизации
            val fibonacci: (Int) -> Long = { n ->
                when (n) {
                    0 -> 0L
                    1 -> 1L
                    else -> fibonacci(n - 1) + fibonacci(n - 2)
                }
            }.memoize()
            ```

            ---

            ## 💡 Советы

            ✅ Используйте расширения чтобы "обогащать" классы новыми методами  
            ✅ Для часто используемых операций создавайте расширения вместо утилитных классов  
            ✅ Помните, что расширения не могут переопределять существующие методы  
            ✅ Будьте осторожны с `!!` в расширениях — предпочитайте безопасные вызовы  
            ✅ Используйте обобщенные расширения для работы с разными типами  
            ✅ Для создания DSL используйте расширения-приемники  
            ✅ Импортируйте расширения явно для ясности кода

            ---

            ## 🚀 Что дальше?

            В следующем уроке мы изучим корутины — мощный инструмент для асинхронного программирования в Kotlin.
            
        """.trimIndent(),
        codeExample = """
            import kotlin.math.sqrt
            
            fun main() {
                println("🧩 ДЕМОНСТРАЦИЯ РАСШИРЕНИЙ В KOTLIN\n")
                
                // 1️⃣ БАЗОВЫЕ РАСШИРЕНИЯ ДЛЯ STRING
                println("1️⃣ РАСШИРЕНИЯ ДЛЯ STRING:")
                val text = "  Hello World from Kotlin  "
                
                println("Исходный текст: '${'$'}{'$'}text'")
                println("С восклицанием: '${'$'}{'$'}{text.addExclamation()}'")
                println("Email (user@example.com): ${'$'}{'$'}{"user@example.com".isEmail()}")
                println("Email (not-email): ${'$'}{'$'}{"not-email".isEmail()}")
                println("Обрезать до 10: '${'$'}{'$'}{text.truncate(10)}'")
                println("Перевернуть: '${'$'}{'$'}{text.reverse()}'")
                println("Slug: '${'$'}{'$'}{"Hello World! Kotlin".toSlug()}'")
                println()
                
                // 2️⃣ СВОЙСТВА РАСШИРЕНИЯ
                println("2️⃣ СВОЙСТВА РАСШИРЕНИЯ:")
                val test1 = "Hello123"
                val test2 = "madam"
                val test3 = "user@example.com"
                
                println("'${'$'}{'$'}test1' — email? ${'$'}{'$'}{test1.isEmail}")
                println("'${'$'}{'$'}test2' — палиндром? ${'$'}{'$'}{test2.isPalindrome}")
                println("'${'$'}{'$'}test3' — email? ${'$'}{'$'}{test3.isEmail}")
                println("'${'$'}{'$'}test1' — первый символ: '${'$'}{'$'}{test1.firstChar}'")
                println("'${'$'}{'$'}test1' — последний символ: '${'$'}{'$'}{test1.lastChar}'")
                println("'Hello world' — количество слов: ${'$'}{'$'}{"Hello world".wordCount}")
                println()
                
                // 3️⃣ РАСШИРЕНИЯ ДЛЯ ЧИСЕЛ
                println("3️⃣ РАСШИРЕНИЯ ДЛЯ ЧИСЕЛ:")
                val num = 42
                val primeNum = 17
                val evenNum = 24
                
                println("Число ${'$'}{'$'}num:")
                println("  чётное? ${'$'}{'$'}{num.isEven()}")
                println("  нечётное? ${'$'}{'$'}{num.isOdd()}")
                println("  квадрат: ${'$'}{'$'}{num.squared()}")
                println("  куб: ${'$'}{'$'}{num.cubed()}")
                
                println("Число ${'$'}{'$'}primeNum:")
                println("  простое? ${'$'}{'$'}{primeNum.isPrime()}")
                
                println("Число ${'$'}{'$'}evenNum:")
                println("  простое? ${'$'}{'$'}{evenNum.isPrime()}")
                
                println("5! = ${'$'}{'$'}{5.factorial()}")
                println("2024 в римских: ${'$'}{'$'}{2024.toRoman()}")
                println("42 в римских: ${'$'}{'$'}{42.toRoman()}")
                println()
                
                // 4️⃣ РАСШИРЕНИЯ ДЛЯ КОЛЛЕКЦИЙ
                println("4️⃣ РАСШИРЕНИЯ ДЛЯ КОЛЛЕКЦИЙ:")
                val numbers = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                val words = listOf("apple", "banana", "cherry", "date", "elderberry")
                
                println("Числа: ${'$'}{'$'}numbers")
                println("  второй элемент: ${'$'}{'$'}{numbers.secondOrNull()}")
                println("  предпоследний: ${'$'}{'$'}{numbers.penultimate()}")
                println("  средний: ${'$'}{'$'}{numbers.middle()}")
                println("  сумма чётных: ${'$'}{'$'}{numbers.sumOfEven()}")
                println("  содержит 1,2,3? ${'$'}{'$'}{numbers.containsAll(1, 2, 3)}")
                println("  содержит 1,2,11? ${'$'}{'$'}{numbers.containsAll(1, 2, 11)}")
                
                println("\nСлова: ${'$'}{'$'}words")
                println("  вторая половина: ${'$'}{'$'}{words.secondHalf()}")
                
                val (shortWords, longWords) = words.partitionBy { it.length <= 5 }
                println("  короткие (≤5): ${'$'}{'$'}shortWords")
                println("  длинные (>5): ${'$'}{'$'}longWords")
                println()
                
                // 5️⃣ ОБОБЩЕННЫЕ РАСШИРЕНИЯ
                println("5️⃣ ОБОБЩЕННЫЕ РАСШИРЕНИЯ:")
                val doubles = listOf(1.5, 2.5, 3.5, 4.5)
                val strings = listOf("a", "bb", "ccc", "dddd")
                
                println("Числа: ${'$'}{'$'}numbers")
                println("  пользовательское объединение: ${'$'}{'$'}{numbers.customJoin(" | ")}")
                println("  среднее: ${'$'}{'$'}{numbers.average()}")
                
                println("\nДроби: ${'$'}{'$'}doubles")
                println("  среднее: ${'$'}{'$'}{doubles.average()}")
                
                println("\nСтроки: ${'$'}{'$'}strings")
                println("  средняя длина: ${'$'}{'$'}{strings.average()}")
                println()
                
                // 6️⃣ NULLABLE РАСШИРЕНИЯ
                println("6️⃣ NULLABLE РАСШИРЕНИЯ:")
                val s1: String? = "Hello"
                val s2: String? = null
                val s3: String? = "   "
                
                println("s1 = '${'$'}{'$'}s1'")
                println("  пустое или null? ${'$'}{'$'}{s1.isNullOrEmptyOrBlank()}")
                println("  длина или 42: ${'$'}{'$'}{s1.getLengthOrDefault(42)}")
                
                println("s2 = null")
                println("  пустое или null? ${'$'}{'$'}{s2.isNullOrEmptyOrBlank()}")
                println("  или 'Гость': '${'$'}{'$'}{s2.orElse("Гость")}'")
                println("  длина или 42: ${'$'}{'$'}{s2.getLengthOrDefault(42)}")
                
                println("s3 = '   '")
                println("  пустое или null? ${'$'}{'$'}{s3.isNullOrEmptyOrBlank()}")
                println()
                
                // 7️⃣ ИНФИКСНЫЕ РАСШИРЕНИЯ
                println("7️⃣ ИНФИКСНЫЕ РАСШИРЕНИЯ:")
                println("'Hi ' повторённое 3 раза: '${'$'}{'$'}{"Hi " repeatTimes 3}'")
                println("3 раза 'Kotlin': '${'$'}{'$'}{3 times "Kotlin"}'")
                println("Список содержит 5? ${'$'}{'$'}{numbers has 5}")
                println("Список содержит 11? ${'$'}{'$'}{numbers has 11}")
                println()
                
                // 8️⃣ ВАЛИДАЦИЯ ФОРМ
                println("8️⃣ ВАЛИДАЦИЯ ФОРМ:")
                val email = "user@example.com"
                val badEmail = "user@.com"
                val password = "StrongPass123!"
                val weakPassword = "123"
                
                println("Email '${'$'}{'$'}email' валидный? ${'$'}{'$'}{email.isValidEmail()}")
                println("Email '${'$'}{'$'}badEmail' валидный? ${'$'}{'$'}{badEmail.isValidEmail()}")
                println("Пароль '${'$'}{'$'}password' надёжный? ${'$'}{'$'}{password.isStrongPassword()}")
                println("Пароль '${'$'}{'$'}weakPassword' надёжный? ${'$'}{'$'}{weakPassword.isStrongPassword()}")
                
                val form = FormData(email, password)
                val badForm = FormData(badEmail, weakPassword)
                println("Форма валидна? ${'$'}{'$'}{form.isValid()}")
                println("Плохая форма валидна? ${'$'}{'$'}{badForm.isValid()}")
                println()
                
                // 9️⃣ МЕМОИЗАЦИЯ (КЭШИРОВАНИЕ)
                println("9️⃣ МЕМОИЗАЦИЯ:")
                val fibonacci: (Int) -> Long = { n ->
                    when (n) {
                        0 -> 0L
                        1 -> 1L
                        else -> fibonacci(n - 1) + fibonacci(n - 2)
                    }
                }.memoize()
                
                println("fibonacci(10) = ${'$'}{'$'}{fibonacci(10)}")
                println("fibonacci(20) = ${'$'}{'$'}{fibonacci(20)}")
                println("fibonacci(30) = ${'$'}{'$'}{fibonacci(30)}")
                println("fibonacci(40) = ${'$'}{'$'}{fibonacci(40)}")
                println()
                
                // 🔟 DSL ПРИМЕР
                println("🔟 HTML DSL:")
                val html = html {
                    body {
                        h1("Мой сайт на Kotlin")
                        p("Добро пожаловать в мир расширений!")
                        p("Это пример DSL, созданного с помощью расширений-приемников")
                    }
                }
                println(html)
                
                println("\n=== КОНЕЦ ДЕМОНСТРАЦИИ ===")
            }
            
            // ==================== РАСШИРЕНИЯ ====================
            
            // 1️⃣ String расширения
            fun String.addExclamation(): String = this + "!"
            
            fun String.isEmail(): Boolean = 
                this.contains("@") && this.contains(".")
            
            fun String.truncate(maxLength: Int): String = 
                if (this.length <= maxLength) this 
                else this.substring(0, maxLength) + "..."
            
            fun String.reverse(): String = this.reversed()
            
            fun String.toSlug(): String = 
                this.lowercase()
                    .replace(""${'"'}[^a-zA-Z0-9\s]""${'"'}.toRegex(), "")
                    .replace(""${'"'}\s+""${'"'}.toRegex(), "-")
            
            fun String.isValidEmail(): Boolean {
                val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
                return this.matches(Regex(emailRegex))
            }
            
            fun String.isStrongPassword(): Boolean {
                return this.length >= 8 &&
                       this.any { it.isDigit() } &&
                       this.any { it.isLetter() } &&
                       this.any { !it.isLetterOrDigit() }
            }
            
            // 2️⃣ String свойства
            val String.isEmail: Boolean
                get() = this.contains("@") && this.contains(".")
            
            val String.wordCount: Int
                get() = this.split(""${'"'}\s+""${'"'}.toRegex()).size
            
            val String.firstChar: Char?
                get() = this.firstOrNull()
            
            val String.lastChar: Char?
                get() = this.lastOrNull()
            
            val String.isPalindrome: Boolean
                get() = this == this.reversed()
            
            // 3️⃣ Int расширения
            fun Int.isEven(): Boolean = this % 2 == 0
            fun Int.isOdd(): Boolean = !this.isEven()
            fun Int.squared(): Int = this * this
            fun Int.cubed(): Int = this * this * this
            
            fun Int.isPrime(): Boolean {
                if (this <= 1) return false
                for (i in 2..sqrt(this.toDouble()).toInt()) {
                    if (this % i == 0) return false
                }
                return true
            }
            
            fun Int.factorial(): Long {
                var result = 1L
                for (i in 2..this) {
                    result *= i
                }
                return result
            }
            
            fun Int.toRoman(): String {
                val values = listOf(1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1)
                val symbols = listOf("M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I")
                
                var num = this
                val result = StringBuilder()
                
                for (i in values.indices) {
                    while (num >= values[i]) {
                        num -= values[i]
                        result.append(symbols[i])
                    }
                }
                return result.toString()
            }
            
            // 4️⃣ List расширения
            fun <T> List<T>.secondOrNull(): T? = this.getOrNull(1)
            
            fun <T> List<T>.penultimate(): T? = this.getOrNull(this.size - 2)
            
            fun <T> List<T>.middle(): T? = this.getOrNull(this.size / 2)
            
            fun List<Int>.sumOfEven(): Int = this.filter { it % 2 == 0 }.sum()
            
            fun <T> List<T>.containsAll(vararg elements: T): Boolean = 
                elements.all { it in this }
            
            fun <T> List<T>.secondHalf(): List<T> = 
                this.drop(this.size / 2)
            
            fun <T> List<T>.partitionBy(predicate: (T) -> Boolean): Pair<List<T>, List<T>> {
                val first = mutableListOf<T>()
                val second = mutableListOf<T>()
                for (item in this) {
                    if (predicate(item)) first.add(item)
                    else second.add(item)
                }
                return Pair(first, second)
            }
            
            // 5️⃣ Generic расширения
            fun <T> List<T>.customJoin(separator: String = ", "): String = 
                this.joinToString(separator)
            
            fun <T : Number> List<T>.average(): Double {
                if (isEmpty()) return 0.0
                return this.sumOf { it.toDouble() } / size
            }
            
            // 6️⃣ Nullable расширения
            fun String?.isNullOrEmptyOrBlank(): Boolean = 
                this == null || this.isBlank()
            
            fun String?.orElse(default: String): String = 
                this ?: default
            
            fun String?.getLengthOrDefault(default: Int = 0): Int = 
                this?.length ?: default
            
            // 7️⃣ Инфиксные расширения
            infix fun String.repeatTimes(n: Int): String = this.repeat(n)
            
            infix fun Int.times(str: String): String = str.repeat(this)
            
            infix fun <T> List<T>.has(element: T): Boolean = this.contains(element)
            
            // 8️⃣ Мемоизация
            fun <T, R> ((T) -> R).memoize(): (T) -> R {
                val cache = mutableMapOf<T, R>()
                return { key ->
                    cache.getOrPut(key) { this(key) }
                }
            }
            
            // 9️⃣ Валидация форм
            data class FormData(
                val email: String,
                val password: String
            ) {
                fun isValid(): Boolean = 
                    email.isValidEmail() && password.isStrongPassword()
            }
            
            // 🔟 HTML DSL
            class HTML {
                private val elements = mutableListOf<String>()
                
                fun body(init: Body.() -> Unit) {
                    val body = Body()
                    body.init()
                    elements.add(body.toString())
                }
                
                override fun toString() = "<html>${'$'}{'$'}{elements.joinToString("")}</html>"
            }
            
            class Body {
                private val elements = mutableListOf<String>()
                
                fun h1(text: String) {
                    elements.add("<h1>${'$'}{'$'}text</h1>")
                }
                
                fun p(text: String) {
                    elements.add("<p>${'$'}{'$'}text</p>")
                }
                
                override fun toString() = "<body>${'$'}{'$'}{elements.joinToString("")}</body>"
            }
            
            fun html(init: HTML.() -> Unit): HTML {
                val html = HTML()
                html.init()
                return html
            }
        """.trimIndent()
    )
}