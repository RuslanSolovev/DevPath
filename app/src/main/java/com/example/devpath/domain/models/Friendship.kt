package com.example.devpath.domain.models

import com.google.firebase.Timestamp

data class Friendship(
    val friendshipId: String = "",
    val userId1: String = "",
    val userId2: String = "",
    val createdAt: Timestamp = Timestamp.now()
)