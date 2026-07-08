package com.quickchat.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.quickchat.core.database.entities.OutboxEntity
import com.quickchat.core.database.entities.SessionEntity

@Dao
interface SessionDao {
    @Query("SELECT * FROM crypto_sessions WHERE recipientPhone = :recipientPhone")
    suspend fun getSession(recipientPhone: String): SessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    @Query("DELETE FROM crypto_sessions WHERE recipientPhone = :recipientPhone")
    suspend fun deleteSession(recipientPhone: String)
}

@Dao
interface OutboxDao {
    @Query("SELECT * FROM outbox_queue ORDER BY timestamp ASC")
    suspend fun getAllQueuedMessages(): List<OutboxEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueueMessage(msg: OutboxEntity)

    @Query("DELETE FROM outbox_queue WHERE id = :id")
    suspend fun dequeueMessage(id: String)
}
