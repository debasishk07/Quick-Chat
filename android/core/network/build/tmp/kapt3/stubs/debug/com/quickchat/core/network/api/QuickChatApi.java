package com.quickchat.core.network.api;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u00a0\u0001\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J$\u0010\u0002\u001a\u00020\u00032\b\b\u0001\u0010\u0004\u001a\u00020\u00052\n\b\u0001\u0010\u0006\u001a\u0004\u0018\u00010\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0007J\u0018\u0010\b\u001a\u00020\t2\b\b\u0001\u0010\u0006\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\nJ\u0018\u0010\u000b\u001a\u00020\f2\b\b\u0001\u0010\r\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\nJ\u0018\u0010\u000e\u001a\u00020\u000f2\b\b\u0001\u0010\u0010\u001a\u00020\u0011H\u00a7@\u00a2\u0006\u0002\u0010\u0012J\u0018\u0010\u0013\u001a\u00020\u00142\b\b\u0001\u0010\u0010\u001a\u00020\u0015H\u00a7@\u00a2\u0006\u0002\u0010\u0016J\u0018\u0010\u0017\u001a\u00020\u00182\b\b\u0001\u0010\u0010\u001a\u00020\u0019H\u00a7@\u00a2\u0006\u0002\u0010\u001aJ\"\u0010\u001b\u001a\u00020\u001c2\b\b\u0001\u0010\u0004\u001a\u00020\u00052\b\b\u0001\u0010\u001d\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0007J\u0018\u0010\u001e\u001a\u00020\u001f2\b\b\u0001\u0010\u0010\u001a\u00020 H\u00a7@\u00a2\u0006\u0002\u0010!J\u0018\u0010\"\u001a\u00020#2\b\b\u0001\u0010\u0010\u001a\u00020$H\u00a7@\u00a2\u0006\u0002\u0010%J\u0018\u0010&\u001a\u00020\'2\b\b\u0001\u0010\u0010\u001a\u00020(H\u00a7@\u00a2\u0006\u0002\u0010)J\u0018\u0010*\u001a\u00020+2\b\b\u0001\u0010,\u001a\u00020-H\u00a7@\u00a2\u0006\u0002\u0010.J\u0018\u0010/\u001a\u0002002\b\b\u0001\u0010\u0010\u001a\u000201H\u00a7@\u00a2\u0006\u0002\u00102J\u0018\u00103\u001a\u0002042\b\b\u0001\u0010\u0010\u001a\u000205H\u00a7@\u00a2\u0006\u0002\u00106\u00a8\u00067"}, d2 = {"Lcom/quickchat/core/network/api/QuickChatApi;", "", "checkUsernameAvailability", "Lcom/quickchat/core/network/api/CheckUsernameResponse;", "username", "", "userId", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteAccountBackend", "Lcom/quickchat/core/network/api/DeleteAccountResponse;", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getPreKeyBundle", "Lcom/quickchat/core/network/api/PreKeyBundleResponse;", "phone", "googleLogin", "Lcom/quickchat/core/network/api/GoogleLoginResponse;", "request", "Lcom/quickchat/core/network/api/GoogleLoginRequest;", "(Lcom/quickchat/core/network/api/GoogleLoginRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "linkPhone", "Lcom/quickchat/core/network/api/LinkPhoneResponse;", "Lcom/quickchat/core/network/api/LinkPhoneRequest;", "(Lcom/quickchat/core/network/api/LinkPhoneRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "registerProfile", "Lcom/quickchat/core/network/api/RegisterProfileResponse;", "Lcom/quickchat/core/network/api/RegisterProfileRequest;", "(Lcom/quickchat/core/network/api/RegisterProfileRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "searchUser", "Lcom/quickchat/core/network/api/SearchUserResponse;", "requesterId", "syncContacts", "Lcom/quickchat/core/network/api/SyncContactsResponse;", "Lcom/quickchat/core/network/api/SyncContactsRequest;", "(Lcom/quickchat/core/network/api/SyncContactsRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updatePrivacy", "Lcom/quickchat/core/network/api/UpdatePrivacyResponse;", "Lcom/quickchat/core/network/api/UpdatePrivacyRequest;", "(Lcom/quickchat/core/network/api/UpdatePrivacyRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateUsername", "Lcom/quickchat/core/network/api/UpdateUsernameResponse;", "Lcom/quickchat/core/network/api/UpdateUsernameRequest;", "(Lcom/quickchat/core/network/api/UpdateUsernameRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "uploadMedia", "Lcom/quickchat/core/network/api/UploadMediaResponse;", "file", "Lokhttp3/MultipartBody$Part;", "(Lokhttp3/MultipartBody$Part;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "uploadPreKeys", "Lcom/quickchat/core/network/api/UploadPreKeysResponse;", "Lcom/quickchat/core/network/api/UploadPreKeysRequest;", "(Lcom/quickchat/core/network/api/UploadPreKeysRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "verifyOtp", "Lcom/quickchat/core/network/api/VerifyOtpResponse;", "Lcom/quickchat/core/network/api/VerifyOtpRequest;", "(Lcom/quickchat/core/network/api/VerifyOtpRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "network_debug"})
public abstract interface QuickChatApi {
    
    @retrofit2.http.POST(value = "auth/verify-otp")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object verifyOtp(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.quickchat.core.network.api.VerifyOtpRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.quickchat.core.network.api.VerifyOtpResponse> $completion);
    
    @retrofit2.http.POST(value = "auth/register-profile")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object registerProfile(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.quickchat.core.network.api.RegisterProfileRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.quickchat.core.network.api.RegisterProfileResponse> $completion);
    
    @retrofit2.http.POST(value = "contacts/sync")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object syncContacts(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.quickchat.core.network.api.SyncContactsRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.quickchat.core.network.api.SyncContactsResponse> $completion);
    
    @retrofit2.http.POST(value = "prekeys")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object uploadPreKeys(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.quickchat.core.network.api.UploadPreKeysRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.quickchat.core.network.api.UploadPreKeysResponse> $completion);
    
    @retrofit2.http.GET(value = "prekeys/{phone}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getPreKeyBundle(@retrofit2.http.Path(value = "phone")
    @org.jetbrains.annotations.NotNull()
    java.lang.String phone, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.quickchat.core.network.api.PreKeyBundleResponse> $completion);
    
    @retrofit2.http.Multipart()
    @retrofit2.http.POST(value = "media/upload")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object uploadMedia(@retrofit2.http.Part()
    @org.jetbrains.annotations.NotNull()
    okhttp3.MultipartBody.Part file, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.quickchat.core.network.api.UploadMediaResponse> $completion);
    
    @retrofit2.http.POST(value = "auth/google")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object googleLogin(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.quickchat.core.network.api.GoogleLoginRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.quickchat.core.network.api.GoogleLoginResponse> $completion);
    
    @retrofit2.http.POST(value = "auth/link-phone")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object linkPhone(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.quickchat.core.network.api.LinkPhoneRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.quickchat.core.network.api.LinkPhoneResponse> $completion);
    
    @retrofit2.http.GET(value = "auth/check-username")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object checkUsernameAvailability(@retrofit2.http.Query(value = "username")
    @org.jetbrains.annotations.NotNull()
    java.lang.String username, @retrofit2.http.Query(value = "userId")
    @org.jetbrains.annotations.Nullable()
    java.lang.String userId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.quickchat.core.network.api.CheckUsernameResponse> $completion);
    
    @retrofit2.http.POST(value = "auth/update-username")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateUsername(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.quickchat.core.network.api.UpdateUsernameRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.quickchat.core.network.api.UpdateUsernameResponse> $completion);
    
    @retrofit2.http.GET(value = "users/search")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object searchUser(@retrofit2.http.Query(value = "username")
    @org.jetbrains.annotations.NotNull()
    java.lang.String username, @retrofit2.http.Query(value = "requesterId")
    @org.jetbrains.annotations.NotNull()
    java.lang.String requesterId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.quickchat.core.network.api.SearchUserResponse> $completion);
    
    @retrofit2.http.POST(value = "users/privacy")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updatePrivacy(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.quickchat.core.network.api.UpdatePrivacyRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.quickchat.core.network.api.UpdatePrivacyResponse> $completion);
    
    @retrofit2.http.DELETE(value = "auth/account/{userId}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteAccountBackend(@retrofit2.http.Path(value = "userId")
    @org.jetbrains.annotations.NotNull()
    java.lang.String userId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.quickchat.core.network.api.DeleteAccountResponse> $completion);
}