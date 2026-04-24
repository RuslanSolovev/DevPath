package com.example.devpath.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesHubScreen(
    navController: NavHostController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Игры и развлечения", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                GameCard(
                    title = "Викторина",
                    description = "Проверь свои знания IT",
                    icon = Icons.Outlined.Quiz,
                    gradient = listOf(Color(0xFF6C63FF), Color(0xFF3F3D9E)),
                    onClick = { /* открыть викторину */ }
                )
            }
            item {
                GameCard(
                    title = "2048",
                    description = "Классическая головоломка",
                    icon = Icons.Outlined.GridOn,
                    gradient = listOf(Color(0xFFFF9800), Color(0xFFE65100)),
                    onClick = { /* открыть 2048 */ }
                )
            }
            item {
                GameCard(
                    title = "Змейка",
                    description = "Ностальгическая игра",
                    icon = Icons.Outlined.SportsEsports,
                    gradient = listOf(Color(0xFF4CAF50), Color(0xFF1B5E20)),
                    onClick = { /* открыть змейку */ }
                )
            }
            item {
                GameCard(
                    title = "Сапёр",
                    description = "Найди все мины",
                    icon = Icons.Outlined.Room,
                    gradient = listOf(Color(0xFFF44336), Color(0xFFB71C1C)),
                    onClick = { /* открыть сапёра */ }
                )
            }
            item {
                GameCard(
                    title = "Память",
                    description = "Тренируй память",
                    icon = Icons.Outlined.Style,
                    gradient = listOf(Color(0xFF9C27B0), Color(0xFF4A148C)),
                    onClick = { /* открыть игру на память */ }
                )
            }
            item {
                GameCard(
                    title = "Скорость печати",
                    description = "Печатай быстрее",
                    icon = Icons.Outlined.Keyboard,
                    gradient = listOf(Color(0xFF00BCD4), Color(0xFF006064)),
                    onClick = { /* открыть клавиатурный тренажёр */ }
                )
            }
        }
    }
}

@Composable
fun GameCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradient: List<Color>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.9f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = gradient
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                }

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}