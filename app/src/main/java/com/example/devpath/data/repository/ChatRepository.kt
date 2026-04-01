package com.example.devpath.data.repository

import com.example.devpath.domain.models.*
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

    // Получить список друзей пользователя
    fun getFriends(userId: String): Flow<List<UserProfile>> = callbackFlow {
        val query1 = db.collection("friendships").whereEqualTo("userId1", userId)
        val query2 = db.collection("friendships").whereEqualTo("userId2", userId)

        val subscription1 = query1.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val friendIds = mutableListOf<String>()
            snapshot?.documents?.forEach { doc ->
                val friendship = doc.toObject<Friendship>()
                if (friendship?.userId1 == userId) {
                    friendIds.add(friendship.userId2)
                } else if (friendship?.userId2 == userId) {
                    friendIds.add(friendship.userId1)
                }
            }

            if (friendIds.isEmpty()) {
                trySend(emptyList())
                return@addSnapshotListener
            }

            db.collection("users")
                .whereIn("userId", friendIds)
                .addSnapshotListener { usersSnapshot, userError ->
                    if (userError != null) {
                        close(userError)
                        return@addSnapshotListener
                    }
                    val friends = usersSnapshot?.documents?.mapNotNull { it.toObject<UserProfile>() } ?: emptyList()
                    trySend(friends)
                }
        }

        val subscription2 = query2.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val friendIds = mutableListOf<String>()
            snapshot?.documents?.forEach { doc ->
                val friendship = doc.toObject<Friendship>()
                if (friendship?.userId1 == userId) {
                    friendIds.add(friendship.userId2)
                } else if (friendship?.userId2 == userId) {
                    friendIds.add(friendship.userId1)
                }
            }

            if (friendIds.isNotEmpty()) {
                db.collection("users")
                    .whereIn("userId", friendIds)
                    .addSnapshotListener { usersSnapshot, userError ->
                        if (userError != null) {
                            close(userError)
                            return@addSnapshotListener
                        }
                        val friends = usersSnapshot?.documents?.mapNotNull { it.toObject<UserProfile>() } ?: emptyList()
                        trySend(friends)
                    }
            }
        }

        awaitClose {
            subscription1.remove()
            subscription2.remove()
        }
    }

    // Получить входящие заявки
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
                val request = doc.toObject<FriendRequest>()
                request?.copy(requestId = doc.id)
            } ?: emptyList()
            trySend(requests)
        }

        awaitClose { subscription.remove() }
    }

    // Получить отправленные заявки
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
                val request = doc.toObject<FriendRequest>()
                request?.copy(requestId = doc.id)
            } ?: emptyList()
            trySend(requests)
        }

        awaitClose { subscription.remove() }
    }

    // Отправить заявку
    suspend fun sendFriendRequest(fromUserId: String, toUserId: String): Boolean {
        return try {
            val existingRequest = db.collection("friend_requests")
                .whereEqualTo("fromUserId", fromUserId)
                .whereEqualTo("toUserId", toUserId)
                .whereIn("status", listOf("pending", "accepted"))
                .get()
                .await()

            if (existingRequest.isEmpty) {
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
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Принять заявку (без создания чата)
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

    // Отклонить заявку
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

    // Удалить из друзей
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

            deletePersonalChat(userId, friendId)

            true
        } catch (e: Exception) {
            false
        }
    }

    // Создать личный чат
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

    // Получить или создать чат между пользователями (исправленная версия)
    suspend fun createOrGetChat(userId1: String, userId2: String): Chat? {
        return try {
            // Ищем чаты, где userId1 есть в participants
            val query = db.collection("chats")
                .whereEqualTo("type", "personal")
                .whereArrayContains("participants", userId1)

            val snapshot = query.get().await()

            // Фильтруем в коде, проверяя наличие userId2
            val existingChat = snapshot.documents.firstOrNull { doc ->
                val participants = doc.get("participants") as? List<String> ?: emptyList()
                participants.contains(userId2)
            }

            if (existingChat != null) {
                return existingChat.toObject<Chat>()?.copy(chatId = existingChat.id)
            }

            // Создаём новый чат
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

    // Удалить личный чат
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

    // Получить чаты пользователя
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
                val chat = doc.toObject<Chat>()
                chat?.copy(chatId = doc.id)
            } ?: emptyList()
            trySend(chats)
        }

        awaitClose { subscription.remove() }
    }

    // Получить сообщения чата
    fun getMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val query = db.collection("messages")
            .whereEqualTo("chatId", chatId)
            .orderBy("timestamp", Query.Direction.ASCENDING)

        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val messages = snapshot?.documents?.mapNotNull { it.toObject<Message>() } ?: emptyList()
            trySend(messages)
        }

        awaitClose { subscription.remove() }
    }

    // Отправить сообщение
    suspend fun sendMessage(chatId: String, senderId: String, text: String): Boolean {
        return try {
            val message = Message(
                chatId = chatId,
                senderId = senderId,
                text = text,
                timestamp = com.google.firebase.Timestamp.now()
            )
            db.collection("messages").add(message).await()

            db.collection("chats").document(chatId)
                .update(
                    mapOf(
                        "lastMessage" to text,
                        "lastMessageTime" to com.google.firebase.Timestamp.now()
                    )
                )
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Отметить сообщение как прочитанное
    suspend fun markMessageAsRead(messageId: String, userId: String) {
        db.collection("messages").document(messageId)
            .update("readBy", FieldValue.arrayUnion(userId))
            .await()
    }

    // Поиск пользователей по имени
    suspend fun searchUsers(query: String): List<UserProfile> {
        return try {
            val snapshot = db.collection("users")
                .whereGreaterThanOrEqualTo("name", query)
                .whereLessThanOrEqualTo("name", query + "\uf8ff")
                .limit(20)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject<UserProfile>() }
        } catch (e: Exception) {
            emptyList()
        }
    }
}