package com.example.devpath.domain.models

import com.google.firebase.Timestamp

data class Reaction(
    val userId: String = "",
    val reaction: String = "", // 👍, ❤️, 😂, 😮, 😢, 😡
    val timestamp: Timestamp = Timestamp.now()
)

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
    val imageUrl: String = "",
    val replyToText: String = "",
    val replyToSenderName: String = "",
    val edited: Boolean = false,
    val editedAt: Timestamp? = null,
    val deleted: Boolean = false,
    val reactions: List<Reaction> = emptyList(), // Добавляем реакции
    val forwardedFrom: String = "", // ID исходного сообщения при пересылке
    val forwardedFromChatId: String = "", // Откуда переслано
    val isForwarded: Boolean = false // Флаг пересланного сообщения
)