package com.quickchat.core.crypto

import android.util.Base64
import java.security.*
import java.security.spec.ECGenParameterSpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object SignalKeys {
    private const val EC_ALGORITHM = "EC"
    private const val CURVE_NAME = "secp256r1"
    private const val HMAC_SHA256 = "HmacSHA256"
    private const val AES_GCM = "AES/GCM/NoPadding"

    fun generateKeyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance(EC_ALGORITHM)
        val ecSpec = ECGenParameterSpec(CURVE_NAME)
        keyPairGenerator.initialize(ecSpec)
        return keyPairGenerator.generateKeyPair()
    }

    fun calculateDH(privateKey: PrivateKey, publicKey: PublicKey): ByteArray {
        val agreement = KeyAgreement.getInstance("ECDH")
        agreement.init(privateKey)
        agreement.doPhase(publicKey, true)
        return agreement.generateSecret()
    }

    /**
     * Decode a public key from its encoded X509 format (Base64).
     */
    fun decodePublicKey(base64PublicKey: String): PublicKey {
        val keyBytes = Base64.decode(base64PublicKey, Base64.DEFAULT)
        val keyFactory = KeyFactory.getInstance(EC_ALGORITHM)
        return keyFactory.generatePublic(X509EncodedKeySpec(keyBytes))
    }

    /**
     * Encode a public key to Base64.
     */
    fun encodePublicKey(publicKey: PublicKey): String {
        return Base64.encodeToString(publicKey.encoded, Base64.DEFAULT).trim()
    }

    /**
     * HMAC-SHA256 extraction & expansion (HKDF)
     */
    fun hkdf(sharedSecret: ByteArray, length: Int, info: ByteArray, salt: ByteArray? = null): ByteArray {
        val actualSalt = salt ?: ByteArray(32) // default to zero-filled salt of 32 bytes
        
        // 1. Extract
        val macExtract = Mac.getInstance(HMAC_SHA256)
        macExtract.init(SecretKeySpec(actualSalt, HMAC_SHA256))
        val prk = macExtract.doFinal(sharedSecret)

        // 2. Expand
        val macExpand = Mac.getInstance(HMAC_SHA256)
        macExpand.init(SecretKeySpec(prk, HMAC_SHA256))
        
        val result = ByteArray(length)
        var currentOffset = 0
        var blockNumber = 1
        var t = ByteArray(0)

        while (currentOffset < length) {
            macExpand.update(t)
            macExpand.update(info)
            macExpand.update(blockNumber.toByte())
            t = macExpand.doFinal()
            
            val bytesToCopy = minOf(t.size, length - currentOffset)
            System.arraycopy(t, 0, result, currentOffset, bytesToCopy)
            currentOffset += bytesToCopy
            blockNumber++
        }
        return result
    }

    fun encryptAES(plaintext: ByteArray, keyBytes: ByteArray): Pair<ByteArray, ByteArray> {
        val cipher = Cipher.getInstance(AES_GCM)
        val iv = ByteArray(12).apply { SecureRandom().nextBytes(this) }
        val secretKey = SecretKeySpec(keyBytes, "AES")
        val spec = GCMParameterSpec(128, iv)
        
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)
        val ciphertext = cipher.doFinal(plaintext)
        return Pair(iv, ciphertext)
    }

    fun decryptAES(ciphertext: ByteArray, keyBytes: ByteArray, iv: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(AES_GCM)
        val secretKey = SecretKeySpec(keyBytes, "AES")
        val spec = GCMParameterSpec(128, iv)
        
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        return cipher.doFinal(ciphertext)
    }

    fun hmac(key: ByteArray, data: ByteArray): ByteArray {
        val mac = Mac.getInstance(HMAC_SHA256)
        mac.init(SecretKeySpec(key, HMAC_SHA256))
        return mac.doFinal(data)
    }
}
