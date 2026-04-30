package com.example.devpath.domain.models

data class UserProfile(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val online: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis(),
    val lastActiveInApp: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)