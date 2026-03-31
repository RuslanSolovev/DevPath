package com.example.devpath.domain.models

import com.google.firebase.Timestamp

data class FriendRequest(
    val requestId: String = "",
    val fromUserId: String = "",
    val toUserId: String = "",
    val status: String = "pending", // pending, accepted, rejected
    val createdAt: Timestamp = Timestamp.now()
)