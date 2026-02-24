// ui/viewmodel/InterviewViewModel.kt
package com.example.devpath.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.devpath.api.GigaChatService
import com.example.devpath.api.models.GigaChatMessage
import com.example.devpath.ui.EnhancedInterviewStep
import com.example.devpath.ui.ResumeData
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
    }

    fun clearAnswers() {
        _answers.value = emptyMap()
    }

    fun analyzeFullInterview(
        interviewSteps: List<EnhancedInterviewStep>,
        answers: Map<Int, String>,
        resumeData: ResumeData? = null // Добавляем параметр резюме
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val answeredQuestions = interviewSteps.filter { answers.containsKey(it.id - 1) }
                val unansweredQuestions = interviewSteps.filter { !answers.containsKey(it.id - 1) }

                if (answeredQuestions.isEmpty()) {
                    _analysisResult.value = "📝 **Нет ответов для анализа**\n\nОтветьте хотя бы на один вопрос, чтобы получить обратную связь."
                    return@launch
                }

                val totalScore = calculateTotalScore(answers, interviewSteps)
                val averageDifficulty = if (interviewSteps.isNotEmpty()) {
                    interviewSteps.sumOf { it.difficulty } / interviewSteps.size
                } else 0

                val prompt = buildString {
                    appendLine("Ты — Senior Android разработчик и HR-специалист с 10+ годами опыта.")
                    appendLine("Проведи максимально подробный, честный и конструктивный анализ собеседования кандидата на позицию Android Developer.")
                    appendLine()

                    // Добавляем информацию из резюме, если она есть
                    if (resumeData?.hasData() == true) {
                        appendLine("📄 **ИНФОРМАЦИЯ О КАНДИДАТЕ ИЗ РЕЗЮМЕ:**")
                        appendLine(resumeData.toPrompt())
                        appendLine()
                        appendLine("---")
                        appendLine()
                    }

                    appendLine("🎯 **ЦЕЛЬ**: Помочь кандидату реально улучшить свои навыки и подготовиться к реальному собеседованию.")
                    appendLine()
                    appendLine("📊 **СТАТИСТИКА СОБЕСЕДОВАНИЯ**:")
                    appendLine("• Всего вопросов: ${interviewSteps.size}")
                    appendLine("• Отвечено: ${answeredQuestions.size}")
                    appendLine("• Пропущено: ${unansweredQuestions.size}")
                    appendLine("• Средняя сложность: $averageDifficulty/5")
                    appendLine("• Общая оценка (предварительная): ${String.format("%.1f", totalScore)}/10")
                    appendLine()
                    appendLine("📋 **ДЕТАЛИ ПО ВОПРОСАМ**:")
                    appendLine()

                    answeredQuestions.forEachIndexed { index, step ->
                        val answer = answers[step.id - 1] ?: ""
                        appendLine("=== ВОПРОС ${index + 1} [ID:${step.id}] ===")
                        appendLine("📌 Категория: ${step.category}")
                        appendLine("🔥 Сложность: ${step.difficulty}/5")
                        appendLine("❓ Вопрос: ${step.question}")
                        appendLine("💬 Ответ кандидата: ${if (answer.isNotBlank()) answer else "НЕТ ОТВЕТА (пропущено)"}")
                        appendLine("💡 Ожидалось: ${step.tips.joinToString("; ")}")
                        appendLine()
                    }

                    if (unansweredQuestions.isNotEmpty()) {
                        appendLine("⚠️ **ПРОПУЩЕННЫЕ ВОПРОСЫ**:")
                        unansweredQuestions.forEach { step ->
                            appendLine("• ${step.title} (${step.category}, сложность ${step.difficulty}/5)")
                        }
                        appendLine()
                    }

                    appendLine("""
                        🎯 **СТРУКТУРА АНАЛИЗА**:

                        📊 **1. ОБЩАЯ ОЦЕНКА (0-10)**
                        • Общий балл: [число]
                        • Уровень кандидата: (Junior / Middle / Senior)
                        • Краткое резюме собеседования (2-3 предложения)

                        ⭐ **2. СИЛЬНЫЕ СТОРОНЫ**
                        • [Что получилось отлично]
                        • [Какие темы кандидат знает хорошо]
                        • [Примеры удачных ответов]

                        🔧 **3. ЗОНЫ РОСТА**
                        • [Что нужно улучшить в первую очередь]
                        • [Типичные ошибки]
                        • [Сложные темы, которые вызвали затруднения]

                        📈 **4. ОЦЕНКА ПО КОМПЕТЕНЦИЯМ**
                        • Kotlin Basics: [оценка/10] — [комментарий]
                        • Android Development: [оценка/10] — [комментарий]
                        • Архитектура: [оценка/10] — [комментарий]
                        • Алгоритмы: [оценка/10] — [комментарий]
                        • Soft Skills: [оценка/10] — [комментарий]

                        🎓 **5. ПЛАН ПОДГОТОВКИ**
                        • [Темы для изучения с приоритетами]
                        • [Ресурсы: книги, курсы, документация]
                        • [Практические задания для закрепления]

                        💡 **6. СОВЕТЫ ПЕРЕД РЕАЛЬНЫМ СОБЕСЕДОВАНИЕМ**
                        • [Как презентовать себя]
                        • [На что обратить внимание]
                        • [Типичные подводные камни]

                        🏆 **7. ИТОГОВЫЙ ВЕРДИКТ**
                        • Готовность к реальному собеседованию: (Низкая / Средняя / Высокая)
                        • Прогноз успеха: [процент]
                        • Главные рекомендации (топ-3)

                        Форматируй ответ красиво, используй эмодзи, делай его максимально полезным и мотивирующим.
                        Пиши на русском языке, будь доброжелательным, но объективным.
                    """.trimIndent())
                }

                val messages = listOf(
                    GigaChatMessage(
                        role = "system",
                        content = "Ты — ментор с многолетним опытом подготовки Android-разработчиков. Твои анализы настолько полезны, что кандидаты сохраняют их и возвращаются к ним спустя годы. Ты умеешь находить баланс между поддержкой и честной критикой. Каждый твой разбор — это готовый план действий для роста."
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

                    _analysisResult.value = formatAnalysisResult(analysis, totalScore)
                } else {
                    val exception = result.exceptionOrNull()
                    _error.value = "Ошибка анализа: ${exception?.message ?: "Неизвестная ошибка"}"
                    _analysisResult.value = generateFallbackAnalysis(answeredQuestions, unansweredQuestions, totalScore, resumeData)
                }
            } catch (e: Exception) {
                _error.value = "Исключение: ${e.message}"
                _analysisResult.value = "⚠️ **Ошибка при анализе**\n\n${e.message}\n\nПожалуйста, попробуйте еще раз."
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun analyzeSingleQuestion(
        step: EnhancedInterviewStep,
        userAnswer: String
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val prompt = """
                    Ты - опытный технический интервьюер.
                    Проанализируй ответ кандидата на вопрос с собеседования.
                    
                    📌 **Категория**: ${step.category}
                    🔥 **Сложность**: ${step.difficulty}/5
                    ❓ **Вопрос**: ${step.question}
                    
                    💬 **Ответ кандидата**: 
                    $userAnswer
                    
                    💡 **Что ожидалось увидеть**:
                    ${step.tips.joinToString("\n• ", "• ")}
                    
                    Дай краткий анализ (3-5 предложений):
                    1. Что хорошо в ответе
                    2. Что можно улучшить
                    3. Оценку от 1 до 10
                    
                    Будь конструктивным и доброжелательным.
                """.trimIndent()

                val messages = listOf(
                    GigaChatMessage(
                        role = "system",
                        content = "Ты - опытный Android разработчик, проводишь собеседования. Даешь краткую, но полезную обратную связь."
                    ),
                    GigaChatMessage(
                        role = "user",
                        content = prompt
                    )
                )

                val result = gigaChatService.sendMessage(
                    messages = messages,
                    maxTokens = 2048
                )

                if (result.isSuccess) {
                    val response = result.getOrNull()
                    val analysis = response?.choices?.firstOrNull()?.message?.content
                        ?: "Не удалось получить анализ."

                    _analysisResult.value = analysis
                } else {
                    val exception = result.exceptionOrNull()
                    _error.value = "Ошибка анализа: ${exception?.message}"
                    _analysisResult.value = generateSingleQuestionFallback(step, userAnswer)
                }
            } catch (e: Exception) {
                _error.value = "Исключение: ${e.message}"
                _analysisResult.value = "⚠️ Ошибка при анализе ответа"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun calculateTotalScore(answers: Map<Int, String>, steps: List<EnhancedInterviewStep>): Double {
        if (answers.isEmpty()) return 0.0

        var totalScore = 0.0
        var maxPossibleScore = 0.0

        answers.forEach { (questionId, answer) ->
            val step = steps.find { it.id - 1 == questionId }
            step?.let {
                val answerScore = when {
                    answer.isBlank() -> 0.0
                    answer.length < 50 -> 3.0
                    answer.length < 200 -> 5.0
                    answer.length < 500 -> 7.0
                    else -> 9.0
                }
                val difficultyMultiplier = it.difficulty.toDouble() / 3.0
                totalScore += answerScore * difficultyMultiplier
                maxPossibleScore += 10.0 * difficultyMultiplier
            }
        }

        return if (maxPossibleScore > 0) (totalScore / maxPossibleScore) * 10 else 0.0
    }

    private fun formatAnalysisResult(analysis: String, totalScore: Double): String {
        return buildString {
            appendLine("📊 **РЕЗУЛЬТАТЫ АНАЛИЗА СОБЕСЕДОВАНИЯ**")
            appendLine()
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine()
            append(analysis)
            appendLine()
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine()
            appendLine("💪 **Продолжайте в том же духе!** Каждое собеседование делает вас сильнее.")
            appendLine("🌟 Сохраните этот анализ и вернитесь к нему через месяц, чтобы увидеть свой прогресс.")
        }
    }

    private fun generateFallbackAnalysis(
        answered: List<EnhancedInterviewStep>,
        unanswered: List<EnhancedInterviewStep>,
        totalScore: Double,
        resumeData: ResumeData?
    ): String {
        return buildString {
            appendLine("📊 **АНАЛИЗ СОБЕСЕДОВАНИЯ (БАЗОВАЯ ВЕРСИЯ)**")
            appendLine()

            if (resumeData?.hasData() == true) {
                appendLine("📄 **Информация о кандидате:**")
                if (resumeData.fullName.isNotBlank()) appendLine("• Имя: ${resumeData.fullName}")
                if (resumeData.expectedPosition.isNotBlank()) appendLine("• Позиция: ${resumeData.expectedPosition}")
                if (resumeData.skills.isNotBlank()) appendLine("• Навыки: ${resumeData.skills.take(100)}...")
                appendLine()
            }

            appendLine("К сожалению, GigaChat временно недоступен, но вот краткая статистика:")
            appendLine()
            appendLine("📈 **Общая оценка**: ${String.format("%.1f", totalScore)}/10")
            appendLine("✅ **Отвечено**: ${answered.size} вопросов")
            appendLine("⏭️ **Пропущено**: ${unanswered.size} вопросов")
            appendLine()

            if (answered.isNotEmpty()) {
                appendLine("📝 **Детали по отвеченным вопросам:**")
                answered.forEach { step ->
                    appendLine("• ${step.title} (${step.category}, сложность ${step.difficulty}/5)")
                }
                appendLine()
            }

            if (unanswered.isNotEmpty()) {
                appendLine("📚 **Рекомендуем обратить внимание на:**")
                unanswered.forEach { step ->
                    appendLine("• ${step.title} — ${step.category}")
                }
                appendLine()
            }

            appendLine("💡 **Совет**: Попробуйте повторить анализ позже или напишите нам, если проблема повторяется.")
        }
    }

    private fun generateSingleQuestionFallback(step: EnhancedInterviewStep, answer: String): String {
        val score = when {
            answer.isBlank() -> 0
            answer.length < 100 -> 5
            answer.length < 300 -> 7
            else -> 8
        }

        return buildString {
            appendLine("📝 **Анализ ответа на вопрос: ${step.title}**")
            appendLine()
            appendLine("К сожалению, GigaChat временно недоступен. Вот предварительная оценка:")
            appendLine()
            appendLine("📊 **Оценка**: $score/10")
            appendLine("📏 **Длина ответа**: ${answer.length} символов")
            appendLine("📌 **Категория**: ${step.category}")
            appendLine("🔥 **Сложность**: ${step.difficulty}/5")
            appendLine()
            appendLine("💡 **Что можно улучшить**:")
            step.tips.forEach { tip ->
                appendLine("• $tip")
            }
            appendLine()
            appendLine("Попробуйте повторить анализ позже для получения детальной обратной связи.")
        }
    }

    fun clearAnalysis() {
        _analysisResult.value = null
    }

    fun clearError() {
        _error.value = null
    }
}