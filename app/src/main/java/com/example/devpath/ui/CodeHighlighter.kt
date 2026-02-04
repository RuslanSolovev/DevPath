package com.example.devpath.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CodeBlock(
    code: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = Color(0xFF1E1E1E), // Темный фон как в Android Studio
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = buildAnnotatedString {
                    val lines = code.lines()

                    lines.forEachIndexed { index, line ->
                        // Подсветка ключевых слов Kotlin
                        val highlightedLine = highlightKotlinSyntax(line)

                        withStyle(
                            style = SpanStyle(
                                color = Color(0xFFD4D4D4), // Светло-серый для обычного текста
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp
                            )
                        ) {
                            append(highlightedLine)
                        }

                        if (index < lines.lastIndex) {
                            append("\n")
                        }
                    }
                },
                fontFamily = FontFamily.Monospace,
                lineHeight = 20.sp
            )
        }
    }
}

// Удаляем verticalScroll из CodeBlock

// Добавьте эти улучшения в highlightKotlinSyntax функцию:
fun highlightKotlinSyntax(line: String): AnnotatedString {
    return buildAnnotatedString {
        // Разделяем строку на токены, сохраняя пробелы и другие символы
        val pattern = Regex("""(\s+|//.*|".*?"|'.'|\b\w+\b|[^\w\s])""")
        val tokens = pattern.findAll(line).map { it.value }.toList()

        tokens.forEach { token ->
            val color = when {
                // Ключевые слова Kotlin
                token in listOf(
                    "fun", "val", "var", "if", "else", "when", "for", "while",
                    "return", "class", "object", "interface", "enum", "data",
                    "sealed", "open", "abstract", "override", "operator",
                    "infix", "inline", "suspend", "typealias", "package",
                    "import", "as", "is", "in", "!in", "as?", "null", "true",
                    "false", "this", "super", "typeof", "try", "catch",
                    "finally", "throw", "break", "continue", "do", "companion",
                    "init", "internal", "private", "protected", "public",
                    "const", "expect", "actual", "external", "lateinit",
                    "noinline", "crossinline", "reified", "tailrec", "vararg",
                    "where", "by", "get", "set", "constructor", "field",
                    "property", "receiver", "param", "setparam", "delegate",
                    "dynamic", "annotation", "it"
                ) -> Color(0xFF569CD6) // Синий

                // Типы данных
                token in listOf(
                    "String", "Int", "Long", "Double", "Float", "Boolean",
                    "Char", "Byte", "Short", "Unit", "Any", "Nothing",
                    "List", "MutableList", "Set", "MutableSet", "Map",
                    "MutableMap", "Array", "Sequence", "Flow", "CoroutineScope",
                    "Mutable", "Immutable", "Collection", "Iterable"
                ) -> Color(0xFF4EC9B0) // Бирюзовый

                // Функции и константы (CAPS_WITH_UNDERSCORE)
                token.matches(Regex("^[A-Z_][A-Z0-9_]*$")) -> Color(0xFFDCDCAA) // Светло-желтый

                // Числа
                token.matches(Regex("\\b\\d+(\\.\\d+)?([eE][+-]?\\d+)?[fFL]?\\b")) ->
                    Color(0xFFB5CEA8) // Зеленый

                // Строки
                token.matches(Regex("\".*?\"")) -> Color(0xFFCE9178) // Оранжевый

                // Комментарии
                token.startsWith("//") -> Color(0xFF6A9955) // Зеленый

                // Аннотации
                token.startsWith("@") -> Color(0xFF569CD6) // Синий (как ключевые слова)

                // Операторы и пунктуация
                token in listOf(
                    "=", "+", "-", "*", "/", "%", "==", "!=", "<", ">", "<=", ">=",
                    "&&", "||", "!", "?:", "..", "->", "?.", "!!", "as", "is"
                ) -> Color(0xFFD4D4D4) // Светло-серый

                else -> Color(0xFFD4D4D4) // Светло-серый по умолчанию
            }

            withStyle(
                style = SpanStyle(
                    color = color,
                    fontWeight = if (color == Color(0xFF569CD6)) FontWeight.Bold else FontWeight.Normal
                )
            ) {
                append(token)
            }
        }
    }
}

@Composable
fun FormattedLessonContent(
    content: String,
    modifier: Modifier = Modifier
) {
    // Разделяем на абзацы, но сохраняем пустые строки как разделители
    val paragraphs = content.split("\n\n")

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        paragraphs.forEachIndexed { index, paragraph ->
            if (paragraph.isBlank()) {
                // Пропускаем пустые абзацы
                return@forEachIndexed
            }

            parseParagraph(paragraph, index)
        }
    }
}

@Composable
private fun ColumnScope.parseParagraph(paragraph: String, index: Int) {
    val lines = paragraph.lines()

    when {
        // Заголовки
        lines.firstOrNull()?.startsWith("# ") == true -> {
            Text(
                text = lines.first().removePrefix("# "),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Добавляем остальной текст (если есть)
            if (lines.size > 1) {
                val remainingText = lines.drop(1).joinToString("\n")
                if (remainingText.isNotBlank()) {
                    Text(
                        text = remainingText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 24.sp
                    )
                }
            }
        }

        lines.firstOrNull()?.startsWith("## ") == true -> {
            Text(
                text = lines.first().removePrefix("## "),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            if (lines.size > 1) {
                val remainingText = lines.drop(1).joinToString("\n")
                if (remainingText.isNotBlank()) {
                    Text(
                        text = remainingText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 24.sp
                    )
                }
            }
        }

        lines.firstOrNull()?.startsWith("### ") == true -> {
            Text(
                text = lines.first().removePrefix("### "),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            if (lines.size > 1) {
                val remainingText = lines.drop(1).joinToString("\n")
                if (remainingText.isNotBlank()) {
                    Text(
                        text = remainingText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 24.sp
                    )
                }
            }
        }

        // Блоки кода - новая безопасная реализация
        paragraph.contains("```") -> {
            parseCodeBlock(paragraph, index)
        }

        // Списки
        lines.any { it.trim().matches(Regex("^\\d+\\.\\s")) } -> {
            parseList(paragraph)
        }

        // Жирный текст
        paragraph.contains("**") -> {
            parseBoldText(paragraph)
        }

        // Обычный текст
        else -> {
            Text(
                text = paragraph,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
private fun ColumnScope.parseCodeBlock(paragraph: String, index: Int) {
    val codeStart = paragraph.indexOf("```kotlin")
    val codeEnd = paragraph.lastIndexOf("```")

    // Проверяем, есть ли полный блок кода
    if (codeStart != -1 && codeEnd != -1 && codeStart < codeEnd) {
        val code = paragraph
            .substring(codeStart + 9, codeEnd)
            .trimIndent()

        CodeBlock(
            code = code,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp, max = 400.dp) // Ограничиваем высоту
        )

        // Проверяем, есть ли текст после блока кода
        val textAfterCode = paragraph.substring(codeEnd + 3).trim()
        if (textAfterCode.isNotEmpty()) {
            Text(
                text = textAfterCode,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 24.sp
            )
        }
    } else {
        // Если блок кода неполный, показываем как есть
        Text(
            text = paragraph,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 24.sp
        )
    }
}

@Composable
private fun ColumnScope.parseList(paragraph: String) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        paragraph.lines().forEach { line ->
            val trimmedLine = line.trim()
            when {
                trimmedLine.matches(Regex("^\\d+\\.\\s+.*")) -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = trimmedLine.substringBefore(". "),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = trimmedLine.substringAfter(". "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                trimmedLine.startsWith("- ") -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = trimmedLine.removePrefix("- "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                trimmedLine.isNotEmpty() -> {
                    Text(
                        text = trimmedLine,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.parseBoldText(paragraph: String) {
    Text(
        text = buildAnnotatedString {
            var remainingText = paragraph

            while (remainingText.contains("**")) {
                val startIndex = remainingText.indexOf("**")
                if (startIndex == -1) break

                // Текст до **
                val before = remainingText.substring(0, startIndex)
                if (before.isNotEmpty()) {
                    append(before)
                }

                // Убираем открывающие **
                remainingText = remainingText.substring(startIndex + 2)

                // Ищем закрывающие **
                val endIndex = remainingText.indexOf("**")
                if (endIndex == -1) {
                    // Нет закрывающих, добавляем ** и остальной текст
                    append("**")
                    append(remainingText)
                    break
                }

                // Жирный текст
                val boldText = remainingText.substring(0, endIndex)
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    append(boldText)
                }

                // Убираем закрывающие **
                remainingText = remainingText.substring(endIndex + 2)
            }

            // Добавляем оставшийся текст
            if (remainingText.isNotEmpty()) {
                append(remainingText)
            }
        },
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
        lineHeight = 24.sp
    )
}