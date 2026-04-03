package com.example.devpath.data.repository

import com.example.devpath.domain.models.Announcement
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnnouncementRepository @Inject constructor() {
    private val db: FirebaseFirestore = Firebase.firestore

    // Получить активные объявления (не закрытые пользователем)
    fun getActiveAnnouncements(userId: String): Flow<List<Announcement>> = callbackFlow {
        val query = db.collection("announcements")
            .whereEqualTo("active", true)  // ← было "isActive", стало "active"
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)

        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val allAnnouncements = snapshot?.documents?.mapNotNull { doc ->
                val announcement = doc.toObject(Announcement::class.java)
                announcement?.copy(announcementId = doc.id)
            } ?: emptyList()

            // Фильтруем только те, которые пользователь не закрыл
            val activeForUser = allAnnouncements.filter { !it.dismissedBy.contains(userId) }
            trySend(activeForUser)
        }

        awaitClose { subscription.remove() }
    }

    // Создать объявление (только для владельца)
    suspend fun createAnnouncement(title: String, message: String, ownerId: String): Boolean {
        return try {
            val announcement = Announcement(
                title = title,
                message = message,
                createdAt = com.google.firebase.Timestamp.now(),
                createdBy = ownerId,
                active = true,  // ← было isActive, стало active
                dismissedBy = emptyList()
            )
            db.collection("announcements").add(announcement).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Закрыть объявление (добавить пользователя в dismissedBy)
    suspend fun dismissAnnouncement(announcementId: String, userId: String): Boolean {
        return try {
            db.collection("announcements").document(announcementId)
                .update("dismissedBy", com.google.firebase.firestore.FieldValue.arrayUnion(userId))
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Удалить объявление (для владельца)
    suspend fun deleteAnnouncement(announcementId: String): Boolean {
        return try {
            db.collection("announcements").document(announcementId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Получить все объявления (для владельца)
    suspend fun getAllAnnouncements(): List<Announcement> {
        return try {
            val snapshot = db.collection("announcements")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                val announcement = doc.toObject(Announcement::class.java)
                announcement?.copy(announcementId = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}