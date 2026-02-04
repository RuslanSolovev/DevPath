package com.example.devpath.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.devpath.data.repository.ProgressRepository
import com.example.devpath.domain.models.PracticeTask
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
    var isLoading by remember { mutableStateOf(false) }
    val currentUser = Firebase.auth.currentUser
    val coroutineScope = rememberCoroutineScope()
    val progressRepo = remember { ProgressRepository() }
    val clipboardManager = LocalClipboardManager.current

    val difficultyColor = when (task.difficulty) {
        "beginner" -> Color(0xFF10B981)
        "intermediate" -> Color(0xFFF59E0B)
        "advanced" -> Color(0xFFEF4444)
        else -> MaterialTheme.colorScheme.primary
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column {
                        Text(
                            task.title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(difficultyColor.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                when (task.difficulty) {
                                    "beginner" -> "Начальный уровень"
                                    "intermediate" -> "Средний уровень"
                                    "advanced" -> "Продвинутый уровень"
                                    else -> "Начальный"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = difficultyColor
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Назад",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                ),
                actions = {
                    IconButton(
                        onClick = { userCode = task.starterCode },
                        enabled = userCode != task.starterCode
                    ) {
                        Icon(
                            Icons.Rounded.RestartAlt,
                            contentDescription = "Сбросить код",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (userCode.isNotBlank() && !isLoading) {
                        isLoading = true
                        feedbackMessage = ""

                        val result = checkSolution(task.id, userCode)
                        isCorrect = result.isCorrect
                        feedbackMessage = result.message

                        if (result.isCorrect && currentUser != null) {
                            coroutineScope.launch {
                                progressRepo.markPracticeTaskCompleted(currentUser.uid, task.id)
                                isLoading = false
                            }
                        } else {
                            isLoading = false
                        }
                    }
                },
                icon = {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            if (isCorrect) Icons.Default.DoneAll else Icons.Default.PlayArrow,
                            contentDescription = if (isCorrect) "Задание пройдено" else "Проверить решение"
                        )
                    }
                },
                text = {
                    Text(
                        when {
                            isLoading -> "Проверка..."
                            isCorrect -> "Задание пройдено!"
                            else -> "Проверить решение"
                        }
                    )
                },
                containerColor = if (isCorrect)
                    MaterialTheme.colorScheme.secondary
                else
                    MaterialTheme.colorScheme.primary,
                contentColor = if (isCorrect)
                    MaterialTheme.colorScheme.onSecondary
                else
                    MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.alpha(
                    if (isLoading || userCode.isBlank()) 0.5f else 1f
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Карточка с описанием задания
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(difficultyColor.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.Description,
                                    contentDescription = "Описание",
                                    tint = difficultyColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Text(
                                "Описание задания",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Используем FormattedLessonContent для форматированного текста
                        FormattedLessonContent(
                            content = task.description,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                // Редактор кода (без дублирования)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(30.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Edit,
                                    contentDescription = "Код",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "Редактор кода",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Countertops,
                                    contentDescription = "Строки",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "${userCode.lines().size} строк",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }


                        Text(
                            "👇 Редактируйте код ниже:",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        // Редактор кода пользователя с подсветкой синтаксиса
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 200.dp, max = 400.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E1E1E),
                            tonalElevation = 2.dp
                        ) {
                            BasicTextField(
                                value = userCode,
                                onValueChange = { userCode = it },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                                    .verticalScroll(rememberScrollState()),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    color = Color.Transparent, // Делаем текст невидимым
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                ),
                                maxLines = Int.MAX_VALUE,
                                decorationBox = { innerTextField ->
                                    // Показываем подсвеченный код
                                    val highlightedCode = remember(userCode) {
                                        buildAnnotatedString {
                                            userCode.lines().forEachIndexed { index, line ->
                                                val highlightedLine = highlightKotlinSyntax(line)
                                                append(highlightedLine)
                                                if (index < userCode.lines().lastIndex) {
                                                    append("\n")
                                                }
                                            }
                                        }
                                    }

                                    // Отображаем подсвеченный код
                                    Text(
                                        text = highlightedCode,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp
                                    )

                                    // Отрисовываем невидимое текстовое поле поверх
                                    innerTextField()
                                }
                            )
                        }

                        Text(
                            "💡 Совет: Изменяйте код и экспериментируйте!",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }

            item {
                // Кнопки действий
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            showHint = !showHint
                            if (showHint) feedbackMessage = ""
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                if (showHint) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                contentDescription = "Подсказка",
                                modifier = Modifier.size(18.dp)
                            )
                            Text(if (showHint) "Скрыть подсказку" else "Показать подсказку")
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(userCode))
                            // Можно добавить Snackbar для подтверждения копирования
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Rounded.ContentCopy,
                                contentDescription = "Копировать",
                                modifier = Modifier.size(18.dp)
                            )
                            Text("Копировать код")
                        }
                    }
                }
            }

            if (showHint || feedbackMessage.isNotEmpty()) {
                item {
                    // Подсказка или результат проверки
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCorrect)
                                MaterialTheme.colorScheme.secondaryContainer
                            else if (feedbackMessage.isNotEmpty() && !isCorrect)
                                MaterialTheme.colorScheme.errorContainer
                            else
                                MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isCorrect)
                                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                            else if (feedbackMessage.isNotEmpty() && !isCorrect)
                                                MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                            else
                                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        when {
                                            isCorrect -> Icons.Rounded.CheckCircle
                                            feedbackMessage.isNotEmpty() && !isCorrect -> Icons.Rounded.Error
                                            showHint -> Icons.Rounded.Lightbulb
                                            else -> Icons.Rounded.Info
                                        },
                                        contentDescription = "Информация",
                                        tint = if (isCorrect)
                                            MaterialTheme.colorScheme.onSecondaryContainer
                                        else if (feedbackMessage.isNotEmpty() && !isCorrect)
                                            MaterialTheme.colorScheme.onErrorContainer
                                        else
                                            MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        when {
                                            isCorrect -> "Поздравляем!"
                                            feedbackMessage.isNotEmpty() && !isCorrect -> "Есть ошибки"
                                            showHint -> "Подсказка"
                                            else -> "Информация"
                                        },
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = if (isCorrect)
                                            MaterialTheme.colorScheme.onSecondaryContainer
                                        else if (feedbackMessage.isNotEmpty() && !isCorrect)
                                            MaterialTheme.colorScheme.onErrorContainer
                                        else
                                            MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }

                            // Используем FormattedLessonContent для форматирования подсказки
                            if (showHint && feedbackMessage.isEmpty()) {
                                FormattedLessonContent(
                                    content = task.hint,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                Text(
                                    text = feedbackMessage,
                                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                    color = if (isCorrect)
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    else if (feedbackMessage.isNotEmpty() && !isCorrect)
                                        MaterialTheme.colorScheme.onErrorContainer
                                    else
                                        MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
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