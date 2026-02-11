// api/models/GigaChatRequest.kt
package com.example.devpath.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GigaChatAuthRequest(
    @SerialName("scope")
    val scope: String = "GIGACHAT_API_PERS"
)

@Serializable
data class GigaChatAuthResponse(
    @SerialName("access_token")
    val accessToken: String,

    @SerialName("expires_at")
    val expiresAt: Long
)

@Serializable
data class GigaChatRequest(
    @SerialName("model")
    val model: String = "GigaChat",

    @SerialName("messages")
    val messages: List<GigaChatMessage>,

    @SerialName("temperature")
    val temperature: Double = 0.7,

    @SerialName("max_tokens")
    val max_tokens: Int? = null,

    @SerialName("stream")
    val stream: Boolean = false
)

@Serializable
data class GigaChatMessage(
    @SerialName("role")
    val role: String, // "system", "user", "assistant"

    @SerialName("content")
    val content: String
)

@SerialName("choices")
@Serializable
data class GigaChatResponse(
    @SerialName("choices")
    val choices: List<GigaChatChoice>,

    @SerialName("created")
    val created: Long,

    @SerialName("model")
    val model: String,

    @SerialName("usage")
    val usage: GigaChatUsage?
)

@Serializable
data class GigaChatChoice(
    @SerialName("message")
    val message: GigaChatMessage,

    @SerialName("finish_reason")
    val finishReason: String?,

    @SerialName("index")
    val index: Int
)

@Serializable
data class GigaChatUsage(
    @SerialName("prompt_tokens")
    val promptTokens: Int,

    @SerialName("completion_tokens")
    val completionTokens: Int,

    @SerialName("total_tokens")
    val totalTokens: Int
)