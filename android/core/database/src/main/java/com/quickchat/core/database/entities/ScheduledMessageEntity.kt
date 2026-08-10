package com.quickchat.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scheduled_messages")
data class ScheduledMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipientPhone: String,
    val plainText: String,
    val messageType: String,
    val scheduledTime: Long
)
