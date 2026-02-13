// ui/viewmodel/ChatHistoryViewModel.kt
package com.example.devpath.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.devpath.data.local.AppDatabase
import com.example.devpath.data.local.dao.ChatSessionDao
import com.example.devpath.domain.models.AIMessage
import com.example.devpath.domain.models.ChatSession
import com.example.devpath.domain.models.StoredMessage
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val currentUserId = Firebase.auth.currentUser?.uid ?: "anonymous"

    init {
        loadSessions()
    }

    /**
     * Загрузить список сохраненных диалогов
     */
    fun loadSessions() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val sessionsList = database.chatSessionDao().getAllSessionsSync(currentUserId)
                _sessions.value = sessionsList
                println("📚 Загружено сессий: ${sessionsList.size}")
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки истории: ${e.message}"
                println("❌ Ошибка загрузки сессий: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Сохранить новый диалог
     */
    suspend fun saveChat(messages: List<AIMessage>): Long {
        if (messages.isEmpty()) {
            println("⚠️ Нет сообщений для сохранения")
            return -1
        }

        return try {
            // Создаем заголовок из первого сообщения пользователя
            val firstUserMessage = messages.firstOrNull { it.isUser }?.text ?: "Новый чат"
            val title = if (firstUserMessage.length > 30) {
                firstUserMessage.take(30) + "..."
            } else {
                firstUserMessage
            }

            // Превью из последнего сообщения
            val preview = messages.lastOrNull()?.text?.take(50) ?: ""

            val session = ChatSession(
                title = title,
                preview = preview,
                messageCount = messages.size,
                userId = currentUserId,
                timestamp = System.currentTimeMillis()
            )

            // Сохраняем сессию
            val sessionId = database.chatSessionDao().insertSession(session)

            // Сохраняем сообщения
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

            println("💾 Чат сохранен: ID=$sessionId, сообщений=${messages.size}")

            // Обновляем список сессий
            loadSessions()

            sessionId
        } catch (e: Exception) {
            _error.value = "Ошибка сохранения: ${e.message}"
            println("❌ Ошибка сохранения чата: ${e.message}")
            e.printStackTrace()
            -1
        }
    }

    /**
     * Удалить диалог
     */
    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            try {
                // Удаляем все сообщения сессии
                database.chatSessionDao().deleteMessages(sessionId)
                // Удаляем саму сессию
                database.chatSessionDao().deleteSessionById(sessionId)

                println("🗑️ Сессия удалена: ID=$sessionId")

                // Обновляем список
                loadSessions()

            } catch (e: Exception) {
                _error.value = "Ошибка удаления: ${e.message}"
                println("❌ Ошибка удаления сессии: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    /**
     * Удалить все диалоги пользователя
     */
    fun clearAllHistory() {
        viewModelScope.launch {
            try {
                database.chatSessionDao().deleteAllSessions(currentUserId)
                println("🧹 Вся история очищена")
                loadSessions()
            } catch (e: Exception) {
                _error.value = "Ошибка очистки истории: ${e.message}"
                println("❌ Ошибка очистки истории: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    /**
     * Очистить ошибку
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Обновить список сессий
     */
    fun refreshSessions() {
        loadSessions()
    }

    override fun onCleared() {
        super.onCleared()
        println("🔄 ChatHistoryViewModel очищен")
    }
}