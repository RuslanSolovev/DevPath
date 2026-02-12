// ui/viewmodel/ChatViewModel.kt
package com.example.devpath.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.devpath.api.GigaChatService
import com.example.devpath.api.models.GigaChatMessage
import com.example.devpath.data.local.AppDatabase
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val gigaChatService: GigaChatService,
    private val database: AppDatabase
) : ViewModel() {

    private val _messages = MutableStateFlow<List<AIMessage>>(emptyList())
    val messages: StateFlow<List<AIMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _success = MutableStateFlow<String?>(null)
    val success: StateFlow<String?> = _success.asStateFlow()

    private val _savedSessionId = MutableStateFlow<Long?>(null)
    val savedSessionId: StateFlow<Long?> = _savedSessionId.asStateFlow()

    private val currentUserId = Firebase.auth.currentUser?.uid ?: "anonymous"

    // Системный промпт
    private val systemPrompt = """
        Ты - эксперт по программированию на Kotlin и Android разработке.
        Отвечай на русском языке.
        Будь дружелюбным и полезным.
        Форматируй код с помощью ```kotlin и ```.
        Давай подробные объяснения с примерами.
        Если вопрос не связан с программированием, вежливо отклони и предложи задать вопрос по теме.
        Отвечай максимально подробно и информативно.
    """.trimIndent()

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        viewModelScope.launch {
            try {
                val userAIMessage = AIMessage(
                    text = userMessage,
                    isUser = true
                )
                _messages.update { it + userAIMessage }

                _isLoading.value = true
                _error.value = null
                _success.value = null

                val contextMessages = buildList {
                    add(GigaChatMessage(
                        role = "system",
                        content = systemPrompt
                    ))

                    val recentMessages = _messages.value.takeLast(10)
                    recentMessages.forEach { msg ->
                        add(GigaChatMessage(
                            role = if (msg.isUser) "user" else "assistant",
                            content = msg.text
                        ))
                    }
                }

                val result = gigaChatService.sendMessage(
                    messages = contextMessages,
                    maxTokens = 4096
                )

                if (result.isSuccess) {
                    val response = result.getOrNull()
                    val aiMessage = response?.choices?.firstOrNull()?.message?.content
                        ?: "Не удалось получить ответ"

                    val aiAIMessage = AIMessage(
                        text = aiMessage,
                        isUser = false
                    )
                    _messages.update { it + aiAIMessage }

                    val usage = response?.usage
                    usage?.let {
                        println("📊 GigaChat: prompt=${it.promptTokens}, completion=${it.completionTokens}, total=${it.totalTokens}")
                    }
                } else {
                    val exception = result.exceptionOrNull()
                    _error.value = "Ошибка: ${exception?.message ?: "Неизвестная ошибка"}"

                    val errorMessage = AIMessage(
                        text = "Извините, произошла ошибка при подключении к GigaChat. Пожалуйста, попробуйте еще раз.",
                        isUser = false
                    )
                    _messages.update { it + errorMessage }
                }
            } catch (e: Exception) {
                _error.value = "Исключение: ${e.message}"
                e.printStackTrace()

                val errorMessage = AIMessage(
                    text = "Произошла непредвиденная ошибка. Пожалуйста, проверьте соединение с интернетом.",
                    isUser = false
                )
                _messages.update { it + errorMessage }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * ✅ СОХРАНИТЬ ТЕКУЩИЙ ЧАТ В ROOM
     */
    fun saveCurrentChat() {
        viewModelScope.launch {
            try {
                val messages = _messages.value
                if (messages.isEmpty()) {
                    _error.value = "Нет сообщений для сохранения"
                    return@launch
                }

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
                _savedSessionId.value = sessionId

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

                _success.value = "✅ Диалог сохранен"

                // Сбрасываем сообщение через 3 секунды
                viewModelScope.launch {
                    delay(3000)
                    _success.value = null
                    _savedSessionId.value = null
                }

                println("💾 Чат сохранен: ID=$sessionId, сообщений=${messages.size}")

            } catch (e: Exception) {
                _error.value = "Ошибка сохранения: ${e.message}"
                println("❌ Ошибка сохранения чата: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    /**
     * ✅ ЗАГРУЗИТЬ СОХРАНЕННЫЙ ЧАТ ПО ID
     */
    fun loadChatSession(sessionId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val messages = database.chatSessionDao().getMessages(sessionId)
                // Это Flow, нужно собирать
                messages.collect { storedMessages ->
                    val loadedMessages = storedMessages.map {
                        AIMessage(
                            text = it.text,
                            isUser = it.isUser,
                            timestamp = it.timestamp
                        )
                    }
                    _messages.value = loadedMessages
                    println("📂 Чат загружен: ID=$sessionId, сообщений=${loadedMessages.size}")
                }

            } catch (e: Exception) {
                _error.value = "Ошибка загрузки: ${e.message}"
                println("❌ Ошибка загрузки чата: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * ✅ ОЧИСТИТЬ ЧАТ
     */
    fun clearChat() {
        _messages.value = emptyList()
        _error.value = null
        _success.value = null
        _savedSessionId.value = null
        println("🧹 Чат очищен")
    }

    /**
     * ✅ УДАЛИТЬ СОХРАНЕННЫЙ ЧАТ
     */
    fun deleteSavedChat(sessionId: Long) {
        viewModelScope.launch {
            try {
                val session = database.chatSessionDao().getSession(sessionId)
                session?.let {
                    database.chatSessionDao().deleteMessages(sessionId)
                    database.chatSessionDao().deleteSession(it)
                    println("🗑️ Чат удален: ID=$sessionId")
                    _success.value = "✅ Чат удален"

                    viewModelScope.launch {
                        delay(2000)
                        _success.value = null
                    }
                }
            } catch (e: Exception) {
                _error.value = "Ошибка удаления: ${e.message}"
                println("❌ Ошибка удаления чата: ${e.message}")
            }
        }
    }

    /**
     * ✅ ОБРАБОТКА ПРИМЕРОВ ВОПРОСОВ
     */
    fun handleExampleQuestion(questionType: String) {
        val question = when (questionType) {
            "val_var" -> "Объясни разницу между val и var в Kotlin. Приведи примеры использования и объясни, когда что использовать."
            "higher_order" -> "Что такое функции высшего порядка в Kotlin? Покажи несколько примеров с объяснениями."
            "coroutines" -> "Объясни, что такое корутины в Kotlin и как они отличаются от потоков. Приведи пример использования корутин в Android."
            "interview_tips" -> "Дай советы по подготовке к собеседованию на Android разработчика. Какие вопросы чаще всего задают и как на них отвечать?"
            "null_safety" -> "Как работает null safety в Kotlin? Объясни операторы ?., ?:, !! и let."
            "collections" -> "Объясни разницу между List, Set и Map в Kotlin. Приведи примеры."
            "flow" -> "Что такое Flow в Kotlin и как его использовать с корутинами?"
            else -> questionType
        }
        sendMessage(question)
    }

    /**
     * ✅ ПОЛУЧИТЬ СТАТИСТИКУ ЧАТА
     */
    fun getChatStats(): String {
        val messages = _messages.value
        val userMessages = messages.count { it.isUser }
        val aiMessages = messages.count { !it.isUser }
        val totalChars = messages.sumOf { it.text.length }

        return """
            📊 Статистика чата:
            ─────────────────
            Всего сообщений: ${messages.size}
            Вы: $userMessages
            GigaChat: $aiMessages
            Всего символов: $totalChars
            Средняя длина: ${if (messages.isNotEmpty()) totalChars / messages.size else 0}
        """.trimIndent()
    }

    /**
     * ✅ ОЧИСТИТЬ ОШИБКУ
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * ✅ ОЧИСТИТЬ СООБЩЕНИЕ ОБ УСПЕХЕ
     */
    fun clearSuccess() {
        _success.value = null
    }

    /**
     * ✅ ПРОВЕРИТЬ, СОХРАНЕН ЛИ ТЕКУЩИЙ ЧАТ
     */
    fun isCurrentChatSaved(): Boolean {
        return _savedSessionId.value != null
    }

    override fun onCleared() {
        super.onCleared()
        println("🔄 ChatViewModel очищен")
    }
}