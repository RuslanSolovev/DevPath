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

// ИСПРАВЛЕНО: result может быть массивом строк или объектом
@Serializable
data class SaluteSpeechRecognitionResponse(
    @SerialName("result")
    val result: RecognitionResultData,

    @SerialName("status")
    val status: Int? = null,

    @SerialName("emotions")
    val emotions: List<EmotionData>? = null,

    @SerialName("person_identity")
    val personIdentity: PersonIdentityData? = null
)

@Serializable
data class RecognitionResultData(
    @SerialName("text")
    val text: String? = null,

    @SerialName("end_sec")
    val endSec: Double? = null,

    @SerialName("words")
    val words: List<Word>? = null
) {
    // Для обратной совместимости, если приходит массив строк
    companion object {
        fun fromStringList(strings: List<String>): RecognitionResultData {
            return RecognitionResultData(
                text = strings.firstOrNull() ?: ""
            )
        }
    }
}

@Serializable
data class EmotionData(
    @SerialName("negative")
    val negative: Float? = null,

    @SerialName("neutral")
    val neutral: Float? = null,

    @SerialName("positive")
    val positive: Float? = null
)

@Serializable
data class PersonIdentityData(
    @SerialName("age")
    val age: String? = null,

    @SerialName("gender")
    val gender: String? = null,

    @SerialName("age_score")
    val ageScore: Float? = null,

    @SerialName("gender_score")
    val genderScore: Float? = null
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

// Добавляем упрощенную модель для парсинга, если API возвращает result как массив
@Serializable
data class SimpleRecognitionResponse(
    @SerialName("result")
    val result: List<String> = emptyList(),

    @SerialName("status")
    val status: Int? = null,

    @SerialName("emotions")
    val emotions: List<EmotionData>? = null,

    @SerialName("person_identity")
    val personIdentity: PersonIdentityData? = null
)