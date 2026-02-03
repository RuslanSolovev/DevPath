package com.example.devpath.ui


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.devpath.data.repository.ProgressRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonScreen(
    lessonTitle: String,
    lessonContent: String,
    lessonId: String, // ← ДОБАВЬ ПАРАМЕТР
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val progressRepo = remember { ProgressRepository() }
    val currentUser = Firebase.auth.currentUser

    // Автоматически отмечаем урок как пройденный при первом просмотре
    LaunchedEffect(Unit) {
        if (currentUser != null) {
            coroutineScope.launch {
                progressRepo.markLessonCompleted(currentUser.uid, lessonId)
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(lessonTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 38.dp)
        ) {
            item {
            Text(
                text = lessonContent,
                style = MaterialTheme.typography.bodyLarge
            )}
        }
    }
}
