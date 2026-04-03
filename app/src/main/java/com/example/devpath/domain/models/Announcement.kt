package com.example.devpath.domain.models

import com.google.firebase.Timestamp

data class Announcement(
    val announcementId: String = "",
    val title: String = "",
    val message: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val createdBy: String = "",
    val active: Boolean = true,  // ← было isActive, стало active
    val dismissedBy: List<String> = emptyList()
)