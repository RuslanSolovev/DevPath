package com.example.devpath.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.devpath.data.repository.ProgressRepository
import com.example.devpath.domain.models.UserProgress
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    onNavigateToTabs: () -> Unit
) {
    val currentUser = Firebase.auth.currentUser
    val progressRepo = remember { ProgressRepository() }
    val coroutineScope = rememberCoroutineScope()

    // Загружаем текущее имя из Firestore
    var displayName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (currentUser != null) {
            val progress = progressRepo.loadProgress(currentUser.uid)
            displayName = progress?.displayName ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Добро пожаловать!",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Ваше имя") },
                placeholder = { Text("Например, Алексей") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (currentUser != null && displayName.isNotBlank()) {
                        isLoading = true
                        // В ProfileScreen.kt
                        coroutineScope.launch {
                            val progress = progressRepo.loadProgress(currentUser.uid)
                                ?: UserProgress.createEmpty(currentUser.uid)
                            val updatedProgress = progress.copy(displayName = displayName.trim())

                            // Логируем перед сохранением
                            println("DEBUG: Сохраняем прогресс: ${updatedProgress.displayName}")

                            progressRepo.saveProgress(updatedProgress)
                            isLoading = false
                            onNavigateToTabs()
                        }
                    }
                },
                enabled = displayName.isNotBlank() && !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Сохранить и начать обучение")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Ваш email: ${currentUser?.email}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}