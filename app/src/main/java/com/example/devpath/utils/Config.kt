package com.example.devpath.utils

object Config {
    // 🔑 Новые ключи от chatapp-image-uploader
    const val YC_ACCESS_KEY = "YCAJEa3xacmjl8RsaKFgdBi7E"
    const val YC_SECRET_KEY = "YCP4pa5MZb2gZ9M0nUTbTCQ4qIe6-zqnsNca51AF"

    // 🗄️ Document API конфигурация
    private const val YC_YDB_ENDPOINT = "https://docapi.serverless.yandexcloud.net"
    private const val YC_YDB_DATABASE = "/ru-central1/b1gi2cjf38q138pibbf9/etn0oj16t141i5bjhfkd"
    val YDB_FULL_ENDPOINT: String = "$YC_YDB_ENDPOINT$YC_YDB_DATABASE"

    // 🪣 Object Storage
    const val YC_BUCKET_NAME = "chatskii"

    // 🆔 Временный ID владельца
    const val OWNER_USER_ID = "nPX20T5lVTVQzjLTINkLp0f9xxI2"
}