package com.quickchat.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.quickchat.core.database.entities.ScheduledMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduledMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduled(msg: ScheduledMessageEntity): Long

    @Query("SELECT * FROM scheduled_messages WHERE scheduledTime <= :now")
    suspend fun getPendingScheduledMessages(now: Long): List<ScheduledMessageEntity>

    @Query("DELETE FROM scheduled_messages WHERE id = :id")
    suspend fun deleteScheduled(id: Long)

    @Query("SELECT * FROM scheduled_messages WHERE recipientPhone = :phone ORDER BY scheduledTime ASC")
    fun getScheduledMessagesFlow(phone: String): Flow<List<ScheduledMessageEntity>>

    @Query("SELECT * FROM scheduled_messages ORDER BY scheduledTime ASC")
    suspend fun getAllScheduledMessages(): List<ScheduledMessageEntity>
}
