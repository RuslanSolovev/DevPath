package com.example.devpath.utils

import java.io.FileInputStream
import java.util.Properties

object Config {
    private val properties = Properties()

    init {
        try {
            // Пробуем загрузить local.properties
            val file = FileInputStream("local.properties")
            properties.load(file)
            file.close()
        } catch (e: Exception) {
            println("⚠️ local.properties не найден")
        }
    }

    val YC_ACCESS_KEY: String
        get() = properties.getProperty("yc_access_key", "")

    val YC_SECRET_KEY: String
        get() = properties.getProperty("yc_secret_key", "")

    const val YC_BUCKET_NAME = "chatskii"

    private const val YC_YDB_ENDPOINT = "https://docapi.serverless.yandexcloud.net"
    private const val YC_YDB_DATABASE = "/ru-central1/b1gi2cjf38q138pibbf9/etn0oj16t141i5bjhfkd"
    val YDB_FULL_ENDPOINT: String = "$YC_YDB_ENDPOINT$YC_YDB_DATABASE"

    const val OWNER_USER_ID = "nPX20T5lVTVQzjLTINkLp0f9xxI2"
}