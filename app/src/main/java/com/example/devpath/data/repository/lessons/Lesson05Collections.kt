package com.example.devpath.data.repository.lessons

import com.example.devpath.domain.models.Lesson

object Lesson05Collections {
    fun get(): Lesson = Lesson(
        id = "collections",
        title = "📚 Коллекции в Kotlin",
        description = "Изучаем List, Set, Map и функциональные операции с коллекциями",
        difficulty = "intermediate",
        duration = 40,
        topic = "collections",
        theory = """
            # 📚 Коллекции в Kotlin

            Коллекции — это фундаментальная концепция программирования, позволяющая хранить и обрабатывать группы объектов. Kotlin предлагает богатую и продуманную библиотеку коллекций с двумя важными особенностями:

            1. **Разделение на изменяемые и неизменяемые** интерфейсы
            2. **Мощные функциональные операции** (map, filter, reduce и др.)

            ## 🎯 Иерархия коллекций

            ```
                            Iterable (корень)
                                |
                    ┌───────────┴───────────┐
                    │                       │
                Collection               Sequence (ленивые)
                    │
            ┌───────┼───────┐
            │       │       │
            List    Set     Map
            ```

            ## 1️⃣ List — упорядоченные коллекции

            List хранит элементы в определённом порядке и допускает дубликаты.

            ### Создание списков

            ```kotlin
            // Неизменяемый список (immutable)
            val readOnlyList = listOf(1, 2, 3, 4, 5)
            
            // Изменяемый список (mutable)
            val mutableList = mutableListOf("A", "B", "C")
            
            // Пустой список с указанием типа
            val emptyList: List<String> = emptyList()
            
            // Список с помощью билдера
            val builtList = buildList {
                add(1)
                add(2)
                addAll(listOf(3, 4, 5))
            }
            ```

            ### Основные операции с List

            ```kotlin
            val list = listOf(1, 2, 3, 4, 5)
            
            // Доступ по индексу
            println(list[0])           // 1
            println(list.get(1))        // 2
            
            // Размер списка
            println(list.size)          // 5
            
            // Проверка наличия элемента
            println(3 in list)          // true
            println(10 in list)         // false
            
            // Поиск индекса элемента
            println(list.indexOf(3))     // 2
            println(list.lastIndexOf(3)) // 2 (если дубликатов нет)
            
            // Первый и последний элементы
            println(list.first())        // 1
            println(list.last())         // 5
            println(list.firstOrNull())  // 1 (безопасная версия)
            ```

            ### Изменяемые операции (для MutableList)

            ```kotlin
            val mutableList = mutableListOf(1, 2, 3)
            
            mutableList.add(4)              // [1, 2, 3, 4]
            mutableList.add(1, 10)          // [1, 10, 2, 3, 4] (вставка по индексу)
            
            mutableList.remove(2)            // [1, 10, 3, 4] (удаление по значению)
            mutableList.removeAt(1)          // [1, 3, 4] (удаление по индексу)
            
            mutableList[0] = 100             // [100, 3, 4]
            mutableList.set(1, 200)          // [100, 200, 4]
            
            mutableList.clear()              // [] (очистить всё)
            ```

            ## 2️⃣ Set — уникальные элементы

            Set хранит только уникальные элементы (без дубликатов). Порядок не гарантируется.

            ### Создание множеств

            ```kotlin
            // Неизменяемое множество
            val readOnlySet = setOf(1, 2, 3, 2, 1)  // [1, 2, 3] (дубликаты удалены)
            
            // Изменяемое множество
            val mutableSet = mutableSetOf("apple", "banana", "orange")
            
            // HashSet (более производительный)
            val hashSet = hashSetOf(1, 2, 3, 4, 5)
            
            // LinkedHashSet (сохраняет порядок добавления)
            val linkedSet = linkedSetOf("one", "two", "three")
            ```

            ### Основные операции с Set

            ```kotlin
            val set = setOf(1, 2, 3, 4, 5)
            
            // Проверка наличия
            println(3 in set)           // true
            println(10 in set)          // false
            
            // Размер
            println(set.size)           // 5
            
            // Добавление/удаление (для MutableSet)
            val mutableSet = mutableSetOf(1, 2, 3)
            mutableSet.add(4)            // [1, 2, 3, 4]
            mutableSet.add(2)            // [1, 2, 3, 4] (дубликат игнорируется)
            mutableSet.remove(2)          // [1, 3, 4]
            
            // Операции над множествами
            val set1 = setOf(1, 2, 3)
            val set2 = setOf(3, 4, 5)
            
            println(set1 union set2)              // [1, 2, 3, 4, 5] (объединение)
            println(set1 intersect set2)          // [3] (пересечение)
            println(set1 subtract set2)           // [1, 2] (разность)
            ```

            ## 3️⃣ Map — словари (ключ-значение)

            Map хранит пары ключ-значение. Каждый ключ уникален.

            ### Создание словарей

            ```kotlin
            // Неизменяемый Map
            val readOnlyMap = mapOf(
                "name" to "John",
                "age" to 25,
                "city" to "New York"
            )
            
            // Изменяемый Map
            val mutableMap = mutableMapOf<String, Int>()
            
            // HashMap
            val hashMap = hashMapOf(1 to "one", 2 to "two", 3 to "three")
            
            // С помощью билдера
            val builtMap = buildMap {
                put("key1", "value1")
                put("key2", "value2")
            }
            ```

            ### Основные операции с Map

            ```kotlin
            val map = mapOf("a" to 1, "b" to 2, "c" to 3)
            
            // Доступ по ключу
            println(map["a"])              // 1
            println(map.get("b"))           // 2
            println(map["d"])               // null (ключа нет)
            
            // Безопасное получение с дефолтом
            println(map.getOrDefault("d", 0))  // 0
            
            // Проверка наличия ключа/значения
            println("a" in map)            // true (проверка ключа)
            println(2 in map.values)       // true (проверка значения)
            
            // Все ключи и значения
            println(map.keys)               // [a, b, c]
            println(map.values)             // [1, 2, 3]
            
            // Размер
            println(map.size)               // 3
            ```

            ### Изменяемые операции (для MutableMap)

            ```kotlin
            val mutableMap = mutableMapOf("x" to 10, "y" to 20)
            
            // Добавление/изменение
            mutableMap["z"] = 30            // {x=10, y=20, z=30}
            mutableMap.put("y", 25)          // {x=10, y=25, z=30}
            
            // Удаление
            mutableMap.remove("x")           // {y=25, z=30}
            
            // Добавление нескольких элементов
            mutableMap.putAll(mapOf("a" to 1, "b" to 2))
            
            // Очистка
            mutableMap.clear()               // {}
            ```

            ## 🔄 Функциональные операции с коллекциями

            Самая мощная часть Kotlin — функции высшего порядка для работы с коллекциями.

            ### Трансформация (map, flatMap)

            ```kotlin
            val numbers = listOf(1, 2, 3, 4, 5)
            
            // map - преобразует каждый элемент
            val squares = numbers.map { it * it }          // [1, 4, 9, 16, 25]
            
            // mapIndexed - с доступом к индексу
            // mapIndexed - с доступом к индексу
val indexed = numbers.mapIndexed { index, value -> 
    "элемент[${'$'}index] = ${'$'}value"  // Здесь ${'$'}index и ${'$'}value - это обычные переменные, не шаблоны строки
}
            
            // flatMap - преобразует и сглаживает результат
            val nested = numbers.flatMap { listOf(it, it * 10) }
            // [1, 10, 2, 20, 3, 30, 4, 40, 5, 50]
            ```

            ### Фильтрация (filter, take, drop)

            ```kotlin
            val numbers = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
            
            // filter - оставляет элементы, удовлетворяющие условию
            val evens = numbers.filter { it % 2 == 0 }        // [2, 4, 6, 8, 10]
            val odds = numbers.filterNot { it % 2 == 0 }      // [1, 3, 5, 7, 9]
            
            // filterNotNull - убирает null
            val withNulls = listOf(1, null, 2, null, 3)
            val withoutNulls = withNulls.filterNotNull()      // [1, 2, 3]
            
            // filterIsInstance - фильтр по типу
            val mixed = listOf("a", 1, "b", 2, "c", 3)
            val strings = mixed.filterIsInstance<String>()    // ["a", "b", "c"]
            
            // take - берёт первые N элементов
            val firstThree = numbers.take(3)                  // [1, 2, 3]
            
            // drop - пропускает первые N элементов
            val afterThree = numbers.drop(3)                  // [4, 5, 6, 7, 8, 9, 10]
            
            // takeWhile/dropWhile - по условию
            val whileLessThan5 = numbers.takeWhile { it < 5 } // [1, 2, 3, 4]
            ```

            ### Агрегация (sum, reduce, fold)

            ```kotlin
            val numbers = listOf(1, 2, 3, 4, 5)
            
            // sum - сумма (для числовых коллекций)
            val sum = numbers.sum()                           // 15
            
            // average - среднее
            val avg = numbers.average()                        // 3.0
            
            // minOrNull / maxOrNull
            val min = numbers.minOrNull()                      // 1
            val max = numbers.maxOrNull()                      // 5
            
            // count - подсчёт элементов
            val count = numbers.count()                        // 5
            val evenCount = numbers.count { it % 2 == 0 }      // 2
            
            // reduce - сворачивание слева направо
            val product = numbers.reduce { acc, i -> acc * i } // 120
            
            // fold - как reduce, но с начальным значением
            val sumWithInitial = numbers.fold(10) { acc, i -> acc + i } // 25
            ```

            ### Поиск (find, first, last)

            ```kotlin
            val numbers = listOf(1, 2, 3, 4, 5)
            
            // find - находит первый подходящий элемент (или null)
            val firstEven = numbers.find { it % 2 == 0 }      // 2
            
            // first/last - с проверкой или без
            val first = numbers.first()                        // 1
            val firstOrNull = numbers.firstOrNull { it > 10 } // null
            
            // elementAtOrNull - безопасный доступ по индексу
            val atIndex = numbers.elementAtOrNull(10)          // null
            ```

            ### Проверки (any, all, none)

            ```kotlin
            val numbers = listOf(1, 2, 3, 4, 5)
            
            // any - есть ли хотя бы один подходящий элемент
            val hasEven = numbers.any { it % 2 == 0 }         // true
            
            // all - все ли элементы удовлетворяют условию
            val allPositive = numbers.all { it > 0 }          // true
            
            // none - нет ли подходящих элементов
            val noNegative = numbers.none { it < 0 }          // true
            ```

            ### Группировка и сортировка

            ```kotlin
            val words = listOf("apple", "banana", "avocado", "blueberry")
            
            // groupBy - группировка по ключу
            val byFirstLetter = words.groupBy { it.first() }
            // {a=[apple, avocado], b=[banana, blueberry]}
            
            // groupingBy - для более сложной статистики
            val eachCount = words.groupingBy { it.first() }.eachCount()
            // {a=2, b=2}
            
            // sorted / sortedDescending
            val sorted = numbers.sorted()                      // по возрастанию
            val sortedDesc = numbers.sortedDescending()        // по убыванию
            
            // sortedBy / sortedByDescending
            val sortedByLength = words.sortedBy { it.length }
            // [apple, banana, avocado, blueberry]
            
            // reversed
            val reversed = numbers.reversed()                  // [5, 4, 3, 2, 1]
            ```

            ## ⚡ Последовательности (Sequences)

            Последовательности работают **лениво** — элементы обрабатываются по одному, а не вся коллекция целиком. Это эффективно для длинных цепочек операций.

            ```kotlin
            // Создание последовательности
            val sequence = sequenceOf(1, 2, 3, 4, 5)
            
            // Из коллекции
            val fromList = listOf(1, 2, 3).asSequence()
            
            // Генерация последовательности
            val generated = generateSequence(1) { it + 1 }
                .take(10)  // берём только первые 10
            
            // Пример: ленивая обработка
            val result = (1..1_000_000)
                .asSequence()
                .filter { it % 2 == 0 }
                .map { it * 2 }
                .take(10)
                .toList()  // только здесь происходят вычисления
            
            // Бесконечная последовательность
            val infinite = generateSequence(1) { it + 1 }
                .takeWhile { it < 100 }
                .toList()
            ```

            ### Сравнение: коллекции vs последовательности

            | Коллекции (жадные) | Последовательности (ленивые) |
            |-------------------|-------------------------------|
            | Сразу вычисляют все элементы | Вычисляют по требованию |
            | Память под всю коллекцию | Память под один элемент |
            | Лучше для небольших коллекций | Лучше для больших или бесконечных |
            | Каждая операция создаёт новую коллекцию | Операции цепляются без создания промежуточных коллекций |

            ## 🚀 Потоки (Flow) — асинхронные последовательности

            Flow — часть корутин для работы с асинхронными потоками данных.

            ```kotlin
            import kotlinx.coroutines.*
            import kotlinx.coroutines.flow.*
            
            suspend fun main() {
                // Создание Flow
                val flow = flow {
                    for (i in 1..5) {
                        delay(100)          // имитация асинхронной операции
                        emit(i)             // отправка значения
                    }
                }
                
                // Обработка Flow
                flow
                    .filter { it % 2 == 0 }
                    .map { it * 10 }
                    .collect { value ->
                        println("Получено: \${'$'}value")
                    }
            }
            ```

            ## 📋 Полезные функции-расширения

            ```kotlin
            val list = listOf(1, 2, 3, 2, 1)
            
            // distinct - уникальные элементы
            println(list.distinct())              // [1, 2, 3]
            
            // chunked - разбиение на части
            println(list.chunked(2))               // [[1, 2], [3, 2], [1]]
            
            // windowed - скользящее окно
            println(list.windowed(3))              // [[1, 2, 3], [2, 3, 2], [3, 2, 1]]
            
            // zip - объединение двух коллекций
            val list1 = listOf(1, 2, 3)
            val list2 = listOf("a", "b", "c")
            println(list1.zip(list2))              // [(1, a), (2, b), (3, c)]
            
            // associate - преобразование в Map
            val map = list.associate { it to it * 10 }
            // {1=10, 2=20, 3=30}
            ```

            ## 🎯 Практические советы

            1. **Используйте неизменяемые коллекции по умолчанию** (`listOf`, `setOf`, `mapOf`)
            2. **Переходите к изменяемым** (`mutableListOf`), когда действительно нужно менять
            3. **Для сложных цепочек операций используйте `asSequence()`**
            4. **Предпочитайте функциональный стиль** циклам с индексами
            5. **Используйте деструктуризацию** для работы с парами и тройками

            ```kotlin
            // Вместо циклов с индексами
            for (i in 0 until list.size) {
                println(list[i])
            }
            
            // Используйте forEach
            list.forEach { println(it) }
            
            // Или forEachIndexed, если нужен индекс
            // Или forEachIndexed, если нужен индекс
list.forEachIndexed { index, value ->
    println("[${'$'}index] = ${'$'}value")  // Здесь ${'$'}index и ${'$'}value - обычные переменные
}
            ```

            ## 📚 Резюме

            ✅ **List** — упорядоченные элементы с дубликатами  
            ✅ **Set** — уникальные элементы  
            ✅ **Map** — пары ключ-значение  
            ✅ **Функциональные операции** — map, filter, reduce, fold  
            ✅ **Последовательности** — ленивые вычисления для больших данных  
            ✅ **Flow** — асинхронные потоки  

            Коллекции — это инструмент, который вы будете использовать каждый день. Освойте их хорошо, и ваш код станет чище, безопаснее и эффективнее!
        """.trimIndent(),
        codeExample = """
            fun main() {
                println("🎯 Коллекции в Kotlin - демонстрация возможностей\n")
                
                // 1️⃣ Базовые операции с List
                println("1️⃣ List - базовые операции")
                val numbers = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                val names = mutableListOf("Анна", "Борис", "Владимир", "Дарья")
                
                println("Исходные числа: ${'$'}numbers")
                println("Исходные имена: ${'$'}names")
                println()
                
                // 2️⃣ Трансформации
                println("2️⃣ Трансформации")
                val squares = numbers.map { it * it }
                val cubes = numbers.map { it * it * it }
                val evenNumbers = numbers.filter { it % 2 == 0 }
                val oddNumbers = numbers.filterNot { it % 2 == 0 }
                
                println("Квадраты: ${'$'}squares")
                println("Кубы: ${'$'}cubes")
                println("Чётные: ${'$'}evenNumbers")
                println("Нечётные: ${'$'}oddNumbers")
                println()
                
                // 3️⃣ Агрегация
                println("3️⃣ Агрегация")
                val sum = numbers.sum()
                val average = numbers.average()
                val max = numbers.maxOrNull()
                val min = numbers.minOrNull()
                val product = numbers.reduce { acc, i -> acc * i }
                
                println("Сумма: ${'$'}sum")
                println("Среднее: ${'$'}average")
                println("Максимум: ${'$'}max")
                println("Минимум: ${'$'}min")
                println("Произведение (reduce): ${'$'}product")
                println()
                
                // 4️⃣ Группировка
                println("4️⃣ Группировка")
                val groupedByLength = names.groupBy { it.length }
                println("Имена по длине:")
                groupedByLength.forEach { (length, nameList) ->
                    println("  Длина ${'$'}length: ${'$'}nameList")
                }
                
                val byFirstLetter = names.groupBy { it.first() }
                println("\nИмена по первой букве:")
                byFirstLetter.forEach { (letter, nameList) ->
                    println("  '${'$'}letter': ${'$'}nameList")
                }
                println()
                
                // 5️⃣ Сортировка
                println("5️⃣ Сортировка")
                val sortedNames = names.sorted()
                val sortedByLength = names.sortedBy { it.length }
                val sortedDesc = names.sortedDescending()
                
                println("По алфавиту: ${'$'}sortedNames")
                println("По длине: ${'$'}sortedByLength")
                println("В обратном порядке: ${'$'}sortedDesc")
                println()
                
                // 6️⃣ Работа с Map
                println("6️⃣ Работа с Map")
                val userMap = mapOf(
                    "id" to 1,
                    "name" to "Алексей",
                    "age" to 30,
                    "city" to "Москва",
                    "profession" to "Разработчик"
                )
                
                println("Информация о пользователе:")
                userMap.forEach { (key, value) ->
                    println("  ${'$'}key: ${'$'}value")
                }
                
                // Доступ к значениям
                println("\nИмя: ${'$'}{userMap["name"]}")
                println("Возраст: ${'$'}{userMap["age"]}")
                println("Город: ${'$'}{userMap.getOrDefault("city", "не указан")}")
                println("Зарплата: ${'$'}{userMap.getOrDefault("salary", "не указана")}")
                println()
                
                // 7️⃣ Поиск и проверки
                println("7️⃣ Поиск и проверки")
                val hasLongName = names.any { it.length > 6 }
                val allNamesValid = names.all { it.isNotEmpty() }
                val noEmptyNames = names.none { it.isEmpty() }
                val firstLongName = names.find { it.length > 5 }
                val lastShortName = names.findLast { it.length < 6 }
                
                println("Есть ли имена длиннее 6 символов? ${'$'}hasLongName")
                println("Все имена непустые? ${'$'}allNamesValid")
                println("Нет пустых имён? ${'$'}noEmptyNames")
                println("Первое имя длиннее 5: ${'$'}{firstLongName ?: "не найдено"}")
                println("Последнее имя короче 6: ${'$'}{lastShortName ?: "не найдено"}")
                println()
                
                // 8️⃣ Изменяемые операции
                println("8️⃣ Изменяемые операции")
                println("Имена до изменений: ${'$'}names")
                
                names.add("Екатерина")
                names.add(2, "Григорий")
                names.remove("Борис")
                names[0] = "Анна Ивановна"
                
                println("Имена после изменений: ${'$'}names")
                println()
                
                // 9️⃣ Set - уникальные элементы
                println("9️⃣ Set - уникальные элементы")
                val set1 = setOf(1, 2, 3, 2, 1, 4, 5, 4)
                val set2 = setOf(4, 5, 6, 7, 8)
                
                println("set1: ${'$'}set1")  // дубликаты удалены
                println("set2: ${'$'}set2")
                println("Объединение: ${'$'}{set1 union set2}")
                println("Пересечение: ${'$'}{set1 intersect set2}")
                println("Разность (set1 - set2): ${'$'}{set1 subtract set2}")
                println()
                
                // 🔟 Функции высшего порядка
                println("🔟 Сложные операции")
                val numbers2 = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                
                // Цепочка операций
                val result = numbers2
                    .filter { it % 2 == 0 }
                    .map { it * it }
                    .take(3)
                    .reduce { acc, i -> acc + i }
                
                println("Чётные числа в квадрате, первые 3, сумма: ${'$'}result")
                
                // flatMap
                val expanded = numbers2.take(3).flatMap { listOf(it, it * 10) }
                println("flatMap пример: ${'$'}expanded")
                
                // windowed
                val windows = numbers2.windowed(3)
                println("Скользящее окно размером 3: ${'$'}windows")
                
                // chunked
                val chunks = numbers2.chunked(4)
                println("Разбиение на части по 4: ${'$'}chunks")
                println()
                
                // 1️⃣1️⃣ Последовательности (ленивые вычисления)
                println("1️⃣1️⃣ Последовательности")
                val largeRange = 1..1_000_000
                
                val time = measureTimeMillis {
                    val sequenceResult = largeRange.asSequence()
                        .filter { it % 2 == 0 }
                        .map { it * 2 }
                        .take(10)
                        .toList()
                    
                    println("Первые 10 чётных чисел, умноженных на 2: ${'$'}sequenceResult")
                }
                println("Время выполнения (последовательность): ${'$'}time мс")
                
                // Сравнение с обычной коллекцией
                val time2 = measureTimeMillis {
                    val listResult = largeRange.toList()
                        .filter { it % 2 == 0 }
                        .map { it * 2 }
                        .take(10)
                    
                    println("Тот же результат через коллекцию: ${'$'}listResult")
                }
                println("Время выполнения (коллекция): ${'$'}time2 мс")
                println()
                
                // 1️⃣2️⃣ Работа с null в коллекциях
                println("1️⃣2️⃣ Работа с null")
                val withNulls = listOf(1, null, 2, null, 3, null, 4)
                
                println("С null: ${'$'}withNulls")
                println("Без null: ${'$'}{withNulls.filterNotNull()}")
                println("Только числа > 2: ${'$'}{withNulls.filterNotNull().filter { it > 2 }}")
                
                // Использование mapNotNull
                val strings = listOf("1", "2", "a", "3", "b", "4")
                val numbers_only = strings.mapNotNull { it.toIntOrNull() }
                println("Только числа из строк: ${'$'}numbers_only")
            }
            
            // Вспомогательная функция для измерения времени
            fun measureTimeMillis(block: () -> Unit): Long {
                val start = System.currentTimeMillis()
                block()
                return System.currentTimeMillis() - start
            }
        """.trimIndent()
    )
}