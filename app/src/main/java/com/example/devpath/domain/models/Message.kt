package com.example.devpath.domain.models



data class Reaction(
    val userId: String = "",
    val reaction: String = "",
)



data class Message(
    val messageId: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val imageUrl: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val edited: Boolean = false,
    val deleted: Boolean = false,
    val readBy: List<String> = emptyList(),
    val deliveredTo: List<String> = emptyList(),
    val replyToId: String = "",
    val replyToText: String = "",
    val replyToSenderName: String = "",
    val reactions: List<Reaction> = emptyList()
)