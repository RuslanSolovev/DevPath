package com.example.devpath.data.repository

import com.example.devpath.domain.models.MapMarker
import com.example.devpath.domain.models.MarkerType
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventsRepository @Inject constructor() {
    private val db: FirebaseFirestore = Firebase.firestore

    // 🔹 ИСПРАВЛЕННЫЙ МЕТОД - БЕЗ orderBy и limit
    fun getNearbyMarkers(
        userId: String,
        latitude: Double,
        longitude: Double,
        maxRadiusMeters: Int = 50000,
        limit: Int = 50
    ): Flow<List<MapMarker>> = callbackFlow {
        println("DEBUG: EventsRepository - starting markers listener")

        val subscription = db.collection("markers")
            .whereEqualTo("status", "active")
            // 🔹 УБРАЛИ orderBy и limit чтобы не требовался составной индекс
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("DEBUG: EventsRepository - markers error: ${error.message}")
                    // Не закрываем flow при ошибке, просто логируем
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val markers = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        val marker = MapMarker.fromMap(doc.id, data) ?: return@mapNotNull null

                        // Фильтрация по видимости
                        when (marker.visibility) {
                            "private" -> if (marker.createdBy != userId) return@mapNotNull null
                            "friends" -> {
                                if (marker.createdBy != userId &&
                                    !marker.participants.contains(userId)) {
                                    return@mapNotNull null
                                }
                            }
                            "public" -> { /* все видят */ }
                        }

                        // Фильтрация по расстоянию (временно отключена для теста)
                        // val distance = calculateDistance(latitude, longitude, marker.latitude, marker.longitude)
                        // if (distance > maxRadiusMeters) return@mapNotNull null

                        // Пропускаем истёкшие
                        if (marker.isExpired) return@mapNotNull null

                        marker
                    } catch (e: Exception) {
                        println("DEBUG: EventsRepository - error parsing marker ${doc.id}: ${e.message}")
                        null
                    }
                } ?: emptyList()

                println("DEBUG: EventsRepository - found ${markers.size} markers")
                trySend(markers)
            }

        awaitClose {
            subscription.remove()
            println("DEBUG: EventsRepository - markers listener removed")
        }
    }

    // 🔹 Создание маркера
    suspend fun createMarker(marker: MapMarker): String {
        return try {
            println("DEBUG: EventsRepository - creating marker: ${marker.title}")

            val markerData = hashMapOf<String, Any>(
                "type" to marker.type.name,
                "title" to marker.title,
                "description" to marker.description,
                "createdBy" to marker.createdBy,
                "createdAt" to com.google.firebase.Timestamp.now(),
                "latitude" to marker.latitude,
                "longitude" to marker.longitude,
                "visibility" to marker.visibility,
                "participants" to listOf(marker.createdBy),
                "views" to 0,
                "reports" to 0,
                "status" to "active"
            )

            marker.endsAt?.let { markerData["endsAt"] = it }
            marker.participantLimit?.let { markerData["participantLimit"] = it }

            val docRef = db.collection("markers").add(markerData).await()
            val markerId = docRef.id
            println("DEBUG: EventsRepository - marker created with ID: $markerId")

            // Создаем чат для COMMUNITY и DISCUSSION
            if (marker.type == MarkerType.COMMUNITY || marker.type == MarkerType.DISCUSSION) {
                val chatId = createChatForMarker(markerId, marker.title, marker.createdBy, marker.type)
                db.collection("markers").document(markerId)
                    .update("chatId", chatId).await()
                println("DEBUG: EventsRepository - chat created: $chatId")
            }

            markerId
        } catch (e: Exception) {
            println("DEBUG: EventsRepository - error creating marker: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    private suspend fun createChatForMarker(
        markerId: String,
        title: String,
        createdBy: String,
        type: MarkerType
    ): String {
        val chatType = if (type == MarkerType.COMMUNITY) "community" else "discussion"
        val chatData = mapOf(
            "type" to chatType,
            "name" to "${if (type == MarkerType.COMMUNITY) "👥" else "💬"} $title",
            "participants" to listOf(createdBy),
            "lastMessage" to "",
            "lastMessageSender" to "",
            "lastMessageTime" to com.google.firebase.Timestamp.now(),
            "createdAt" to com.google.firebase.Timestamp.now(),
            "markerId" to markerId,
            "createdBy" to createdBy
        )
        return db.collection("chats").add(chatData).await().id
    }

    suspend fun joinMarker(markerId: String, userId: String) {
        db.collection("markers").document(markerId)
            .update("participants", FieldValue.arrayUnion(userId))
            .await()
    }

    suspend fun leaveMarker(markerId: String, userId: String) {
        db.collection("markers").document(markerId)
            .update("participants", FieldValue.arrayRemove(userId))
            .await()
    }

    suspend fun reportMarker(markerId: String, userId: String, reason: String) {
        db.collection("markers").document(markerId)
            .collection("reports")
            .add(mapOf(
                "reportedBy" to userId,
                "reason" to reason,
                "timestamp" to com.google.firebase.Timestamp.now()
            ))
            .await()

        db.collection("markers").document(markerId)
            .update("reports", FieldValue.increment(1))
            .await()
    }

    suspend fun incrementMarkerViews(markerId: String) {
        db.collection("markers").document(markerId)
            .update("views", FieldValue.increment(1))
            .await()
    }

    suspend fun getMarker(markerId: String): MapMarker? {
        return try {
            val doc = db.collection("markers").document(markerId).get().await()
            MapMarker.fromMap(doc.id, doc.data ?: emptyMap())
        } catch (e: Exception) {
            println("DEBUG: EventsRepository - error getting marker: ${e.message}")
            null
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
        val earthRadius = 6371000
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return (earthRadius * c).toInt()
    }
}