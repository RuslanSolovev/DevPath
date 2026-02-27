package com.example.devpath.data.repository.lessons

import com.example.devpath.domain.models.Lesson

object Lesson02VariablesTypes {
    fun get(): Lesson = Lesson(
        id = "variables_types",
        title = "📦 Переменные и типы данных",
        description = "Всё о переменных, типах, null-безопасности и работе с данными в Kotlin",
        difficulty = "beginner",
        duration = 30,
        topic = "kotlin_basics",
        theory = """
            # 📦 Переменные и типы данных в Kotlin

            Kotlin — это **статически типизированный** язык, но благодаря **выводу типов** (type inference) вы редко указываете тип явно. Компилятор сам понимает, какой тип у переменной, на основе присвоенного значения.

            ## 🎯 Два мира переменных: `val` и `var`

            В Kotlin есть два ключевых слова для объявления переменных:

            | Ключевое слово | Описание | Аналог в Java |
            |----------------|----------|---------------|
            | `val` | Неизменяемая ссылка (read-only) | `final` |
            | `var` | Изменяемая ссылка | Обычная переменная |

            ```kotlin
            val name = "Kotlin"        // Нельзя изменить
            var version = 1.9           // Можно изменить
            
            version = 2.0               // ✅ OK
            // name = "Java"            // ❌ Ошибка компиляции!
            ```

            ### 🥇 Золотое правило: используйте `val` везде, где возможно!
            - Код становится безопаснее (нет неожиданных изменений)
            - Легче понимать и отлаживать
            - Потокобезопасность в многопоточных сценариях

            ## 🔍 Вывод типов (Type Inference)

            Kotlin умный — чаще всего тип можно не указывать:

            ```kotlin
            val message = "Hello"        // String
            val count = 42                // Int
            val pi = 3.14159               // Double
            val isReady = true             // Boolean
            val firstLetter = 'A'          // Char
            ```

            Но при желании можно указать явно:

            ```kotlin
            val username: String = "developer"
            val score: Int = 100
            val temperature: Double = 25.5
            val isComplete: Boolean = false
            ```

            ## 🧬 Иерархия типов в Kotlin

            ```
                            Any (корень иерархии)
                                  |
                  ┌───────────────┴───────────────┐
                  │                               │
              Типы-значения                  Ссылочные типы
              (примитивы)                    (String, классы)
                  │
            ┌─────┼─────┬─────┬─────┐
            Byte Short Int Long Float Double Boolean Char
            ```

            ### Числовые типы

            | Тип    | Размер (бит) | Минимальное значение | Максимальное значение | Пример |
            |--------|--------------|----------------------|------------------------|--------|
            | `Byte` | 8 | -128 | 127 | `val b: Byte = 127` |
            | `Short`| 16 | -32768 | 32767 | `val s: Short = 32767` |
            | `Int`  | 32 | -2³¹ | 2³¹-1 | `val i = 1_000_000` |
            | `Long` | 64 | -2⁶³ | 2⁶³-1 | `val l = 100L` |
            | `Float`| 32 | ~1.4e-45 | ~3.4e38 | `val f = 3.14f` |
            | `Double`| 64 | ~4.9e-324 | ~1.8e308 | `val d = 3.14159` |

            ```kotlin
            val byte: Byte = 127
            val short: Short = 32767
            val int = 100_000_000              // Подчёркивания для читаемости
            val long = 100L                     // Суффикс L
            val float = 3.14f                    // Суффикс f
            val double = 3.14159
            val hex = 0xFF                       // Шестнадцатеричное (255)
            val binary = 0b00001011               // Двоичное (11)
            ```

            ## 📝 Работа со строками (String)

            Строки в Kotlin — это последовательности символов в двойных кавычках.

            ### Обычные и многострочные строки

            ```kotlin
            val singleLine = "Обычная строка"
            
            val multiLine = \"\"\"
                Это пример
                многострочной
                строки
                Сохраняются все пробелы и переносы!
            \"\"\".trimIndent()  // Убирает общие отступы
            ```

            ### 🌟 Шаблоны строк (String Templates)

            Одна из самых крутых фич Kotlin — встраивание выражений прямо в строку:

            ```kotlin
            val name = "Мир"
            val greeting = "Привет, \${'$'}name!"              // Привет, Мир!
            
            val a = 10
            val b = 20
            val sum = "Сумма \${'$'}a и \${'$'}b = \${'$'}{a + b}"  // Сумма 10 и 20 = 30
            ```

            ### Полезные методы строк

            ```kotlin
            val text = "Kotlin Programming"
            
            println(text.length)              // 18
            println(text.uppercase())         // KOTLIN PROGRAMMING
            println(text.lowercase())          // kotlin programming
            println(text.startsWith("Kot"))    // true
            println(text.contains("Pro"))      // true
            println(text.substring(0, 6))      // Kotlin
            ```

            ## 📊 Массивы (Arrays)

            ```kotlin
            // Создание массивов
            val numbers = arrayOf(1, 2, 3, 4, 5)
            val strings = arrayOf("A", "B", "C")
            val mixed = arrayOf(1, "два", 3.0)      // Может содержать разные типы
            
            // Примитивные массивы (без автобоксинга, эффективнее)
            val intArray = intArrayOf(1, 2, 3)
            val doubleArray = doubleArrayOf(1.1, 2.2, 3.3)
            val booleanArray = booleanArrayOf(true, false, true)
            
            // Доступ к элементам
            println(numbers[0])        // 1
            println(numbers.get(1))     // 2
            
            // Изменение элементов
            numbers[2] = 30
            numbers.set(3, 40)
            
            // Размер массива
            println(numbers.size)       // 5
            ```

            ## 🛡️ Null Safety — гордость Kotlin

            Одна из главных причин популярности Kotlin — встроенная защита от `NullPointerException`.

            ### Nullable и Non-null типы

            ```kotlin
            // Non-null тип (не может быть null)
            var nonNull: String = "Всегда значение"
            // nonNull = null           // ❌ Ошибка компиляции!
            
            // Nullable тип (может быть null)
            var nullable: String? = "Может быть null"
            nullable = null              // ✅ OK
            ```

            ### Безопасный вызов (Safe Call) `?.`

            ```kotlin
            val length = nullable?.length  
            // Если nullable == null, то length == null
            // Если nullable != null, то length = nullable.length
            ```

            ### Оператор Элвиса (Elvis) `?:`

            ```kotlin
            val safeLength = nullable?.length ?: 0
            // Если nullable == null, вернёт 0
            // Иначе вернёт nullable.length
            ```

            ### Цепочка безопасных вызовов

            ```kotlin
            data class Address(val city: String?, val street: String?)
            data class User(val name: String, val address: Address?)
            
            val user: User? = User("Алексей", Address("Москва", null))
            
            val city = user?.address?.city ?: "город не указан"
            // Если user == null или address == null или city == null, вернётся "город не указан"
            ```

            ### Оператор `!!` — используйте с осторожностью!

            ```kotlin
            val forcedLength = nullable!!.length  
            // Если nullable == null → выбросит NullPointerException
            // Используйте только если вы **абсолютно уверены**, что значение не null!
            ```

            ### Безопасное приведение типов `as?`

            ```kotlin
            val obj: Any = "Hello"
            val str: String? = obj as? String      // "Hello"
            val num: Int? = obj as? Int             // null (без исключения!)
            ```

            ### Проверка на null в условиях

            ```kotlin
            val text: String? = "Kotlin"
            
            if (text != null) {
                // Здесь text автоматически преобразован в non-null тип!
                println(text.length)  // ✅ Безопасно!
            }
            
            // Аналогично с when
            when (text) {
                null -> println("text is null")
                else -> println("text length = ${'$'}{text.length}")
            }
            ```

            ## 🎨 Преобразование типов (Type Conversion)

            В Kotlin нет автоматического расширения типов, как в Java:

            ```kotlin
            val intNumber = 100
            // val longNumber: Long = intNumber  // ❌ Ошибка!
            val longNumber: Long = intNumber.toLong()  // ✅ Явное преобразование
            ```

            Все числовые типы имеют методы преобразования:
            - `toByte()`, `toShort()`, `toInt()`, `toLong()`
            - `toFloat()`, `toDouble()`
            - `toChar()`, `toString()`

            ```kotlin
            val a = 42
            val b = a.toDouble()     // 42.0
            val c = a.toFloat()       // 42.0f
            val d = a.toString()       // "42"
            
            val pi = 3.14159
            val intPi = pi.toInt()     // 3 (дробная часть отбрасывается)
            ```

            ## 📋 Резюме

            ✅ Используйте `val` для неизменяемых переменных, `var` — только когда нужно менять  
            ✅ Kotlin сам выводит типы — не пишите их без необходимости  
            ✅ Для чисел есть 6 типов: `Byte`, `Short`, `Int`, `Long`, `Float`, `Double`  
            ✅ Строки поддерживают многострочность и шаблоны  
            ✅ Массивы создаются через `arrayOf()` или `intArrayOf()`  
            ✅ Null-безопасность — используйте `?.` и `?:` вместо `!!`  
            ✅ Явно преобразуйте типы через `toInt()`, `toDouble()` и т.д.

            В следующем уроке мы изучим функции — основу функционального программирования в Kotlin!
        """.trimIndent(),
        codeExample = """
            fun main() {
                println("=== Переменные и типы данных в Kotlin ===\n")
                
                // 1️⃣ val vs var
                println("1️⃣ val vs var:")
                val language = "Kotlin"
                var version = 1.9
                println("Язык: ${'$'}language, версия: ${'$'}version")
                
                version = 2.0
                println("Обновлённая версия: ${'$'}version")
                // language = "Java" // ❌ Раскомментируйте - будет ошибка!
                
                // 2️⃣ Вывод типов
                println("\n2️⃣ Вывод типов:")
                val message = "Hello"        // String
                val count = 42                // Int
                val pi = 3.14159               // Double
                val isReady = true             // Boolean
                val letter = 'K'               // Char
                
                println("message: ${'$'}message (${'$'}{message::class.simpleName})")
                println("count: ${'$'}count (${'$'}{count::class.simpleName})")
                println("pi: ${'$'}pi (${'$'}{pi::class.simpleName})")
                
                // 3️⃣ Числовые типы
                println("\n3️⃣ Числовые типы:")
                val byteValue: Byte = 127
                val shortValue: Short = 32767
                val intValue = 100_000_000
                val longValue = 100L
                val floatValue = 3.14f
                val doubleValue = 3.14159
                val hexValue = 0xFF
                val binaryValue = 0b00001011
                
                println("byte: ${'$'}byteValue")
                println("int с подчёркиваниями: ${'$'}intValue")
                println("long: ${'$'}longValue")
                println("float: ${'$'}floatValue")
                println("double: ${'$'}doubleValue")
                println("hex (0xFF): ${'$'}hexValue")
                println("binary (0b1011): ${'$'}binaryValue")
                
                // 4️⃣ Строки и шаблоны
                println("\n4️⃣ Строки:")
                val name = "Мир"
                val greeting = "Привет, ${'$'}name!"
                val a = 10
                val b = 20
                val sum = "Сумма ${'$'}a и ${'$'}b = ${'$'}{a + b}"
                
                println(greeting)
                println(sum)
                
                val multiLine = \"\"\"
                    Это пример
                    многострочной
                    строки
                \"\"\".trimIndent()
                println("Многострочная строка:\\n${'$'}multiLine")
                
                // 5️⃣ Массивы
                println("\n5️⃣ Массивы:")
                val numbers = arrayOf(1, 2, 3, 4, 5)
                val intArray = intArrayOf(10, 20, 30, 40, 50)
                
                println("Первый элемент: ${'$'}{numbers[0]}")
                println("Размер массива: ${'$'}{numbers.size}")
                
                numbers[2] = 100
                println("После изменения: ${'$'}{numbers.joinToString()}")
                
                // 6️⃣ Null Safety
                println("\n6️⃣ Null Safety:")
                var nullable: String? = "Kotlin"
                println("Длина (безопасно): ${'$'}{nullable?.length ?: "null"}")
                
                nullable = null
                println("Длина после null: ${'$'}{nullable?.length ?: "null"}")
                
                // Безопасная цепочка
                data class Address(val city: String?, val street: String?)
                data class User(val name: String, val address: Address?)
                
                val user = User("Алексей", Address("Москва", null))
                val city = user.address?.city ?: "город не указан"
                println("Город пользователя: ${'$'}city")
                
                // 7️⃣ Преобразование типов
                println("\n7️⃣ Преобразование типов:")
                val intNumber = 42
                val doubleNumber = intNumber.toDouble()
                val stringNumber = intNumber.toString()
                
                println("Int: ${'$'}intNumber")
                println("Double: ${'$'}doubleNumber")
                println("String: ${'$'}stringNumber")
                
                val pi = 3.14159
                val intPi = pi.toInt()
                println("Double ${'$'}pi -> Int: ${'$'}intPi")
                
                // 8️⃣ Проверка типов
                println("\n8️⃣ Проверка типов:")
                val obj: Any = "Hello, Kotlin!"
                
                if (obj is String) {
                    println("obj это String, длина: ${'$'}{obj.length}")
                }
                
                val safeString = obj as? String
                val safeInt = obj as? Int
                println("safeString: ${'$'}safeString")
                println("safeInt: ${'$'}safeInt")  // null
            }
        """.trimIndent()
    )
}