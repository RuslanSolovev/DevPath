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

    fun getActiveAnnouncements(userId: String): Flow<List<Announcement>> = callbackFlow {
        val query = db.collection("announcements")
            .whereEqualTo("active", true)
            .orderBy("priority", com.google.firebase.firestore.Query.Direction.DESCENDING)
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

            val activeForUser = allAnnouncements.filter { !it.dismissedBy.contains(userId) }
            trySend(activeForUser)
        }

        awaitClose { subscription.remove() }
    }

    suspend fun createAnnouncement(
        title: String,
        message: String,
        ownerId: String,
        type: String = "info",
        priority: Int = 1,
        imageUrl: String? = null,
        actionUrl: String? = null,
        actionText: String? = null,
        expiresAt: com.google.firebase.Timestamp? = null
    ): Boolean {
        return try {
            val announcement = Announcement(
                title = title,
                message = message,
                createdAt = com.google.firebase.Timestamp.now(),
                createdBy = ownerId,
                active = true,
                dismissedBy = emptyList(),
                type = type,
                priority = priority,
                imageUrl = imageUrl,
                actionUrl = actionUrl,
                actionText = actionText,
                expiresAt = expiresAt
            )
            db.collection("announcements").add(announcement).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

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

    suspend fun deleteAnnouncement(announcementId: String): Boolean {
        return try {
            db.collection("announcements").document(announcementId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

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

    suspend fun updateAnnouncementPriority(announcementId: String, priority: Int): Boolean {
        return try {
            db.collection("announcements").document(announcementId)
                .update("priority", priority)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
}