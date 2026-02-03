package com.example.devpath.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.devpath.data.repository.PracticeRepository
import com.example.devpath.domain.models.PracticeTask

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen() {
    val navController = rememberNavController()
    val tasks = PracticeRepository.getPracticeTasks()

    NavHost(
        navController = navController,
        startDestination = "task_list"
    ) {
        composable("task_list") {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(tasks, key = { it.id }) { task ->
                    PracticeTaskItem(
                        task = task,
                        onClick = {
                            navController.navigate("task/${task.id}")
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        composable("task/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: "hello_world"
            val task = PracticeRepository.getTaskById(taskId)
                ?: PracticeRepository.getPracticeTasks().first()

            PracticeTaskScreen(
                task = task,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun PracticeTaskItem(task: PracticeTask, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}