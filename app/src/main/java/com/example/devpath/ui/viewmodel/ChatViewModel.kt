// ui/viewmodel/ChatViewModel.kt
package com.example.devpath.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.devpath.api.GigaChatService
import com.example.devpath.api.models.GigaChatMessage
import com.example.devpath.domain.models.AIMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val gigaChatService: GigaChatService
) : ViewModel() {

    private val _messages = MutableStateFlow<List<AIMessage>>(emptyList())
    val messages: StateFlow<List<AIMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

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
                        println("GigaChat: prompt=${it.promptTokens}, completion=${it.completionTokens}, total=${it.totalTokens}")
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

    fun handleExampleQuestion(questionType: String) {
        val question = when (questionType) {
            "val_var" -> "Объясни разницу между val и var в Kotlin. Приведи примеры использования и объясни, когда что использовать."
            "higher_order" -> "Что такое функции высшего порядка в Kotlin? Покажи несколько примеров с объяснениями."
            "coroutines" -> "Объясни, что такое корутины в Kotlin и как они отличаются от потоков. Приведи пример использования корутин в Android."
            "interview_tips" -> "Дай советы по подготовке к собеседованию на Android разработчика. Какие вопросы чаще всего задают и как на них отвечать?"
            else -> questionType
        }
        sendMessage(question)
    }

    fun clearError() {
        _error.value = null
    }

    fun clearChat() {
        _messages.value = emptyList()
    }
}