// ui/components/VoiceInputButton.kt
package com.example.devpath.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.devpath.ui.viewmodel.VoiceInputViewModel

@Composable
fun VoiceInputButton(
    onTextRecognized: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VoiceInputViewModel = hiltViewModel()
) {
    val isRecording by viewModel.isRecording.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val recognizedText by viewModel.recognizedText.collectAsState()
    val error by viewModel.error.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(recognizedText) {
        if (recognizedText.isNotBlank()) {
            onTextRecognized(recognizedText)
            viewModel.clearRecognizedText()
        }
    }

    LaunchedEffect(error) {
        error?.let {
            // Показать ошибку
            println("Voice error: $it")
            viewModel.clearError()
        }
    }

    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(
                when {
                    isProcessing -> MaterialTheme.colorScheme.tertiary
                    isRecording -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                }
            )
            .clickable(
                enabled = !isProcessing,
                onClick = {
                    if (isRecording) {
                        viewModel.stopRecordingAndRecognize()
                    } else {
                        viewModel.startRecording()
                    }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        // Анимированный индикатор записи
        if (isRecording) {
            InfiniteTransitionEffect()
        }

        Icon(
            imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
            contentDescription = if (isRecording) "Остановить запись" else "Голосовой ввод",
            tint = when {
                isProcessing -> MaterialTheme.colorScheme.onTertiary
                isRecording -> MaterialTheme.colorScheme.onError
                else -> MaterialTheme.colorScheme.primary
            },
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun InfiniteTransitionEffect() {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
            .scale(scale)
    )
}

@Composable
fun VoiceSettingsDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onVoiceSelected: (String) -> Unit,
    onSpeedSelected: (Double) -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Настройки голоса") },
            text = {
                Column {
                    Text("Выберите голос:", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))

                    VoiceOption("Мэй (женский)", "May")
                    VoiceOption("Ост (мужской)", "Ost")
                    VoiceOption("Бус (мужской)", "Bys")
                    VoiceOption("Нез (женский)", "Nez")
                    VoiceOption("Микс (мультиязычный)", "Mix")
                    VoiceOption("Пон (детский)", "Pon")
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Закрыть")
                }
            }
        )
    }
}

@Composable
private fun VoiceOption(name: String, id: String) {
    // Реализация выбора голоса
}