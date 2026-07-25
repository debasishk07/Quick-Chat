package com.quickchat.core.network.api

import okhttp3.MultipartBody
import retrofit2.http.*

data class VerifyOtpRequest(val phone: String, val code: String)
data class VerifyOtpResponse(val success: Boolean, val isNewUser: Boolean, val user: ApiUser?)

data class VerifyFirebaseTokenRequest(
    val idToken: String,
    val provider: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val displayName: String? = null,
    val avatarUrl: String? = null
)
data class VerifyFirebaseTokenResponse(
    val success: Boolean,
    val isNewUser: Boolean,
    val token: String?,
    val user: ApiUser?
)

data class RegisterProfileRequest(val phone: String, val displayName: String, val avatarUrl: String?, val about: String?)
data class RegisterProfileResponse(val success: Boolean, val user: ApiUser)

data class SyncContactsRequest(val phones: List<String>)
data class SyncContactsResponse(val contacts: List<ApiUser>)

data class UploadPreKeysRequest(
    val phone: String,
    val identityKey: String,
    val signedPreKey: String,
    val signedPreKeySignature: String,
    val oneTimePreKeys: List<String>
)
data class UploadPreKeysResponse(val success: Boolean)

data class PreKeyBundleResponse(
    val phone: String,
    val identityKey: String,
    val signedPreKey: String,
    val signedPreKeySignature: String,
    val oneTimePreKey: String? // Optional one-time prekey returned
)

data class UploadMediaResponse(val success: Boolean, val fileUrl: String)

data class CloudinaryUploadResponse(
    val success: Boolean,
    val secure_url: String?,
    val public_id: String?,
    val resource_type: String?,
    val duration: Double?,
    val format: String?,
    val bytes: Long?
)

data class GoogleLoginRequest(
    val googleUid: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String?
)
data class GoogleLoginResponse(
    val success: Boolean,
    val isNewUser: Boolean,
    val user: ApiUser
)

data class LinkPhoneRequest(
    val userId: String,
    val phone: String,
    val code: String
)
data class LinkPhoneResponse(
    val success: Boolean,
    val user: ApiUser
)

data class UpdateUsernameRequest(
    val userId: String,
    val username: String
)
data class UpdateUsernameResponse(
    val success: Boolean,
    val user: ApiUser
)

data class UpdatePrivacyRequest(
    val userId: String,
    val usernameSearchEnabled: Boolean
)
data class UpdatePrivacyResponse(
    val success: Boolean
)

data class SearchUserResponse(
    val success: Boolean,
    val user: ApiUser
)
data class DeleteAccountResponse(
    val success: Boolean
)

data class ApiUser(
    val phone: String,
    val phoneNumber: String?,
    val email: String?,
    val username: String?,
    val displayName: String,
    val avatarUrl: String?,
    val about: String?,
    val lastSeen: Long,
    val isOnline: Int,
    val authProviders: String?,
    val usernameSearchEnabled: Int?
)

data class CheckUsernameResponse(val available: Boolean, val error: String?)

interface QuickChatApi {
    @POST("auth/verify")
    suspend fun verifyFirebaseToken(@Body request: VerifyFirebaseTokenRequest): VerifyFirebaseTokenResponse

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): VerifyOtpResponse

    @POST("auth/register-profile")
    suspend fun registerProfile(@Body request: RegisterProfileRequest): RegisterProfileResponse

    @POST("contacts/sync")
    suspend fun syncContacts(@Body request: SyncContactsRequest): SyncContactsResponse

    @POST("prekeys")
    suspend fun uploadPreKeys(@Body request: UploadPreKeysRequest): UploadPreKeysResponse

    @GET("prekeys/{phone}")
    suspend fun getPreKeyBundle(@Path("phone") phone: String): PreKeyBundleResponse

    @Multipart
    @POST("media/upload")
    suspend fun uploadMedia(@Part file: MultipartBody.Part): UploadMediaResponse

    @Multipart
    @POST("media/upload-cloudinary")
    suspend fun uploadCloudinaryMedia(@Part file: MultipartBody.Part): CloudinaryUploadResponse

    @POST("auth/google")
    suspend fun googleLogin(@Body request: GoogleLoginRequest): GoogleLoginResponse

    @POST("auth/link-phone")
    suspend fun linkPhone(@Body request: LinkPhoneRequest): LinkPhoneResponse

    @GET("auth/check-username")
    suspend fun checkUsernameAvailability(
        @Query("username") username: String,
        @Query("userId") userId: String?
    ): CheckUsernameResponse

    @POST("auth/update-username")
    suspend fun updateUsername(@Body request: UpdateUsernameRequest): UpdateUsernameResponse

    @GET("users/search")
    suspend fun searchUser(
        @Query("username") username: String,
        @Query("requesterId") requesterId: String
    ): SearchUserResponse

    @POST("users/privacy")
    suspend fun updatePrivacy(@Body request: UpdatePrivacyRequest): UpdatePrivacyResponse

    @DELETE("auth/account/{userId}")
    suspend fun deleteAccountBackend(@Path("userId") userId: String): DeleteAccountResponse

    @POST("users/block")
    suspend fun blockUser(@Body request: BlockRequest): BlockResponse

    @POST("users/unblock")
    suspend fun unblockUser(@Body request: UnblockRequest): UnblockResponse

    @GET("users/blocked/{phone}")
    suspend fun getBlockedUsers(@Path("phone") phone: String): BlockedUsersResponse

    @POST("users/report")
    suspend fun reportUser(@Body request: ReportRequest): ReportResponse

    @POST("messages/{id}/delete")
    suspend fun deleteMessageApi(
        @Path("id") id: String,
        @Body request: DeleteMessageRequest
    ): DeleteMessageResponse
}

data class DeleteMessageRequest(val requesterPhone: String, val recipientPhone: String, val mode: String)
data class DeleteMessageResponse(val success: Boolean, val error: String? = null)

data class BlockRequest(val blockerPhone: String, val blockedPhone: String)
data class BlockResponse(val success: Boolean, val error: String? = null)

data class UnblockRequest(val blockerPhone: String, val blockedPhone: String)
data class UnblockResponse(val success: Boolean, val error: String? = null)

data class BlockedUsersResponse(val success: Boolean, val blocked: List<String> = emptyList(), val error: String? = null)

data class ReportRequest(
    val reporterPhone: String,
    val reportedPhone: String,
    val reason: String,
    val description: String?,
    val attachMessages: Boolean,
    val messages: List<ReportedMessageDto> = emptyList()
)
data class ReportedMessageDto(
    val id: String,
    val senderPhone: String,
    val recipientPhone: String,
    val text: String,
    val timestamp: Long
)
data class ReportResponse(val success: Boolean, val error: String? = null)
