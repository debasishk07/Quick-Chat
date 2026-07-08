package com.quickchat.core.network.repository

import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.quickchat.core.crypto.MediaEncryptor
import com.quickchat.core.database.dao.StatusDao
import com.quickchat.core.database.entities.StatusEntity
import com.quickchat.core.model.Status
import com.quickchat.core.model.StatusMediaType
import com.quickchat.core.model.StatusView
import com.quickchat.core.model.UserStatus
import com.quickchat.core.network.api.QuickChatApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface StatusRepository {
    fun getStatusesFlow(): Flow<List<UserStatus>>
    suspend fun uploadStatus(mediaBytes: ByteArray, caption: String?, mediaType: StatusMediaType): Boolean
    suspend fun purgeExpiredStatuses()
}

@Singleton
class StatusRepositoryImpl @Inject constructor(
    private val api: QuickChatApi,
    private val statusDao: StatusDao,
    private val userRepository: UserRepository
) : StatusRepository {

    private val gson = Gson()

    override fun getStatusesFlow(): Flow<List<UserStatus>> {
        return statusDao.getAllStatusesFlow().map { entities ->
            // Map Room entities to domain Status items
            val domainStatuses = entities.map { entity ->
                val type = object : TypeToken<List<StatusView>>() {}.type
                val viewersList: List<StatusView> = gson.fromJson(entity.viewersJson, type) ?: emptyList()
                entity.toDomain(viewersList)
            }

            // Group by sender phone number into UserStatus feeds
            val groupedBySender = domainStatuses.groupBy { it.senderPhone }
            groupedBySender.map { (senderPhone, statuses) ->
                // Mock user profile details for status sender
                val senderUser = userRepository.currentUser.value?.let { me ->
                    if (me.phone == senderPhone) me else null
                } ?: com.quickchat.core.model.User(
                    phone = senderPhone,
                    displayName = "Contact $senderPhone",
                    avatarUrl = null,
                    about = "Hey there! I am using Quick Chat.",
                    lastSeen = 0L,
                    isOnline = false
                )
                UserStatus(user = senderUser, statuses = statuses)
            }
        }
    }

    override suspend fun uploadStatus(
        mediaBytes: ByteArray,
        caption: String?,
        mediaType: StatusMediaType
    ): Boolean {
        return try {
            val myPhone = userRepository.currentUser.value?.phone ?: return false

            // 1. Encrypt status media file client-side using AES-GCM
            val encryptResult = MediaEncryptor.encryptFile(mediaBytes)

            // 2. Prepare multipart upload of ciphertext bytes
            val requestFile = encryptResult.encryptedBytes.toRequestBody("application/octet-stream".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", "encrypted_status_media", requestFile)

            // 3. Upload to server
            val uploadResponse = api.uploadMedia(body)
            if (!uploadResponse.success) {
                return false
            }

            // 4. Save status locally (along with AES key & iv for local playback)
            val statusId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()
            val expiresAt = now + (24 * 60 * 60 * 1000) // Expires in 24 hours

            // Embed key and IV into the mediaUrl string or store it alongside.
            // A clean way to pack E2EE media links: url#keyBase64,ivBase64
            val keyBase64 = Base64.encodeToString(encryptResult.mediaKey, Base64.NO_WRAP)
            val ivBase64 = Base64.encodeToString(encryptResult.iv, Base64.NO_WRAP)
            val encryptedMediaUrlWithKey = "${uploadResponse.fileUrl}#$keyBase64,$ivBase64"

            val newStatus = Status(
                id = statusId,
                senderPhone = myPhone,
                mediaUrl = encryptedMediaUrlWithKey,
                caption = caption,
                mediaType = mediaType,
                timestamp = now,
                expiresAt = expiresAt,
                views = emptyList()
            )

            statusDao.insertStatus(
                StatusEntity.fromDomain(
                    newStatus,
                    gson.toJson(emptyList<StatusView>())
                )
            )

            // Emit to server database status table (in real implementation)
            // api.publishStatusMetadata(statusId, myPhone, uploadResponse.fileUrl, expiresAt, etc.)
            
            true
        } catch (e: Exception) {
            Log.e("StatusRepository", "Failed to upload status", e)
            false
        }
    }

    override suspend fun purgeExpiredStatuses() {
        val now = System.currentTimeMillis()
        statusDao.purgeExpiredStatuses(now)
    }
}
