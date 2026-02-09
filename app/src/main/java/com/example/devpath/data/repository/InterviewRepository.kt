package com.example.devpath.data.repository

import com.example.devpath.domain.models.InterviewQuestion

object InterviewRepository {
    fun getInterviewQuestions(): List<InterviewQuestion> {
        return listOf(
            InterviewQuestion(
                id = "iq1",
                question = "Что такое null safety в Kotlin и как он работает?",
                answer = """
                    Null safety — одна из ключевых особенностей Kotlin, которая помогает избежать NullPointerException.
                    
                    В Kotlin типы по умолчанию не могут содержать null. Если переменная может быть null, нужно явно указать это с помощью знака вопроса:
                    
                    ```kotlin
                    val name: String = "Kotlin"     // не может быть null
                    val nullableName: String? = null // может быть null
                    ```
                    
                    Для работы с nullable типами используются:
                    - Оператор безопасного вызова `?.`
                    - Оператор Elvis `?:`
                    - Утверждение not-null `!!`
                """.trimIndent(),
                category = "kotlin",
                difficulty = "intermediate"
            ),
            InterviewQuestion(
                id = "iq2",
                question = "В чем разница между val и var в Kotlin?",
                answer = """
                    - `val` (value) — объявляет неизменяемую переменную (immutable). После присвоения значения его нельзя изменить.
                    - `var` (variable) — объявляет изменяемую переменную (mutable). Значение можно изменять.
                    
                    Пример:
                    ```kotlin
                    val pi = 3.14 // нельзя изменить
                    var counter = 0 // можно изменить
                    counter = 1 // OK
                    // pi = 3.14159 // Ошибка компиляции!
                    ```
                    
                    Используйте `val` по умолчанию, а `var` только когда действительно нужно изменять значение.
                """.trimIndent(),
                category = "kotlin",
                difficulty = "beginner"
            ),
            InterviewQuestion(
                id = "iq3",
                question = "Что такое Activity и Fragment в Android?",
                answer = """
                    **Activity** — это отдельный экран приложения с пользовательским интерфейсом. Каждое приложение обычно имеет несколько Activity.
                    
                    **Fragment** — это модульный компонент UI, который можно использовать внутри Activity. Фрагменты позволяют создавать гибкие и адаптивные интерфейсы.
                    
                    Основные различия:
                    - Activity представляет полный экран
                    - Fragment представляет часть экрана
                    - Fragment зависит от Activity
                    - Несколько Fragment'ов могут находиться в одной Activity
                    
                    В современной разработке рекомендуется использовать **Jetpack Compose** вместо Fragment'ов.
                """.trimIndent(),
                category = "android",
                difficulty = "intermediate"
            ),
            InterviewQuestion(
                id = "iq4",
                question = "Что такое Coroutines в Kotlin и чем они отличаются от потоков?",
                answer = """
                    **Coroutines (Корутины)** — это легковесные потоки для асинхронного программирования в Kotlin.
                    
                    Отличия от обычных потоков (Threads):
                    1. **Легковесность** — можно запускать тысячи корутин, в то время как потоки потребляют много памяти
                    2. **Структурированная отмена** — корутины могут быть легко отменены
                    3. **Не блокируют поток** — приостанавливаются (suspend) вместо блокировки
                    4. **Простота использования** — синхронный стиль кода для асинхронных операций
                    
                    Пример:
                    ```kotlin
                    suspend fun fetchData(): String {
                        return withContext(Dispatchers.IO) {
                            // Асинхронная операция
                            "Данные"
                        }
                    }
                    ```
                """.trimIndent(),
                category = "kotlin",
                difficulty = "advanced"
            ),
            InterviewQuestion(
                id = "iq5",
                question = "Объясните жизненный цикл Activity в Android",
                answer = """
                    Жизненный цикл Activity состоит из следующих состояний:
                    
                    1. **onCreate()** — вызывается при создании Activity
                    2. **onStart()** — Activity становится видимым
                    3. **onResume()** — Activity получает фокус и готово к взаимодействию
                    4. **onPause()** — другое Activity получает фокус
                    5. **onStop()** — Activity становится невидимым
                    6. **onDestroy()** — перед уничтожением Activity
                    7. **onRestart()** — перед повторным запуском остановленной Activity
                    
                    Важно сохранять состояние в onPause()/onStop() и восстанавливать в onCreate()/onResume().
                """.trimIndent(),
                category = "android",
                difficulty = "intermediate"
            ),
            InterviewQuestion(
                id = "iq6",
                question = "Что такое ViewModel в Android Architecture Components?",
                answer = """
                    **ViewModel** — это компонент архитектуры, который хранит и управляет UI-данными способом, учитывающим жизненный цикл.
                    
                    Преимущества ViewModel:
                    - **Выживает при повороте экрана** — данные не теряются
                    - **Отделяет бизнес-логику от UI** — следуя принципам чистой архитектуры
                    - **Предоставляет данные для UI** через LiveData или StateFlow
                    
                    ViewModel не должен содержать ссылки на View, Context или Activity, чтобы избежать утечек памяти.
                """.trimIndent(),
                category = "android",
                difficulty = "intermediate"
            ),
            InterviewQuestion(
                id = "iq7",
                question = "Что такое Dependency Injection (DI) и зачем он нужен?",
                answer = """
                    **Dependency Injection (Внедрение зависимостей)** — это шаблон проектирования, при котором зависимости передаются объекту извне, а не создаются внутри него.
                    
                    Преимущества DI:
                    1. **Уменьшение связанности** — классы не зависят от конкретных реализаций
                    2. **Упрощение тестирования** — можно легко подменять зависимости моками
                    3. **Повторное использование кода** — компоненты становятся более универсальными
                    4. **Упрощение конфигурации** — зависимости можно настраивать извне
                    
                    В Android популярны DI-библиотеки: Dagger, Hilt, Koin.
                """.trimIndent(),
                category = "android",
                difficulty = "advanced"
            ),
            InterviewQuestion(
                id = "iq8",
                question = "В чем разница между List, Set и Map в Kotlin?",
                answer = """
                    **List** — упорядоченная коллекция с доступом по индексу. Может содержать дубликаты.
                    ```kotlin
                    val list = listOf(1, 2, 3, 3) // [1, 2, 3, 3]
                    ```
                    
                    **Set** — неупорядоченная коллекция уникальных элементов.
                    ```kotlin
                    val set = setOf(1, 2, 3, 3) // [1, 2, 3]
                    ```
                    
                    **Map** — коллекция пар ключ-значение. Ключи уникальны.
                    ```kotlin
                    val map = mapOf("a" to 1, "b" to 2)
                    ```
                    
                    Для изменяемых версий используйте: mutableListOf(), mutableSetOf(), mutableMapOf().
                """.trimIndent(),
                category = "kotlin",
                difficulty = "beginner"
            ),
            InterviewQuestion(
                id = "iq9",
                question = "Что такое LiveData в Android и зачем он нужен?",
                answer = """
                    **LiveData** — это наблюдаемый холдер данных, который учитывает жизненный цикл компонентов Android.
                    
                    Особенности LiveData:
                    - **Учет жизненного цикла** — уведомляет только активные наблюдатели
                    - **Нет утечек памяти** — автоматически очищает ссылки
                    - **Автоматическое обновление UI** — при изменении данных
                    - **Кэширование данных** — сохраняет последнее значение
                    
                    LiveData обычно используется в сочетании с ViewModel для обновления UI при изменении данных.
                """.trimIndent(),
                category = "android",
                difficulty = "intermediate"
            ),
            InterviewQuestion(
                id = "iq10",
                question = "Что такое sealed class в Kotlin?",
                answer = """
                    **Sealed class (Запечатанный класс)** — это специальный класс, который ограничивает иерархию наследования.
                    
                    Особенности:
                    1. **Ограниченное наследование** — все подклассы должны быть объявлены в том же файле
                    2. **Используется с when** — компилятор проверяет exhaustiveness
                    3. **Представляют ограниченные иерархии** — как улучшенные enum
                    
                    Пример:
                    ```kotlin
                    sealed class Result {
                        data class Success(val data: String) : Result()
                        data class Error(val message: String) : Result()
                    }
                    
                    fun handleResult(result: Result) {
                        when (result) {
                            is Result.Success -> println(result.data)
                            is Result.Error -> println(result.message)
                        }
                    }
                    ```
                """.trimIndent(),
                category = "kotlin",
                difficulty = "intermediate"
            ),
            InterviewQuestion(
                id = "iq11",
                question = "Что такое RecyclerView и как он работает?",
                answer = """
                    **RecyclerView** — это гибкий и эффективный виджет для отображения больших наборов данных в виде прокручиваемых списков или сеток.
                    
                    Ключевые компоненты:
                    1. **Adapter** — преобразует данные в View элементы
                    2. **ViewHolder** — хранит ссылки на View для переиспользования
                    3. **LayoutManager** — управляет расположением элементов
                    4. **ItemDecoration** — добавляет отступы и разделители
                    
                    Преимущества перед ListView:
                    - **Переиспользование View** (Recycling) — экономит память
                    - **Гибкость LayoutManager** — линейный, сеточный, каскадный
                    - **Анимации по умолчанию** — вставка, удаление, перемещение
                """.trimIndent(),
                category = "android",
                difficulty = "intermediate"
            ),
            InterviewQuestion(
                id = "iq12",
                question = "Что такое Scope функции в Kotlin (let, run, with, apply, also)?",
                answer = """
                    **Scope функции** — это функции-расширения, которые позволяют выполнить блок кода в контексте объекта.
                    
                    1. **let** — выполняет блок кода и возвращает результат
                    ```kotlin
                    val result = nullable?.let { it.length }
                    ```
                    
                    2. **run** — как let, но с доступом к this
                    ```kotlin
                    val result = "text".run { length }
                    ```
                    
                    3. **with** — не extension, но работает похоже на run
                    ```kotlin
                    with(view) {
                        text = "Hello"
                        visibility = View.VISIBLE
                    }
                    ```
                    
                    4. **apply** — возвращает объект-контекст
                    ```kotlin
                    val view = TextView().apply {
                        text = "Hello"
                        textSize = 16f
                    }
                    ```
                    
                    5. **also** — как apply, но с доступом к it
                    ```kotlin
                    val list = mutableListOf<Int>().also {
                        it.add(1)
                        it.add(2)
                    }
                    ```
                """.trimIndent(),
                category = "kotlin",
                difficulty = "intermediate"
            ),
            InterviewQuestion(
                id = "iq13",
                question = "Что такое SharedPreferences и для чего он используется?",
                answer = """
                    **SharedPreferences** — это API Android для хранения небольших объемов данных в формате ключ-значение.
                    
                    Используется для:
                    - Сохранения настроек пользователя
                    - Хранения состояния приложения
                    - Кэширования простых данных
                    
                    Ограничения:
                    - Только примитивные типы данных
                    - Не подходит для больших объемов данных
                    - Не потокобезопасный
                    
                    Пример:
                    ```kotlin
                    val prefs = getSharedPreferences("app", Context.MODE_PRIVATE)
                    prefs.edit().putString("username", "user").apply()
                    val username = prefs.getString("username", null)
                    ```
                    
                    Для сложных данных используйте Room Database или DataStore.
                """.trimIndent(),
                category = "android",
                difficulty = "beginner"
            ),
            InterviewQuestion(
                id = "iq14",
                question = "Что такое Extension функции в Kotlin?",
                answer = """
                    **Extension функции** — это функции, которые позволяют добавлять новые функции к существующим классам без наследования.
                    
                    Пример:
                    ```kotlin
                    // Добавляем функцию к String
                    fun String.addExclamation(): String {
                        return "$this!"
                    }
                    
                    // Использование
                    val text = "Hello".addExclamation() // "Hello!"
                    ```
                    
                    Особенности:
                    - Не изменяют оригинальный класс
                    - Работают как статические методы
                    - Можно создавать для любых классов, включая сторонние
                    - Имеют доступ только к публичным членам класса
                """.trimIndent(),
                category = "kotlin",
                difficulty = "intermediate"
            ),
            InterviewQuestion(
                id = "iq15",
                question = "Что такое Room Database и как она работает?",
                answer = """
                    **Room** — это библиотека persistence из Android Jetpack, которая предоставляет абстракцию над SQLite.
                    
                    Компоненты Room:
                    1. **Entity** — класс, представляющий таблицу в базе данных
                    2. **DAO (Data Access Object)** — интерфейс для доступа к данным
                    3. **Database** — абстрактный класс, расширяющий RoomDatabase
                    
                    Пример Entity:
                    ```kotlin
                    @Entity
                    data class User(
                        @PrimaryKey val id: Int,
                        @ColumnInfo(name = "name") val name: String
                    )
                    ```
                    
                    Преимущества Room:
                    - Проверка SQL запросов на этапе компиляции
                    - Автоматическое преобразование объектов
                    - Поддержка LiveData и RxJava
                    - Минимальный boilerplate код
                """.trimIndent(),
                category = "android",
                difficulty = "advanced"
            ),
            InterviewQuestion(
                id = "iq16",
                question = "Что такое Flow в Kotlin и чем отличается от LiveData?",
                answer = """
                    **Flow** — это асинхронный поток данных в Kotlin, часть корутин.
                    
                    Отличия от LiveData:
                    1. **Не привязан к Android** — можно использовать вне Android
                    2. **Богатые операторы** — map, filter, transform и другие
                    3. **Поддержка backpressure** — контроль скорости данных
                    4. **Множественные коллекторы** — в отличие от LiveData
                    5. **Не учитывает жизненный цикл** по умолчанию
                    
                    Для работы с жизненным циклом в Android используется `lifecycleScope` или `repeatOnLifecycle`.
                    
                    Пример:
                    ```kotlin
                    fun getData(): Flow<String> = flow {
                        emit("Data 1")
                        delay(1000)
                        emit("Data 2")
                    }
                    ```
                """.trimIndent(),
                category = "kotlin",
                difficulty = "advanced"
            ),
            InterviewQuestion(
                id = "iq17",
                question = "Что такое Intent в Android и какие виды Intent бывают?",
                answer = """
                    **Intent** — это объект сообщения, который используется для запроса действия от другого компонента приложения.
                    
                    Виды Intent:
                    
                    1. **Explicit Intent (Явный)** — указывает конкретный компонент для запуска
                    ```kotlin
                    val intent = Intent(this, DetailActivity::class.java)
                    startActivity(intent)
                    ```
                    
                    2. **Implicit Intent (Неявный)** — указывает действие, система выбирает подходящий компонент
                    ```kotlin
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com"))
                    startActivity(intent)
                    ```
                    
                    Intent также используется для:
                    - Запуска Activity
                    - Запуска Service
                    - Отправки Broadcast
                    - Передачи данных между компонентами
                """.trimIndent(),
                category = "android",
                difficulty = "beginner"
            ),
            InterviewQuestion(
                id = "iq18",
                question = "Что такое data class в Kotlin?",
                answer = """
                    **Data class** — это специальный класс в Kotlin, предназначенный для хранения данных.
                    
                    Компилятор автоматически генерирует:
                    1. **equals()/hashCode()** — для сравнения объектов
                    2. **toString()** — читаемое строковое представление
                    3. **componentN()** — функции для деструктуризации
                    4. **copy()** — функция для копирования с изменением некоторых свойств
                    
                    Пример:
                    ```kotlin
                    data class User(val name: String, val age: Int)
                    
                    // Автоматически генерируются:
                    // equals(), hashCode(), toString(), component1(), component2(), copy()
                    ```
                    
                    Требования:
                    - Основной конструктор должен иметь хотя бы один параметр
                    - Все параметры конструктора должны быть val или var
                    - Не могут быть abstract, open, sealed или inner
                """.trimIndent(),
                category = "kotlin",
                difficulty = "beginner"
            ),
            InterviewQuestion(
                id = "iq19",
                question = "Что такое Service в Android и какие виды Service бывают?",
                answer = """
                    **Service** — это компонент приложения, который выполняет операции в фоновом режиме без пользовательского интерфейса.
                    
                    Виды Service:
                    
                    1. **Foreground Service** — работает на переднем плане с обязательным уведомлением
                    ```kotlin
                    // Для длительных операций (музыка, GPS)
                    ```
                    
                    2. **Background Service** — работает в фоне (ограничения в новых версиях Android)
                    
                    3. **Bound Service** — привязывается к компонентам и работает, пока они активны
                    
                    4. **IntentService** (устарел) — автоматически останавливается после выполнения задачи
                    
                    Вместо Service в новых приложениях рекомендуется использовать:
                    - **WorkManager** для отложенных фоновых задач
                    - **Foreground Service** с уведомлением для критически важных задач
                """.trimIndent(),
                category = "android",
                difficulty = "advanced"
            ),
            InterviewQuestion(
                id = "iq20",
                question = "Что такое suspend функции в Kotlin?",
                answer = """
                    **Suspend функции** — это функции, которые могут быть приостановлены и возобновлены позже без блокировки потока.
                    
                    Особенности:
                    1. **Могут быть приостановлены** — не блокируют поток выполнения
                    2. **Работают только внутри корутин** или других suspend функций
                    3. **Могут вызывать другие suspend функции**
                    4. **Используются для асинхронных операций**
                    
                    Пример:
                    ```kotlin
                    suspend fun fetchUserData(): User {
                        return withContext(Dispatchers.IO) {
                            // Долгая операция
                            apiService.getUser()
                        }
                    }
                    ```
                    
                    Ключевое слово `suspend` указывает компилятору, что функция может быть приостановлена.
                """.trimIndent(),
                category = "kotlin",
                difficulty = "intermediate"
            ),
            InterviewQuestion(
                id = "iq21",
                question = "Что такое Gradle в Android и для чего он нужен?",
                answer = """
                    **Gradle** — это система сборки и управления зависимостями для Android проектов.
                    
                    Основные функции:
                    1. **Сборка приложения** — компиляция, линковка, подписание
                    2. **Управление зависимостями** — автоматическая загрузка библиотек
                    3. **Конфигурация сборок** — разные варианты для debug/release
                    4. **Создание APK/AAB** — пакетов для публикации
                    
                    Ключевые файлы:
                    - **build.gradle (Project)** — настройки всего проекта
                    - **build.gradle (Module)** — настройки конкретного модуля
                    - **settings.gradle** — список модулей проекта
                    - **gradle.properties** — свойства Gradle
                    
                    Gradle использует DSL на основе Groovy или Kotlin для конфигурации.
                """.trimIndent(),
                category = "android",
                difficulty = "intermediate"
            ),
            InterviewQuestion(
                id = "iq22",
                question = "Что такое SOLID принципы в разработке ПО?",
                answer = """
                    **SOLID** — это набор принципов объектно-ориентированного проектирования:
                    
                    1. **S (Single Responsibility)** — класс должен иметь только одну причину для изменения
                    
                    2. **O (Open/Closed)** — классы должны быть открыты для расширения, но закрыты для модификации
                    
                    3. **L (Liskov Substitution)** — объекты базового класса должны быть заменяемы объектами производных классов
                    
                    4. **I (Interface Segregation)** — лучше несколько специализированных интерфейсов, чем один общий
                    
                    5. **D (Dependency Inversion)** — зависимости должны быть на абстракциях, а не на конкретных реализациях
                    
                    Эти принципы помогают создавать гибкий, поддерживаемый и тестируемый код.
                """.trimIndent(),
                category = "general",
                difficulty = "advanced"
            ),
            InterviewQuestion(
                id = "iq23",
                question = "Что такое Repository паттерн в Android?",
                answer = """
                    **Repository** — это паттерн проектирования, который абстрагирует источник данных от остального приложения.
                    
                    Задачи Repository:
                    1. **Абстрагирование источника данных** — база данных, сеть, кэш
                    2. **Объединение нескольких источников** — стратегия кэширования
                    3. **Предоставление единого интерфейса** для доступа к данным
                    
                    Пример:
                    ```kotlin
                    class UserRepository(
                        private val localDataSource: UserLocalDataSource,
                        private val remoteDataSource: UserRemoteDataSource
                    ) {
                        suspend fun getUsers(): List<User> {
                            // Проверяем кэш, затем сеть
                            val localUsers = localDataSource.getUsers()
                            return if (localUsers.isNotEmpty()) {
                                localUsers
                            } else {
                                val remoteUsers = remoteDataSource.getUsers()
                                localDataSource.saveUsers(remoteUsers)
                                remoteUsers
                            }
                        }
                    }
                    ```
                    
                    Repository обычно используется с ViewModel в архитектуре MVVM.
                """.trimIndent(),
                category = "android",
                difficulty = "advanced"
            ),
            InterviewQuestion(
                id = "iq24",
                question = "Что такое Lambda выражения и Higher-Order функции в Kotlin?",
                answer = """
                    **Lambda выражения** — это анонимные функции, которые могут быть переданы как значения.
                    
                    Пример:
                    ```kotlin
                    val sum: (Int, Int) -> Int = { a, b -> a + b }
                    println(sum(2, 3)) // 5
                    ```
                    
                    **Higher-Order функции** — это функции, которые принимают другие функции как параметры или возвращают их.
                    
                    Пример:
                    ```kotlin
                    fun calculate(a: Int, b: Int, operation: (Int, Int) -> Int): Int {
                        return operation(a, b)
                    }
                    
                    val result = calculate(10, 5) { x, y -> x * y }
                    ```
                    
                    Стандартные higher-order функции в Kotlin:
                    - **filter()** — фильтрация коллекций
                    - **map()** — преобразование элементов
                    - **forEach()** — итерация по элементам
                    - **let()**, **run()**, **apply()**, **also()** — scope функции
                """.trimIndent(),
                category = "kotlin",
                difficulty = "intermediate"
            ),
            InterviewQuestion(
                id = "iq25",
                question = "Что такое WorkManager в Android?",
                answer = """
                    **WorkManager** — это библиотека Android Jetpack для выполнения отложенных, гарантированных фоновых задач.
                    
                    Особенности WorkManager:
                    1. **Гарантированное выполнение** — даже после перезагрузки устройства
                    2. **Учет ограничений** — выполнение при наличии сети, зарядке и т.д.
                    3. **Цепочки задач** — последовательное или параллельное выполнение
                    4. **Наблюдение за состоянием** — через LiveData или Flow
                    
                    Пример:
                    ```kotlin
                    val uploadWork = OneTimeWorkRequestBuilder<UploadWorker>()
                        .setConstraints(
                            Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build()
                        )
                        .build()
                    
                    WorkManager.getInstance(context).enqueue(uploadWork)
                    ```
                    
                    WorkManager подходит для периодических или разовых фоновых задач.
                """.trimIndent(),
                category = "android",
                difficulty = "intermediate"
            ),
            InterviewQuestion(
                id = "iq26",
                question = "Что такое делегаты (delegates) в Kotlin?",
                answer = """
        **Делегаты** — это шаблон проектирования, позволяющий передать реализацию методов другому объекту.
        
        В Kotlin есть встроенная поддержка делегатов через ключевое слово `by`.
        
        Типы делегатов:
        
        1. **Delegated Properties** — делегирование свойств
        ```kotlin
        class Example {
            var p: String by Delegate()
        }
        ```
        
        2. **Lazy** — отложенная инициализация
        ```kotlin
        val lazyValue: String by lazy {
            println("Инициализация")
            "Hello"
        }
        ```
        
        3. **Observable** — отслеживание изменений
        ```kotlin
        var name: String by Delegates.observable("") { 
            property, previousValue, newValue ->
            println("${"$"}previousValue -> ${"$"}newValue")
        }
        ```
        
        4. **Delegated Implementation** — делегирование интерфейсов
        ```kotlin
        interface Base { fun print() }
        class BaseImpl(val x: Int) : Base {
            override fun print() { print(x) }
        }
        class Derived(b: Base) : Base by b
        ```
    """.trimIndent(),
                category = "kotlin",
                difficulty = "intermediate"
            ),
            InterviewQuestion(
                id = "iq27",
                question = "Что такое делегаты (delegates) в Kotlin?",
                answer = """
        **Делегаты** — это шаблон проектирования, позволяющий передать реализацию методов другому объекту.
        
        В Kotlin есть встроенная поддержка делегатов через ключевое слово `by`.
        
        Типы делегатов:
        
        1. **Delegated Properties** — делегирование свойств
            class Example {
                var p: String by Delegate()
            }
        
        2. **Lazy** — отложенная инициализация
            val lazyValue: String by lazy {
                println("Инициализация")
                "Hello"
            }
        
        3. **Observable** — отслеживание изменений
            var name: String by Delegates.observable("") { 
                prop, old, new ->
                println("old -> new")
            }
        
        4. **Delegated Implementation** — делегирование интерфейсов
            interface Base { fun print() }
            class BaseImpl(val x: Int) : Base {
                override fun print() { print(x) }
            }
            class Derived(b: Base) : Base by b
    """.trimIndent(),
                category = "kotlin",
                difficulty = "intermediate"
            ),
            InterviewQuestion(
                id = "iq28",
                question = "Что такое pattern matching с when в Kotlin?",
                answer = """
                    **Pattern matching (Сопоставление с образцом)** — это мощная функция Kotlin, реализуемая через выражение `when`.
                    
                    Возможности when:
                    
                    1. **Проверка значений**
                    ```kotlin
                    when (x) {
                        1 -> print("x == 1")
                        2 -> print("x == 2")
                        else -> print("x не 1 и не 2")
                    }
                    ```
                    
                    2. **Проверка диапазонов**
                    ```kotlin
                    when (x) {
                        in 1..10 -> print("от 1 до 10")
                        !in 10..20 -> print("не от 10 до 20")
                    }
                    ```
                    
                    3. **Проверка типов (Smart Cast)**
                    ```kotlin
                    when (obj) {
                        is String -> print(obj.length)
                        is Int -> print(obj + 1)
                    }
                    ```
                    
                    4. **Без аргумента (как if-else chain)**
                    ```kotlin
                    when {
                        x.isOdd() -> print("нечетное")
                        x.isEven() -> print("четное")
                    }
                    ```
                    
                    5. **С sealed class (exhaustive checking)**
                    ```kotlin
                    when (result) {
                        is Success -> print(result.data)
                        is Error -> print(result.message)
                    }
                    ```
                """.trimIndent(),
                category = "kotlin",
                difficulty = "intermediate"
            ),
            InterviewQuestion(
                id = "iq29",
                question = "Что такое ContentProvider в Android?",
                answer = """
                    **ContentProvider** — это компонент Android, который предоставляет данные приложения другим приложениям через стандартизированный интерфейс.
                    
                    Использование ContentProvider:
                    
                    1. **Предоставление данных** — доступ к вашим данным другим приложениям
                    2. **Использование данных** — доступ к данным других приложений (контакты, календарь)
                    3. **Работа с файлами** — безопасный обмен файлами
                    
                    Основные компоненты:
                    - **URI (Uniform Resource Identifier)** — идентификатор данных
                    - **Cursor** — результат запроса
                    - **ContentResolver** — клиент для доступа к ContentProvider
                    
                    Пример запроса контактов:
                    ```kotlin
                    val cursor = contentResolver.query(
                        ContactsContract.Contacts.CONTENT_URI,
                        null, null, null, null
                    )
                    ```
                    
                    В современных приложениях часто используют FileProvider вместо создания собственного ContentProvider.
                """.trimIndent(),
                category = "android",
                difficulty = "advanced"
            ),
            InterviewQuestion(
                id = "iq30",
                question = "Что такое инверсия управления (IoC) и как она реализована в Android?",
                answer = """
                    **Инверсия управления (Inversion of Control)** — это принцип, при котором управление объектами передается контейнеру или фреймворку.
                    
                    В Android IoC реализована через:
                    
                    1. **Жизненный цикл компонентов** — Android управляет созданием и уничтожением Activity, Fragment, Service
                    
                    2. **Dependency Injection** — внедрение зависимостей через Hilt/Dagger
                    ```kotlin
                    @HiltAndroidApp
                    class MyApp : Application()
                    
                    @AndroidEntryPoint
                    class MainActivity : AppCompatActivity() {
                        @Inject lateinit var repository: MyRepository
                    }
                    ```
                    
                    3. **Системные сервисы** — получение сервисов через getSystemService()
                    ```kotlin
                    val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    ```
                    
                    4. **Intent система** — запуск компонентов через Intent
                    
                    Преимущества IoC:
                    - Уменьшение связанности компонентов
                    - Упрощение тестирования
                    - Повторное использование кода
                    - Централизованное управление зависимостями
                """.trimIndent(),
                category = "android",
                difficulty = "advanced"
            ),
            // === Новые вопросы (31–50) ===

            InterviewQuestion(
                id = "iq31",
                question = "Что такое Jetpack Compose и чем он отличается от XML-разметки?",
                answer = """
                    **Jetpack Compose** — это современный декларативный фреймворк для создания UI в Android.
                    
                    Отличия от XML:
                    - **Декларативный подход**: описываем, *каким должен быть* UI, а не как его строить
                    - **Код вместо XML**: UI пишется на Kotlin, что упрощает логику и рефакторинг
                    - **State-driven**: UI автоматически перерисовывается при изменении состояния
                    - **Нет findViewById**: всё работает через функции и параметры
                    - **Меньше boilerplate**: нет адаптеров, слушателей, связывания
                    
                    Пример:
                    ```kotlin
                    @Composable
                    fun Greeting(name: String) {
                        Text("Hello, (name!))
                    }
                    ```
                """.trimIndent(),
                category = "android",
                difficulty = "intermediate"
            ),
            InterviewQuestion(
                id = "iq32",
                question = "Что такое State и MutableState в Jetpack Compose?",
                answer = """
                    **State** — это объект, который хранит значение и позволяет Compose отслеживать изменения.
                    
                    **MutableState** — изменяемая версия State, которая вызывает recomposition при изменении.
                    
                    Создание:
                    ```kotlin
                    var count by remember { mutableStateOf(0) }
                    ```
                    
                    Использование:
                    ```kotlin
                    Button(onClick = { count++ }) {
                        Text("Clicked $("count") times")
                    }
                    ```
                    
                    Важно: только изменения `State`/`MutableState` вызывают перерисовку UI. Обычные переменные — нет.
                """.trimIndent(),
                category = "android",
                difficulty = "intermediate"
            ),
            InterviewQuestion(
                id = "iq33",
                question = "Что такое Clean Architecture и зачем она нужна?",
                answer = """
                    **Clean Architecture** — это подход к проектированию приложений, основанный на разделении ответственности и независимости от фреймворков.
                    
                    Слои (изнутри наружу):
                    1. **Domain** — чистая бизнес-логика (use cases, entities)
                    2. **Data** — реализация источников данных (сеть, БД)
                    3. **Presentation** — UI и взаимодействие с пользователем (ViewModel, Compose/Activity)
                    
                    Преимущества:
                    - Тестируемость (бизнес-логика не зависит от Android)
                    - Поддерживаемость (изменения в одном слое не ломают другие)
                    - Гибкость (можно заменить UI или источник данных без переписывания логики)
                """.trimIndent(),
                category = "architecture",
                difficulty = "advanced"
            ),
            InterviewQuestion(
                id = "iq34",
                question = "Как работает механизм обработки разрешений (permissions) в Android?",
                answer = """
                    Начиная с Android 6.0 (API 23), разрешения запрашиваются **во время выполнения**, а не при установке.
                    
                    Этапы:
                    1. Проверка: `ContextCompat.checkSelfPermission()`
                    2. Запрос: `ActivityCompat.requestPermissions()` или `registerForActivityResult()`
                    3. Обработка результата: `onRequestPermissionsResult()` или `ActivityResultLauncher`
                    
                    Пример с ActivityResult:
                    ```kotlin
                    val permissionLauncher = registerForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        if (isGranted) { /* разрешено */ }
                    }
                    
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                    ```
                    
                    Важно: пользователь может выбрать «Больше не спрашивать» — тогда нужно направить в настройки.
                """.trimIndent(),
                category = "android",
                difficulty = "intermediate"
            ),
            InterviewQuestion(
                id = "iq35",
                question = "Что такое Unit-тесты и интеграционные тесты? В чём разница?",
                answer = """
                    **Unit-тесты** проверяют отдельные функции или классы в изоляции (без зависимостей).
                    - Быстрые
                    - Не требуют Android-окружения
                    - Используют JUnit, MockK
                    
                    **Интеграционные тесты** проверяют взаимодействие нескольких компонентов.
                    - Медленнее
                    - Могут использовать реальные зависимости (например, Room + Repository)
                    - Проверяют корректность работы слоёв вместе
                    
                    Пример:
                    - Unit: тестируем UseCase без ViewModel и Repository
                    - Integration: тестируем Repository с настоящей базой данных
                """.trimIndent(),
                category = "testing",
                difficulty = "intermediate"
            ),
            InterviewQuestion(
                id = "iq36",
                question = "Что такое Parcelable и Serializable в Android? В чём разница?",
                answer = """
                    Оба интерфейса используются для **сериализации объектов**, но:
                    
                    - **Parcelable** — специфичен для Android, быстрее, меньше аллокаций
                      ```kotlin
                      data class User(val name: String) : Parcelable
                      ```
                      Генерируется автоматически с помощью `@Parcelize` (требуется плагин)
                    
                    - **Serializable** — стандарт Java, медленнее, проще в использовании
                      ```kotlin
                      data class User(val name: String) : Serializable
                      ```
                    
                    Рекомендация: используйте **Parcelable** (или `@Parcelize`) для передачи данных между компонентами.
                """.trimIndent(),
                category = "android",
                difficulty = "intermediate"
            ),
            InterviewQuestion(
                id = "iq37",
                question = "Что такое Context в Android и какие его типы бывают?",
                answer = """
                    **Context** — это интерфейс, предоставляющий доступ к ресурсам и системным сервисам приложения.
                    
                    Основные типы:
                    - **Application Context** — живёт всё время приложения, не привязан к UI
                      ```kotlin
                      applicationContext
                      ```
                    - **Activity Context** — привязан к жизненному циклу Activity, содержит тему и UI-ресурсы
                    
                    Когда что использовать:
                    - Для UI, диалогов, LayoutInflater — **Activity Context**
                    - Для долгоживущих операций (например, WorkManager) — **Application Context**
                    
                    ❗ Никогда не сохраняйте ссылку на Activity Context в долгоживущих объектах — будет утечка памяти!
                """.trimIndent(),
                category = "android",
                difficulty = "intermediate"
            ),
            InterviewQuestion(
                id = "iq38",
                question = "Что такое inline-функции в Kotlin и зачем они нужны?",
                answer = """
                    **Inline-функции** — это функции, код которых вставляется («встраивается») в место вызова во время компиляции.
                    
                    Зачем:
                    - Избегаем накладных расходов на создание объектов (особенно для лямбд)
                    - Позволяют использовать `return` из внешней функции (non-local return)
                    
                    Пример:
                    ```kotlin
                    inline fun myRun(block: () -> Unit) {
                        block()
                    }
                    ```
                    
                    ⚠️ Не злоупотребляйте: увеличивает размер байткода. Используйте для функций с лямбдами.
                """.trimIndent(),
                category = "kotlin",
                difficulty = "intermediate"
            ),
            InterviewQuestion(
                id = "iq39",
                question = "Что такое Koin и чем он отличается от Hilt?",
                answer = """
                    **Koin** и **Hilt** — это фреймворки для Dependency Injection в Android.
                    
                    **Koin**:
                    - Лёгкий, написан на Kotlin
                    - Не использует аннотации компиляции (работает в рантайме)
                    - Простая настройка через DSL
                    - Подходит для небольших проектов
                    
                    **Hilt**:
                    - Официальный DI от Google, основан на Dagger
                    - Использует аннотации компиляции (быстрее в рантайме)
                    - Глубокая интеграция с Android (Activity, Fragment, ViewModel)
                    - Лучше для крупных проектов
                    
                    Выбор зависит от масштаба и предпочтений команды.
                """.trimIndent(),
                category = "android",
                difficulty = "advanced"
            ),
            InterviewQuestion(
                id = "iq40",
                question = "Что такое DataStore и почему он лучше SharedPreferences?",
                answer = """
                    **DataStore** — это современная замена SharedPreferences от Android Jetpack.
                    
                    Преимущества над SharedPreferences:
                    - **Асинхронный** — не блокирует UI-поток
                    - **Типобезопасный** — поддержка Kotlin-типов и сериализации
                    - **Поддержка Flow** — реактивное чтение данных
                    - **Нет метода apply()/commit()** — всё через suspend-функции или Flow
                    - **Меньше ошибок** — нет риска ConcurrentModificationException
                    
                    Типы:
                    - **Preferences DataStore** — как SharedPreferences, но лучше
                    - **Proto DataStore** — для сложных объектов через Protocol Buffers
                    
                    Рекомендуется использовать DataStore вместо SharedPreferences в новых проектах.
                """.trimIndent(),
                category = "android",
                difficulty = "intermediate"
            ),
            InterviewQuestion(
                id = "iq41",
                question = "Что такое sealed interface в Kotlin?",
                answer = """
                    **Sealed interface** — это расширение sealed class, появившееся в Kotlin 1.5.
                    
                    Позволяет определять ограниченную иерархию реализаций, где все реализации известны компилятору.
                    
                    Пример:
                    ```kotlin
                    sealed interface Result
                    data class Success(val data: String) : Result
                    data class Error(val msg: String) : Result
                    object Loading : Result
                    ```
                    
                    Преимущества:
                    - Можно использовать в `when` без `else` (exhaustive)
                    - Реализации могут быть классами, объектами, даже другими sealed-типами
                    - Гибче, чем sealed class (поддерживает множественное наследование)
                """.trimIndent(),
                category = "kotlin",
                difficulty = "advanced"
            ),
            InterviewQuestion(
                id = "iq42",
                question = "Как работает система сборки Gradle в Android?",
                answer = """
                    **Gradle** использует **плагины** и **задачи (tasks)** для сборки проекта.
                    
                    Этапы сборки:
                    1. **Configuration phase** — читаются build.gradle файлы
                    2. **Execution phase** — выполняются выбранные задачи (например, `assembleDebug`)
                    
                    Ключевые задачи:
                    - `assemble` — собирает APK/AAB
                    - `check` — запускает тесты и линтеры
                    - `build` — объединяет assemble + check
                    
                    Конфигурация:
                    - **Flavors** — разные версии приложения (free/paid)
                    - **Build types** — debug/release с разными настройками
                    - **Dependencies** — управление библиотеками
                    
                    Процесс полностью настраивается через Kotlin/Groovy DSL.
                """.trimIndent(),
                category = "android",
                difficulty = "intermediate"
            ),
            InterviewQuestion(
                id = "iq43",
                question = "Что такое деструктуризация в Kotlin?",
                answer = """
                    **Деструктуризация** — это возможность извлекать значения из объектов в отдельные переменные.
                    
                    Работает с:
                    - **data class**
                    - **List, Pair, Triple**
                    - Любыми классами с `componentN()` функциями
                    
                    Пример:
                    ```kotlin
                    data class Person(val name: String, val age: Int)
                    val person = Person("Alice", 30)
                    val (name, age) = person
                    println(name) // Alice
                    ```
                    
                    Для списков:
                    ```kotlin
                    val list = listOf("a", "b", "c")
                    val (first, second) = list
                    ```
                    
                    Удобно для упрощения кода и повышения читаемости.
                """.trimIndent(),
                category = "kotlin",
                difficulty = "beginner"
            ),
            InterviewQuestion(
                id = "iq44",
                question = "Что такое Navigation Component в Android?",
                answer = """
                    **Navigation Component** — часть Jetpack для управления навигацией между экранами.
                    
                    Компоненты:
                    - **NavGraph** — XML или Compose-граф маршрутов
                    - **NavController** — управляет переходами
                    - **NavHost** — контейнер для destinations
                    
                    Преимущества:
                    - Автоматическая обработка кнопки «Назад»
                    - Безопасная передача аргументов (с типами)
                    - Deep links поддержка
                    - Интеграция с меню и BottomNavigationView
                    
                    В Compose:
                    ```kotlin
                    NavHost(navController, startDestination = "home") {
                        composable("home") { HomeScreen() }
                        composable("detail/{id}") { DetailScreen(it.arguments?.getString("id")) }
                    }
                    ```
                """.trimIndent(),
                category = "android",
                difficulty = "intermediate"
            ),
            InterviewQuestion(
                id = "iq45",
                question = "Что такое ViewBinding и чем он отличается от findViewById?",
                answer = """
                    **ViewBinding** — это функция, генерирующая класс для каждого layout-файла, позволяя безопасно обращаться к View.
                    
                    Преимущества над findViewById:
                    - **Типобезопасность** — ошибка на этапе компиляции, а не рантайма
                    - **Null safety** — не нужно проверять null для View в Activity/Fragment
                    - **Нет магических ID** — доступ через свойства
                    
                    Пример:
                    ```kotlin
                    class MainActivity : AppCompatActivity() {
                        private lateinit var binding: ActivityMainBinding
                        
                        override fun onCreate(savedInstanceState: Bundle?) {
                            super.onCreate(savedInstanceState)
                            binding = ActivityMainBinding.inflate(layoutInflater)
                            setContentView(binding.root)
                            binding.button.text = "Click me"
                        }
                    }
                    ```
                    
                    ViewBinding заменил ButterKnife и упростил работу с UI.
                """.trimIndent(),
                category = "android",
                difficulty = "beginner"
            ),
            InterviewQuestion(
                id = "iq46",
                question = "Что такое typealias в Kotlin?",
                answer = """
                    **typealias** — это псевдоним для существующего типа, улучшающий читаемость кода.
                    
                    Пример:
                    ```kotlin
                    typealias JSON = Map<String, Any?>
                    typealias UserId = String
                    typealias CompletionHandler = (Result<String>) -> Unit
                    ```
                    
                    Использование:
                    ```kotlin
                    fun fetchUser(id: UserId, handler: CompletionHandler) {
                        // ...
                    }
                    ```
                    
                    Не создаёт новый тип — это просто алиас. Полезен для документирования и упрощения сложных сигнатур.
                """.trimIndent(),
                category = "kotlin",
                difficulty = "intermediate"
            ),
            InterviewQuestion(
                id = "iq47",
                question = "Что такое Memory Leak в Android и как его избежать?",
                answer = """
                    **Memory Leak** — это ситуация, когда объект остаётся в памяти, хотя больше не нужен.
                    
                    Распространённые причины:
                    - Хранение ссылки на Activity/Context в статическом поле
                    - Анонимные классы (например, Runnable), захватывающие Activity
                    - Незакрытые слушатели или подписки (например, в LiveData без LifecycleOwner)
                    
                    Как избежать:
                    - Использовать **Application Context** вместо Activity Context, где возможно
                    - Отписываться от наблюдателей в `onDestroy()` или через `Lifecycle`
                    - Использовать **WeakReference** для коллбэков
                    - Проверять утечки с помощью **Android Studio Profiler** или **LeakCanary**
                """.trimIndent(),
                category = "android",
                difficulty = "advanced"
            ),
            InterviewQuestion(
                id = "iq48",
                question = "Что такое CoroutineScope и какие виды Scope бывают?",
                answer = """
                    **CoroutineScope** — это контекст, в котором запускаются корутины. Он управляет их жизненным циклом.
                    
                    Встроенные Scope:
                    - **GlobalScope** — глобальный, не привязан к жизненному циклу (опасен!)
                    - **lifecycleScope** — в Activity/Fragment, автоматически отменяется при onDestroy()
                    - **viewModelScope** — в ViewModel, отменяется при onCleared()
                    - **coroutineScope {}** — создаёт дочерний scope, ждёт завершения всех дочерних корутин
                    - **supervisorScope {}** — как coroutineScope, но одна ошибка не отменяет остальные
                    
                    Рекомендация: никогда не используйте GlobalScope в Android. Всегда привязывайте к жизненному циклу.
                """.trimIndent(),
                category = "kotlin",
                difficulty = "advanced"
            ),
            InterviewQuestion(
                id = "iq49",
                question = "Что такое Material Design и как он применяется в Android?",
                answer = """
                    **Material Design** — это система дизайна от Google для создания единообразных и интуитивных интерфейсов.
                    
                    Основные принципы:
                    - **Глубина и тени** — карточки, возвышения
                    - **Анимации** — плавные переходы, ripple-эффекты
                    - **Цветовая палитра** — primary, secondary, surface цвета
                    - **Компоненты** — Button, Card, BottomSheet, Snackbar
                    
                    В Android используется через:
                    - **Material Components Library** (`com.google.android.material:material`)
                    - **Compose Material 3** — для Jetpack Compose (`androidx.compose.material3`)
                    
                    Пример Compose:
                    ```kotlin
                    Button(onClick = {}) {
                        Text("Submit")
                    }
                    ```
                """.trimIndent(),
                category = "android",
                difficulty = "beginner"
            ),
            InterviewQuestion(
                id = "iq50",
                question = "Что такое CI/CD и как оно используется в Android-разработке?",
                answer = """
                    **CI/CD (Continuous Integration / Continuous Delivery)** — это практика автоматизации сборки, тестирования и доставки приложения.
                    
                    В Android CI/CD обычно включает:
                    - **Сборку APK/AAB** на каждый коммит
                    - **Запуск unit и интеграционных тестов**
                    - **Проверку кода** (lint, detekt, ktlint)
                    - **Публикацию в Firebase App Distribution или Google Play**
                    
                    Популярные инструменты:
                    - GitHub Actions
                    - GitLab CI
                    - Bitrise
                    - CircleCI
                    - Firebase Test Lab (для тестов на реальных устройствах)
                    
                    Преимущества: быстрая обратная связь, меньше багов в продакшене, автоматизация рутины.
                """.trimIndent(),
                category = "general",
                difficulty = "intermediate"
            )
        )
    }

    fun getQuestionById(id: String): InterviewQuestion? {
        return getInterviewQuestions().find { it.id == id }
    }

    fun getQuestionsByCategory(category: String): List<InterviewQuestion> {
        return getInterviewQuestions().filter { it.category == category }
    }
}