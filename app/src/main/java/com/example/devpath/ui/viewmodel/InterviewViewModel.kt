// ui/viewmodel/InterviewViewModel.kt
package com.example.devpath.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.devpath.api.GigaChatService
import com.example.devpath.api.models.GigaChatMessage
import com.example.devpath.ui.InterviewStep
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InterviewViewModel @Inject constructor(
    private val gigaChatService: GigaChatService
) : ViewModel() {

    private val _answers = MutableStateFlow<Map<Int, String>>(emptyMap())
    val answers: StateFlow<Map<Int, String>> = _answers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _analysisResult = MutableStateFlow<String?>(null)
    val analysisResult: StateFlow<String?> = _analysisResult.asStateFlow()

    fun saveAnswer(questionId: Int, answer: String) {
        _answers.value = _answers.value + (questionId to answer)
    }

    fun loadAnswers() {
        // Здесь можно загрузить ответы из SharedPreferences или Room
        // Пока просто оставляем пустым
    }

    fun clearAnswers() {
        _answers.value = emptyMap()
    }

    fun analyzeAnswer(
        question: String,
        userAnswer: String,
        tips: List<String>
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val prompt = """
                    Ты - опытный технический интервьюер в IT-компании.
                    Проанализируй ответ кандидата на вопрос собеседования.
                    
                    Вопрос: $question
                    
                    Ответ кандидата: $userAnswer
                    
                    Подсказки для идеального ответа: ${tips.joinToString(", ")}
                    
                    Проанализируй ответ по следующим критериям:
                    1. Полнота ответа (0-10 баллов)
                    2. Техническая точность (0-10 баллов)  
                    3. Структурированность (0-10 баллов)
                    4. Приведение примеров (0-10 баллов)
                    5. Ясность изложения (0-10 баллов)
                    
                    Для каждого критерия поставь оценку и напиши комментарий.
                    В конце дай общую оценку (0-10) и рекомендации по улучшению.
                    
                    Отвечай на русском языке, используй понятное форматирование.
                    Будь конструктивным и доброжелательным.
                """.trimIndent()

                val messages = listOf(
                    GigaChatMessage(
                        role = "system",
                        content = "Ты - опытный технический интервьюер в сфере Android разработки. Анализируй ответы кандидатов и давай конструктивную обратную связь. Отвечай подробно, структурированно и по делу."
                    ),
                    GigaChatMessage(
                        role = "user",
                        content = prompt
                    )
                )

                val result = gigaChatService.sendMessage(
                    messages = messages,
                    maxTokens = 4096
                )

                if (result.isSuccess) {
                    val response = result.getOrNull()
                    val analysis = response?.choices?.firstOrNull()?.message?.content
                        ?: "Не удалось получить анализ. Попробуйте еще раз."

                    _analysisResult.value = analysis
                } else {
                    val exception = result.exceptionOrNull()
                    _error.value = "Ошибка анализа: ${exception?.message ?: "Неизвестная ошибка"}"
                    _analysisResult.value = "Произошла ошибка при анализе ответа. Пожалуйста, попробуйте еще раз."
                }
            } catch (e: Exception) {
                _error.value = "Исключение: ${e.message}"
                _analysisResult.value = "Ошибка при анализе ответа: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun analyzeFullInterview(
        interviewSteps: List<InterviewStep>,
        answers: Map<Int, String>
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val answeredQuestions = interviewSteps.filter { answers.containsKey(it.id - 1) }

                if (answeredQuestions.isEmpty()) {
                    _analysisResult.value = "Нет ответов для анализа. Ответьте хотя бы на один вопрос."
                    return@launch
                }

                val prompt = """
                    Ты - опытный HR-специалист и технический интервьюер.
                    Проанализируй все ответы кандидата на собеседовании и дай общую обратную связь.
                    
                    Отвеченные вопросы:
                    ${answeredQuestions.joinToString("\n\n") { step ->
                    val answer = answers[step.id - 1] ?: ""
                    """
                        === Вопрос ${step.id}: ${step.title} ===
                        Вопрос: ${step.question}
                        Ответ кандидата: ${answer.take(500)}${if (answer.length > 500) "..." else ""}
                        """.trimIndent()
                }}
                    
                    Проведи детальный анализ по следующим пунктам:
                    
                    1. ОБЩИЙ УРОВЕНЬ ЗНАНИЙ (0-10)
                    - Оценка технической подготовки
                    - Понимание концепций
                    - Широта кругозора
                    
                    2. СИЛЬНЫЕ СТОРОНЫ КАНДИДАТА
                    - В каких вопросах показал лучшие результаты
                    - Какие навыки выделяются
                    
                    3. ОБЛАСТИ ДЛЯ УЛУЧШЕНИЯ
                    - Слабые места
                    - Что нужно подтянуть
                    
                    4. КОММУНИКАЦИЯ И ПОДАЧА
                    - Структурированность ответов
                    - Умение объяснять
                    - Примеры из опыта
                    
                    5. РЕКОМЕНДАЦИИ ПО ДАЛЬНЕЙШЕМУ ОБУЧЕНИЮ
                    - Конкретные темы для изучения
                    - Ресурсы и материалы
                    - Практические задания
                    
                    6. ОЦЕНКА ГОТОВНОСТИ К РЕАЛЬНОМУ СОБЕСЕДОВАНИЮ
                    - Уровень (Junior/Middle/Senior)
                    - Что нужно улучшить перед собеседованием
                    - Прогноз успеха
                    
                    Отвечай на русском языке, будь конкретен и давай полезные рекомендации.
                    Форматируй ответ для удобного чтения.
                """.trimIndent()

                val messages = listOf(
                    GigaChatMessage(
                        role = "system",
                        content = "Ты - Senior Android разработчик и HR-специалист. Проводишь комплексный анализ собеседований. Даешь объективную оценку и практические рекомендации. Твои советы помогают кандидатам реально улучшить свои навыки."
                    ),
                    GigaChatMessage(
                        role = "user",
                        content = prompt
                    )
                )

                val result = gigaChatService.sendMessage(
                    messages = messages,
                    maxTokens = 8192
                )

                if (result.isSuccess) {
                    val response = result.getOrNull()
                    val analysis = response?.choices?.firstOrNull()?.message?.content
                        ?: "Не удалось получить полный анализ."

                    _analysisResult.value = analysis
                } else {
                    val exception = result.exceptionOrNull()
                    _error.value = "Ошибка анализа: ${exception?.message ?: "Неизвестная ошибка"}"
                    _analysisResult.value = "Произошла ошибка при полном анализе собеседования. Пожалуйста, попробуйте еще раз."
                }
            } catch (e: Exception) {
                _error.value = "Исключение: ${e.message}"
                _analysisResult.value = "Ошибка при полном анализе: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearAnalysis() {
        _analysisResult.value = null
    }

    fun clearError() {
        _error.value = null
    }
}