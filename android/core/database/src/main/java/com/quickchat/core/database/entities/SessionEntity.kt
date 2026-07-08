package com.quickchat.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crypto_sessions")
data class SessionEntity(
    @PrimaryKey val recipientPhone: String,
    val sessionJson: String // Serialized DoubleRatchetSession state (Base64 keys, sequence numbers)
)
