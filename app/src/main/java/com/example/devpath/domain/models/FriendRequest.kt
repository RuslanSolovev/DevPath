package com.example.devpath.domain.models

data class FriendRequest(
    val requestId: String = "",
    val fromUserId: String = "",
    val toUserId: String = "",
    val status: String = "pending", // pending, accepted, rejected
    val createdAt: Long = System.currentTimeMillis()  // ✅ Long вместо Timestamp
)