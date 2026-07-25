package com.quickchat.core.model

enum class MessageType {
    TEXT, IMAGE, VIDEO, VOICE, DOCUMENT, LOCATION, CONTACT, CALL_SIGNAL, CALL_LOG, SYSTEM
}

enum class MessageStatus {
    SENDING, SENT, DELIVERED, READ
}

data class Message(
    val id: String,
    val senderPhone: String,
    val recipientPhone: String,
    val isGroup: Boolean,
    val ciphertext: String,
    val iv: String,
    val ephemeralPublicKey: String? = null,
    val messageType: MessageType,
    val timestamp: Long,
    val status: MessageStatus,
    val plainText: String? = null, // ONLY populated locally after decryption
    val isStarred: Boolean = false,
    val expireAt: Long? = null,
    val reaction: String? = null,
    val playbackSpeed: Float = 1.0f,
    val isEdited: Boolean = false,
    val pinnedAt: Long? = null,
    val isDeleted: Boolean = false,
    val publicId: String? = null,
    val mediaDuration: Double? = null,
    val mediaFormat: String? = null,
    val fileSize: Long? = null
)
