package com.example.devpath.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.devpath.ui.viewmodel.ChatsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(
    userId: String,
    navController: NavHostController = androidx.navigation.compose.rememberNavController()
) {
    val viewModel: ChatsViewModel = hiltViewModel()
    val chats by viewModel.chats.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadChats(userId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Чаты") },
                actions = {
                    IconButton(onClick = { navController.navigate("friends") }) {
                        Icon(Icons.Default.People, contentDescription = "Друзья")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(chats) { chat ->
                ChatItem(
                    chat = chat,
                    onClick = {
                        navController.navigate("chat_detail/${chat.chatId}")
                    }
                )
                Divider()
            }
        }
    }
}

@Composable
fun ChatItem(chat: com.example.devpath.domain.models.Chat, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (chat.type == "personal") Icons.Default.Person else Icons.Default.Group,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Column(
                modifier = Modifier.weight(1f).padding(start = 12.dp)
            ) {
                Text(
                    text = if (chat.type == "personal") {
                        chat.name.ifEmpty { "Чат" }
                    } else {
                        chat.name.ifEmpty { "Групповой чат" }
                    },
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = chat.lastMessage.ifEmpty { "Нет сообщений" },
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )
            }
            Text(
                text = "•",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}