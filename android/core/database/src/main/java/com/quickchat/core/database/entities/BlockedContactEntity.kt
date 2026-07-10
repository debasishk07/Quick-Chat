package com.quickchat.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_contacts")
data class BlockedContactEntity(
    @PrimaryKey val phone: String,
    val displayName: String
)
