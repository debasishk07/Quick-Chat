package com.quickchat.core.model

data class User(
    val phone: String, // Treat as stable internal user ID (to prevent breaking E2EE routing)
    val phoneNumber: String? = null,
    val email: String? = null,
    val username: String? = null,
    val displayName: String,
    val avatarUrl: String? = null,
    val about: String? = null,
    val lastSeen: Long = 0L,
    val isOnline: Boolean = false,
    val authProviders: List<String> = emptyList(),
    val usernameSearchEnabled: Boolean = true
)
