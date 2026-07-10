package com.quickchat.core.database.entities

import androidx.room.Entity
import androidx.room.Fts4

@Fts4
@Entity(tableName = "messages_fts")
data class MessageFtsEntity(
    val messageId: String,
    val chatPhone: String,
    val plainText: String
)
