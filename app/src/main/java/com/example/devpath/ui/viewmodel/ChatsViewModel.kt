package com.example.devpath.ui.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.devpath.data.repository.YdbRepository
import com.example.devpath.data.storage.YandexStorageClient
import com.example.devpath.domain.models.Chat
import com.example.devpath.domain.models.FriendRequest
import com.example.devpath.domain.models.Message
import com.example.devpath.domain.models.Reaction
import com.example.devpath.domain.models.UserProfile
import com.example.devpath.utils.Config
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val ydbRepository: YdbRepository
) : ViewModel() {

    private val _friends = MutableStateFlow<List<UserProfile>>(emptyList())
    val friends: StateFlow<List<UserProfile>> = _friends.asStateFlow()

    private val _incomingRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val incomingRequests: StateFlow<List<FriendRequest>> = _incomingRequests.asStateFlow()

    private val _sentRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val sentRequests: StateFlow<List<FriendRequest>> = _sentRequests.asStateFlow()

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _searchResults = MutableStateFlow<List<UserProfile>>(emptyList())
    val searchResults: StateFlow<List<UserProfile>> = _searchResults.asStateFlow()

    private val _currentChatName = MutableStateFlow("")
    val currentChatName: StateFlow<String> = _currentChatName.asStateFlow()

    private val _replyingTo = MutableStateFlow<Message?>(null)
    val replyingTo: StateFlow<Message?> = _replyingTo.asStateFlow()

    private val _editingMessage = MutableStateFlow<Message?>(null)
    val editingMessage: StateFlow<Message?> = _editingMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoadingMessages = MutableStateFlow(false)
    val isLoadingMessages: StateFlow<Boolean> = _isLoadingMessages.asStateFlow()

    private val _totalMessagesCount = MutableStateFlow(0L)
    val totalMessagesCount: StateFlow<Long> = _totalMessagesCount.asStateFlow()

    private val _isUserOnline = MutableStateFlow(false)
    val isUserOnline: StateFlow<Boolean> = _isUserOnline.asStateFlow()

    private val _userLastActive = MutableStateFlow("")
    val userLastActive: StateFlow<String> = _userLastActive.asStateFlow()

    private val _messageSearchResults = MutableStateFlow<List<Message>>(emptyList())
    val messageSearchResults: StateFlow<List<Message>> = _messageSearchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private var chatListPollingJob: Job? = null

    private var pollingJob: Job? = null



    // ==================== POLLING ====================

    fun startPolling(chatId: String) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(800) // уменьшили задержку для живости
                try {
                    // Получаем все сообщения (последние 200)
                    val allMessagesFromDb = ydbRepository.getRecentMessages(chatId, 200)
                        .map { jsonToMessage(it) }

                    // Строим новый список: либо обновляем существующие, либо добавляем новые
                    val currentMap = _messages.value.associateBy { it.messageId }
                    val updatedList = allMessagesFromDb.map { dbMsg ->
                        currentMap[dbMsg.messageId]?.let { localMsg ->
                            // Если сообщение уже есть – берём самые свежие данные из БД
                            // но сохраняем локальные оптимистичные изменения, которых ещё нет в БД?
                            // Нет, теперь мы доверяем БД для всех полей, чтобы всё синхронизировалось.
                            dbMsg
                        } ?: dbMsg
                    }.toMutableList()

                    // Добавляем сообщения, которые есть локально, но отсутствуют в ответе БД
                    // (например, только что отправленные, ещё не попавшие в выдачу)
                    val dbIds = allMessagesFromDb.map { it.messageId }.toSet()
                    currentMap.values.filter { it.messageId !in dbIds }.forEach {
                        updatedList.add(it)
                    }

                    _messages.value = updatedList.sortedBy { it.timestamp }
                    _totalMessagesCount.value = _messages.value.size.toLong()
                } catch (_: Exception) {
                }
            }
        }
    }


    fun startSmartPolling(userId: String) {
        chatListPollingJob?.cancel()
        chatListPollingJob = viewModelScope.launch {
            while (true) {
                delay(1_500) // проверяем каждые 1.5 секунды
                try {
                    val newChats = ydbRepository.getUserChats(userId).map { jsonToChat(it, userId) }
                    val currentChats = _chats.value

                    // Сравниваем только lastMessage, lastMessageTime и lastMessageSender
                    val hasChanges = newChats.any { newChat ->
                        val currentChat = currentChats.find { it.chatId == newChat.chatId }
                        currentChat == null ||
                                currentChat.lastMessage != newChat.lastMessage ||
                                currentChat.lastMessageTime != newChat.lastMessageTime ||
                                currentChat.lastMessageSender != newChat.lastMessageSender
                    }

                    // Обновляем только если есть реальные изменения
                    if (hasChanges || newChats.size != currentChats.size) {
                        _chats.value = newChats
                    }
                } catch (_: Exception) { }
            }
        }
    }

    fun stopChatListPolling() {
        chatListPollingJob?.cancel()
    }

    fun stopPolling() {
        pollingJob?.cancel()
    }

    // ==================== ДРУЗЬЯ ====================

    fun loadFriends(userId: String) {
        viewModelScope.launch {
            try {
                val friendIds = ydbRepository.getUserFriends(userId)
                val friendProfiles = friendIds.mapNotNull { friendId ->
                    val user = ydbRepository.getUser(friendId)
                    user?.let { jsonToUserProfile(it) }
                }
                _friends.value = friendProfiles
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun removeFriend(userId: String, friendId: String) {
        viewModelScope.launch {
            ydbRepository.removeFriend(userId, friendId)
            loadFriends(userId)
        }
    }

    // ==================== ЗАЯВКИ ====================

    fun loadIncomingRequests(userId: String) {
        viewModelScope.launch {
            _incomingRequests.value =
                ydbRepository.getIncomingRequests(userId).map { jsonToFriendRequest(it) }
        }
    }

    fun loadSentRequests(userId: String) {
        viewModelScope.launch {
            _sentRequests.value =
                ydbRepository.getSentRequests(userId).map { jsonToFriendRequest(it) }
        }
    }

    fun sendFriendRequest(fromUserId: String, toUserId: String) {
        viewModelScope.launch { ydbRepository.sendFriendRequest(fromUserId, toUserId) }
    }

    fun acceptFriendRequest(requestId: String, fromUserId: String, toUserId: String) {
        viewModelScope.launch {
            ydbRepository.acceptFriendRequest(requestId)
            ydbRepository.addFriend(fromUserId, toUserId)
            ydbRepository.addFriend(toUserId, fromUserId)
            loadFriends(toUserId)
            loadIncomingRequests(toUserId)
        }
    }

    fun rejectFriendRequest(requestId: String) {
        viewModelScope.launch { ydbRepository.rejectFriendRequest(requestId) }
    }

    // ==================== ПОИСК ====================

    fun searchUsers(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _searchResults.value = emptyList(); return@launch
            }
            _searchResults.value = ydbRepository.searchUsers(query).map { jsonToUserProfile(it) }
        }
    }

    // ==================== ЧАТЫ ====================

    fun loadChats(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _chats.value = ydbRepository.getUserChats(userId).map { jsonToChat(it, userId) }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createChatAndNavigate(
        currentUserId: String,
        friendId: String,
        navController: NavHostController
    ) {
        viewModelScope.launch {
            val existingChats = ydbRepository.getUserChats(currentUserId)
            val existingChat = existingChats.find { chat ->
                val participants =
                    chat.optJSONObject("participants")?.optJSONArray("SS") ?: JSONArray()
                val list = (0 until participants.length()).map { participants.getString(it) }
                list.contains(friendId) && chat.optJSONObject("type")?.optString("S") == "personal"
            }
            if (existingChat != null) {
                val chatId = existingChat.optJSONObject("chat_id")?.optString("S") ?: return@launch
                navController.navigate("chat_detail/$chatId/$friendId")
            } else {
                val chatId = UUID.randomUUID().toString()
                ydbRepository.createChat(
                    chatId,
                    "personal",
                    listOf(currentUserId, friendId),
                    "",
                    currentUserId
                )
                navController.navigate("chat_detail/$chatId/$friendId")
            }
        }
    }

    fun loadChatName(chatId: String, currentUserId: String) {
        viewModelScope.launch {
            val chat = ydbRepository.getChat(chatId) ?: return@launch
            val type = chat.optJSONObject("type")?.optString("S") ?: "personal"
            if (type == "personal") {
                val participants = chat.optJSONObject("participants")?.optJSONArray("SS")
                val otherUserId =
                    (0 until (participants?.length() ?: 0)).map { participants!!.getString(it) }
                        .firstOrNull { it != currentUserId }
                if (otherUserId != null) {
                    _currentChatName.value =
                        ydbRepository.getUser(otherUserId)?.optJSONObject("name")?.optString("S")
                            ?: "Пользователь"
                }
            } else {
                _currentChatName.value =
                    chat.optJSONObject("name")?.optString("S") ?: "Групповой чат"
            }
        }
    }

    fun deleteChat(chatId: String, currentUserId: String) {
        viewModelScope.launch {
            // Мгновенное удаление из UI
            _chats.value = _chats.value.filter { it.chatId != chatId }

            val success = ydbRepository.deleteChat(chatId)

            if (!success) {
                // Если не удалилось — восстанавливаем список
                _chats.value = ydbRepository.getUserChats(currentUserId).map {
                    jsonToChat(it, currentUserId)
                }
            }
        }
    }

    // ==================== СООБЩЕНИЯ ====================

    fun loadMessages(chatId: String, reset: Boolean = true) {
        viewModelScope.launch {
            _isLoadingMessages.value = true
            try {
                val messagesJson = ydbRepository.getChatMessages(chatId)
                _messages.value = messagesJson.map { jsonToMessage(it) }
                _totalMessagesCount.value = _messages.value.size.toLong()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoadingMessages.value = false
            }
        }
    }

    fun sendMessage(
        chatId: String, senderId: String, text: String,
        replyToId: String = "", replyToText: String = "", replyToSenderName: String = ""
    ) {
        viewModelScope.launch {
            val messageId = UUID.randomUUID().toString()
            val user = ydbRepository.getUser(senderId)
            val senderName = user?.optJSONObject("name")?.optString("S", "Вы") ?: "Вы"

            val tempMessage = Message(
                messageId = messageId, chatId = chatId, senderId = senderId,
                senderName = senderName, text = text, timestamp = System.currentTimeMillis(),
                deliveredTo = emptyList(),    // ⬅️ не добавляем себя в deliveredTo
                replyToId = replyToId, replyToText = replyToText,
                replyToSenderName = replyToSenderName
            )
            _messages.value = _messages.value + tempMessage
            _totalMessagesCount.value = _messages.value.size.toLong()
            _replyingTo.value = null

            // Отправляем в БД и обрабатываем ошибку
            val success = ydbRepository.sendMessage(
                messageId, chatId, senderId, senderName, text, "",
                replyToId, replyToText, replyToSenderName
            )
            if (success) {
                ydbRepository.updateChatLastMessage(chatId, text, senderName)
            } else {
                // Откатываем, если не удалось сохранить в YDB
                _messages.value = _messages.value.filter { it.messageId != messageId }
                _totalMessagesCount.value = _messages.value.size.toLong()
                // Здесь можно выставить состояние ошибки и показать Snackbar
            }
        }
    }

    fun editMessage(messageId: String, newText: String) {
        viewModelScope.launch {
            // ✅ Мгновенно в UI
            _messages.value = _messages.value.map {
                if (it.messageId == messageId) it.copy(
                    text = newText,
                    edited = true
                ) else it
            }
            ydbRepository.editMessage(messageId, newText)
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            // ✅ Мгновенно в UI
            _messages.value = _messages.value.filter { it.messageId != messageId }
            _totalMessagesCount.value = _messages.value.size.toLong()
            ydbRepository.deleteMessage(messageId)
        }
    }

    fun setReplyingTo(message: Message?) {
        _replyingTo.value = message
    }

    fun setEditingMessage(message: Message?) {
        _editingMessage.value = message
    }

    fun addReaction(messageId: String, userId: String, reaction: String) {
        viewModelScope.launch {
            val message = _messages.value.find { it.messageId == messageId } ?: return@launch

            val updated = message.reactions.filterNot { it.userId == userId }.toMutableList()
            updated.add(Reaction(userId = userId, reaction = reaction))

            val newList = _messages.value.map {
                if (it.messageId == messageId) {
                    it.copy(reactions = updated.toList())
                } else it
            }.toList() // Новый объект списка

            _messages.value = newList

            launch {
                ydbRepository.updateMessageReactions(messageId, updated.toList())
            }
        }
    }

    fun removeReaction(messageId: String, userId: String) {
        viewModelScope.launch {
            // Мгновенно обновляем UI
            val message = _messages.value.find { it.messageId == messageId } ?: return@launch
            val updated = message.reactions.filterNot { it.userId == userId }
            _messages.value = _messages.value.map {
                if (it.messageId == messageId) it.copy(reactions = updated)
                else it
            }

            // Отправляем на сервер
            ydbRepository.updateMessageReactions(messageId, updated)
        }
    }

    fun markMessageAsDelivered(messageId: String, userId: String) {
        viewModelScope.launch {
            // Создаём новый список чтобы точно триггернуть рекомпозицию
            val newList = _messages.value.map {
                if (it.messageId == messageId && !it.deliveredTo.contains(userId)) {
                    it.copy(deliveredTo = it.deliveredTo + userId)
                } else it
            }.toList() // toList() создаёт новый объект списка

            _messages.value = newList

            // Отправляем на сервер в фоне
            launch {
                ydbRepository.markMessageDelivered(messageId, userId)
            }
        }
    }

    fun markMessageAsRead(messageId: String, userId: String) {
        viewModelScope.launch {
            // Создаём новый список чтобы точно триггернуть рекомпозицию
            val newList = _messages.value.map {
                if (it.messageId == messageId && !it.readBy.contains(userId)) {
                    it.copy(readBy = it.readBy + userId)
                } else it
            }.toList() // toList() создаёт новый объект списка

            _messages.value = newList

            // Отправляем на сервер в фоне
            launch {
                ydbRepository.markMessageRead(messageId, userId)
            }
        }
    }


    fun searchMessagesInChat(chatId: String, query: String) {
        viewModelScope.launch {
            _isSearching.value = true
            try {
                _messageSearchResults.value =
                    ydbRepository.searchMessages(chatId, query).map { jsonToMessage(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun clearMessageSearchResults() {
        _messageSearchResults.value = emptyList()
    }

    // ==================== ОНЛАЙН СТАТУС ====================

    fun updateUserLastActive(userId: String) {
        viewModelScope.launch { ydbRepository.updateLastSeen(userId) }
    }

    fun observeFriendOnlineStatus(friendId: String) {
        viewModelScope.launch {
            while (true) {
                val user = ydbRepository.getUser(friendId)
                val lastSeen = user?.optJSONObject("last_seen")?.optString("S")?.toLongOrNull() ?: 0
                _isUserOnline.value = (System.currentTimeMillis() - lastSeen) < 120_000
                delay(5000)
            }
        }
    }

    // ==================== КОНВЕРТЕРЫ ====================

    private fun jsonToUserProfile(json: org.json.JSONObject) = UserProfile(
        userId = json.optJSONObject("user_id")?.optString("S") ?: "",
        name = json.optJSONObject("name")?.optString("S") ?: "",
        email = json.optJSONObject("email")?.optString("S") ?: "",
        avatarUrl = json.optJSONObject("avatar_url")?.optString("S")?.ifEmpty { null }
    )

    private fun jsonToFriendRequest(json: org.json.JSONObject) = FriendRequest(
        requestId = json.optJSONObject("request_id")?.optString("S") ?: "",
        fromUserId = json.optJSONObject("from_user_id")?.optString("S") ?: "",
        toUserId = json.optJSONObject("to_user_id")?.optString("S") ?: "",
        status = json.optJSONObject("status")?.optString("S") ?: "pending",
        createdAt = json.optJSONObject("created_at")?.optString("S")?.toLongOrNull()
            ?: System.currentTimeMillis()
    )

    private fun jsonToChat(json: org.json.JSONObject, currentUserId: String): Chat {
        val participantsArray = json.optJSONObject("participants")?.optJSONArray("SS")
        val participants = mutableListOf<String>()
        if (participantsArray != null) for (i in 0 until participantsArray.length()) participants.add(
            participantsArray.getString(i)
        )

        return Chat(
            chatId = json.optJSONObject("chat_id")?.optString("S") ?: "",
            type = json.optJSONObject("type")?.optString("S") ?: "personal",
            participants = participants,
            name = json.optJSONObject("name")?.optString("S") ?: "",
            lastMessage = json.optJSONObject("last_message")?.optString("S") ?: "",
            lastMessageTime = json.optJSONObject("last_message_time")?.optString("S")
                ?.toLongOrNull() ?: System.currentTimeMillis(),  // ✅ Добавить
            lastMessageSender = json.optJSONObject("last_message_sender")?.optString("S") ?: ""
        )
    }

    private fun jsonToMessage(json: org.json.JSONObject): Message {
        val reactionsArray = json.optJSONObject("reactions")?.optJSONArray("L")
        val reactions = mutableListOf<Reaction>()

        if (reactionsArray != null) {
            for (i in 0 until reactionsArray.length()) {
                val reactionItem = reactionsArray.getJSONObject(i)
                val reactionData =
                    if (reactionItem.has("M")) reactionItem.getJSONObject("M") else reactionItem

                // ✅ ИСПРАВЛЕНИЕ: Извлекаем строки из S-объектов
                val userIdObj = reactionData.opt("user_id")
                val reactionObj = reactionData.opt("reaction")

                val userId = when (userIdObj) {
                    is String -> userIdObj
                    is JSONObject -> userIdObj.optString("S", "")
                    else -> ""
                }

                val reaction = when (reactionObj) {
                    is String -> reactionObj
                    is JSONObject -> reactionObj.optString("S", "")
                    else -> ""
                }

                if (userId.isNotEmpty() && reaction.isNotEmpty()) {
                    reactions.add(Reaction(userId = userId, reaction = reaction))
                }
            }
        }

        // Парсим delivered_to
        val deliveredArray = json.optJSONObject("delivered_to")?.optJSONArray("SS")
        val deliveredTo = mutableListOf<String>()
        if (deliveredArray != null) {
            for (i in 0 until deliveredArray.length()) {
                deliveredTo.add(deliveredArray.getString(i))
            }
        }

        // Парсим read_by
        val readArray = json.optJSONObject("read_by")?.optJSONArray("SS")
        val readBy = mutableListOf<String>()
        if (readArray != null) {
            for (i in 0 until readArray.length()) {
                readBy.add(readArray.getString(i))
            }
        }

        return Message(
            messageId = json.optJSONObject("message_id")?.optString("S") ?: "",
            chatId = json.optJSONObject("chat_id")?.optString("S") ?: "",
            senderId = json.optJSONObject("sender_id")?.optString("S") ?: "",
            senderName = json.optJSONObject("sender_name")?.optString("S") ?: "",
            text = json.optJSONObject("text")?.optString("S") ?: "",
            imageUrl = json.optJSONObject("image_url")?.optString("S") ?: "",
            timestamp = json.optJSONObject("timestamp")?.optString("S")?.toLongOrNull()
                ?: System.currentTimeMillis(),
            edited = json.optJSONObject("edited")?.optBoolean("BOOL") ?: false,
            deleted = json.optJSONObject("deleted")?.optBoolean("BOOL") ?: false,
            reactions = reactions,
            replyToId = json.optJSONObject("reply_to_id")?.optString("S") ?: "",
            replyToText = json.optJSONObject("reply_to_text")?.optString("S") ?: "",
            replyToSenderName = json.optJSONObject("reply_to_sender_name")?.optString("S") ?: "",
            deliveredTo = deliveredTo,
            readBy = readBy
        )
    }
}