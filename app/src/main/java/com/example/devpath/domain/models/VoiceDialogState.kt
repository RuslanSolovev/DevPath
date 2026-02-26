package com.example.devpath.domain.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.ui.graphics.vector.ImageVector

enum class VoiceDialogState {
    IDLE,           // режим диалога выключен или микрофон неактивен
    LISTENING,      // микрофон слушает пользователя
    THINKING,       // ожидание ответа от GigaChat
    SPEAKING;       // озвучка ответа ИИ

    val displayText: String
        get() = when (this) {
            IDLE -> "Готов к вопросу"
            LISTENING -> "Слушаю..."
            THINKING -> "Думаю..."
            SPEAKING -> "Отвечаю..."
        }

    val icon: ImageVector
        get() = when (this) {
            IDLE -> Icons.Default.MicOff
            LISTENING -> Icons.Default.Mic
            THINKING -> Icons.Default.HourglassEmpty
            SPEAKING -> Icons.Default.VolumeUp
        }
}