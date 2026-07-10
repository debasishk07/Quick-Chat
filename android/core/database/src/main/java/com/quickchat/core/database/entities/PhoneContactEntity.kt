package com.quickchat.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "phone_contacts")
data class PhoneContactEntity(
    @PrimaryKey val phone: String,
    val contactName: String
)
