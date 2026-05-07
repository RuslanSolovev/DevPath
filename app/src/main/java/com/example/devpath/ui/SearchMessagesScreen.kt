package com.example.devpath.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.devpath.domain.models.Message
import com.example.devpath.ui.viewmodel.ChatsViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchMessagesScreen(
    chatId: String,
    navController: NavHostController
) {
    val viewModel: ChatsViewModel = hiltViewModel()
    val searchResults by viewModel.messageSearchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    var query by remember { mutableStateOf("") }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Поиск в чате") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(androidx.compose.material.icons.Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(28.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(12.dp))
                    OutlinedTextField(
                        value = query,
                        onValueChange = {
                            query = it
                            if (it.length >= 2) viewModel.searchMessagesInChat(chatId, it)
                            else viewModel.clearMessageSearchResults()
                        },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Поиск сообщений...") },
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent)
                    )
                }
            }

            when {
                isSearching -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                searchResults.isEmpty() && query.isNotEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.SearchOff, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Сообщения не найдены", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                searchResults.isNotEmpty() -> LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(searchResults, key = { it.messageId }) { message ->
                        MessageSearchResultItem(message = message, timeFormatter = timeFormatter, navController = navController)
                    }
                }
            }
        }
    }
}

@Composable
fun MessageSearchResultItem(
    message: Message,
    timeFormatter: SimpleDateFormat,
    navController: NavHostController
) {
    val messageTime = if (message.timestamp > 0) Date(message.timestamp) else Date()

    Card(modifier = Modifier.fillMaxWidth().clickable {
        navController.previousBackStackEntry?.savedStateHandle?.set("scrollToMessageId", message.messageId)
        navController.popBackStack()
    }, shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(message.senderName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                Text(timeFormatter.format(messageTime), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(message.text.ifEmpty { if (message.imageUrl.isNotEmpty()) "📷 Изображение" else "Сообщение" }, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
        }
    }
}