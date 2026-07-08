package com.quickchat.core.crypto

import java.security.SecureRandom

data class EncryptedMediaResult(
    val encryptedBytes: ByteArray,
    val mediaKey: ByteArray,
    val iv: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as EncryptedMediaResult
        if (!encryptedBytes.contentEquals(other.encryptedBytes)) return false
        if (!mediaKey.contentEquals(other.mediaKey)) return false
        return iv.contentEquals(other.iv)
    }

    override fun hashCode(): Int {
        var result = encryptedBytes.contentHashCode()
        result = 31 * result + mediaKey.contentHashCode()
        result = 31 * result + iv.contentHashCode()
        return result
    }
}

object MediaEncryptor {

    fun encryptFile(fileBytes: ByteArray): EncryptedMediaResult {
        val random = SecureRandom()
        val mediaKey = ByteArray(32).apply { random.nextBytes(this) }
        val (iv, ciphertext) = SignalKeys.encryptAES(fileBytes, mediaKey)
        return EncryptedMediaResult(ciphertext, mediaKey, iv)
    }

    fun decryptFile(encryptedBytes: ByteArray, mediaKey: ByteArray, iv: ByteArray): ByteArray {
        return SignalKeys.decryptAES(encryptedBytes, mediaKey, iv)
    }
}
