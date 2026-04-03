package com.example.devpath.domain.models

import com.google.firebase.Timestamp

data class Message(
    val messageId: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val readBy: List<String> = emptyList(),
    val deliveredTo: List<String> = emptyList(),
    val replyToId: String = "",
    val replyToText: String = "",
    val replyToSenderName: String = "",
    val edited: Boolean = false,        // ← было isEdited, стало edited
    val editedAt: Timestamp? = null,
    val deleted: Boolean = false        // ← было isDeleted, стало deleted
)