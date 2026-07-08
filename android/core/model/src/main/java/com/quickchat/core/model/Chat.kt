package com.quickchat.core.model

data class Chat(
    val recipientPhone: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val isGroup: Boolean = false,
    val lastMessage: Message? = null,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val isArchived: Boolean = false,
    val isTyping: Boolean = false // Volatile UI presence state
)
