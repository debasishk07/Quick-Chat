package com.quickchat.core.network.repository;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000T\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0012\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u001f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u0014\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\r0\fH\u0016J\u000e\u0010\u000f\u001a\u00020\u0010H\u0096@\u00a2\u0006\u0002\u0010\u0011J(\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u00152\b\u0010\u0016\u001a\u0004\u0018\u00010\u00172\u0006\u0010\u0018\u001a\u00020\u0019H\u0096@\u00a2\u0006\u0002\u0010\u001aR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001b"}, d2 = {"Lcom/quickchat/core/network/repository/StatusRepositoryImpl;", "Lcom/quickchat/core/network/repository/StatusRepository;", "api", "Lcom/quickchat/core/network/api/QuickChatApi;", "statusDao", "Lcom/quickchat/core/database/dao/StatusDao;", "userRepository", "Lcom/quickchat/core/network/repository/UserRepository;", "(Lcom/quickchat/core/network/api/QuickChatApi;Lcom/quickchat/core/database/dao/StatusDao;Lcom/quickchat/core/network/repository/UserRepository;)V", "gson", "Lcom/google/gson/Gson;", "getStatusesFlow", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/quickchat/core/model/UserStatus;", "purgeExpiredStatuses", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "uploadStatus", "", "mediaBytes", "", "caption", "", "mediaType", "Lcom/quickchat/core/model/StatusMediaType;", "([BLjava/lang/String;Lcom/quickchat/core/model/StatusMediaType;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "network_debug"})
public final class StatusRepositoryImpl implements com.quickchat.core.network.repository.StatusRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.quickchat.core.network.api.QuickChatApi api = null;
    @org.jetbrains.annotations.NotNull()
    private final com.quickchat.core.database.dao.StatusDao statusDao = null;
    @org.jetbrains.annotations.NotNull()
    private final com.quickchat.core.network.repository.UserRepository userRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.google.gson.Gson gson = null;
    
    @javax.inject.Inject()
    public StatusRepositoryImpl(@org.jetbrains.annotations.NotNull()
    com.quickchat.core.network.api.QuickChatApi api, @org.jetbrains.annotations.NotNull()
    com.quickchat.core.database.dao.StatusDao statusDao, @org.jetbrains.annotations.NotNull()
    com.quickchat.core.network.repository.UserRepository userRepository) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.util.List<com.quickchat.core.model.UserStatus>> getStatusesFlow() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object uploadStatus(@org.jetbrains.annotations.NotNull()
    byte[] mediaBytes, @org.jetbrains.annotations.Nullable()
    java.lang.String caption, @org.jetbrains.annotations.NotNull()
    com.quickchat.core.model.StatusMediaType mediaType, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object purgeExpiredStatuses(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
}