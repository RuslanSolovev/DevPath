package com.example.devpath.data.repository.lessons

import com.example.devpath.domain.models.Lesson

object Lesson11Delegation {
    fun get(): Lesson = Lesson(
        id = "delegation",
        title = "🔄 Делегирование в Kotlin",
        description = "Композиция вместо наследования, делегированные свойства и практические паттерны",
        difficulty = "advanced",
        duration = 45,
        topic = "delegation",
        theory = """
            # 🔄 Делегирование в Kotlin

            **Делегирование** — это мощный паттерн проектирования, при котором объект передаёт выполнение задачи другому объекту (делегату). Kotlin поддерживает этот паттерн на уровне языка, что позволяет легко реализовывать композицию вместо наследования и создавать переиспользуемые компоненты.

            ## 🎯 Зачем нужно делегирование?

            | Проблема | Решение с делегированием |
            |----------|-------------------------|
            | Наследование создаёт жёсткую связь между классами | Композиция через делегирование гибче |
            | Множественное наследование не поддерживается | Можно делегировать нескольким объектам |
            | Повторяющийся код в свойствах | Делегированные свойства устраняют дублирование |
            | Сложность тестирования | Делегаты легко заменять моками |

            ## 🏗️ Делегирование реализации (Implementation by Delegation)

            Ключевое слово `by` позволяет классу делегировать реализацию интерфейса другому объекту.

            ### Базовый пример

            ```kotlin
            interface Printer {
                fun print(message: String)
                fun getStatus(): String
            }

            class ConsolePrinter : Printer {
                override fun print(message: String) {
                    println("Вывод в консоль: ${'$'}message")
                }
                
                override fun getStatus(): String = "Консольный принтер готов"
            }

            // Класс DocumentProcessor делегирует всю работу принтеру
            class DocumentProcessor(printer: Printer) : Printer by printer
            //                 👆 всё, что нужно — ключевое слово by!

            fun main() {
                val processor = DocumentProcessor(ConsolePrinter())
                processor.print("Документ")  // Вывод в консоль: Документ
                println(processor.getStatus()) // Консольный принтер готов
            }
            ```

            ### Множественное делегирование

            Kotlin позволяет делегировать нескольким интерфейсам одновременно:

            ```kotlin
            interface Uploader {
                fun upload(data: String)
                fun getProgress(): Int
            }

            interface Validator {
                fun validate(data: String): Boolean
            }

            class SimpleUploader : Uploader {
                override fun upload(data: String) = println("Загрузка: ${'$'}data")
                override fun getProgress(): Int = 100
            }

            class SimpleValidator : Validator {
                override fun validate(data: String): Boolean = data.isNotBlank()
            }

            // Класс делегирует реализацию обоим интерфейсам
            class FileProcessor(
                uploader: Uploader,
                validator: Validator
            ) : Uploader by uploader, Validator by validator {
                
                // Можно переопределить некоторые методы
                override fun upload(data: String) {
                    if (validate(data)) {
                        println("Валидация пройдена, загружаем...")
                        uploader.upload(data)
                    } else {
                        println("Ошибка валидации")
                    }
                }
            }
            ```

            ## 📦 Делегированные свойства (Delegated Properties)

            Самая мощная фича делегирования — возможность делегировать **геттеры и сеттеры** свойств.

            ### 🔍 Стандартные делегаты из стандартной библиотеки

            #### 1. `lazy` — ленивая инициализация

            ```kotlin
            val heavyObject: HeavyClass by lazy {
                println("🔨 Создаём тяжёлый объект...")
                HeavyClass() // Создастся только при первом обращении
            }

            // Использование
            println(heavyObject) // Создаётся объект
            println(heavyObject) // Используется уже созданный
            ```

            **Варианты `lazy`**:
            - `lazy(LazyThreadSafetyMode.NONE)` — без синхронизации (для однопоточных сценариев)
            - `lazy(LazyThreadSafetyMode.SYNCHRONIZED)` — потокобезопасный (по умолчанию)
            - `lazy(LazyThreadSafetyMode.PUBLICATION)` — для конкурентного доступа

            #### 2. `observable` — наблюдение за изменениями

            ```kotlin
            import kotlin.properties.Delegates

            class UserViewModel {
                var userName: String by Delegates.observable("Гость") { prop, old, new ->
                    println("Имя изменено: ${'$'}old -> ${'$'}new")
                    // Здесь можно обновить UI, сохранить в БД и т.д.
                }
            }

            val vm = UserViewModel()
            vm.userName = "Алексей" // Имя изменено: Гость -> Алексей
            vm.userName = "Мария"   // Имя изменено: Алексей -> Мария
            ```

            #### 3. `vetoable` — изменение с проверкой

            ```kotlin
            var age: Int by Delegates.vetoable(0) { prop, old, new ->
                when {
                    new < 0 -> {
                        println("❌ Возраст не может быть отрицательным")
                        false // запрещаем изменение
                    }
                    new > 150 -> {
                        println("❌ Слишком большой возраст")
                        false
                    }
                    else -> {
                        println("✅ Возраст обновлён: ${'$'}old -> ${'$'}new")
                        true // разрешаем изменение
                    }
                }
            }

            age = 25   // ✅ Возраст обновлён: 0 -> 25
            age = -5   // ❌ Возраст не может быть отрицательным
            println(age) // 25 (значение не изменилось)
            ```

            #### 4. `notNull` — для свойств, инициализируемых позже

            ```kotlin
            class LateInitExample {
                var database: Database by Delegates.notNull()
                // Аналог lateinit, но работает с примитивами
                
                fun initDatabase() {
                    database = Database.connect("localhost")
                }
                
                fun query() {
                    // database будет доступен только после initDatabase()
                    database.execute("SELECT * FROM users")
                }
            }
            ```

            ## 🛠️ Создание собственных делегатов

            ### Базовая структура делегата

            ```kotlin
            class MyDelegate<T> {
                // Для val (только чтение)
                operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
                    // thisRef — ссылка на объект, содержащий свойство
                    // property — метаданные свойства (имя, тип и т.д.)
                    return getValueFromSomewhere()
                }
                
                // Для var (чтение и запись)
                operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
                    saveValueSomewhere(value)
                }
            }
            ```

            ### 📝 Пример: делегат с валидацией

            ```kotlin
            class ValidatedDelegate<T>(
                private val initialValue: T,
                private val validator: (T) -> Boolean
            ) {
                private var value = initialValue
                
                operator fun getValue(thisRef: Any?, property: KProperty<*>): T = value
                
                operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: T) {
                    if (validator(newValue)) {
                        println("✅ ${'$'}{property.name} = ${'$'}newValue (принято)")
                        value = newValue
                    } else {
                        println("❌ ${'$'}{property.name} = ${'$'}newValue (отклонено)")
                    }
                }
            }

            class UserProfile {
                var email: String by ValidatedDelegate("") { it.contains('@') }
                var age: Int by ValidatedDelegate(0) { it in 0..150 }
            }

            fun main() {
                val profile = UserProfile()
                profile.email = "test@example.com"  // ✅ email = test@example.com (принято)
                profile.email = "invalid"           // ❌ email = invalid (отклонено)
                profile.age = 25                     // ✅ age = 25 (принято)
                profile.age = 200                     // ❌ age = 200 (отклонено)
            }
            ```

            ### 🔐 Делегат с кэшированием

            ```kotlin
            class CacheDelegate<T : Any>(
                private val loader: () -> T
            ) {
                private var cachedValue: T? = null
                
                operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
                    return synchronized(this) {
                        if (cachedValue == null) {
                            println("⚡ Загружаем ${'$'}{property.name}...")
                            cachedValue = loader()
                        } else {
                            println("📦 Используем кэш для ${'$'}{property.name}")
                        }
                        cachedValue!!
                    }
                }
            }

            class DataRepository {
                val users: List<User> by CacheDelegate {
                    api.fetchUsers()  // Дорогой запрос
                }
                
                val settings: Settings by CacheDelegate {
                    database.loadSettings()
                }
            }
            ```

            ### 📊 Делегат с Map (для динамических конфигураций)

            ```kotlin
            class AppConfig(private val map: Map<String, Any?>) {
                val host: String by map
                val port: Int by map
                val debugMode: Boolean by map
                val timeout: Long by map
            }

            val config = AppConfig(mapOf(
                "host" to "localhost",
                "port" to 8080,
                "debugMode" to true,
                "timeout" to 30000L
            ))

            println(config.host)     // localhost
            println(config.port)     // 8080
            println(config.debugMode) // true
            ```

            ### 📝 MutableMap делегат (для изменения)

            ```kotlin
            class DynamicConfig(private val map: MutableMap<String, Any?>) {
                var host: String by map
                var port: Int by map
                var debugMode: Boolean by map
            }

            val mutableMap = mutableMapOf(
                "host" to "localhost",
                "port" to 8080,
                "debugMode" to true
            )
            val config = DynamicConfig(mutableMap)
            
            config.host = "example.com"
            println(mutableMap["host"]) // example.com (изменилось в оригинальной map!)
            ```

            ## 🎨 Продвинутые примеры

            ### 🔄 Делегат с историей изменений

            ```kotlin
            class HistoryDelegate<T> {
                private val history = mutableListOf<T>()
                private var currentValue: T? = null
                
                operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
                    return currentValue ?: throw IllegalStateException("Value not set")
                }
                
                operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
                    if (currentValue != null) {
                        history.add(currentValue!!)
                        println("📝 ${'$'}{property.name}: ${'$'}{currentValue} -> ${'$'}value")
                    } else {
                        println("✨ ${'$'}{property.name} = ${'$'}value")
                    }
                    currentValue = value
                }
                
                fun undo(): T? {
                    return if (history.isNotEmpty()) {
                        currentValue = history.removeAt(history.lastIndex)
                        currentValue
                    } else null
                }
                
                fun history(): List<T> = history.toList()
            }

            class Document {
                var content: String by HistoryDelegate()
            }

            fun main() {
                val doc = Document()
                doc.content = "Версия 1"   // ✨ content = Версия 1
                doc.content = "Версия 2"   // 📝 content: Версия 1 -> Версия 2
                doc.content = "Версия 3"   // 📝 content: Версия 2 -> Версия 3
                // doc.undo() можно добавить метод для отката
            }
            ```

            ### 📏 Делегат с ограничениями (Range)

            ```kotlin
            import kotlin.reflect.KProperty

            class BoundedDelegate<T : Comparable<T>>(
                private val initialValue: T,
                private val range: ClosedRange<T>
            ) {
                private var value = initialValue
                
                operator fun getValue(thisRef: Any?, property: KProperty<*>): T = value
                
                operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: T) {
                    value = when {
                        newValue < range.start -> {
                            println("⚠️ ${'$'}{property.name} = ${'$'}newValue меньше минимума, установлено ${'$'}{range.start}")
                            range.start
                        }
                        newValue > range.endInclusive -> {
                            println("⚠️ ${'$'}{property.name} = ${'$'}newValue больше максимума, установлено ${'$'}{range.endInclusive}")
                            range.endInclusive
                        }
                        else -> newValue
                    }
                }
            }

            class GameCharacter {
                var health: Int by BoundedDelegate(100, 0..100)
                var level: Int by BoundedDelegate(1, 1..99)
                var name: String by Delegates.observable("Hero") { _, old, new ->
                    println("Персонаж переименован: ${'$'}old -> ${'$'}new")
                }
            }

            fun main() {
                val hero = GameCharacter()
                hero.health = 150  // ⚠️ health = 150 больше максимума, установлено 100
                hero.health = -10   // ⚠️ health = -10 меньше минимума, установлено 0
                hero.name = "Архимаг" // Персонаж переименован: Hero -> Архимаг
            }
            ```

            ### 🎭 Делегирование в Android (SharedPreferences)

            ```kotlin
            class PrefDelegate<T>(
                private val prefs: SharedPreferences,
                private val key: String,
                private val defaultValue: T
            ) {
                @Suppress("UNCHECKED_CAST")
                operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
                    return when (defaultValue) {
                        is String -> prefs.getString(key, defaultValue) as T
                        is Int -> prefs.getInt(key, defaultValue) as T
                        is Boolean -> prefs.getBoolean(key, defaultValue) as T
                        is Float -> prefs.getFloat(key, defaultValue) as T
                        is Long -> prefs.getLong(key, defaultValue) as T
                        else -> throw IllegalArgumentException("Unsupported type")
                    }
                }
                
                operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
                    with(prefs.edit()) {
                        when (value) {
                            is String -> putString(key, value)
                            is Int -> putInt(key, value)
                            is Boolean -> putBoolean(key, value)
                            is Float -> putFloat(key, value)
                            is Long -> putLong(key, value)
                            else -> throw IllegalArgumentException("Unsupported type")
                        }.apply()
                    }
                }
            }

            class Settings(context: Context) {
                private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                
                var username: String by PrefDelegate(prefs, "username", "Гость")
                var isDarkMode: Boolean by PrefDelegate(prefs, "dark_mode", false)
                var fontSize: Float by PrefDelegate(prefs, "font_size", 14f)
            }
            ```

            ## 📊 Сравнение с наследованием

            | Аспект | Наследование | Делегирование |
            |--------|--------------|---------------|
            | Связь | Жёсткая (is-a) | Гибкая (has-a) |
            | Повторное использование | Ограниченное | Широкое |
            | Тестирование | Сложное | Лёгкое (моки) |
            | Гибкость | Низкая | Высокая |
            | Множественное "наследование" | ❌ | ✅ |

            ## 💡 Лучшие практики

            1. **Используйте делегирование вместо наследования**, когда возможны оба варианта
            2. **Для повторяющейся логики свойств создавайте свои делегаты**
            3. **Документируйте поведение делегатов** (особенно с побочными эффектами)
            4. **Предпочитайте стандартные делегаты** (`lazy`, `observable`) самодельным
            5. **Будьте осторожны с `vetoable`** — он вызывается при каждой записи
            6. **Для многопоточности используйте потокобезопасные делегаты**

            ## 🎯 Резюме

            ✅ **Делегирование реализации** — композиция вместо наследования через `by`
            ✅ **Делегированные свойства** — переиспользуемая логика для геттеров/сеттеров
            ✅ **Стандартные делегаты** — `lazy`, `observable`, `vetoable`, `notNull`
            ✅ **Map делегаты** — удобная работа с конфигурациями
            ✅ **Собственные делегаты** — для специфической логики (валидация, кэш, логирование)

            Делегирование — это не просто синтаксический сахар, а мощный инструмент для создания чистой, тестируемой и гибкой архитектуры!
        """.trimIndent(),
        codeExample = """
            import kotlin.properties.Delegates
            import kotlin.reflect.KProperty
            import kotlin.random.Random
            import kotlin.system.measureTimeMillis

            fun main() {
                println("🔄 Делегирование в Kotlin - Полный пример")
                println("=".repeat(60))
                
                // 1️⃣ Делегирование реализации
                println("\n1️⃣ ДЕЛЕГИРОВАНИЕ РЕАЛИЗАЦИИ")
                println("-".repeat(40))
                
                val fileLogger = FileLogger("app.log")
                val consoleLogger = ConsoleLogger()
                
                val fileProcessor = DataProcessor(fileLogger)
                val consoleProcessor = DataProcessor(consoleLogger)
                
                fileProcessor.process("Важные данные")
                consoleProcessor.process("Отладочная информация")
                
                // 2️⃣ Множественное делегирование
                println("\n2️⃣ МНОЖЕСТВЕННОЕ ДЕЛЕГИРОВАНИЕ")
                println("-".repeat(40))
                
                val uploader = CloudUploader()
                val validator = DataValidator()
                val backupService = BackupService(uploader, validator)
                
                backupService.backup("user_data.json")
                println("Прогресс бэкапа: ${'$'}{backupService.getProgress()}%")
                
                // 3️⃣ LAZY делегат
                println("\n3️⃣ LAZY ДЕЛЕГАТ")
                println("-".repeat(40))
                
                val server = Server()
                
                repeat(3) { i ->
                    println("Запрос ${'$'}i: ${'$'}{server.config}")
                }
                
                // 4️⃣ OBSERVABLE делегат
                println("\n4️⃣ OBSERVABLE ДЕЛЕГАТ")
                println("-".repeat(40))
                
                val settings = AppSettings()
                settings.theme = "Тёмная"
                settings.theme = "Светлая"
                settings.fontSize = 16
                
                // 5️⃣ VETOABLE делегат
                println("\n5️⃣ VETOABLE ДЕЛЕГАТ")
                println("-".repeat(40))
                
                val account = BankAccount()
                account.balance = 1000.0
                account.balance = 500.0
                account.balance = -100.0  // Должно отклонить
                println("Итоговый баланс: ${'$'}{account.balance}")
                
                // 6️⃣ СОБСТВЕННЫЙ ДЕЛЕГАТ: Валидация
                println("\n6️⃣ СОБСТВЕННЫЙ ДЕЛЕГАТ: Валидация")
                println("-".repeat(40))
                
                val user = User()
                user.email = "test@example.com"
                user.email = "invalid-email"
                user.password = "12345"
                user.password = "StrongP@ss123"
                
                println("Email: ${'$'}{user.email}")
                println("Пароль: ${'$'}{user.password}")
                
                // 7️⃣ СОБСТВЕННЫЙ ДЕЛЕГАТ: Кэширование
                println("\n7️⃣ СОБСТВЕННЫЙ ДЕЛЕГАТ: Кэширование")
                println("-".repeat(40))
                
                val repository = UserRepository()
                
                repeat(4) { i ->
                    val time = measureTimeMillis {
                        val users = repository.users
                        if (i == 0) println("Получено ${'$'}{users.size} пользователей")
                    }
                    println("  Попытка ${'$'}i: ${'$'}time мс")
                }
                
                // 8️⃣ MAP ДЕЛЕГАТ
                println("\n8️⃣ MAP ДЕЛЕГАТ")
                println("-".repeat(40))
                
                val config = ServerConfig(mapOf(
                    "host" to "api.example.com",
                    "port" to 443,
                    "useSSL" to true,
                    "timeoutMs" to 30000L
                ))
                
                println("Подключение к ${'$'}{config.host}:${'$'}{config.port}")
                println("SSL: ${'$'}{config.useSSL}")
                println("Таймаут: ${'$'}{config.timeoutMs} мс")
                
                // 9️⃣ СОБСТВЕННЫЙ ДЕЛЕГАТ: Делегат с историей
                println("\n9️⃣ СОБСТВЕННЫЙ ДЕЛЕГАТ: История изменений")
                println("-".repeat(40))
                
                val doc = EditableDocument()
                doc.content = "Первая версия"
                doc.content = "Вторая версия"
                doc.content = "Третья версия"
                
                doc.undo()
                println("После отмены: ${'$'}{doc.content}")
                doc.undo()
                println("После ещё одной отмены: ${'$'}{doc.content}")
                
                // 🔟 NOTNULL делегат
                println("\n🔟 NOTNULL ДЕЛЕГАТ")
                println("-".repeat(40))
                
                val database = DatabaseConnection()
                // database.query() // Ошибка! Не инициализировано
                
                database.initialize("jdbc:mysql://localhost:3306/mydb")
                database.query("SELECT * FROM users") // Работает!
                
                println("\n" + "=".repeat(60))
                println("✅ Все примеры делегирования выполнены!")
            }

            // ==================== КЛАССЫ ДЛЯ ПРИМЕРОВ ====================

            // 1️⃣ Делегирование реализации
            interface Logger {
                fun log(message: String)
                fun getLogLevel(): String = "INFO"
            }

            class FileLogger(private val filename: String) : Logger {
                override fun log(message: String) {
                    println("📁 [Файл ${'$'}filename] ${'$'}message")
                }
            }

            class ConsoleLogger : Logger {
                override fun log(message: String) {
                    println("🖥️ [Консоль] ${'$'}message")
                }
            }

            class DataProcessor(logger: Logger) : Logger by logger {
                fun process(data: String) {
                    log("Обработка данных: ${'$'}data")
                }
            }

            // 2️⃣ Множественное делегирование
            interface Uploader {
                fun upload(data: String): Boolean
                fun getProgress(): Int
            }

            interface Validator {
                fun validate(data: String): Boolean
            }

            class CloudUploader : Uploader {
                override fun upload(data: String): Boolean {
                    println("☁️ Загрузка ${'$'}data в облако...")
                    return true
                }
                override fun getProgress(): Int = Random.nextInt(0, 100)
            }

            class DataValidator : Validator {
                override fun validate(data: String): Boolean = data.isNotBlank() && data.length > 5
            }

            class BackupService(
                uploader: Uploader,
                validator: Validator
            ) : Uploader by uploader, Validator by validator {
                
                fun backup(data: String) {
                    if (validate(data)) {
                        println("✅ Данные валидны")
                        upload(data)
                    } else {
                        println("❌ Данные не прошли валидацию")
                    }
                }
            }

            // 3️⃣ Lazy делегат
            class Server {
                val config: ServerConfigLazy by lazy {
                    println("⚙️ Загружаем конфигурацию сервера...")
                    Thread.sleep(1000) // Имитация загрузки
                    ServerConfigLazy("localhost", 8080)
                }
            }

            data class ServerConfigLazy(val host: String, val port: Int)

            // 4️⃣ Observable делегат
            class AppSettings {
                var theme: String by Delegates.observable("Системная") { prop, old, new ->
                    println("🎨 Тема изменена: ${'$'}old -> ${'$'}new")
                }
                
                var fontSize: Int by Delegates.observable(14) { prop, old, new ->
                    println("🔤 Размер шрифта: ${'$'}old -> ${'$'}new pt")
                }
            }

            // 5️⃣ Vetoable делегат
            class BankAccount {
                var balance: Double by Delegates.vetoable(0.0) { prop, old, new ->
                    when {
                        new < 0 -> {
                            println("⚠️ Попытка установить отрицательный баланс")
                            false
                        }
                        new > 1000000 -> {
                            println("⚠️ Сумма превышает лимит")
                            false
                        }
                        else -> {
                            println("💰 Баланс изменён: ${'$'}old -> ${'$'}new")
                            true
                        }
                    }
                }
            }

            // 6️⃣ Собственный делегат с валидацией
            class ValidatedString(
                private val validator: (String) -> Boolean,
                private val errorMessage: String
            ) {
                private var value: String = ""
                
                operator fun getValue(thisRef: Any?, property: KProperty<*>): String = value
                
                operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: String) {
                    if (validator(newValue)) {
                        value = newValue
                        println("✅ ${'$'}{property.name} установлен")
                    } else {
                        println("❌ ${'$'}{property.name}: ${'$'}errorMessage")
                    }
                }
            }

            class User {
                var email: String by ValidatedString(
                    validator = { it.contains('@') && it.contains('.') },
                    errorMessage = "Email должен содержать @ и ."
                )
                
                var password: String by ValidatedString(
                    validator = { it.length >= 8 && it.any { it.isDigit() } && it.any { it.isUpperCase() } },
                    errorMessage = "Пароль: мин. 8 символов, цифра, заглавная буква"
                )
            }

            // 7️⃣ Собственный делегат с кэшированием
            class CacheDelegate<T : Any>(private val loader: () -> T) {
                private var cachedValue: T? = null
                private var loadCount = 0
                
                operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
                    if (cachedValue == null) {
                        loadCount++
                        println("📡 Загрузка ${'$'}{property.name} (попытка ${'$'}loadCount)...")
                        cachedValue = loader()
                    } else {
                        println("📦 Кэш для ${'$'}{property.name}")
                    }
                    return cachedValue!!
                }
            }

            class UserRepository {
                val users: List<String> by CacheDelegate {
                    // Имитация загрузки из сети
                    Thread.sleep(1000)
                    List(10) { "User${'$'}{it + 1}" }
                }
            }

            // 8️⃣ Map делегат
            class ServerConfig(map: Map<String, Any?>) {
                val host: String by map
                val port: Int by map
                val useSSL: Boolean by map
                val timeoutMs: Long by map
            }

            // 9️⃣ Собственный делегат с историей
            class HistoryDelegate<T> {
                private val history = mutableListOf<T>()
                private var currentValue: T? = null
                
                operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
                    return currentValue ?: throw IllegalStateException("Значение не установлено")
                }
                
                operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
                    if (currentValue != null) {
                        history.add(currentValue!!)
                        println("📝 История: ${'$'}{currentValue} -> ${'$'}value")
                    } else {
                        println("✨ Первое значение: ${'$'}value")
                    }
                    currentValue = value
                }
                
                fun undo(): Boolean {
                    return if (history.isNotEmpty()) {
                        currentValue = history.removeAt(history.lastIndex)
                        true
                    } else false
                }
            }

            class EditableDocument {
                var content: String by HistoryDelegate()
                
                fun undo() {
                    if ((this::content.delegate as HistoryDelegate<String>).undo()) {
                        println("↩️ Отмена: текущее содержание = '${'$'}content'")
                    }
                }
            }

            // 🔟 NotNull делегат
            class DatabaseConnection {
                var connection: String by Delegates.notNull()
                
                fun initialize(url: String) {
                    connection = "Подключено к ${'$'}url"
                }
                
                fun query(sql: String) {
                    println("🔍 Выполнение запроса: ${'$'}sql на ${'$'}connection")
                }
            }
        """.trimIndent()
    )
}