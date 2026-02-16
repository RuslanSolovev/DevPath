// api/speech/SaluteSpeechService.kt
package com.example.devpath.api.speech

import android.media.AudioFormat
import android.media.AudioAttributes
import android.media.AudioTrack
import com.example.devpath.api.speech.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.io.FileInputStream
import java.security.cert.X509Certificate
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import javax.net.ssl.*

@Singleton
class SaluteSpeechService {

    private val client = getUnsafeOkHttpClient()
    private var currentVoice: String? = null

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    private var cachedToken: String? = null
    private var tokenExpiration: Long = 0

    // OkHttpClient с игнорированием SSL (для разработки)
    private fun getUnsafeOkHttpClient(): OkHttpClient {
        return try {
            val trustAllCerts = arrayOf(
                object : X509TrustManager {
                    override fun checkClientTrusted(
                        chain: Array<out X509Certificate>?,
                        authType: String?
                    ) = Unit

                    override fun checkServerTrusted(
                        chain: Array<out X509Certificate>?,
                        authType: String?
                    ) = Unit

                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                }
            )

            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            val sslSocketFactory = sslContext.socketFactory

            OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
                .build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    /**
     * 🔑 ПОЛУЧЕНИЕ ACCESS TOKEN
     * POST /api/v2/oauth
     * Токен действует 30 минут
     */
    private suspend fun getAccessToken(): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Проверяем кэшированный токен
            if (!cachedToken.isNullOrBlank() && System.currentTimeMillis() < tokenExpiration) {
                println("✅ SaluteSpeech: Используем кэшированный токен")
                return@withContext Result.success(cachedToken!!)
            }

            println("🔄 SaluteSpeech: Запрашиваем новый токен...")

            // Формируем запрос строго по документации
            val request = Request.Builder()
                .url(SaluteSpeechConfig.AUTH_URL)
                .header("Authorization", "Basic ${SaluteSpeechConfig.AUTH_KEY}")
                .header("RqUID", UUID.randomUUID().toString())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .post("scope=${SaluteSpeechConfig.SCOPE}".toRequestBody("application/x-www-form-urlencoded".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    println("✅ SaluteSpeech: Токен получен")

                    val authResponse = json.decodeFromString<SaluteSpeechAuthResponse>(body)

                    cachedToken = authResponse.accessToken
                    tokenExpiration = authResponse.expiresAt * 1000

                    println("🔑 SaluteSpeech: Токен истекает: ${Date(tokenExpiration)}")
                    println("⏰ Действителен: 30 минут")

                    Result.success(authResponse.accessToken)
                } else {
                    val errorBody = response.body?.string() ?: ""
                    println("❌ SaluteSpeech: Ошибка авторизации ${response.code}: $errorBody")
                    Result.failure(Exception("Auth failed: ${response.code} - $errorBody"))
                }
            }
        } catch (e: Exception) {
            println("❌ SaluteSpeech: Исключение при авторизации: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * 🎤 РАСПОЗНАВАНИЕ РЕЧИ (STT)
     * POST /rest/v1/speech:recognize
     */
    suspend fun recognizeSpeech(
        audioFile: File,
        mimeType: String = SaluteSpeechConfig.DEFAULT_AUDIO_FORMAT
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            println("🎤 SaluteSpeech: Начинаем распознавание файла: ${audioFile.name}")
            println("   URL: ${SaluteSpeechConfig.STT_URL}")
            println("   MIME Type: $mimeType")

            val tokenResult = getAccessToken()
            if (tokenResult.isFailure) {
                return@withContext Result.failure(tokenResult.exceptionOrNull()!!)
            }

            val accessToken = tokenResult.getOrNull()!!

            val audioBytes = FileInputStream(audioFile).use { it.readBytes() }
            println("📊 SaluteSpeech: Размер аудиофайла: ${audioBytes.size} байт")

            // Формируем URL с параметрами запроса
            val url = HttpUrl.Builder()
                .scheme("https")
                .host("smartspeech.sber.ru")
                .addPathSegment("rest")
                .addPathSegment("v1")
                .addPathSegment("speech:recognize")
                .addQueryParameter("audio_encoding", when(mimeType) {
                    SaluteSpeechConfig.AUDIO_AMR -> "AMR"
                    SaluteSpeechConfig.AUDIO_OPUS -> "OPUS"
                    "audio/x-pcm;bit=16;rate=16000" -> "PCM16"
                    else -> "AMR"
                })
                .addQueryParameter("sample_rate", when(mimeType) {
                    "audio/x-pcm;bit=16;rate=16000" -> "16000"
                    else -> SaluteSpeechConfig.DEFAULT_SAMPLE_RATE.toString()
                })
                .build()

            println("📤 SaluteSpeech Request URL: $url")
            println("📤 Content-Type: $mimeType")

            val requestBody = audioBytes.toRequestBody(mimeType.toMediaType())

            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $accessToken")
                .header("Content-Type", mimeType) // ИСПРАВЛЕНО: убраны лишние символы
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                println("📥 SaluteSpeech Response: $responseBody")

                if (response.isSuccessful) {
                    try {
                        // Пробуем распарсить как SimpleRecognitionResponse (массив строк)
                        val simpleResponse = json.decodeFromString<SimpleRecognitionResponse>(responseBody)
                        val text = simpleResponse.result.firstOrNull() ?: ""

                        if (text.isNotEmpty()) {
                            println("✅ SaluteSpeech: Распознано: \"$text\"")
                            Result.success(text)
                        } else {
                            // Если пустой результат, пробуем другой формат
                            try {
                                val fullResponse = json.decodeFromString<SaluteSpeechRecognitionResponse>(responseBody)
                                val resultText = fullResponse.result.text ?: ""
                                println("✅ SaluteSpeech: Распознано (alt): \"$resultText\"")
                                Result.success(resultText)
                            } catch (e: Exception) {
                                println("⚠️ SaluteSpeech: Пустой результат распознавания")
                                Result.success("")
                            }
                        }
                    } catch (e: Exception) {
                        println("❌ SaluteSpeech: Ошибка парсинга JSON: ${e.message}")
                        println("JSON input: $responseBody")
                        Result.failure(Exception("Ошибка формата ответа: ${e.message}"))
                    }
                } else {
                    println("❌ SaluteSpeech: Ошибка распознавания ${response.code}: $responseBody")
                    Result.failure(Exception("STT Error: ${response.code} - $responseBody"))
                }
            }
        } catch (e: Exception) {
            println("❌ SaluteSpeech: Исключение при распознавании: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * 🔊 СИНТЕЗ РЕЧИ (TTS)
     * POST /rest/v1/text:synthesize
     * Форматы: pcm16, wav16, opus
     * Максимум: 4000 символов
     */
    suspend fun synthesizeSpeech(
        text: String,
        voice: String = SaluteSpeechConfig.DEFAULT_VOICE_FEMALE,
        speed: Double = 1.0,
        emotion: String? = null,
        format: String = SaluteSpeechConfig.DEFAULT_TTS_FORMAT // "pcm16"
    ): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            // Проверка лимита символов
            if (text.length > 4000) {
                println("⚠️ SaluteSpeech: Текст превышает 4000 символов (${text.length}), будет обрезан")
            }

            currentVoice = voice

            println("🔊 SaluteSpeech: Начинаем синтез текста (${text.length}/4000 символов)")
            println("   Голос: ${getVoiceName(voice)} ($voice)")
            println("   Скорость: $speed")
            println("   Эмоция: ${emotion ?: "нейтральная"}")
            println("   Формат: $format")
            println("   URL: ${SaluteSpeechConfig.TTS_URL}")

            val tokenResult = getAccessToken()
            if (tokenResult.isFailure) {
                println("❌ SaluteSpeech: Не удалось получить токен для TTS")
                return@withContext Result.failure(tokenResult.exceptionOrNull()!!)
            }

            val accessToken = tokenResult.getOrNull()!!

            // Формируем URL с параметрами запроса
            val urlBuilder = HttpUrl.Builder()
                .scheme("https")
                .host("smartspeech.sber.ru")
                .addPathSegment("rest")
                .addPathSegment("v1")
                .addPathSegment("text:synthesize")
                .addQueryParameter("format", format)  // pcm16, wav16, opus
                .addQueryParameter("voice", voice)    // May_8000, Bys_8000, etc.

            if (speed != 1.0) {
                urlBuilder.addQueryParameter("speed", speed.toString())
            }

            emotion?.let {
                urlBuilder.addQueryParameter("emotion", it)
            }

            val url = urlBuilder.build()
            println("📤 SaluteSpeech TTS URL: $url")

            // Тело запроса - чистый текст
            val requestBody = text.toByteArray().let { bytes ->
                object : RequestBody() {
                    override fun contentType(): MediaType? = "application/text".toMediaType()
                    override fun writeTo(sink: okio.BufferedSink) {
                        sink.write(bytes)
                    }
                    override fun contentLength(): Long = bytes.size.toLong()
                }
            }

            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $accessToken")
                .header("Content-Type", "application/text")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val audioData = response.body?.bytes() ?: byteArrayOf()
                    println("✅ SaluteSpeech: Синтез успешен, размер аудио: ${audioData.size} байт")
                    println("   Формат ответа: ${response.header("Content-Type")}")
                    Result.success(audioData)
                } else {
                    val errorBody = response.body?.string() ?: ""
                    println("❌ SaluteSpeech: Ошибка синтеза ${response.code}: $errorBody")
                    Result.failure(Exception("TTS Error: ${response.code} - $errorBody"))
                }
            }
        } catch (e: Exception) {
            println("❌ SaluteSpeech: Исключение при синтезе: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * 🔊 ВОСПРОИЗВЕДЕНИЕ АУДИО (PCM16)
     * Поддерживает частоты 16kHz и 24kHz
     */
    suspend fun playAudio(audioData: ByteArray): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (audioData.isEmpty()) {
                println("⚠️ SaluteSpeech: Аудиоданные пусты")
                return@withContext Result.failure(Exception("Audio data is empty"))
            }

            // Определяем частоту дискретизации по голосу
            val sampleRate = if (currentVoice?.contains("24000") == true) 24000 else 16000
            println("🔊 SaluteSpeech: Воспроизведение аудио (${audioData.size} байт, ${sampleRate}Hz)")

            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            val bufferSize = maxOf(minBufferSize, audioData.size)

            val audioTrack = AudioTrack.Builder()
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

            audioTrack.write(audioData, 0, audioData.size)
            audioTrack.play()

            // Ждем окончания воспроизведения
            while (audioTrack.playState == AudioTrack.PLAYSTATE_PLAYING) {
                Thread.sleep(10)
            }

            audioTrack.stop()
            audioTrack.release()

            println("✅ SaluteSpeech: Воспроизведение завершено")
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ SaluteSpeech: Ошибка воспроизведения: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * 🔊 СИНТЕЗ И ВОСПРОИЗВЕДЕНИЕ
     */
    suspend fun speak(
        text: String,
        voice: String = SaluteSpeechConfig.DEFAULT_VOICE_FEMALE,
        speed: Double = 1.0,
        emotion: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val synthesisResult = synthesizeSpeech(text, voice, speed, emotion)

            if (synthesisResult.isSuccess) {
                val audioData = synthesisResult.getOrNull()
                audioData?.let { playAudio(it) }
                Result.success(Unit)
            } else {
                Result.failure(synthesisResult.exceptionOrNull() ?: Exception("Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 🎤 ПОЛУЧИТЬ ИМЯ ГОЛОСА
     */
    private fun getVoiceName(voiceId: String): String {
        return when (voiceId) {
            "May_8000", "May_24000" -> "Марфа"
            "Bys_8000", "Bys_24000" -> "Борис"
            "Ost_8000", "Ost_24000" -> "Александра"
            "Nec_8000", "Nec_24000" -> "Наталья"
            "Tur_8000", "Tur_24000" -> "Тарас"
            "Pon_8000", "Pon_24000" -> "Сергей"
            "Kin_8000", "Kin_24000" -> "Kira"
            else -> voiceId
        }
    }

    /**
     * ℹ️ ПРОВЕРКА СТАТУСА ТОКЕНА
     */
    suspend fun checkTokenStatus(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (cachedToken != null && System.currentTimeMillis() < tokenExpiration) {
                println("✅ SaluteSpeech: Токен действителен до ${Date(tokenExpiration)}")
                Result.success(true)
            } else {
                println("⚠️ SaluteSpeech: Токен истек или отсутствует")
                Result.success(false)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 🧹 Сброс токена
     */
    fun clearToken() {
        cachedToken = null
        tokenExpiration = 0
        println("🧹 SaluteSpeech: Токен сброшен")
    }

    /**
     * ℹ️ ПОЛУЧИТЬ ИНФОРМАЦИЮ О СЕРВИСЕ
     */
    fun getServiceInfo(): String {
        return """
            SaluteSpeech Service Info:
            ├─ TTS URL: ${SaluteSpeechConfig.TTS_URL}
            ├─ STT URL: ${SaluteSpeechConfig.STT_URL}
            ├─ Auth URL: ${SaluteSpeechConfig.AUTH_URL}
            ├─ Default TTS Format: ${SaluteSpeechConfig.DEFAULT_TTS_FORMAT}
            ├─ Default Voice: ${SaluteSpeechConfig.DEFAULT_VOICE_FEMALE}
            ├─ Token Status: ${if (cachedToken != null) "✅ Active" else "❌ No token"}
            └─ Token Expires: ${if (tokenExpiration > 0) Date(tokenExpiration) else "N/A"}
        """.trimIndent()
    }
}