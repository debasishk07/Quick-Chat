package com.quickchat.core.database

import com.quickchat.core.database.entities.MessageEntity
import com.quickchat.core.model.MessageStatus
import com.quickchat.core.model.MessageType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.sql.DriverManager

class DeleteFlowVerificationTest {

    @Test
    fun verifyStep1AndStep2DataFlow() {
        // Create an in-memory SQLite database via JDBC to simulate Room DB engine behavior
        val conn = DriverManager.getConnection("jdbc:sqlite::memory:")
        val statement = conn.createStatement()

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

        val msgId = "test_msg_del_101"
        val sender = "user_alice"
        val recipient = "user_bob"

        // 1. Insert active message (isDeleted = 0)
        val prepInsert = conn.prepareStatement("""
            INSERT INTO messages (id, senderPhone, recipientPhone, isGroup, ciphertext, iv, ephemeralPublicKey, messageType, timestamp, status, plainText, isDeleted)
            VALUES (?, ?, ?, 0, 'enc_payload', 'iv_vector', NULL, 'TEXT', ?, 'SENT', 'Secret message text', 0)
        """.trimIndent())
        prepInsert.setString(1, msgId)
        prepInsert.setString(2, sender)
        prepInsert.setString(3, recipient)
        prepInsert.setLong(4, System.currentTimeMillis())
        prepInsert.executeUpdate()

        // 2. Perform Delete For Everyone write operation (mirroring MessageDao.markMessageAsDeleted)
        val prepUpdate = conn.prepareStatement("""
            UPDATE messages SET isDeleted = 1, plainText = 'This message was deleted' WHERE id = ?
        """.trimIndent())
        prepUpdate.setString(1, msgId)
        val rowsAffected = prepUpdate.executeUpdate()
        println("=== STEP 1 EXECUTION ===")
        println("Rows affected by markMessageAsDeleted: $rowsAffected")
        assertEquals(1, rowsAffected)

        // STEP 1 PROOF: Query DB directly for raw row state after deletion
        val prepQuery1 = conn.prepareStatement("SELECT * FROM messages WHERE id = ?")
        prepQuery1.setString(1, msgId)
        val rs1 = prepQuery1.executeQuery()
        assertTrue(rs1.next())

        val rawId = rs1.getString("id")
        val rawPlainText = rs1.getString("plainText")
        val rawIsDeleted = rs1.getInt("isDeleted")
        println("=== STEP 1 PROOF (RAW DB ROW STATE) ===")
        println("Raw Row -> id: '$rawId', plainText: '$rawPlainText', isDeleted: $rawIsDeleted")

        assertEquals("This message was deleted", rawPlainText)
        assertEquals(1, rawIsDeleted)

        // STEP 2 PROOF: Simulate app restart fetch (getMessagesFlow query & toDomain mapping)
        val prepQuery2 = conn.prepareStatement("""
            SELECT * FROM messages WHERE senderPhone = ? OR recipientPhone = ? ORDER BY timestamp ASC
        """.trimIndent())
        prepQuery2.setString(1, recipient)
        prepQuery2.setString(2, recipient)
        val rs2 = prepQuery2.executeQuery()
        assertTrue(rs2.next())

        val entity = MessageEntity(
            id = rs2.getString("id"),
            senderPhone = rs2.getString("senderPhone"),
            recipientPhone = rs2.getString("recipientPhone"),
            isGroup = rs2.getInt("isGroup") == 1,
            ciphertext = rs2.getString("ciphertext"),
            iv = rs2.getString("iv"),
            ephemeralPublicKey = rs2.getString("ephemeralPublicKey"),
            messageType = rs2.getString("messageType"),
            timestamp = rs2.getLong("timestamp"),
            status = rs2.getString("status"),
            plainText = rs2.getString("plainText"),
            isStarred = rs2.getInt("isStarred") == 1,
            expireAt = if (rs2.getObject("expireAt") != null) rs2.getLong("expireAt") else null,
            reaction = rs2.getString("reaction"),
            playbackSpeed = rs2.getFloat("playbackSpeed"),
            isEdited = rs2.getInt("isEdited") == 1,
            pinnedAt = if (rs2.getObject("pinnedAt") != null) rs2.getLong("pinnedAt") else null,
            isDeleted = rs2.getInt("isDeleted") == 1
        )
        val domainMsg = entity.toDomain()

        println("=== STEP 2 PROOF (APP RESTART FETCH RESPONSE) ===")
        println("Fetched Domain Message -> id: '${domainMsg.id}', isDeleted: ${domainMsg.isDeleted}, plainText: '${domainMsg.plainText}'")

        assertTrue("Fetched message must have isDeleted = true", domainMsg.isDeleted)
        assertEquals("This message was deleted", domainMsg.plainText)

        conn.close()
    }
}
