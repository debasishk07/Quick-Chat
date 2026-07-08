package com.quickchat.core.network.repository;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000n\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0014\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010 \n\u0002\b\t\n\u0002\u0010\u0012\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\u001f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u0018\u0010\u0012\u001a\u0004\u0018\u00010\u00132\u0006\u0010\u0014\u001a\u00020\u0013H\u0096@\u00a2\u0006\u0002\u0010\u0015J\u000e\u0010\u0016\u001a\u00020\u0017H\u0096@\u00a2\u0006\u0002\u0010\u0018J\u0016\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\u0013H\u0082@\u00a2\u0006\u0002\u0010\u0015J\u0012\u0010\u001c\u001a\u0004\u0018\u00010\u001d2\u0006\u0010\u001e\u001a\u00020\u0013H\u0002J\n\u0010\u001f\u001a\u0004\u0018\u00010\u001dH\u0016J\u0012\u0010 \u001a\u0004\u0018\u00010\u001d2\u0006\u0010!\u001a\u00020\u0013H\u0016J\n\u0010\"\u001a\u0004\u0018\u00010\u001dH\u0016J0\u0010#\u001a\u00020\u00172\u0006\u0010$\u001a\u00020\u00132\u0006\u0010%\u001a\u00020\u00132\u0006\u0010&\u001a\u00020\u00132\b\u0010\'\u001a\u0004\u0018\u00010\u0013H\u0096@\u00a2\u0006\u0002\u0010(J\u0010\u0010)\u001a\u00020\u001d2\u0006\u0010*\u001a\u00020\u0013H\u0002J&\u0010+\u001a\u00020\u00172\u0006\u0010,\u001a\u00020\u00132\u0006\u0010\u001b\u001a\u00020\u00132\u0006\u0010-\u001a\u00020\u0013H\u0096@\u00a2\u0006\u0002\u0010.J\b\u0010/\u001a\u00020\u001aH\u0016J\u0010\u00100\u001a\u00020\u000b2\u0006\u00101\u001a\u000202H\u0002J2\u00103\u001a\u00020\u00172\u0006\u0010\u001b\u001a\u00020\u00132\u0006\u0010&\u001a\u00020\u00132\b\u0010\'\u001a\u0004\u0018\u00010\u00132\b\u00104\u001a\u0004\u0018\u00010\u0013H\u0096@\u00a2\u0006\u0002\u0010(J\u0018\u00105\u001a\u00020\u001a2\u0006\u0010\u001e\u001a\u00020\u00132\u0006\u00106\u001a\u00020\u001dH\u0002J\u0010\u00107\u001a\u00020\u001a2\u0006\u00108\u001a\u00020\u000bH\u0002J\u0016\u00109\u001a\u00020\u000b2\u0006\u0010\u0014\u001a\u00020\u0013H\u0096@\u00a2\u0006\u0002\u0010\u0015J\"\u0010:\u001a\b\u0012\u0004\u0012\u00020\u000b0;2\f\u0010<\u001a\b\u0012\u0004\u0012\u00020\u00130;H\u0096@\u00a2\u0006\u0002\u0010=J\u001e\u0010>\u001a\u00020\u00172\u0006\u0010,\u001a\u00020\u00132\u0006\u0010?\u001a\u00020\u0017H\u0096@\u00a2\u0006\u0002\u0010@J \u0010A\u001a\u0004\u0018\u00010\u00132\u0006\u0010,\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u0013H\u0096@\u00a2\u0006\u0002\u0010BJ\u0018\u0010C\u001a\u0004\u0018\u00010\u00132\u0006\u0010D\u001a\u00020EH\u0096@\u00a2\u0006\u0002\u0010FJ\u001e\u0010G\u001a\u00020\u00172\u0006\u0010\u001b\u001a\u00020\u00132\u0006\u0010-\u001a\u00020\u0013H\u0096@\u00a2\u0006\u0002\u0010BR\u0016\u0010\t\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000b0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001c\u0010\f\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000b0\rX\u0096\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006H"}, d2 = {"Lcom/quickchat/core/network/repository/UserRepositoryImpl;", "Lcom/quickchat/core/network/repository/UserRepository;", "api", "Lcom/quickchat/core/network/api/QuickChatApi;", "prefs", "Landroid/content/SharedPreferences;", "db", "Lcom/quickchat/core/database/AppDatabase;", "(Lcom/quickchat/core/network/api/QuickChatApi;Landroid/content/SharedPreferences;Lcom/quickchat/core/database/AppDatabase;)V", "_currentUser", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/quickchat/core/model/User;", "currentUser", "Lkotlinx/coroutines/flow/StateFlow;", "getCurrentUser", "()Lkotlinx/coroutines/flow/StateFlow;", "gson", "Lcom/google/gson/Gson;", "checkUsernameAvailability", "", "username", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteAccount", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "generateAndPublishPreKeys", "", "phone", "getKeyPair", "Ljava/security/KeyPair;", "key", "getLocalIdentityKey", "getLocalOneTimePreKey", "publicKeyBase64", "getLocalSignedPreKey", "googleLogin", "googleUid", "email", "displayName", "avatarUrl", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "keyPairFromJson", "jsonStr", "linkPhone", "userId", "code", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "logout", "mapApiUser", "it", "Lcom/quickchat/core/network/api/ApiUser;", "registerProfile", "about", "saveKeyPair", "keyPair", "saveUserLocally", "user", "searchUserByUsername", "syncContacts", "", "phones", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updatePrivacy", "enabled", "(Ljava/lang/String;ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateUsername", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "uploadAvatar", "mediaBytes", "", "([BLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "verifyOtp", "network_debug"})
public final class UserRepositoryImpl implements com.quickchat.core.network.repository.UserRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.quickchat.core.network.api.QuickChatApi api = null;
    @org.jetbrains.annotations.NotNull()
    private final android.content.SharedPreferences prefs = null;
    @org.jetbrains.annotations.NotNull()
    private final com.quickchat.core.database.AppDatabase db = null;
    @org.jetbrains.annotations.NotNull()
    private final com.google.gson.Gson gson = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.quickchat.core.model.User> _currentUser = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.quickchat.core.model.User> currentUser = null;
    
    @javax.inject.Inject()
    public UserRepositoryImpl(@org.jetbrains.annotations.NotNull()
    com.quickchat.core.network.api.QuickChatApi api, @org.jetbrains.annotations.NotNull()
    android.content.SharedPreferences prefs, @org.jetbrains.annotations.NotNull()
    com.quickchat.core.database.AppDatabase db) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.StateFlow<com.quickchat.core.model.User> getCurrentUser() {
        return null;
    }
    
    private final com.quickchat.core.model.User mapApiUser(com.quickchat.core.network.api.ApiUser it) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object verifyOtp(@org.jetbrains.annotations.NotNull()
    java.lang.String phone, @org.jetbrains.annotations.NotNull()
    java.lang.String code, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object registerProfile(@org.jetbrains.annotations.NotNull()
    java.lang.String phone, @org.jetbrains.annotations.NotNull()
    java.lang.String displayName, @org.jetbrains.annotations.Nullable()
    java.lang.String avatarUrl, @org.jetbrains.annotations.Nullable()
    java.lang.String about, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object syncContacts(@org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> phones, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.quickchat.core.model.User>> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object googleLogin(@org.jetbrains.annotations.NotNull()
    java.lang.String googleUid, @org.jetbrains.annotations.NotNull()
    java.lang.String email, @org.jetbrains.annotations.NotNull()
    java.lang.String displayName, @org.jetbrains.annotations.Nullable()
    java.lang.String avatarUrl, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object linkPhone(@org.jetbrains.annotations.NotNull()
    java.lang.String userId, @org.jetbrains.annotations.NotNull()
    java.lang.String phone, @org.jetbrains.annotations.NotNull()
    java.lang.String code, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object updateUsername(@org.jetbrains.annotations.NotNull()
    java.lang.String userId, @org.jetbrains.annotations.NotNull()
    java.lang.String username, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object updatePrivacy(@org.jetbrains.annotations.NotNull()
    java.lang.String userId, boolean enabled, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object checkUsernameAvailability(@org.jetbrains.annotations.NotNull()
    java.lang.String username, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object searchUserByUsername(@org.jetbrains.annotations.NotNull()
    java.lang.String username, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.quickchat.core.model.User> $completion) {
        return null;
    }
    
    private final void saveUserLocally(com.quickchat.core.model.User user) {
    }
    
    @java.lang.Override()
    public void logout() {
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object deleteAccount(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object uploadAvatar(@org.jetbrains.annotations.NotNull()
    byte[] mediaBytes, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.security.KeyPair getLocalIdentityKey() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.security.KeyPair getLocalSignedPreKey() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.security.KeyPair getLocalOneTimePreKey(@org.jetbrains.annotations.NotNull()
    java.lang.String publicKeyBase64) {
        return null;
    }
    
    private final java.lang.Object generateAndPublishPreKeys(java.lang.String phone, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final void saveKeyPair(java.lang.String key, java.security.KeyPair keyPair) {
    }
    
    private final java.security.KeyPair getKeyPair(java.lang.String key) {
        return null;
    }
    
    private final java.security.KeyPair keyPairFromJson(java.lang.String jsonStr) {
        return null;
    }
}