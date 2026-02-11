// api/GigaChatConfig.kt
package com.example.devpath.api

object GigaChatConfig {
    // Базовый URL
    const val BASE_URL = "https://gigachat.devices.sberbank.ru/api/v1"

    // Эндпоинты
    const val CHAT_COMPLETION = "/chat/completions"
    const val AUTH_ENDPOINT = "https://ngw.devices.sberbank.ru:9443/api/v2/oauth"

    // ===== ВАШИ ДАННЫЕ ИЗ ЛИЧНОГО КАБИНЕТА =====
    // Client ID
    const val CLIENT_ID = "3badd479-4ec7-4ff2-a8dd-53295bd83c9d"

    // Client Secret (декодировано из Authorization Key)
    const val CLIENT_SECRET = "5d0a3be0-d64e-4d78-b63d-a51415bb4d43"

    // Authorization Key (base64 от Client ID:Client Secret)
    const val AUTH_KEY = "M2JhZGQ0NzktNGVjNy00ZmYyLWE4ZGQtNTMyOTViZDgzYzlkOjVkMGEzYmU0LWQ2NGUtNGQ3OC1iNjNkLWE1MTQxNWJiNGQ0Mw=="

    // Scope
    const val SCOPE = "GIGACHAT_API_PERS"
    // ============================================

    // Модели
    const val MODEL_GIGACHAT = "GigaChat"
    const val MODEL_GIGACHAT_PRO = "GigaChat-Pro"
    const val MODEL_GIGACHAT_PLUS = "GigaChat-Plus"

    // Хедеры
    const val HEADER_AUTHORIZATION = "Authorization"
    const val HEADER_CONTENT_TYPE = "Content-Type"
    const val CONTENT_TYPE_JSON = "application/json"
    const val CONTENT_TYPE_FORM = "application/x-www-form-urlencoded"
    const val HEADER_ACCEPT = "Accept"
    const val HEADER_RQUID = "RqUID"
}