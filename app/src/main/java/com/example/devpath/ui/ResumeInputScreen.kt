// ui/ResumeInputScreen.kt
package com.example.devpath.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumeInputScreen(
    onResumeSaved: (ResumeData) -> Unit,
    onSkip: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    // Состояния для полей формы
    var fullName by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf("") }
    var projects by remember { mutableStateOf("") }
    var education by remember { mutableStateOf("") }
    var expectedPosition by remember { mutableStateOf("Android Developer") }
    var github by remember { mutableStateOf("") }
    var linkedin by remember { mutableStateOf("") }

    // Анимация
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Фокус для первого поля
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            // Верхняя панель
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Ваше резюме",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))

                    // Кнопка пропуска
                    TextButton(onClick = onSkip) {
                        Text("Пропустить")
                    }
                }
            }

            // Основной контент
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Аватар-заглушка
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        )
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(50.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Расскажите о себе перед собеседованием",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Форма
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // ФИО
                        ResumeTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = "ФИО",
                            placeholder = "Иванов Иван Иванович",
                            icon = Icons.Default.Person,
                            focusRequester = focusRequester,
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )

                        // Желаемая позиция
                        ResumeTextField(
                            value = expectedPosition,
                            onValueChange = { expectedPosition = it },
                            label = "Желаемая позиция",
                            placeholder = "Android Developer / Mobile Developer",
                            icon = Icons.Default.Work,
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )

                        // Опыт работы
                        ResumeTextField(
                            value = experience,
                            onValueChange = { experience = it },
                            label = "Опыт работы",
                            placeholder = "3 года в разработке Android приложений...",
                            icon = Icons.Default.Timeline,
                            maxLines = 4,
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )

                        // Навыки
                        ResumeTextField(
                            value = skills,
                            onValueChange = { skills = it },
                            label = "Ключевые навыки",
                            placeholder = "Kotlin, Jetpack Compose, Coroutines, MVVM, Clean Architecture...",
                            icon = Icons.Default.Code,
                            maxLines = 3,
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )

                        // Проекты
                        ResumeTextField(
                            value = projects,
                            onValueChange = { projects = it },
                            label = "Проекты",
                            placeholder = "Pet проект - приложение для трекинга привычек...",
                            icon = Icons.Default.Folder,
                            maxLines = 4,
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )

                        // Образование
                        ResumeTextField(
                            value = education,
                            onValueChange = { education = it },
                            label = "Образование",
                            placeholder = "МГУ, Факультет ВМК, 2020",
                            icon = Icons.Default.School,
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )

                        // GitHub
                        ResumeTextField(
                            value = github,
                            onValueChange = { github = it },
                            label = "GitHub (опционально)",
                            placeholder = "github.com/username",
                            icon = Icons.Default.Link,
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )

                        // LinkedIn
                        ResumeTextField(
                            value = linkedin,
                            onValueChange = { linkedin = it },
                            label = "LinkedIn (опционально)",
                            placeholder = "linkedin.com/in/username",
                            icon = Icons.Default.Link,
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Кнопки действий
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onSkip,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Пропустить")
                    }

                    Button(
                        onClick = {
                            val resumeData = ResumeData(
                                fullName = fullName,
                                experience = experience,
                                skills = skills,
                                projects = projects,
                                education = education,
                                expectedPosition = expectedPosition,
                                github = github,
                                linkedin = linkedin
                            )
                            onResumeSaved(resumeData)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = fullName.isNotBlank() || experience.isNotBlank() || skills.isNotBlank()
                    ) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Начать собеседование")
                    }
                }
            }
        }
    }
}

@Composable
private fun ResumeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    focusRequester: FocusRequester? = null,
    maxLines: Int = 2,
    onNext: () -> Unit = {}
) {
    var isFocused by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (isFocused)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        else
            MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (isFocused)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isFocused)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(20.dp)
                    .padding(top = 12.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isFocused)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
                        .onFocusChanged { isFocused = it.isFocused },
                    placeholder = {
                        Text(
                            placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    maxLines = maxLines,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = if (maxLines == 2) ImeAction.Next else ImeAction.Default
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { onNext() }
                    ),
                    singleLine = maxLines == 1
                )
            }
        }
    }
}

data class ResumeData(
    val fullName: String = "",
    val experience: String = "",
    val skills: String = "",
    val projects: String = "",
    val education: String = "",
    val expectedPosition: String = "Android Developer",
    val github: String = "",
    val linkedin: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toPrompt(): String = buildString {
        appendLine("ИНФОРМАЦИЯ О КАНДИДАТЕ:")
        if (fullName.isNotBlank()) appendLine("• Имя: $fullName")
        if (expectedPosition.isNotBlank()) appendLine("• Желаемая позиция: $expectedPosition")
        if (experience.isNotBlank()) appendLine("• Опыт работы:\n$experience")
        if (skills.isNotBlank()) appendLine("• Навыки:\n$skills")
        if (projects.isNotBlank()) appendLine("• Проекты:\n$projects")
        if (education.isNotBlank()) appendLine("• Образование:\n$education")
        if (github.isNotBlank()) appendLine("• GitHub: $github")
        if (linkedin.isNotBlank()) appendLine("• LinkedIn: $linkedin")
    }

    fun hasData(): Boolean {
        return fullName.isNotBlank() || experience.isNotBlank() || skills.isNotBlank() ||
                projects.isNotBlank() || education.isNotBlank()
    }
}