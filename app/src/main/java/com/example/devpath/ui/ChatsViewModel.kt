package com.example.devpath.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.devpath.data.repository.ChatRepository
import com.example.devpath.domain.models.Chat
import com.example.devpath.domain.models.FriendRequest
import com.example.devpath.domain.models.Message
import com.example.devpath.domain.models.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

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

    // Загрузить отправленные заявки
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

    fun loadIncomingRequests(userId: String) {
        viewModelScope.launch {
            chatRepository.getIncomingRequests(userId).collect { requests ->
                _incomingRequests.value = requests
            }
        }
    }

    fun loadChats(userId: String) {
        viewModelScope.launch {
            chatRepository.getChats(userId).collect { chats ->
                _chats.value = chats
            }
        }
    }

    fun loadMessages(chatId: String) {
        viewModelScope.launch {
            chatRepository.getMessages(chatId).collect { messages ->
                _messages.value = messages
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
        }
    }

    fun rejectFriendRequest(requestId: String) {
        viewModelScope.launch {
            chatRepository.rejectFriendRequest(requestId)
        }
    }

    fun removeFriend(userId: String, friendId: String) {
        viewModelScope.launch {
            chatRepository.removeFriend(userId, friendId)
        }
    }

    fun sendMessage(chatId: String, senderId: String, text: String) {
        viewModelScope.launch {
            chatRepository.sendMessage(chatId, senderId, text)
        }
    }

    fun searchUsers(query: String) {
        viewModelScope.launch {
            val results = chatRepository.searchUsers(query)
            _searchResults.value = results
        }
    }
}