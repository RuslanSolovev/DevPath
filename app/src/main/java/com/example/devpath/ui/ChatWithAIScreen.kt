package com.example.devpath.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatWithAIScreen(
    navController: NavHostController
) {
    var message by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<AIMessage>() }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Чат с ИИ-помощником") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (messages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.SmartToy,
                        contentDescription = "ИИ Помощник",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "ИИ-помощник по Kotlin",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Задайте мне вопросы по программированию на Kotlin, " +
                                "я помогу с объяснениями, кодом и советами!",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    // Примеры вопросов
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ExampleQuestionButton(
                            text = "Объясни разницу между val и var",
                            onClick = {
                                message = "Объясни разницу между val и var в Kotlin"
                            }
                        )
                        ExampleQuestionButton(
                            text = "Покажи пример функции высшего порядка",
                            onClick = {
                                message = "Покажи пример функции высшего порядка в Kotlin"
                            }
                        )
                        ExampleQuestionButton(
                            text = "Что такое корутины?",
                            onClick = {
                                message = "Что такое корутины в Kotlin и как их использовать?"
                            }
                        )
                    }
                }
            } else {
                // Список сообщений
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    reverseLayout = true,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(messages.reversed()) { msg ->
                        AIMessageItem(message = msg)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // Поле ввода
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Введите ваш вопрос...") },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (message.isNotBlank()) {
                                    messages.add(AIMessage(text = message, isUser = true))
                                    // TODO: Вызов ИИ API
                                    val botResponse = when {
                                        message.contains("val") || message.contains("var") -> "В Kotlin:\n• `val` - неизменяемая ссылка (immutable), как final в Java\n• `var` - изменяемая ссылка (mutable)\n\nПример:\n```kotlin\nval name = \"Kotlin\" // нельзя изменить\nvar count = 0 // можно изменить\ncount = 5 // OK\nname = \"Java\" // Ошибка!```"
                                        message.contains("функции высшего порядка") -> "Функция высшего порядка — это функция, которая принимает другие функции как параметры или возвращает функцию.\n\nПример:\n```kotlin\nfun calculate(x: Int, y: Int, operation: (Int, Int) -> Int): Int {\n    return operation(x, y)\n}\n\nval sum = calculate(5, 3) { a, b -> a + b } // 8\nval product = calculate(5, 3) { a, b -> a * b } // 15```"
                                        message.contains("корутин") -> "Корутины в Kotlin — это легковесные потоки для асинхронного программирования. Они не блокируют основной поток и эффективно используют ресурсы.\n\nПример:\n```kotlin\nsuspend fun fetchData(): String {\n    delay(1000) // не блокирует поток\n    return \"Данные загружены\"\n}\n\n// Использование\nviewModelScope.launch {\n    val data = fetchData()\n    updateUI(data)\n}```"
                                        else -> "Я помогу вам с изучением Kotlin! Задайте более конкретный вопрос, например: 'Как работает наследование в Kotlin?' или 'Покажи пример RecyclerView'"
                                    }
                                    messages.add(AIMessage(text = botResponse, isUser = false))
                                    message = ""
                                }
                            }
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Отправить")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ExampleQuestionButton(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        onClick = onClick,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Lightbulb,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Default.Send,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun AIMessageItem(message: AIMessage) {
    val horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    val backgroundColor = if (message.isUser)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.surfaceVariant

    val textColor = if (message.isUser)
        MaterialTheme.colorScheme.onPrimary
    else
        MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isUser) 16.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 16.dp
            ),
            color = backgroundColor,
            tonalElevation = 1.dp,
            modifier = Modifier
                .widthIn(max = 280.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                if (!message.isUser) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.SmartToy,
                            contentDescription = "ИИ",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "ИИ-помощник",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
            }
        }
    }
}