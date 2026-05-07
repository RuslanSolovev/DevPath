package com.example.devpath.data.repository

import com.example.devpath.domain.models.MapMarker
import com.example.devpath.domain.models.MarkerType
import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventsRepository @Inject constructor(
    private val ydbRepository: YdbRepository
) {
    private val markersTable = "markers_doc"
    private val scope = CoroutineScope(Dispatchers.IO)

    suspend fun initMarkersTable() {
        // Используем публичный метод initTable из YdbRepository (нужно сделать его публичным)
        // Или вызываем через создание таблицы напрямую
        val body = JSONObject().apply {
            put("TableName", markersTable)
            put("KeySchema", JSONArray().apply {
                put(JSONObject().apply {
                    put("AttributeName", "marker_id")
                    put("KeyType", "HASH")
                })
            })
            put("AttributeDefinitions", JSONArray().apply {
                put(JSONObject().apply {
                    put("AttributeName", "marker_id")
                    put("AttributeType", "S")
                })
            })
        }
        ydbRepository.executeSignedRequest("CreateTable", body)
    }

    fun getNearbyMarkers(
        userId: String,
        latitude: Double,
        longitude: Double,
        maxRadiusMeters: Int = 50000,
        limit: Int = 50
    ): Flow<List<MapMarker>> = callbackFlow {
        val job = scope.launch {
            while (true) {
                try {
                    val body = JSONObject().apply {
                        put("TableName", markersTable)
                        put("FilterExpression", "#status = :active")
                        put("ExpressionAttributeNames", JSONObject().apply { put("#status", "status") })
                        put("ExpressionAttributeValues", JSONObject().apply {
                            put(":active", JSONObject().put("S", "active"))
                        })
                        put("Limit", limit)
                    }
                    val result = ydbRepository.executeSignedRequest("Scan", body)
                    val items = result?.optJSONArray("Items") ?: JSONArray()

                    val markers = (0 until items.length()).mapNotNull { i ->
                        val json = items.getJSONObject(i)
                        val marker = mapJsonToMarker(json)
                        if (marker != null && !marker.isExpired) {
                            when (marker.visibility) {
                                "private" -> if (marker.createdBy == userId) marker else null
                                "friends" -> if (marker.createdBy == userId || marker.participants.contains(userId)) marker else null
                                "public" -> marker
                                else -> null
                            }
                        } else null
                    }
                    trySend(markers)
                } catch (e: Exception) {
                    println("DEBUG: EventsRepository error: ${e.message}")
                }
                delay(10000)
            }
        }
        awaitClose { job.cancel() }
    }

    suspend fun createMarker(marker: MapMarker): String {
        val markerId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val participantsArray = JSONArray()
        participantsArray.put(marker.createdBy)

        val item = JSONObject().apply {
            put("marker_id", JSONObject().put("S", markerId))
            put("type", JSONObject().put("S", marker.type.name))
            put("title", JSONObject().put("S", marker.title))
            put("description", JSONObject().put("S", marker.description))
            put("created_by", JSONObject().put("S", marker.createdBy))
            put("created_at", JSONObject().put("S", now.toString()))
            put("latitude", JSONObject().put("N", marker.latitude.toString()))
            put("longitude", JSONObject().put("N", marker.longitude.toString()))
            put("visibility", JSONObject().put("S", marker.visibility))
            put("participants", JSONObject().put("SS", participantsArray))
            put("views", JSONObject().put("N", "0"))
            put("reports", JSONObject().put("N", "0"))
            put("status", JSONObject().put("S", "active"))
            marker.endsAt?.let { put("ends_at", JSONObject().put("S", it.seconds.toString())) }
            marker.participantLimit?.let { put("participant_limit", JSONObject().put("N", it.toString())) }
        }

        val body = JSONObject().apply {
            put("TableName", markersTable)
            put("Item", item)
        }
        ydbRepository.executeSignedRequest("PutItem", body)

        if (marker.type == MarkerType.COMMUNITY || marker.type == MarkerType.DISCUSSION) {
            val chatId = UUID.randomUUID().toString()
            ydbRepository.createChat(chatId, "community", listOf(marker.createdBy), marker.title, marker.createdBy)
            ydbRepository.executeSignedRequest("UpdateItem", JSONObject().apply {
                put("TableName", markersTable)
                put("Key", JSONObject().apply { put("marker_id", JSONObject().put("S", markerId)) })
                put("UpdateExpression", "SET chat_id = :chatId")
                put("ExpressionAttributeValues", JSONObject().apply { put(":chatId", JSONObject().put("S", chatId)) })
            })
        }

        return markerId
    }

    suspend fun joinMarker(markerId: String, userId: String) {
        ydbRepository.executeSignedRequest("UpdateItem", JSONObject().apply {
            put("TableName", markersTable)
            put("Key", JSONObject().apply { put("marker_id", JSONObject().put("S", markerId)) })
            put("UpdateExpression", "ADD participants :userId")
            put("ExpressionAttributeValues", JSONObject().apply { put(":userId", JSONObject().put("SS", JSONArray(listOf(userId)))) })
        })
    }

    suspend fun leaveMarker(markerId: String, userId: String) {
        ydbRepository.executeSignedRequest("UpdateItem", JSONObject().apply {
            put("TableName", markersTable)
            put("Key", JSONObject().apply { put("marker_id", JSONObject().put("S", markerId)) })
            put("UpdateExpression", "DELETE participants :userId")
            put("ExpressionAttributeValues", JSONObject().apply { put(":userId", JSONObject().put("SS", JSONArray(listOf(userId)))) })
        })
    }

    suspend fun reportMarker(markerId: String, userId: String, reason: String) {
        ydbRepository.executeSignedRequest("UpdateItem", JSONObject().apply {
            put("TableName", markersTable)
            put("Key", JSONObject().apply { put("marker_id", JSONObject().put("S", markerId)) })
            put("UpdateExpression", "ADD reports :one")
            put("ExpressionAttributeValues", JSONObject().apply { put(":one", JSONObject().put("N", "1")) })
        })
    }

    suspend fun incrementMarkerViews(markerId: String) {
        ydbRepository.executeSignedRequest("UpdateItem", JSONObject().apply {
            put("TableName", markersTable)
            put("Key", JSONObject().apply { put("marker_id", JSONObject().put("S", markerId)) })
            put("UpdateExpression", "ADD views :one")
            put("ExpressionAttributeValues", JSONObject().apply { put(":one", JSONObject().put("N", "1")) })
        })
    }

    suspend fun getMarker(markerId: String): MapMarker? {
        val key = JSONObject().apply { put("marker_id", JSONObject().put("S", markerId)) }
        val body = JSONObject().apply { put("TableName", markersTable); put("Key", key) }
        val result = ydbRepository.executeSignedRequest("GetItem", body)
        val item = result?.optJSONObject("Item") ?: return null
        return mapJsonToMarker(item)
    }

    private fun mapJsonToMarker(json: JSONObject): MapMarker? {
        return try {
            MapMarker(
                id = json.optJSONObject("marker_id")?.optString("S") ?: return null,
                type = try { MarkerType.valueOf(json.optJSONObject("type")?.optString("S") ?: "ANNOUNCEMENT") } catch (e: Exception) { MarkerType.ANNOUNCEMENT },
                title = json.optJSONObject("title")?.optString("S") ?: "",
                description = json.optJSONObject("description")?.optString("S") ?: "",
                createdBy = json.optJSONObject("created_by")?.optString("S") ?: "",
                createdAt = json.optJSONObject("created_at")?.optString("S")?.toLongOrNull()?.let { Timestamp.now() } ?: Timestamp.now(),
                endsAt = json.optJSONObject("ends_at")?.optString("S")?.toLongOrNull()?.let { Timestamp(it, 0) },
                latitude = json.optJSONObject("latitude")?.optString("N")?.toDoubleOrNull() ?: 0.0,
                longitude = json.optJSONObject("longitude")?.optString("N")?.toDoubleOrNull() ?: 0.0,
                visibility = json.optJSONObject("visibility")?.optString("S") ?: "public",
                participants = (json.optJSONObject("participants")?.optJSONArray("SS") ?: JSONArray()).let { arr ->
                    (0 until arr.length()).map { arr.getString(it) }
                },
                views = json.optJSONObject("views")?.optString("N")?.toIntOrNull() ?: 0,
                status = json.optJSONObject("status")?.optString("S") ?: "active",
                participantLimit = json.optJSONObject("participant_limit")?.optString("N")?.toIntOrNull(),
                chatId = json.optJSONObject("chat_id")?.optString("S")
            )
        } catch (e: Exception) {
            println("DEBUG: Error parsing marker: ${e.message}")
            null
        }
    }
}