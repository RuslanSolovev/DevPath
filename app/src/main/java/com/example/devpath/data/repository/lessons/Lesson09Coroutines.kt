package com.example.devpath.data.repository.lessons

import com.example.devpath.domain.models.Lesson

object Lesson09Coroutines {
    fun get(): Lesson = Lesson(
        id = "coroutines_basics",
        title = "🔄 Корутины в Kotlin",
        description = "Асинхронное программирование без боли и коллбэков",
        difficulty = "advanced",
        duration = 45,
        topic = "coroutines",
        theory = """
            # 🔄 Корутины в Kotlin — асинхронность для людей

            **Корутины (Coroutines)** — это легковесные потоки для асинхронного программирования. Они позволяют писать асинхронный код в **синхронном стиле**, без коллбэков и сложных цепочек вызовов.

            ## 📋 Содержание урока
            - Что такое корутины и зачем они нужны
            - CoroutineScope, Dispatcher, Job
            - Запуск корутин (launch, async, runBlocking)
            - Приостановка и возобновление (suspend functions)
            - Обработка исключений
            - Структурная конкуренция
            - Отмена корутин
            - Таймауты
            - Flow — асинхронные потоки
            - Практические паттерны

            ---

            ## 🤔 Проблема асинхронного кода

            ### Традиционный подход (коллбэки) — "ад коллбэков"

            ```kotlin
            // Псевдокод на Java/JavaScript
            fetchUserData { user ->
                fetchUserPosts(user.id) { posts ->
                    fetchPostComments(posts[0].id) { comments ->
                        // Уровень вложенности растёт...
                        updateUI(comments)
                    }
                }
            }
            ```

            ### Корутины — линейный код

            ```kotlin
            suspend fun loadData() {
                val user = fetchUserData()      // приостановка, но не блокировка
                val posts = fetchUserPosts(user.id)
                val comments = fetchPostComments(posts[0].id)
                updateUI(comments)               // выполняется после всех загрузок
            }
            ```

            ---

            ## 🎯 Основные понятия

            ### 1. CoroutineScope — контекст выполнения

            `CoroutineScope` определяет жизненный цикл корутин. Все корутины должны запускаться в каком-то scope.

            ```kotlin
            // Глобальный scope (живёт пока живёт приложение)
            val globalScope = GlobalScope
            
            // Пользовательский scope
            val scope = CoroutineScope(Dispatchers.Default)
            
            // В Android есть встроенные scopes:
            // viewModelScope — для ViewModel
            // lifecycleScope — для Activity/Fragment
            ```

            ### 2. Dispatcher — где выполняется корутина

            | Диспетчер | Назначение |
            |-----------|------------|
            | `Dispatchers.Default` | CPU-интенсивные задачи (сортировка, обработка данных) |
            | `Dispatchers.IO` | Операции ввода-вывода (сеть, база данных, файлы) |
            | `Dispatchers.Main` | Главный поток UI (только для Android) |
            | `Dispatchers.Unconfined` | Не ограничен (обычно не используется) |

            ```kotlin
            scope.launch(Dispatchers.IO) {
                // Работа с сетью или БД
            }
            ```

            ### 3. Job — контроль жизненного цикла

            `Job` представляет собой выполняющуюся корутину. Через неё можно отслеживать состояние и отменять выполнение.

            ```kotlin
            val job = scope.launch {
                // какая-то работа
            }
            
            job.cancel()           // отмена
            job.join()             // ожидание завершения
            job.cancelAndJoin()    // отмена + ожидание
            ```

            ---

            ## 🚀 Запуск корутин

            ### launch — запуск без результата

            ```kotlin
            fun main() = runBlocking {
                val job = launch {
                    delay(1000)
                    println("Корутина завершена")
                }
                println("Ожидание...")
                job.join()
                println("Готово")
            }
            // Вывод:
            // Ожидание...
            // Корутина завершена
            // Готово
            ```

            ### async — запуск с результатом

            ```kotlin
            suspend fun fetchData(): String {
                delay(1000)
                return "Данные"
            }
            
            fun main() = runBlocking {
                val deferred: Deferred<String> = async { fetchData() }
                println("Ожидание данных...")
                val result = deferred.await()  // приостановка до получения результата
                println("Получено: ${'$'}result")
            }
            ```

            ### runBlocking — мост между мирами

            `runBlocking` блокирует текущий поток до завершения всех корутин. Используется в основном в `main()` и тестах.

            ```kotlin
            fun main() = runBlocking {
                // здесь можно вызывать suspend функции
                delay(1000)
                println("Прошла секунда")
            }
            ```

            ---

            ## ⏸️ Приостановка (suspend)

            Ключевое слово `suspend` отмечает функции, которые можно приостанавливать без блокировки потока.

            ```kotlin
            suspend fun longRunningTask(): String {
                delay(2000)  // приостановка на 2 секунды
                return "Результат"
            }
            
            suspend fun process() {
                println("Начало")
                val result = longRunningTask()  // приостановка здесь
                println("Результат: ${'$'}result")
            }
            ```

            ---

            ## 🧵 Переключение контекстов (withContext)

            ```kotlin
            suspend fun loadUserData(): UserData = withContext(Dispatchers.IO) {
                // Выполняется в IO потоке
                val user = api.fetchUser()
                val posts = api.fetchPosts()
                UserData(user, posts)
            }  // возвращаемся в исходный контекст
            ```

            ---

            ## ⚠️ Обработка исключений

            ### try-catch внутри корутины

            ```kotlin
            scope.launch {
                try {
                    val data = fetchData()
                    println("Данные: ${'$'}data")
                } catch (e: Exception) {
                    println("Ошибка: ${'$'}{e.message}")
                }
            }
            ```

            ### CoroutineExceptionHandler — глобальный обработчик

            ```kotlin
            val handler = CoroutineExceptionHandler { _, exception ->
                println("Поймано исключение: ${'$'}{exception.message}")
            }
            
            val scope = CoroutineScope(Dispatchers.Default + handler)
            
            scope.launch {
                throw RuntimeException("Ошибка в корутине")
            }
            ```

            ---

            ## 👨‍👦 Структурная конкуренция

            Корутины следуют иерархии родитель-потомок:

            ```kotlin
            fun main() = runBlocking {
                launch {
                    // родительская корутина
                    launch {
                        delay(1000)
                        println("Дочерняя 1")
                    }
                    launch {
                        delay(1500)
                        println("Дочерняя 2")
                    }
                }
                println("Родитель ждёт всех детей")
            }  // runBlocking ждёт завершения всех
            ```

            **Важно:** отмена родителя отменяет всех детей!

            ```kotlin
            val parentJob = scope.launch {
                launch { repeat(100) { delay(100); print(".") } }
                launch { repeat(100) { delay(150); print("-") } }
            }
            
            delay(500)
            parentJob.cancel()  // отменяет обе дочерние корутины
            ```

            ---

            ## 🛑 Отмена корутин

            ### Проверка на отмену

            ```kotlin
            scope.launch {
                while (i < 1000 && isActive) {  // isActive — свойство корутины
                    doSomeWork()
                    yield()  // добровольная приостановка для проверки отмены
                }
            }
            ```

            ### ensureActive — выбросит исключение при отмене

            ```kotlin
            scope.launch {
                repeat(1000) { i ->
                    ensureActive()  // CancellationException если корутина отменена
                    processItem(i)
                }
            }
            ```

            ---

            ## ⏱️ Таймауты

            ### withTimeout — выбрасывает исключение

            ```kotlin
            try {
                val result = withTimeout(1000) {
                    longRunningTask()  // если выполняется дольше 1 сек — исключение
                }
            } catch (e: TimeoutCancellationException) {
                println("Таймаут!")
            }
            ```

            ### withTimeoutOrNull — возвращает null при таймауте

            ```kotlin
            val result = withTimeoutOrNull(1000) {
                longRunningTask()
            } ?: "Таймаут, используем значение по умолчанию"
            ```

            ---

            ## 🔀 Параллельное выполнение

            ```kotlin
            suspend fun fetchTwoThings(): Pair<String, String> = coroutineScope {
                val deferred1 = async { fetchData1() }
                val deferred2 = async { fetchData2() }
                
                // Выполняются параллельно!
                deferred1.await() to deferred2.await()
            }
            
            // Или с awaitAll
            suspend fun fetchAll(): List<String> = coroutineScope {
                val deferreds = listOf(
                    async { fetchData1() },
                    async { fetchData2() },
                    async { fetchData3() }
                )
                deferreds.awaitAll()
            }
            ```

            ---

            ## 🌊 Flow — асинхронные потоки данных

            `Flow` — это асинхронный аналог `Sequence`, который может эмитить несколько значений.

            ### Создание Flow

            ```kotlin
            fun getNumbers(): Flow<Int> = flow {
                for (i in 1..5) {
                    delay(500)           // асинхронная задержка
                    emit(i)               // эмитим значение
                }
            }
            ```

            ### Коллектинг Flow

            ```kotlin
            suspend fun main() {
                getNumbers().collect { value ->
                    println("Получено: ${'$'}value")
                }
            }
            ```

            ### Операторы Flow

            ```kotlin
            getNumbers()
                .filter { it % 2 == 0 }
                .map { it * 10 }
                .catch { e -> println("Ошибка: ${'$'}{e.message}") }
                .collect { println("Результат: ${'$'}it") }
            ```

            ### StateFlow — для UI состояния

            ```kotlin
            class MyViewModel : ViewModel() {
                private val _state = MutableStateFlow("initial")
                val state: StateFlow<String> = _state.asStateFlow()
                
                fun updateState(newValue: String) {
                    _state.value = newValue
                }
            }
            ```

            ---

            ## 🎨 Практические паттерны

            ### 1. Ретри (повторы) с экспоненциальной задержкой

            ```kotlin
            suspend fun <T> retryWithBackoff(
                maxRetries: Int = 3,
                initialDelay: Long = 100,
                block: suspend () -> T
            ): T {
                repeat(maxRetries - 1) { attempt ->
                    try {
                        return block()
                    } catch (e: Exception) {
                        val delayTime = initialDelay * (1 shl attempt)  // 100, 200, 400...
                        println("Попытка ${'$'}{attempt + 1} не удалась, ждём ${'$'}delayTime мс")
                        delay(delayTime)
                    }
                }
                return block()  // последняя попытка
            }
            ```

            ### 2. Таймаут с действием по умолчанию

            ```kotlin
            suspend fun fetchWithTimeout(): String = withTimeoutOrNull(2000) {
                fetchData()
            } ?: "Данные не получены вовремя"
            ```

            ### 3. Последовательная и параллельная загрузка

            ```kotlin
            suspend fun loadDataConcurrently() = coroutineScope {
                val users = async { api.getUsers() }
                val posts = async { api.getPosts() }
                val comments = async { api.getComments() }
                
                Data(
                    users = users.await(),
                    posts = posts.await(),
                    comments = comments.await()
                )
            }
            
            suspend fun loadDataSequentially() {
                val users = api.getUsers()        // ждём
                val posts = api.getPosts()        // потом это
                val comments = api.getComments()  // потом это
            }
            ```

            ### 4. Обработка списка с ограничением параллелизма

            ```kotlin
            suspend fun <T, R> List<T>.mapConcurrent(
                maxConcurrent: Int = 5,
                transform: suspend (T) -> R
            ): List<R> = coroutineScope {
                val semaphore = Semaphore(maxConcurrent)
                map { item ->
                    async {
                        semaphore.withPermit {
                            transform(item)
                        }
                    }
                }.awaitAll()
            }
            ```

            ---

            ## 📊 Сравнение с потоками

            | Характеристика | Потоки (Threads) | Корутины |
            |----------------|------------------|----------|
            | Накладные расходы | Высокие (1-2 МБ на поток) | Очень низкие (единицы байт) |
            | Количество | Ограничено (сотни) | Практически безлимитно (миллионы) |
            | Блокировка | Блокирует поток | Приостанавливается без блокировки |
            | Переключение контекста | Дорогое | Очень дешёвое |
            | Отмена | Сложная (interrupt) | Встроенная (cancel) |
            | Синтаксис | Сложный | Линейный, как синхронный код |

            ---

            ## 💡 Советы и лучшие практики

            ✅ **Всегда используйте структурированную конкуренцию** — не запускайте глобальные корутины без необходимости  
            ✅ **Выбирайте правильный диспетчер** — IO для сети/БД, Default для вычислений  
            ✅ **Обрабатывайте исключения** — никогда не игнорируйте возможные ошибки  
            ✅ **Используйте таймауты** — любая внешняя операция может зависнуть  
            ✅ **Для UI состояния используйте StateFlow**  
            ✅ **Тестируйте корутины** с `runTest` и `TestCoroutineDispatcher`  
            ✅ **Не злоупотребляйте `async`** — если задачи независимы, то да, иначе лучше последовательный код  

            ---

            ## 🚫 Чего следует избегать

            ❌ **GlobalScope** — почти никогда не нужен, используйте свой scope  
            ❌ **Запуск корутин без сохранения Job** — нельзя будет отменить  
            ❌ **Блокирующие вызовы в корутинах** — используйте `withContext(Dispatchers.IO)`  
            ❌ **Слишком много параллельных задач** — контролируйте через семафоры  

            ---

            ## 📚 Резюме

            ✅ **Корутины** — легковесные потоки для асинхронности  
            ✅ **launch** — запуск без результата  
            ✅ **async/await** — запуск с результатом  
            ✅ **suspend** — функции, которые можно приостанавливать  
            ✅ **withContext** — переключение контекста  
            ✅ **Flow** — асинхронные потоки данных  
            ✅ **Структурная конкуренция** — иерархия родитель-потомок  
            ✅ **Отмена** — встроенная и кооперативная  

            ---

            ## 🚀 Что дальше?

            В следующих уроках мы изучим более продвинутые темы:
            - Каналы (Channels) для обмена данными между корутинами
            - Акторы (Actors) для конкурентного состояния
            - Тестирование корутин
            - Интеграция с библиотеками (Room, Retrofit)
        """.trimIndent(),
        codeExample = """
            import kotlinx.coroutines.*
            import kotlinx.coroutines.flow.*
            import kotlin.system.measureTimeMillis
            import java.lang.RuntimeException
            import kotlin.math.max
            
            fun main() = runBlocking {
                println("🔄 КОРУТИНЫ В KOTLIN - ПОЛНАЯ ДЕМОНСТРАЦИЯ\n")
                
                // 1️⃣ ОСНОВЫ: launch vs async
                println("1️⃣ ОСНОВЫ: launch vs async")
                
                // launch - без результата
                val job = launch {
                    delay(500)
                    println("  ✅ launch: задача выполнена")
                }
                job.join()
                
                // async - с результатом
                val deferred = async {
                    delay(500)
                    return@async 42
                }
                val result = deferred.await()
                println("  ✅ async: результат = ${'$'}result\n")
                
                // 2️⃣ ПАРАЛЛЕЛЬНЫЕ ВЫЧИСЛЕНИЯ
                println("2️⃣ ПАРАЛЛЕЛЬНЫЕ ВЫЧИСЛЕНИЯ")
                val time = measureTimeMillis {
                    val result = parallelComputation()
                    println("  Результат: ${'$'}result")
                }
                println("  Время выполнения: ${'$'}time мс (параллельно)\n")
                
                // 3️⃣ ПОСЛЕДОВАТЕЛЬНЫЕ ВЫЧИСЛЕНИЯ (для сравнения)
                println("3️⃣ ПОСЛЕДОВАТЕЛЬНЫЕ ВЫЧИСЛЕНИЯ")
                val time2 = measureTimeMillis {
                    val result = sequentialComputation()
                    println("  Результат: ${'$'}result")
                }
                println("  Время выполнения: ${'$'}time2 мс (последовательно)\n")
                
                // 4️⃣ ПЕРЕКЛЮЧЕНИЕ КОНТЕКСТОВ
                println("4️⃣ ПЕРЕКЛЮЧЕНИЕ КОНТЕКСТОВ")
                contextSwitching()
                
                // 5️⃣ ОБРАБОТКА ИСКЛЮЧЕНИЙ
                println("\n5️⃣ ОБРАБОТКА ИСКЛЮЧЕНИЙ")
                exceptionHandling()
                
                // 6️⃣ ТАЙМАУТЫ
                println("\n6️⃣ ТАЙМАУТЫ")
                timeoutExample()
                
                // 7️⃣ СТРУКТУРНАЯ КОНКУРЕНЦИЯ
                println("\n7️⃣ СТРУКТУРНАЯ КОНКУРЕНЦИЯ")
                structuredConcurrency()
                
                // 8️⃣ ОТМЕНА КОРУТИН
                println("\n8️⃣ ОТМЕНА КОРУТИН")
                cancellationExample()
                
                // 9️⃣ FLOW - АСИНХРОННЫЕ ПОТОКИ
                println("\n9️⃣ FLOW - АСИНХРОННЫЕ ПОТОКИ")
                flowExample()
                
                // 🔟 ПРАКТИЧЕСКИЕ ПАТТЕРНЫ
                println("\n🔟 ПРАКТИЧЕСКИЕ ПАТТЕРНЫ")
                practicalPatterns()
                
                println("\n=== ДЕМОНСТРАЦИЯ ЗАВЕРШЕНА ===")
            }
            
            // ==================== ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ ====================
            
            // Параллельные вычисления
            suspend fun parallelComputation(): Int = coroutineScope {
                println("  ⏳ Запуск 3 параллельных задач...")
                
                val deferred1 = async {
                    delay(1000)
                    println("    ✓ Задача 1 завершена")
                    10
                }
                
                val deferred2 = async {
                    delay(1200)
                    println("    ✓ Задача 2 завершена")
                    20
                }
                
                val deferred3 = async {
                    delay(800)
                    println("    ✓ Задача 3 завершена")
                    30
                }
                
                val results = awaitAll(deferred1, deferred2, deferred3)
                results.sum()
            }
            
            // Последовательные вычисления
            suspend fun sequentialComputation(): Int {
                println("  ⏳ Запуск 3 последовательных задач...")
                
                val r1 = task1()
                val r2 = task2()
                val r3 = task3()
                
                return r1 + r2 + r3
            }
            
            suspend fun task1(): Int {
                delay(1000)
                println("    ✓ Задача 1 завершена")
                return 10
            }
            
            suspend fun task2(): Int {
                delay(1200)
                println("    ✓ Задача 2 завершена")
                return 20
            }
            
            suspend fun task3(): Int {
                delay(800)
                println("    ✓ Задача 3 завершена")
                return 30
            }
            
            // Переключение контекстов
            suspend fun contextSwitching() {
                println("  Старт в потоке: ${'$'}{Thread.currentThread().name}")
                
                val result = withContext(Dispatchers.IO) {
                    println("  Работа в IO: ${'$'}{Thread.currentThread().name}")
                    delay(300)
                    "Данные из сети"
                }
                
                println("  Результат: ${'$'}result")
                println("  Вернулись в: ${'$'}{Thread.currentThread().name}")
            }
            
            // Обработка исключений
            suspend fun exceptionHandling() {
                // Локальный try-catch
                try {
                    val job = launch {
                        throw RuntimeException("Ошибка в корутине")
                    }
                    job.join()
                } catch (e: Exception) {
                    println("  Локальный catch: ${'$'}{e.message}")
                }
                
                // Глобальный обработчик
                val handler = CoroutineExceptionHandler { _, exception ->
                    println("  Глобальный handler: ${'$'}{exception.message}")
                }
                
                val scope = CoroutineScope(Dispatchers.Default + handler)
                scope.launch {
                    throw RuntimeException("Тестовое исключение")
                }
                delay(100) // даём время на выполнение
            }
            
            // Таймауты
            suspend fun timeoutExample() {
                // withTimeout - исключение
                try {
                    withTimeout(500) {
                        repeat(100) { i ->
                            delay(100)
                            println("    Итерация ${'$'}i")
                        }
                    }
                } catch (e: TimeoutCancellationException) {
                    println("    ⏰ Таймаут! (withTimeout)")
                }
                
                // withTimeoutOrNull - null
                val result = withTimeoutOrNull(300) {
                    delay(500)
                    "Успех"
                }
                println("    withTimeoutOrNull: ${'$'}{result ?: "null (таймаут)"}")
            }
            
            // Структурная конкуренция
            suspend fun structuredConcurrency() = coroutineScope {
                println("  Родительская корутина запущена")
                
                launch {
                    delay(500)
                    println("    Дочерняя 1 завершена")
                }
                
                launch {
                    delay(300)
                    println("    Дочерняя 2 завершена")
                }
                
                delay(200)
                println("  Родитель ждёт детей...")
                // coroutineScope автоматически ждёт всех детей
            }
            
            // Отмена корутин
            suspend fun cancellationExample() = coroutineScope {
                println("  Запуск долгой задачи...")
                
                val job = launch {
                    try {
                        repeat(100) { i ->
                            ensureActive()
                            println("    Работа ${'$'}i")
                            delay(200)
                        }
                    } catch (e: CancellationException) {
                        println("    ❌ Задача отменена на итерации")
                        throw e
                    }
                }
                
                delay(500)
                println("  Отменяем задачу...")
                job.cancelAndJoin()
                println("  Задача отменена")
            }
            
            // Flow - асинхронные потоки
            suspend fun flowExample() {
                // Простой Flow
                fun simpleFlow(): Flow<Int> = flow {
                    for (i in 1..5) {
                        delay(200)
                        emit(i)
                    }
                }
                
                println("  Простой Flow:")
                simpleFlow().collect { value ->
                    println("    Получено: ${'$'}value")
                }
                
                // Flow с операторами
                println("  Flow с операторами:")
                (1..10).asFlow()
                    .filter { it % 2 == 0 }
                    .map { it * 10 }
                    .take(3)
                    .collect { value ->
                        println("    Результат: ${'$'}value")
                    }
                
                // StateFlow
                println("  StateFlow:")
                val state = MutableStateFlow("Начальное")
                
                val collector = launch {
                    state.collect { newState ->
                        println("    Состояние: ${'$'}newState")
                    }
                }
                
                delay(100)
                state.value = "Обновление 1"
                delay(100)
                state.value = "Обновление 2"
                
                collector.cancel()
            }
            
            // Практические паттерны
            suspend fun practicalPatterns() {
                // 1. Ретри с экспоненциальной задержкой
                println("  Ретри с задержкой:")
                var attempt = 0
                val result = retryWithBackoff(
                    maxRetries = 3,
                    initialDelay = 50
                ) {
                    attempt++
                    if (attempt < 3) throw RuntimeException("Ошибка ${'$'}attempt")
                    "Успех после ${'$'}attempt попыток"
                }
                println("    Результат: ${'$'}result")
                
                // 2. Параллельная обработка списка
                println("\n  Параллельная обработка:")
                val numbers = (1..10).toList()
                val processed = numbers.mapConcurrent(maxConcurrent = 3) { num ->
                    delay(100)
                    "Обработано ${'$'}num"
                }
                processed.forEach { println("    ${'$'}it") }
                
                // 3. Таймаут с дефолтом
                val data = withTimeoutOrNull(200) {
                    delay(500)
                    "Данные"
                } ?: "Данные по умолчанию"
                println("\n  Таймаут с дефолтом: ${'$'}data")
            }
            
            // Функция ретри с экспоненциальной задержкой
            suspend fun <T> retryWithBackoff(
                maxRetries: Int = 3,
                initialDelay: Long = 100,
                block: suspend () -> T
            ): T {
                repeat(maxRetries - 1) { attempt ->
                    try {
                        return block()
                    } catch (e: Exception) {
                        val delayTime = initialDelay * (1 shl attempt)
                        println("    Попытка ${'$'}{attempt + 1} не удалась, ждём ${'$'}delayTime мс")
                        delay(delayTime)
                    }
                }
                return block()
            }
            
            // Функция для параллельной обработки с ограничением
            suspend fun <T, R> List<T>.mapConcurrent(
                maxConcurrent: Int = 5,
                transform: suspend (T) -> R
            ): List<R> = coroutineScope {
                val semaphore = Semaphore(maxConcurrent)
                map { item ->
                    async {
                        semaphore.withPermit {
                            transform(item)
                        }
                    }
                }.awaitAll()
            }
            
            // Простой семафор для ограничения параллелизма
            class Semaphore(private val maxConcurrent: Int) {
                private val counter = java.util.concurrent.atomic.AtomicInteger(0)
                
                suspend fun <T> withPermit(block: suspend () -> T): T {
                    while (true) {
                        val current = counter.get()
                        if (current < maxConcurrent && counter.compareAndSet(current, current + 1)) {
                            return try {
                                block()
                            } finally {
                                counter.decrementAndGet()
                            }
                        }
                        delay(10)
                    }
                }
            }
        """.trimIndent()
    )
}