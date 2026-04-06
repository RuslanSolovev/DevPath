package com.example.devpath.domain.models

import com.google.firebase.Timestamp

data class Announcement(
    val announcementId: String = "",
    val title: String = "",
    val message: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val createdBy: String = "",
    val active: Boolean = true,
    val dismissedBy: List<String> = emptyList(),
    val type: String = "info", // info, warning, success, event, update
    val priority: Int = 1, // 1-5, где 5 - самый высокий приоритет
    val imageUrl: String? = null,
    val actionUrl: String? = null,
    val actionText: String? = null,
    val expiresAt: Timestamp? = null
)