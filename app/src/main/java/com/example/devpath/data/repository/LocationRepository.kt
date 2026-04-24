package com.example.devpath.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    @ApplicationContext private val context: Context
) {
    private val db: FirebaseFirestore = Firebase.firestore
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    private val _currentLocation = MutableStateFlow<android.location.Location?>(null)
    val currentLocation: StateFlow<android.location.Location?> = _currentLocation

    private val _locationSettings = MutableStateFlow<LocationSettings>(LocationSettings())
    val locationSettings: StateFlow<LocationSettings> = _locationSettings

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
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
        val settings = _locationSettings.value

        val data = mapOf(
            "latitude" to location.latitude,
            "longitude" to location.longitude,
            "lastUpdated" to System.currentTimeMillis(),
            "name" to name,
            "avatarUrl" to avatarUrl,
            "isOnline" to true,
            "visibility" to settings.visibility,
            "selectedFriends" to settings.selectedFriends
        )
        db.collection("user_locations").document(userId).set(data).await()
    }

    fun getNearbyUsers(userId: String, visibility: String, friendsList: List<String>): Flow<List<UserLocation>> = callbackFlow {
        val subscription = db.collection("user_locations")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val users = snapshot?.documents?.mapNotNull { doc ->
                    val uid = doc.id
                    if (uid == userId) return@mapNotNull null

                    val otherVisibility = doc.getString("visibility") ?: "all"
                    val otherSelectedFriends = doc.get("selectedFriends") as? List<String> ?: emptyList()

                    when (otherVisibility) {
                        "nobody" -> return@mapNotNull null
                        "friends" -> {
                            if (!otherSelectedFriends.contains(userId)) {
                                return@mapNotNull null
                            }
                        }
                        "all" -> { }
                    }

                    UserLocation(
                        userId = uid,
                        name = doc.getString("name") ?: "",
                        avatarUrl = doc.getString("avatarUrl"),
                        latitude = doc.getDouble("latitude") ?: 0.0,
                        longitude = doc.getDouble("longitude") ?: 0.0,
                        isOnline = System.currentTimeMillis() - (doc.getLong("lastUpdated") ?: 0) < 120_000,
                        lastUpdated = doc.getLong("lastUpdated") ?: 0
                    )
                } ?: emptyList()

                println("DEBUG: getNearbyUsers - найдено ${users.size} пользователей")
                trySend(users)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun saveLocationSettings(userId: String, settings: LocationSettings) {
        _locationSettings.value = settings
        db.collection("location_settings").document(userId).set(settings).await()
        db.collection("user_locations").document(userId).update(
            mapOf(
                "visibility" to settings.visibility,
                "selectedFriends" to settings.selectedFriends
            )
        ).await()
    }

    suspend fun loadLocationSettings(userId: String): LocationSettings {
        return try {
            val doc = db.collection("location_settings").document(userId).get().await()
            doc.toObject(LocationSettings::class.java) ?: LocationSettings()
        } catch (e: Exception) {
            LocationSettings()
        }.also { _locationSettings.value = it }
    }
}