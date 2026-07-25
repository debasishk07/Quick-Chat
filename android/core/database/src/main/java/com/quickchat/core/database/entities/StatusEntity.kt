package com.quickchat.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.quickchat.core.model.Status
import com.quickchat.core.model.StatusMediaType
import com.quickchat.core.model.StatusView

@Entity(tableName = "statuses")
data class StatusEntity(
    @PrimaryKey val id: String,
    val senderPhone: String,
    val mediaUrl: String,
    val caption: String?,
    val mediaType: String,
    val timestamp: Long,
    val expiresAt: Long,
    val viewersJson: String // Serialized JSON array of StatusView
) {
    fun toDomain(viewers: List<StatusView>): Status = Status(
        id = id,
        senderPhone = senderPhone,
        mediaUrl = mediaUrl,
        caption = caption,
        mediaType = StatusMediaType.valueOf(mediaType),
        timestamp = timestamp,
        expiresAt = expiresAt,
        views = viewers
    )

    companion object {
        fun fromDomain(s: Status, viewersJson: String): StatusEntity = StatusEntity(
            id = s.id,
            senderPhone = s.senderPhone,
            mediaUrl = s.mediaUrl,
            caption = s.caption,
            mediaType = s.mediaType.name,
            timestamp = s.timestamp,
            expiresAt = s.expiresAt,
            viewersJson = viewersJson
        )
    }
}

