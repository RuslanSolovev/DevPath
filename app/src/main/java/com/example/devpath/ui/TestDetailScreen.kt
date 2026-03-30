package com.example.devpath.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.devpath.data.local.entity.TestAttemptEntity
import com.example.devpath.ui.viewmodel.ProgressViewModel
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.jsonObject

@Serializable
data class StoredQuizQuestion(
    val question: String,
    val options: List<String>,
    val correct: Int,
    val userAnswer: Int,
    val explanation: String,
    val topic: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestDetailScreen(
    attemptId: Long,
    onBack: () -> Unit
) {
    val viewModel: ProgressViewModel = hiltViewModel()
    var attempt by remember { mutableStateOf<TestAttemptEntity?>(null) }
    var questions by remember { mutableStateOf<List<StoredQuizQuestion>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(attemptId) {
        attempt = viewModel.progressRepository.getTestAttempt(attemptId)
        attempt?.let {
            questions = parseQuestionsFromJson(it.detailsJson)
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Разбор теста") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(questions) { q ->
                    QuestionReviewCard(q)
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun QuestionReviewCard(q: StoredQuizQuestion) {
    val isCorrect = q.userAnswer == q.correct
    val borderColor = if (isCorrect) Color(0xFF10B981) else Color(0xFFEF4444)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, borderColor.copy(alpha = 0.5f)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                    contentDescription = if (isCorrect) "Правильно" else "Неправильно",
                    tint = if (isCorrect) Color(0xFF10B981) else Color(0xFFEF4444)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = q.question,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Divider()

            q.options.forEachIndexed { index, option ->
                val isUserSelected = index == q.userAnswer
                val isCorrectOption = index == q.correct
                val textColor = when {
                    isCorrectOption -> Color(0xFF10B981)
                    isUserSelected && !isCorrectOption -> Color(0xFFEF4444)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${(index + 65).toChar()}. $option",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (isCorrectOption) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Правильный ответ",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                    } else if (isUserSelected && !isCorrectOption) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Ваш ответ",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Divider()

            Text(
                text = "Пояснение: ${q.explanation}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun parseQuestionsFromJson(json: String): List<StoredQuizQuestion> {
    // Простая парсинг без kotlinx.serialization, можно использовать JSONArray вручную
    val list = mutableListOf<StoredQuizQuestion>()
    try {
        val jsonArray = org.json.JSONArray(json)
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val options = mutableListOf<String>()
            val optsArray = obj.getJSONArray("options")
            for (j in 0 until optsArray.length()) {
                options.add(optsArray.getString(j))
            }
            list.add(
                StoredQuizQuestion(
                    question = obj.getString("question"),
                    options = options,
                    correct = obj.getInt("correct"),
                    userAnswer = obj.getInt("userAnswer"),
                    explanation = obj.getString("explanation"),
                    topic = obj.getString("topic")
                )
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return list
}