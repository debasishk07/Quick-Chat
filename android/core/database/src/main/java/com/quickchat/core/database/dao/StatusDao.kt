package com.quickchat.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.quickchat.core.database.entities.StatusEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StatusDao {
    @Query("SELECT * FROM statuses ORDER BY timestamp DESC")
    fun getAllStatusesFlow(): Flow<List<StatusEntity>>

    @Query("SELECT * FROM statuses WHERE id = :statusId")
    suspend fun getStatus(statusId: String): StatusEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatus(status: StatusEntity)

    @Query("DELETE FROM statuses WHERE expiresAt < :now")
    suspend fun purgeExpiredStatuses(now: Long)

    @Query("DELETE FROM statuses")
    suspend fun clear()
}
