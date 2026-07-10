package com.quickchat.core.database.dao

import androidx.room.*
import com.quickchat.core.database.entities.BlockedContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedContactDao {
    @Query("SELECT * FROM blocked_contacts")
    fun getAllBlockedContactsFlow(): Flow<List<BlockedContactEntity>>

    @Query("SELECT * FROM blocked_contacts")
    suspend fun getAllBlockedContacts(): List<BlockedContactEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedContact(contact: BlockedContactEntity)

    @Query("DELETE FROM blocked_contacts WHERE phone = :phone")
    suspend fun deleteBlockedContact(phone: String)

    @Query("SELECT EXISTS(SELECT 1 FROM blocked_contacts WHERE phone = :phone)")
    suspend fun isBlocked(phone: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM blocked_contacts WHERE phone = :phone)")
    fun isBlockedFlow(phone: String): Flow<Boolean>
}
