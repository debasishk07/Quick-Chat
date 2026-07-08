package com.quickchat.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.quickchat.core.database.dao.*
import com.quickchat.core.database.entities.*

@Database(
    entities = [
        MessageEntity::class,
        ChatEntity::class,
        StatusEntity::class,
        SessionEntity::class,
        OutboxEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
    abstract fun statusDao(): StatusDao
    abstract fun sessionDao(): SessionDao
    abstract fun outboxDao(): OutboxDao
}
