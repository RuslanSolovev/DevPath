// ui/viewmodel/VoiceOutputViewModel.kt
package com.example.devpath.ui.viewmodel

import android.content.Context
import android.media.AudioManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.devpath.api.speech.SaluteSpeechConfig
import com.example.devpath.api.speech.SaluteSpeechService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class VoiceOutputViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val saluteSpeechService: SaluteSpeechService
) : ViewModel() {

    // Состояния
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isVoiceEnabled = MutableStateFlow(true)
    val isVoiceEnabled: StateFlow<Boolean> = _isVoiceEnabled.asStateFlow()

    private val _selectedVoice = MutableStateFlow(SaluteSpeechConfig.DEFAULT_VOICE_FEMALE)
    val selectedVoice: StateFlow<String> = _selectedVoice.asStateFlow()

    private val _voiceSpeed = MutableStateFlow(1.0)
    val voiceSpeed: StateFlow<Double> = _voiceSpeed.asStateFlow()

    private val _selectedEmotion = MutableStateFlow<String?>(null)
    val selectedEmotion: StateFlow<String?> = _selectedEmotion.asStateFlow()

    private val _availableVoices = MutableStateFlow(SaluteSpeechConfig.AVAILABLE_VOICES)
    val availableVoices: StateFlow<List<SaluteSpeechConfig.Voice>> = _availableVoices.asStateFlow()

    private val _availableEmotions = MutableStateFlow(SaluteSpeechConfig.AVAILABLE_EMOTIONS)
    val availableEmotions: StateFlow<List<SaluteSpeechConfig.Emotion>> = _availableEmotions.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _speakingProgress = MutableStateFlow(0f)
    val speakingProgress: StateFlow<Float> = _speakingProgress.asStateFlow()

    private val _currentSpeechText = MutableStateFlow("")
    val currentSpeechText: StateFlow<String> = _currentSpeechText.asStateFlow()

    private val _audioDuration = MutableStateFlow(0)
    val audioDuration: StateFlow<Int> = _audioDuration.asStateFlow()

    // ✅ ДОБАВЛЕНО: ID текущего озвучиваемого сообщения
    private val _currentMessageId = MutableStateFlow<Long?>(null)
    val currentMessageId: StateFlow<Long?> = _currentMessageId.asStateFlow()

    private var isPlaying = false
    private var currentAudioData: ByteArray? = null

    // Константы
    private companion object {
        const val MAX_TEXT_LENGTH = 4000 // Максимум символов для TTS
        const val CHUNK_DELAY_MS = 500L // Задержка между частями
    }

    init {
        loadSettings()
        validateCurrentVoice()
        println("🎤 VoiceOutput: Инициализирован, голос: ${_selectedVoice.value}")
    }

    /**
     * ✅ ПРОВЕРКА И ИСПРАВЛЕНИЕ ГОЛОСА
     */
    private fun validateCurrentVoice() {
        val currentVoice = _selectedVoice.value
        if (isValidVoiceFormat(currentVoice)) return

        val correctedVoice = when {
            currentVoice.contains("16000") -> currentVoice.replace("16000", "8000")
            currentVoice == "May" -> SaluteSpeechConfig.DEFAULT_VOICE_FEMALE
            currentVoice == "Ost" -> SaluteSpeechConfig.DEFAULT_VOICE_MALE
            currentVoice == "Bys" -> "Bys_8000"
            currentVoice == "Nez" -> "Nez_8000"
            currentVoice == "Tur" -> "Tur_8000"
            currentVoice == "Nec" -> "Nec_8000"
            currentVoice == "Pon" -> SaluteSpeechConfig.DEFAULT_VOICE_CHILD
            currentVoice == "Kin" -> "Kin_8000"
            currentVoice == "Kma" -> "Kma_8000"
            currentVoice == "Rma" -> "Rma_8000"
            currentVoice == "Nur" -> "Nur_8000"
            currentVoice == "Rnu" -> "Rnu_8000"
            else -> SaluteSpeechConfig.DEFAULT_VOICE_FEMALE
        }

        println("⚠️ VoiceOutput: Исправляем некорректный голос: $currentVoice -> $correctedVoice")
        _selectedVoice.value = correctedVoice
        saveSettings()
    }

    private fun isValidVoiceFormat(voice: String): Boolean {
        return _availableVoices.value.any { it.id == voice }
    }

    private fun loadSettings() {
        try {
            val prefs = context.getSharedPreferences("voice_settings", Context.MODE_PRIVATE)
            _isVoiceEnabled.value = prefs.getBoolean("is_voice_enabled", true)
            _selectedVoice.value = prefs.getString("selected_voice", SaluteSpeechConfig.DEFAULT_VOICE_FEMALE)
                ?: SaluteSpeechConfig.DEFAULT_VOICE_FEMALE
            _voiceSpeed.value = prefs.getFloat("voice_speed", 1.0f).toDouble().coerceIn(0.5, 2.0)
            _selectedEmotion.value = prefs.getString("selected_emotion", null)
            println("📱 VoiceOutput: Настройки загружены")
        } catch (e: Exception) {
            println("❌ VoiceOutput: Ошибка загрузки настроек: ${e.message}")
            resetToDefaults()
        }
    }

    private fun saveSettings() {
        try {
            val prefs = context.getSharedPreferences("voice_settings", Context.MODE_PRIVATE)
            prefs.edit().apply {
                putBoolean("is_voice_enabled", _isVoiceEnabled.value)
                putString("selected_voice", _selectedVoice.value)
                putFloat("voice_speed", _voiceSpeed.value.toFloat())
                putString("selected_emotion", _selectedEmotion.value)
                apply()
            }
            println("💾 VoiceOutput: Настройки сохранены")
        } catch (e: Exception) {
            println("❌ VoiceOutput: Ошибка сохранения настроек: ${e.message}")
        }
    }

    /**
     * ✅ Озвучить текст с проверкой лимита 4000 символов
     * @param text Текст для озвучки
     * @param messageId ID сообщения (для предотвращения дублей)
     * @param isChunk Флаг, указывающий что это часть разбитого текста (предотвращает рекурсию)
     */
    fun speakText(text: String, messageId: Long? = null, isChunk: Boolean = false) {
        if (!_isVoiceEnabled.value) {
            println("🔇 VoiceOutput: Озвучка отключена")
            return
        }

        if (text.isBlank()) {
            println("⚠️ VoiceOutput: Текст пустой")
            return
        }

        // ✅ Защита от дублей сообщений
        if (messageId != null && _currentMessageId.value == messageId && !isChunk) {
            println("⏭️ VoiceOutput: Сообщение уже озвучивается, пропускаем")
            return
        }

        // ✅ Проверяем длину текста ТОЛЬКО если это не чанк
        if (!isChunk && text.length > MAX_TEXT_LENGTH) {
            println("⚠️ VoiceOutput: Текст превышает $MAX_TEXT_LENGTH символов (${text.length}), разбиваем на части")
            speakLongText(text, messageId)
            return
        }

        viewModelScope.launch {
            try {
                // Останавливаем предыдущую озвучку (но не для чанков!)
                if (!isChunk) {
                    stopSpeaking()
                    delay(100)
                }

                validateCurrentVoice()

                if (!isValidVoiceFormat(_selectedVoice.value)) {
                    _selectedVoice.value = SaluteSpeechConfig.DEFAULT_VOICE_FEMALE
                    saveSettings()
                }

                // ✅ Сохраняем ID текущего сообщения (только для первого чанка)
                if (messageId != null && _currentMessageId.value == null) {
                    _currentMessageId.value = messageId
                }

                _isSpeaking.value = true
                _currentSpeechText.value = text
                _error.value = null

                val voiceInfo = getCurrentVoiceInfo()
                println("🔊 VoiceOutput: ${if (isChunk) "  └─ ЧАСТЬ" else "Начинаем озвучку"} (${text.length}/$MAX_TEXT_LENGTH символов)")
                println("   Голос: ${voiceInfo?.name ?: _selectedVoice.value} (${_selectedVoice.value})")
                println("   Скорость: ${_voiceSpeed.value}x")
                if (isChunk) println("   ⚡ Это часть разбитого текста")
                if (messageId != null) println("   Message ID: $messageId")

                val result = withContext(Dispatchers.IO) {
                    saluteSpeechService.synthesizeSpeech(
                        text = text,
                        voice = _selectedVoice.value,
                        speed = _voiceSpeed.value,
                        emotion = _selectedEmotion.value,
                        format = SaluteSpeechConfig.DEFAULT_TTS_FORMAT
                    )
                }

                if (result.isSuccess) {
                    val audioData = result.getOrNull()
                    if (audioData != null && audioData.isNotEmpty()) {
                        currentAudioData = audioData
                        _audioDuration.value = if (_selectedVoice.value.contains("24000")) {
                            audioData.size / 48
                        } else {
                            audioData.size / 32
                        }

                        val playResult = saluteSpeechService.playAudio(audioData)

                        if (playResult.isSuccess) {
                            println("✅ VoiceOutput: ${if (isChunk) "  └─ ЧАСТЬ завершена" else "Озвучка успешно завершена"}")
                            if (!isChunk) {
                                animateProgress(_audioDuration.value)
                            }
                        } else {
                            val error = playResult.exceptionOrNull()
                            _error.value = "Ошибка воспроизведения: ${error?.message}"
                            println("❌ VoiceOutput: Ошибка воспроизведения: ${error?.message}")
                        }
                    } else {
                        _error.value = "Получены пустые аудиоданные"
                        println("❌ VoiceOutput: Пустые аудиоданные")
                    }
                } else {
                    val error = result.exceptionOrNull()
                    _error.value = "Ошибка синтеза речи: ${error?.message}"
                    println("❌ VoiceOutput: Ошибка синтеза: ${error?.message}")

                    if (error?.message?.contains("invalid voice") == true) {
                        println("🔄 VoiceOutput: Сбрасываем голос на дефолтный из-за ошибки")
                        _selectedVoice.value = SaluteSpeechConfig.DEFAULT_VOICE_FEMALE
                        saveSettings()
                    }
                }

            } catch (e: Exception) {
                _error.value = "Исключение: ${e.message}"
                println("❌ VoiceOutput: Исключение: ${e.message}")
                e.printStackTrace()
            } finally {
                _isSpeaking.value = false
                _currentSpeechText.value = ""
                _speakingProgress.value = 0f
                _audioDuration.value = 0

                // ✅ Сбрасываем ID только после последнего чанка
                if (!isChunk && messageId != null && _currentMessageId.value == messageId) {
                    _currentMessageId.value = null
                }
            }
        }
    }

    /**
     * ✅ Озвучить длинный текст с разбиением на части по 4000 символов
     * БЕЗ РЕКУРСИИ!
     */
    private fun speakLongText(text: String, messageId: Long? = null) {
        viewModelScope.launch {
            try {
                // Сохраняем ID для первого чанка
                if (messageId != null) {
                    _currentMessageId.value = messageId
                }

                val chunks = text.chunked(MAX_TEXT_LENGTH)
                println("🔊 VoiceOutput: Разбиваем текст на ${chunks.size} частей")

                for ((index, chunk) in chunks.withIndex()) {
                    val isLastChunk = index == chunks.size - 1

                    // ✅ Добавляем индикатор продолжения для всех частей, кроме последней
                    val chunkText = if (!isLastChunk) {
                        chunk + "\n\n[Продолжение следует...]"
                    } else {
                        chunk
                    }

                    println("   Часть ${index + 1}/${chunks.size}: ${chunkText.length} символов")

                    // ✅ ВАЖНО: Вызываем speakText с флагом isChunk = true
                    speakText(
                        text = chunkText,
                        messageId = if (index == 0) messageId else null,
                        isChunk = true
                    )

                    // Ждем окончания текущей части
                    while (_isSpeaking.value) {
                        delay(100)
                    }

                    // Пауза между частями
                    if (!isLastChunk) {
                        println("   ⏸️ Пауза между частями...")
                        delay(CHUNK_DELAY_MS)
                    }
                }

                println("✅ VoiceOutput: Все ${chunks.size} частей озвучены")

                // Сбрасываем ID сообщения после завершения всех частей
                if (messageId != null) {
                    _currentMessageId.value = null
                }

            } catch (e: Exception) {
                _error.value = "Ошибка при разбиении текста: ${e.message}"
                println("❌ VoiceOutput: Ошибка разбиения текста: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun stopSpeaking() {
        viewModelScope.launch {
            try {
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                audioManager.abandonAudioFocus(null)
                isPlaying = false
                _isSpeaking.value = false
                _speakingProgress.value = 0f
                _currentSpeechText.value = ""
                _currentMessageId.value = null // Сбрасываем ID при принудительной остановке
                println("🔇 VoiceOutput: Озвучка остановлена")
            } catch (e: Exception) {
                println("❌ VoiceOutput: Ошибка остановки: ${e.message}")
            }
        }
    }

    private suspend fun animateProgress(duration: Int) {
        val step = 50L
        val totalSteps = if (duration > 0) (duration / step).coerceIn(20, 200) else 100

        var progress = 0f
        while (progress < 1f && _isSpeaking.value) {
            progress += 1f / totalSteps
            _speakingProgress.value = progress.coerceIn(0f, 1f)
            delay(step)
        }
    }

    fun setVoice(voiceId: String) {
        if (isValidVoiceFormat(voiceId)) {
            _selectedVoice.value = voiceId
            saveSettings()
            val voiceInfo = getCurrentVoiceInfo()
            println("🎤 VoiceOutput: Выбран голос: ${voiceInfo?.name ?: voiceId} ($voiceId)")
        } else {
            println("❌ VoiceOutput: Некорректный формат голоса: $voiceId")
        }
    }

    fun setVoiceSpeed(speed: Double) {
        _voiceSpeed.value = speed.coerceIn(0.5, 2.0)
        saveSettings()
        println("⚡ VoiceOutput: Скорость речи: ${_voiceSpeed.value}x")
    }

    fun setEmotion(emotion: String?) {
        _selectedEmotion.value = emotion
        saveSettings()
        println("😊 VoiceOutput: Эмоция: ${emotion ?: "нейтральная"}")
    }

    fun toggleVoiceEnabled() {
        _isVoiceEnabled.value = !_isVoiceEnabled.value
        saveSettings()
        if (!_isVoiceEnabled.value) stopSpeaking()
        println("🔊 VoiceOutput: Озвучка ${if (_isVoiceEnabled.value) "включена" else "выключена"}")
    }

    fun getCurrentVoiceInfo(): SaluteSpeechConfig.Voice? {
        return _availableVoices.value.find { it.id == _selectedVoice.value }
    }

    fun getVoiceDisplayName(voiceId: String): String {
        return _availableVoices.value.find { it.id == voiceId }?.name ?: "Неизвестный голос"
    }

    fun getVoicesByGender(gender: String): List<SaluteSpeechConfig.Voice> {
        return _availableVoices.value.filter { it.gender == gender }
    }

    fun getVoicesByQuality(quality: String): List<SaluteSpeechConfig.Voice> {
        return _availableVoices.value.filter { it.quality == quality }
    }

    fun isEmotionSupported(emotionId: String): Boolean {
        return _selectedVoice.value.contains("8000") &&
                _availableEmotions.value.any { it.id == emotionId }
    }

    fun clearError() {
        _error.value = null
    }

    fun resetToDefaults() {
        _isVoiceEnabled.value = true
        _selectedVoice.value = SaluteSpeechConfig.DEFAULT_VOICE_FEMALE
        _voiceSpeed.value = 1.0
        _selectedEmotion.value = null
        saveSettings()
        println("🔄 VoiceOutput: Сброс настроек на значения по умолчанию")
    }

    fun forceClearSettings() {
        val prefs = context.getSharedPreferences("voice_settings", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        resetToDefaults()
        println("🧹 VoiceOutput: Настройки полностью очищены")
    }

    fun testVoice() {
        val voiceInfo = getCurrentVoiceInfo()
        val testText = when {
            voiceInfo?.gender == "Мужской" -> "Привет! Я ${voiceInfo.name}. Мой голос звучит так."
            voiceInfo?.gender == "Женский" -> "Привет! Я ${voiceInfo.name}. Мой голос звучит так."
            voiceInfo?.name == "Пон" -> "Привет! Я Пон. Мой детский голос звучит так."
            else -> "Привет! Я голосовой помощник. Этот голос звучит так."
        }
        speakText(testText)
    }

    fun getVoiceStats(): String {
        val voiceInfo = getCurrentVoiceInfo()
        return """
            Voice Settings:
            ├─ Голос: ${voiceInfo?.name ?: _selectedVoice.value}
            ├─ ID: ${_selectedVoice.value}
            ├─ Пол: ${voiceInfo?.gender ?: "Неизвестно"}
            ├─ Качество: ${voiceInfo?.quality ?: "Неизвестно"}
            ├─ Скорость: ${_voiceSpeed.value}x
            ├─ Эмоция: ${_selectedEmotion.value ?: "нейтральная"}
            ├─ Формат TTS: ${SaluteSpeechConfig.DEFAULT_TTS_FORMAT}
            ├─ Лимит символов: $MAX_TEXT_LENGTH
            └─ Озвучка: ${if (_isVoiceEnabled.value) "включена" else "выключена"}
        """.trimIndent()
    }

    override fun onCleared() {
        super.onCleared()
        stopSpeaking()
        currentAudioData = null
    }
}