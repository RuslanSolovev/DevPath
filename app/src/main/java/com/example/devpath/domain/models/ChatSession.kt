// domain/models/ChatSession.kt
package com.example.devpath.domain.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "chat_sessions",
    indices = [
        Index(value = ["userId"], name = "index_chat_sessions_userId")
    ]
)
data class ChatSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val preview: String,
    val timestamp: Long = System.currentTimeMillis(),
    val messageCount: Int,
    val userId: String
)



@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["sessionId"], name = "index_chat_messages_sessionId")
    ]
)
data class StoredMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long,
    val orderIndex: Int
)