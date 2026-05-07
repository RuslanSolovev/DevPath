package com.example.devpath.data.repository

import com.example.devpath.domain.models.Reaction
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

    // Имена таблиц Document API
    private val usersTable = "users_doc"
    private val chatsTable = "chats_doc"
    private val messagesTable = "messages_doc"
    private val friendsTable = "friends_doc"
    private val friendRequestsTable = "friend_requests_doc"

    init {
        if (accessKey.isEmpty() || secretKey.isEmpty()) {
            println("YDB: ⚠️ Ключи доступа не настроены в Config.kt")
        }
        println("YDB: ✅ Инициализирован. Endpoint: $documentApiEndpoint")
    }

    // ==================== ИНИЦИАЛИЗАЦИЯ БАЗЫ ====================

    suspend fun initDatabase(): Boolean {
        return initTable(usersTable, "user_id")
    }

    suspend fun initChatTables(): Boolean {
        val success = listOf(
            initTable(chatsTable, "chat_id"),
            initTable(messagesTable, "message_id"),
            initTable(friendsTable, "friendship_id"),
            initTable("steps_doc", "step_id"),
            initTable("markers_doc", "marker_id"),
            initTable(friendRequestsTable, "request_id"),
            initTable("user_locations_doc", "user_id"),
            initTable("location_settings_doc", "user_id")
        ).all { it }
        println("YDB: Инициализация чат-таблиц: ${if (success) "✅" else "❌"}")
        return success
    }

    suspend fun initTable(tableName: String, keyName: String): Boolean {
        return try {
            val describeBody = JSONObject().apply { put("TableName", tableName) }
            val existing = executeSignedRequest("DescribeTable", describeBody)
            if (existing != null) {
                println("YDB: ✅ Таблица $tableName уже существует")
                return true
            }
            createTable(tableName, keyName)
        } catch (e: Exception) {
            createTable(tableName, keyName)
        }
    }

    private suspend fun createTable(tableName: String, keyName: String): Boolean {
        val body = JSONObject().apply {
            put("TableName", tableName)
            put("KeySchema", JSONArray().apply {
                put(JSONObject().apply {
                    put("AttributeName", keyName)
                    put("KeyType", "HASH")
                })
            })
            put("AttributeDefinitions", JSONArray().apply {
                put(JSONObject().apply {
                    put("AttributeName", keyName)
                    put("AttributeType", "S")
                })
            })
        }
        val result = executeSignedRequest("CreateTable", body)
        if (result != null) {
            println("YDB: ✅ Таблица $tableName создана")
            return true
        }
        return false
    }

    suspend fun forwardMessage(
        originalMessage: JSONObject,
        targetChatId: String,
        senderId: String,
        senderName: String
    ): Boolean {
        val newMessageId = UUID.randomUUID().toString()
        val text = originalMessage.optJSONObject("text")?.optString("S") ?: ""
        val imageUrl = originalMessage.optJSONObject("image_url")?.optString("S") ?: ""

        return sendMessage(
            messageId = newMessageId,
            chatId = targetChatId,
            senderId = senderId,
            senderName = senderName,
            text = if (text.isNotEmpty()) "📎 Пересланное: $text" else "",
            imageUrl = imageUrl
        )
    }

    suspend fun getNewMessages(chatId: String, lastTimestamp: Long): List<JSONObject> {
        val allMessages = mutableListOf<JSONObject>()
        var lastEvaluatedKey: JSONObject? = null

        do {
            val body = JSONObject().apply {
                put("TableName", messagesTable)
                put("FilterExpression", "chat_id = :chatId")
                put("ExpressionAttributeValues", JSONObject().apply {
                    put(":chatId", JSONObject().put("S", chatId))
                })
                put("Limit", 100)
                if (lastEvaluatedKey != null) {
                    put("ExclusiveStartKey", lastEvaluatedKey)
                }
            }
            val result = executeSignedRequest("Scan", body)
            val items = result?.optJSONArray("Items") ?: JSONArray()
            for (i in 0 until items.length()) {
                allMessages.add(items.getJSONObject(i))
            }
            lastEvaluatedKey = result?.optJSONObject("LastEvaluatedKey")
        } while (lastEvaluatedKey != null && lastEvaluatedKey.length() > 0)

        return allMessages.filter { json ->
            val ts = json.optJSONObject("timestamp")?.optString("S")?.toLongOrNull() ?: 0L
            ts > lastTimestamp
        }
    }

    // ==================== ПОЛЬЗОВАТЕЛИ ====================

    suspend fun createUser(userId: String, name: String, email: String, password: String, avatarUrl: String = ""): Boolean {
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
            put("TableName", usersTable)
            put("Item", item)
        }
        return executeSignedRequest("PutItem", body) != null
    }

    suspend fun getUser(userId: String): JSONObject? {
        val key = JSONObject().apply { put("user_id", JSONObject().put("S", userId)) }
        val body = JSONObject().apply {
            put("TableName", usersTable)
            put("Key", key)
        }
        return executeSignedRequest("GetItem", body)?.optJSONObject("Item")
    }

    suspend fun findUserByEmail(email: String): JSONObject? {
        val body = JSONObject().apply {
            put("TableName", usersTable)
            put("FilterExpression", "#email = :email")
            put("ExpressionAttributeNames", JSONObject().apply { put("#email", "email") })
            put("ExpressionAttributeValues", JSONObject().apply {
                put(":email", JSONObject().put("S", email))
            })
        }
        val result = executeSignedRequest("Scan", body)
        val items = result?.optJSONArray("Items")
        return if (items != null && items.length() > 0) items.getJSONObject(0) else null
    }

    suspend fun searchUsers(query: String): List<JSONObject> {
        val lowerQuery = query.lowercase().trim()
        println("DEBUG: YDB.searchUsers - query='$lowerQuery'")

        // ✅ Сначала ищем по началу имени
        val body1 = JSONObject().apply {
            put("TableName", usersTable)
            put("FilterExpression", "begins_with(#name, :query) OR begins_with(email, :query)")
            put("ExpressionAttributeNames", JSONObject().apply { put("#name", "name") })
            put("ExpressionAttributeValues", JSONObject().apply {
                put(":query", JSONObject().put("S", lowerQuery))
            })
            put("Limit", 20)
        }

        val result = executeSignedRequest("Scan", body1)
        val items = result?.optJSONArray("Items") ?: JSONArray()

        // Если не нашли — пробуем contains с lower case
        if (items.length() == 0) {
            println("DEBUG: YDB.searchUsers - begins_with не нашел, пробуем полный скан...")
            // Сканируем всех и фильтруем в коде
            val allBody = JSONObject().apply {
                put("TableName", usersTable)
                put("Limit", 100)
            }
            val allResult = executeSignedRequest("Scan", allBody)
            val allItems = allResult?.optJSONArray("Items") ?: JSONArray()

            val filtered = mutableListOf<JSONObject>()
            for (i in 0 until allItems.length()) {
                val user = allItems.getJSONObject(i)
                val name = user.optJSONObject("name")?.optString("S")?.lowercase() ?: ""
                val email = user.optJSONObject("email")?.optString("S")?.lowercase() ?: ""
                if (name.contains(lowerQuery) || email.contains(lowerQuery)) {
                    filtered.add(user)
                }
            }
            println("DEBUG: YDB.searchUsers - полный скан нашёл ${filtered.size}")
            return filtered
        }

        println("DEBUG: YDB.searchUsers - begins_with нашёл ${items.length()}")
        return (0 until items.length()).map { items.getJSONObject(it) }
    }

    suspend fun authenticateUser(email: String, password: String): String? {
        val user = findUserByEmail(email) ?: return null
        val storedHash = user.optJSONObject("password_hash")?.optString("S") ?: return null
        val inputHash = hashPassword(password)
        return if (storedHash == inputHash) user.optJSONObject("user_id")?.optString("S") else null
    }

    suspend fun updateLastSeen(userId: String): Boolean {
        return updateStringField(usersTable, "user_id", userId, "last_seen", System.currentTimeMillis().toString())
    }

    suspend fun updateUserAvatar(userId: String, avatarUrl: String): Boolean {
        return updateStringField(usersTable, "user_id", userId, "avatar_url", avatarUrl)
    }

    suspend fun updateUserName(userId: String, name: String): Boolean {
        return updateStringField(usersTable, "user_id", userId, "name", name)
    }

    // ==================== ЧАТЫ ====================

    suspend fun createChat(chatId: String, type: String, participants: List<String>, name: String, createdBy: String): Boolean {
        val participantsArray = JSONArray()
        participants.forEach { participantsArray.put(it) }

        val now = System.currentTimeMillis().toString()
        val item = JSONObject().apply {
            put("chat_id", JSONObject().put("S", chatId))
            put("type", JSONObject().put("S", type))
            put("participants", JSONObject().put("SS", participantsArray))
            put("name", JSONObject().put("S", name))
            put("created_by", JSONObject().put("S", createdBy))
            put("created_at", JSONObject().put("S", now))
            put("last_message", JSONObject().put("S", ""))
            put("last_message_time", JSONObject().put("S", now))
            put("last_message_sender", JSONObject().put("S", ""))
        }
        val body = JSONObject().apply {
            put("TableName", chatsTable)
            put("Item", item)
        }
        return executeSignedRequest("PutItem", body) != null
    }

    suspend fun getChat(chatId: String): JSONObject? {
        val key = JSONObject().apply { put("chat_id", JSONObject().put("S", chatId)) }
        val body = JSONObject().apply {
            put("TableName", chatsTable)
            put("Key", key)
        }
        return executeSignedRequest("GetItem", body)?.optJSONObject("Item")
    }

    suspend fun getUserChats(userId: String): List<JSONObject> {
        val body = JSONObject().apply {
            put("TableName", chatsTable)
            put("FilterExpression", "contains(participants, :userId)")
            put("ExpressionAttributeValues", JSONObject().apply {
                put(":userId", JSONObject().put("S", userId))
            })
        }
        val result = executeSignedRequest("Scan", body)
        val items = result?.optJSONArray("Items") ?: JSONArray()
        return (0 until items.length()).map { items.getJSONObject(it) }
    }

    suspend fun updateChatLastMessage(chatId: String, message: String, senderName: String): Boolean {
        val key = JSONObject().apply { put("chat_id", JSONObject().put("S", chatId)) }
        val body = JSONObject().apply {
            put("TableName", chatsTable)
            put("Key", key)
            put("UpdateExpression", "SET last_message = :msg, last_message_time = :time, last_message_sender = :sender")
            put("ExpressionAttributeValues", JSONObject().apply {
                put(":msg", JSONObject().put("S", message.take(100)))
                put(":time", JSONObject().put("S", System.currentTimeMillis().toString()))
                put(":sender", JSONObject().put("S", senderName.take(50)))
            })
        }
        return executeSignedRequest("UpdateItem", body) != null
    }

    suspend fun deleteChat(chatId: String): Boolean {
        // Получаем все сообщения чата с пагинацией
        val messages = getChatMessages(chatId)

        // Физически удаляем каждое сообщение
        messages.forEach { message ->
            val messageId = message.optJSONObject("message_id")?.optString("S") ?: return@forEach
            deleteMessagePermanently(messageId)
        }

        // Удаляем сам чат
        val key = JSONObject().apply { put("chat_id", JSONObject().put("S", chatId)) }
        val body = JSONObject().apply {
            put("TableName", chatsTable)
            put("Key", key)
        }
        return executeSignedRequest("DeleteItem", body) != null
    }

    // ==================== СООБЩЕНИЯ ====================

    suspend fun sendMessage(
        messageId: String, chatId: String, senderId: String, senderName: String,
        text: String, imageUrl: String = "", replyToId: String = "",
        replyToText: String = "", replyToSenderName: String = ""
    ): Boolean {
        val now = System.currentTimeMillis().toString()
        val item = JSONObject().apply {
            put("message_id", JSONObject().put("S", messageId))
            put("chat_id", JSONObject().put("S", chatId))
            put("sender_id", JSONObject().put("S", senderId))
            put("sender_name", JSONObject().put("S", senderName))
            put("text", JSONObject().put("S", text))
            put("image_url", JSONObject().put("S", imageUrl))
            put("timestamp", JSONObject().put("S", now))
            put("reply_to_id", JSONObject().put("S", replyToId))
            put("reactions", JSONObject().put("L", JSONArray()))
            put("reply_to_text", JSONObject().put("S", replyToText))
            put("reply_to_sender_name", JSONObject().put("S", replyToSenderName))
            put("read_by", JSONObject().put("SS", JSONArray()))
            put("delivered_to", JSONObject().put("SS", JSONArray()))
            put("edited", JSONObject().put("BOOL", false))
            put("deleted", JSONObject().put("BOOL", false))
        }
        val body = JSONObject().apply {
            put("TableName", messagesTable)
            put("Item", item)
        }

        println("DEBUG: sendMessage - BODY: $body")  // ← Добавить
        val result = executeSignedRequest("PutItem", body)
        println("DEBUG: sendMessage - RESULT: $result")  // ← Добавить
        return result != null
    }

    suspend fun getChatMessages(chatId: String, limit: Int = 100): List<JSONObject> {
        val allMessages = mutableListOf<JSONObject>()
        var lastEvaluatedKey: JSONObject? = null

        do {
            val body = JSONObject().apply {
                put("TableName", messagesTable)
                put("FilterExpression", "chat_id = :chatId")
                put("ExpressionAttributeValues", JSONObject().apply {
                    put(":chatId", JSONObject().put("S", chatId))
                })
                put("Limit", limit)
                if (lastEvaluatedKey != null) {
                    put("ExclusiveStartKey", lastEvaluatedKey)
                }
            }
            val result = executeSignedRequest("Scan", body)
            val items = result?.optJSONArray("Items") ?: JSONArray()
            for (i in 0 until items.length()) {
                allMessages.add(items.getJSONObject(i))
            }
            lastEvaluatedKey = result?.optJSONObject("LastEvaluatedKey")
        } while (lastEvaluatedKey != null && lastEvaluatedKey.length() > 0)

        return allMessages.sortedBy { it.optJSONObject("timestamp")?.optString("S")?.toLongOrNull() ?: 0L }
    }

    suspend fun markMessageDelivered(messageId: String, userId: String): Boolean {
        val key = JSONObject().apply { put("message_id", JSONObject().put("S", messageId)) }
        val body = JSONObject().apply {
            put("TableName", messagesTable)
            put("Key", key)
            put("UpdateExpression", "ADD delivered_to :userId")
            put("ExpressionAttributeValues", JSONObject().apply {
                put(":userId", JSONObject().put("SS", JSONArray(listOf(userId))))
            })
        }
        return executeSignedRequest("UpdateItem", body) != null
    }

    suspend fun updateMessageReactions(messageId: String, reactions: List<Reaction>): Boolean {
        val key = JSONObject().apply { put("message_id", JSONObject().put("S", messageId)) }
        val reactionsArray = JSONArray()
        reactions.forEach { reaction ->
            reactionsArray.put(JSONObject().apply {
                put("M", JSONObject().apply {
                    put("user_id", JSONObject().put("S", reaction.userId))
                    put("reaction", JSONObject().put("S", reaction.reaction))
                })
            })
        }
        val body = JSONObject().apply {
            put("TableName", messagesTable)
            put("Key", key)
            put("UpdateExpression", "SET reactions = :reactions")  // ✅ БЕЗ обновления timestamp
            put("ExpressionAttributeValues", JSONObject().apply {
                put(":reactions", JSONObject().put("L", reactionsArray))
            })
        }
        return executeSignedRequest("UpdateItem", body) != null
    }

    suspend fun markMessageRead(messageId: String, userId: String): Boolean {
        val key = JSONObject().apply { put("message_id", JSONObject().put("S", messageId)) }
        val body = JSONObject().apply {
            put("TableName", messagesTable)
            put("Key", key)
            put("UpdateExpression", "ADD read_by :userId")
            put("ExpressionAttributeValues", JSONObject().apply {
                put(":userId", JSONObject().put("SS", JSONArray(listOf(userId))))
            })
        }
        return executeSignedRequest("UpdateItem", body) != null
    }

    suspend fun editMessage(messageId: String, newText: String): Boolean {
        val key = JSONObject().apply { put("message_id", JSONObject().put("S", messageId)) }
        val body = JSONObject().apply {
            put("TableName", messagesTable)
            put("Key", key)
            put("UpdateExpression", "SET #text = :text, edited = :true, edited_at = :time, #ts = :ts")
            put("ExpressionAttributeNames", JSONObject().apply {
                put("#text", "text")
                put("#ts", "timestamp")  // ✅ Обновляем timestamp
            })
            put("ExpressionAttributeValues", JSONObject().apply {
                put(":text", JSONObject().put("S", newText))
                put(":true", JSONObject().put("BOOL", true))
                put(":time", JSONObject().put("S", System.currentTimeMillis().toString()))
                put(":ts", JSONObject().put("S", System.currentTimeMillis().toString()))
            })
        }
        return executeSignedRequest("UpdateItem", body) != null
    }

    suspend fun deleteMessagePermanently(messageId: String): Boolean {
        val key = JSONObject().apply { put("message_id", JSONObject().put("S", messageId)) }
        val body = JSONObject().apply {
            put("TableName", messagesTable)
            put("Key", key)
        }
        return executeSignedRequest("DeleteItem", body) != null
    }

    suspend fun deleteMessage(messageId: String): Boolean {
        val key = JSONObject().apply { put("message_id", JSONObject().put("S", messageId)) }
        val body = JSONObject().apply {
            put("TableName", messagesTable)
            put("Key", key)
            put("UpdateExpression", "SET deleted = :true, #text = :empty, #ts = :ts")  // ✅ Оставляем timestamp
            put("ExpressionAttributeNames", JSONObject().apply {
                put("#text", "text")
                put("#ts", "timestamp")
            })
            put("ExpressionAttributeValues", JSONObject().apply {
                put(":true", JSONObject().put("BOOL", true))
                put(":empty", JSONObject().put("S", ""))
                put(":ts", JSONObject().put("S", System.currentTimeMillis().toString()))
            })
        }
        return executeSignedRequest("UpdateItem", body) != null
    }

    // ==================== ДРУЗЬЯ ====================

    suspend fun addFriend(userId: String, friendId: String): Boolean {
        val friendshipId = "$userId`_`$friendId"
        val item = JSONObject().apply {
            put("friendship_id", JSONObject().put("S", friendshipId))
            put("user_id", JSONObject().put("S", userId))
            put("friend_id", JSONObject().put("S", friendId))
            put("created_at", JSONObject().put("S", System.currentTimeMillis().toString()))
        }
        val body = JSONObject().apply {
            put("TableName", friendsTable)
            put("Item", item)
        }
        return executeSignedRequest("PutItem", body) != null
    }

    suspend fun removeFriend(userId: String, friendId: String): Boolean {
        val friendshipId = "$userId`_`$friendId"
        val key = JSONObject().apply { put("friendship_id", JSONObject().put("S", friendshipId)) }
        val body = JSONObject().apply {
            put("TableName", friendsTable)
            put("Key", key)
        }
        return executeSignedRequest("DeleteItem", body) != null
    }

    suspend fun getUserFriends(userId: String): List<String> {
        val body = JSONObject().apply {
            put("TableName", friendsTable)
            put("FilterExpression", "user_id = :userId")
            put("ExpressionAttributeValues", JSONObject().apply {
                put(":userId", JSONObject().put("S", userId))
            })
        }
        val result = executeSignedRequest("Scan", body)
        val items = result?.optJSONArray("Items") ?: JSONArray()
        return (0 until items.length()).map {
                it -> items.getJSONObject(it).optJSONObject("friend_id")?.optString("S") ?: ""
        }.filter { it.isNotEmpty() }
    }

    // ==================== ЗАЯВКИ В ДРУЗЬЯ ====================

    suspend fun sendFriendRequest(fromUserId: String, toUserId: String): Boolean {
        val requestId = UUID.randomUUID().toString()
        val item = JSONObject().apply {
            put("request_id", JSONObject().put("S", requestId))
            put("from_user_id", JSONObject().put("S", fromUserId))
            put("to_user_id", JSONObject().put("S", toUserId))
            put("status", JSONObject().put("S", "pending"))
            put("created_at", JSONObject().put("S", System.currentTimeMillis().toString()))
        }
        val body = JSONObject().apply {
            put("TableName", friendRequestsTable)
            put("Item", item)
        }
        return executeSignedRequest("PutItem", body) != null
    }

    suspend fun getIncomingRequests(userId: String): List<JSONObject> {
        val body = JSONObject().apply {
            put("TableName", friendRequestsTable)
            put("FilterExpression", "to_user_id = :userId AND #status = :status")
            put("ExpressionAttributeNames", JSONObject().apply { put("#status", "status") })
            put("ExpressionAttributeValues", JSONObject().apply {
                put(":userId", JSONObject().put("S", userId))
                put(":status", JSONObject().put("S", "pending"))
            })
        }
        val result = executeSignedRequest("Scan", body)
        val items = result?.optJSONArray("Items") ?: JSONArray()
        return (0 until items.length()).map { items.getJSONObject(it) }
    }

    suspend fun getSentRequests(userId: String): List<JSONObject> {
        val body = JSONObject().apply {
            put("TableName", friendRequestsTable)
            put("FilterExpression", "from_user_id = :userId")
            put("ExpressionAttributeValues", JSONObject().apply {
                put(":userId", JSONObject().put("S", userId))
            })
        }
        val result = executeSignedRequest("Scan", body)
        val items = result?.optJSONArray("Items") ?: JSONArray()
        return (0 until items.length()).map { items.getJSONObject(it) }
    }



    suspend fun acceptFriendRequest(requestId: String): Boolean {
        val key = JSONObject().apply { put("request_id", JSONObject().put("S", requestId)) }
        val body = JSONObject().apply {
            put("TableName", friendRequestsTable)
            put("Key", key)
            put("UpdateExpression", "SET #status = :accepted")
            put("ExpressionAttributeNames", JSONObject().apply { put("#status", "status") })
            put("ExpressionAttributeValues", JSONObject().apply {
                put(":accepted", JSONObject().put("S", "accepted"))
            })
        }
        return executeSignedRequest("UpdateItem", body) != null
    }

    suspend fun rejectFriendRequest(requestId: String): Boolean {
        val key = JSONObject().apply { put("request_id", JSONObject().put("S", requestId)) }
        val body = JSONObject().apply {
            put("TableName", friendRequestsTable)
            put("Key", key)
            put("UpdateExpression", "SET #status = :rejected")
            put("ExpressionAttributeNames", JSONObject().apply { put("#status", "status") })
            put("ExpressionAttributeValues", JSONObject().apply {
                put(":rejected", JSONObject().put("S", "rejected"))
            })
        }
        return executeSignedRequest("UpdateItem", body) != null
    }

    // ==================== УНИВЕРСАЛЬНЫЕ МЕТОДЫ ====================

    private suspend fun updateStringField(tableName: String, keyField: String, keyValue: String, updateField: String, newValue: String): Boolean {
        val key = JSONObject().apply { put(keyField, JSONObject().put("S", keyValue)) }
        val body = JSONObject().apply {
            put("TableName", tableName)
            put("Key", key)
            put("UpdateExpression", "SET $updateField = :value")
            put("ExpressionAttributeValues", JSONObject().apply {
                put(":value", JSONObject().put("S", newValue))
            })
        }
        return executeSignedRequest("UpdateItem", body) != null
    }

    // ==================== AWS SIGNATURE V4 ====================

    suspend fun executeSignedRequest(action: String, body: JSONObject): JSONObject? {
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
                        append("\n\n")
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

                    connection.outputStream.use { it.write(body.toString().toByteArray()) }

                    val responseCode = connection.responseCode

                    if (responseCode == 200) {
                        val responseText = connection.inputStream.bufferedReader().readText()
                        return@withContext JSONObject(responseText)
                    } else {
                        val errorText = connection.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
                        lastError = "HTTP $responseCode: $errorText"

                        if (responseCode == 401 || responseCode == 403) {
                            return@withContext null
                        }

                        retries++
                        if (retries < 3) {
                            delay(1000L * retries)
                        }
                    }
                } catch (e: Exception) {
                    lastError = e.message
                    retries++
                    if (retries < 3) delay(1000L * retries)
                }
            }
            null
        }
    }

    suspend fun searchMessages(chatId: String, query: String): List<JSONObject> {
        val body = JSONObject().apply {
            put("TableName", messagesTable)
            put("FilterExpression", "chat_id = :chatId AND contains(#text, :query)")
            put("ExpressionAttributeNames", JSONObject().apply { put("#text", "text") })
            put("ExpressionAttributeValues", JSONObject().apply {
                put(":chatId", JSONObject().put("S", chatId))
                put(":query", JSONObject().put("S", query.lowercase()))
            })
            put("Limit", 50)
        }
        val result = executeSignedRequest("Scan", body)
        val items = result?.optJSONArray("Items") ?: JSONArray()
        return (0 until items.length()).map { items.getJSONObject(it) }
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

    suspend fun getRecentMessages(chatId: String, limit: Int = 200): List<JSONObject> {
        val body = JSONObject().apply {
            put("TableName", messagesTable)
            put("FilterExpression", "chat_id = :chatId")
            put("ExpressionAttributeValues", JSONObject().apply {
                put(":chatId", JSONObject().put("S", chatId))
            })
            put("Limit", limit)
            put("ScanIndexForward", false) // чтобы сначала шли новые
        }
        val result = executeSignedRequest("Scan", body)
        val items = result?.optJSONArray("Items") ?: JSONArray()
        return (0 until items.length()).map { items.getJSONObject(it) }
            .sortedBy { it.optJSONObject("timestamp")?.optString("S")?.toLongOrNull() ?: 0L }
    }

    suspend fun forwardMessageWithImage(
        originalMessage: JSONObject,
        targetChatId: String,
        senderId: String,
        senderName: String
    ): Boolean {
        val newMessageId = UUID.randomUUID().toString()
        val text = originalMessage.optJSONObject("text")?.optString("S") ?: ""
        val imageUrl = originalMessage.optJSONObject("image_url")?.optString("S") ?: ""

        val forwardedText = when {
            text.isNotEmpty() && imageUrl.isNotEmpty() -> "📎 Пересланное: $text\n📷 Изображение"
            imageUrl.isNotEmpty() -> "📎 Пересланное: 📷 Изображение"
            else -> "📎 Пересланное: $text"
        }

        return sendMessage(
            messageId = newMessageId,
            chatId = targetChatId,
            senderId = senderId,
            senderName = senderName,
            text = forwardedText,
            imageUrl = imageUrl
        )
    }

    private fun getSignatureKey(key: String, dateStamp: String, regionName: String, serviceName: String): ByteArray {
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