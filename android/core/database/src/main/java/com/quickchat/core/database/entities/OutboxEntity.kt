package com.quickchat.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "outbox_queue")
data class OutboxEntity(
    @PrimaryKey val id: String,
    val recipientPhone: String,
    val isGroup: Boolean,
    val messageType: String,
    val plainText: String?,
    val mediaPath: String?, // Location of local file to upload
    val timestamp: Long
)
