package com.example.devpath.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.devpath.data.repository.EventsRepository
import com.example.devpath.data.repository.LocationRepository
import com.example.devpath.data.repository.YdbRepository
import com.example.devpath.data.repository.UserLocation
import com.example.devpath.data.repository.LocationSettings
import com.example.devpath.domain.models.MapMarker
import com.example.devpath.domain.models.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val eventsRepository: EventsRepository,
    private val ydbRepository: YdbRepository
) : ViewModel() {

    private val _currentLocation = MutableStateFlow<android.location.Location?>(null)
    val currentLocation: StateFlow<android.location.Location?> = _currentLocation.asStateFlow()

    private val _nearbyUsers = MutableStateFlow<List<UserLocation>>(emptyList())
    val nearbyUsers: StateFlow<List<UserLocation>> = _nearbyUsers.asStateFlow()

    private val _locationSettings = MutableStateFlow(LocationSettings())
    val locationSettings: StateFlow<LocationSettings> = _locationSettings.asStateFlow()

    private val _friends = MutableStateFlow<List<UserProfile>>(emptyList())
    val friends: StateFlow<List<UserProfile>> = _friends.asStateFlow()

    private val _currentUserProfile = MutableStateFlow<UserProfile?>(null)
    val currentUserProfile: StateFlow<UserProfile?> = _currentUserProfile.asStateFlow()

    private val _nearbyMarkers = MutableStateFlow<List<MapMarker>>(emptyList())
    val nearbyMarkers: StateFlow<List<MapMarker>> = _nearbyMarkers.asStateFlow()

    init {
        viewModelScope.launch {
            locationRepository.currentLocation.collect { location ->
                _currentLocation.value = location
            }
        }
        viewModelScope.launch {
            locationRepository.locationSettings.collect { settings ->
                _locationSettings.value = settings
            }
        }
    }

    fun startLocationUpdates(userId: String, userName: String, avatarUrl: String?) {
        viewModelScope.launch {
            locationRepository.startLocationUpdates()

            // Загружаем профиль пользователя из YDB
            val user = ydbRepository.getUser(userId)
            _currentUserProfile.value = UserProfile(
                userId = userId,
                name = user?.optJSONObject("name")?.optString("S", userName) ?: userName,
                avatarUrl = user?.optJSONObject("avatar_url")?.optString("S", "")?.ifEmpty { null }
            )

            // Загружаем друзей из YDB
            val friendIds = ydbRepository.getUserFriends(userId)
            _friends.value = friendIds.mapNotNull { friendId ->
                val friendUser = ydbRepository.getUser(friendId)
                friendUser?.let {
                    UserProfile(
                        userId = friendId,
                        name = it.optJSONObject("name")?.optString("S", "") ?: "",
                        email = it.optJSONObject("email")?.optString("S", "") ?: "",
                        avatarUrl = it.optJSONObject("avatar_url")?.optString("S", "")?.ifEmpty { null }
                    )
                }
            }

            // Обновляем свою локацию
            locationRepository.updateUserLocation(
                userId,
                _currentUserProfile.value?.name ?: userName,
                _currentUserProfile.value?.avatarUrl
            )
        }
    }

    fun loadNearbyUsers() {
        viewModelScope.launch {
            while (true) {
                try {
                    _nearbyUsers.value = locationRepository.getNearbyUsers()
                } catch (e: Exception) {
                    println("DEBUG: MapViewModel - ошибка загрузки nearby users: ${e.message}")
                }
                delay(10000)
            }
        }
    }

    fun loadNearbyMarkers(userId: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            eventsRepository.getNearbyMarkers(userId, latitude, longitude).collect { markers ->
                _nearbyMarkers.value = markers
                println("DEBUG: MapViewModel - маркеров загружено: ${markers.size}")
            }
        }
    }

    suspend fun createMarker(marker: MapMarker): String {
        return eventsRepository.createMarker(marker)
    }

    suspend fun joinMarker(markerId: String) {
        val userId = _currentUserProfile.value?.userId ?: return
        eventsRepository.joinMarker(markerId, userId)
    }

    suspend fun leaveMarker(markerId: String) {
        val userId = _currentUserProfile.value?.userId ?: return
        eventsRepository.leaveMarker(markerId, userId)
    }

    suspend fun reportMarker(markerId: String, reason: String) {
        val userId = _currentUserProfile.value?.userId ?: return
        eventsRepository.reportMarker(markerId, userId, reason)
    }

    suspend fun incrementMarkerViews(markerId: String) {
        eventsRepository.incrementMarkerViews(markerId)
    }

    suspend fun getMarker(markerId: String): MapMarker? {
        return eventsRepository.getMarker(markerId)
    }

    // ✅ НОВЫЙ МЕТОД: Удаление маркера
    suspend fun deleteMarker(markerId: String, chatId: String?) {
        eventsRepository.deleteMarker(markerId, chatId)
    }

    suspend fun getOrCreatePersonalChat(userId1: String, userId2: String): String {
        // Ищем существующий чат
        val existingChats = ydbRepository.getUserChats(userId1)
        val existing = existingChats.find { chat ->
            val participants = chat.optJSONObject("participants")?.optJSONArray("SS")
            participants != null && (0 until participants.length()).any { participants.getString(it) == userId2 }
        }
        if (existing != null) {
            return existing.optJSONObject("chat_id")?.optString("S") ?: ""
        }
        // Создаём новый чат
        val chatId = java.util.UUID.randomUUID().toString()
        ydbRepository.createChat(chatId, "personal", listOf(userId1, userId2), "", userId1)
        return chatId
    }

    fun loadLocationSettings(userId: String) {
        viewModelScope.launch {
            val settings = locationRepository.loadLocationSettings(userId)
            _locationSettings.value = settings
        }
    }

    fun updateLocationSettings(settings: LocationSettings, userId: String) {
        viewModelScope.launch {
            locationRepository.saveLocationSettings(userId, settings)
            _locationSettings.value = settings
        }
    }

    fun loadFriends(userId: String) {
        viewModelScope.launch {
            val friendIds = ydbRepository.getUserFriends(userId)
            _friends.value = friendIds.mapNotNull { friendId ->
                val friendUser = ydbRepository.getUser(friendId)
                friendUser?.let {
                    UserProfile(
                        userId = friendId,
                        name = it.optJSONObject("name")?.optString("S", "") ?: "",
                        email = it.optJSONObject("email")?.optString("S", "") ?: "",
                        avatarUrl = it.optJSONObject("avatar_url")?.optString("S", "")?.ifEmpty { null }
                    )
                }
            }
        }
    }

    fun sendFriendRequest(fromUserId: String, toUserId: String) {
        viewModelScope.launch {
            ydbRepository.sendFriendRequest(fromUserId, toUserId)
        }
    }

    suspend fun getUserProfile(userId: String): UserProfile? {
        val user = ydbRepository.getUser(userId) ?: return null
        return UserProfile(
            userId = userId,
            name = user.optJSONObject("name")?.optString("S", "") ?: "",
            email = user.optJSONObject("email")?.optString("S", "") ?: "",
            avatarUrl = user.optJSONObject("avatar_url")?.optString("S", "")?.ifEmpty { null }
        )
    }
}