package com.example.devpath.data.repository.lessons

import com.example.devpath.domain.models.Lesson

object Lesson07NullSafety {
    fun get(): Lesson = Lesson(
        id = "null_safety",
        title = "🛡️ Null Safety в Kotlin",
        description = "Полное руководство по работе с nullable типами и безопасному коду",
        difficulty = "intermediate",
        duration = 35,
        topic = "null_safety",
        theory = """
            # 🛡️ Null Safety в Kotlin — прощай, NullPointerException!

            Одной из самых революционных фич Kotlin является встроенная система **null safety**, которая практически устраняет опасность `NullPointerException` (NPE) — ошибку, которую называют "миллиардной ошибкой" (billion dollar mistake).

            ## 🎯 Проблема null в языках программирования

            В Java, C# и многих других языках любая ссылка может быть `null`, что приводит к:

            ```java
            // Java код
            String name = null;
            int length = name.length();  // 💥 NullPointerException!
            ```

            В Kotlin эта проблема решается **на уровне системы типов**.

            ## 🔷 Nullable и Non-null типы

            В Kotlin типы разделены на две категории:

            | Тип | Может быть null? | Пример |
            |-----|------------------|--------|
            | `String` | ❌ Нет | `val name: String = "Kotlin"` |
            | `String?` | ✅ Да | `val name: String? = null` |

            ```kotlin
            // Non-null тип (не может быть null)
            var nonNull: String = "Kotlin"
            // nonNull = null  // ❌ Ошибка компиляции!
            
            // Nullable тип (может быть null)
            var nullable: String? = "Kotlin"
            nullable = null  // ✅ OK
            ```

            ### Почему это гениально?

            Компилятор **требует** проверки перед использованием nullable переменной:

            ```kotlin
            fun printLength(text: String?) {
                // println(text.length)  // ❌ Ошибка! text может быть null
                
                if (text != null) {
                    println(text.length)  // ✅ Компилятор знает, что text не null
                }
            }
            ```

            ## 🔍 Безопасные вызовы (Safe Calls) — оператор `?.`

            Оператор `?.` позволяет безопасно вызывать методы и обращаться к свойствам:

            ```kotlin
            val text: String? = "Hello"
            val length: Int? = text?.length  
            // Если text == null, то length = null
            // Если text != null, то length = text.length
            
            val upper = text?.uppercase()  // "HELLO" или null
            ```

            ### Цепочка безопасных вызовов

            Особенно полезно для вложенных объектов:

            ```kotlin
            data class Address(val street: String?, val city: String?)
            data class User(val name: String, val address: Address?)
            
            val user: User? = User("Алексей", Address("Тверская", "Москва"))
            
            // Безопасное получение улицы
            val street = user?.address?.street  // String? — может быть null на любом уровне
            println("Улица: ${'$'}street")
            
            // Если где-то null, вся цепочка вернёт null
            val invalidUser: User? = User("Петр", null)
            val invalidStreet = invalidUser?.address?.street  // null
            ```

            ## 🎸 Оператор Элвиса (Elvis Operator) — `?:`

            Оператор Элвиса (похож на причёску Элвиса Пресли) позволяет задать значение по умолчанию, если слева от него `null`:

            ```kotlin
            val text: String? = null
            
            // Классический способ
            val length1 = if (text != null) text.length else 0
            
            // С оператором Элвиса (гораздо короче!)
            val length2 = text?.length ?: 0
            
            println("Длина: ${'$'}length2")  // 0
            ```

            ### Использование с `throw` и `return`

            ```kotlin
            fun processUser(user: User?) {
                val name = user?.name ?: throw IllegalArgumentException("User must have name")
                val age = user?.address?.city ?: return  // выход из функции если нет города
                
                println("Обрабатываем: ${'$'}name, город: ${'$'}age")
            }
            ```

            ### Несколько примеров Элвиса

            ```kotlin
            val scores = mapOf("Alice" to 95, "Bob" to null)
            
            val aliceScore = scores["Alice"] ?: 0  // 95
            val bobScore = scores["Bob"] ?: 0      // 0 (вместо null)
            val charlieScore = scores["Charlie"] ?: 0  // 0 (ключа нет)
            ```

            ## ⚠️ Оператор `!!` — утверждение не-null

            Оператор `!!` говорит компилятору: "Я **точно знаю**, что здесь не null". Используйте его **только** когда абсолютно уверены!

            ```kotlin
            val text: String? = "Hello"
            val length = text!!.length  // ✅ OK, text не null
            ```

            Если окажется null, получите NPE:

            ```kotlin
            val text: String? = null
            val length = text!!.length  // 💥 NullPointerException!
            
            // Исключение будет указывать на конкретную строку:
            // Exception in thread "main" kotlin.KotlinNullPointerException
            //     at MyFileKt.main (MyFile.kt:3)
            ```

            ### Когда использовать `!!`?
            - При взаимодействии с Java кодом, где вы уверены в не-null значении
            - В тестах
            - Когда значение действительно **не может** быть null по логике программы

            **Лучше избегать `!!`** — почти всегда можно использовать безопасные вызовы или Элвиса.

            ## 🔄 Безопасное приведение типов (Safe Cast) — `as?`

            Обычное приведение `as` может выбросить `ClassCastException`:

            ```kotlin
            val obj: Any = "Hello"
            val str: String = obj as String  // OK
            val num: Int = obj as Int        // 💥 ClassCastException!
            ```

            Безопасное приведение `as?` возвращает `null` при неудаче:

            ```kotlin
            val obj: Any = "Hello"
            
            val str: String? = obj as? String  // "Hello"
            val num: Int? = obj as? Int        // null (без исключения!)
            
            println(str)  // Hello
            println(num)  // null
            ```

            ## 📚 Коллекции и null safety

            ### Коллекции с nullable элементами

            ```kotlin
            val listWithNulls: List<Int?> = listOf(1, null, 2, null, 3)
            
            // filterNotNull() — оставляет только не-null элементы
            val withoutNulls: List<Int> = listWithNulls.filterNotNull()
            println(withoutNulls)  // [1, 2, 3]
            
            // mapNotNull — трансформация с отбрасыванием null
            val doubled = listWithNulls.mapNotNull { it?.times(2) }
            println(doubled)  // [2, 4, 6]
            ```

            ### Безопасное получение элемента

            ```kotlin
            val list = listOf(1, 2, 3)
            
            // elementAtOrNull — безопасное получение по индексу
            val element = list.elementAtOrNull(5)  // null
            val safe = list.elementAtOrElse(5) { -1 }  // -1
            
            // firstOrNull / lastOrNull
            val firstEven = list.firstOrNull { it % 2 == 0 }  // 2
            val firstBig = list.firstOrNull { it > 10 }       // null
            ```

            ## 🎯 Функции с nullable параметрами

            ```kotlin
            fun printLength(str: String?) {
                // Вариант 1: безопасный вызов + Элвис
                val length = str?.length ?: -1
                println("Длина: ${'$'}length")
                
                // Вариант 2: проверка на null
                if (str == null) {
                    println("Строка null")
                } else {
                    println("Длина: ${'$'}{str.length}")
                }
            }
            
            // Функция с nullable параметром и значением по умолчанию
            fun greet(name: String? = null) {
                val displayName = name ?: "Гость"
                println("Привет, ${'$'}displayName!")
            }
            ```

            ## 🧩 Расширения для nullable типов

            Можно писать функции-расширения для nullable типов:

            ```kotlin
            fun String?.isNullOrEmptyOrBlank(): Boolean {
                return this == null || this.isBlank()
            }
            
            fun String?.getLengthOrDefault(default: Int = 0): Int {
                return this?.length ?: default
            }
            
            fun main() {
                val text1: String? = "Hello"
                val text2: String? = null
                val text3: String? = "   "
                
                println(text1.isNullOrEmptyOrBlank())  // false
                println(text2.isNullOrEmptyOrBlank())  // true
                println(text3.isNullOrEmptyOrBlank())  // true (потому что isBlank)
                
                println(text1.getLengthOrDefault())    // 5
                println(text2.getLengthOrDefault(42))  // 42
            }
            ```

            ## ⏱️ Поздняя инициализация (lateinit)

            Иногда невозможно инициализировать свойство в конструкторе (например, в Android при внедрении зависимостей). Для таких случаев есть `lateinit`:

            ```kotlin
            class UserManager {
                lateinit var database: Database
                
                fun initialize(db: Database) {
                    this.database = db
                }
                
                fun getUsers(): List<User> {
                    // Здесь мы уверены, что database уже инициализирована
                    return database.query("SELECT * FROM users")
                }
                
                fun isDatabaseInitialized(): Boolean {
                    return ::database.isInitialized  // проверка инициализации
                }
            }
            ```

            ### Правила lateinit:
            - Только для `var` (не для `val`)
            - Не может быть nullable (нельзя `lateinit var name: String?`)
            - Не может быть примитивного типа (`Int`, `Double` и т.д.)
            - До инициализации обращение вызовет исключение

            ## 👀 Делегаты для nullable свойств

            ### observable делегат для отслеживания изменений

            ```kotlin
            import kotlin.properties.Delegates
            
            class User {
                var name: String? by Delegates.observable(null) { prop, old, new ->
                    println("Имя изменилось: '${'$'}old' -> '${'$'}new'")
                }
                
                var age: Int by Delegates.vetoable(0) { _, _, new ->
                    new >= 0  // запрещаем отрицательный возраст
                }
            }
            ```

            ### notNull делегат

            ```kotlin
            class Config {
                var timeout: Int by Delegates.notNull<Int>()
                
                fun initialize() {
                    timeout = 5000
                }
                
                fun getTimeout(): Int {
                    // Если не инициализировано — исключение
                    return timeout
                }
            }
            ```

            ## 🧠 Умные приведения (Smart Casts)

            Компилятор Kotlin отслеживает проверки и автоматически приводит типы:

            ```kotlin
            fun processString(str: String?) {
                if (str == null) {
                    println("str is null")
                    return
                }
                
                // Здесь компилятор ЗНАЕТ, что str не null!
                println(str.length)  // автоматически String, а не String?
            }
            ```

            ### Проверки в условиях

            ```kotlin
            val text: String? = getText()
            
            // 1. Простая проверка
            if (text != null) {
                println(text.length)  // Smart cast
            }
            
            // 2. Проверка с and
            if (text != null && text.length > 5) {
                println("Длинный текст: ${'$'}text")  // Smart cast
            }
            
            // 3. Проверка с or
            if (text == null || text.length == 0) {
                println("Текст пуст или null")
            } else {
                println(text.length)  // Smart cast
            }
            
            // 4. Проверка с when
            when (text) {
                null -> println("null")
                else -> println("Длина: ${'$'}{text.length}")  // Smart cast
            }
            ```

            ## 🌉 Платформенные типы (Platform Types)

            При взаимодействии с Java кодом появляются **платформенные типы** (обозначаются `!`):

            ```kotlin
            // Java код
            public class JavaClass {
                public String getName() { ... }      // может вернуть null?
                public String getAddress() { ... }   // может вернуть null?
            }
            
            // Kotlin код
            val javaObj = JavaClass()
            
            // Тип String! (platform type) — может быть String или String?
            val name = javaObj.name  
            
            // Вы сами решаете, как с ним обращаться:
            val nonNullName: String = javaObj.name  // риск NPE если null
            val nullableName: String? = javaObj.name  // безопасно
            ```

            ### Аннотации для Java кода

            Чтобы сделать взаимодействие безопаснее, используйте аннотации:

            ```kotlin
            // Java
            import org.jetbrains.annotations.NotNull;
            import org.jetbrains.annotations.Nullable;
            
            public class JavaClass {
                @NotNull
                public String getName() { return "John"; }
                
                @Nullable
                public String getAddress() { return null; }
            }
            
            // Kotlin
            val javaObj = JavaClass()
            val name: String = javaObj.name     // @NotNull → String
            val address: String? = javaObj.address  // @Nullable → String?
            ```

            ## 🎨 Практические паттерны

            ### Безопасный билдер

            ```kotlin
            class Person private constructor(
                val name: String,
                val age: Int?,
                val email: String?
            ) {
                class Builder {
                    private var name: String? = null
                    private var age: Int? = null
                    private var email: String? = null
                    
                    fun setName(name: String) = apply { this.name = name }
                    fun setAge(age: Int) = apply { this.age = age }
                    fun setEmail(email: String) = apply { this.email = email }
                    
                    fun build(): Person? {
                        val name = name ?: return null  // обязательное поле
                        return Person(name, age, email)
                    }
                }
            }
            ```

            ### Функция с несколькими nullable параметрами

            ```kotlin
            data class SearchCriteria(
                val query: String?,
                val category: String?,
                val minPrice: Double?,
                val maxPrice: Double?
            )
            
            fun search(criteria: SearchCriteria): List<Product> {
                return products.filter { product ->
                    criteria.query?.let { product.name.contains(it) } ?: true &&
                    criteria.category?.let { product.category == it } ?: true &&
                    criteria.minPrice?.let { product.price >= it } ?: true &&
                    criteria.maxPrice?.let { product.price <= it } ?: true
                }
            }
            ```

            ## 📊 Сравнение подходов к null safety

            | Ситуация | Подход | Пример |
            |----------|--------|--------|
            | Простой доступ | Safe call `?.` | `user?.name` |
            | Значение по умолчанию | Elvis `?:` | `user?.name ?: "Гость"` |
            | Проверка перед использованием | if check | `if (user != null) { ... }` |
            | Цепочка вызовов | Chained `?.` | `user?.address?.city` |
            | Уверенность в не-null | `!!` | `user!!.name` |
            | Поздняя инициализация | `lateinit` | `lateinit var db` |
            | Наблюдение за изменениями | Delegates | `by Delegates.observable()` |

            ## 🚫 Чего следует избегать

            ❌ **Избегайте `!!`** — это костыль, а не решение  
            ❌ **Не игнорируйте предупреждения компилятора** о nullable типах  
            ❌ **Не используйте `lateinit` для примитивных типов**  
            ❌ **Не возвращайте `null` без необходимости** — используйте пустые коллекции или специальные объекты  

            ## ✅ Лучшие практики

            ✅ **Используйте nullable типы только когда это действительно нужно**  
            ✅ **Предпочитайте `?.` и `?:` проверкам на `null`** — код чище  
            ✅ **Для коллекций используйте `filterNotNull()` и `mapNotNull()`**  
            ✅ **В public API документируйте, может ли параметр/возврат быть `null`**  
            ✅ **Используйте аннотации `@Nullable`/`@NotNull` при взаимодействии с Java**  
            ✅ **Для обязательных полей используйте `lateinit` вместо nullable**  

            ## 📝 Резюме

            ✅ **Nullable типы** (`String?`) могут быть null, non-null (`String`) — нет  
            ✅ **Safe call `?.`** — безопасный доступ к членам  
            ✅ **Elvis `?:`** — значение по умолчанию для null  
            ✅ **`!!`** — утверждение не-null (используйте с осторожностью!)  
            ✅ **Smart casts** — автоматическое приведение после проверки  
            ✅ **`lateinit`** — для поздней инициализации  
            ✅ **`filterNotNull()`** — для очистки коллекций от null  
            ✅ **Platform types** — при взаимодействии с Java  

            Null safety в Kotlin — это не просто защита от ошибок, это новый способ мышления о данных. Он делает код надёжнее, читаемее и самодокументированнее!
        """.trimIndent(),
        codeExample = """
            import kotlin.properties.Delegates
            
            fun main() {
                println("🛡️ Null Safety в Kotlin - демонстрация всех возможностей\n")
                
                // 1️⃣ Nullable и Non-null типы
                println("1️⃣ Nullable и Non-null типы")
                var nonNull: String = "Никогда не null"
                var nullable: String? = "Может быть null"
                
                println("nonNull: ${'$'}nonNull")
                println("nullable: ${'$'}nullable")
                
                nullable = null
                println("nullable после null: ${'$'}nullable")
                
                // nonNull = null  // ❌ Ошибка компиляции - раскомментируйте для проверки
                println()
                
                // 2️⃣ Безопасные вызовы (Safe calls)
                println("2️⃣ Безопасные вызовы")
                val text1: String? = "Kotlin"
                val text2: String? = null
                
                println("text1?.length = ${'$'}{text1?.length}")  // 6
                println("text2?.length = ${'$'}{text2?.length}")  // null
                
                // Цепочка безопасных вызовов
                data class Address(val street: String?, val city: String?)
                data class User(val name: String, val address: Address?)
                
                val user1 = User("Алексей", Address("Тверская", "Москва"))
                val user2 = User("Петр", null)
                val user3: User? = null
                
                println("user1 улица: ${'$'}{user1.address?.street}")
                println("user2 улица: ${'$'}{user2.address?.street}")
                println("user3 улица: ${'$'}{user3?.address?.street}")
                println()
                
                // 3️⃣ Оператор Элвиса (Elvis)
                println("3️⃣ Оператор Элвиса")
                val input: String? = null
                
                val length1 = input?.length ?: 0
                val length2 = input?.length ?: -1
                
                println("Длина с дефолтом 0: ${'$'}length1")
                println("Длина с дефолтом -1: ${'$'}length2")
                
                // Использование с throw
                fun requireNonNull(value: String?): String {
                    return value ?: throw IllegalArgumentException("Значение не может быть null")
                }
                
                try {
                    requireNonNull(null)
                } catch (e: IllegalArgumentException) {
                    println("Исключение: ${'$'}{e.message}")
                }
                println()
                
                // 4️⃣ Оператор !! (с осторожностью!)
                println("4️⃣ Оператор !! (утверждение не-null)")
                val safe: String? = "Безопасное значение"
                
                println("safe!!.length = ${'$'}{safe!!.length}")  // OK
                
                try {
                    val danger: String? = null
                    println(danger!!.length)  // 💥 Исключение!
                } catch (e: NullPointerException) {
                    println("Поймано NPE: ${'$'}{e.message}")
                }
                println()
                
                // 5️⃣ Безопасное приведение типов
                println("5️⃣ Безопасное приведение (as?)")
                val obj1: Any = "Hello"
                val obj2: Any = 123
                
                val str1 = obj1 as? String
                val str2 = obj2 as? String
                val num1 = obj1 as? Int
                val num2 = obj2 as? Int
                
                println("obj1 as? String: ${'$'}str1")
                println("obj2 as? String: ${'$'}str2")  // null
                println("obj1 as? Int: ${'$'}num1")     // null
                println("obj2 as? Int: ${'$'}num2")     // 123
                println()
                
                // 6️⃣ Коллекции и null safety
                println("6️⃣ Коллекции и null safety")
                val numbers: List<Int?> = listOf(1, null, 2, null, 3, 4, null, 5)
                
                println("Исходный список: ${'$'}numbers")
                println("filterNotNull(): ${'$'}{numbers.filterNotNull()}")
                println("mapNotNull { it?.times(2) }: ${'$'}{numbers.mapNotNull { it?.times(2) }}")
                
                // Безопасное получение элементов
                val list = listOf(10, 20, 30)
                println("elementAtOrNull(5): ${'$'}{list.elementAtOrNull(5)}")
                println("firstOrNull { it > 100 }: ${'$'}{list.firstOrNull { it > 100 }}")
                println()
                
                // 7️⃣ Проверки в условиях (Smart casts)
                println("7️⃣ Умные приведения")
                fun processText(text: String?) {
                    println("Обработка: ${'$'}text")
                    
                    if (text == null) {
                        println("  -> текст null")
                        return
                    }
                    
                    // Здесь text автоматически String!
                    println("  -> длина: ${'$'}{text.length}")
                    println("  -> заглавные: ${'$'}{text.uppercase()}")
                }
                
                processText("Kotlin")
                processText(null)
                println()
                
                // 8️⃣ Поздняя инициализация (lateinit)
                println("8️⃣ Поздняя инициализация")
                val processor = DataProcessor()
                
                println("Инициализирована? ${'$'}{processor.isInitialized()}")
                
                try {
                    processor.process()  // Ошибка!
                } catch (e: UninitializedPropertyAccessException) {
                    println("Ошибка: ${'$'}{e.message}")
                }
                
                processor.initialize("Важные данные для обработки")
                println("Инициализирована? ${'$'}{processor.isInitialized()}")
                processor.process()
                println()
                
                // 9️⃣ Делегаты для nullable свойств
                println("9️⃣ Делегаты для nullable")
                val config = Configuration()
                
                config.setting = "Первое значение"
                config.setting = "Второе значение"
                config.setting = null
                
                // notNull делегат
                val settings = AppSettings()
                
                try {
                    println(settings.timeout)  // Ошибка!
                } catch (e: IllegalStateException) {
                    println("timeout не инициализирован: ${'$'}{e.message}")
                }
                
                settings.initialize()
                println("timeout после инициализации: ${'$'}{settings.timeout}")
                println()
                
                // 🔟 Функции с nullable параметрами
                println("🔟 Функции с nullable параметрами")
                greetUser("Алексей")
                greetUser(null)
                
                printLength("Hello")
                printLength(null)
                println()
                
                // 1️⃣1️⃣ Расширения для nullable типов
                println("1️⃣1️⃣ Расширения для nullable")
                val s1: String? = "Kotlin"
                val s2: String? = null
                val s3: String? = "   "
                
                println("s1.isNullOrEmptyOrBlank(): ${'$'}{s1.isNullOrEmptyOrBlank()}")
                println("s2.isNullOrEmptyOrBlank(): ${'$'}{s2.isNullOrEmptyOrBlank()}")
                println("s3.isNullOrEmptyOrBlank(): ${'$'}{s3.isNullOrEmptyOrBlank()}")
                
                println("s1.getLengthOrDefault(): ${'$'}{s1.getLengthOrDefault()}")
                println("s2.getLengthOrDefault(42): ${'$'}{s2.getLengthOrDefault(42)}")
                println()
                
                // 1️⃣2️⃣ Практический пример: безопасный билдер
                println("1️⃣2️⃣ Безопасный билдер")
                val person1 = Person.Builder()
                    .setName("Иван Петров")
                    .setAge(30)
                    .setEmail("ivan@example.com")
                    .build()
                
                val person2 = Person.Builder()
                    .setAge(25)
                    .setEmail("test@example.com")
                    .build()  // нет имени → null
                
                println("person1: ${'$'}person1")
                println("person2: ${'$'}person2")
                println()
                
                // 1️⃣3️⃣ Поиск с null-safe фильтрацией
                println("1️⃣3️⃣ Поиск с null-safe фильтрацией")
                val products = listOf(
                    Product("Ноутбук", "Электроника", 75000.0),
                    Product("Мышь", "Электроника", 1500.0),
                    Product("Книга", "Книги", 500.0),
                    Product("Наушники", "Электроника", 3000.0),
                    Product("Тетрадь", "Канцелярия", 50.0)
                )
                
                val criteria = SearchCriteria("Электроника", 1000.0, 50000.0)
                val filtered = searchProducts(products, criteria)
                
                println("Критерии: категория=${'$'}{criteria.category}, цена от ${'$'}{criteria.minPrice} до ${'$'}{criteria.maxPrice}")
                println("Результаты:")
                filtered.forEach { println("  - ${'$'}{it.name}: ${'$'}{it.price}") }
                println()
                
                // 1️⃣4️⃣ Статистика с nullable значениями
                println("1️⃣4️⃣ Статистика с nullable")
                val data = listOf(10, null, 20, null, 30, 40, null, 50)
                
                val validData = data.filterNotNull()
                val stats = DataStats(
                    count = validData.size,
                    sum = validData.sum(),
                    average = validData.average(),
                    min = validData.minOrNull(),
                    max = validData.maxOrNull()
                )
                
                println("Данные: ${'$'}data")
                println("Статистика:")
                println("  count: ${'$'}{stats.count}")
                println("  sum: ${'$'}{stats.sum}")
                println("  average: ${'$'}{stats.average}")
                println("  min: ${'$'}{stats.min}")
                println("  max: ${'$'}{stats.max}")
            }
            
            // Классы для примеров
            
            class DataProcessor {
                lateinit var data: String
                
                fun initialize(value: String) {
                    data = value
                    println("📦 Данные инициализированы: ${'$'}value")
                }
                
                fun process() {
                    if (::data.isInitialized) {
                        println("⚙️ Обработка: ${'$'}data.uppercase()")
                    } else {
                        throw UninitializedPropertyAccessException("data не инициализирована")
                    }
                }
                
                fun isInitialized(): Boolean = ::data.isInitialized
            }
            
            class Configuration {
                var setting: String? by Delegates.observable(null) { prop, old, new ->
                    println("⚙️ ${'$'}{prop.name}: '${'$'}old' -> '${'$'}new'")
                }
            }
            
            class AppSettings {
                var timeout: Int by Delegates.notNull<Int>()
                
                fun initialize() {
                    timeout = 5000
                }
            }
            
            // Расширения для nullable строк
            fun String?.isNullOrEmptyOrBlank(): Boolean {
                return this == null || this.isBlank()
            }
            
            fun String?.getLengthOrDefault(default: Int = 0): Int {
                return this?.length ?: default
            }
            
            // Функции с nullable параметрами
            fun greetUser(name: String?) {
                val displayName = name ?: "Гость"
                println("👋 Привет, ${'$'}displayName!")
            }
            
            fun printLength(text: String?) {
                val length = text?.length ?: -1
                println("📏 Длина '${'$'}text': ${'$'}length")
            }
            
            // Безопасный билдер
            data class Person(
                val name: String,
                val age: Int?,
                val email: String?
            ) {
                class Builder {
                    private var name: String? = null
                    private var age: Int? = null
                    private var email: String? = null
                    
                    fun setName(name: String) = apply { this.name = name }
                    fun setAge(age: Int) = apply { this.age = age }
                    fun setEmail(email: String) = apply { this.email = email }
                    
                    fun build(): Person? {
                        val name = name ?: return null
                        return Person(name, age, email)
                    }
                }
            }
            
            // Поиск с критериями
            data class Product(
                val name: String,
                val category: String,
                val price: Double
            )
            
            data class SearchCriteria(
                val category: String?,
                val minPrice: Double?,
                val maxPrice: Double?
            )
            
            fun searchProducts(products: List<Product>, criteria: SearchCriteria): List<Product> {
                return products.filter { product ->
                    (criteria.category?.let { product.category == it } ?: true) &&
                    (criteria.minPrice?.let { product.price >= it } ?: true) &&
                    (criteria.maxPrice?.let { product.price <= it } ?: true)
                }
            }
            
            // Статистика
            data class DataStats(
                val count: Int,
                val sum: Int,
                val average: Double,
                val min: Int?,
                val max: Int?
            )
        """.trimIndent()
    )
}