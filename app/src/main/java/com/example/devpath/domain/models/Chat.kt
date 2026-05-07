package com.example.devpath.domain.models

import com.google.firebase.Timestamp

data class Chat(
    val chatId: String = "",
    val type: String = "personal",
    val participants: List<String> = emptyList(),
    val name: String = "", val lastMessage: String = "",
    val lastMessageSender: String = "",
    val lastMessageTime: Long = System.currentTimeMillis(),
)