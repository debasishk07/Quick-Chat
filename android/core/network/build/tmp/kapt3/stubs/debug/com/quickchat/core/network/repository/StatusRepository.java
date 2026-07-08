package com.quickchat.core.network.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0012\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J\u0014\u0010\u0002\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00040\u0003H&J\u000e\u0010\u0006\u001a\u00020\u0007H\u00a6@\u00a2\u0006\u0002\u0010\bJ(\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\b\u0010\r\u001a\u0004\u0018\u00010\u000e2\u0006\u0010\u000f\u001a\u00020\u0010H\u00a6@\u00a2\u0006\u0002\u0010\u0011\u00a8\u0006\u0012"}, d2 = {"Lcom/quickchat/core/network/repository/StatusRepository;", "", "getStatusesFlow", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/quickchat/core/model/UserStatus;", "purgeExpiredStatuses", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "uploadStatus", "", "mediaBytes", "", "caption", "", "mediaType", "Lcom/quickchat/core/model/StatusMediaType;", "([BLjava/lang/String;Lcom/quickchat/core/model/StatusMediaType;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "network_debug"})
public abstract interface StatusRepository {
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.quickchat.core.model.UserStatus>> getStatusesFlow();
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object uploadStatus(@org.jetbrains.annotations.NotNull()
    byte[] mediaBytes, @org.jetbrains.annotations.Nullable()
    java.lang.String caption, @org.jetbrains.annotations.NotNull()
    com.quickchat.core.model.StatusMediaType mediaType, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object purgeExpiredStatuses(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}