package com.example.devpath.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.devpath.domain.models.QuizQuestion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizTopicScreen(
    questions: List<QuizQuestion>,
    topic: String,
    onBack: () -> Unit,
    onQuestionClick: (String) -> Unit,
    onUnlockNext: (() -> Unit)? = null
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Тест: $topic") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        if (questions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Нет вопросов по этой теме")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(questions, key = { it.id }) { question ->
                    QuizQuestionItem(
                        question = question,
                        onClick = { onQuestionClick(question.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizQuestionItem(
    question: QuizQuestion,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = question.question,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Варианты: ${question.options.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// УДАЛИТЕ ЭТУ ФУНКЦИЮ - ОНА ДУБЛИРУЕТСЯ
// @Composable
// fun QuizQuestionScreen(...)