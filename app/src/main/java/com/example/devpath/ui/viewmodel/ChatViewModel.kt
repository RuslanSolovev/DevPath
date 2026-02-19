package com.example.devpath.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
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
    private val database: AppDatabase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Ключи для SavedStateHandle
    companion object {
        private const val KEY_MESSAGES = "messages"
        private const val KEY_IS_LOADING = "isLoading"
        private const val KEY_SAVED_SESSION_ID = "savedSessionId"
        private const val KEY_CURRENT_SESSION_TITLE = "currentSessionTitle"
        private const val KEY_IS_HISTORY_MODE = "isHistoryMode"
        const val MAX_HISTORY_MESSAGES = 50
    }

    // Восстанавливаем состояние из SavedStateHandle
    private val _messages = MutableStateFlow(
        savedStateHandle.get<List<AIMessage>>(KEY_MESSAGES) ?: emptyList()
    )
    val messages: StateFlow<List<AIMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(
        savedStateHandle.get<Boolean>(KEY_IS_LOADING) ?: false
    )
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _success = MutableStateFlow<String?>(null)
    val success: StateFlow<String?> = _success.asStateFlow()

    private val _savedSessionId = MutableStateFlow<Long?>(
        savedStateHandle.get(KEY_SAVED_SESSION_ID)
    )
    val savedSessionId: StateFlow<Long?> = _savedSessionId.asStateFlow()

    private val _currentSessionTitle = MutableStateFlow<String?>(
        savedStateHandle.get(KEY_CURRENT_SESSION_TITLE)
    )
    val currentSessionTitle: StateFlow<String?> = _currentSessionTitle.asStateFlow()

    private val _isHistoryMode = MutableStateFlow(
        savedStateHandle.get<Boolean>(KEY_IS_HISTORY_MODE) ?: false
    )
    val isHistoryMode: StateFlow<Boolean> = _isHistoryMode.asStateFlow()

    private val currentUserId = Firebase.auth.currentUser?.uid ?: "anonymous"

    private val systemPrompt = """
        Ты - эксперт по программированию на Kotlin и Android разработке.
        Отвечай на русском языке.
        Будь дружелюбным и полезным.
        Форматируй код с помощью ```kotlin и ```.
        Давай подробные объяснения с примерами.
        Если вопрос не связан с программированием, обязательно ответь на него но в конце предложи продолжить заниматься программированием.
        Отвечай максимально подробно и информативно.
    """.trimIndent()

    init {
        // Логируем восстановление
        val restoredCount = _messages.value.size
        if (restoredCount > 0) {
            println("🔄 ChatViewModel: Восстановлено $restoredCount сообщений")
        }
        println("🔄 ChatViewModel: Состояние загружено из SavedStateHandle")
    }

    // Сохраняем состояние в SavedStateHandle
    private fun saveState() {
        savedStateHandle[KEY_MESSAGES] = _messages.value
        savedStateHandle[KEY_IS_LOADING] = _isLoading.value
        savedStateHandle[KEY_SAVED_SESSION_ID] = _savedSessionId.value
        savedStateHandle[KEY_CURRENT_SESSION_TITLE] = _currentSessionTitle.value
        savedStateHandle[KEY_IS_HISTORY_MODE] = _isHistoryMode.value
    }

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        viewModelScope.launch {
            try {
                val userAIMessage = AIMessage(
                    text = userMessage,
                    isUser = true
                )
                _messages.update { it + userAIMessage }
                saveState()

                _isLoading.value = true
                saveState()
                _error.value = null
                _success.value = null

                val contextMessages = buildList {
                    add(GigaChatMessage(
                        role = "system",
                        content = systemPrompt
                    ))

                    val recentMessages = _messages.value.takeLast(MAX_HISTORY_MESSAGES)
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
                    saveState()

                    val usage = response?.usage
                    usage?.let {
                        println("📊 GigaChat: prompt=${it.promptTokens}, completion=${it.completionTokens}, total=${it.totalTokens}")
                    }

                    if (_isHistoryMode.value) {
                        _isHistoryMode.value = false
                        saveState()
                    }
                } else {
                    val exception = result.exceptionOrNull()
                    _error.value = "Ошибка: ${exception?.message ?: "Неизвестная ошибка"}"

                    val errorMessage = AIMessage(
                        text = "Извините, произошла ошибка при подключении к GigaChat. Пожалуйста, попробуйте еще раз.",
                        isUser = false
                    )
                    _messages.update { it + errorMessage }
                    saveState()
                }
            } catch (e: Exception) {
                _error.value = "Исключение: ${e.message}"
                e.printStackTrace()

                val errorMessage = AIMessage(
                    text = "Произошла непредвиденная ошибка. Пожалуйста, проверьте соединение с интернетом.",
                    isUser = false
                )
                _messages.update { it + errorMessage }
                saveState()
            } finally {
                _isLoading.value = false
                saveState()
            }
        }
    }

    fun saveCurrentChat(customTitle: String? = null) {
        viewModelScope.launch {
            try {
                val messages = _messages.value
                if (messages.isEmpty()) {
                    _error.value = "Нет сообщений для сохранения"
                    return@launch
                }

                if (_savedSessionId.value != null && !_isHistoryMode.value) {
                    updateExistingChat(customTitle)
                    return@launch
                }

                val title = when {
                    !customTitle.isNullOrBlank() -> customTitle
                    else -> {
                        val firstUserMessage = messages.firstOrNull { it.isUser }?.text ?: "Новый чат"
                        if (firstUserMessage.length > 30) firstUserMessage.take(30) + "..." else firstUserMessage
                    }
                }

                val preview = messages.lastOrNull()?.text?.take(50) ?: ""

                val session = ChatSession(
                    title = title,
                    preview = preview,
                    messageCount = messages.size,
                    userId = currentUserId,
                    timestamp = System.currentTimeMillis()
                )

                val sessionId = database.chatSessionDao().insertSession(session)
                _savedSessionId.value = sessionId
                _currentSessionTitle.value = title
                _isHistoryMode.value = false
                saveState()

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

                _success.value = "✅ Диалог сохранен как \"$title\""

                viewModelScope.launch {
                    delay(3000)
                    _success.value = null
                }

                println("💾 Чат сохранен: ID=$sessionId, название=$title, сообщений=${messages.size}")

            } catch (e: Exception) {
                _error.value = "Ошибка сохранения: ${e.message}"
                println("❌ Ошибка сохранения чата: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private suspend fun updateExistingChat(customTitle: String? = null) {
        try {
            val sessionId = _savedSessionId.value ?: return
            val messages = _messages.value
            val oldSession = database.chatSessionDao().getSession(sessionId)

            if (oldSession == null) {
                _savedSessionId.value = null
                saveState()
                saveCurrentChat(customTitle)
                return
            }

            val title = if (!customTitle.isNullOrBlank()) {
                customTitle
            } else {
                _currentSessionTitle.value ?: oldSession.title
            }

            val preview = messages.lastOrNull()?.text?.take(50) ?: oldSession.preview

            val updatedSession = oldSession.copy(
                title = title,
                preview = preview,
                messageCount = messages.size,
                timestamp = System.currentTimeMillis()
            )

            database.chatSessionDao().updateSession(updatedSession)
            _currentSessionTitle.value = title
            saveState()

            database.chatSessionDao().deleteMessages(sessionId)

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

            _success.value = "✅ Диалог обновлен как \"$title\""

            viewModelScope.launch {
                delay(3000)
                _success.value = null
            }

            println("🔄 Чат обновлен: ID=$sessionId, название=$title, сообщений=${messages.size}")

        } catch (e: Exception) {
            _error.value = "Ошибка обновления: ${e.message}"
            println("❌ Ошибка обновления чата: ${e.message}")
            e.printStackTrace()
        }
    }

    fun loadChatSession(sessionId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                saveState()
                _error.value = null
                _isHistoryMode.value = true
                saveState()

                val messagesFlow = database.chatSessionDao().getMessages(sessionId)
                messagesFlow.collect { storedMessages ->
                    val loadedMessages = storedMessages.sortedBy { it.orderIndex }.map {
                        AIMessage(
                            text = it.text,
                            isUser = it.isUser,
                            timestamp = it.timestamp
                        )
                    }
                    _messages.value = loadedMessages
                    _savedSessionId.value = sessionId
                    saveState()

                    val session = database.chatSessionDao().getSession(sessionId)
                    _currentSessionTitle.value = session?.title ?: "Загруженный чат"
                    saveState()

                    _success.value = "✅ Диалог загружен"

                    viewModelScope.launch {
                        delay(2000)
                        _success.value = null
                    }

                    println("📂 Чат загружен: ID=$sessionId, сообщений=${loadedMessages.size}")

                    _isLoading.value = false
                    saveState()
                }

            } catch (e: Exception) {
                _error.value = "Ошибка загрузки: ${e.message}"
                println("❌ Ошибка загрузки чата: ${e.message}")
                e.printStackTrace()
                _isLoading.value = false
                _isHistoryMode.value = false
                saveState()
            }
        }
    }

    fun clearChat() {
        _messages.value = emptyList()
        _error.value = null
        _success.value = null
        _savedSessionId.value = null
        _currentSessionTitle.value = null
        _isHistoryMode.value = false
        saveState()
        println("🧹 Чат очищен - начата новая тема")
    }

    fun deleteSavedChat(sessionId: Long) {
        viewModelScope.launch {
            try {
                val session = database.chatSessionDao().getSession(sessionId)
                session?.let {
                    database.chatSessionDao().deleteMessages(sessionId)
                    database.chatSessionDao().deleteSession(it)
                    println("🗑️ Чат удален: ID=$sessionId")

                    if (_savedSessionId.value == sessionId) {
                        _savedSessionId.value = null
                        _currentSessionTitle.value = null
                        _messages.value = emptyList()
                        _isHistoryMode.value = false
                        saveState()
                    }

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

    fun forceResetLoading() {
        _isLoading.value = false
        saveState()
        println("🔄 Принудительный сброс isLoading")
    }

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

    fun clearError() {
        _error.value = null
    }

    fun clearSuccess() {
        _success.value = null
    }

    fun isCurrentChatSaved(): Boolean {
        return _savedSessionId.value != null && !_isHistoryMode.value
    }

    override fun onCleared() {
        // Сохраняем состояние перед очисткой
        saveState()
        println("🔄 ChatViewModel очищен, состояние сохранено в SavedStateHandle")
        super.onCleared()
    }
}