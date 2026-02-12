// ui/viewmodel/ChatHistoryViewModel.kt
package com.example.devpath.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.devpath.data.local.AppDatabase
import com.example.devpath.domain.models.AIMessage
import com.example.devpath.domain.models.ChatSession
import com.example.devpath.domain.models.StoredMessage
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatHistoryViewModel @Inject constructor(
    private val database: AppDatabase
) : ViewModel() {

    private val _sessions = MutableStateFlow<List<ChatSession>>(emptyList())
    val sessions: StateFlow<List<ChatSession>> = _sessions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val currentUserId = Firebase.auth.currentUser?.uid ?: "anonymous"

    init {
        loadSessions()
    }

    fun loadSessions() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                database.chatSessionDao().getAllSessions(currentUserId).collect { sessions ->
                    _sessions.value = sessions
                }
            } catch (e: Exception) {
                println("❌ Error loading sessions: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun saveChat(messages: List<AIMessage>): Long {
        if (messages.isEmpty()) return -1

        val firstMessage = messages.firstOrNull { it.isUser }?.text ?: "Новый чат"
        val title = if (firstMessage.length > 30) firstMessage.take(30) + "..." else firstMessage

        val session = ChatSession(
            title = title,
            preview = messages.lastOrNull()?.text?.take(50) ?: "",
            messageCount = messages.size,
            userId = currentUserId
        )

        val sessionId = database.chatSessionDao().insertSession(session)

        val storedMessages = messages.mapIndexed { index, msg ->
            StoredMessage(
                sessionId = sessionId,
                text = msg.text,
                isUser = msg.isUser,
                timestamp = msg.timestamp,
                orderIndex = index
            )
        }

        database.chatSessionDao().insertMessages(*storedMessages.toTypedArray())
        return sessionId
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            val session = database.chatSessionDao().getSession(sessionId)
            session?.let {
                database.chatSessionDao().deleteMessages(sessionId)
                database.chatSessionDao().deleteSession(it)
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            database.chatSessionDao().deleteAllSessions(currentUserId)
        }
    }
}