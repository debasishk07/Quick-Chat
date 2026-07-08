package com.quickchat.core.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object KeystoreManager {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val DB_KEY_ALIAS = "QuickChatDbMasterKey"
    private const val AES_GCM_NOPADDING = "AES/GCM/NoPadding"

    init {
        getOrCreateMasterKey()
    }

    private fun getOrCreateMasterKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        
        if (keyStore.containsAlias(DB_KEY_ALIAS)) {
            val entry = keyStore.getEntry(DB_KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
            if (entry != null) {
                return entry.secretKey
            }
        }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            DB_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
            
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    /**
     * Encrypts a database passphrase so it can be stored in secure SharedPreferences.
     */
    fun encryptPassphrase(passphrase: String): String {
        val cipher = Cipher.getInstance(AES_GCM_NOPADDING)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateMasterKey())
        
        val ciphertextBytes = cipher.doFinal(passphrase.toByteArray(Charsets.UTF_8))
        val iv = cipher.iv

        // Combine IV and Ciphertext: [IV length (1 byte)] + [IV] + [Ciphertext]
        val combined = ByteArray(1 + iv.size + ciphertextBytes.size)
        combined[0] = iv.size.toByte()
        System.arraycopy(iv, 0, combined, 1, iv.size)
        System.arraycopy(ciphertextBytes, 0, combined, 1 + iv.size, ciphertextBytes.size)

        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    /**
     * Decrypts the sealed database passphrase.
     */
    fun decryptPassphrase(encryptedPassphraseBase64: String): String {
        val combined = Base64.decode(encryptedPassphraseBase64, Base64.DEFAULT)
        val ivSize = combined[0].toInt()
        
        val iv = ByteArray(ivSize)
        System.arraycopy(combined, 1, iv, 0, ivSize)

        val ciphertextBytes = ByteArray(combined.size - 1 - ivSize)
        System.arraycopy(combined, 1 + ivSize, ciphertextBytes, 0, ciphertextBytes.size)

        val cipher = Cipher.getInstance(AES_GCM_NOPADDING)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateMasterKey(), spec)

        val decryptedBytes = cipher.doFinal(ciphertextBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }
}
