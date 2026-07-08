package com.quickchat.core.crypto

import android.util.Base64
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom

class DoubleRatchetSession(
    var rootKey: ByteArray,
    var sendingChainKey: ByteArray?,
    var receivingChainKey: ByteArray?,
    var localKeyPair: KeyPair,
    var remotePublicKey: PublicKey?,
    var skippedMessageKeys: MutableMap<String, ByteArray> = mutableMapOf(), // Key: "remotePubKeyBase64_seqNumber" -> messageKey
    var sequenceNumberSending: Int = 0,
    var sequenceNumberReceiving: Int = 0
)

object DoubleRatchetEngine {
    private val HKDF_SALT = "DoubleRatchetSalt".toByteArray()
    private val INFO_ROOT = "DoubleRatchetRootInfo".toByteArray()
    private val INFO_CHAIN = "DoubleRatchetChainInfo".toByteArray()
    private val INFO_MESSAGE = "DoubleRatchetMessageInfo".toByteArray()

    /**
     * Initialize active session from X3DH Alice initiator side.
     */
    fun initAlice(
        ourIdentityKey: KeyPair,
        recipientIdentityKey: PublicKey,
        recipientSignedPreKey: PublicKey,
        recipientOneTimePreKey: PublicKey?
    ): DoubleRatchetSession {
        val ephemeralKeyPair = SignalKeys.generateKeyPair()

        // DH1 = DH(ourIdentityKey, recipientSignedPreKey)
        val dh1 = SignalKeys.calculateDH(ourIdentityKey.private, recipientSignedPreKey)
        // DH2 = DH(ephemeralKeyPair, recipientIdentityKey)
        val dh2 = SignalKeys.calculateDH(ephemeralKeyPair.private, recipientIdentityKey)
        // DH3 = DH(ephemeralKeyPair, recipientSignedPreKey)
        val dh3 = SignalKeys.calculateDH(ephemeralKeyPair.private, recipientSignedPreKey)
        
        var combinedDh = dh1 + dh2 + dh3

        if (recipientOneTimePreKey != null) {
            // DH4 = DH(ephemeralKeyPair, recipientOneTimePreKey)
            val dh4 = SignalKeys.calculateDH(ephemeralKeyPair.private, recipientOneTimePreKey)
            combinedDh += dh4
        }

        // Shared master root key
        val sharedMaster = SignalKeys.hkdf(combinedDh, 64, INFO_ROOT, HKDF_SALT)
        val rootKey = sharedMaster.sliceArray(0..31)
        val sendingChainKey = sharedMaster.sliceArray(32..63)

        // Initialize session
        return DoubleRatchetSession(
            rootKey = rootKey,
            sendingChainKey = sendingChainKey,
            receivingChainKey = null,
            localKeyPair = ephemeralKeyPair,
            remotePublicKey = recipientSignedPreKey
        )
    }

    /**
     * Initialize active session from X3DH Bob receiver side.
     */
    fun initBob(
        ourIdentityKey: KeyPair,
        ourSignedPreKey: KeyPair,
        ourOneTimePreKey: KeyPair?,
        senderIdentityKey: PublicKey,
        senderEphemeralKey: PublicKey
    ): DoubleRatchetSession {
        // DH1 = DH(ourSignedPreKey, senderIdentityKey)
        val dh1 = SignalKeys.calculateDH(ourSignedPreKey.private, senderIdentityKey)
        // DH2 = DH(ourIdentityKey, senderEphemeralKey)
        val dh2 = SignalKeys.calculateDH(ourIdentityKey.private, senderEphemeralKey)
        // DH3 = DH(ourSignedPreKey, senderEphemeralKey)
        val dh3 = SignalKeys.calculateDH(ourSignedPreKey.private, senderEphemeralKey)

        var combinedDh = dh1 + dh2 + dh3

        if (ourOneTimePreKey != null) {
            // DH4 = DH(ourOneTimePreKey, senderEphemeralKey)
            val dh4 = SignalKeys.calculateDH(ourOneTimePreKey.private, senderEphemeralKey)
            combinedDh += dh4
        }

        val sharedMaster = SignalKeys.hkdf(combinedDh, 64, INFO_ROOT, HKDF_SALT)
        val rootKey = sharedMaster.sliceArray(0..31)
        val receivingChainKey = sharedMaster.sliceArray(32..63)

        val newKeyPair = SignalKeys.generateKeyPair()

        return DoubleRatchetSession(
            rootKey = rootKey,
            sendingChainKey = null,
            receivingChainKey = receivingChainKey,
            localKeyPair = newKeyPair,
            remotePublicKey = senderEphemeralKey
        )
    }

    /**
     * Performs a Symmetric-Key Ratchet step to generate the next message key.
     */
    private fun symmetricRatchet(chainKey: ByteArray): Pair<ByteArray, ByteArray> {
        val mac = SignalKeys.hmac(chainKey, INFO_CHAIN)
        val nextChainKey = mac.sliceArray(0..31)
        val messageKey = SignalKeys.hmac(chainKey, INFO_MESSAGE)
        return Pair(nextChainKey, messageKey)
    }

    /**
     * Encrypt a message using the current session.
     */
    fun encrypt(session: DoubleRatchetSession, plaintext: ByteArray): EncryptedPayload {
        if (session.sendingChainKey == null) {
            // DH Ratchet step: Generate new DH local key pair and update root key
            val newKeyPair = SignalKeys.generateKeyPair()
            val dhSecret = SignalKeys.calculateDH(newKeyPair.private, session.remotePublicKey!!)
            val rootHkdf = SignalKeys.hkdf(dhSecret, 64, INFO_ROOT, session.rootKey)

            session.rootKey = rootHkdf.sliceArray(0..31)
            session.sendingChainKey = rootHkdf.sliceArray(32..63)
            session.localKeyPair = newKeyPair
        }

        val (nextChain, messageKey) = symmetricRatchet(session.sendingChainKey!!)
        session.sendingChainKey = nextChain
        session.sequenceNumberSending++

        val (iv, ciphertext) = SignalKeys.encryptAES(plaintext, messageKey)

        return EncryptedPayload(
            ciphertext = Base64.encodeToString(ciphertext, Base64.DEFAULT).trim(),
            iv = Base64.encodeToString(iv, Base64.DEFAULT).trim(),
            ephemeralPublicKey = SignalKeys.encodePublicKey(session.localKeyPair.public)
        )
    }

    /**
     * Decrypt an incoming message.
     */
    fun decrypt(session: DoubleRatchetSession, payload: EncryptedPayload): ByteArray {
        val remotePublicKey = SignalKeys.decodePublicKey(payload.ephemeralPublicKey)
        val ciphertext = Base64.decode(payload.ciphertext, Base64.DEFAULT)
        val iv = Base64.decode(payload.iv, Base64.DEFAULT)

        // DH Ratchet Step if remote key is new
        if (session.remotePublicKey == null || session.remotePublicKey != remotePublicKey) {
            // Skip keys for current receiving chain
            skipMessageKeys(session, session.sequenceNumberReceiving)

            session.remotePublicKey = remotePublicKey
            val dhSecret = SignalKeys.calculateDH(session.localKeyPair.private, remotePublicKey)
            val rootHkdf = SignalKeys.hkdf(dhSecret, 64, INFO_ROOT, session.rootKey)

            session.rootKey = rootHkdf.sliceArray(0..31)
            session.receivingChainKey = rootHkdf.sliceArray(32..63)
            session.sequenceNumberReceiving = 0

            // Generate new local key pair for our next sending chain
            val newKeyPair = SignalKeys.generateKeyPair()
            val dhSecretSend = SignalKeys.calculateDH(newKeyPair.private, remotePublicKey)
            val rootHkdfSend = SignalKeys.hkdf(dhSecretSend, 64, INFO_ROOT, session.rootKey)

            session.rootKey = rootHkdfSend.sliceArray(0..31)
            session.sendingChainKey = rootHkdfSend.sliceArray(32..63)
            session.localKeyPair = newKeyPair
            session.sequenceNumberSending = 0
        }

        // Symmetric-Key Ratchet Step
        val (nextChain, messageKey) = symmetricRatchet(session.receivingChainKey!!)
        session.receivingChainKey = nextChain
        session.sequenceNumberReceiving++

        return SignalKeys.decryptAES(ciphertext, messageKey, iv)
    }

    private fun skipMessageKeys(session: DoubleRatchetSession, untilSeq: Int) {
        if (session.receivingChainKey == null) return
        while (session.sequenceNumberReceiving < untilSeq) {
            val (nextChain, messageKey) = symmetricRatchet(session.receivingChainKey!!)
            session.receivingChainKey = nextChain
            val keyStr = "${SignalKeys.encodePublicKey(session.remotePublicKey!!)}_${session.sequenceNumberReceiving}"
            session.skippedMessageKeys[keyStr] = messageKey
            session.sequenceNumberReceiving++
        }
    }
}

data class EncryptedPayload(
    val ciphertext: String,
    val iv: String,
    val ephemeralPublicKey: String
)
