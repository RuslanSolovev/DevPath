package com.example.devpath.data.repository

import com.example.devpath.domain.models.QuizQuestion

object QuizRepository {
    fun getQuizQuestions(): List<QuizQuestion> {
        return listOf(
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
            )
        )
    }

    fun getQuestionById(id: String): QuizQuestion? {
        return getQuizQuestions().find { it.id == id }
    }
}