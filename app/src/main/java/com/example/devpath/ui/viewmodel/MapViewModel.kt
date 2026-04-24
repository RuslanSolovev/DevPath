package com.example.devpath.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.devpath.data.repository.EventsRepository
import com.example.devpath.data.repository.LocationRepository
import com.example.devpath.data.repository.ChatRepository
import com.example.devpath.domain.models.MapMarker
import com.example.devpath.domain.models.UserProfile
import com.google.firebase.auth.FirebaseAuth
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
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _currentLocation = MutableStateFlow<android.location.Location?>(null)
    val currentLocation: StateFlow<android.location.Location?> = _currentLocation.asStateFlow()

    private val _nearbyUsers = MutableStateFlow<List<com.example.devpath.data.repository.UserLocation>>(emptyList())
    val nearbyUsers: StateFlow<List<com.example.devpath.data.repository.UserLocation>> = _nearbyUsers.asStateFlow()

    private val _locationSettings = MutableStateFlow(locationRepository.locationSettings.value)
    val locationSettings: StateFlow<com.example.devpath.data.repository.LocationSettings> = _locationSettings.asStateFlow()

    private val _friends = MutableStateFlow<List<UserProfile>>(emptyList())
    val friends: StateFlow<List<UserProfile>> = _friends.asStateFlow()

    private val _currentUserProfile = MutableStateFlow<UserProfile?>(null)
    val currentUserProfile: StateFlow<UserProfile?> = _currentUserProfile.asStateFlow()

    private val _nearbyMarkers = MutableStateFlow<List<MapMarker>>(emptyList())
    val nearbyMarkers: StateFlow<List<MapMarker>> = _nearbyMarkers.asStateFlow()

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    init {
        // 🔹 ЭТАП 1: Локация (высокий приоритет)
        viewModelScope.launch {
            locationRepository.currentLocation.collect { location ->
                _currentLocation.value = location
                updateMyLocation()
            }
        }

        // 🔹 ЭТАП 2: Друзья (средний приоритет)
        viewModelScope.launch {
            if (currentUserId.isNotEmpty()) {
                chatRepository.getFriends(currentUserId).collect { friendsList ->
                    _friends.value = friendsList
                    println("DEBUG: MapViewModel - друзья: ${friendsList.size}")
                }
            }
        }

        // 🔹 ЭТАП 3: Настройки (средний приоритет)
        viewModelScope.launch {
            locationRepository.locationSettings.collect { settings ->
                _locationSettings.value = settings
            }
        }

        // 🔹 ЭТАП 4: Профиль пользователя
        viewModelScope.launch {
            if (currentUserId.isNotEmpty()) {
                val profile = chatRepository.getUser(currentUserId)
                _currentUserProfile.value = profile
                println("DEBUG: MapViewModel - профиль: ${profile?.name}")
            }
        }

        // 🔹 ЭТАП 5: Загружаем nearby users
        viewModelScope.launch {
            delay(1000)
            loadNearbyUsers()
        }
    }

    fun startLocationUpdates() {
        viewModelScope.launch {
            locationRepository.startLocationUpdates()
        }
    }

    private suspend fun updateMyLocation() {
        if (currentUserId.isNotEmpty()) {
            val user = chatRepository.getUser(currentUserId)
            locationRepository.updateUserLocation(
                currentUserId,
                user?.name ?: "Пользователь",
                user?.avatarUrl
            )
        }
    }

    fun loadNearbyUsers() {
        viewModelScope.launch {
            if (currentUserId.isEmpty()) return@launch
            val friendsIds = _friends.value.map { it.userId }
            locationRepository.getNearbyUsers(
                currentUserId,
                _locationSettings.value.visibility,
                friendsIds
            ).collect { users ->
                _nearbyUsers.value = users
                println("DEBUG: MapViewModel - пользователей рядом: ${users.size}")
            }
        }
    }

    private var markersJob: Job? = null

    fun updateMarkerLocation(latitude: Double, longitude: Double) {
        markersJob?.cancel()
        markersJob = viewModelScope.launch {
            if (currentUserId.isEmpty()) return@launch
            eventsRepository.getNearbyMarkers(
                userId = currentUserId,
                latitude = latitude,
                longitude = longitude,
                maxRadiusMeters = 50000,
                limit = 50
            ).collect { markers ->
                _nearbyMarkers.value = markers
                println("DEBUG: MapViewModel - маркеров загружено: ${markers.size}")
            }
        }
    }

    // 🔹 СОЗДАНИЕ МАРКЕРА - ИСПОЛЬЗУЕМ EventsRepository
    suspend fun createMarker(marker: MapMarker): String {
        return try {
            println("DEBUG: MapViewModel - создание маркера: ${marker.title} (${marker.type})")
            val markerId = eventsRepository.createMarker(marker)
            println("DEBUG: MapViewModel - маркер создан с ID: $markerId")
            markerId
        } catch (e: Exception) {
            println("DEBUG: MapViewModel - ошибка создания маркера: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    // 🔹 ПРИСОЕДИНИТЬСЯ К МАРКЕРУ
    suspend fun joinMarker(markerId: String) {
        try {
            println("DEBUG: MapViewModel - присоединение к маркеру: $markerId")
            eventsRepository.joinMarker(markerId, currentUserId)
            println("DEBUG: MapViewModel - присоединились к маркеру")
        } catch (e: Exception) {
            println("DEBUG: MapViewModel - ошибка присоединения: ${e.message}")
            throw e
        }
    }

    // 🔹 ПОКИНУТЬ МАРКЕР
    suspend fun leaveMarker(markerId: String) {
        try {
            println("DEBUG: MapViewModel - выход из маркера: $markerId")
            eventsRepository.leaveMarker(markerId, currentUserId)
            println("DEBUG: MapViewModel - вышли из маркера")
        } catch (e: Exception) {
            println("DEBUG: MapViewModel - ошибка выхода: ${e.message}")
            throw e
        }
    }

    // 🔹 ПОЖАЛОВАТЬСЯ НА МАРКЕР
    suspend fun reportMarker(markerId: String, reason: String) {
        try {
            println("DEBUG: MapViewModel - жалоба на маркер: $markerId")
            eventsRepository.reportMarker(markerId, currentUserId, reason)
            println("DEBUG: MapViewModel - жалоба отправлена")
        } catch (e: Exception) {
            println("DEBUG: MapViewModel - ошибка жалобы: ${e.message}")
            throw e
        }
    }

    // 🔹 УВЕЛИЧИТЬ ПРОСМОТРЫ
    suspend fun incrementMarkerViews(markerId: String) {
        try {
            eventsRepository.incrementMarkerViews(markerId)
        } catch (e: Exception) {
            println("DEBUG: MapViewModel - ошибка просмотров: ${e.message}")
        }
    }

    // 🔹 ПОЛУЧИТЬ МАРКЕР
    suspend fun getMarker(markerId: String): MapMarker? {
        return eventsRepository.getMarker(markerId)
    }

    // 🔹 ПОЛУЧИТЬ ИЛИ СОЗДАТЬ ЧАТ
    suspend fun getOrCreatePersonalChat(userId1: String, userId2: String): String {
        return chatRepository.findOrCreatePersonalChat(userId1, userId2)
    }

    fun loadLocationSettings() {
        viewModelScope.launch {
            val settings = locationRepository.loadLocationSettings(currentUserId)
            _locationSettings.value = settings
        }
    }

    fun updateLocationSettings(settings: com.example.devpath.data.repository.LocationSettings) {
        viewModelScope.launch {
            locationRepository.saveLocationSettings(currentUserId, settings)
            _locationSettings.value = settings
        }
    }

    fun loadFriends() {
        viewModelScope.launch {
            if (currentUserId.isNotEmpty()) {
                chatRepository.getFriends(currentUserId).collect { friendsList ->
                    _friends.value = friendsList
                }
            }
        }
    }

    fun sendFriendRequest(toUserId: String) {
        viewModelScope.launch {
            chatRepository.sendFriendRequest(currentUserId, toUserId)
        }
    }

    suspend fun getUserProfile(userId: String): UserProfile? {
        return chatRepository.getUser(userId)
    }
}