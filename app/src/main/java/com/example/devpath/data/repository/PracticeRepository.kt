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
                hint = "Используйте: println(\"Hello, DevPath!\")"
            ),
            PracticeTask(
                id = "variables_sum",
                title = "Сумма переменных",
                description = "Создайте две неизменяемые переменные: a = 5 и b = 10. Выведите их сумму с помощью println()",
                starterCode = """fun main() {
    // Объявите переменные и выведите сумму
}""".trimIndent(),
                solution = "",
                hint = "Используйте: val a = 5, val b = 10, println(a + b)"
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
                hint = "Используйте: if (number % 2 == 0) { ... } else { ... }"
            ),
            PracticeTask(
                id = "for_loop",
                title = "Цикл for",
                description = "Используя цикл for, выведите все числа от 1 до 5 включительно",
                starterCode = """fun main() {
    // Используйте цикл for для вывода чисел 1-5
}""".trimIndent(),
                solution = "",
                hint = "Используйте: for (i in 1..5) { println(i) }"
            ),
            PracticeTask(
                id = "while_loop",
                title = "Цикл while",
                description = "Используя цикл while, выведите числа от 1 до 5. Создайте счётчик counter = 1",
                starterCode = """fun main() {
    // Используйте цикл while для вывода чисел 1-5
}""".trimIndent(),
                solution = "",
                hint = "Используйте: var counter = 1; while (counter <= 5) { println(counter); counter++ }"
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
                hint = "Используйте: fun multiply(a: Int, b: Int): Int { return a * b }"
            ),
            PracticeTask(
                id = "list_operations",
                title = "Работа со списками",
                description = "Создайте список чисел (например, 1, 2, 3, 4, 5) и выведите каждый элемент с помощью forEach или цикла for",
                starterCode = """fun main() {
    // Создайте список и переберите его элементы
}""".trimIndent(),
                solution = "",
                hint = "Используйте: val list = listOf(1, 2, 3, 4, 5); list.forEach { println(it) }"
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
                hint = "Используйте: val name = \"Имя\"; val age = 25; println(\"Меня зовут \$name, мне \$age лет\")"
            ),
            PracticeTask(
                id = "null_safety",
                title = "Null безопасность",
                description = "Создайте nullable переменную типа String?. Используя оператор ?:, задайте значение по умолчанию \"Гость\", если переменная равна null",
                starterCode = """fun main() {
    // Создайте nullable переменную и обработайте null
}""".trimIndent(),
                solution = "",
                hint = "Используйте: val name: String? = null; val result = name ?: \"Гость\"; println(result)"
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
                hint = "Используйте: val grade = \"A\"; when (grade) { \"A\" -> println(\"Отлично\") \"B\" -> println(\"Хорошо\") else -> println(\"Удовлетворительно\") }"
            )
        )
    }

    fun getTaskById(id: String): PracticeTask? {
        return getPracticeTasks().find { it.id == id }
    }
}