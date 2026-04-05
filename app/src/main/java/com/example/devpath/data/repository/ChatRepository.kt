package com.example.devpath.data.repository

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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor() {
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

        // ✅ Используем addSnapshotListener для реального времени
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

    // Загрузить следующие сообщения (однократно)
    suspend fun loadMoreMessages(chatId: String, lastMessage: Message, limit: Long = 30): List<Message> {
        return try {
            val query = db.collection("messages")
                .whereEqualTo("chatId", chatId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastMessage.timestamp)
                .limit(limit)
                .get()
                .await()  // ✅ Для пагинации используем get() – однократная загрузка

            query.documents.mapNotNull { doc ->
                val message = doc.toObject(Message::class.java)
                message?.copy(messageId = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateUserOnlineStatus(userId: String, isOnline: Boolean) {
        try {
            db.collection("users").document(userId)
                .update(
                    mapOf(
                        "online" to isOnline,
                        "lastSeen" to com.google.firebase.Timestamp.now()
                    )
                )
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getUserOnlineStatus(userId: String): Boolean {
        return try {
            val doc = db.collection("users").document(userId).get().await()
            doc.getBoolean("online") ?: false
        } catch (e: Exception) {
            false
        }
    }

    fun observeUserOnlineStatus(userId: String): Flow<Boolean> = callbackFlow {
        val subscription = db.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val isOnline = snapshot?.getBoolean("online") ?: false
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