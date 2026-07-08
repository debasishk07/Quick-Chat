package com.quickchat.core.model

enum class StatusMediaType {
    TEXT, IMAGE, VIDEO
}

data class Status(
    val id: String,
    val senderPhone: String,
    val mediaUrl: String, // Server URL to encrypted ciphertext media file
    val caption: String? = null,
    val mediaType: StatusMediaType,
    val timestamp: Long,
    val expiresAt: Long,
    val views: List<StatusView> = emptyList()
)

data class StatusView(
    val statusId: String,
    val viewerPhone: String,
    val timestamp: Long
)

data class UserStatus(
    val user: User,
    val statuses: List<Status>
) {
    val hasUnviewedStatus: Boolean
        get() = statuses.isNotEmpty() // Can be calculated based on views list
}
