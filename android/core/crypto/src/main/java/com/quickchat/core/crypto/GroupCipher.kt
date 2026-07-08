package com.quickchat.core.crypto

import android.util.Base64
import java.security.SecureRandom

data class SenderKey(
    val chainKeyBytes: ByteArray,
    val signatureKeyBytes: ByteArray, // Mock representation for signing
    var iteration: Int = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SenderKey
        if (!chainKeyBytes.contentEquals(other.chainKeyBytes)) return false
        if (!signatureKeyBytes.contentEquals(other.signatureKeyBytes)) return false
        return iteration == other.iteration
    }

    override fun hashCode(): Int {
        var result = chainKeyBytes.contentHashCode()
        result = 31 * result + signatureKeyBytes.contentHashCode()
        result = 31 * result + iteration
        return result
    }
}

class GroupSessionState {
    private val senderKeys = mutableMapOf<String, SenderKey>() // senderPhone -> SenderKey

    fun getSenderKey(senderPhone: String): SenderKey? {
        return senderKeys[senderPhone]
    }

    fun storeSenderKey(senderPhone: String, senderKey: SenderKey) {
        senderKeys[senderPhone] = senderKey
    }
}

object GroupCipher {
    private val GROUP_KDF_INFO = "GroupSenderKeyKDF".toByteArray()

    /**
     * Generate a new Sender Key bundle for ourself to distribute to group members.
     */
    fun generateOurSenderKey(): SenderKey {
        val random = SecureRandom()
        val chainKey = ByteArray(32).apply { random.nextBytes(this) }
        val signatureKey = ByteArray(32).apply { random.nextBytes(this) }
        return SenderKey(chainKey, signatureKey, 0)
    }

    /**
     * Encrypt a message using our own Sender Key.
     */
    fun encrypt(ourKey: SenderKey, plaintext: ByteArray): GroupEncryptedPayload {
        // Derive message key from the current chain key
        val messageKey = SignalKeys.hkdf(
            ourKey.chainKeyBytes,
            32,
            "GroupMessageKey_${ourKey.iteration}".toByteArray(),
            GROUP_KDF_INFO
        )

        // Encrypt message
        val (iv, ciphertext) = SignalKeys.encryptAES(plaintext, messageKey)

        // Advance Chain Key (symmetric ratchet)
        val nextChainKey = SignalKeys.hmac(ourKey.chainKeyBytes, "NextGroupChainKey".toByteArray())
        val updatedKey = ourKey.copy(
            chainKeyBytes = nextChainKey,
            iteration = ourKey.iteration + 1
        )
        // Update state parameters manually since ourKey is passed by reference
        System.arraycopy(nextChainKey, 0, ourKey.chainKeyBytes, 0, 32)
        ourKey.iteration++

        return GroupEncryptedPayload(
            ciphertext = Base64.encodeToString(ciphertext, Base64.DEFAULT).trim(),
            iv = Base64.encodeToString(iv, Base64.DEFAULT).trim(),
            iteration = updatedKey.iteration - 1
        )
    }

    /**
     * Decrypt a message received from a group member, using their stored Sender Key.
     */
    fun decrypt(
        senderPhone: String,
        sessionState: GroupSessionState,
        payload: GroupEncryptedPayload
    ): ByteArray {
        val senderKey = sessionState.getSenderKey(senderPhone) 
            ?: throw IllegalStateException("No Sender Key registered for user $senderPhone")

        // Fast-forward or match iteration key (simplified symmetric ratchet)
        var currentChainKey = senderKey.chainKeyBytes
        var currentIteration = senderKey.iteration

        if (payload.iteration < currentIteration) {
            throw IllegalArgumentException("Message received out of order (duplicate or replay attack). Payload iteration ${payload.iteration} < current ${currentIteration}")
        }

        while (currentIteration < payload.iteration) {
            currentChainKey = SignalKeys.hmac(currentChainKey, "NextGroupChainKey".toByteArray())
            currentIteration++
        }

        // Derive message key
        val messageKey = SignalKeys.hkdf(
            currentChainKey,
            32,
            "GroupMessageKey_${payload.iteration}".toByteArray(),
            GROUP_KDF_INFO
        )

        val ciphertextBytes = Base64.decode(payload.ciphertext, Base64.DEFAULT)
        val ivBytes = Base64.decode(payload.iv, Base64.DEFAULT)

        val decrypted = SignalKeys.decryptAES(ciphertextBytes, messageKey, ivBytes)

        // Advance cached key state in DB
        val nextChainKey = SignalKeys.hmac(currentChainKey, "NextGroupChainKey".toByteArray())
        senderKey.iteration = currentIteration + 1
        System.arraycopy(nextChainKey, 0, senderKey.chainKeyBytes, 0, 32)

        return decrypted
    }
}

data class GroupEncryptedPayload(
    val ciphertext: String,
    val iv: String,
    val iteration: Int
)
