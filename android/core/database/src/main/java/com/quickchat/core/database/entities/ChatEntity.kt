package com.quickchat.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.quickchat.core.model.Chat
import com.quickchat.core.model.Message

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val recipientPhone: String,
    val displayName: String,
    val avatarUrl: String?,
    val isGroup: Boolean,
    val lastMessageId: String?,
    val unreadCount: Int,
    val isPinned: Boolean,
    val isMuted: Boolean,
    val isArchived: Boolean
) {
    fun toDomain(lastMessage: Message?): Chat = Chat(
        recipientPhone = recipientPhone,
        displayName = displayName,
        avatarUrl = avatarUrl,
        isGroup = isGroup,
        lastMessage = lastMessage,
        unreadCount = unreadCount,
        isPinned = isPinned,
        isMuted = isMuted,
        isArchived = isArchived
    )

    companion object {
        fun fromDomain(c: Chat): ChatEntity = ChatEntity(
            recipientPhone = c.recipientPhone,
            displayName = c.displayName,
            avatarUrl = c.avatarUrl,
            isGroup = c.isGroup,
            lastMessageId = c.lastMessage?.id,
            unreadCount = c.unreadCount,
            isPinned = c.isPinned,
            isMuted = c.isMuted,
            isArchived = c.isArchived
        )
    }
}
