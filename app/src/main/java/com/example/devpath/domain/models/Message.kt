package com.example.devpath.domain.models

import com.google.firebase.Timestamp

data class Message(
    val messageId: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val readBy: List<String> = emptyList()
)