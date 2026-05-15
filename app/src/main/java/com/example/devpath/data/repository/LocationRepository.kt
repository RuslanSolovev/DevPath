package com.example.devpath.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

data class UserLocation(
    val userId: String,
    val name: String,
    val avatarUrl: String?,
    val latitude: Double,
    val longitude: Double,
    val isOnline: Boolean,
    val lastUpdated: Long
)

data class LocationSettings(
    val visibility: String = "all",
    val selectedFriends: List<String> = emptyList()
)

@Singleton
class LocationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ydbRepository: YdbRepository
) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val locationsTable = "user_locations_doc"
    private val settingsTable = "location_settings_doc"

    private val _currentLocation = MutableStateFlow<android.location.Location?>(null)
    val currentLocation: StateFlow<android.location.Location?> = _currentLocation

    private val _locationSettings = MutableStateFlow<LocationSettings>(LocationSettings())
    val locationSettings: StateFlow<LocationSettings> = _locationSettings

    // Кэш настроек видимости с временем жизни
    private val visibilityCache = mutableMapOf<String, LocationSettings>()
    private var cacheTimestamp = 0L
    private val cacheLifetime = 30_000L // 30 секунд

    suspend fun initLocationTables() {
        ydbRepository.initTable(locationsTable, "user_id")
        ydbRepository.initTable(settingsTable, "user_id")
    }

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun startLocationUpdates() {
        if (!hasLocationPermission()) return
        try {
            val location = fusedLocationClient.lastLocation.await()
            _currentLocation.value = location
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    suspend fun updateUserLocation(userId: String, name: String, avatarUrl: String?) {
        val location = _currentLocation.value ?: return
        val now = System.currentTimeMillis()

        val item = JSONObject().apply {
            put("user_id", JSONObject().put("S", userId))
            put("name", JSONObject().put("S", name))
            put("avatar_url", JSONObject().put("S", avatarUrl ?: ""))
            put("latitude", JSONObject().put("N", location.latitude.toString()))
            put("longitude", JSONObject().put("N", location.longitude.toString()))
            put("last_updated", JSONObject().put("S", now.toString()))
            put("is_online", JSONObject().put("BOOL", true))
        }

        val body = JSONObject().apply {
            put("TableName", locationsTable)
            put("Item", item)
        }
        ydbRepository.executeSignedRequest("PutItem", body)
    }

    suspend fun getNearbyUsers(currentUserId: String): List<UserLocation> {
        val body = JSONObject().apply {
            put("TableName", locationsTable)
            put("Limit", 200)
        }
        val result = ydbRepository.executeSignedRequest("Scan", body)
        val items = result?.optJSONArray("Items") ?: JSONArray()

        val allUsers = (0 until items.length()).mapNotNull { i ->
            val json = items.getJSONObject(i)
            val lastUpdated = json.optJSONObject("last_updated")?.optString("S")?.toLongOrNull() ?: 0
            val userId = json.optJSONObject("user_id")?.optString("S") ?: return@mapNotNull null

            UserLocation(
                userId = userId,
                name = json.optJSONObject("name")?.optString("S") ?: "",
                avatarUrl = json.optJSONObject("avatar_url")?.optString("S")?.ifEmpty { null },
                latitude = json.optJSONObject("latitude")?.optString("N")?.toDoubleOrNull() ?: 0.0,
                longitude = json.optJSONObject("longitude")?.optString("N")?.toDoubleOrNull() ?: 0.0,
                isOnline = System.currentTimeMillis() - lastUpdated < 120_000,
                lastUpdated = lastUpdated
            )
        }

        // Инвалидируем кэш если прошло больше cacheLifetime
        if (System.currentTimeMillis() - cacheTimestamp > cacheLifetime) {
            visibilityCache.clear()
            cacheTimestamp = System.currentTimeMillis()
        }

        // Применяем фильтр видимости
        val filteredUsers = allUsers.filter { user ->
            if (user.userId == currentUserId) return@filter false // Не показываем себя

            // Получаем настройки из кэша или загружаем
            val settings = getLocationSettingsCached(user.userId)

            println("DEBUG: getNearbyUsers - userId=${user.userId}, visibility=${settings.visibility}, selectedFriends=${settings.selectedFriends}")

            when (settings.visibility) {
                "all" -> true
                "nobody" -> false
                "friends" -> {
                    // Проверяем, выбран ли currentUserId в selectedFriends этого пользователя
                    val isSelected = settings.selectedFriends.contains(currentUserId)
                    println("DEBUG: getNearbyUsers - $currentUserId в selectedFriends у ${user.userId} = $isSelected")
                    isSelected
                }
                else -> false
            }
        }

        println("DEBUG: getNearbyUsers - всего пользователей: ${allUsers.size}, после фильтра: ${filteredUsers.size}")

        return filteredUsers
    }

    // Получение настроек с кэшированием
    private suspend fun getLocationSettingsCached(userId: String): LocationSettings {
        // Проверяем кэш
        visibilityCache[userId]?.let {
            println("DEBUG: getLocationSettingsCached - из кэша: $userId -> ${it.visibility}")
            return it
        }

        // Загружаем из БД
        val settings = loadLocationSettingsInternal(userId)

        // Сохраняем в кэш
        visibilityCache[userId] = settings
        println("DEBUG: getLocationSettingsCached - загружено: $userId -> ${settings.visibility}")

        return settings
    }

    // Внутренний метод загрузки настроек (без обновления StateFlow)
    private suspend fun loadLocationSettingsInternal(userId: String): LocationSettings {
        val key = JSONObject().apply { put("user_id", JSONObject().put("S", userId)) }
        val body = JSONObject().apply { put("TableName", settingsTable); put("Key", key) }
        val result = ydbRepository.executeSignedRequest("GetItem", body)
        val item = result?.optJSONObject("Item")

        if (item == null) {
            println("DEBUG: loadLocationSettingsInternal - настройки не найдены для $userId")
            return LocationSettings()
        }

        val friendsArray = item.optJSONObject("selected_friends")?.optJSONArray("SS") ?: JSONArray()
        val friends = (0 until friendsArray.length()).map { friendsArray.getString(it) }

        return LocationSettings(
            visibility = item.optJSONObject("visibility")?.optString("S") ?: "all",
            selectedFriends = friends
        )
    }

    suspend fun saveLocationSettings(userId: String, settings: LocationSettings) {
        val selectedFriendsArray = JSONArray()
        settings.selectedFriends.forEach { selectedFriendsArray.put(it) }

        val item = JSONObject().apply {
            put("user_id", JSONObject().put("S", userId))
            put("visibility", JSONObject().put("S", settings.visibility))
            put("selected_friends", JSONObject().put("SS", selectedFriendsArray))
        }

        val body = JSONObject().apply {
            put("TableName", settingsTable)
            put("Item", item)
        }
        ydbRepository.executeSignedRequest("PutItem", body)

        // Обновляем кэш и StateFlow
        visibilityCache[userId] = settings
        _locationSettings.value = settings

        println("DEBUG: LocationRepository - сохранены настройки: visibility=${settings.visibility}")
    }

    // Публичный метод для загрузки настроек текущего пользователя
    suspend fun loadLocationSettings(userId: String): LocationSettings {
        val settings = loadLocationSettingsInternal(userId)

        // Обновляем кэш и StateFlow
        visibilityCache[userId] = settings
        _locationSettings.value = settings

        println("DEBUG: LocationRepository - загружены настройки для $userId: visibility=${settings.visibility}")

        return settings
    }

    // Принудительная очистка кэша
    fun clearVisibilityCache() {
        visibilityCache.clear()
        cacheTimestamp = 0
    }
}