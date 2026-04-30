package com.example.devpath.data.repository

import com.example.devpath.utils.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YdbRepository @Inject constructor() {

    private val documentApiEndpoint = Config.YDB_FULL_ENDPOINT
    private val accessKey = Config.YC_ACCESS_KEY
    private val secretKey = Config.YC_SECRET_KEY
    private val region = "ru-central1"
    private val service = "dynamodb"
    private val salt = "devpath_salt_v1_secure"

    // ✅ Имя таблицы Document API
    private val tableName = "users_doc"

    init {
        if (accessKey.isEmpty() || secretKey.isEmpty()) {
            println("YDB: ⚠️ Ключи доступа не настроены в Config.kt")
        }
        println("YDB: ✅ Инициализирован. Endpoint: $documentApiEndpoint, Table: $tableName")
    }

    // ==================== ИНИЦИАЛИЗАЦИЯ БАЗЫ ====================

    suspend fun initDatabase(): Boolean {
        return try {
            // Пробуем описать таблицу — если есть, значит создана
            val describeBody = JSONObject().apply {
                put("TableName", tableName)
            }
            val existing = executeSignedRequest("DescribeTable", describeBody)
            if (existing != null) {
                println("YDB: ✅ Таблица $tableName уже существует")
                return true
            }

            // Создаём таблицу
            println("YDB: Создаю таблицу $tableName...")
            createUsersDocTable()
        } catch (e: Exception) {
            println("YDB: Ошибка проверки таблицы, пробую создать: ${e.message}")
            createUsersDocTable()
        }
    }

    private suspend fun createUsersDocTable(): Boolean {
        val body = JSONObject().apply {
            put("TableName", tableName)
            put("KeySchema", JSONArray().apply {
                put(JSONObject().apply {
                    put("AttributeName", "user_id")
                    put("KeyType", "HASH")
                })
            })
            put("AttributeDefinitions", JSONArray().apply {
                put(JSONObject().apply {
                    put("AttributeName", "user_id")
                    put("AttributeType", "S")
                })
            })
        }

        val result = executeSignedRequest("CreateTable", body)
        if (result != null) {
            println("YDB: ✅ Таблица $tableName создана успешно")
            return true
        }
        println("YDB: ❌ Не удалось создать таблицу $tableName")
        return false
    }

    // ==================== ПОЛЬЗОВАТЕЛИ ====================

    suspend fun createUser(
        userId: String,
        name: String,
        email: String,
        password: String,
        avatarUrl: String = ""
    ): Boolean {
        val now = System.currentTimeMillis().toString()
        val item = JSONObject().apply {
            put("user_id", JSONObject().put("S", userId))
            put("name", JSONObject().put("S", name))
            put("email", JSONObject().put("S", email))
            put("password_hash", JSONObject().put("S", hashPassword(password)))
            put("avatar_url", JSONObject().put("S", avatarUrl))
            put("created_at", JSONObject().put("S", now))
            put("last_seen", JSONObject().put("S", now))
            put("is_active", JSONObject().put("BOOL", true))
        }

        val body = JSONObject().apply {
            put("TableName", tableName)
            put("Item", item)
        }

        val result = executeSignedRequest("PutItem", body)
        return result != null
    }

    suspend fun getUser(userId: String): JSONObject? {
        val key = JSONObject().apply {
            put("user_id", JSONObject().put("S", userId))
        }
        val body = JSONObject().apply {
            put("TableName", tableName)
            put("Key", key)
        }
        val result = executeSignedRequest("GetItem", body)
        return result?.optJSONObject("Item")
    }

    suspend fun findUserByEmail(email: String): JSONObject? {
        val body = JSONObject().apply {
            put("TableName", tableName)
            put("FilterExpression", "#email = :email")
            put("ExpressionAttributeNames", JSONObject().apply {
                put("#email", "email")
            })
            put("ExpressionAttributeValues", JSONObject().apply {
                put(":email", JSONObject().put("S", email))
            })
        }
        val result = executeSignedRequest("Scan", body)
        val items = result?.optJSONArray("Items")
        return if (items != null && items.length() > 0) items.getJSONObject(0) else null
    }

    suspend fun authenticateUser(email: String, password: String): String? {
        val user = findUserByEmail(email) ?: return null
        val storedHash = user.optJSONObject("password_hash")?.optString("S") ?: return null
        val inputHash = hashPassword(password)
        return if (storedHash == inputHash) {
            user.optJSONObject("user_id")?.optString("S")
        } else null
    }

    suspend fun updateLastSeen(userId: String): Boolean {
        val key = JSONObject().apply {
            put("user_id", JSONObject().put("S", userId))
        }
        val updates = JSONObject().apply {
            put(":lastSeen", JSONObject().put("S", System.currentTimeMillis().toString()))
        }
        val body = JSONObject().apply {
            put("TableName", tableName)
            put("Key", key)
            put("UpdateExpression", "SET last_seen = :lastSeen")
            put("ExpressionAttributeValues", updates)
        }
        return executeSignedRequest("UpdateItem", body) != null
    }

    suspend fun updateUserAvatar(userId: String, avatarUrl: String): Boolean {
        return updateStringField(userId, "avatar_url", avatarUrl)
    }

    suspend fun updateUserName(userId: String, name: String): Boolean {
        return updateStringField(userId, "name", name)
    }

    private suspend fun updateStringField(userId: String, fieldName: String, value: String): Boolean {
        val key = JSONObject().apply {
            put("user_id", JSONObject().put("S", userId))
        }
        val updates = JSONObject().apply {
            put(":value", JSONObject().put("S", value))
        }
        val body = JSONObject().apply {
            put("TableName", tableName)
            put("Key", key)
            put("UpdateExpression", "SET $fieldName = :value")
            put("ExpressionAttributeValues", updates)
        }
        return executeSignedRequest("UpdateItem", body) != null
    }

    // ==================== AWS SIGNATURE V4 ====================

    private suspend fun executeSignedRequest(action: String, body: JSONObject): JSONObject? {
        return withContext(Dispatchers.IO) {
            var retries = 0
            var lastError: String? = null

            while (retries < 3) {
                try {
                    val timestamp = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }.format(Date())
                    val dateStamp = timestamp.substring(0, 8)

                    val url = URL(documentApiEndpoint)
                    val host = url.host
                    val canonicalUri = url.path
                    val canonicalQueryString = ""
                    val payloadHash = hashSHA256(body.toString())

                    val canonicalHeaders = buildString {
                        append("content-type:application/x-amz-json-1.0\n")
                        append("host:$host\n")
                        append("x-amz-date:$timestamp\n")
                        append("x-amz-target:DynamoDB_20120810.$action\n")
                    }
                    val signedHeaders = "content-type;host;x-amz-date;x-amz-target"

                    val canonicalRequest = buildString {
                        append("POST\n")
                        append(canonicalUri)
                        append("\n")
                        append(canonicalQueryString)
                        append("\n")
                        append(canonicalHeaders)
                        append("\n")
                        append(signedHeaders)
                        append("\n")
                        append(payloadHash)
                    }

                    val credentialScope = "$dateStamp/$region/$service/aws4_request"
                    val stringToSign = buildString {
                        append("AWS4-HMAC-SHA256\n")
                        append("$timestamp\n")
                        append("$credentialScope\n")
                        append(hashSHA256(canonicalRequest))
                    }

                    val signingKey = getSignatureKey(secretKey, dateStamp, region, service)
                    val signature = hmacSHA256(stringToSign, signingKey)

                    val authorizationHeader = "AWS4-HMAC-SHA256 " +
                            "Credential=$accessKey/$credentialScope, " +
                            "SignedHeaders=$signedHeaders, " +
                            "Signature=$signature"

                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "POST"
                    connection.setRequestProperty("Authorization", authorizationHeader)
                    connection.setRequestProperty("Content-Type", "application/x-amz-json-1.0")
                    connection.setRequestProperty("X-Amz-Target", "DynamoDB_20120810.$action")
                    connection.setRequestProperty("X-Amz-Date", timestamp)
                    connection.doOutput = true
                    connection.connectTimeout = 15000
                    connection.readTimeout = 15000

                    connection.outputStream.use {
                        it.write(body.toString().toByteArray())
                    }

                    val responseCode = connection.responseCode
                    println("YDB: $action - HTTP $responseCode")

                    if (responseCode == 200) {
                        val responseText = connection.inputStream.bufferedReader().readText()
                        println("YDB: ✅ $action успешен")
                        return@withContext JSONObject(responseText)
                    } else {
                        val errorText = connection.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
                        lastError = "HTTP $responseCode: $errorText"
                        println("YDB ERROR: $lastError")

                        if (responseCode == 401 || responseCode == 403) {
                            println("YDB: ❌ Ошибка авторизации")
                            return@withContext null
                        }

                        retries++
                        if (retries < 3) {
                            println("YDB: Повтор $retries/3 для $action")
                            delay(1000L * retries)
                            continue
                        }
                    }
                } catch (e: Exception) {
                    lastError = e.message ?: "Unknown exception"
                    println("YDB EXCEPTION: $lastError для $action")
                    retries++
                    if (retries < 3) delay(1000L * retries)
                }
            }
            println("YDB: ❌ Все попытки исчерпаны для $action. Ошибка: $lastError")
            null
        }
    }

    // ==================== КРИПТОГРАФИЯ ====================

    private fun hashSHA256(data: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(data.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    private fun hmacSHA256(data: String, key: ByteArray): String {
        return Mac.getInstance("HmacSHA256").run {
            init(SecretKeySpec(key, "HmacSHA256"))
            doFinal(data.toByteArray()).joinToString("") { "%02x".format(it) }
        }
    }

    private fun getSignatureKey(
        key: String,
        dateStamp: String,
        regionName: String,
        serviceName: String
    ): ByteArray {
        fun hmac(key: ByteArray, data: String): ByteArray {
            return Mac.getInstance("HmacSHA256").run {
                init(SecretKeySpec(key, "HmacSHA256"))
                doFinal(data.toByteArray())
            }
        }
        val kSecret = "AWS4$key".toByteArray()
        val kDate = hmac(kSecret, dateStamp)
        val kRegion = hmac(kDate, regionName)
        val kService = hmac(kRegion, serviceName)
        return hmac(kService, "aws4_request")
    }

    fun hashPassword(password: String): String {
        return hashSHA256(password + salt)
    }

    suspend fun getActiveAnnouncements(): JSONArray? = null
}