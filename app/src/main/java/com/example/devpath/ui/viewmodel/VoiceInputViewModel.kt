// ui/viewmodel/VoiceInputViewModel.kt
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
    private val saluteSpeechService: SaluteSpeechService
) : ViewModel() {

    // Состояния
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
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

    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var startTime: Long = 0

    private var durationJob: Job? = null
    private var audioLevelJob: Job? = null
    private var silenceDetectorJob: Job? = null
    private var autoRestartJob: Job? = null

    private var lastVoiceTime = 0L
    private val silenceThreshold = 1500L
    private var isSpeaking = false
    private var currentCallback: ((String) -> Unit)? = null

    init {
        println("🎤 VoiceInputViewModel инициализирован")
    }

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

    fun startListening(onResult: (String) -> Unit) {
        println("🎤 VoiceInput: startListening вызван")

        if (!hasRecordAudioPermission()) {
            _error.value = "Нет разрешения на запись аудио"
            _showPermissionDialog.value = true
            println("❌ VoiceInput: Нет разрешения")
            return
        }

        stopRecording()

        _isListening.value = true
        _error.value = null
        currentCallback = onResult
        startRecordingWithCallback()
    }

    fun stopListening() {
        println("🎤 VoiceInput: stopListening вызван")
        _isListening.value = false
        currentCallback = null
        stopRecording()
        autoRestartJob?.cancel()
        autoRestartJob = null
    }

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
                    // Используем OPUS в OGG контейнере
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
                _isVoiceDetected.value = false
                isSpeaking = false
                startTime = System.currentTimeMillis()
                lastVoiceTime = startTime

                startDurationTimer()
                startAudioLevelMonitor()
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

    private fun startSilenceDetector() {
        silenceDetectorJob = viewModelScope.launch {
            while (_isRecording.value) {
                delay(100)

                if (_isVoiceDetected.value) {
                    if (!isSpeaking) {
                        isSpeaking = true
                        lastVoiceTime = System.currentTimeMillis()
                        println("🎤 VoiceInput: Голос обнаружен")
                    }
                    lastVoiceTime = System.currentTimeMillis()
                } else {
                    if (isSpeaking) {
                        val silenceDuration = System.currentTimeMillis() - lastVoiceTime
                        if (silenceDuration > silenceThreshold) {
                            println("🎤 VoiceInput: Тишина ${silenceDuration}мс, останавливаем")
                            isSpeaking = false
                            stopRecordingAndRecognize { text ->
                                currentCallback?.invoke(text)
                                if (_isListening.value) {
                                    autoRestartJob = viewModelScope.launch {
                                        delay(500)
                                        startRecordingWithCallback()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

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

            _isRecording.value = false
            _audioLevel.value = 0f

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

    private fun recognizeAudio(file: File, onResult: ((String) -> Unit)? = null) {
        viewModelScope.launch {
            _isProcessing.value = true

            try {
                println("🎤 VoiceInput: Отправка на распознавание, размер: ${file.length()} байт")

                // Используем OPUS формат
                val result = saluteSpeechService.recognizeSpeech(
                    audioFile = file,
                    mimeType = "audio/ogg;codecs=opus"
                )

                if (result.isSuccess) {
                    val text = result.getOrNull() ?: ""
                    _recognizedText.value = text
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
            }
        }
    }

    private fun startDurationTimer() {
        durationJob = viewModelScope.launch {
            while (_isRecording.value) {
                val duration = (System.currentTimeMillis() - startTime) / 1000
                _recordingDuration.value = duration.toInt()
                delay(1000)
            }
        }
    }

    private fun startAudioLevelMonitor() {
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
                    _isVoiceDetected.value = _audioLevel.value > 0.15f
                } catch (e: Exception) {
                    // Игнорируем ошибки амплитуды
                }
                delay(100)
            }
        }
    }

    private fun stopTimers() {
        durationJob?.cancel()
        durationJob = null
        audioLevelJob?.cancel()
        audioLevelJob = null
    }

    private fun createAudioFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val audioFileName = "VOICE_INPUT_${timeStamp}.ogg" // .ogg для OPUS

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

    private fun cleanup() {
        println("🎤 VoiceInput: cleanup")
        stopTimers()
        silenceDetectorJob?.cancel()
        silenceDetectorJob = null
        autoRestartJob?.cancel()
        autoRestartJob = null

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
        _isProcessing.value = false
        _isListening.value = false
        _audioLevel.value = 0f
        _recordingDuration.value = 0
    }

    fun clearRecognizedText() {
        _recognizedText.value = ""
    }

    fun clearError() {
        _error.value = null
    }

    fun reset() {
        cleanup()
        _recognizedText.value = ""
        _error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        cleanup()
        println("🎤 VoiceInputViewModel очищен")
    }
}