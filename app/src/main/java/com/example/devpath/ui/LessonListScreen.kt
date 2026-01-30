package com.example.devpath.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.devpath.data.repository.LessonRepository

// В LessonListScreen.kt
@Composable
fun LessonListScreen(onLessonClick: (String) -> Unit) {
    val lessons = LessonRepository.getLessons()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 40.dp, start = 10.dp, end = 10.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(lessons, key = { it.id }) { lesson ->
            LessonItem(
                lesson = lesson.title,
                onClick = { onLessonClick(lesson.id) } // Передаём ID, а не название
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun LessonItem(lesson: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = lesson, style = MaterialTheme.typography.titleMedium)
        }
    }
}