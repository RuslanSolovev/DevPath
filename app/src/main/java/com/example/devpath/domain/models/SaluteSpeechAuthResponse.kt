// api/speech/models/SaluteSpeechModels.kt
package com.example.devpath.api.speech.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SaluteSpeechAuthResponse(
    @SerialName("access_token")
    val accessToken: String,

    @SerialName("expires_at")
    val expiresAt: Long
)

@Serializable
data class SaluteSpeechRecognitionRequest(
    @SerialName("options")
    val options: RecognitionOptions = RecognitionOptions()
)

@Serializable
data class RecognitionOptions(
    @SerialName("audio_encoding")
    val audioEncoding: String = "OPUS", // или PCM_S16LE

    @SerialName("sample_rate")
    val sampleRate: Int = 16000,

    @SerialName("language")
    val language: String = "ru-RU",

    @SerialName("enable_profanity_filter")
    val enableProfanityFilter: Boolean = true,

    @SerialName("enable_partial_results")
    val enablePartialResults: Boolean = false
)

@Serializable
data class SaluteSpeechRecognitionResponse(
    @SerialName("result")
    val result: RecognitionResult
)

@Serializable
data class RecognitionResult(
    @SerialName("text")
    val text: String,

    @SerialName("end_sec")
    val endSec: Double? = null,

    @SerialName("words")
    val words: List<Word>? = null
)

@Serializable
data class Word(
    @SerialName("word")
    val word: String,

    @SerialName("start_sec")
    val startSec: Double,

    @SerialName("end_sec")
    val endSec: Double
)

@Serializable
data class SaluteSpeechSynthesisRequest(
    @SerialName("text")
    val text: String,

    @SerialName("voice")
    val voice: String = "May", // Ost, Bys, May, Nez, Mix, Pon

    @SerialName("format")
    val format: String = "audio/x-pcm;bit=16;rate=16000",

    @SerialName("speed")
    val speed: Double = 1.0,

    @SerialName("emotion")
    val emotion: String? = null, // good, evil, neutral, sad, whisper

    @SerialName("pitch")
    val pitch: Double? = null
)