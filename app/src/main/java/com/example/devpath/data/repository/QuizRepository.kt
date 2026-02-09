package com.example.devpath.data.repository

import com.example.devpath.domain.models.QuizQuestion

object QuizRepository {
    fun getQuizQuestions(): List<QuizQuestion> {
        return listOf(
            // === Оригинальные 20 вопросов (без изменений) ===
            QuizQuestion(
                id = "q1",
                question = "Какой тип переменной используется для неизменяемых значений в Kotlin?",
                options = listOf("var", "val", "const", "let"),
                correctAnswerIndex = 1,
                explanation = "Ключевое слово 'val' используется для объявления неизменяемых переменных.",
                topic = "kotlin_basics"
            ),
            QuizQuestion(
                id = "q2",
                question = "Какой тип данных используется для логических значений?",
                options = listOf("Integer", "Boolean", "String", "Double"),
                correctAnswerIndex = 1,
                explanation = "Тип Boolean может принимать значения true или false.",
                topic = "kotlin_basics"
            ),
            QuizQuestion(
                id = "q3",
                question = "Что выведет этот код: println(\"Kotlin\".length)?",
                options = listOf("6", "7", "8", "Ошибка компиляции"),
                correctAnswerIndex = 0,
                explanation = "Строка \"Kotlin\" содержит 6 символов. Метод length возвращает количество символов в строке.",
                topic = "strings"
            ),
            QuizQuestion(
                id = "q4",
                question = "Какой оператор используется для безопасного вызова nullable переменной?",
                options = listOf("!!", "?:", "?.", "as?"),
                correctAnswerIndex = 2,
                explanation = "Оператор ?. безопасно вызывает метод или свойство, возвращая null если переменная null.",
                topic = "null_safety"
            ),
            QuizQuestion(
                id = "q5",
                question = "Как объявить массив целых чисел в Kotlin?",
                options = listOf(
                    "Array<Int>()",
                    "int[] array = new int[5]",
                    "val array = arrayOf(1, 2, 3)",
                    "List<Int>()"
                ),
                correctAnswerIndex = 2,
                explanation = "Функция arrayOf() создаёт массив с заданными элементами.",
                topic = "collections"
            ),
            QuizQuestion(
                id = "q6",
                question = "Что делает ключевое слово 'when' в Kotlin?",
                options = listOf(
                    "Создаёт цикл",
                    "Объявляет функцию",
                    "Заменяет оператор switch/case",
                    "Обрабатывает исключения"
                ),
                correctAnswerIndex = 2,
                explanation = "when - это улучшенная версия оператора switch из других языков.",
                topic = "control_flow"
            ),
            QuizQuestion(
                id = "q7",
                question = "Как создать изменяемый список (MutableList) в Kotlin?",
                options = listOf(
                    "val list = listOf(1, 2, 3)",
                    "val list = mutableListOf(1, 2, 3)",
                    "val list = arrayListOf(1, 2, 3)",
                    "Варианты 2 и 3 верны"
                ),
                correctAnswerIndex = 3,
                explanation = "Обе функции mutableListOf() и arrayListOf() создают изменяемые списки.",
                topic = "collections"
            ),
            QuizQuestion(
                id = "q8",
                question = "Что выведет: println(10 / 3)?",
                options = listOf("3.333", "3", "3.0", "Ошибка"),
                correctAnswerIndex = 1,
                explanation = "При делении целых чисел результат будет целым числом. Для десятичного результата нужно использовать числа с плавающей точкой.",
                topic = "operators"
            ),
            QuizQuestion(
                id = "q9",
                question = "Как объявить функцию без возвращаемого значения?",
                options = listOf(
                    "fun myFunc() -> Unit",
                    "fun myFunc(): Unit",
                    "fun myFunc()",
                    "Варианты 2 и 3 верны"
                ),
                correctAnswerIndex = 3,
                explanation = "Unit - это аналог void в Kotlin. Его можно не указывать явно.",
                topic = "functions"
            ),
            QuizQuestion(
                id = "q10",
                question = "Что делает оператор '!!' (двойное восклицание)?",
                options = listOf(
                    "Безопасный вызов",
                    "Преобразование типа",
                    "Не-null утверждение",
                    "Логическое НЕ"
                ),
                correctAnswerIndex = 2,
                explanation = "Оператор !! утверждает, что значение не null. Если значение null, будет выброшено исключение.",
                topic = "null_safety"
            ),
            QuizQuestion(
                id = "q11",
                question = "Какой из этих циклов выполнится хотя бы один раз?",
                options = listOf("for", "while", "do-while", "forEach"),
                correctAnswerIndex = 2,
                explanation = "Цикл do-while сначала выполняет тело цикла, затем проверяет условие.",
                topic = "loops"
            ),
            QuizQuestion(
                id = "q12",
                question = "Как получить последний элемент списка?",
                options = listOf(
                    "list.getLast()",
                    "list.last()",
                    "list[list.size]",
                    "list[-1]"
                ),
                correctAnswerIndex = 1,
                explanation = "Функция last() возвращает последний элемент списка.",
                topic = "collections"
            ),
            QuizQuestion(
                id = "q13",
                question = "Что такое 'data class' в Kotlin?",
                options = listOf(
                    "Класс для работы с базами данных",
                    "Класс для хранения данных с автоматическими методами",
                    "Абстрактный класс",
                    "Класс для обработки данных"
                ),
                correctAnswerIndex = 1,
                explanation = "Data class автоматически генерирует методы toString(), equals(), hashCode() и copy().",
                topic = "classes"
            ),
            QuizQuestion(
                id = "q14",
                question = "Как объявить функцию с параметром по умолчанию?",
                options = listOf(
                    "fun greet(name = \"Гость\")",
                    "fun greet(name: String = \"Гость\")",
                    "fun greet(name: \"Гость\")",
                    "fun greet(name: String? = null)"
                ),
                correctAnswerIndex = 1,
                explanation = "Параметры по умолчанию указываются через знак равно после типа.",
                topic = "functions"
            ),
            QuizQuestion(
                id = "q15",
                question = "Что делает функция filter() для коллекций?",
                options = listOf(
                    "Сортирует элементы",
                    "Преобразует элементы",
                    "Фильтрует элементы по условию",
                    "Объединяет коллекции"
                ),
                correctAnswerIndex = 2,
                explanation = "filter() возвращает новую коллекцию с элементами, удовлетворяющими условию.",
                topic = "collections"
            ),
            QuizQuestion(
                id = "q16",
                question = "Как объявить переменную, которая может быть null?",
                options = listOf(
                    "val name: String",
                    "val name: String?",
                    "val name: String = null",
                    "val name = null"
                ),
                correctAnswerIndex = 1,
                explanation = "Знак вопроса после типа указывает, что переменная может быть null.",
                topic = "null_safety"
            ),
            QuizQuestion(
                id = "q17",
                question = "Что такое 'companion object'?",
                options = listOf(
                    "Объект-компаньон для статических методов",
                    "Вложенный класс",
                    "Анонимный класс",
                    "Синглтон"
                ),
                correctAnswerIndex = 0,
                explanation = "Companion object используется для создания статических методов и свойств в Kotlin.",
                topic = "classes"
            ),
            QuizQuestion(
                id = "q18",
                question = "Как преобразовать строку в целое число?",
                options = listOf(
                    "\"123\".toInt()",
                    "Integer.parseInt(\"123\")",
                    "Int.parse(\"123\")",
                    "Варианты 1 и 2 верны"
                ),
                correctAnswerIndex = 3,
                explanation = "В Kotlin можно использовать extension функцию toInt() или Java-метод parseInt().",
                topic = "type_conversion"
            ),
            QuizQuestion(
                id = "q19",
                question = "Что такое 'lambda' в Kotlin?",
                options = listOf(
                    "Анонимная функция",
                    "Именованная функция",
                    "Функция высшего порядка",
                    "Вложенная функция"
                ),
                correctAnswerIndex = 0,
                explanation = "Lambda - это анонимная функция, которую можно передавать как значение.",
                topic = "functions"
            ),
            QuizQuestion(
                id = "q20",
                question = "Какой оператор используется для проверки типа с безопасным приведением?",
                options = listOf("is", "as", "as?", "instanceof"),
                correctAnswerIndex = 2,
                explanation = "Оператор as? безопасно приводит тип, возвращая null если приведение невозможно.",
                topic = "type_checking"
            ),

            // === Новые вопросы (41–60) ===

            // kotlin_basics
            QuizQuestion(
                id = "q21",
                question = "Какой модификатор видимости по умолчанию используется в Kotlin?",
                options = listOf("public", "private", "internal", "protected"),
                correctAnswerIndex = 0,
                explanation = "По умолчанию все объявления в Kotlin имеют модификатор public.",
                topic = "kotlin_basics"
            ),
            QuizQuestion(
                id = "q22",
                question = "Какой тип используется для представления отсутствия значения?",
                options = listOf("Void", "Null", "Nothing", "Unit"),
                correctAnswerIndex = 3,
                explanation = "Unit используется как возвращаемый тип функций, которые ничего не возвращают (аналог void).",
                topic = "kotlin_basics"
            ),

            // strings
            QuizQuestion(
                id = "q23",
                question = "Какой символ используется для интерполяции строк в Kotlin?",
                options = listOf("$", "#", "@", "%"),
                correctAnswerIndex = 0,
                explanation = "Для подстановки переменных в строку используется символ $, например: (nane).",
                topic = "strings"
            ),
            QuizQuestion(
                id = "q24",
                question = "Какой метод проверяет, начинается ли строка с определённого префикса?",
                options = listOf("startsWith()", "hasPrefix()", "beginWith()", "prefix()"),
                correctAnswerIndex = 0,
                explanation = "Метод startsWith() возвращает true, если строка начинается с указанного префикса.",
                topic = "strings"
            ),

            // null_safety
            QuizQuestion(
                id = "q25",
                question = "Что делает оператор Elvis (?:)?",
                options = listOf(
                    "Выполняет безопасный вызов",
                    "Возвращает правое значение, если левое null",
                    "Утверждает не-null значение",
                    "Преобразует тип"
                ),
                correctAnswerIndex = 1,
                explanation = "Оператор ?: возвращает левое значение, если оно не null, иначе — правое.",
                topic = "null_safety"
            ),
            QuizQuestion(
                id = "q26",
                question = "Какой тип используется для гарантированно не-null значений?",
                options = listOf("NonNull<T>", "T!", "T", "NotNull<T>"),
                correctAnswerIndex = 2,
                explanation = "Обычный тип T означает, что значение не может быть null.",
                topic = "null_safety"
            ),

            // collections
            QuizQuestion(
                id = "q27",
                question = "Какой метод используется для получения уникальных элементов списка?",
                options = listOf("distinct()", "unique()", "filterUnique()", "deduplicate()"),
                correctAnswerIndex = 0,
                explanation = "Метод distinct() возвращает список без дубликатов.",
                topic = "collections"
            ),
            QuizQuestion(
                id = "q28",
                question = "Какой интерфейс представляет неизменяемый набор элементов?",
                options = listOf("List", "MutableList", "Set", "Map"),
                correctAnswerIndex = 2,
                explanation = "Set — это коллекция без дубликатов. По умолчанию неизменяема.",
                topic = "collections"
            ),
            QuizQuestion(
                id = "q29",
                question = "Какой метод применяет функцию ко всем элементам и возвращает новый список?",
                options = listOf("map()", "forEach()", "transform()", "apply()"),
                correctAnswerIndex = 0,
                explanation = "map() преобразует каждый элемент с помощью переданной функции.",
                topic = "collections"
            ),
            QuizQuestion(
                id = "q30",
                question = "Как проверить, содержит ли список определённый элемент?",
                options = listOf("has()", "contains()", "includes()", "exists()"),
                correctAnswerIndex = 1,
                explanation = "Метод contains() возвращает true, если элемент присутствует в коллекции.",
                topic = "collections"
            ),

            // functions
            QuizQuestion(
                id = "q31",
                question = "Что такое функция высшего порядка?",
                options = listOf(
                    "Функция, возвращающая Unit",
                    "Функция, принимающая или возвращающая другую функцию",
                    "Рекурсивная функция",
                    "Функция внутри класса"
                ),
                correctAnswerIndex = 1,
                explanation = "Функция высшего порядка работает с другими функциями как с параметрами или результатами.",
                topic = "functions"
            ),
            QuizQuestion(
                id = "q32",
                question = "Как объявить inline-функцию?",
                options = listOf(
                    "inline fun myFun()",
                    "fun inline myFun()",
                    "fun myFun() inline",
                    "myFun inline()"
                ),
                correctAnswerIndex = 0,
                explanation = "Ключевое слово inline ставится перед fun.",
                topic = "functions"
            ),

            // classes
            QuizQuestion(
                id = "q33",
                question = "Какой модификатор делает класс наследуемым?",
                options = listOf("open", "inheritable", "extendable", "public"),
                correctAnswerIndex = 0,
                explanation = "По умолчанию классы в Kotlin final. Чтобы разрешить наследование, нужно open.",
                topic = "classes"
            ),
            QuizQuestion(
                id = "q34",
                question = "Что такое primary constructor в Kotlin?",
                options = listOf(
                    "Конструктор, объявленный в теле класса",
                    "Конструктор, указанный после имени класса",
                    "Конструктор по умолчанию",
                    "Статический конструктор"
                ),
                correctAnswerIndex = 1,
                explanation = "Primary constructor объявляется прямо в заголовке класса.",
                topic = "classes"
            ),

            // control_flow
            QuizQuestion(
                id = "q35",
                question = "Можно ли использовать when без аргумента?",
                options = listOf("Нет", "Только в функциях", "Да", "Только с else"),
                correctAnswerIndex = 2,
                explanation = "when может использоваться как замена if-else цепочке без аргумента.",
                topic = "control_flow"
            ),
            QuizQuestion(
                id = "q36",
                question = "Какой оператор используется для немедленного выхода из цикла?",
                options = listOf("exit", "stop", "break", "return"),
                correctAnswerIndex = 2,
                explanation = "Оператор break прерывает выполнение цикла.",
                topic = "control_flow"
            ),

            // loops
            QuizQuestion(
                id = "q37",
                question = "Какой цикл лучше использовать для итерации по диапазону чисел?",
                options = listOf("while", "do-while", "for", "repeat"),
                correctAnswerIndex = 2,
                explanation = "Цикл for идеально подходит для итерации по диапазонам, например: for (i in 1..10).",
                topic = "loops"
            ),
            QuizQuestion(
                id = "q38",
                question = "Что делает функция repeat(n)?",
                options = listOf(
                    "Повторяет строку n раз",
                    "Выполняет блок кода n раз",
                    "Создаёт список из n элементов",
                    "Зацикливает выполнение"
                ),
                correctAnswerIndex = 1,
                explanation = "repeat(n) { ... } выполняет блок кода n раз.",
                topic = "loops"
            ),

            // operators
            QuizQuestion(
                id = "q39",
                question = "Какой оператор используется для объединения строк?",
                options = listOf("+", "&", "concat", "++"),
                correctAnswerIndex = 0,
                explanation = "Оператор + используется для конкатенации строк.",
                topic = "operators"
            ),
            QuizQuestion(
                id = "q40",
                question = "Что делает оператор in?",
                options = listOf(
                    "Проверяет принадлежность элемента к коллекции",
                    "Объявляет цикл",
                    "Присваивает значение",
                    "Сравнивает строки"
                ),
                correctAnswerIndex = 0,
                explanation = "Оператор in проверяет, содержится ли элемент в коллекции или диапазоне.",
                topic = "operators"
            ),

            // type_conversion
            QuizQuestion(
                id = "q41",
                question = "Как преобразовать Double в Int?",
                options = listOf(
                    "doubleValue.toInt()",
                    "(int) doubleValue",
                    "Int(doubleValue)",
                    "doubleValue.asInt()"
                ),
                correctAnswerIndex = 0,
                explanation = "В Kotlin используются extension-функции вроде toInt(), toDouble() и т.д.",
                topic = "type_conversion"
            ),
            QuizQuestion(
                id = "q42",
                question = "Что произойдёт при toInt() строки \"abc\"?",
                options = listOf(
                    "Вернёт 0",
                    "Вернёт null",
                    "Бросит исключение",
                    "Вернёт -1"
                ),
                correctAnswerIndex = 2,
                explanation = "toInt() бросает NumberFormatException, если строка не является числом.",
                topic = "type_conversion"
            ),

            // type_checking
            QuizQuestion(
                id = "q43",
                question = "Какой оператор проверяет, является ли объект экземпляром типа?",
                options = listOf("instanceof", "is", "as", "typeof"),
                correctAnswerIndex = 1,
                explanation = "Оператор is используется для проверки типа во время выполнения.",
                topic = "type_checking"
            ),
            QuizQuestion(
                id = "q44",
                question = "Что делает smart cast в Kotlin?",
                options = listOf(
                    "Автоматически приводит тип после проверки is",
                    "Преобразует строку в число",
                    "Создаёт копию объекта",
                    "Проверяет null"
                ),
                correctAnswerIndex = 0,
                explanation = "После проверки if (obj is String), obj автоматически становится String внутри блока.",
                topic = "type_checking"
            ),

            // coroutines
            QuizQuestion(
                id = "q45",
                question = "Какой builder запускает корутину и блокирует текущий поток до завершения?",
                options = listOf("launch", "async", "runBlocking", "withContext"),
                correctAnswerIndex = 2,
                explanation = "runBlocking используется в основном для тестов или main-функций.",
                topic = "coroutines"
            ),
            QuizQuestion(
                id = "q46",
                question = "Какой диспетчер используется для выполнения кода в основном потоке Android?",
                options = listOf("Dispatchers.Main", "Dispatchers.IO", "Dispatchers.Default", "Dispatchers.UI"),
                correctAnswerIndex = 0,
                explanation = "Dispatchers.Main обеспечивает выполнение в UI-потоке на Android.",
                topic = "coroutines"
            ),
            QuizQuestion(
                id = "q47",
                question = "Что такое suspend-функция?",
                options = listOf(
                    "Функция, которая останавливает программу",
                    "Функция, которую можно приостановить и возобновить",
                    "Функция без параметров",
                    "Функция, работающая в фоне"
                ),
                correctAnswerIndex = 1,
                explanation = "Suspend-функции могут приостанавливать выполнение корутины без блокировки потока.",
                topic = "coroutines"
            ),
            QuizQuestion(
                id = "q48",
                question = "Какой builder возвращает Deferred<T>?",
                options = listOf("launch", "async", "produce", "actor"),
                correctAnswerIndex = 1,
                explanation = "async возвращает Deferred, который можно await() для получения результата.",
                topic = "coroutines"
            ),

            // android_basics
            QuizQuestion(
                id = "q49",
                question = "Какой компонент отвечает за UI в Android?",
                options = listOf("Service", "Activity", "BroadcastReceiver", "ContentProvider"),
                correctAnswerIndex = 1,
                explanation = "Activity — это компонент, представляющий один экран с UI.",
                topic = "android_basics"
            ),
            QuizQuestion(
                id = "q50",
                question = "Где объявляются разрешения (permissions) в Android?",
                options = listOf(
                    "build.gradle",
                    "MainActivity.kt",
                    "AndroidManifest.xml",
                    "strings.xml"
                ),
                correctAnswerIndex = 2,
                explanation = "Все разрешения объявляются в файле AndroidManifest.xml.",
                topic = "android_basics"
            ),

            // jetpack_compose
            QuizQuestion(
                id = "q51",
                question = "Какой аннотацией помечаются composable-функции?",
                options = listOf("@Composable", "@UI", "@Function", "@View"),
                correctAnswerIndex = 0,
                explanation = "@Composable — это аннотация для функций, которые описывают UI в Jetpack Compose.",
                topic = "jetpack_compose"
            ),
            QuizQuestion(
                id = "q52",
                question = "Что такое State в Jetpack Compose?",
                options = listOf(
                    "Переменная для хранения данных",
                    "Специальный тип для управления изменяемыми данными в UI",
                    "Класс Activity",
                    "Тип View"
                ),
                correctAnswerIndex = 1,
                explanation = "State позволяет Compose отслеживать изменения и перерисовывать UI при их изменении.",
                topic = "jetpack_compose"
            ),

            // dependency_injection
            QuizQuestion(
                id = "q53",
                question = "Какой фреймворк DI официально поддерживается Google для Android?",
                options = listOf("Dagger", "Koin", "Hilt", "Toothpick"),
                correctAnswerIndex = 2,
                explanation = "Hilt — это DI-фреймворк от Google, основанный на Dagger, но упрощённый для Android.",
                topic = "dependency_injection"
            ),
            QuizQuestion(
                id = "q54",
                question = "Что делает аннотация @Inject в Hilt?",
                options = listOf(
                    "Помечает класс как зависимость",
                    "Указывает, где внедрять зависимости",
                    "Объявляет модуль",
                    "Создаёт синглтон"
                ),
                correctAnswerIndex = 1,
                explanation = "@Inject указывает, куда и какие зависимости нужно внедрить (в конструктор, поле и т.д.).",
                topic = "dependency_injection"
            ),

            // architecture
            QuizQuestion(
                id = "q55",
                question = "Какой компонент Architecture Components хранит данные, связанные с UI?",
                options = listOf("LiveData", "ViewModel", "Room", "Repository"),
                correctAnswerIndex = 1,
                explanation = "ViewModel предназначен для хранения и управления данными, связанными с UI.",
                topic = "architecture"
            ),
            QuizQuestion(
                id = "q56",
                question = "Что такое Repository pattern?",
                options = listOf(
                    "Шаблон для работы с сетью",
                    "Слой абстракции между источниками данных и ViewModel",
                    "База данных",
                    "Класс для хранения изображений"
                ),
                correctAnswerIndex = 1,
                explanation = "Repository объединяет несколько источников данных (сеть, БД) и предоставляет единый API.",
                topic = "architecture"
            ),

            // testing
            QuizQuestion(
                id = "q57",
                question = "Какой фреймворк используется для unit-тестов в Kotlin?",
                options = listOf("JUnit", "Espresso", "Mockito", "TestNG"),
                correctAnswerIndex = 0,
                explanation = "JUnit — стандартный фреймворк для unit-тестов на JVM.",
                topic = "testing"
            ),
            QuizQuestion(
                id = "q58",
                question = "Какой инструмент используется для UI-тестов на Android?",
                options = listOf("JUnit", "MockK", "Espresso", "Robolectric"),
                correctAnswerIndex = 2,
                explanation = "Espresso — фреймворк от Google для написания UI-тестов на Android.",
                topic = "testing"
            ),

            // advanced_kotlin
            QuizQuestion(
                id = "q59",
                question = "Что такое sealed class?",
                options = listOf(
                    "Класс с закрытым конструктором",
                    "Класс, ограничивающий иерархию наследников",
                    "Абстрактный класс",
                    "Класс без методов"
                ),
                correctAnswerIndex = 1,
                explanation = "Sealed class позволяет точно знать все возможные подклассы при использовании when.",
                topic = "advanced_kotlin"
            ),
            QuizQuestion(
                id = "q60",
                question = "Какой оператор используется для расширения функциональности класса без наследования?",
                options = listOf("extension", "extend", "infix", "operator"),
                correctAnswerIndex = 0,
                explanation = "Extension functions позволяют добавлять функции к существующим классам.",
                topic = "advanced_kotlin"
            ),
            // === Дополнительные вопросы (61–100) ===

            // coroutines
            QuizQuestion(
                id = "q61",
                question = "Какой оператор используется для отмены корутины?",
                options = listOf("cancel()", "stop()", "abort()", "terminate()"),
                correctAnswerIndex = 0,
                explanation = "Job.cancel() или CoroutineScope.cancel() отменяют выполнение корутины.",
                topic = "coroutines"
            ),
            QuizQuestion(
                id = "q62",
                question = "Что произойдёт, если вызвать suspend-функцию вне корутины?",
                options = listOf(
                    "Компилятор выдаст ошибку",
                    "Программа упадёт во время выполнения",
                    "Функция выполнится синхронно",
                    "Ничего не произойдёт"
                ),
                correctAnswerIndex = 0,
                explanation = "Suspend-функции можно вызывать только из других suspend-функций или корутин.",
                topic = "coroutines"
            ),

            // flow
            QuizQuestion(
                id = "q63",
                question = "Какой тип представляет холодный асинхронный поток значений в Kotlin?",
                options = listOf("LiveData", "Flow", "Channel", "StateFlow"),
                correctAnswerIndex = 1,
                explanation = "Flow — это холодный поток, который генерирует значения по запросу и не держит состояние.",
                topic = "flow"
            ),
            QuizQuestion(
                id = "q64",
                question = "Какой оператор Flow используется для обработки каждого элемента?",
                options = listOf("map", "collect", "onEach", "transform"),
                correctAnswerIndex = 2,
                explanation = "onEach позволяет выполнять побочные эффекты (например, логирование) для каждого элемента.",
                topic = "flow"
            ),

            // room
            QuizQuestion(
                id = "q65",
                question = "Какой аннотацией помечается класс сущности в Room?",
                options = listOf("@Entity", "@Table", "@Data", "@Model"),
                correctAnswerIndex = 0,
                explanation = "@Entity указывает, что класс представляет таблицу в базе данных Room.",
                topic = "room"
            ),
            QuizQuestion(
                id = "q66",
                question = "Как объявить DAO в Room?",
                options = listOf(
                    "class UserDao : Dao()",
                    "interface UserDao { @Query(...) }",
                    "object UserDao { ... }",
                    "abstract class UserDao"
                ),
                correctAnswerIndex = 1,
                explanation = "DAO в Room обычно объявляется как interface или abstract class с аннотациями запросов.",
                topic = "room"
            ),
            QuizQuestion(
                id = "q67",
                question = "Какой тип возвращаемого значения позволяет Room наблюдать за изменениями в БД?",
                options = listOf("List<User>", "LiveData<List<User>>", "Array<User>", "User?"),
                correctAnswerIndex = 1,
                explanation = "Room поддерживает LiveData и Flow для реактивного обновления UI при изменении данных.",
                topic = "room"
            ),

            // navigation
            QuizQuestion(
                id = "q68",
                question = "Какой компонент Jetpack отвечает за навигацию между экранами в Compose?",
                options = listOf(
                    "NavController",
                    "NavigationGraph",
                    "NavHost",
                    "Варианты 1 и 3 верны"
                ),
                correctAnswerIndex = 3,
                explanation = "NavController управляет навигацией, а NavHost определяет граф маршрутов в Compose.",
                topic = "navigation"
            ),
            QuizQuestion(
                id = "q69",
                question = "Как передать аргумент в destination через NavController?",
                options = listOf(
                    "navController.navigate(\"profile/123\")",
                    "navController.push(\"profile\", id=123)",
                    "navController.goTo(ProfileScreen(123))",
                    "navController.route(\"profile?id=123\")"
                ),
                correctAnswerIndex = 0,
                explanation = "Аргументы передаются через строку маршрута, например: \"profile/{userId}\".",
                topic = "navigation"
            ),

            // lifecycle
            QuizQuestion(
                id = "q70",
                question = "Какой класс позволяет выполнять действия при изменении жизненного цикла Activity?",
                options = listOf("LifecycleObserver", "LifeWatcher", "ActivityMonitor", "StateHandler"),
                correctAnswerIndex = 0,
                explanation = "LifecycleObserver получает события жизненного цикла (ON_CREATE, ON_RESUME и т.д.).",
                topic = "lifecycle"
            ),
            QuizQuestion(
                id = "q71",
                question = "Что такое LifecycleOwner?",
                options = listOf(
                    "Класс, который создаёт Activity",
                    "Объект, имеющий Lifecycle (например, Activity или Fragment)",
                    "Синглтон для управления жизненным циклом",
                    "Интерфейс для ViewModel"
                ),
                correctAnswerIndex = 1,
                explanation = "Activity и Fragment реализуют LifecycleOwner, чтобы предоставлять свой Lifecycle другим компонентам.",
                topic = "lifecycle"
            ),

            // jetpack_compose
            QuizQuestion(
                id = "q72",
                question = "Какой модификатор используется для обработки кликов в Compose?",
                options = listOf("clickable", "onClick", "tap", "gesture"),
                correctAnswerIndex = 0,
                explanation = "Modifier.clickable { } делает элемент кликабельным.",
                topic = "jetpack_compose"
            ),
            QuizQuestion(
                id = "q73",
                question = "Что такое recomposition в Jetpack Compose?",
                options = listOf(
                    "Повторная компиляция кода",
                    "Перерисовка UI при изменении State",
                    "Создание нового Activity",
                    "Очистка памяти"
                ),
                correctAnswerIndex = 1,
                explanation = "Recomposition — это процесс перерисовки только тех частей UI, которые зависят от изменившегося состояния.",
                topic = "jetpack_compose"
            ),

            // android_basics
            QuizQuestion(
                id = "q74",
                question = "Какой метод вызывается при создании Activity?",
                options = listOf("onStart()", "onCreate()", "onResume()", "onInit()"),
                correctAnswerIndex = 1,
                explanation = "onCreate() — первый метод жизненного цикла Activity, где инициализируется UI.",
                topic = "android_basics"
            ),
            QuizQuestion(
                id = "q75",
                question = "Где хранятся строковые ресурсы в Android-проекте?",
                options = listOf("values/strings.xml", "res/strings.xml", "assets/strings.txt", "main/strings.kt"),
                correctAnswerIndex = 0,
                explanation = "Строки хранятся в res/values/strings.xml для поддержки локализации.",
                topic = "android_basics"
            ),

            // permissions
            QuizQuestion(
                id = "q76",
                question = "Как запросить разрешение на запись в Android 13+?",
                options = listOf(
                    "WRITE_EXTERNAL_STORAGE",
                    "MANAGE_EXTERNAL_STORAGE",
                    "READ_MEDIA_IMAGES / WRITE_MEDIA_IMAGES",
                    "STORAGE_ACCESS"
                ),
                correctAnswerIndex = 2,
                explanation = "Начиная с Android 13, используются более гранулярные разрешения: READ_MEDIA_*.",
                topic = "permissions"
            ),
            QuizQuestion(
                id = "q77",
                question = "Какой метод используется для запроса разрешения во время выполнения?",
                options = listOf(
                    "requestPermissions()",
                    "ActivityCompat.requestPermissions()",
                    "Context.requestPermission()",
                    "Варианты 1 и 2 верны"
                ),
                correctAnswerIndex = 3,
                explanation = "На Java/Kotlin используют ActivityCompat.requestPermissions() или Activity.requestPermissions().",
                topic = "permissions"
            ),

            // gradle
            QuizQuestion(
                id = "q78",
                question = "В каком файле настраивается зависимость от библиотеки в Android?",
                options = listOf(
                    "build.gradle (Module: app)",
                    "settings.gradle",
                    "gradle.properties",
                    "AndroidManifest.xml"
                ),
                correctAnswerIndex = 0,
                explanation = "Зависимости добавляются в build.gradle уровня модуля (обычно app).",
                topic = "gradle"
            ),
            QuizQuestion(
                id = "q79",
                question = "Что означает 'implementation' в Gradle?",
                options = listOf(
                    "Библиотека видна только в этом модуле",
                    "Библиотека экспортируется в другие модули",
                    "Библиотека загружается во время выполнения",
                    "Это устаревший способ подключения"
                ),
                correctAnswerIndex = 0,
                explanation = "implementation скрывает зависимости от других модулей, ускоряя сборку.",
                topic = "gradle"
            ),

            // serialization
            QuizQuestion(
                id = "q80",
                question = "Какой аннотацией помечается класс для сериализации в kotlinx.serialization?",
                options = listOf("@Serializable", "@JsonClass", "@Data", "@Encode"),
                correctAnswerIndex = 0,
                explanation = "@Serializable автоматически генерирует сериализатор для класса.",
                topic = "serialization"
            ),
            QuizQuestion(
                id = "q81",
                question = "Какой формат поддерживается kotlinx.serialization по умолчанию?",
                options = listOf("XML", "JSON", "Protobuf", "YAML"),
                correctAnswerIndex = 1,
                explanation = "JSON — основной формат, но можно подключить другие (например, Protobuf).",
                topic = "serialization"
            ),

            // multithreading
            QuizQuestion(
                id = "q82",
                question = "Какой класс в Kotlin обеспечивает потокобезопасность для изменяемого состояния?",
                options = listOf("Mutex", "synchronized", "ThreadSafe", "Lock"),
                correctAnswerIndex = 0,
                explanation = "Mutex — это корутинный аналог synchronized, но без блокировки потока.",
                topic = "multithreading"
            ),
            QuizQuestion(
                id = "q83",
                question = "Можно ли обновлять UI из фонового потока в Android?",
                options = listOf("Да", "Нет", "Только через Handler", "Только в эмуляторе"),
                correctAnswerIndex = 1,
                explanation = "UI можно обновлять только из главного (UI) потока. Иначе — исключение.",
                topic = "multithreading"
            ),

            // collections
            QuizQuestion(
                id = "q84",
                question = "Какой метод коллекции возвращает сумму всех элементов?",
                options = listOf("sum()", "total()", "reduce()", "aggregate()"),
                correctAnswerIndex = 0,
                explanation = "sum() работает с числами и возвращает их сумму.",
                topic = "collections"
            ),
            QuizQuestion(
                id = "q85",
                question = "Что делает функция groupBy()?",
                options = listOf(
                    "Сортирует элементы",
                    "Группирует элементы по ключу",
                    "Удаляет дубликаты",
                    "Разбивает список на части"
                ),
                correctAnswerIndex = 1,
                explanation = "groupBy { it.firstLetter } создаёт Map<Char, List<String>>.",
                topic = "collections"
            ),

            // advanced_kotlin
            QuizQuestion(
                id = "q86",
                question = "Что такое inline-класс в Kotlin?",
                options = listOf(
                    "Класс, встроенный в другой",
                    "Класс, который упаковывает значение без накладных расходов",
                    "Класс с inline-функциями",
                    "Устаревшая фича"
                ),
                correctAnswerIndex = 1,
                explanation = "Inline-классы (value classes) оборачивают значение без создания объекта в рантайме (оптимизация).",
                topic = "advanced_kotlin"
            ),
            QuizQuestion(
                id = "q87",
                question = "Какой оператор используется для объявления infix-функции?",
                options = listOf("infix", "operator", "fun infix", "prefix"),
                correctAnswerIndex = 0,
                explanation = "infix fun Int.plus(other: Int) = this + other позволяет писать: 5 plus 3.",
                topic = "advanced_kotlin"
            ),

            // architecture
            QuizQuestion(
                id = "q88",
                question = "Какой принцип лежит в основе Clean Architecture?",
                options = listOf(
                    "Зависимости направлены внутрь",
                    "Все данные хранятся в ViewModel",
                    "Использование только Room",
                    "Максимальное количество слоёв"
                ),
                correctAnswerIndex = 0,
                explanation = "Внешние слои (UI, Data) зависят от внутренних (Domain), но не наоборот.",
                topic = "architecture"
            ),
            QuizQuestion(
                id = "q89",
                question = "Где должна находиться бизнес-логика в MVVM?",
                options = listOf(
                    "В Activity",
                    "В ViewModel",
                    "В UseCase / Interactor",
                    "В Repository"
                ),
                correctAnswerIndex = 2,
                explanation = "Бизнес-логика принадлежит Domain-слою (UseCase), а не ViewModel.",
                topic = "architecture"
            ),

            // testing
            QuizQuestion(
                id = "q90",
                question = "Какой фреймворк используется для мокирования в Kotlin?",
                options = listOf("Mockito", "MockK", "FakeIt", "Stubber"),
                correctAnswerIndex = 1,
                explanation = "MockK — нативный Kotlin-фреймворк для моков с поддержкой корутин и suspend-функций.",
                topic = "testing"
            ),
            QuizQuestion(
                id = "q91",
                question = "Что проверяет интеграционный тест?",
                options = listOf(
                    "Отдельную функцию",
                    "Взаимодействие нескольких компонентов",
                    "UI-элемент",
                    "Производительность"
                ),
                correctAnswerIndex = 1,
                explanation = "Интеграционный тест проверяет, как компоненты работают вместе (например, Repository + API).",
                topic = "testing"
            ),

            // android_basics
            QuizQuestion(
                id = "q92",
                question = "Какой контейнер используется для хранения пар «ключ-значение» в Android?",
                options = listOf("SharedPreferences", "KeyValueStore", "LocalCache", "DataStore"),
                correctAnswerIndex = 0,
                explanation = "SharedPreferences — простой способ хранить примитивы. DataStore — современная замена.",
                topic = "android_basics"
            ),

            // jetpack_compose
            QuizQuestion(
                id = "q93",
                question = "Какой комposable используется для отображения списка с прокруткой?",
                options = listOf("Column", "LazyColumn", "ScrollView", "RecyclerView"),
                correctAnswerIndex = 1,
                explanation = "LazyColumn эффективно отображает большие списки, создавая только видимые элементы.",
                topic = "jetpack_compose"
            ),

            // coroutines
            QuizQuestion(
                id = "q94",
                question = "Что делает withContext(Dispatchers.IO)?",
                options = listOf(
                    "Запускает новую корутину",
                    "Меняет диспетчер текущей корутины",
                    "Отменяет корутину",
                    "Создаёт новый поток"
                ),
                correctAnswerIndex = 1,
                explanation = "withContext переключает контекст выполнения (например, на IO для работы с диском/сетью).",
                topic = "coroutines"
            ),

            // room
            QuizQuestion(
                id = "q95",
                question = "Как объявить первичный ключ в Entity Room?",
                options = listOf(
                    "@PrimaryKey val id: Int",
                    "@Id val id: Int",
                    "primary key id: Int",
                    "val id: Int = autoIncrement()"
                ),
                correctAnswerIndex = 0,
                explanation = "@PrimaryKey указывает, какое поле является первичным ключом.",
                topic = "room"
            ),

            // navigation
            QuizQuestion(
                id = "q96",
                question = "Как вернуться назад в Navigation Compose?",
                options = listOf(
                    "navController.popBackStack()",
                    "navController.goBack()",
                    "finish()",
                    "navController.backStack.pop()"
                ),
                correctAnswerIndex = 0,
                explanation = "popBackStack() удаляет текущий destination из стека и возвращает к предыдущему.",
                topic = "navigation"
            ),

            // flow
            QuizQuestion(
                id = "q97",
                question = "Как преобразовать Flow в StateFlow?",
                options = listOf(
                    "flow.asStateFlow()",
                    "stateIn(scope, SharingStarted, initialValue)",
                    "flow.toStateFlow()",
                    "StateFlow(flow)"
                ),
                correctAnswerIndex = 1,
                explanation = "stateIn создаёт StateFlow из Flow с заданным начальным значением и стратегией запуска.",
                topic = "flow"
            ),

            // serialization
            QuizQuestion(
                id = "q98",
                question = "Как десериализовать JSON в объект с kotlinx.serialization?",
                options = listOf(
                    "Json.decodeFromString<MyClass>(json)",
                    "MyClass.fromJson(json)",
                    "Gson().fromJson(json, MyClass::class)",
                    "json.toObject(MyClass::class)"
                ),
                correctAnswerIndex = 0,
                explanation = "Json.decodeFromString<T>() — стандартный способ десериализации.",
                topic = "serialization"
            ),

            // gradle
            QuizQuestion(
                id = "q99",
                question = "Какой плагин применяется для сборки Android-приложения?",
                options = listOf(
                    "kotlin(\"jvm\")",
                    "com.android.application",
                    "android.library",
                    "google.gms"
                ),
                correctAnswerIndex = 1,
                explanation = "com.android.application применяется в build.gradle для сборки APK/AAB.",
                topic = "gradle"
            ),

            // advanced_kotlin
            QuizQuestion(
                id = "q100",
                question = "Что такое контракт в Kotlin?",
                options = listOf(
                    "Договор между интерфейсами",
                    "Механизм для улучшения smart cast",
                    "Способ описания API",
                    "Устаревшая фича"
                ),
                correctAnswerIndex = 1,
                explanation = "Контракты (experimental) позволяют компилятору лучше понимать логику функций (например, ensures { this != null }).",
                topic = "advanced_kotlin"
            ),
            // === Дополнительные вопросы по недостающим темам ===

// 1. variables_types (Переменные и типы)
            QuizQuestion(
                id = "q101",
                question = "Какой тип переменной используется для изменяемых значений в Kotlin?",
                options = listOf("val", "var", "const", "final"),
                correctAnswerIndex = 1,
                explanation = "Ключевое слово 'var' используется для объявления изменяемых переменных.",
                topic = "variables_types"
            ),
            QuizQuestion(
                id = "q102",
                question = "Как объявить переменную с явным указанием типа Float?",
                options = listOf(
                    "val num: Float = 3.14",
                    "val num = 3.14f",
                    "val num: Float = 3.14f",
                    "val num = Float(3.14)"
                ),
                correctAnswerIndex = 2,
                explanation = "Для Float нужно явно указать тип и добавить суффикс 'f' к значению.",
                topic = "variables_types"
            ),
            QuizQuestion(
                id = "q103",
                question = "Что такое тип вывода (type inference) в Kotlin?",
                options = listOf(
                    "Явное указание типа переменной",
                    "Автоматическое определение типа компилятором",
                    "Преобразование типов во время выполнения",
                    "Проверка типов в IDE"
                ),
                correctAnswerIndex = 1,
                explanation = "Kotlin может автоматически определить тип переменной на основе присваиваемого значения.",
                topic = "variables_types"
            ),
            QuizQuestion(
                id = "q104",
                question = "Какой тип данных используется для хранения больших целых чисел?",
                options = listOf("Int", "Long", "BigInteger", "Double"),
                correctAnswerIndex = 1,
                explanation = "Long используется для больших целых чисел (до 2^63-1), а для очень больших - BigInteger.",
                topic = "variables_types"
            ),
            QuizQuestion(
                id = "q105",
                question = "Как объявить константу времени компиляции в Kotlin?",
                options = listOf(
                    "val PI = 3.14159",
                    "const val PI = 3.14159",
                    "static val PI = 3.14159",
                    "final val PI = 3.14159"
                ),
                correctAnswerIndex = 1,
                explanation = "const val объявляет константу, известную во время компиляции. Можно использовать только с примитивами и String.",
                topic = "variables_types"
            ),

// 2. delegation (Делегирование)
            QuizQuestion(
                id = "q106",
                question = "Что такое делегирование в Kotlin?",
                options = listOf(
                    "Передача выполнения метода другому объекту",
                    "Наследование от нескольких классов",
                    "Создание интерфейса",
                    "Объявление абстрактного класса"
                ),
                correctAnswerIndex = 0,
                explanation = "Делегирование позволяет передать выполнение методов другому объекту, используя ключевое слово 'by'.",
                topic = "delegation"
            ),
            QuizQuestion(
                id = "q107",
                question = "Какой стандартный делегат используется для отложенной инициализации?",
                options = listOf("observable", "vetoable", "lazy", "notNull"),
                correctAnswerIndex = 2,
                explanation = "Делегат lazy инициализирует значение только при первом обращении.",
                topic = "delegation"
            ),
            QuizQuestion(
                id = "q108",
                question = "Что делает делегат observable?",
                options = listOf(
                    "Запрещает изменение значения",
                    "Отслеживает изменения значения",
                    "Откладывает инициализацию",
                    "Проверяет значение на null"
                ),
                correctAnswerIndex = 1,
                explanation = "Observable позволяет выполнять код при каждом изменении значения свойства.",
                topic = "delegation"
            ),
            QuizQuestion(
                id = "q109",
                question = "Какой оператор используется для делегирования реализации интерфейса?",
                options = listOf("with", "by", "delegate", "as"),
                correctAnswerIndex = 1,
                explanation = "Ключевое слово 'by' используется для делегирования реализации интерфейса другому объекту.",
                topic = "delegation"
            ),
            QuizQuestion(
                id = "q110",
                question = "Что такое делегирование свойств (property delegation)?",
                options = listOf(
                    "Назначение свойств объекту",
                    "Автоматическая генерация getter/setter",
                    "Передача управления доступом к свойству другому объекту",
                    "Создание вычисляемых свойств"
                ),
                correctAnswerIndex = 2,
                explanation = "Делегирование свойств позволяет передать управление доступом к свойству другому объекту.",
                topic = "delegation"
            ),

// 3. extensions (Расширения)
            QuizQuestion(
                id = "q111",
                question = "Что такое extension-функция в Kotlin?",
                options = listOf(
                    "Функция, которая расширяет интерфейс",
                    "Функция, добавляемая к существующему классу без наследования",
                    "Функция с переменным числом параметров",
                    "Встроенная функция"
                ),
                correctAnswerIndex = 1,
                explanation = "Extension-функции позволяют добавлять новые функции к существующим классам без их модификации.",
                topic = "extensions"
            ),
            QuizQuestion(
                id = "q112",
                question = "Как объявить extension-функцию для класса String?",
                options = listOf(
                    "fun String.customFunction() { ... }",
                    "function String.customFunction() { ... }",
                    "extension fun String.customFunction() { ... }",
                    "String.fun customFunction() { ... }"
                ),
                correctAnswerIndex = 0,
                explanation = "Extension-функция объявляется с указанием класса-приемника перед именем функции.",
                topic = "extensions"
            ),
            QuizQuestion(
                id = "q113",
                question = "Могут ли extension-функции обращаться к private полям класса?",
                options = listOf(
                    "Да, всегда",
                    "Нет, только к public",
                    "Только к protected",
                    "Только если функция в том же файле"
                ),
                correctAnswerIndex = 1,
                explanation = "Extension-функции имеют доступ только к public членам класса.",
                topic = "extensions"
            ),
            QuizQuestion(
                id = "q114",
                question = "Что такое extension-свойство?",
                options = listOf(
                    "Свойство с особым модификатором",
                    "Вычисляемое свойство, добавляемое к существующему классу",
                    "Свойство в data-классе",
                    "Свойство с делегатом"
                ),
                correctAnswerIndex = 1,
                explanation = "Extension-свойства добавляют вычисляемые свойства к существующим классам (не могут иметь backing field).",
                topic = "extensions"
            ),
            QuizQuestion(
                id = "q115",
                question = "Какой символ используется для обращения к объекту-приемнику в extension-функции?",
                options = listOf("it", "this", "receiver", "self"),
                correctAnswerIndex = 1,
                explanation = "Внутри extension-функции 'this' ссылается на объект-приемник.",
                topic = "extensions"
            ),

// 4. dsl (DSL и билдеры)
            QuizQuestion(
                id = "q116",
                question = "Что означает аббревиатура DSL?",
                options = listOf(
                    "Digital System Language",
                    "Domain Specific Language",
                    "Data Structure Language",
                    "Development Script Language"
                ),
                correctAnswerIndex = 1,
                explanation = "DSL - Domain Specific Language (предметно-ориентированный язык).",
                topic = "dsl"
            ),
            QuizQuestion(
                id = "q117",
                question = "Что такое функция с получателем (function with receiver) в контексте DSL?",
                options = listOf(
                    "Функция, принимающая другой класс как параметр",
                    "Функция, которая имеет доступ к членам объекта-получателя",
                    "Функция с модификатором receiver",
                    "Статическая функция"
                ),
                correctAnswerIndex = 1,
                explanation = "Функции с получателем позволяют внутри лямбды обращаться к членам объекта как будто это методы этого объекта.",
                topic = "dsl"
            ),
            QuizQuestion(
                id = "q118",
                question = "Какой аннотацией помечаются DSL маркеры в Kotlin?",
                options = listOf("@DslMarker", "@Dsl", "@Marker", "@Scope"),
                correctAnswerIndex = 0,
                explanation = "@DslMarker предотвращает доступ к внешним scope в вложенных DSL блоках.",
                topic = "dsl"
            ),
            QuizQuestion(
                id = "q119",
                question = "Что такое fluent interface в контексте DSL?",
                options = listOf(
                    "Интерфейс с плавающей точкой",
                    "Цепочка вызовов методов, возвращающих this",
                    "Интерфейс с одним методом",
                    "Асинхронный интерфейс"
                ),
                correctAnswerIndex = 1,
                explanation = "Fluent interface позволяет строить цепочки вызовов, где каждый метод возвращает текущий объект.",
                topic = "dsl"
            ),
            QuizQuestion(
                id = "q120",
                question = "Как создать DSL для HTML в Kotlin?",
                options = listOf(
                    "Использование extension функций и лямбд с получателем",
                    "Создание специального компилятора",
                    "Написание XML парсера",
                    "Использование шаблонов строк"
                ),
                correctAnswerIndex = 0,
                explanation = "DSL для HTML создаётся с помощью extension функций и лямбд с получателем для построения иерархии тегов.",
                topic = "dsl"
            ),

// 5. functional (Функциональное программирование)
            QuizQuestion(
                id = "q121",
                question = "Что такое чистая функция (pure function)?",
                options = listOf(
                    "Функция без параметров",
                    "Функция, которая всегда возвращает одинаковый результат для одинаковых аргументов и не имеет побочных эффектов",
                    "Функция, написанная на чистом Kotlin",
                    "Функция без возвращаемого значения"
                ),
                correctAnswerIndex = 1,
                explanation = "Чистая функция зависит только от своих аргументов и не имеет побочных эффектов.",
                topic = "functional"
            ),
            QuizQuestion(
                id = "q122",
                question = "Что такое функция высшего порядка (higher-order function)?",
                options = listOf(
                    "Функция с большим количеством параметров",
                    "Функция, принимающая или возвращающая другую функцию",
                    "Функция в корутине",
                    "Функция с рекурсией"
                ),
                correctAnswerIndex = 1,
                explanation = "Функция высшего порядка работает с другими функциями как с параметрами или результатами.",
                topic = "functional"
            ),
            QuizQuestion(
                id = "q123",
                question = "Что такое каррирование (currying)?",
                options = listOf(
                    "Преобразование функции от нескольких аргументов в цепочку функций от одного аргумента",
                    "Сортировка коллекции",
                    "Обработка ошибок",
                    "Создание копии объекта"
                ),
                correctAnswerIndex = 0,
                explanation = "Каррирование преобразует функцию f(a, b, c) в цепочку f(a)(b)(c).",
                topic = "functional"
            ),
            QuizQuestion(
                id = "q124",
                question = "Что такое монада (monad) в функциональном программировании?",
                options = listOf(
                    "Структура данных для хранения одного значения",
                    "Абстракция, позволяющая связывать вычисления в цепочку",
                    "Тип для обработки ошибок",
                    "Функция без побочных эффектов"
                ),
                correctAnswerIndex = 1,
                explanation = "Монада - это паттерн для последовательной композиции вычислений, например Option, Either, List.",
                topic = "functional"
            ),
            QuizQuestion(
                id = "q125",
                question = "Что такое хвостовая рекурсия (tail recursion) в Kotlin?",
                options = listOf(
                    "Рекурсия в конце функции",
                    "Рекурсия, которая оптимизируется компилятором в цикл",
                    "Рекурсия с большим стеком",
                    "Рекурсия в корутинах"
                ),
                correctAnswerIndex = 1,
                explanation = "Хвостовая рекурсия помечается tailrec и оптимизируется компилятором Kotlin в цикл для предотвращения StackOverflowError.",
                topic = "functional"
            )
        )

    }

    fun getQuestionsByTopic(topic: String): List<QuizQuestion> {
        return getQuizQuestions().filter { it.topic == topic }
    }

    fun getQuestionById(id: String): QuizQuestion? {
        return getQuizQuestions().find { it.id == id }
    }
}