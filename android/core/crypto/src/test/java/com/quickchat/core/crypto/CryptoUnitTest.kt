package com.quickchat.core.crypto

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CryptoUnitTest {

    @Test
    fun testDiffieHellmanAgreement() {
        // Generate key pairs for Alice and Bob
        val aliceKeys = SignalKeys.generateKeyPair()
        val bobKeys = SignalKeys.generateKeyPair()

        // Compute DH exchange secrets
        val dhAlice = SignalKeys.calculateDH(aliceKeys.private, bobKeys.public)
        val dhBob = SignalKeys.calculateDH(bobKeys.private, aliceKeys.public)

        // Verify mathematical parity: A_priv * B_pub == B_priv * A_pub
        assertArrayEquals("Diffie-Hellman secrets must be mathematically equivalent", dhAlice, dhBob)
    }

    @Test
    fun testDoubleRatchetMessaging() {
        // 1. Generate identity and prekey bundles
        val aliceIdentity = SignalKeys.generateKeyPair()
        val bobIdentity = SignalKeys.generateKeyPair()
        val bobSignedPre = SignalKeys.generateKeyPair()
        val bobOneTimePre = SignalKeys.generateKeyPair()

        // 2. Initialize E2E Session on Alice (initiator) side
        val aliceSession = DoubleRatchetEngine.initAlice(
            ourIdentityKey = aliceIdentity,
            recipientIdentityKey = bobIdentity.public,
            recipientSignedPreKey = bobSignedPre.public,
            recipientOneTimePreKey = bobOneTimePre.public
        )

        // 3. Alice encrypts message
        val plaintextAlice = "Hello Bob! This is an end-to-end encrypted message."
        val payload1 = DoubleRatchetEngine.encrypt(aliceSession, plaintextAlice.toByteArray(Charsets.UTF_8))

        assertNotNull(payload1.ciphertext)
        assertNotNull(payload1.iv)
        assertNotNull(payload1.ephemeralPublicKey)

        // 4. Initialize E2E Session on Bob (receiver) side using Alice's ephemeral key from headers
        val bobSession = DoubleRatchetEngine.initBob(
            ourIdentityKey = bobIdentity,
            ourSignedPreKey = bobSigned,
            ourOneTimePreKey = bobOneTimePre,
            senderIdentityKey = aliceIdentity.public,
            senderEphemeralKey = SignalKeys.decodePublicKey(payload1.ephemeralPublicKey)
        )

        // 5. Bob decrypts message
        val decryptedBytes1 = DoubleRatchetEngine.decrypt(bobSession, payload1)
        val decryptedText1 = String(decryptedBytes1, Charsets.UTF_8)
        assertEquals(plaintextAlice, decryptedText1)

        // 6. Bidirectional Ratchet test: Bob replies to Alice
        val plaintextBob = "Hi Alice, I decrypted your message successfully!"
        val payload2 = DoubleRatchetEngine.encrypt(bobSession, plaintextBob.toByteArray(Charsets.UTF_8))

        // Alice decrypts Bob's message
        val decryptedBytes2 = DoubleRatchetEngine.decrypt(aliceSession, payload2)
        val decryptedText2 = String(decryptedBytes2, Charsets.UTF_8)
        assertEquals(plaintextBob, decryptedText2)
    }

    @Test
    fun testSecurityCodeFingerprintConsensus() {
        val keyA = SignalKeys.generateKeyPair()
        val keyB = SignalKeys.generateKeyPair()

        // Fingerprint calculated from Alice's viewpoint
        val fpAlice = SecurityCodeVerifier.generateFingerprint(keyA.public, keyB.public)
        
        // Fingerprint calculated from Bob's viewpoint
        val fpBob = SecurityCodeVerifier.generateFingerprint(keyB.public, keyA.public)

        // Fingerprints must match identically (sorted internally)
        assertEquals("Fingerprints must achieve identical consensus", fpAlice, fpBob)
        assertEquals("Fingerprint format must contain 5 groups of 12 digits (60 chars + 4 spaces)", 64, fpAlice.length)
    }

    @Test
    fun testMediaSymmetricCrypto() {
        val originalFileContent = "Heavy file content representation (image/video/voice bytes)".toByteArray()

        // Encrypt media
        val encResult = MediaEncryptor.encryptFile(originalFileContent)
        assertNotNull(encResult.encryptedBytes)
        assertNotNull(encResult.mediaKey)
        assertNotNull(encResult.iv)

        // Decrypt media
        val decryptedFileContent = MediaEncryptor.decryptFile(
            encResult.encryptedBytes,
            encResult.mediaKey,
            encResult.iv
        )

        assertArrayEquals("Decrypted media bytes must match the source bytes exactly", originalFileContent, decryptedFileContent)
    }
}
