// api/speech/SaluteSpeechConfig.kt
package com.example.devpath.api.speech

object SaluteSpeechConfig {
    // Базовые URL
    const val AUTH_URL = "https://ngw.devices.sberbank.ru:9443/api/v2/oauth"
    const val STT_URL = "https://smartspeech.sber.ru/rest/v1/speech:recognize"
    const val TTS_URL = "https://smartspeech.sber.ru/rest/v1/text:synthesize"

    // ===== ВАШИ ДАННЫЕ ИЗ ЛИЧНОГО КАБИНЕТА =====
    const val CLIENT_ID = "019c4c8a-8205-7d18-b610-ca8788dce739"
    const val CLIENT_SECRET = "3ac08012-1043-49c3-a33c-329cd885ed64"
    const val AUTH_KEY = "MDE5YzRjOGEtODIwNS03ZDE4LWI2MTAtY2E4Nzg4ZGNlNzM5OjNhYzA4MDEyLTEwNDMtNDljMy1hMzNjLTMyOWNkODg1ZWQ2NA=="
    const val SCOPE = "SALUTE_SPEECH_PERS"
    // =============================================

    // === ФОРМАТЫ АУДИО ДЛЯ TTS ===
    const val AUDIO_PCM16 = "pcm16"        // RAW PCM 16bit

    // === ФОРМАТЫ АУДИО ДЛЯ STT ===
    const val AUDIO_OPUS = "audio/opus"
    const val AUDIO_AMR = "audio/amr"

    // Частоты дискретизации
    const val SAMPLE_RATE_24K = 24000 // ✅ ТОЛЬКО 24kHz!

    // Формат по умолчанию для STT
    const val DEFAULT_AUDIO_FORMAT = AUDIO_AMR
    const val DEFAULT_SAMPLE_RATE = SAMPLE_RATE_24K

    // Формат по умолчанию для TTS
    const val DEFAULT_TTS_FORMAT = AUDIO_PCM16

    // ===== ГОЛОСА - ТОЛЬКО 24kHz! =====
    val AVAILABLE_VOICES = listOf(
        // Женские голоса
        Voice("May_24000", "Мэй", "Женский", "24kHz"),
        Voice("Nez_24000", "Нез", "Женский", "24kHz"),
        Voice("Ost_24000", "Александра", "Женский", "24kHz"),

        // Мужские голоса
        Voice("Bys_24000", "Борис", "Мужской", "24kHz"),
        Voice("Tur_24000", "Тарас", "Мужской", "24kHz"),
        Voice("Pon_24000", "Сергей", "Мужской", "24kHz"),

        // Английский голос
        Voice("Kin_24000", "Kira", "Женский", "24kHz", "en-US")
    )

    // Голоса по умолчанию - ТОЛЬКО 24kHz!
    const val DEFAULT_VOICE = "May_24000"
    const val DEFAULT_VOICE_MALE = "Bys_24000"
    const val DEFAULT_VOICE_FEMALE = "May_24000"
    const val DEFAULT_VOICE_CHILD = "Pon_24000"

    // Эмоции для синтеза
    val AVAILABLE_EMOTIONS = listOf(
        Emotion("neutral", "Нейтральный"),
        Emotion("good", "Добрый"),
        Emotion("evil", "Злой"),
        Emotion("sad", "Грустный"),
        Emotion("whisper", "Шёпот")
    )

    data class Voice(
        val id: String,
        val name: String,
        val gender: String,
        val quality: String = "24kHz",
        val language: String = "ru-RU"
    )

    data class Emotion(
        val id: String,
        val name: String
    )
}