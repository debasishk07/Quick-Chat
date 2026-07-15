package com.quickchat.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.quickchat.core.model.Message
import com.quickchat.core.model.MessageStatus
import com.quickchat.core.model.MessageType

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val senderPhone: String,
    val recipientPhone: String,
    val isGroup: Boolean,
    val ciphertext: String,
    val iv: String,
    val ephemeralPublicKey: String?,
    val messageType: String, // mapped from enum Name
    val timestamp: Long,
    val status: String,      // mapped from enum Name
    val plainText: String?,   // ONLY local decrypted text
    val isStarred: Boolean = false,
    val expireAt: Long? = null,
    val reaction: String? = null,
    val playbackSpeed: Float = 1.0f,
    val isEdited: Boolean = false,
    val pinnedAt: Long? = null
) {
    fun toDomain(): Message = Message(
        id = id,
        senderPhone = senderPhone,
        recipientPhone = recipientPhone,
        isGroup = isGroup,
        ciphertext = ciphertext,
        iv = iv,
        ephemeralPublicKey = ephemeralPublicKey,
        messageType = MessageType.valueOf(messageType),
        timestamp = timestamp,
        status = MessageStatus.valueOf(status),
        plainText = plainText,
        isStarred = isStarred,
        expireAt = expireAt,
        reaction = reaction,
        playbackSpeed = playbackSpeed,
        isEdited = isEdited,
        pinnedAt = pinnedAt
    )

    companion object {
        fun fromDomain(m: Message): MessageEntity = MessageEntity(
            id = m.id,
            senderPhone = m.senderPhone,
            recipientPhone = m.recipientPhone,
            isGroup = m.isGroup,
            ciphertext = m.ciphertext,
            iv = m.iv,
            ephemeralPublicKey = m.ephemeralPublicKey,
            messageType = m.messageType.name,
            timestamp = m.timestamp,
            status = m.status.name,
            plainText = m.plainText,
            isStarred = m.isStarred,
            expireAt = m.expireAt,
            reaction = m.reaction,
            playbackSpeed = m.playbackSpeed,
            isEdited = m.isEdited,
            pinnedAt = m.pinnedAt
        )
    }
}
