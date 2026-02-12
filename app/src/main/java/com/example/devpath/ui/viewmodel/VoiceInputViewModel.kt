// ui/viewmodel/VoiceInputViewModel.kt
package com.example.devpath.ui.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.devpath.api.speech.SaluteSpeechService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
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

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _audioLevel = MutableStateFlow(0f)
    val audioLevel: StateFlow<Float> = _audioLevel.asStateFlow()

    private val _recordingDuration = MutableStateFlow(0)
    val recordingDuration: StateFlow<Int> = _recordingDuration.asStateFlow()

    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var startTime: Long = 0

    // Jobs для корутин вместо Timer
    private var durationJob: Job? = null
    private var audioLevelJob: Job? = null

    // Проверка разрешения на запись аудио
    fun hasRecordAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Начать запись голоса
    fun startRecording() {
        if (!hasRecordAudioPermission()) {
            _error.value = "Нет разрешения на запись аудио"
            return
        }

        viewModelScope.launch {
            try {
                // Создаем файл для записи
                audioFile = createAudioFile()

                // Инициализируем MediaRecorder с совместимыми параметрами
                mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder(context)
                } else {
                    MediaRecorder()
                }.apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)

                    // Используем 3GP формат (поддерживается на всех API уровнях)
                    setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                    setAudioSamplingRate(8000)
                    setOutputFile(audioFile?.absolutePath)

                    prepare()
                    start()
                }

                _isRecording.value = true
                _error.value = null
                startTime = System.currentTimeMillis()

                // Запускаем таймер длительности записи с помощью корутин
                startDurationTimer()

                // Запускаем мониторинг уровня звука с помощью корутин
                startAudioLevelMonitor()

                println("🎤 VoiceInput: Начало записи, файл: ${audioFile?.absolutePath}")

            } catch (e: Exception) {
                _error.value = "Ошибка записи: ${e.message}"
                e.printStackTrace()
                cleanup()
            }
        }
    }

    // Остановить запись
    fun stopRecording() {
        try {
            mediaRecorder?.apply {
                try {
                    stop()
                } catch (e: Exception) {
                    // Игнорируем ошибки остановки
                }
                release()
            }
            mediaRecorder = null

            // Останавливаем корутины
            stopTimers()

            _isRecording.value = false
            _audioLevel.value = 0f

            val duration = if (startTime > 0) (System.currentTimeMillis() - startTime) / 1000 else 0
            println("🎤 VoiceInput: Запись остановлена, длительность: ${duration}с")

        } catch (e: Exception) {
            _error.value = "Ошибка остановки записи: ${e.message}"
            e.printStackTrace()
        }
    }

    // Остановить запись и распознать текст
    fun stopRecordingAndRecognize(onResult: ((String) -> Unit)? = null) {
        stopRecording()

        audioFile?.let { file ->
            if (file.exists() && file.length() > 0) {
                recognizeAudio(file, onResult)
            } else {
                _error.value = "Файл записи пуст или не существует"
                onResult?.invoke("")
            }
        }
    }

    // Распознать аудиофайл
    private fun recognizeAudio(file: File, onResult: ((String) -> Unit)? = null) {
        viewModelScope.launch {
            _isProcessing.value = true

            try {
                println("🎤 VoiceInput: Отправка на распознавание, размер: ${file.length()} байт")

                // Используем AMR кодек для распознавания
                val result = saluteSpeechService.recognizeSpeech(
                    audioFile = file,
                    mimeType = "audio/amr"
                )

                if (result.isSuccess) {
                    val text = result.getOrNull() ?: ""
                    _recognizedText.value = text
                    println("✅ VoiceInput: Распознано: \"$text\"")

                    // Удаляем временный файл
                    try {
                        file.delete()
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
                e.printStackTrace()
                onResult?.invoke("")
            } finally {
                _isProcessing.value = false
            }
        }
    }

    // ✅ ИСПРАВЛЕНО: Запуск таймера длительности с помощью корутин (вместо Timer)
    private fun startDurationTimer() {
        durationJob = viewModelScope.launch {
            while (_isRecording.value) {
                val duration = (System.currentTimeMillis() - startTime) / 1000
                _recordingDuration.value = duration.toInt()
                delay(1000) // Задержка 1 секунда
            }
        }
    }

    // ✅ ИСПРАВЛЕНО: Запуск мониторинга уровня звука с помощью корутин (вместо Timer)
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
                } catch (e: Exception) {
                    // Игнорируем ошибки амплитуды
                }
                delay(100) // Задержка 100 мс
            }
        }
    }

    // ✅ ИСПРАВЛЕНО: Остановка таймеров
    private fun stopTimers() {
        durationJob?.cancel()
        durationJob = null
        audioLevelJob?.cancel()
        audioLevelJob = null
    }

    // Создать файл для аудиозаписи
    private fun createAudioFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val audioFileName = "VOICE_INPUT_${timeStamp}.amr"

        val storageDir = File(context.cacheDir, "voice_recording")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        return File(storageDir, audioFileName).apply {
            createNewFile()
        }
    }

    // Очистка
    private fun cleanup() {
        stopTimers()

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
        _audioLevel.value = 0f
        _recordingDuration.value = 0
    }

    // Очистить распознанный текст
    fun clearRecognizedText() {
        _recognizedText.value = ""
    }

    // Очистить ошибку
    fun clearError() {
        _error.value = null
    }

    // Сбросить состояние
    fun reset() {
        cleanup()
        _recognizedText.value = ""
        _error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        cleanup()
    }
}