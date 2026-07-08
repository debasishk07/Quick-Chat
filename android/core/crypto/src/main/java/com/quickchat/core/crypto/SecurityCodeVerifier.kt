package com.quickchat.core.crypto

import android.util.Base64
import java.security.MessageDigest
import java.security.PublicKey

object SecurityCodeVerifier {

    /**
     * Computes the 60-digit security fingerprint between two users.
     */
    fun generateFingerprint(ourIdentityKey: PublicKey, partnerIdentityKey: PublicKey): String {
        val keyA = SignalKeys.encodePublicKey(ourIdentityKey)
        val keyB = SignalKeys.encodePublicKey(partnerIdentityKey)
        
        // Sort keys to guarantee consensus regardless of who initiates
        val sortedKeys = if (keyA < keyB) keyA + keyB else keyB + keyA
        
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(sortedKeys.toByteArray(Charsets.UTF_8))
        
        // Form 5 blocks of 12 digits (total 60 digits)
        val sb = StringBuilder()
        for (i in 0 until 5) {
            val offset = i * 4
            // Read 4 bytes as unsigned integer
            val val0 = hash[offset].toInt() and 0xFF
            val val1 = hash[offset + 1].toInt() and 0xFF
            val val2 = hash[offset + 2].toInt() and 0xFF
            val val3 = hash[offset + 3].toInt() and 0xFF
            
            val longVal = ((val0.toLong() shl 24) or
                           (val1.toLong() shl 16) or
                           (val2.toLong() shl 8) or
                           val3.toLong()) % 100000000000L // Cap at 100,000,000,000 (11 digits max + padding = 12 digits)
            
            val blockStr = String.format("%012d", longVal)
            sb.append(blockStr)
            if (i < 4) sb.append(" ")
        }
        
        return sb.toString()
    }
}
