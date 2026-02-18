package com.example.devpath.ui.viewmodel

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
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
import java.io.File
import java.security.MessageDigest
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

    private val _currentMessageId = MutableStateFlow<Long?>(null)
    val currentMessageId: StateFlow<Long?> = _currentMessageId.asStateFlow()

    private val _cacheStats = MutableStateFlow("")
    val cacheStats: StateFlow<String> = _cacheStats.asStateFlow()

    // AudioTrack для воспроизведения PCM
    private var audioTrack: AudioTrack? = null
    private var isPlaybackActive = false

    // Очередь сообщений
    private val speechQueue = mutableListOf<Pair<String, Long?>>()
    private var isProcessingQueue = false

    // ✅ КЭШ ДЛЯ АУДИО
    private val audioCacheDir = File(context.cacheDir, "tts_cache").apply {
        if (!exists()) mkdirs()
    }

    // Максимальный размер кэша: 50 МБ
    private val maxCacheSize = 50 * 1024 * 1024L
    private val cacheStatsMap = mutableMapOf<String, Long>()

    companion object {
        const val MAX_TEXT_LENGTH = 4000
        const val CHUNK_DELAY_MS = 500L
    }

    init {
        loadSettings()
        validateCurrentVoice()
        cleanCacheIfNeeded()
        updateCacheStats()
        println("🎤 VoiceOutput: Инициализирован, голос: ${_selectedVoice.value}")
        println("📁 TTS Cache: ${audioCacheDir.absolutePath}")
    }

    /**
     * ✅ ПОЛУЧИТЬ ХЕШ ТЕКСТА ДЛЯ КЭША
     */
    private fun getTextHash(text: String, voice: String, speed: Double): String {
        val input = "$text|$voice|$speed|${_selectedEmotion.value ?: "neutral"}"
        val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * ✅ ПОЛУЧИТЬ КЭШИРОВАННЫЙ ФАЙЛ
     */
    private fun getCachedAudioFile(hash: String): File {
        return File(audioCacheDir, "$hash.pcm")
    }

    /**
     * ✅ СОХРАНИТЬ АУДИО В КЭШ
     */
    private fun cacheAudioData(hash: String, audioData: ByteArray) {
        try {
            val cacheFile = getCachedAudioFile(hash)
            cacheFile.writeBytes(audioData)
            cacheStatsMap[hash] = System.currentTimeMillis()
            updateCacheStats()
            println("💾 TTS Cache: Сохранено ${audioData.size} байт, хеш=$hash")
        } catch (e: Exception) {
            println("❌ TTS Cache: Ошибка сохранения: ${e.message}")
        }
    }

    /**
     * ✅ ЗАГРУЗИТЬ АУДИО ИЗ КЭША
     */
    private fun loadCachedAudio(hash: String): ByteArray? {
        return try {
            val cacheFile = getCachedAudioFile(hash)
            if (cacheFile.exists()) {
                val data = cacheFile.readBytes()
                cacheStatsMap[hash] = System.currentTimeMillis()
                println("✅ TTS Cache: Загружено ${data.size} байт, хеш=$hash")
                data
            } else {
                null
            }
        } catch (e: Exception) {
            println("❌ TTS Cache: Ошибка загрузки: ${e.message}")
            null
        }
    }

    /**
     * ✅ ОЧИСТИТЬ УСТАРЕВШИЙ КЭШ
     */
    private fun cleanCacheIfNeeded() {
        try {
            val files = audioCacheDir.listFiles() ?: return
            var totalSize = files.sumOf { it.length() }

            if (totalSize <= maxCacheSize) return

            // Сортируем по дате последнего доступа
            val sortedFiles = files.sortedBy { it.lastModified() }

            for (file in sortedFiles) {
                if (totalSize <= maxCacheSize) break
                val size = file.length()
                if (file.delete()) {
                    totalSize -= size
                    println("🗑️ TTS Cache: Удален ${file.name}, освобождено ${size} байт")
                }
            }

            updateCacheStats()
        } catch (e: Exception) {
            println("❌ TTS Cache: Ошибка очистки: ${e.message}")
        }
    }

    /**
     * ✅ ОБНОВИТЬ СТАТИСТИКУ КЭША
     */
    private fun updateCacheStats() {
        try {
            val files = audioCacheDir.listFiles() ?: return
            val count = files.size
            val size = files.sumOf { it.length() }
            val sizeMB = size / (1024.0 * 1024.0)
            val maxMB = maxCacheSize / (1024.0 * 1024.0)

            _cacheStats.value = "📦 Кэш TTS: $count файлов, ${"%.1f".format(sizeMB)}/${"%.1f".format(maxMB)} МБ"
        } catch (e: Exception) {
            _cacheStats.value = "📦 Кэш TTS: ошибка"
        }
    }

    /**
     * ✅ ОЧИСТИТЬ КЭШ
     */
    fun clearCache() {
        viewModelScope.launch {
            try {
                val files = audioCacheDir.listFiles() ?: return@launch
                var deleted = 0
                var freed = 0L

                files.forEach { file ->
                    val size = file.length()
                    if (file.delete()) {
                        deleted++
                        freed += size
                    }
                }

                cacheStatsMap.clear()
                updateCacheStats()
                println("🧹 TTS Cache: Удалено $deleted файлов, освобождено ${freed} байт")

            } catch (e: Exception) {
                println("❌ TTS Cache: Ошибка очистки: ${e.message}")
            }
        }
    }

    /**
     * ✅ ОЗВУЧИТЬ ДЛИННЫЙ ТЕКСТ (С ОБРЕЗКОЙ ДО 4000)
     */
    private suspend fun speakLongText(text: String, messageId: Long? = null) {
        val chunks = text.chunked(MAX_TEXT_LENGTH)
        println("🔊 VoiceOutput: Разбиваем текст на ${chunks.size} частей")

        for ((index, chunk) in chunks.withIndex()) {
            if (!_isVoiceEnabled.value) break

            val isLastChunk = index == chunks.size - 1

            // ✅ Обрезаем каждую часть до 4000 символов
            val chunkText = if (chunk.length > MAX_TEXT_LENGTH) {
                val trimmed = chunk.take(MAX_TEXT_LENGTH - 3) + "..."
                println("   ⚠️ Часть ${index + 1} обрезана с ${chunk.length} до ${trimmed.length}")
                trimmed
            } else {
                chunk
            }

            println("   Часть ${index + 1}/${chunks.size}: ${chunkText.length} символов")

            speakTextInternal(
                text = chunkText,
                messageId = if (index == 0) messageId else null
            )

            while (_isSpeaking.value) {
                delay(100)
            }

            if (!isLastChunk) {
                delay(CHUNK_DELAY_MS)
            }
        }
    }

    /**
     * ✅ ВНУТРЕННИЙ МЕТОД ОЗВУЧКИ (С КЭШЕМ)
     */
    private suspend fun speakTextInternal(text: String, messageId: Long? = null) {
        try {
            stopCurrentPlayback()
            validateCurrentVoice()

            if (!isValidVoiceFormat(_selectedVoice.value)) {
                _selectedVoice.value = SaluteSpeechConfig.DEFAULT_VOICE_FEMALE
                saveSettings()
            }

            if (messageId != null) {
                _currentMessageId.value = messageId
            }

            _isSpeaking.value = true
            println("🎤 VoiceOutput: isSpeaking установлен в true")
            _currentSpeechText.value = text
            _error.value = null

            val voiceInfo = getCurrentVoiceInfo()
            println("🔊 VoiceOutput: Озвучка (${text.length}/$MAX_TEXT_LENGTH символов)")
            println("   Голос: ${voiceInfo?.name ?: _selectedVoice.value} (${_selectedVoice.value})")
            println("   Скорость: ${_voiceSpeed.value}x")

            // ✅ Генерируем хеш и проверяем кэш
            val hash = getTextHash(text, _selectedVoice.value, _voiceSpeed.value)
            var audioData = loadCachedAudio(hash)

            if (audioData == null) {
                println("   ⏳ Кэш: промах, запрос к API...")

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
                    audioData = result.getOrNull()
                    if (audioData != null && audioData.isNotEmpty()) {
                        // Сохраняем в кэш
                        cacheAudioData(hash, audioData)
                        cleanCacheIfNeeded()
                    }
                } else {
                    val error = result.exceptionOrNull()
                    _error.value = "Ошибка синтеза: ${error?.message}"
                    println("❌ VoiceOutput: Ошибка синтеза: ${error?.message}")
                    _isSpeaking.value = false
                    println("🎤 VoiceOutput: isSpeaking установлен в false (ошибка синтеза)")
                    _currentMessageId.value = null
                    return
                }
            } else {
                println("   ✅ Кэш: попадание!")
            }

            if (audioData != null && audioData.isNotEmpty()) {
                playAudioData(audioData)
            } else {
                _error.value = "Получены пустые аудиоданные"
                _isSpeaking.value = false
                println("🎤 VoiceOutput: isSpeaking установлен в false (пустые данные)")
                _currentMessageId.value = null
            }

        } catch (e: Exception) {
            _error.value = "Исключение: ${e.message}"
            println("❌ VoiceOutput: Исключение: ${e.message}")
            e.printStackTrace()
            _isSpeaking.value = false
            println("🎤 VoiceOutput: isSpeaking установлен в false (исключение)")
            _currentMessageId.value = null
        }
    }

    /**
     * ✅ ВОСПРОИЗВЕДЕНИЕ АУДИО
     */
    private suspend fun playAudioData(audioData: ByteArray) = withContext(Dispatchers.IO) {
        try {
            val sampleRate = if (_selectedVoice.value.contains("24000")) 24000 else 16000
            println("🔊 VoiceOutput: Воспроизведение аудио (${audioData.size} байт, ${sampleRate}Hz)")

            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            val bufferSize = maxOf(minBufferSize, audioData.size)

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack?.write(audioData, 0, audioData.size)
            audioTrack?.play()

            // Запускаем анимацию в фоне, чтобы не блокировать основной поток
            val animationJob = launch {
                var progress = 0f
                while (progress < 1f && audioTrack?.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    progress += 0.01f
                    _speakingProgress.value = progress.coerceIn(0f, 1f)
                    delay(50)
                }
            }

            // Рассчитываем максимальное время ожидания (длительность аудио + запас)
            val expectedDurationMs = (audioData.size.toFloat() / (sampleRate * 2) * 1000).toLong()
            val timeoutMs = expectedDurationMs + 5000 // +5 секунд
            val startTime = System.currentTimeMillis()

            println("⏱️ Ожидаемая длительность: ${expectedDurationMs}ms, таймаут: ${timeoutMs}ms")

            // Ждём окончания воспроизведения или таймаута
            while (audioTrack?.playState == AudioTrack.PLAYSTATE_PLAYING) {
                delay(100)
                if (System.currentTimeMillis() - startTime > timeoutMs) {
                    println("⚠️ Таймаут воспроизведения (${timeoutMs}ms), принудительно останавливаем")
                    break
                }
            }

            // Отменяем анимацию, если она ещё работает
            animationJob.cancel()

            // Останавливаем и освобождаем ресурсы
            try {
                audioTrack?.stop()
            } catch (e: Exception) {
                println("⚠️ Ошибка при stop: ${e.message}")
            }
            audioTrack?.release()
            audioTrack = null

            // ВАЖНО: сбрасываем флаг isSpeaking
            _isSpeaking.value = false
            println("✅ VoiceOutput: Воспроизведение завершено, isSpeaking=false")
            _currentMessageId.value = null
            _speakingProgress.value = 0f

        } catch (e: Exception) {
            println("❌ VoiceOutput: Ошибка воспроизведения: ${e.message}")
            e.printStackTrace()
            audioTrack = null
            _isSpeaking.value = false
            println("🎤 VoiceOutput: isSpeaking установлен в false (ошибка воспроизведения)")
            _currentMessageId.value = null
        }
    }

    /**
     * ✅ АНИМАЦИЯ ПРОГРЕССА
     */
    private suspend fun launchAnimation() {
        var progress = 0f
        while (progress < 1f && isPlaybackActive && audioTrack?.playState == AudioTrack.PLAYSTATE_PLAYING) {
            progress += 0.01f
            _speakingProgress.value = progress.coerceIn(0f, 1f)
            delay(50)
        }
    }

    /**
     * ✅ ЗАГРУЗИТЬ НАСТРОЙКИ
     */
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

    /**
     * ✅ СОХРАНИТЬ НАСТРОЙКИ
     */
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
     * ✅ ПРОВЕРКА И ИСПРАВЛЕНИЕ ГОЛОСА
     */
    private fun validateCurrentVoice() {
        val currentVoice = _selectedVoice.value
        if (isValidVoiceFormat(currentVoice)) return

        val correctedVoice = when {
            currentVoice.contains("16000") -> currentVoice.replace("16000", "24000")
            currentVoice == "May" -> SaluteSpeechConfig.DEFAULT_VOICE_FEMALE
            currentVoice == "Ost" -> "Ost_24000"
            currentVoice == "Bys" -> "Bys_24000"
            currentVoice == "Nez" -> "Nez_24000"
            currentVoice == "Tur" -> "Tur_24000"
            currentVoice == "Nec" -> "Nec_24000"
            currentVoice == "Pon" -> SaluteSpeechConfig.DEFAULT_VOICE_CHILD
            currentVoice == "Kin" -> "Kin_24000"
            else -> SaluteSpeechConfig.DEFAULT_VOICE_FEMALE
        }

        println("⚠️ VoiceOutput: Исправляем некорректный голос: $currentVoice -> $correctedVoice")
        _selectedVoice.value = correctedVoice
        saveSettings()
    }

    private fun isValidVoiceFormat(voice: String): Boolean {
        return _availableVoices.value.any { it.id == voice }
    }

    /**
     * ✅ ПОЛНОСТЬЮ ОСТАНОВИТЬ ОЗВУЧКУ
     */
    fun stopSpeaking() {
        viewModelScope.launch {
            try {
                isPlaybackActive = false
                audioTrack?.apply {
                    if (playState == AudioTrack.PLAYSTATE_PLAYING) {
                        stop()
                    }
                    release()
                }
                audioTrack = null

                speechQueue.clear()
                isProcessingQueue = false

                _isSpeaking.value = false
                println("🔇 VoiceOutput: Озвучка остановлена, isSpeaking=false")
                _speakingProgress.value = 0f
                _currentSpeechText.value = ""
                _currentMessageId.value = null

            } catch (e: Exception) {
                println("❌ VoiceOutput: Ошибка остановки: ${e.message}")
            }
        }
    }

    /**
     * ✅ ОСТАНОВИТЬ ТЕКУЩЕЕ ВОСПРОИЗВЕДЕНИЕ
     */
    fun stopCurrentPlayback() {
        try {
            isPlaybackActive = false
            audioTrack?.apply {
                if (playState == AudioTrack.PLAYSTATE_PLAYING) {
                    stop()
                }
                release()
            }
            audioTrack = null
            _isSpeaking.value = false
            println("🔇 VoiceOutput: Текущее воспроизведение остановлено, isSpeaking=false")
        } catch (e: Exception) {
            println("❌ VoiceOutput: Ошибка остановки воспроизведения: ${e.message}")
        }
    }

    /**
     * ✅ ПЕРЕКЛЮЧИТЬ ОЗВУЧКУ
     */
    fun toggleVoiceEnabled() {
        _isVoiceEnabled.value = !_isVoiceEnabled.value
        saveSettings()

        if (!_isVoiceEnabled.value) {
            stopSpeaking()
        }

        println("🔊 VoiceOutput: Озвучка ${if (_isVoiceEnabled.value) "включена" else "выключена"}")
    }

    /**
     * ✅ ОЗВУЧИТЬ ТЕКСТ
     */
    fun speakText(text: String, messageId: Long? = null) {
        if (!_isVoiceEnabled.value) {
            println("🔇 VoiceOutput: Озвучка отключена")
            return
        }

        if (text.isBlank()) {
            println("⚠️ VoiceOutput: Текст пустой")
            return
        }

        viewModelScope.launch {
            speechQueue.add(text to messageId)
            processQueue()
        }
    }

    /**
     * ✅ ОБРАБОТКА ОЧЕРЕДИ
     */
    private suspend fun processQueue() {
        if (isProcessingQueue) return
        if (speechQueue.isEmpty()) return

        isProcessingQueue = true

        try {
            while (speechQueue.isNotEmpty()) {
                val (text, messageId) = speechQueue.removeAt(0)

                if (!_isVoiceEnabled.value) {
                    speechQueue.clear()
                    break
                }

                if (text.length > MAX_TEXT_LENGTH) {
                    println("⚠️ VoiceOutput: Текст превышает $MAX_TEXT_LENGTH символов (${text.length}), разбиваем на части")
                    speakLongText(text, messageId)
                } else {
                    speakTextInternal(text, messageId)
                }

                while (_isSpeaking.value) {
                    delay(100)
                }

                delay(200)
            }
        } finally {
            isProcessingQueue = false
        }
    }

    /**
     * ✅ УСТАНОВИТЬ ГОЛОС
     */
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

    /**
     * ✅ УСТАНОВИТЬ СКОРОСТЬ
     */
    fun setVoiceSpeed(speed: Double) {
        _voiceSpeed.value = speed.coerceIn(0.5, 2.0)
        saveSettings()
        println("⚡ VoiceOutput: Скорость речи: ${_voiceSpeed.value}x")
    }

    /**
     * ✅ УСТАНОВИТЬ ЭМОЦИЮ
     */
    fun setEmotion(emotion: String?) {
        _selectedEmotion.value = emotion
        saveSettings()
        println("😊 VoiceOutput: Эмоция: ${emotion ?: "нейтральная"}")
    }

    fun getCurrentVoiceInfo(): SaluteSpeechConfig.Voice? {
        return _availableVoices.value.find { it.id == _selectedVoice.value }
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
        println("🔄 VoiceOutput: Сброс настроек")
    }

    fun forceClearSettings() {
        val prefs = context.getSharedPreferences("voice_settings", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        resetToDefaults()
        println("🧹 VoiceOutput: Настройки очищены")
    }

    fun testVoice() {
        val voiceInfo = getCurrentVoiceInfo()
        val testText = when {
            voiceInfo?.gender == "Мужской" -> "Привет! Я ${voiceInfo.name}. Мой голос звучит так."
            voiceInfo?.gender == "Женский" -> "Привет! Я ${voiceInfo.name}. Мой голос звучит так."
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
            ├─ Качество: 24kHz
            ├─ Скорость: ${_voiceSpeed.value}x
            ├─ Эмоция: ${_selectedEmotion.value ?: "нейтральная"}
            ├─ Озвучка: ${if (_isVoiceEnabled.value) "включена" else "выключена"}
            └─ ${_cacheStats.value}
        """.trimIndent()
    }

    override fun onCleared() {
        super.onCleared()
        stopSpeaking()
        println("🔄 VoiceOutputViewModel очищен")
    }
}