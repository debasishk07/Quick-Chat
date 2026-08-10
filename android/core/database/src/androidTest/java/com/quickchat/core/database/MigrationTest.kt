package com.quickchat.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quickchat.core.database.di.DatabaseModule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val TEST_DB = "migration-test"

    @Rule
    @JvmField
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate1To4() {
        // 1. Create version 1 database state and insert some data
        var db = helper.createDatabase(TEST_DB, 1)
        
        // Execute raw SQLite inserts since version 1 models don't compile anymore
        db.execSQL("""
            INSERT INTO chats (recipientPhone, displayName, avatarUrl, isProfileLoaded, isGroup, lastMessageId, unreadCount, isPinned, isMuted, isArchived)
            VALUES ('1234567890', 'Alice', NULL, 1, 0, NULL, 0, 0, 0, 0)
        """)
        
        db.execSQL("""
            INSERT INTO messages (id, senderPhone, recipientPhone, isGroup, ciphertext, iv, ephemeralPublicKey, messageType, timestamp, status, plainText)
            VALUES ('msg_1', '1234567890', '9876543210', 0, 'encrypted', 'iv_1', NULL, 'TEXT', 1625097600000, 'SENT', 'Hello Alice!')
        """)
        
        db.close()

        // 2. Re-open and migrate database up to version 4
        db = helper.runMigrationsAndValidate(
            TEST_DB,
            4,
            true, 
            DatabaseModule.MIGRATION_1_2,
            DatabaseModule.MIGRATION_2_3,
            DatabaseModule.MIGRATION_3_4
        )

        // 3. Verify that old seed data is fully preserved and columns have expected migrated values
        val cursorChats = db.query("SELECT * FROM chats WHERE recipientPhone = '1234567890'")
        assert(cursorChats.moveToFirst())
        val disappearingDurationIndex = cursorChats.getColumnIndex("disappearingDuration")
        assert(disappearingDurationIndex != -1)
        val disappearingDurationVal = cursorChats.getLong(disappearingDurationIndex)
        assert(disappearingDurationVal == 0L)
        cursorChats.close()

        val cursorMessages = db.query("SELECT * FROM messages WHERE id = 'msg_1'")
        assert(cursorMessages.moveToFirst())
        val isStarredIndex = cursorMessages.getColumnIndex("isStarred")
        assert(isStarredIndex != -1)
        assert(cursorMessages.getInt(isStarredIndex) == 0)
        
        val expireAtIndex = cursorMessages.getColumnIndex("expireAt")
        assert(expireAtIndex != -1)
        assert(cursorMessages.isNull(expireAtIndex))
        cursorMessages.close()

        // 4. Verify FTS Table is populated automatically with existing message
        val cursorFts = db.query("SELECT * FROM messages_fts WHERE messageId = 'msg_1'")
        assert(cursorFts.moveToFirst())
        val plainTextIndex = cursorFts.getColumnIndex("plainText")
        assert(cursorFts.getString(plainTextIndex) == "Hello Alice!")
        cursorFts.close()

        db.close()
    }
}
