package com.quickchat.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.quickchat.core.database.entities.PhoneContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhoneContactDao {
    @Query("SELECT * FROM phone_contacts WHERE phone = :phone")
    suspend fun getPhoneContact(phone: String): PhoneContactEntity?

    @Query("SELECT * FROM phone_contacts WHERE phone = :phone")
    fun getPhoneContactFlow(phone: String): Flow<PhoneContactEntity?>

    @Query("SELECT * FROM phone_contacts")
    fun getAllPhoneContactsFlow(): Flow<List<PhoneContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePhoneContact(contact: PhoneContactEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePhoneContacts(contacts: List<PhoneContactEntity>)
}
