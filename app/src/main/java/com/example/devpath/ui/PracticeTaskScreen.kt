package com.example.devpath.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.devpath.data.repository.ProgressRepository
import com.example.devpath.domain.models.PracticeTask
import com.example.devpath.ui.fffff.Green40
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeTaskScreen(task: PracticeTask, onBack: () -> Unit) {
    var userCode by remember { mutableStateOf(task.starterCode) }
    var isCorrect by remember { mutableStateOf(false) }
    var showHint by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf("") }
    val currentUser = Firebase.auth.currentUser
    val coroutineScope = rememberCoroutineScope()
    val progressRepo = remember { ProgressRepository() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(task.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Редактор кода
            OutlinedTextField(
                value = userCode,
                onValueChange = { userCode = it },
                label = { Text("Ваш код") },
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                maxLines = 15
            )

            if (showHint) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Подсказка: ${task.hint}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (feedbackMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = feedbackMessage,
                    color = if (isCorrect) Green40 else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                // В PracticeTaskScreen.kt
                Button(
                    onClick = {
                        val result = checkSolution(task.id, userCode)
                        isCorrect = result.isCorrect
                        feedbackMessage = result.message

                        // Сохраняем прогресс
                        if (result.isCorrect && currentUser != null) {
                            coroutineScope.launch {
                                progressRepo.markPracticeTaskCompleted(currentUser.uid, task.id)
                            }
                        }
                    },
                    enabled = userCode.isNotBlank()
                ) {
                    Text("Проверить")
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(onClick = {
                    showHint = !showHint
                    feedbackMessage = "" // Очищаем предыдущий фидбек
                }) {
                    Text(if (showHint) "Скрыть подсказку" else "Показать подсказку")
                }
            }
        }
    }
}

// Функция гибкой проверки
private fun checkSolution(taskId: String, userCode: String): CheckResult {
    return when (taskId) {
        "hello_world" -> {
            val hasPrintln = userCode.contains("println", ignoreCase = true)
            val hasCorrectString = userCode.contains("Hello, DevPath!", ignoreCase = false)

            if (hasPrintln && hasCorrectString) {
                CheckResult(true, "✅ Отлично! Вы правильно использовали println и строку вывода.")
            } else if (!hasPrintln) {
                CheckResult(false, "❌ Попробуйте использовать функцию println() для вывода текста.")
            } else if (!hasCorrectString) {
                CheckResult(false, "❌ Проверьте, что выводите именно 'Hello, DevPath!'")
            } else {
                CheckResult(false, "❌ Почти правильно! Проверьте синтаксис.")
            }
        }

        "variables_sum" -> {
            val hasValA = userCode.contains("val a = 5", ignoreCase = true)
            val hasValB = userCode.contains("val b = 10", ignoreCase = true)
            val hasSum = userCode.contains("a + b", ignoreCase = true) ||
                    userCode.contains("5 + 10", ignoreCase = true)
            val hasPrintln = userCode.contains("println", ignoreCase = true)

            if (hasValA && hasValB && hasSum && hasPrintln) {
                CheckResult(true, "✅ Прекрасно! Вы правильно создали переменные и вывели их сумму.")
            } else if (!hasValA || !hasValB) {
                CheckResult(false, "❌ Создайте две переменные: val a = 5 и val b = 10")
            } else if (!hasSum) {
                CheckResult(false, "❌ Выведите сумму переменных a и b")
            } else if (!hasPrintln) {
                CheckResult(false, "❌ Используйте println() для вывода результата")
            } else {
                CheckResult(false, "❌ Почти правильно! Проверьте синтаксис.")
            }
        }

        "conditional_if" -> {
            val hasNumber = userCode.contains("val number = ", ignoreCase = true)
            val hasIf = userCode.contains("if", ignoreCase = true)
            val hasElse = userCode.contains("else", ignoreCase = true)
            val hasPrintln = userCode.contains("println", ignoreCase = true)
            val hasEvenCheck = userCode.contains("number % 2 == 0", ignoreCase = true) ||
                    userCode.contains("% 2 == 0", ignoreCase = true)

            if (hasNumber && hasIf && hasElse && hasPrintln && hasEvenCheck) {
                CheckResult(true, "✅ Отлично! Вы правильно определили чётность числа.")
            } else if (!hasNumber) {
                CheckResult(false, "❌ Создайте переменную number с любым числом")
            } else if (!hasIf || !hasElse) {
                CheckResult(false, "❌ Используйте конструкцию if-else для проверки условия")
            } else if (!hasEvenCheck) {
                CheckResult(false, "❌ Проверьте, что проверяете остаток от деления на 2")
            } else if (!hasPrintln) {
                CheckResult(false, "❌ Используйте println() для вывода результата")
            } else {
                CheckResult(false, "❌ Почти правильно! Проверьте синтаксис.")
            }
        }

        "for_loop" -> {
            val hasFor = userCode.contains("for", ignoreCase = true)
            val hasIn = userCode.contains("in", ignoreCase = true)
            val hasRange = userCode.contains("1..5", ignoreCase = true) ||
                    userCode.contains("1 until 6", ignoreCase = true)
            val hasPrintln = userCode.contains("println", ignoreCase = true)

            if (hasFor && hasIn && hasRange && hasPrintln) {
                CheckResult(true, "✅ Отлично! Вы правильно используете цикл for.")
            } else if (!hasFor) {
                CheckResult(false, "❌ Используйте цикл for для перебора значений")
            } else if (!hasIn) {
                CheckResult(false, "❌ Используйте ключевое слово in в цикле for")
            } else if (!hasRange) {
                CheckResult(false, "❌ Используйте диапазон 1..5 для перебора чисел")
            } else if (!hasPrintln) {
                CheckResult(false, "❌ Используйте println() для вывода каждого числа")
            } else {
                CheckResult(false, "❌ Почти правильно! Проверьте синтаксис.")
            }
        }

        "while_loop" -> {
            val hasWhile = userCode.contains("while", ignoreCase = true)
            val hasCounter = userCode.contains("var counter = 1", ignoreCase = true)
            val hasCondition = userCode.contains("counter <= 5", ignoreCase = true) ||
                    userCode.contains("counter < 6", ignoreCase = true)
            val hasIncrement = userCode.contains("counter++", ignoreCase = true) ||
                    userCode.contains("counter += 1", ignoreCase = true)
            val hasPrintln = userCode.contains("println", ignoreCase = true)

            if (hasWhile && hasCounter && hasCondition && hasIncrement && hasPrintln) {
                CheckResult(true, "✅ Отлично! Вы правильно используете цикл while.")
            } else if (!hasWhile) {
                CheckResult(false, "❌ Используйте цикл while для выполнения условий")
            } else if (!hasCounter) {
                CheckResult(false, "❌ Создайте переменную-счётчик counter = 1")
            } else if (!hasCondition) {
                CheckResult(false, "❌ Условие должно проверять counter <= 5")
            } else if (!hasIncrement) {
                CheckResult(false, "❌ Не забудьте увеличивать счётчик на каждой итерации")
            } else if (!hasPrintln) {
                CheckResult(false, "❌ Используйте println() для вывода каждого числа")
            } else {
                CheckResult(false, "❌ Почти правильно! Проверьте синтаксис.")
            }
        }

        "function_basic" -> {
            val hasFun = userCode.contains("fun", ignoreCase = true)
            val hasFunctionName = userCode.contains("multiply", ignoreCase = true)
            val hasParams = userCode.contains("a: Int", ignoreCase = true) &&
                    userCode.contains("b: Int", ignoreCase = true)
            val hasReturn = userCode.contains("return", ignoreCase = true)
            val hasMultiplication = userCode.contains("a * b", ignoreCase = true)
            val hasPrintln = userCode.contains("println", ignoreCase = true)

            if (hasFun && hasFunctionName && hasParams && hasReturn && hasMultiplication && hasPrintln) {
                CheckResult(true, "✅ Отлично! Вы правильно создали функцию умножения.")
            } else if (!hasFun) {
                CheckResult(false, "❌ Используйте ключевое слово fun для создания функции")
            } else if (!hasFunctionName) {
                CheckResult(false, "❌ Функция должна называться multiply")
            } else if (!hasParams) {
                CheckResult(false, "❌ Функция должна принимать два параметра: a: Int и b: Int")
            } else if (!hasReturn) {
                CheckResult(false, "❌ Используйте return для возврата результата")
            } else if (!hasMultiplication) {
                CheckResult(false, "❌ Функция должна возвращать произведение a * b")
            } else if (!hasPrintln) {
                CheckResult(false, "❌ Используйте println() для вызова функции и вывода результата")
            } else {
                CheckResult(false, "❌ Почти правильно! Проверьте синтаксис.")
            }
        }

        "list_operations" -> {
            val hasValList = userCode.contains("val list = listOf", ignoreCase = true) ||
                    userCode.contains("val list = mutableListOf", ignoreCase = true)
            val hasForEachOrFor = userCode.contains("forEach", ignoreCase = true) ||
                    userCode.contains("for", ignoreCase = true)
            val hasPrintln = userCode.contains("println", ignoreCase = true)

            if (hasValList && hasForEachOrFor && hasPrintln) {
                CheckResult(true, "✅ Отлично! Вы правильно работаете со списком.")
            } else if (!hasValList) {
                CheckResult(false, "❌ Создайте список чисел с помощью listOf()")
            } else if (!hasForEachOrFor) {
                CheckResult(false, "❌ Используйте forEach или for для перебора элементов списка")
            } else if (!hasPrintln) {
                CheckResult(false, "❌ Используйте println() для вывода каждого элемента")
            } else {
                CheckResult(false, "❌ Почти правильно! Проверьте синтаксис.")
            }
        }

        "string_template" -> {
            val hasValName = userCode.contains("val name =", ignoreCase = true)
            val hasValAge = userCode.contains("val age =", ignoreCase = true)
            val hasTemplate = userCode.contains("\$", ignoreCase = false)
            val hasPrintln = userCode.contains("println", ignoreCase = true)
            val hasNameInString = userCode.contains("name", ignoreCase = true)
            val hasAgeInString = userCode.contains("age", ignoreCase = true)

            if (hasValName && hasValAge && hasTemplate && hasPrintln && hasNameInString && hasAgeInString) {
                CheckResult(true, "✅ Отлично! Вы правильно используете шаблоны строк.")
            } else if (!hasValName || !hasValAge) {
                CheckResult(false, "❌ Создайте две переменные: name и age")
            } else if (!hasTemplate) {
                CheckResult(false, "❌ Используйте шаблон строки с \$, чтобы вставить переменные")
            } else if (!hasPrintln) {
                CheckResult(false, "❌ Используйте println() для вывода результата")
            } else if (!hasNameInString || !hasAgeInString) {
                CheckResult(false, "❌ В строке должны использоваться обе переменные: name и age")
            } else {
                CheckResult(false, "❌ Почти правильно! Проверьте синтаксис.")
            }
        }

        "null_safety" -> {
            val hasNullable = userCode.contains("String?", ignoreCase = true)
            val hasElvis = userCode.contains("?:", ignoreCase = false)
            val hasDefault = userCode.contains("\"Гость\"", ignoreCase = false)
            val hasPrintln = userCode.contains("println", ignoreCase = true)

            if (hasNullable && hasElvis && hasDefault && hasPrintln) {
                CheckResult(true, "✅ Отлично! Вы правильно обрабатываете nullable-типы.")
            } else if (!hasNullable) {
                CheckResult(false, "❌ Создайте nullable-переменную типа String?")
            } else if (!hasElvis) {
                CheckResult(false, "❌ Используйте оператор ?: (elvis operator) для задания значения по умолчанию")
            } else if (!hasDefault) {
                CheckResult(false, "❌ Укажите значение по умолчанию \"Гость\"")
            } else if (!hasPrintln) {
                CheckResult(false, "❌ Используйте println() для вывода результата")
            } else {
                CheckResult(false, "❌ Почти правильно! Проверьте синтаксис.")
            }
        }

        "when_expression" -> {
            val hasValGrade = userCode.contains("val grade =", ignoreCase = true)
            val hasWhen = userCode.contains("when", ignoreCase = true)
            val hasConditions = userCode.contains("\"A\"", ignoreCase = true) &&
                    userCode.contains("\"B\"", ignoreCase = true) &&
                    userCode.contains("else", ignoreCase = true)
            val hasPrintln = userCode.contains("println", ignoreCase = true)

            if (hasValGrade && hasWhen && hasConditions && hasPrintln) {
                CheckResult(true, "✅ Отлично! Вы правильно используете when-выражение.")
            } else if (!hasValGrade) {
                CheckResult(false, "❌ Создайте переменную grade с буквенной оценкой")
            } else if (!hasWhen) {
                CheckResult(false, "❌ Используйте when-выражение для проверки значения")
            } else if (!hasConditions) {
                CheckResult(false, "❌ Проверьте как минимум значения \"A\" и \"B\", добавьте else ветку")
            } else if (!hasPrintln) {
                CheckResult(false, "❌ Используйте println() для вывода результата")
            } else {
                CheckResult(false, "❌ Почти правильно! Проверьте синтаксис.")
            }
        }

        else -> {
            CheckResult(false, "❌ Задание пока не поддерживается. Попробуйте другое.")
        }
    }
}

// Вспомогательный класс для результата проверки
data class CheckResult(val isCorrect: Boolean, val message: String)