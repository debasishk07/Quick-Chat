package com.quickchat.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.quickchat.core.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val phone: String,
    val phoneNumber: String?,
    val email: String?,
    val username: String?,
    val displayName: String,
    val avatarUrl: String?,
    val about: String?,
    val lastSeen: Long,
    val isOnline: Boolean
) {
    fun toDomain(): User = User(
        phone = phone,
        phoneNumber = phoneNumber,
        email = email,
        username = username,
        displayName = displayName,
        avatarUrl = avatarUrl,
        about = about,
        lastSeen = lastSeen,
        isOnline = isOnline
    )

    companion object {
        fun fromDomain(u: User): UserEntity = UserEntity(
            phone = u.phone,
            phoneNumber = u.phoneNumber,
            email = u.email,
            username = u.username,
            displayName = u.displayName,
            avatarUrl = u.avatarUrl,
            about = u.about,
            lastSeen = u.lastSeen,
            isOnline = u.isOnline
        )
    }
}
