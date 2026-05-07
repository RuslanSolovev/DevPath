package com.example.devpath.domain.models

data class UserProfile(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val avatarUrl: String? = null
)