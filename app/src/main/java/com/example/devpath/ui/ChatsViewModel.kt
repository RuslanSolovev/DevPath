package com.example.devpath.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.devpath.data.repository.ChatRepository
import com.example.devpath.domain.models.Chat
import com.example.devpath.domain.models.FriendRequest
import com.example.devpath.domain.models.Message
import com.example.devpath.domain.models.UserProfile
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.channels.awaitClose

@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val db = Firebase.firestore

    private val _friends = MutableStateFlow<List<UserProfile>>(emptyList())
    val friends: StateFlow<List<UserProfile>> = _friends.asStateFlow()

    private val _incomingRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val incomingRequests: StateFlow<List<FriendRequest>> = _incomingRequests.asStateFlow()

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _searchResults = MutableStateFlow<List<UserProfile>>(emptyList())
    val searchResults: StateFlow<List<UserProfile>> = _searchResults.asStateFlow()

    private val _sentRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val sentRequests: StateFlow<List<FriendRequest>> = _sentRequests.asStateFlow()

    private val _currentChatName = MutableStateFlow("")
    val currentChatName: StateFlow<String> = _currentChatName.asStateFlow()

    private val _replyingTo = MutableStateFlow<Message?>(null)
    val replyingTo: StateFlow<Message?> = _replyingTo.asStateFlow()

    private val _editingMessage = MutableStateFlow<Message?>(null)
    val editingMessage: StateFlow<Message?> = _editingMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoadingMessages = MutableStateFlow(true)
    val isLoadingMessages: StateFlow<Boolean> = _isLoadingMessages.asStateFlow()

    private val _hasMoreMessages = MutableStateFlow(true)
    val hasMoreMessages: StateFlow<Boolean> = _hasMoreMessages.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private var lastLoadedMessage: Message? = null
    private val MESSAGE_PAGE_SIZE = 30L

    private val _totalMessagesCount = MutableStateFlow(0L)
    val totalMessagesCount: StateFlow<Long> = _totalMessagesCount.asStateFlow()

    private val _isUserOnline = MutableStateFlow(false)
    val isUserOnline: StateFlow<Boolean> = _isUserOnline.asStateFlow()

    private var typingTimeoutJob: kotlinx.coroutines.Job? = null

    fun getOtherParticipantId(chatId: String, currentUserId: String): String? {
        val chat = _chats.value.find { it.chatId == chatId }
        return chat?.participants?.firstOrNull { it != currentUserId }
    }

    fun observeFriendOnlineStatus(friendId: String) {
        viewModelScope.launch {
            chatRepository.observeUserOnlineStatus(friendId).collect { isOnline ->
                _isUserOnline.value = isOnline
            }
        }
    }

    fun updateUserOnlineStatus(userId: String, isOnline: Boolean) {
        viewModelScope.launch {
            chatRepository.updateUserOnlineStatus(userId, isOnline)
        }
    }

    fun observeTypingStatus(chatId: String, currentUserId: String): Flow<Boolean> = callbackFlow {
        val subscription = db.collection("chats").document(chatId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val typingUsers = snapshot?.get("typingUsers") as? List<*> ?: listOf<String>()
                val isSomeoneTyping = typingUsers.any { it != currentUserId }
                trySend(isSomeoneTyping)
            }
        awaitClose { subscription.remove() }
    }

    fun startTyping(chatId: String, userId: String) {
        viewModelScope.launch {
            db.collection("chats").document(chatId)
                .update("typingUsers", FieldValue.arrayUnion(userId))
                .await()
        }
        typingTimeoutJob?.cancel()
        typingTimeoutJob = viewModelScope.launch {
            delay(3000)
            stopTyping(chatId, userId)
        }
    }

    fun stopTyping(chatId: String, userId: String) {
        viewModelScope.launch {
            db.collection("chats").document(chatId)
                .update("typingUsers", FieldValue.arrayRemove(userId))
                .await()
        }
    }

    fun markMessageAsDelivered(messageId: String, userId: String) {
        if (messageId.startsWith("temp_")) return

        viewModelScope.launch {
            chatRepository.markMessageAsDelivered(messageId, userId)
            _messages.value = _messages.value.map { message ->
                if (message.messageId == messageId && !message.deliveredTo.contains(userId)) {
                    message.copy(deliveredTo = message.deliveredTo + userId)
                } else {
                    message
                }
            }
        }
    }

    fun loadTotalMessagesCount(chatId: String) {
        viewModelScope.launch {
            val count = chatRepository.getMessagesCount(chatId)
            _totalMessagesCount.value = count
        }
    }

    fun loadMessages(chatId: String, reset: Boolean = true) {
        viewModelScope.launch {
            if (reset) {
                _isLoadingMessages.value = true
                _messages.value = emptyList()
                lastLoadedMessage = null
                _hasMoreMessages.value = true
            }

            val messages = chatRepository.getMessages(chatId, MESSAGE_PAGE_SIZE, lastLoadedMessage).first()

            if (reset) {
                _messages.value = messages.reversed()
            } else {
                _messages.value = messages.reversed() + _messages.value
            }

            lastLoadedMessage = messages.lastOrNull()
            _hasMoreMessages.value = messages.size.toLong() == MESSAGE_PAGE_SIZE
            _isLoadingMessages.value = false
        }
    }

    fun loadMoreMessages(chatId: String) {
        if (_isLoadingMore.value || !_hasMoreMessages.value) return

        viewModelScope.launch {
            _isLoadingMore.value = true
            try {
                val moreMessages = chatRepository.loadMoreMessages(chatId, lastLoadedMessage ?: return@launch, MESSAGE_PAGE_SIZE)
                if (moreMessages.isNotEmpty()) {
                    _messages.value = moreMessages.reversed() + _messages.value
                    lastLoadedMessage = moreMessages.lastOrNull()
                    _hasMoreMessages.value = moreMessages.size.toLong() == MESSAGE_PAGE_SIZE
                } else {
                    _hasMoreMessages.value = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    fun setReplyingTo(message: Message?) {
        _replyingTo.value = message
    }

    fun setEditingMessage(message: Message?) {
        _editingMessage.value = message
    }

    fun editMessage(messageId: String, newText: String) {
        _messages.value = _messages.value.map { message ->
            if (message.messageId == messageId) {
                message.copy(text = newText, edited = true)
            } else {
                message
            }
        }
        _editingMessage.value = null

        viewModelScope.launch {
            chatRepository.editMessage(messageId, newText)
        }
    }

    fun deleteMessage(messageId: String) {
        _messages.value = _messages.value.filter { it.messageId != messageId }
        _totalMessagesCount.value -= 1

        viewModelScope.launch {
            chatRepository.deleteMessage(messageId)
        }
    }

    fun markMessageAsRead(messageId: String, userId: String) {
        if (messageId.startsWith("temp_")) return

        viewModelScope.launch {
            chatRepository.markMessageAsRead(messageId, userId)
            _messages.value = _messages.value.map { message ->
                if (message.messageId == messageId && !message.readBy.contains(userId)) {
                    message.copy(readBy = message.readBy + userId)
                } else {
                    message
                }
            }
        }
    }

    fun markAllMessagesAsRead(chatId: String, userId: String) {
        viewModelScope.launch {
            chatRepository.markAllMessagesAsRead(chatId, userId)
            _messages.value = _messages.value.map { message ->
                if (!message.readBy.contains(userId) && message.senderId != userId) {
                    message.copy(readBy = message.readBy + userId)
                } else {
                    message
                }
            }
        }
    }

    fun sendMessage(chatId: String, senderId: String, text: String, replyToId: String = "", replyToText: String = "", replyToSenderName: String = "") {
        val tempMessageId = "temp_${System.currentTimeMillis()}"

        val tempMessage = Message(
            messageId = tempMessageId,
            chatId = chatId,
            senderId = senderId,
            senderName = "Вы",
            text = text,
            timestamp = com.google.firebase.Timestamp.now(),
            readBy = emptyList(),
            deliveredTo = emptyList(),
            replyToId = replyToId,
            replyToText = replyToText,
            replyToSenderName = replyToSenderName,
            edited = false,
            deleted = false
        )

        _messages.value = _messages.value + tempMessage
        _replyingTo.value = null
        _totalMessagesCount.value += 1

        viewModelScope.launch {
            val success = chatRepository.sendMessage(chatId, senderId, text, replyToId, replyToText, replyToSenderName)
            if (success) {
                _messages.value = _messages.value.map { message ->
                    if (message.messageId == tempMessageId) {
                        message.copy(deliveredTo = listOf(senderId))
                    } else {
                        message
                    }
                }
            }
        }
    }

    fun loadSentRequests(userId: String) {
        viewModelScope.launch {
            chatRepository.getSentRequests(userId).collect { requests ->
                _sentRequests.value = requests
            }
        }
    }

    fun loadFriends(userId: String) {
        viewModelScope.launch {
            chatRepository.getFriends(userId).collect { friends ->
                _friends.value = friends
            }
        }
    }

    fun createChatAndNavigate(
        currentUserId: String,
        friendId: String,
        navController: NavHostController
    ) {
        viewModelScope.launch {
            val chat = chatRepository.createOrGetChat(currentUserId, friendId)
            chat?.let {
                navController.navigate("chat_detail/${it.chatId}")
            }
        }
    }

    fun loadIncomingRequests(userId: String) {
        viewModelScope.launch {
            chatRepository.getIncomingRequests(userId).collect { requests ->
                _incomingRequests.value = requests
            }
        }
    }

    fun loadChats(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            chatRepository.getChats(userId).collect { chats ->
                val enrichedChats = mutableListOf<Chat>()
                for (chat in chats) {
                    if (chat.type == "personal") {
                        val otherUserId = chat.participants.firstOrNull { it != userId }
                        if (otherUserId != null) {
                            val user = chatRepository.getUser(otherUserId)
                            enrichedChats.add(chat.copy(name = user?.name ?: "Пользователь"))
                        } else {
                            enrichedChats.add(chat)
                        }
                    } else {
                        enrichedChats.add(chat)
                    }
                }
                _chats.value = enrichedChats
                _isLoading.value = false
            }
        }
    }

    fun loadChatName(chatId: String, currentUserId: String) {
        viewModelScope.launch {
            val chat = chatRepository.getChat(chatId)
            if (chat?.type == "personal") {
                val otherUserId = chat.participants.firstOrNull { it != currentUserId }
                if (otherUserId != null) {
                    val user = chatRepository.getUser(otherUserId)
                    _currentChatName.value = user?.name ?: "Пользователь"
                } else {
                    _currentChatName.value = "Чат"
                }
            } else {
                _currentChatName.value = chat?.name ?: "Групповой чат"
            }
        }
    }

    fun sendFriendRequest(fromUserId: String, toUserId: String) {
        viewModelScope.launch {
            chatRepository.sendFriendRequest(fromUserId, toUserId)
        }
    }

    fun acceptFriendRequest(requestId: String, fromUserId: String, toUserId: String) {
        viewModelScope.launch {
            chatRepository.acceptFriendRequest(requestId, fromUserId, toUserId)
            loadFriends(toUserId)
        }
    }

    fun rejectFriendRequest(requestId: String) {
        viewModelScope.launch {
            chatRepository.rejectFriendRequest(requestId)
        }
    }

    fun deleteChat(chatId: String) {
        viewModelScope.launch {
            chatRepository.deleteChat(chatId)
            val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
            loadChats(currentUserId)
        }
    }

    fun removeFriend(userId: String, friendId: String) {
        viewModelScope.launch {
            chatRepository.removeFriend(userId, friendId)
            loadFriends(userId)
        }
    }

    fun searchUsers(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _searchResults.value = emptyList()
                return@launch
            }
            val results = chatRepository.searchUsers(query)
            _searchResults.value = results
        }
    }
}