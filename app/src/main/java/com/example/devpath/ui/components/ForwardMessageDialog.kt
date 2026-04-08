package com.example.devpath.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.devpath.BuildConfig
import com.example.devpath.domain.models.Chat
import com.example.devpath.domain.models.Message
import kotlinx.coroutines.delay

@Composable
fun ForwardMessageDialog(
    message: Message,
    currentUserId: String,
    chats: List<Chat>,
    onForward: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedChats by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isForwarding by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var successMessageText by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Создаем репозиторий для получения имен пользователей
    val chatRepository = remember {
        com.example.devpath.data.repository.ChatRepository(
            yandexStorageClient = com.example.devpath.data.storage.YandexStorageClient(
                context = context,
                accessKey = BuildConfig.YC_ACCESS_KEY,
                secretKey = BuildConfig.YC_SECRET_KEY,
                bucketName = BuildConfig.YC_BUCKET_NAME
            )
        )
    }

    // Состояние для хранения имен чатов
    var chatDisplayNames by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    // Загружаем имена для всех чатов
    LaunchedEffect(chats) {
        val names = mutableMapOf<String, String>()
        for (chat in chats) {
            val displayName = if (chat.type == "personal") {
                val otherUserId = chat.participants.firstOrNull { it != currentUserId }
                if (otherUserId != null) {
                    val user = chatRepository.getUser(otherUserId)
                    user?.name?.takeIf { it.isNotEmpty() } ?: "Пользователь"
                } else {
                    "Пользователь"
                }
            } else {
                chat.name.takeIf { it.isNotEmpty() } ?: "Групповой чат"
            }
            names[chat.chatId] = displayName
        }
        chatDisplayNames = names
    }

    AlertDialog(
        onDismissRequest = {
            if (!isForwarding) onDismiss()
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Forward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Переслать сообщение",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column {
                Text(
                    "Выберите чаты для пересылки:",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Превью сообщения
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = message.senderName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (message.text.isNotEmpty()) message.text else "📷 Изображение",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Список чатов
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(chats) { chat ->
                        val isSelected = selectedChats.contains(chat.chatId)
                        val displayName = chatDisplayNames[chat.chatId] ?: when {
                            chat.type == "personal" -> "Пользователь"
                            else -> chat.name.ifEmpty { "Групповой чат" }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isForwarding) {
                                    selectedChats = if (isSelected) {
                                        selectedChats - chat.chatId
                                    } else {
                                        selectedChats + chat.chatId
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (chat.type == "personal") Icons.Default.Person else Icons.Default.Group,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = displayName,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyLarge
                            )

                            if (isSelected) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Выбрано",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        if (chats.last() != chat) {
                            Divider()
                        }
                    }
                }

                if (selectedChats.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Выбрано чатов: ${selectedChats.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isForwarding = true
                    val forwardedTo = mutableListOf<String>()
                    selectedChats.forEach { chatId ->
                        onForward(chatId)
                        val displayName = chatDisplayNames[chatId] ?: "Чат"
                        forwardedTo.add(displayName)
                    }

                    successMessageText = if (forwardedTo.size == 1) {
                        "Сообщение переслано в чат \"${forwardedTo.first()}\""
                    } else {
                        "Сообщение переслано в ${forwardedTo.size} чатов"
                    }

                    isForwarding = false
                    showSuccessMessage = true
                },
                enabled = selectedChats.isNotEmpty() && !isForwarding
            ) {
                if (isForwarding) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Пересылка...")
                } else {
                    Text("Переслать (${selectedChats.size})")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isForwarding
            ) {
                Text("Отмена")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )

    // Диалог успеха
    if (showSuccessMessage) {
        AlertDialog(
            onDismissRequest = {
                showSuccessMessage = false
                onDismiss()
            },
            icon = {
                Icon(
                    Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            },
            title = {
                Text(
                    "Успешно!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = successMessageText,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessMessage = false
                        onDismiss()
                    }
                ) {
                    Text("OK")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}