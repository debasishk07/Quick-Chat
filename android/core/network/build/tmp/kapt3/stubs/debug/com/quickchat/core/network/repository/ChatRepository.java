package com.quickchat.core.network.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010$\n\u0002\u0010\u000e\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0005\bf\u0018\u00002\u00020\u0001J\u0016\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\u0005H\u00a6@\u00a2\u0006\u0002\u0010\fJ\b\u0010\r\u001a\u00020\nH&J\u0018\u0010\u000e\u001a\u0004\u0018\u00010\u000f2\u0006\u0010\u000b\u001a\u00020\u0005H\u00a6@\u00a2\u0006\u0002\u0010\fJ\u0014\u0010\u0010\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\u00120\u0011H&J\u001c\u0010\u0013\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00140\u00120\u00112\u0006\u0010\u0015\u001a\u00020\u0005H&J\u0010\u0010\u0016\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\u0005H&J\u001c\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00140\u00122\u0006\u0010\u0018\u001a\u00020\u0005H\u00a6@\u00a2\u0006\u0002\u0010\fJ&\u0010\u0019\u001a\u00020\u00052\u0006\u0010\u0015\u001a\u00020\u00052\u0006\u0010\u001a\u001a\u00020\u00052\u0006\u0010\u001b\u001a\u00020\u001cH\u00a6@\u00a2\u0006\u0002\u0010\u001dJ\u001e\u0010\u001e\u001a\u00020\n2\u0006\u0010\u0015\u001a\u00020\u00052\u0006\u0010\u001f\u001a\u00020\u0006H\u00a6@\u00a2\u0006\u0002\u0010 R$\u0010\u0002\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00060\u00040\u0003X\u00a6\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0007\u0010\b\u00a8\u0006!"}, d2 = {"Lcom/quickchat/core/network/repository/ChatRepository;", "", "activeTypingState", "Lkotlinx/coroutines/flow/StateFlow;", "", "", "", "getActiveTypingState", "()Lkotlinx/coroutines/flow/StateFlow;", "clearUnreadCount", "", "phone", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "closeSocketConnection", "getChat", "Lcom/quickchat/core/model/Chat;", "getChatsFlow", "Lkotlinx/coroutines/flow/Flow;", "", "getMessagesFlow", "Lcom/quickchat/core/model/Message;", "recipientPhone", "initSocketConnection", "searchMessages", "query", "sendMessage", "messageText", "type", "Lcom/quickchat/core/model/MessageType;", "(Ljava/lang/String;Ljava/lang/String;Lcom/quickchat/core/model/MessageType;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "sendTyping", "isTyping", "(Ljava/lang/String;ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "network_debug"})
public abstract interface ChatRepository {
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.quickchat.core.model.Chat>> getChatsFlow();
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.quickchat.core.model.Message>> getMessagesFlow(@org.jetbrains.annotations.NotNull()
    java.lang.String recipientPhone);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object sendMessage(@org.jetbrains.annotations.NotNull()
    java.lang.String recipientPhone, @org.jetbrains.annotations.NotNull()
    java.lang.String messageText, @org.jetbrains.annotations.NotNull()
    com.quickchat.core.model.MessageType type, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object sendTyping(@org.jetbrains.annotations.NotNull()
    java.lang.String recipientPhone, boolean isTyping, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object searchMessages(@org.jetbrains.annotations.NotNull()
    java.lang.String query, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.quickchat.core.model.Message>> $completion);
    
    public abstract void initSocketConnection(@org.jetbrains.annotations.NotNull()
    java.lang.String phone);
    
    public abstract void closeSocketConnection();
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.StateFlow<java.util.Map<java.lang.String, java.lang.Boolean>> getActiveTypingState();
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getChat(@org.jetbrains.annotations.NotNull()
    java.lang.String phone, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.quickchat.core.model.Chat> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object clearUnreadCount(@org.jetbrains.annotations.NotNull()
    java.lang.String phone, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}