package com.quickchat.core.network.api

import okhttp3.MultipartBody
import retrofit2.http.*

data class VerifyOtpRequest(val phone: String, val code: String)
data class VerifyOtpResponse(val success: Boolean, val isNewUser: Boolean, val user: ApiUser?)

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
}
