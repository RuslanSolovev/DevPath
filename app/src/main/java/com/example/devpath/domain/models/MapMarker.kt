package com.example.devpath.domain.models

import com.google.firebase.Timestamp

// 🔹 Типы маркеров на карте
enum class MarkerType {
    ANNOUNCEMENT,  // 📢 Объявление (только чтение)
    EVENT,         // 🎉 Событие (можно участвовать)
    COMMUNITY,     // 👥 Сообщество (групповой чат)
    DISCUSSION     // 💬 Обсуждение (временный чат)
}

// 🔹 Модель маркера на карте
data class MapMarker(
    val id: String = "",
    val type: MarkerType = MarkerType.ANNOUNCEMENT,
    val title: String = "",
    val description: String = "",
    val createdBy: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val endsAt: Timestamp? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val visibility: String = "public",  // public, friends, private
    val participantLimit: Int? = null,
    val participants: List<String> = emptyList(),
    val chatId: String? = null,
    val views: Int = 0,
    val reports: Int = 0,
    val status: String = "active"  // active, completed, cancelled
) {
    // 🔹 Проверяем, истёк ли маркер
    val isExpired: Boolean
        get() = endsAt?.let { Timestamp.now() > it } ?: false

    // 🔹 Количество участников
    val participantCount: Int
        get() = participants.size

    // 🔹 Есть ли лимит и заполнен ли он
    val isFull: Boolean
        get() = participantLimit?.let { participants.size >= it } ?: false

    // 🔹 Участвует ли пользователь
    fun isParticipant(userId: String): Boolean = participants.contains(userId)

    // 🔹 Преобразование в Map для Firestore
    fun toMap(): Map<String, Any?> = mapOf(
        "type" to type.name,
        "title" to title,
        "description" to description,
        "createdBy" to createdBy,
        "createdAt" to createdAt,
        "endsAt" to endsAt,
        "latitude" to latitude,
        "longitude" to longitude,
        "visibility" to visibility,
        "participantLimit" to participantLimit,
        "participants" to participants,
        "chatId" to chatId,
        "views" to views,
        "reports" to reports,
        "status" to status
    )

    companion object {
        // 🔹 Создание из данных Firestore
        fun fromMap(id: String, data: Map<String, Any?>): MapMarker? {
            return try {
                MapMarker(
                    id = id,
                    type = try {
                        MarkerType.valueOf(data["type"] as String)
                    } catch (e: Exception) { MarkerType.ANNOUNCEMENT },
                    title = data["title"] as? String ?: "Без названия",
                    description = data["description"] as? String ?: "",
                    createdBy = data["createdBy"] as? String ?: "",
                    createdAt = data["createdAt"] as? Timestamp ?: Timestamp.now(),
                    endsAt = data["endsAt"] as? Timestamp,
                    latitude = (data["latitude"] as? Number)?.toDouble() ?: 0.0,
                    longitude = (data["longitude"] as? Number)?.toDouble() ?: 0.0,
                    visibility = data["visibility"] as? String ?: "public",
                    participantLimit = (data["participantLimit"] as? Number)?.toInt(),
                    participants = (data["participants"] as? List<String>) ?: emptyList(),
                    chatId = data["chatId"] as? String,
                    views = (data["views"] as? Number)?.toInt() ?: 0,
                    reports = (data["reports"] as? Number)?.toInt() ?: 0,
                    status = data["status"] as? String ?: "active"
                )
            } catch (e: Exception) {
                println("DEBUG: Error parsing marker from map: ${e.message}")
                null
            }
        }
    }
}