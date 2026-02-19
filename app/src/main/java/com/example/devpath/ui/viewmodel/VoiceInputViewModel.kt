package com.example.devpath.ui.viewmodel

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.devpath.api.speech.SaluteSpeechService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class VoiceInputViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val saluteSpeechService: SaluteSpeechService,
    private val savedStateHandle: SavedStateHandle // Добавлено
) : ViewModel() {

    // ==================== СОСТОЯНИЯ ====================
    private val _isRecording = MutableStateFlow(
        savedStateHandle.get<Boolean>("isRecording") ?: false
    )
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isProcessing = MutableStateFlow(
        savedStateHandle.get<Boolean>("isProcessing") ?: false
    )
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _isListening = MutableStateFlow(
        savedStateHandle.get<Boolean>("isListening") ?: false
    )
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _recognizedText = MutableStateFlow(
        savedStateHandle.get<String>("recognizedText") ?: ""
    )
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _audioLevel = MutableStateFlow(0f)
    val audioLevel: StateFlow<Float> = _audioLevel.asStateFlow()

    private val _recordingDuration = MutableStateFlow(0)
    val recordingDuration: StateFlow<Int> = _recordingDuration.asStateFlow()

    private val _isVoiceDetected = MutableStateFlow(false)
    val isVoiceDetected: StateFlow<Boolean> = _isVoiceDetected.asStateFlow()

    // Состояния для диалога разрешений
    private val _showPermissionDialog = MutableStateFlow(false)
    val showPermissionDialog: StateFlow<Boolean> = _showPermissionDialog.asStateFlow()

    private val _permissionPermanentlyDenied = MutableStateFlow(false)
    val permissionPermanentlyDenied: StateFlow<Boolean> = _permissionPermanentlyDenied.asStateFlow()

    // ==================== ПЕРЕМЕННЫЕ ====================
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var startTime: Long = 0

    private var durationJob: Job? = null
    private var audioLevelJob: Job? = null
    private var silenceDetectorJob: Job? = null
    private var autoRestartJob: Job? = null
    private var noiseCalibrationJob: Job? = null

    private var lastVoiceTime = 0L
    private val silenceThreshold = 1500L // 1.5 секунды тишины
    private val requiredSilenceCount = (silenceThreshold / 100).toInt() // 15 при 100мс
    private val maxSpeechDuration = 10000L // 10 секунд максимум записи
    private var isSpeaking = false
    private var currentCallback: ((String) -> Unit)? = null

    // Флаг для отслеживания, была ли уже запущена обработка тишины
    private var isSilenceProcessing = false

    // Адаптивный шумоподавитель
    private var noiseFloor = 0.1f
    private val noiseSamples = mutableListOf<Float>()
    private val maxNoiseSamples = 30
    private var isCalibrated = false

    // Счетчик тишины
    private var silenceCount = 0

    init {
        println("🎤 VoiceInputViewModel инициализирован")
        // Восстанавливаем состояние
        if (_isListening.value) {
            println("🎤 VoiceInput: Восстановление режима прослушивания")
        }
    }

    // ==================== РАЗРЕШЕНИЯ ====================
    fun hasRecordAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun checkPermissionAndAct(
        onPermissionGranted: () -> Unit,
        onShowDialog: () -> Unit
    ) {
        if (hasRecordAudioPermission()) {
            onPermissionGranted.invoke()
        } else {
            _showPermissionDialog.value = true
            onShowDialog.invoke()
        }
    }

    fun handlePermissionResult(isGranted: Boolean, shouldShowRationale: Boolean) {
        if (isGranted) {
            _showPermissionDialog.value = false
            _permissionPermanentlyDenied.value = false
            _error.value = null

            if (_isListening.value) {
                currentCallback?.let { callback ->
                    startListening(callback)
                }
            } else if (_isRecording.value) {
                startRecording()
            }
        } else {
            if (!shouldShowRationale) {
                _permissionPermanentlyDenied.value = true
                _error.value = "Разрешение на запись аудио заблокировано навсегда"
            } else {
                _error.value = "Разрешение на запись аудио отклонено"
            }
        }
    }

    fun dismissPermissionDialog() {
        _showPermissionDialog.value = false
    }

    fun openAppSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            _error.value = "Не удалось открыть настройки"
        }
    }

    // ==================== ЗАПУСК ПРОСЛУШИВАНИЯ ====================
    fun startListening(onResult: (String) -> Unit) {
        println("🎤 VoiceInput: startListening вызван")

        if (!hasRecordAudioPermission()) {
            _error.value = "Нет разрешения на запись аудио"
            _showPermissionDialog.value = true
            println("❌ VoiceInput: Нет разрешения")
            return
        }

        // ✅ ВАЖНО: Полностью сбрасываем состояние перед запуском
        stopRecording()
        cancelAllJobs()

        _isListening.value = true
        savedStateHandle.set("isListening", true)
        _error.value = null
        currentCallback = onResult
        isSilenceProcessing = false
        isSpeaking = false
        isCalibrated = false
        noiseSamples.clear()
        silenceCount = 0
        lastVoiceTime = 0L

        startRecordingWithCallback()
    }

    fun stopListening() {
        println("🎤 VoiceInput: stopListening вызван")
        _isListening.value = false
        savedStateHandle.set("isListening", false)
        currentCallback = null
        stopRecording()
        cancelAllJobs()
        isSilenceProcessing = false
        isSpeaking = false
        _isVoiceDetected.value = false
        silenceCount = 0
    }

    // ==================== ЗАПИСЬ С КОЛЛБЕКОМ ====================
    private fun startRecordingWithCallback() {
        viewModelScope.launch {
            try {
                println("🎤 VoiceInput: Создание аудиофайла...")
                audioFile = createAudioFile()

                println("🎤 VoiceInput: Инициализация MediaRecorder...")
                mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder(context)
                } else {
                    MediaRecorder()
                }.apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.OGG)
                    setAudioEncoder(MediaRecorder.AudioEncoder.OPUS)
                    setAudioSamplingRate(16000)
                    setAudioEncodingBitRate(32000)
                    setOutputFile(audioFile?.absolutePath)

                    try {
                        prepare()
                        start()
                        println("🎤 VoiceInput: MediaRecorder успешно запущен")
                    } catch (e: Exception) {
                        println("❌ VoiceInput: Ошибка MediaRecorder: ${e.message}")
                        throw e
                    }
                }

                _isRecording.value = true
                savedStateHandle.set("isRecording", true)
                _isVoiceDetected.value = false
                isSpeaking = false
                startTime = System.currentTimeMillis()
                lastVoiceTime = startTime
                isSilenceProcessing = false
                silenceCount = 0
                isCalibrated = false
                noiseSamples.clear()

                startDurationTimer()
                startAudioLevelMonitor()
                startNoiseCalibration()
                startSilenceDetector()

                println("🎤 VoiceInput: Запись начата, файл: ${audioFile?.absolutePath}")

            } catch (e: Exception) {
                _error.value = "Ошибка записи: ${e.message}"
                println("❌ VoiceInput: Ошибка записи: ${e.message}")
                e.printStackTrace()
                stopListening()
            }
        }
    }

    // ==================== КАЛИБРОВКА ШУМА ====================
    private fun startNoiseCalibration() {
        noiseCalibrationJob?.cancel()
        noiseCalibrationJob = viewModelScope.launch {
            println("🎤 NoiseCalibration: Начинаем калибровку шума...")
            delay(500) // Даем время микрофону стабилизироваться

            while (_isRecording.value && noiseSamples.size < maxNoiseSamples) {
                if (_audioLevel.value > 0) {
                    noiseSamples.add(_audioLevel.value)
                    println("🎤 NoiseCalibration: Семпл ${noiseSamples.size}/${maxNoiseSamples} = ${_audioLevel.value}")
                }
                delay(100)
            }

            if (noiseSamples.isNotEmpty()) {
                val sorted = noiseSamples.sorted()
                noiseFloor = sorted[sorted.size / 2] * 1.2f
                isCalibrated = true
                println("🎤 NoiseCalibration: Калибровка завершена, уровень шума = $noiseFloor")
            } else {
                noiseFloor = 0.15f
                println("🎤 NoiseCalibration: Недостаточно данных, используем запасной порог $noiseFloor")
            }
        }
    }

    // ==================== ДЕТЕКТОР ТИШИНЫ ====================
    private fun startSilenceDetector() {
        silenceDetectorJob?.cancel()
        silenceDetectorJob = viewModelScope.launch {
            println("🎤 SilenceDetector: ЗАПУЩЕН")

            while (_isRecording.value) {
                delay(100)
                val currentTime = System.currentTimeMillis()

                // Принудительная отправка при слишком долгой записи
                if (isSpeaking && (currentTime - lastVoiceTime > maxSpeechDuration) && !isSilenceProcessing) {
                    println("🎤 SilenceDetector: ⏰ Длительная запись ${(currentTime - startTime)/1000}с, принудительная отправка")
                    isSilenceProcessing = true
                    stopRecordingAndRecognize { text ->
                        println("🎤 SilenceDetector: Распознано (принудительно): \"$text\"")
                        if (text.isNotBlank()) {
                            currentCallback?.invoke(text)
                        }
                        // ✅ ВАЖНО: Проверяем _isListening.value, а не isListening
                        if (_isListening.value) {
                            scheduleAutoRestart()
                        } else {
                            isSilenceProcessing = false
                        }
                    }
                    continue
                }

                // Используем гистерезис
                val voiceHighThreshold = if (isCalibrated) noiseFloor * 1.5f else 0.15f
                val voiceLowThreshold = if (isCalibrated) noiseFloor * 1.2f else 0.12f

                val isVoiceNow = if (isSpeaking) {
                    _audioLevel.value > voiceLowThreshold
                } else {
                    _audioLevel.value > voiceHighThreshold
                }

                _isVoiceDetected.value = isVoiceNow

                if (isVoiceNow) {
                    if (!isSpeaking) {
                        isSpeaking = true
                        lastVoiceTime = currentTime
                        silenceCount = 0
                        println("🎤 SilenceDetector: 🟢 РЕЧЬ НАЧАЛАСЬ (уровень=${_audioLevel.value})")
                    }
                    lastVoiceTime = currentTime
                    silenceCount = 0
                } else {
                    if (isSpeaking) {
                        silenceCount++
                        if (silenceCount >= requiredSilenceCount && !isSilenceProcessing) {
                            println("🎤 SilenceDetector: 🔴 ТИШИНА ${silenceCount*100}мс, отправляем!")
                            isSilenceProcessing = true

                            stopRecordingAndRecognize { text ->
                                println("🎤 SilenceDetector: Распознано: \"$text\"")
                                if (text.isNotBlank()) {
                                    currentCallback?.invoke(text)
                                }
                                // ✅ ВАЖНО: Проверяем _isListening.value
                                if (_isListening.value) {
                                    scheduleAutoRestart()
                                } else {
                                    isSilenceProcessing = false
                                }
                            }
                        }
                    } else {
                        silenceCount = 0
                    }
                }

                // Логируем каждые 2 секунды
                if (currentTime % 2000 < 100) {
                    println("🎤 SilenceDetector: isSpeaking=$isSpeaking, уровень=${_audioLevel.value}, пороги(h/l)=$voiceHighThreshold/$voiceLowThreshold, silenceCount=$silenceCount")
                }
            }
            println("🎤 SilenceDetector: ОСТАНОВЛЕН")
        }
    }

    // ==================== АВТОМАТИЧЕСКИЙ ПЕРЕЗАПУСК ====================
    private fun scheduleAutoRestart() {
        autoRestartJob?.cancel()
        autoRestartJob = viewModelScope.launch {
            delay(500)
            println("🎤 SilenceDetector: Автоматический перезапуск")

            // ✅ ВАЖНО: Полностью сбрасываем состояние перед перезапуском
            isSilenceProcessing = false
            isSpeaking = false
            silenceCount = 0
            noiseSamples.clear()
            isCalibrated = false
            lastVoiceTime = 0L

            // ✅ Проверяем что режим прослушивания все еще активен
            if (_isListening.value && !_isRecording.value && !_isProcessing.value) {
                startRecordingWithCallback()
            } else {
                println("⚠️ SilenceDetector: Перезапуск отменен, isListening=${_isListening.value}, isRecording=${_isRecording.value}")
            }
        }
    }

    // ==================== ОБЫЧНАЯ ЗАПИСЬ ====================
    fun startRecording() {
        println("🎤 VoiceInput: startRecording вызван")

        if (!hasRecordAudioPermission()) {
            _error.value = "Нет разрешения на запись аудио"
            _showPermissionDialog.value = true
            println("❌ VoiceInput: Нет разрешения")
            return
        }

        if (_isListening.value) {
            stopListening()
        }

        viewModelScope.launch {
            try {
                audioFile = createAudioFile()

                mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder(context)
                } else {
                    MediaRecorder()
                }.apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.OGG)
                    setAudioEncoder(MediaRecorder.AudioEncoder.OPUS)
                    setAudioSamplingRate(16000)
                    setAudioEncodingBitRate(32000)
                    setOutputFile(audioFile?.absolutePath)

                    try {
                        prepare()
                        start()
                        println("🎤 VoiceInput: MediaRecorder успешно запущен")
                    } catch (e: Exception) {
                        println("❌ VoiceInput: Ошибка MediaRecorder: ${e.message}")
                        throw e
                    }
                }

                _isRecording.value = true
                savedStateHandle.set("isRecording", true)
                _error.value = null
                startTime = System.currentTimeMillis()

                startDurationTimer()
                startAudioLevelMonitor()

                println("🎤 VoiceInput: Запись начата, файл: ${audioFile?.absolutePath}")

            } catch (e: Exception) {
                _error.value = "Ошибка записи: ${e.message}"
                println("❌ VoiceInput: Ошибка записи: ${e.message}")
                e.printStackTrace()
                cleanup()
            }
        }
    }

    // ==================== ОСТАНОВКА ЗАПИСИ ====================
    fun stopRecording() {
        println("🎤 VoiceInput: stopRecording вызван")
        try {
            mediaRecorder?.apply {
                try {
                    stop()
                    println("🎤 VoiceInput: MediaRecorder остановлен")
                } catch (e: Exception) {
                    println("⚠️ VoiceInput: Ошибка остановки: ${e.message}")
                }
                release()
            }
            mediaRecorder = null

            stopTimers()
            silenceDetectorJob?.cancel()
            silenceDetectorJob = null
            noiseCalibrationJob?.cancel()
            noiseCalibrationJob = null

            _isRecording.value = false
            savedStateHandle.set("isRecording", false)
            _audioLevel.value = 0f
            _isVoiceDetected.value = false
            isSpeaking = false
            silenceCount = 0

            val duration = if (startTime > 0) (System.currentTimeMillis() - startTime) / 1000 else 0
            println("🎤 VoiceInput: Запись остановлена, длительность: ${duration}с")

        } catch (e: Exception) {
            _error.value = "Ошибка остановки записи: ${e.message}"
            println("❌ VoiceInput: Ошибка остановки: ${e.message}")
            e.printStackTrace()
        }
    }

    fun stopRecordingAndRecognize(onResult: ((String) -> Unit)? = null) {
        println("🎤 VoiceInput: stopRecordingAndRecognize вызван")
        stopRecording()

        audioFile?.let { file ->
            if (file.exists() && file.length() > 0) {
                println("🎤 VoiceInput: Файл существует, размер: ${file.length()} байт")
                recognizeAudio(file, onResult)
            } else {
                _error.value = "Файл записи пуст или не существует"
                println("❌ VoiceInput: Файл пуст или не существует")
                onResult?.invoke("")
            }
        }
    }

    // ==================== РАСПОЗНАВАНИЕ ====================
    private fun recognizeAudio(file: File, onResult: ((String) -> Unit)? = null) {
        viewModelScope.launch {
            _isProcessing.value = true
            savedStateHandle.set("isProcessing", true)

            try {
                println("🎤 VoiceInput: Отправка на распознавание, размер: ${file.length()} байт")

                val result = saluteSpeechService.recognizeSpeech(
                    audioFile = file,
                    mimeType = "audio/ogg;codecs=opus"
                )

                if (result.isSuccess) {
                    val text = result.getOrNull() ?: ""
                    _recognizedText.value = text
                    savedStateHandle.set("recognizedText", text)
                    println("✅ VoiceInput: Распознано: \"$text\"")

                    try {
                        file.delete()
                        println("🎤 VoiceInput: Временный файл удален")
                    } catch (e: Exception) {
                        println("⚠️ VoiceInput: Не удалось удалить файл: ${e.message}")
                    }

                    onResult?.invoke(text)
                } else {
                    val error = result.exceptionOrNull()
                    _error.value = "Ошибка распознавания: ${error?.message}"
                    println("❌ VoiceInput: Ошибка распознавания: ${error?.message}")
                    onResult?.invoke("")
                }

            } catch (e: Exception) {
                _error.value = "Исключение при распознавании: ${e.message}"
                println("❌ VoiceInput: Ошибка: ${e.message}")
                e.printStackTrace()
                onResult?.invoke("")
            } finally {
                _isProcessing.value = false
                savedStateHandle.set("isProcessing", false)
            }
        }
    }

    // ==================== ТАЙМЕРЫ ====================
    private fun startDurationTimer() {
        durationJob?.cancel()
        durationJob = viewModelScope.launch {
            while (_isRecording.value) {
                val duration = (System.currentTimeMillis() - startTime) / 1000
                _recordingDuration.value = duration.toInt()
                delay(1000)
            }
        }
    }

    private fun startAudioLevelMonitor() {
        audioLevelJob?.cancel()
        audioLevelJob = viewModelScope.launch {
            while (_isRecording.value && mediaRecorder != null) {
                try {
                    val maxAmplitude = mediaRecorder!!.maxAmplitude
                    val level = if (maxAmplitude > 0) {
                        (Math.log10(maxAmplitude.toDouble()) * 20).toFloat()
                    } else {
                        0f
                    }
                    _audioLevel.value = level.coerceIn(0f, 100f) / 100f
                } catch (e: Exception) {
                    // Игнорируем ошибки амплитуды
                }
                delay(50)
            }
        }
    }

    private fun stopTimers() {
        durationJob?.cancel()
        durationJob = null
        audioLevelJob?.cancel()
        audioLevelJob = null
    }

    private fun cancelAllJobs() {
        durationJob?.cancel()
        durationJob = null
        audioLevelJob?.cancel()
        audioLevelJob = null
        silenceDetectorJob?.cancel()
        silenceDetectorJob = null
        autoRestartJob?.cancel()
        autoRestartJob = null
        noiseCalibrationJob?.cancel()
        noiseCalibrationJob = null
    }

    // ==================== СОЗДАНИЕ ФАЙЛА ====================
    private fun createAudioFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val audioFileName = "VOICE_INPUT_${timeStamp}.ogg"

        val storageDir = File(context.cacheDir, "voice_recording")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
            println("🎤 VoiceInput: Создана директория: ${storageDir.absolutePath}")
        }

        return File(storageDir, audioFileName).apply {
            createNewFile()
            println("🎤 VoiceInput: Создан файл: ${this.absolutePath}")
        }
    }

    // ==================== ОЧИСТКА ====================
    private fun cleanup() {
        println("🎤 VoiceInput: cleanup")
        cancelAllJobs()

        try {
            mediaRecorder?.release()
            mediaRecorder = null
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            audioFile?.delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        audioFile = null

        _isRecording.value = false
        savedStateHandle.set("isRecording", false)
        _isProcessing.value = false
        savedStateHandle.set("isProcessing", false)
        _isListening.value = false
        savedStateHandle.set("isListening", false)
        _audioLevel.value = 0f
        _recordingDuration.value = 0
        _isVoiceDetected.value = false
        isSilenceProcessing = false
        isSpeaking = false
        noiseSamples.clear()
        silenceCount = 0
        isCalibrated = false
        lastVoiceTime = 0L
    }

    // ==================== ПУБЛИЧНЫЕ МЕТОДЫ ====================
    fun clearRecognizedText() {
        _recognizedText.value = ""
        savedStateHandle.set("recognizedText", "")
    }

    fun clearError() {
        _error.value = null
    }

    fun reset() {
        cleanup()
        _recognizedText.value = ""
        savedStateHandle.set("recognizedText", "")
        _error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        // Сохраняем состояние
        savedStateHandle.set("isRecording", _isRecording.value)
        savedStateHandle.set("isProcessing", _isProcessing.value)
        savedStateHandle.set("isListening", _isListening.value)
        savedStateHandle.set("recognizedText", _recognizedText.value)
        cleanup()
        println("🎤 VoiceInputViewModel очищен, состояние сохранено")
    }
}