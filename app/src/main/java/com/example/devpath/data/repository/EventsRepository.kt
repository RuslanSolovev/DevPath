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

    // Цвета для аватаров чатов
    private val eventColor = "#FF9800"      // Оранжевый для EVENT
    private val discussionColor = "#4CAF50"  // Зелёный для DISCUSSION

    init {
        // Запускаем периодическую очистку истёкших маркеров
        scope.launch {
            while (true) {
                delay(60_000) // Проверяем каждую минуту
                cleanExpiredMarkers()
            }
        }
    }

    /**
     * Автоматически удаляет истёкшие маркеры и их чаты
     */
    private suspend fun cleanExpiredMarkers() {
        try {
            val body = JSONObject().apply {
                put("TableName", markersTable)
                put("FilterExpression", "#status = :active")
                put("ExpressionAttributeNames", JSONObject().apply { put("#status", "status") })
                put("ExpressionAttributeValues", JSONObject().apply {
                    put(":active", JSONObject().put("S", "active"))
                })
                put("Limit", 100)
            }
            val result = ydbRepository.executeSignedRequest("Scan", body)
            val items = result?.optJSONArray("Items") ?: JSONArray()

            for (i in 0 until items.length()) {
                val json = items.getJSONObject(i)
                val marker = mapJsonToMarker(json)

                if (marker != null && marker.isExpired) {
                    println("DEBUG: cleanExpiredMarkers - удаляем истёкший маркер: ${marker.id}")

                    // Удаляем чат если есть
                    if (marker.chatId != null && marker.chatId.isNotEmpty() && marker.chatId != "auto") {
                        ydbRepository.deleteChat(marker.chatId)
                        println("DEBUG: cleanExpiredMarkers - чат удалён: ${marker.chatId}")
                    }

                    // Удаляем сам маркер
                    val deleteKey = JSONObject().apply {
                        put("marker_id", JSONObject().put("S", marker.id))
                    }
                    val deleteBody = JSONObject().apply {
                        put("TableName", markersTable)
                        put("Key", deleteKey)
                    }
                    ydbRepository.executeSignedRequest("DeleteItem", deleteBody)
                    println("DEBUG: cleanExpiredMarkers - маркер удалён: ${marker.id}")
                }
            }
        } catch (e: Exception) {
            println("ERROR: cleanExpiredMarkers - ${e.message}")
        }
    }

    suspend fun initMarkersTable() {
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
                            // Применяем фильтр видимости
                            when (marker.visibility) {
                                "public" -> marker // Видно всем
                                "friends" -> {
                                    // Видно друзьям или создателю
                                    if (marker.createdBy == userId) {
                                        marker
                                    } else {
                                        // Проверяем, друзья ли мы с создателем
                                        val creatorFriends = ydbRepository.getUserFriends(marker.createdBy)
                                        if (creatorFriends.contains(userId) || marker.participants.contains(userId)) {
                                            marker
                                        } else {
                                            null
                                        }
                                    }
                                }
                                "private" -> {
                                    // Видно только создателю
                                    if (marker.createdBy == userId) marker else null
                                }
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

        var chatId: String? = null
        var chatAvatarColor: String? = null

        if (marker.type == MarkerType.EVENT || marker.type == MarkerType.DISCUSSION) {
            chatId = UUID.randomUUID().toString()
            chatAvatarColor = when (marker.type) {
                MarkerType.EVENT -> eventColor
                MarkerType.DISCUSSION -> discussionColor
                else -> null
            }
        }

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
            chatId?.let { put("chat_id", JSONObject().put("S", it)) }
            chatAvatarColor?.let { put("chat_avatar_color", JSONObject().put("S", it)) }
        }

        val body = JSONObject().apply {
            put("TableName", markersTable)
            put("Item", item)
        }
        ydbRepository.executeSignedRequest("PutItem", body)

        if (chatId != null) {
            ydbRepository.createChat(
                chatId = chatId,
                type = "community",
                participants = listOf(marker.createdBy),
                name = marker.title,
                createdBy = marker.createdBy
            )

            val chatAvatar = JSONObject().apply {
                put("type", "marker")
                put("marker_type", marker.type.name)
                put("color", chatAvatarColor)
                put("emoji", when (marker.type) {
                    MarkerType.EVENT -> "🎉"
                    MarkerType.DISCUSSION -> "💬"
                    else -> "📢"
                })
            }

            ydbRepository.executeSignedRequest("UpdateItem", JSONObject().apply {
                put("TableName", "chats_doc")
                put("Key", JSONObject().apply { put("chat_id", JSONObject().put("S", chatId)) })
                put("UpdateExpression", "SET chat_avatar = :avatar")
                put("ExpressionAttributeValues", JSONObject().apply {
                    put(":avatar", JSONObject().put("S", chatAvatar.toString()))
                })
            })

            println("DEBUG: createMarker - создан чат: $chatId, цвет: $chatAvatarColor")
        }

        return markerId
    }

    suspend fun joinMarker(markerId: String, userId: String) {
        // 1. Добавляем пользователя в participants маркера
        ydbRepository.executeSignedRequest("UpdateItem", JSONObject().apply {
            put("TableName", markersTable)
            put("Key", JSONObject().apply { put("marker_id", JSONObject().put("S", markerId)) })
            put("UpdateExpression", "ADD participants :userId")
            put("ExpressionAttributeValues", JSONObject().apply {
                put(":userId", JSONObject().put("SS", JSONArray(listOf(userId))))
            })
        })

        // 2. Добавляем пользователя в чат маркера
        val marker = getMarker(markerId)
        if (marker != null && marker.chatId != null && marker.chatId.isNotEmpty()) {
            // Проверяем, существует ли чат
            val existingChat = ydbRepository.getChat(marker.chatId)
            if (existingChat != null) {
                // Чат существует — добавляем пользователя
                ydbRepository.executeSignedRequest("UpdateItem", JSONObject().apply {
                    put("TableName", "chats_doc")
                    put("Key", JSONObject().apply { put("chat_id", JSONObject().put("S", marker.chatId)) })
                    put("UpdateExpression", "ADD participants :userId")
                    put("ExpressionAttributeValues", JSONObject().apply {
                        put(":userId", JSONObject().put("SS", JSONArray(listOf(userId))))
                    })
                })
            } else {
                // Чат был удалён — пересоздаём его со всеми участниками
                ydbRepository.createChat(
                    chatId = marker.chatId,
                    type = "community",
                    participants = marker.participants,
                    name = marker.title,
                    createdBy = marker.createdBy
                )
                // Восстанавливаем аватар
                val chatAvatar = JSONObject().apply {
                    put("type", "marker")
                    put("marker_type", marker.type.name)
                    put("color", when (marker.type) {
                        MarkerType.EVENT -> eventColor
                        MarkerType.DISCUSSION -> discussionColor
                        else -> "#FF9800"
                    })
                    put("emoji", when (marker.type) {
                        MarkerType.EVENT -> "🎉"
                        MarkerType.DISCUSSION -> "💬"
                        else -> "📢"
                    })
                }
                ydbRepository.executeSignedRequest("UpdateItem", JSONObject().apply {
                    put("TableName", "chats_doc")
                    put("Key", JSONObject().apply { put("chat_id", JSONObject().put("S", marker.chatId)) })
                    put("UpdateExpression", "SET chat_avatar = :avatar")
                    put("ExpressionAttributeValues", JSONObject().apply {
                        put(":avatar", JSONObject().put("S", chatAvatar.toString()))
                    })
                })
                println("DEBUG: joinMarker - чат пересоздан: ${marker.chatId}")
            }
            println("DEBUG: joinMarker - пользователь $userId добавлен в чат ${marker.chatId}")
        }
    }

    suspend fun leaveMarker(markerId: String, userId: String) {
        ydbRepository.executeSignedRequest("UpdateItem", JSONObject().apply {
            put("TableName", markersTable)
            put("Key", JSONObject().apply { put("marker_id", JSONObject().put("S", markerId)) })
            put("UpdateExpression", "DELETE participants :userId")
            put("ExpressionAttributeValues", JSONObject().apply {
                put(":userId", JSONObject().put("SS", JSONArray(listOf(userId))))
            })
        })

        val marker = getMarker(markerId)
        if (marker != null && marker.chatId != null && marker.chatId.isNotEmpty()) {
            ydbRepository.executeSignedRequest("UpdateItem", JSONObject().apply {
                put("TableName", "chats_doc")
                put("Key", JSONObject().apply { put("chat_id", JSONObject().put("S", marker.chatId)) })
                put("UpdateExpression", "DELETE participants :userId")
                put("ExpressionAttributeValues", JSONObject().apply {
                    put(":userId", JSONObject().put("SS", JSONArray(listOf(userId))))
                })
            })
            println("DEBUG: leaveMarker - пользователь $userId удалён из чата ${marker.chatId}")
        }
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

    suspend fun deleteMarker(markerId: String, chatId: String?): Boolean {
        return try {
            println("DEBUG: deleteMarker - удаляем маркер: $markerId, чат: $chatId")

            if (chatId != null && chatId.isNotEmpty() && chatId != "auto") {
                println("DEBUG: deleteMarker - удаляем чат: $chatId")
                ydbRepository.deleteChat(chatId)
                println("DEBUG: deleteMarker - чат удалён")
            }

            val key = JSONObject().apply {
                put("marker_id", JSONObject().put("S", markerId))
            }
            val body = JSONObject().apply {
                put("TableName", markersTable)
                put("Key", key)
            }
            val result = ydbRepository.executeSignedRequest("DeleteItem", body)
            val success = result != null

            println("DEBUG: deleteMarker - метка удалена: $success")
            return success
        } catch (e: Exception) {
            println("ERROR: deleteMarker - ${e.message}")
            e.printStackTrace()
            false
        }
    }

    private fun mapJsonToMarker(json: JSONObject): MapMarker? {
        return try {
            MapMarker(
                id = json.optJSONObject("marker_id")?.optString("S") ?: return null,
                type = try {
                    MarkerType.valueOf(json.optJSONObject("type")?.optString("S") ?: "ANNOUNCEMENT")
                } catch (e: Exception) {
                    MarkerType.ANNOUNCEMENT
                },
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