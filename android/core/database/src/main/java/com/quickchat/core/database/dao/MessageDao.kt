package com.quickchat.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.quickchat.core.database.entities.MessageEntity
import com.quickchat.core.database.entities.MessageFtsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE senderPhone = :chatPartnerPhone OR recipientPhone = :chatPartnerPhone ORDER BY timestamp ASC")
    fun getMessagesFlow(chatPartnerPhone: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessage(messageId: String): MessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("UPDATE messages SET status = :status WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: String, status: String)

    @Query("SELECT * FROM messages WHERE plainText LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    suspend fun searchMessages(query: String): List<MessageEntity>

    @Query("UPDATE messages SET isStarred = :isStarred WHERE id = :messageId")
    suspend fun updateMessageStarred(messageId: String, isStarred: Boolean)

    @Query("SELECT * FROM messages WHERE (senderPhone = :chatPartnerPhone OR recipientPhone = :chatPartnerPhone) AND isStarred = 1 ORDER BY timestamp DESC")
    fun getStarredMessagesFlow(chatPartnerPhone: String): Flow<List<MessageEntity>>

    @Query("DELETE FROM messages WHERE expireAt IS NOT NULL AND expireAt <= :now")
    suspend fun deleteExpiredMessages(now: Long)

    @Query("DELETE FROM messages WHERE senderPhone = :chatPartnerPhone OR recipientPhone = :chatPartnerPhone")
    suspend fun deleteMessagesForChat(chatPartnerPhone: String)

    // FTS functions
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessageFts(messageFts: MessageFtsEntity)

    @Query("DELETE FROM messages_fts WHERE messageId = :messageId")
    suspend fun deleteMessageFts(messageId: String)

    @Query("DELETE FROM messages_fts WHERE chatPhone = :chatPhone")
    suspend fun deleteMessagesFtsForChat(chatPhone: String)

    @Query("DELETE FROM messages_fts WHERE messageId IN (SELECT id FROM messages WHERE expireAt IS NOT NULL AND expireAt <= :now)")
    suspend fun deleteExpiredMessagesFts(now: Long)

    @Query("""
        SELECT m.* FROM messages m 
        JOIN messages_fts f ON m.id = f.messageId 
        WHERE messages_fts MATCH :ftsQuery
        ORDER BY m.timestamp DESC
    """)
    suspend fun searchMessagesFts(ftsQuery: String): List<MessageEntity>
}
