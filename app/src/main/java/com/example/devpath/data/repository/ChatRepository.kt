package com.example.devpath.data.repository

import android.content.ContentResolver
import android.net.Uri
import com.example.devpath.data.storage.YandexStorageClient
import com.example.devpath.domain.models.*
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val yandexStorageClient: YandexStorageClient
) {
    private val db: FirebaseFirestore = Firebase.firestore

    fun getFriends(userId: String): Flow<List<UserProfile>> = callbackFlow {
        val friendIds = mutableSetOf<String>()

        val query1 = db.collection("friendships").whereEqualTo("userId1", userId)
        val query2 = db.collection("friendships").whereEqualTo("userId2", userId)

        var query1Completed = false
        var query2Completed = false

        fun checkAndSend() {

            if (query1Completed && query2Completed) {
                if (friendIds.isEmpty()) {
                    trySend(emptyList())
                    return
                }
                db.collection("users")
                    .whereIn("userId", friendIds.toList())
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val friends = snapshot.documents.mapNotNull { it.toObject(UserProfile::class.java) }
                        trySend(friends)
                    }
                    .addOnFailureListener { error ->
                        close(error)
                    }
            }
        }

        val subscription1 = query1.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            snapshot?.documents?.forEach { doc ->
                doc.toObject(Friendship::class.java)?.userId2?.let { friendIds.add(it) }
            }
            query1Completed = true
            checkAndSend()
        }

        val subscription2 = query2.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            snapshot?.documents?.forEach { doc ->
                doc.toObject(Friendship::class.java)?.userId1?.let { friendIds.add(it) }
            }
            query2Completed = true
            checkAndSend()
        }

        awaitClose {
            subscription1.remove()
            subscription2.remove()
        }
    }

    // Добавьте этот метод в ChatRepository
    suspend fun sendImageMessageWithText(
        chatId: String,
        senderId: String,
        imageUrl: String,
        text: String,
        replyToId: String = "",
        replyToText: String = "",
        replyToSenderName: String = ""
    ): Boolean {
        return try {
            updateUserLastActive(senderId)
            val sender = getUser(senderId)
            val senderName = sender?.name ?: "Пользователь"

            val message = Message(
                chatId = chatId,
                senderId = senderId,
                senderName = senderName,
                text = text,  // ← текст и фото вместе
                imageUrl = imageUrl,
                timestamp = com.google.firebase.Timestamp.now(),
                readBy = emptyList(),
                deliveredTo = listOf(senderId),
                replyToId = replyToId,
                replyToText = replyToText,
                replyToSenderName = replyToSenderName
            )
            db.collection("messages").add(message).await()

            db.collection("chats").document(chatId)
                .update(
                    mapOf(
                        "lastMessage" to if (text.isNotEmpty()) text else "📷 Изображение",
                        "lastMessageSender" to senderName,
                        "lastMessageTime" to com.google.firebase.Timestamp.now()
                    )
                )
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    suspend fun addReaction(messageId: String, userId: String, reaction: String): Boolean {
        return try {
            val newReaction = Reaction(
                userId = userId,
                reaction = reaction,
                timestamp = Timestamp.now()  // ← теперь работает
            )

            val messageRef = db.collection("messages").document(messageId)

            // Получаем текущие реакции
            val snapshot = messageRef.get().await()
            val currentReactions = snapshot.get("reactions") as? List<Map<String, Any>> ?: emptyList()

            // Удаляем старую реакцию пользователя, если есть
            val updatedReactions = currentReactions.filterNot {
                (it["userId"] as? String) == userId
            }.map { it.toMutableMap() } + mapOf(
                "userId" to userId,
                "reaction" to reaction,
                "timestamp" to Timestamp.now()
            )

            messageRef.update("reactions", updatedReactions).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun removeReaction(messageId: String, userId: String): Boolean {
        return try {
            val snapshot = db.collection("messages").document(messageId).get().await()
            val currentReactions = snapshot.get("reactions") as? List<Map<String, Any>> ?: emptyList()
            val updatedReactions = currentReactions.filterNot {
                (it["userId"] as? String) == userId
            }

            db.collection("messages").document(messageId).update("reactions", updatedReactions).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Пересылка сообщений с возвратом результата
    suspend fun forwardMessage(
        originalMessage: Message,
        targetChatId: String,
        senderId: String,
        senderName: String
    ): Boolean {
        return try {
            val forwardedMessage = Message(
                chatId = targetChatId,
                senderId = senderId,
                senderName = senderName,
                text = originalMessage.text,
                imageUrl = originalMessage.imageUrl,
                timestamp = Timestamp.now(),
                readBy = emptyList(),
                deliveredTo = listOf(senderId),
                isForwarded = true,
                forwardedFrom = originalMessage.messageId,
                forwardedFromChatId = originalMessage.chatId
            )

            db.collection("messages").add(forwardedMessage).await()

            // Получаем название чата для отображения в уведомлении
            val chat = getChat(targetChatId)
            val chatDisplayName = when {
                chat?.type == "personal" -> {
                    val otherUserId = chat.participants.firstOrNull { it != senderId }
                    if (otherUserId != null) {
                        val user = getUser(otherUserId)
                        user?.name ?: "Пользователь"
                    } else {
                        "Личный чат"
                    }
                }
                else -> chat?.name ?: "Чат"
            }

            // Обновляем последнее сообщение в чате
            db.collection("chats").document(targetChatId)
                .update(
                    mapOf(
                        "lastMessage" to if (originalMessage.text.isNotEmpty())
                            "📎 Пересланное: ${originalMessage.text.take(50)}"
                        else "📎 Пересланное изображение",
                        "lastMessageSender" to senderName,
                        "lastMessageTime" to Timestamp.now()
                    )
                )
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    // Более эффективный запрос (после создания индекса)
    suspend fun searchMessages(
        chatId: String,
        query: String,
        senderId: String? = null,
        startDate: Timestamp? = null,
        endDate: Timestamp? = null
    ): List<Message> {
        return try {
            var firestoreQuery = db.collection("messages")
                .whereEqualTo("chatId", chatId)
                .whereEqualTo("deleted", false)

            // Поиск по тексту
            if (query.isNotEmpty()) {
                firestoreQuery = firestoreQuery
                    .whereGreaterThanOrEqualTo("text", query)
                    .whereLessThanOrEqualTo("text", query + "\uf8ff")
            }

            // Применяем остальные фильтры
            if (!senderId.isNullOrEmpty()) {
                firestoreQuery = firestoreQuery.whereEqualTo("senderId", senderId)
            }

            startDate?.let {
                firestoreQuery = firestoreQuery.whereGreaterThanOrEqualTo("timestamp", it)
            }
            endDate?.let {
                firestoreQuery = firestoreQuery.whereLessThanOrEqualTo("timestamp", it)
            }

            val snapshot = firestoreQuery
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Message::class.java)?.copy(messageId = doc.id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }



    fun getIncomingRequests(userId: String): Flow<List<FriendRequest>> = callbackFlow {
        val query = db.collection("friend_requests")
            .whereEqualTo("toUserId", userId)
            .whereEqualTo("status", "pending")

        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val requests = snapshot?.documents?.mapNotNull { doc ->
                val request = doc.toObject(FriendRequest::class.java)
                request?.copy(requestId = doc.id)
            } ?: emptyList()
            trySend(requests)
        }

        awaitClose { subscription.remove() }
    }

    fun getSentRequests(userId: String): Flow<List<FriendRequest>> = callbackFlow {
        val query = db.collection("friend_requests")
            .whereEqualTo("fromUserId", userId)
            .whereIn("status", listOf("pending", "accepted"))

        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val requests = snapshot?.documents?.mapNotNull { doc ->
                val request = doc.toObject(FriendRequest::class.java)
                request?.copy(requestId = doc.id)
            } ?: emptyList()
            trySend(requests)
        }

        awaitClose { subscription.remove() }
    }

    suspend fun getMessagesCount(chatId: String): Long {
        return try {
            val query = db.collection("messages").whereEqualTo("chatId", chatId)
            val aggregateQuery = query.count()
            val snapshot = aggregateQuery.get(AggregateSource.SERVER).await()
            snapshot.count
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    suspend fun sendFriendRequest(fromUserId: String, toUserId: String): Boolean {
        return try {
            val existingRequest = db.collection("friend_requests")
                .whereEqualTo("fromUserId", fromUserId)
                .whereEqualTo("toUserId", toUserId)
                .whereIn("status", listOf("pending", "accepted"))
                .get()
                .await()

            if (existingRequest.isEmpty) {
                val existingFriendship = db.collection("friendships")
                    .whereEqualTo("userId1", fromUserId)
                    .whereEqualTo("userId2", toUserId)
                    .get()
                    .await()

                val existingFriendship2 = db.collection("friendships")
                    .whereEqualTo("userId1", toUserId)
                    .whereEqualTo("userId2", fromUserId)
                    .get()
                    .await()

                if (existingFriendship.isEmpty && existingFriendship2.isEmpty) {
                    val request = FriendRequest(
                        fromUserId = fromUserId,
                        toUserId = toUserId,
                        status = "pending"
                    )
                    db.collection("friend_requests").add(request).await()
                    true
                } else {
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun acceptFriendRequest(requestId: String, fromUserId: String, toUserId: String): Boolean {
        return try {
            if (requestId.isBlank()) {
                println("DEBUG: acceptFriendRequest: requestId is blank!")
                return false
            }

            db.collection("friend_requests").document(requestId)
                .update("status", "accepted")
                .await()

            val friendship = Friendship(
                userId1 = fromUserId,
                userId2 = toUserId
            )
            db.collection("friendships").add(friendship).await()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun rejectFriendRequest(requestId: String): Boolean {
        return try {
            if (requestId.isBlank()) return false
            db.collection("friend_requests").document(requestId)
                .update("status", "rejected")
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteChat(chatId: String): Boolean {
        return try {
            val messagesQuery = db.collection("messages")
                .whereEqualTo("chatId", chatId)
                .get()
                .await()

            messagesQuery.documents.forEach { doc ->
                doc.reference.delete().await()
            }

            db.collection("chats").document(chatId).delete().await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun removeFriend(userId: String, friendId: String): Boolean {
        return try {
            val query = db.collection("friendships")
                .whereEqualTo("userId1", userId)
                .whereEqualTo("userId2", friendId)
                .get()
                .await()

            if (query.isEmpty) {
                val query2 = db.collection("friendships")
                    .whereEqualTo("userId1", friendId)
                    .whereEqualTo("userId2", userId)
                    .get()
                    .await()
                query2.documents.forEach { it.reference.delete().await() }
            } else {
                query.documents.forEach { it.reference.delete().await() }
            }

            val requests = db.collection("friend_requests")
                .whereEqualTo("fromUserId", userId)
                .whereEqualTo("toUserId", friendId)
                .get()
                .await()
            requests.documents.forEach { it.reference.delete().await() }

            val requests2 = db.collection("friend_requests")
                .whereEqualTo("fromUserId", friendId)
                .whereEqualTo("toUserId", userId)
                .get()
                .await()
            requests2.documents.forEach { it.reference.delete().await() }

            deletePersonalChat(userId, friendId)

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateUserLastActive(userId: String) {
        try {
            db.collection("users").document(userId)
                .update(
                    mapOf(
                        "lastActiveInApp" to com.google.firebase.Timestamp.now(),
                        "lastSeen" to com.google.firebase.Timestamp.now()
                    )
                )
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getUserLastActiveFormatted(userId: String): String {
        return try {
            println("DEBUG: getUserLastActiveFormatted - запрос для userId=$userId")
            val doc = db.collection("users").document(userId).get().await()
            val lastActive = doc.getTimestamp("lastActiveInApp") ?: doc.getTimestamp("lastSeen")
            println("DEBUG: getUserLastActiveFormatted - timestamp=$lastActive")
            if (lastActive != null) {
                val formatted = formatLastActive(lastActive.toDate())
                println("DEBUG: getUserLastActiveFormatted - formatted='$formatted'")
                formatted
            } else {
                println("DEBUG: getUserLastActiveFormatted - timestamp null")
                "недавно"
            }
        } catch (e: Exception) {
            println("DEBUG: getUserLastActiveFormatted - ошибка: ${e.message}")
            "недавно"
        }
    }

    private fun formatLastActive(date: Date): String {
        val now = Date()
        val diff = now.time - date.time

        return when {
            diff < 60_000 -> "Только что"
            diff < 3_600_000 -> "${diff / 60_000} мин. назад"
            diff < 86_400_000 -> "${diff / 3_600_000} ч. назад"
            diff < 604_800_000 -> "${diff / 86_400_000} д. назад"
            else -> {
                val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                formatter.format(date)
            }
        }
    }

    suspend fun createPersonalChat(userId1: String, userId2: String): String? {
        return try {
            val chat = Chat(
                type = "personal",
                participants = listOf(userId1, userId2),
                createdAt = com.google.firebase.Timestamp.now()
            )
            val docRef = db.collection("chats").add(chat).await()
            docRef.id
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun createOrGetChat(userId1: String, userId2: String): Chat? {
        return try {
            val query = db.collection("chats")
                .whereEqualTo("type", "personal")
                .whereArrayContains("participants", userId1)

            val snapshot = query.get().await()

            val existingChat = snapshot.documents.firstOrNull { doc ->
                val participants = doc.get("participants") as? List<String> ?: emptyList()
                participants.contains(userId2)
            }

            if (existingChat != null) {
                return existingChat.toObject(Chat::class.java)?.copy(chatId = existingChat.id)
            }

            val chat = Chat(
                type = "personal",
                participants = listOf(userId1, userId2),
                createdAt = com.google.firebase.Timestamp.now()
            )
            val docRef = db.collection("chats").add(chat).await()
            chat.copy(chatId = docRef.id)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun deletePersonalChat(userId1: String, userId2: String) {
        val query = db.collection("chats")
            .whereEqualTo("type", "personal")
            .whereArrayContains("participants", userId1)
            .whereArrayContains("participants", userId2)
            .get()
            .await()

        query.documents.forEach { doc ->
            db.collection("messages")
                .whereEqualTo("chatId", doc.id)
                .get()
                .await()
                .documents
                .forEach { it.reference.delete().await() }
            doc.reference.delete().await()
        }
    }

    fun getMessages(chatId: String, limit: Long = 30, lastMessage: Message? = null): Flow<List<Message>> = callbackFlow {
        var query = db.collection("messages")
            .whereEqualTo("chatId", chatId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit)

        if (lastMessage != null && lastMessage.timestamp != null) {
            query = query.startAfter(lastMessage.timestamp)
        }

        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val messages = snapshot?.documents?.mapNotNull { doc ->
                val message = doc.toObject(Message::class.java)
                message?.copy(messageId = doc.id)
            } ?: emptyList()
            trySend(messages)
        }

        awaitClose { subscription.remove() }
    }

    suspend fun loadMoreMessages(chatId: String, lastMessage: Message, limit: Long = 30): List<Message> {
        return try {
            val query = db.collection("messages")
                .whereEqualTo("chatId", chatId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastMessage.timestamp)
                .limit(limit)
                .get()
                .await()

            query.documents.mapNotNull { doc ->
                val message = doc.toObject(Message::class.java)
                message?.copy(messageId = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun observeUserOnlineStatus(userId: String): Flow<Boolean> = callbackFlow {
        val subscription = db.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val lastActive = snapshot?.getTimestamp("lastActiveInApp")
                val isOnline = lastActive != null &&
                        (System.currentTimeMillis() - lastActive.toDate().time) < 120_000
                trySend(isOnline)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun editMessage(messageId: String, newText: String): Boolean {
        return try {
            db.collection("messages").document(messageId)
                .update(
                    mapOf(
                        "text" to newText,
                        "edited" to true,
                        "editedAt" to com.google.firebase.Timestamp.now()
                    )
                )
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun searchUsers(query: String): List<UserProfile> {
        if (query.isBlank()) return emptyList()

        return try {
            val normalizedQuery = query.trim().lowercase()

            val nameSnapshot = db.collection("users")
                .whereGreaterThanOrEqualTo("nameLowercase", normalizedQuery)
                .whereLessThanOrEqualTo("nameLowercase", normalizedQuery + "\uf8ff")
                .limit(20)
                .get()
                .await()

            val nameResults = nameSnapshot.documents.mapNotNull { it.toObject(UserProfile::class.java) }

            val emailSnapshot = db.collection("users")
                .whereGreaterThanOrEqualTo("emailLowercase", normalizedQuery)
                .whereLessThanOrEqualTo("emailLowercase", normalizedQuery + "\uf8ff")
                .limit(20)
                .get()
                .await()

            val emailResults = emailSnapshot.documents.mapNotNull { it.toObject(UserProfile::class.java) }

            (nameResults + emailResults).distinctBy { it.userId }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun deleteMessage(messageId: String): Boolean {
        return try {
            db.collection("messages").document(messageId).delete().await()
            true
        } catch (e: Exception) {
            println("DEBUG: Ошибка удаления сообщения: ${e.message}")
            false
        }
    }

    suspend fun markMessageAsRead(messageId: String, userId: String) {
        try {
            val doc = db.collection("messages").document(messageId).get().await()
            val senderId = doc.getString("senderId") ?: ""
            if (senderId != userId) {
                db.collection("messages").document(messageId)
                    .update("readBy", FieldValue.arrayUnion(userId))
                    .await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun markAllMessagesAsRead(chatId: String, userId: String) {
        val messages = db.collection("messages")
            .whereEqualTo("chatId", chatId)
            .get()
            .await()

        messages.documents.forEach { doc ->
            val readBy = doc.get("readBy") as? List<String> ?: emptyList()
            val senderId = doc.getString("senderId") ?: ""
            if (!readBy.contains(userId) && senderId != userId) {
                doc.reference.update("readBy", FieldValue.arrayUnion(userId)).await()
            }
        }
    }

    suspend fun getMessage(messageId: String): Message? {
        return try {
            val doc = db.collection("messages").document(messageId).get().await()
            doc.toObject(Message::class.java)?.copy(messageId = doc.id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun sendMessage(chatId: String, senderId: String, text: String, replyToId: String = "", replyToText: String = "", replyToSenderName: String = ""): Boolean {
        return try {
            updateUserLastActive(senderId)

            val sender = getUser(senderId)
            val senderName = sender?.name ?: "Пользователь"

            val message = Message(
                chatId = chatId,
                senderId = senderId,
                senderName = senderName,
                text = text,
                timestamp = com.google.firebase.Timestamp.now(),
                readBy = emptyList(),
                deliveredTo = listOf(senderId),
                replyToId = replyToId,
                replyToText = replyToText,
                replyToSenderName = replyToSenderName
            )
            db.collection("messages").add(message).await()

            db.collection("chats").document(chatId)
                .update(
                    mapOf(
                        "lastMessage" to text,
                        "lastMessageSender" to senderName,
                        "lastMessageTime" to com.google.firebase.Timestamp.now()
                    )
                )
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // ✅ НОВЫЙ МЕТОД: Загрузка изображения в Yandex Cloud
    suspend fun uploadImageAndGetUrl(uri: Uri, contentResolver: ContentResolver): String {
        return yandexStorageClient.uploadImage(uri, contentResolver)
    }

    // ✅ НОВЫЙ МЕТОД: Отправка сообщения с изображением
    suspend fun sendImageMessage(
        chatId: String,
        senderId: String,
        imageUrl: String,
        replyToId: String = "",
        replyToText: String = "",
        replyToSenderName: String = ""
    ): Boolean {
        return try {
            updateUserLastActive(senderId)
            val sender = getUser(senderId)
            val senderName = sender?.name ?: "Пользователь"

            val message = Message(
                chatId = chatId,
                senderId = senderId,
                senderName = senderName,
                text = "",
                imageUrl = imageUrl,
                timestamp = com.google.firebase.Timestamp.now(),
                readBy = emptyList(),
                deliveredTo = listOf(senderId),
                replyToId = replyToId,
                replyToText = replyToText,
                replyToSenderName = replyToSenderName
            )
            db.collection("messages").add(message).await()

            db.collection("chats").document(chatId)
                .update(
                    mapOf(
                        "lastMessage" to "📷 Изображение",
                        "lastMessageSender" to senderName,
                        "lastMessageTime" to com.google.firebase.Timestamp.now()
                    )
                )
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    // ============================================================================
// 🔹 ФИНАЛЬНЫЙ МЕТОД: ищет ЛЮБОЙ существующий чат + создаёт без дублей
// ============================================================================

    suspend fun findOrCreatePersonalChat(userId1: String, userId2: String): String = withContext(
        Dispatchers.IO) {
        val sortedIds = listOf(userId1, userId2).sorted()
        val deterministicId = sortedIds.joinToString("_")

        // 🔹 Вспомогательная функция для повторных попыток
        suspend fun retryWithBackoff(
            maxRetries: Int = 3,
            initialDelay: Long = 1000,
            operation: suspend () -> String
        ): String {
            var currentDelay = initialDelay
            repeat(maxRetries - 1) { attempt ->
                try {
                    return operation()
                } catch (e: Exception) {
                    println("DEBUG: Attempt ${attempt + 1} failed: ${e.message}")
                    if (attempt == maxRetries - 2) throw e
                    delay(currentDelay)
                    currentDelay *= 2
                }
            }
            return operation()
        }

        return@withContext try {
            // 🔹 1. Пытаемся найти существующий чат
            retryWithBackoff(maxRetries = 3) {
                try {
                    // Быстрая проверка детерминированного ID
                    val directRef = db.collection("chats").document(deterministicId)
                    val directSnap = try {
                        directRef.get().await()
                    } catch (e: Exception) {
                        if (e.message?.contains("offline") == true) {
                            println("DEBUG: Offline detected, waiting for connection...")
                            delay(2000)
                            directRef.get().await()
                        } else {
                            throw e
                        }
                    }

                    if (directSnap.exists()) {
                        println("DEBUG: Found by deterministic ID: $deterministicId")
                        return@retryWithBackoff deterministicId
                    }

                    // 🔹 2. Поиск по коллекции
                    val querySnapshot = try {
                        db.collection("chats")
                            .whereEqualTo("type", "personal")
                            .whereArrayContains("participants", userId1)
                            .get()
                            .await()
                    } catch (e: Exception) {
                        if (e.message?.contains("offline") == true) {
                            println("DEBUG: Offline during query, using fallback...")
                            // В офлайн-режиме пропускаем поиск
                            throw e
                        }
                        throw e
                    }

                    val existingChat = querySnapshot.documents.firstOrNull { doc ->
                        val participants = doc.get("participants") as? List<String> ?: emptyList()
                        participants.contains(userId2) && participants.size == 2
                    }

                    if (existingChat != null) {
                        println("DEBUG: Found by query search: ${existingChat.id}")
                        return@retryWithBackoff existingChat.id
                    }

                    // 🔹 3. Создаём новый чат
                    val chatData = mapOf(
                        "type" to "personal",
                        "participants" to sortedIds,
                        "name" to "",
                        "lastMessage" to "",
                        "lastMessageSender" to "",
                        "lastMessageTime" to com.google.firebase.Timestamp.now(),
                        "createdAt" to com.google.firebase.Timestamp.now(),
                        "unreadCounts" to mapOf(userId1 to 0, userId2 to 0)
                    )

                    try {
                        directRef.set(chatData).await()
                        println("DEBUG: Created new chat with deterministic ID: $deterministicId")
                    } catch (e: Exception) {
                        // Если не удалось создать (офлайн), используем ID локально
                        println("DEBUG: Failed to create chat, using offline ID: $deterministicId")
                    }

                    return@retryWithBackoff deterministicId
                } catch (e: Exception) {
                    println("DEBUG: Chat operation failed: ${e.message}")
                    // Возвращаем детерминированный ID даже при ошибке
                    if (e.message?.contains("offline") == true) {
                        println("DEBUG: Using deterministic ID in offline mode: $deterministicId")
                        return@retryWithBackoff deterministicId
                    }
                    throw e
                }
            }
        } catch (e: Exception) {
            println("DEBUG: All retries failed, using deterministic ID: $deterministicId")
            // 🔹 Финальный fallback - всегда возвращаем ID
            deterministicId
        }
    }

    fun getChats(userId: String): Flow<List<Chat>> = callbackFlow {
        val query = db.collection("chats")
            .whereArrayContains("participants", userId)
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)

        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val chats = snapshot?.documents?.mapNotNull { doc ->
                val chat = doc.toObject(Chat::class.java)
                chat?.copy(chatId = doc.id)
            } ?: emptyList()

            trySend(chats)

            for (chat in chats) {
                if (chat.type == "personal") {
                    val otherUserId = chat.participants.firstOrNull { it != userId }
                    if (otherUserId != null) {
                        db.collection("users").document(otherUserId).get()
                            .addOnSuccessListener { userDoc ->
                                val userName = userDoc.getString("name") ?: "Пользователь"
                                val updatedChat = chat.copy(name = userName)
                                val updatedChats = chats.map {
                                    if (it.chatId == updatedChat.chatId) updatedChat else it
                                }
                                trySend(updatedChats)
                            }
                    }
                }
            }
        }

        awaitClose { subscription.remove() }
    }

    suspend fun markMessageAsDelivered(messageId: String, userId: String) {
        try {
            val doc = db.collection("messages").document(messageId).get().await()
            val senderId = doc.getString("senderId") ?: ""
            if (senderId != userId) {
                db.collection("messages").document(messageId)
                    .update("deliveredTo", FieldValue.arrayUnion(userId))
                    .await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getUser(userId: String): UserProfile? {
        return try {
            val doc = db.collection("users").document(userId).get().await()
            doc.toObject(UserProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getChat(chatId: String): Chat? {
        return try {
            val doc = db.collection("chats").document(chatId).get().await()
            doc.toObject(Chat::class.java)?.copy(chatId = doc.id)
        } catch (e: Exception) {
            null
        }
    }
}