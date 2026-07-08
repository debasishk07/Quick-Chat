package com.quickchat.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.quickchat.core.database.entities.ChatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chats ORDER BY isPinned DESC, lastMessageId DESC")
    fun getAllChatsFlow(): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chats WHERE recipientPhone = :recipientPhone")
    suspend fun getChat(recipientPhone: String): ChatEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateChat(chat: ChatEntity)

    @Query("UPDATE chats SET unreadCount = 0 WHERE recipientPhone = :recipientPhone")
    suspend fun clearUnreadCount(recipientPhone: String)

    @Query("DELETE FROM chats WHERE recipientPhone = :recipientPhone")
    suspend fun deleteChat(recipientPhone: String)
}
