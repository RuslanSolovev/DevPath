package com.example.devpath.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.devpath.data.repository.ProgressRepository
import com.example.devpath.domain.models.QuizQuestion
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizQuestionScreen(question: QuizQuestion, onBack: () -> Unit) {
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var isAnswered by remember { mutableStateOf(false) }
    var showExplanation by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val currentUser = Firebase.auth.currentUser
    val coroutineScope = rememberCoroutineScope()
    val progressRepo = remember { ProgressRepository() }

    val topicColor = when (question.topic) {
        "kotlin_basics" -> Color(0xFF6366F1)
        "strings" -> Color(0xFF10B981)
        "null_safety" -> Color(0xFFF59E0B)
        "collections" -> Color(0xFFEF4444)
        "functions" -> Color(0xFF8B5CF6)
        "control_flow" -> Color(0xFF06B6D4)
        "classes" -> Color(0xFFEC4899)
        else -> MaterialTheme.colorScheme.primary
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column {
                        Text(
                            "Вопрос",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(topicColor.copy(alpha = 0.1f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                getTopicName(question.topic),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = topicColor
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
                )
            )
        },
        floatingActionButton = {
            if (!isAnswered && selectedOption != null) {
                ExtendedFloatingActionButton(
                    onClick = {
                        isLoading = true
                        isAnswered = true
                        showExplanation = true

                        if (currentUser != null) {
                            coroutineScope.launch {
                                progressRepo.saveQuizResult(
                                    currentUser.uid,
                                    question.id,
                                    selectedOption == question.correctAnswerIndex
                                )
                                isLoading = false
                            }
                        } else {
                            isLoading = false
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
                                Icons.Default.Check,
                                contentDescription = "Проверить"
                            )
                        }
                    },
                    text = { Text("Проверить ответ") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.alpha(if (isLoading) 0.5f else 1f)
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                // Вопрос
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
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
                                    .background(topicColor.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.Quiz,
                                    contentDescription = "Вопрос",
                                    tint = topicColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                "Вопрос",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = question.question,
                            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            item {
                // Варианты ответов
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Выберите правильный ответ:",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    question.options.forEachIndexed { index, option ->
                        val isSelected = selectedOption == index
                        val isCorrect = index == question.correctAnswerIndex
                        val showResult = isAnswered

                        QuizOptionCard(
                            text = option,
                            index = index,
                            isSelected = isSelected,
                            isCorrect = if (showResult) isCorrect else null,
                            onClick = {
                                if (!isAnswered) {
                                    selectedOption = index
                                }
                            },
                            disabled = isAnswered
                        )
                    }
                }
            }

            if (showExplanation) {
                item {
                    // Объяснение
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedOption == question.correctAnswerIndex)
                                Color(0xFF10B981).copy(alpha = 0.1f)
                            else
                                Color(0xFFEF4444).copy(alpha = 0.1f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                                        .background(
                                            if (selectedOption == question.correctAnswerIndex)
                                                Color(0xFF10B981).copy(alpha = 0.2f)
                                            else
                                                Color(0xFFEF4444).copy(alpha = 0.2f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        if (selectedOption == question.correctAnswerIndex)
                                            Icons.Rounded.CheckCircle
                                        else Icons.Rounded.Error,
                                        contentDescription = "Результат",
                                        tint = if (selectedOption == question.correctAnswerIndex)
                                            Color(0xFF10B981)
                                        else Color(0xFFEF4444),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        if (selectedOption == question.correctAnswerIndex)
                                            "✅ Правильно!"
                                        else "❌ Неправильно",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = if (selectedOption == question.correctAnswerIndex)
                                            Color(0xFF10B981)
                                        else Color(0xFFEF4444)
                                    )
                                    Text(
                                        if (selectedOption == question.correctAnswerIndex)
                                            "Отличная работа!"
                                        else "Правильный ответ: ${question.options[question.correctAnswerIndex]}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Divider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )

                            Text(
                                text = question.explanation,
                                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
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

@Composable
fun QuizOptionCard(
    text: String,
    index: Int,
    isSelected: Boolean,
    isCorrect: Boolean?,
    onClick: () -> Unit,
    disabled: Boolean
) {
    val letters = listOf("A", "B", "C", "D")

    val backgroundColor = when {
        isCorrect == true -> Color(0xFF10B981).copy(alpha = 0.1f)
        isCorrect == false && isSelected -> Color(0xFFEF4444).copy(alpha = 0.1f)
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }

    val borderColor = when {
        isCorrect == true -> Color(0xFF10B981)
        isCorrect == false && isSelected -> Color(0xFFEF4444)
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }

    val letterColor = when {
        isCorrect == true -> Color(0xFF10B981)
        isCorrect == false && isSelected -> Color(0xFFEF4444)
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = BorderStroke(
            width = 1.dp,
            color = borderColor
        ),
        onClick = onClick,
        enabled = !disabled
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected || isCorrect != null)
                            letterColor.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    letters.getOrElse(index) { (index + 1).toString() },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = letterColor
                )
            }

            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = if (disabled) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            if (isSelected) {
                Icon(
                    if (isCorrect == true) Icons.Rounded.CheckCircle
                    else if (isCorrect == false) Icons.Rounded.Close
                    else Icons.Rounded.RadioButtonChecked,
                    contentDescription = "Выбрано",
                    tint = letterColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}