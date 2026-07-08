package com.quickchat.core.network.repository

import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import com.quickchat.core.crypto.SignalKeys
import com.quickchat.core.model.User
import com.quickchat.core.network.api.QuickChatApi
import com.quickchat.core.network.api.RegisterProfileRequest
import com.quickchat.core.network.api.SyncContactsRequest
import com.quickchat.core.network.api.UploadPreKeysRequest
import com.quickchat.core.network.api.VerifyOtpRequest
import com.quickchat.core.database.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.security.KeyPair
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

// Simple Helper import to avoid JSON parsing issues
import org.json.JSONObject

interface UserRepository {
    val currentUser: StateFlow<User?>
    suspend fun verifyOtp(phone: String, code: String): Boolean
    suspend fun registerProfile(phone: String, displayName: String, avatarUrl: String?, about: String?): Boolean
    suspend fun syncContacts(phones: List<String>): List<User>
    fun getLocalIdentityKey(): KeyPair?
    fun getLocalSignedPreKey(): KeyPair?
    fun getLocalOneTimePreKey(publicKeyBase64: String): KeyPair?
    fun logout()
    suspend fun deleteAccount(): Boolean
    suspend fun uploadAvatar(mediaBytes: ByteArray): String?
    
    // New features
    suspend fun googleLogin(googleUid: String, email: String, displayName: String, avatarUrl: String?): Boolean
    suspend fun linkPhone(userId: String, phone: String, code: String): Boolean
    suspend fun updateUsername(userId: String, username: String): String?
    suspend fun updatePrivacy(userId: String, enabled: Boolean): Boolean
    suspend fun searchUserByUsername(username: String): User
    suspend fun checkUsernameAvailability(username: String): String?
}

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val api: QuickChatApi,
    private val prefs: SharedPreferences,
    private val db: AppDatabase
) : UserRepository {

    private val gson = Gson()
    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser

    init {
        // Load logged in user if any
        val userJson = prefs.getString("current_user_profile", null)
        if (userJson != null) {
            _currentUser.value = gson.fromJson(userJson, User::class.java)
        }
    }

    private fun mapApiUser(it: com.quickchat.core.network.api.ApiUser): User {
        return User(
            phone = it.phone,
            phoneNumber = it.phoneNumber,
            email = it.email,
            username = it.username,
            displayName = it.displayName,
            avatarUrl = it.avatarUrl,
            about = it.about,
            lastSeen = it.lastSeen,
            isOnline = it.isOnline == 1,
            authProviders = it.authProviders?.split(",") ?: emptyList(),
            usernameSearchEnabled = it.usernameSearchEnabled != 0
        )
    }

    override suspend fun verifyOtp(phone: String, code: String): Boolean {
        return try {
            val response = api.verifyOtp(VerifyOtpRequest(phone, code))
            if (response.success && response.user != null) {
                val u = mapApiUser(response.user)
                saveUserLocally(u)
                true
            } else {
                response.success
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "OTP verification failed", e)
            false
        }
    }

    override suspend fun registerProfile(
        phone: String,
        displayName: String,
        avatarUrl: String?,
        about: String?
    ): Boolean {
        return try {
            val response = api.registerProfile(RegisterProfileRequest(phone, displayName, avatarUrl, about))
            if (response.success) {
                val u = mapApiUser(response.user)
                saveUserLocally(u)
                
                // On first launch / registration, generate and publish E2EE prekeys!
                if (getLocalIdentityKey() == null) {
                    generateAndPublishPreKeys(phone)
                }
                
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Profile registration failed", e)
            false
        }
    }

    override suspend fun syncContacts(phones: List<String>): List<User> {
        return try {
            val response = api.syncContacts(SyncContactsRequest(phones))
            response.contacts.map { mapApiUser(it) }
        } catch (e: Exception) {
            Log.e("UserRepository", "Contact syncing failed", e)
            emptyList()
        }
    }

    override suspend fun googleLogin(
        googleUid: String,
        email: String,
        displayName: String,
        avatarUrl: String?
    ): Boolean {
        return try {
            val response = api.googleLogin(com.quickchat.core.network.api.GoogleLoginRequest(googleUid, email, displayName, avatarUrl))
            if (response.success) {
                val u = mapApiUser(response.user)
                saveUserLocally(u)
                
                // If first launch / registration, generate and publish E2EE prekeys!
                if (getLocalIdentityKey() == null) {
                    generateAndPublishPreKeys(u.phone)
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Google login failed", e)
            false
        }
    }

    override suspend fun linkPhone(userId: String, phone: String, code: String): Boolean {
        return try {
            val response = api.linkPhone(com.quickchat.core.network.api.LinkPhoneRequest(userId, phone, code))
            if (response.success) {
                val u = mapApiUser(response.user)
                saveUserLocally(u)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Link phone failed", e)
            false
        }
    }

    override suspend fun updateUsername(userId: String, username: String): String? {
        return try {
            val response = api.updateUsername(com.quickchat.core.network.api.UpdateUsernameRequest(userId, username))
            if (response.success) {
                val u = mapApiUser(response.user)
                saveUserLocally(u)
                null
            } else {
                "Failed to update username"
            }
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            if (!errorBody.isNullOrEmpty()) {
                try {
                    JSONObject(errorBody).getString("error")
                } catch (jsonEx: Exception) {
                    e.message()
                }
            } else {
                e.message()
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Update username failed", e)
            e.localizedMessage ?: "Unknown error occurred"
        }
    }

    override suspend fun updatePrivacy(userId: String, enabled: Boolean): Boolean {
        return try {
            val response = api.updatePrivacy(com.quickchat.core.network.api.UpdatePrivacyRequest(userId, enabled))
            if (response.success) {
                val current = _currentUser.value
                if (current != null) {
                    saveUserLocally(current.copy(usernameSearchEnabled = enabled))
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Update privacy failed", e)
            false
        }
    }

    override suspend fun checkUsernameAvailability(username: String): String? {
        return try {
            val userId = _currentUser.value?.phone
            val response = api.checkUsernameAvailability(username, userId)
            if (response.available) null else (response.error ?: "Username is already taken")
        } catch (e: Exception) {
            Log.e("UserRepository", "Check username availability failed", e)
            e.localizedMessage ?: "Failed to check username availability"
        }
    }

    override suspend fun searchUserByUsername(username: String): User {
        val currentUserId = _currentUser.value?.phone ?: ""
        val clean = if (username.startsWith("@")) username.substring(1) else username
        val response = api.searchUser(clean, currentUserId)
        return mapApiUser(response.user)
    }

    private fun saveUserLocally(user: User) {
        _currentUser.value = user
        prefs.edit().putString("current_user_profile", gson.toJson(user)).apply()
    }

    override fun logout() {
        _currentUser.value = null
        prefs.edit().remove("current_user_profile").apply()
        // Purge all Room database cache tables
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                db.clearAllTables()
            } catch (e: Exception) {
                Log.e("UserRepository", "Failed to clear Room database on logout", e)
            }
        }
    }

    override suspend fun deleteAccount(): Boolean {
        return try {
            val userId = _currentUser.value?.phone ?: return false
            val response = api.deleteAccountBackend(userId)
            if (response.success) {
                logout()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Failed to delete account on backend", e)
            logout()
            true
        }
    }

    override suspend fun uploadAvatar(mediaBytes: ByteArray): String? {
        return try {
            val requestFile = mediaBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", "avatar_${System.currentTimeMillis()}.jpg", requestFile)
            val response = api.uploadMedia(body)
            if (response.success) response.fileUrl else null
        } catch (e: Exception) {
            Log.e("UserRepository", "Failed to upload avatar", e)
            null
        }
    }

    // Cryptographic keys management (stored encrypted/sealed locally in secure preferences)
    override fun getLocalIdentityKey(): KeyPair? = getKeyPair("local_identity_key")
    override fun getLocalSignedPreKey(): KeyPair? = getKeyPair("local_signed_prekey")
    
    override fun getLocalOneTimePreKey(publicKeyBase64: String): KeyPair? {
        val json = prefs.getString("local_otpk_$publicKeyBase64", null) ?: return null
        return keyPairFromJson(json)
    }

    private suspend fun generateAndPublishPreKeys(phone: String) {
        Log.d("UserRepository", "Generating encryption keys bundle for $phone...")
        val identityKeyPair = SignalKeys.generateKeyPair()
        val signedPreKeyPair = SignalKeys.generateKeyPair()

        // Generate 10 one-time prekeys for simulation
        val oneTimePreKeys = List(10) { SignalKeys.generateKeyPair() }

        // Save keys locally
        saveKeyPair("local_identity_key", identityKeyPair)
        saveKeyPair("local_signed_prekey", signedPreKeyPair)
        
        val oneTimeKeysPublicBase64 = mutableListOf<String>()
        for (kp in oneTimePreKeys) {
            val pubStr = SignalKeys.encodePublicKey(kp.public)
            saveKeyPair("local_otpk_$pubStr", kp)
            oneTimeKeysPublicBase64.add(pubStr)
        }

        // Upload public keys to backend
        try {
            api.uploadPreKeys(
                UploadPreKeysRequest(
                    phone = phone,
                    identityKey = SignalKeys.encodePublicKey(identityKeyPair.public),
                    signedPreKey = SignalKeys.encodePublicKey(signedPreKeyPair.public),
                    signedPreKeySignature = Base64.encodeToString("mock_signature".toByteArray(), Base64.NO_WRAP),
                    oneTimePreKeys = oneTimeKeysPublicBase64
                )
            )
            Log.d("UserRepository", "Keys bundle uploaded successfully.")
        } catch (e: Exception) {
            Log.e("UserRepository", "Failed to upload prekeys bundle to server", e)
        }
    }

    private fun saveKeyPair(key: String, keyPair: KeyPair) {
        val obj = JSONObject().apply {
            put("private", Base64.encodeToString(keyPair.private.encoded, Base64.NO_WRAP))
            put("public", Base64.encodeToString(keyPair.public.encoded, Base64.NO_WRAP))
        }
        prefs.edit().putString(key, obj.toString()).apply()
    }

    private fun getKeyPair(key: String): KeyPair? {
        val json = prefs.getString(key, null) ?: return null
        return keyPairFromJson(json)
    }

    private fun keyPairFromJson(jsonStr: String): KeyPair {
        val json = JSONObject(jsonStr)
        val privBytes = Base64.decode(json.getString("private"), Base64.DEFAULT)
        val pubBytes = Base64.decode(json.getString("public"), Base64.DEFAULT)
        
        val kf = java.security.KeyFactory.getInstance("EC")
        val privateKey = kf.generatePrivate(java.security.spec.PKCS8EncodedKeySpec(privBytes))
        val publicKey = kf.generatePublic(java.security.spec.X509EncodedKeySpec(pubBytes))
        return KeyPair(publicKey, privateKey)
    }
}


