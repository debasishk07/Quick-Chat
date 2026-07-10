package com.quickchat.core.model

enum class MessageType {
    TEXT, IMAGE, VIDEO, VOICE, DOCUMENT, LOCATION, CONTACT, CALL_SIGNAL, CALL_LOG
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
    val expireAt: Long? = null
)
