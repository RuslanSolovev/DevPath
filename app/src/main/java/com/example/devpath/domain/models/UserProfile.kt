package com.example.devpath.domain.models

import com.google.firebase.Timestamp

data class UserProfile(
    val userId: String = "",
    val name: String = "",
    val nameLowercase: String = "",      // ← добавить
    val email: String = "",
    val emailLowercase: String = "",     // ← добавить
    val avatarUrl: String? = null,
    val online: Boolean = false,
    val lastSeen: Timestamp = Timestamp.now(),
    val lastActiveInApp: Timestamp = Timestamp.now(),
    val createdAt: Timestamp = Timestamp.now()
)