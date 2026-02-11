// api/GigaChatService.kt
package com.example.devpath.api

import android.util.Base64
import com.example.devpath.api.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.security.cert.X509Certificate
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import javax.net.ssl.*

@Singleton
class GigaChatService {

    // Специальный клиент для разработки - игнорирует SSL
    private val client = getUnsafeOkHttpClient()

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        isLenient = true
        encodeDefaults = true
    }

    private var cachedToken: String? = null
    private var tokenExpiration: Long = 0

    // 📌 ВАЖНО: Создаем OkHttpClient, который игнорирует SSL сертификаты
    private fun getUnsafeOkHttpClient(): OkHttpClient {
        return try {
            // Создаем TrustManager, который доверяет ВСЕМ сертификатам
            val trustAllCerts = arrayOf(
                object : X509TrustManager {
                    override fun checkClientTrusted(
                        chain: Array<out X509Certificate>?,
                        authType: String?
                    ) {
                        // Доверяем всем клиентским сертификатам
                    }

                    override fun checkServerTrusted(
                        chain: Array<out X509Certificate>?,
                        authType: String?
                    ) {
                        // Доверяем всем серверным сертификатам
                    }

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
                .hostnameVerifier { _, _ -> true } // Игнорируем проверку hostname
                .build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    // Получение токена авторизации
    private suspend fun getAccessToken(): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Проверяем кэшированный токен
            if (!cachedToken.isNullOrBlank() && System.currentTimeMillis() < tokenExpiration) {
                println("✅ Используем кэшированный токен")
                return@withContext Result.success(cachedToken!!)
            }

            println("🔄 Запрашиваем новый токен...")

            // Используем готовый Authorization Key
            val request = Request.Builder()
                .url(GigaChatConfig.AUTH_ENDPOINT)
                .header("Authorization", "Basic ${GigaChatConfig.AUTH_KEY}")
                .header("RqUID", UUID.randomUUID().toString())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .post("scope=${GigaChatConfig.SCOPE}".toRequestBody("application/x-www-form-urlencoded".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    println("✅ GigaChat Auth успешно!")
                    println("📥 Response: ${responseBody.take(200)}...")

                    val authResponse = json.decodeFromString<GigaChatAuthResponse>(responseBody)

                    // Кэшируем токен
                    cachedToken = authResponse.accessToken
                    tokenExpiration = authResponse.expiresAt * 1000

                    println("🔑 Токен получен, истекает: ${Date(tokenExpiration)}")

                    Result.success(authResponse.accessToken)
                } else {
                    val errorBody = response.body?.string() ?: ""
                    println("❌ GigaChat Auth ошибка ${response.code}: $errorBody")
                    Result.failure(Exception("Auth Error: ${response.code} - $errorBody"))
                }
            }
        } catch (e: Exception) {
            println("❌ GigaChat Auth исключение: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // Отправка сообщения
    suspend fun sendMessage(
        messages: List<com.example.devpath.api.models.GigaChatMessage>,
        maxTokens: Int? = 2048
    ): Result<GigaChatResponse> = withContext(Dispatchers.IO) {
        try {
            // Получаем токен доступа
            val tokenResult = getAccessToken()
            if (tokenResult.isFailure) {
                val error = tokenResult.exceptionOrNull()
                println("❌ Не удалось получить токен: ${error?.message}")
                return@withContext Result.failure(error ?: Exception("Unknown auth error"))
            }

            val accessToken = tokenResult.getOrNull() ?: return@withContext Result.failure(Exception("No access token"))

            val request = GigaChatRequest(
                model = GigaChatConfig.MODEL_GIGACHAT,
                messages = messages,
                max_tokens = maxTokens,
                temperature = 0.7,
                stream = false
            )

            val requestJson = json.encodeToString(request)
            println("📤 GigaChat Request:")
            println("📤 URL: ${GigaChatConfig.BASE_URL}${GigaChatConfig.CHAT_COMPLETION}")
            println("📤 Messages: ${messages.size} сообщений")
            println("📤 Body: ${requestJson.take(300)}...")

            val requestBody = requestJson
                .toRequestBody("application/json".toMediaType())

            val httpRequest = Request.Builder()
                .url("${GigaChatConfig.BASE_URL}${GigaChatConfig.CHAT_COMPLETION}")
                .header("Authorization", "Bearer $accessToken")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .post(requestBody)
                .build()

            client.newCall(httpRequest).execute().use { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    println("📥 GigaChat Response успешно!")
                    println("📥 Body: ${responseBody.take(300)}...")

                    val gigaChatResponse = json.decodeFromString<GigaChatResponse>(responseBody)
                    Result.success(gigaChatResponse)
                } else {
                    val errorBody = response.body?.string() ?: ""
                    println("❌ GigaChat API ошибка ${response.code}: $errorBody")

                    // Специальная обработка для ошибок аутентификации
                    if (response.code == 401) {
                        println("🔄 Токен истек, сбрасываем...")
                        cachedToken = null
                    }

                    Result.failure(Exception("GigaChat Error ${response.code}: $errorBody"))
                }
            }
        } catch (e: Exception) {
            println("❌ GigaChat Service ошибка: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // Метод для сброса токена (можно вызвать при выходе из аккаунта)
    fun clearToken() {
        cachedToken = null
        println("🧹 Токен сброшен")
    }
}