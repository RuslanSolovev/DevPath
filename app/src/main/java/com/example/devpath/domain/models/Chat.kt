package com.example.devpath.domain.models

import com.google.firebase.Timestamp

data class Chat(
    val chatId: String = "",  // ← это поле должно быть
    val type: String = "personal",
    val participants: List<String> = emptyList(),
    val name: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val lastMessage: String = "",
    val lastMessageTime: Timestamp = Timestamp.now(),
    val createdBy: String = ""
)