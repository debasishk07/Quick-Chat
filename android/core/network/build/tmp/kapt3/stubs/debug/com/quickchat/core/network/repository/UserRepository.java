package com.quickchat.core.network.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u000f\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0010 \n\u0002\b\t\n\u0002\u0010\u0012\n\u0002\b\u0003\bf\u0018\u00002\u00020\u0001J\u0018\u0010\u0007\u001a\u0004\u0018\u00010\b2\u0006\u0010\t\u001a\u00020\bH\u00a6@\u00a2\u0006\u0002\u0010\nJ\u000e\u0010\u000b\u001a\u00020\fH\u00a6@\u00a2\u0006\u0002\u0010\rJ\n\u0010\u000e\u001a\u0004\u0018\u00010\u000fH&J\u0012\u0010\u0010\u001a\u0004\u0018\u00010\u000f2\u0006\u0010\u0011\u001a\u00020\bH&J\n\u0010\u0012\u001a\u0004\u0018\u00010\u000fH&J0\u0010\u0013\u001a\u00020\f2\u0006\u0010\u0014\u001a\u00020\b2\u0006\u0010\u0015\u001a\u00020\b2\u0006\u0010\u0016\u001a\u00020\b2\b\u0010\u0017\u001a\u0004\u0018\u00010\bH\u00a6@\u00a2\u0006\u0002\u0010\u0018J&\u0010\u0019\u001a\u00020\f2\u0006\u0010\u001a\u001a\u00020\b2\u0006\u0010\u001b\u001a\u00020\b2\u0006\u0010\u001c\u001a\u00020\bH\u00a6@\u00a2\u0006\u0002\u0010\u001dJ\b\u0010\u001e\u001a\u00020\u001fH&J2\u0010 \u001a\u00020\f2\u0006\u0010\u001b\u001a\u00020\b2\u0006\u0010\u0016\u001a\u00020\b2\b\u0010\u0017\u001a\u0004\u0018\u00010\b2\b\u0010!\u001a\u0004\u0018\u00010\bH\u00a6@\u00a2\u0006\u0002\u0010\u0018J\u0016\u0010\"\u001a\u00020\u00042\u0006\u0010\t\u001a\u00020\bH\u00a6@\u00a2\u0006\u0002\u0010\nJ\"\u0010#\u001a\b\u0012\u0004\u0012\u00020\u00040$2\f\u0010%\u001a\b\u0012\u0004\u0012\u00020\b0$H\u00a6@\u00a2\u0006\u0002\u0010&J\u001e\u0010\'\u001a\u00020\f2\u0006\u0010\u001a\u001a\u00020\b2\u0006\u0010(\u001a\u00020\fH\u00a6@\u00a2\u0006\u0002\u0010)J \u0010*\u001a\u0004\u0018\u00010\b2\u0006\u0010\u001a\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\bH\u00a6@\u00a2\u0006\u0002\u0010+J\u0018\u0010,\u001a\u0004\u0018\u00010\b2\u0006\u0010-\u001a\u00020.H\u00a6@\u00a2\u0006\u0002\u0010/J\u001e\u00100\u001a\u00020\f2\u0006\u0010\u001b\u001a\u00020\b2\u0006\u0010\u001c\u001a\u00020\bH\u00a6@\u00a2\u0006\u0002\u0010+R\u001a\u0010\u0002\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00040\u0003X\u00a6\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0005\u0010\u0006\u00a8\u00061"}, d2 = {"Lcom/quickchat/core/network/repository/UserRepository;", "", "currentUser", "Lkotlinx/coroutines/flow/StateFlow;", "Lcom/quickchat/core/model/User;", "getCurrentUser", "()Lkotlinx/coroutines/flow/StateFlow;", "checkUsernameAvailability", "", "username", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteAccount", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getLocalIdentityKey", "Ljava/security/KeyPair;", "getLocalOneTimePreKey", "publicKeyBase64", "getLocalSignedPreKey", "googleLogin", "googleUid", "email", "displayName", "avatarUrl", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "linkPhone", "userId", "phone", "code", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "logout", "", "registerProfile", "about", "searchUserByUsername", "syncContacts", "", "phones", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updatePrivacy", "enabled", "(Ljava/lang/String;ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateUsername", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "uploadAvatar", "mediaBytes", "", "([BLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "verifyOtp", "network_debug"})
public abstract interface UserRepository {
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.StateFlow<com.quickchat.core.model.User> getCurrentUser();
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object verifyOtp(@org.jetbrains.annotations.NotNull()
    java.lang.String phone, @org.jetbrains.annotations.NotNull()
    java.lang.String code, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object registerProfile(@org.jetbrains.annotations.NotNull()
    java.lang.String phone, @org.jetbrains.annotations.NotNull()
    java.lang.String displayName, @org.jetbrains.annotations.Nullable()
    java.lang.String avatarUrl, @org.jetbrains.annotations.Nullable()
    java.lang.String about, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object syncContacts(@org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> phones, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.quickchat.core.model.User>> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.security.KeyPair getLocalIdentityKey();
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.security.KeyPair getLocalSignedPreKey();
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.security.KeyPair getLocalOneTimePreKey(@org.jetbrains.annotations.NotNull()
    java.lang.String publicKeyBase64);
    
    public abstract void logout();
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteAccount(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object uploadAvatar(@org.jetbrains.annotations.NotNull()
    byte[] mediaBytes, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object googleLogin(@org.jetbrains.annotations.NotNull()
    java.lang.String googleUid, @org.jetbrains.annotations.NotNull()
    java.lang.String email, @org.jetbrains.annotations.NotNull()
    java.lang.String displayName, @org.jetbrains.annotations.Nullable()
    java.lang.String avatarUrl, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object linkPhone(@org.jetbrains.annotations.NotNull()
    java.lang.String userId, @org.jetbrains.annotations.NotNull()
    java.lang.String phone, @org.jetbrains.annotations.NotNull()
    java.lang.String code, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateUsername(@org.jetbrains.annotations.NotNull()
    java.lang.String userId, @org.jetbrains.annotations.NotNull()
    java.lang.String username, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updatePrivacy(@org.jetbrains.annotations.NotNull()
    java.lang.String userId, boolean enabled, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object searchUserByUsername(@org.jetbrains.annotations.NotNull()
    java.lang.String username, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.quickchat.core.model.User> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object checkUsernameAvailability(@org.jetbrains.annotations.NotNull()
    java.lang.String username, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion);
}