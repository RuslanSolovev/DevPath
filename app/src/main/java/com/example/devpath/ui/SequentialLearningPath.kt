package com.example.devpath.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.devpath.domain.models.PracticeTask
import com.example.devpath.domain.models.QuizQuestion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SequentialLearningPath(
    lessonTopic: String,
    practiceTasks: List<PracticeTask>,
    quizQuestions: List<QuizQuestion>,
    onPracticeClick: (String) -> Unit,
    onQuizClick: (String) -> Unit,
    onGeneralTestClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            "Закрепите знания",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Практика по теме - ВСЕ ЗАДАНИЯ ОТКРЫТЫ
        if (practiceTasks.isNotEmpty()) {
            for ((index, task) in practiceTasks.withIndex()) {
                LearningStepCard(
                    title = if (index == 0) "Практика" else "Практика ${index + 1}",
                    subtitle = if (index == 0) "${practiceTasks.size} заданий" else "Задание ${index + 1}",
                    icon = if (index == 0) Icons.Rounded.Code else Icons.Default.CheckCircle,
                    color = MaterialTheme.colorScheme.primary,
                    onClick = { onPracticeClick(task.id) },
                    isLocked = false
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Тесты по теме - ВСЕ ВОПРОСЫ ОТКРЫТЫ
        if (quizQuestions.isNotEmpty()) {
            for ((index, question) in quizQuestions.withIndex()) {
                LearningStepCard(
                    title = if (index == 0) "Тест" else "Вопрос ${index + 1}",
                    subtitle = if (index == 0) "${quizQuestions.size} вопросов" else "Вопрос ${index + 1}",
                    icon = if (index == 0) Icons.Rounded.Quiz else Icons.Default.CheckCircle,
                    color = MaterialTheme.colorScheme.secondary,
                    onClick = { onQuizClick(question.id) },
                    isLocked = false
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Общий тест
        LearningStepCard(
            title = "Общий тест знаний",
            subtitle = "10 случайных вопросов по всем темам",
            icon = Icons.Rounded.AutoAwesome,
            color = MaterialTheme.colorScheme.tertiary,
            onClick = onGeneralTestClick,
            isLocked = false
        )
    }
}

@Composable
private fun LearningStepCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    isLocked: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .then(if (!isLocked) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLocked)
                MaterialTheme.colorScheme.surfaceVariant
            else color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isLocked)
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    else color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = if (isLocked)
                        MaterialTheme.colorScheme.outline
                    else color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (isLocked) FontWeight.Normal else FontWeight.Bold
                    ),
                    color = if (isLocked)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isLocked)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!isLocked) {
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "Перейти",
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}