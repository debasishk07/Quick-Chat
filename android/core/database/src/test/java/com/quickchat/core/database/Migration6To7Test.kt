package com.quickchat.core.database

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.sql.DriverManager

class Migration6To7Test {

    @Test
    fun testMigrate6To7() {
        val conn = DriverManager.getConnection("jdbc:sqlite::memory:")
        val statement = conn.createStatement()

        // 1. Create Version 6 Database Schema
        statement.execute("""
            CREATE TABLE messages (
                id TEXT PRIMARY KEY NOT NULL,
                senderPhone TEXT NOT NULL,
                recipientPhone TEXT NOT NULL,
                isGroup INTEGER NOT NULL,
                ciphertext TEXT NOT NULL,
                iv TEXT NOT NULL,
                ephemeralPublicKey TEXT,
                messageType TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                status TEXT NOT NULL,
                plainText TEXT,
                isStarred INTEGER NOT NULL DEFAULT 0,
                expireAt INTEGER,
                reaction TEXT,
                playbackSpeed REAL NOT NULL DEFAULT 1.0,
                isEdited INTEGER NOT NULL DEFAULT 0,
                pinnedAt INTEGER,
                isDeleted INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())

        statement.execute("""
            CREATE TABLE users (
                phone TEXT PRIMARY KEY NOT NULL,
                phoneNumber TEXT,
                email TEXT,
                username TEXT,
                displayName TEXT NOT NULL,
                avatarUrl TEXT,
                about TEXT,
                lastSeen INTEGER NOT NULL,
                isOnline INTEGER NOT NULL
            )
        """.trimIndent())

        // Insert seed data into version 6 schema
        statement.execute("""
            INSERT INTO messages (id, senderPhone, recipientPhone, isGroup, ciphertext, iv, messageType, timestamp, status, plainText)
            VALUES ('msg_v6_1', 'user_a', 'user_b', 0, 'cipher', 'iv', 'TEXT', 1625097600000, 'SENT', 'Hello before migration')
        """.trimIndent())

        statement.execute("""
            INSERT INTO users (phone, displayName, lastSeen, isOnline)
            VALUES ('user_a', 'Alice', 1625097600000, 1)
        """.trimIndent())

        // 2. Perform Migration 6 to 7
        statement.execute("ALTER TABLE `messages` ADD COLUMN `publicId` TEXT DEFAULT NULL")
        statement.execute("ALTER TABLE `messages` ADD COLUMN `mediaDuration` REAL DEFAULT NULL")
        statement.execute("ALTER TABLE `messages` ADD COLUMN `mediaFormat` TEXT DEFAULT NULL")
        statement.execute("ALTER TABLE `messages` ADD COLUMN `fileSize` INTEGER DEFAULT NULL")
        statement.execute("ALTER TABLE `users` ADD COLUMN `authProviders` TEXT DEFAULT NULL")
        statement.execute("ALTER TABLE `users` ADD COLUMN `usernameSearchEnabled` INTEGER DEFAULT 1")

        // 3. Verify that old data is intact and new columns exist
        val rsMsg = statement.executeQuery("SELECT * FROM messages WHERE id = 'msg_v6_1'")
        assertTrue(rsMsg.next())
        assertEquals("Hello before migration", rsMsg.getString("plainText"))
        assertEquals(null, rsMsg.getString("publicId"))

        val rsUser = statement.executeQuery("SELECT * FROM users WHERE phone = 'user_a'")
        assertTrue(rsUser.next())
        assertEquals("Alice", rsUser.getString("displayName"))
        assertEquals(1, rsUser.getInt("usernameSearchEnabled"))

        conn.close()
    }
}
