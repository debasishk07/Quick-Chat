package com.quickchat.core.network.repository;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u008e\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010$\n\u0002\u0010\u000e\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0002\b\u000b\b\u0007\u0018\u00002\u00020\u0001B?\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u0012\u0006\u0010\f\u001a\u00020\r\u0012\u0006\u0010\u000e\u001a\u00020\u000f\u00a2\u0006\u0002\u0010\u0010J\u0016\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u0014H\u0096@\u00a2\u0006\u0002\u0010\u001fJ\b\u0010 \u001a\u00020\u001dH\u0016J\u0010\u0010!\u001a\u00020\"2\u0006\u0010#\u001a\u00020\u0014H\u0002J\u0018\u0010$\u001a\u0004\u0018\u00010%2\u0006\u0010\u001e\u001a\u00020\u0014H\u0096@\u00a2\u0006\u0002\u0010\u001fJ\u0014\u0010&\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020%0(0\'H\u0016J\u001c\u0010)\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020*0(0\'2\u0006\u0010+\u001a\u00020\u0014H\u0016J\"\u0010,\u001a\u00020\"2\u0006\u0010-\u001a\u00020\u00142\n\b\u0002\u0010.\u001a\u0004\u0018\u00010\u0014H\u0082@\u00a2\u0006\u0002\u0010/J\u0016\u00100\u001a\u00020\u001d2\u0006\u00101\u001a\u000202H\u0082@\u00a2\u0006\u0002\u00103J\u0010\u00104\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u0014H\u0016J\b\u00105\u001a\u00020\u001dH\u0002J\u001e\u00106\u001a\u00020\u001d2\u0006\u0010-\u001a\u00020\u00142\u0006\u00107\u001a\u00020\"H\u0082@\u00a2\u0006\u0002\u00108J\u001c\u00109\u001a\b\u0012\u0004\u0012\u00020*0(2\u0006\u0010:\u001a\u00020\u0014H\u0096@\u00a2\u0006\u0002\u0010\u001fJ&\u0010;\u001a\u00020\u00142\u0006\u0010+\u001a\u00020\u00142\u0006\u0010<\u001a\u00020\u00142\u0006\u0010=\u001a\u00020>H\u0096@\u00a2\u0006\u0002\u0010?J\u001e\u0010@\u001a\u00020\u001d2\u0006\u0010+\u001a\u00020\u00142\u0006\u0010A\u001a\u00020\u0015H\u0096@\u00a2\u0006\u0002\u0010BJ\u0010\u0010C\u001a\u00020\u00142\u0006\u00107\u001a\u00020\"H\u0002J\u000e\u0010D\u001a\u00020\u001dH\u0082@\u00a2\u0006\u0002\u0010EJ\u001e\u0010F\u001a\u00020\u001d2\u0006\u0010+\u001a\u00020\u00142\u0006\u0010G\u001a\u00020*H\u0082@\u00a2\u0006\u0002\u0010HR \u0010\u0011\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u00150\u00130\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R&\u0010\u0016\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u00150\u00130\u0017X\u0096\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001a\u001a\u00020\u001bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006I"}, d2 = {"Lcom/quickchat/core/network/repository/ChatRepositoryImpl;", "Lcom/quickchat/core/network/repository/ChatRepository;", "api", "Lcom/quickchat/core/network/api/QuickChatApi;", "socketManager", "Lcom/quickchat/core/network/websocket/SocketManager;", "chatDao", "Lcom/quickchat/core/database/dao/ChatDao;", "messageDao", "Lcom/quickchat/core/database/dao/MessageDao;", "sessionDao", "Lcom/quickchat/core/database/dao/SessionDao;", "outboxDao", "Lcom/quickchat/core/database/dao/OutboxDao;", "userRepository", "Lcom/quickchat/core/network/repository/UserRepository;", "(Lcom/quickchat/core/network/api/QuickChatApi;Lcom/quickchat/core/network/websocket/SocketManager;Lcom/quickchat/core/database/dao/ChatDao;Lcom/quickchat/core/database/dao/MessageDao;Lcom/quickchat/core/database/dao/SessionDao;Lcom/quickchat/core/database/dao/OutboxDao;Lcom/quickchat/core/network/repository/UserRepository;)V", "_activeTypingState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "", "", "activeTypingState", "Lkotlinx/coroutines/flow/StateFlow;", "getActiveTypingState", "()Lkotlinx/coroutines/flow/StateFlow;", "repositoryScope", "Lkotlinx/coroutines/CoroutineScope;", "clearUnreadCount", "", "phone", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "closeSocketConnection", "deserializeSession", "Lcom/quickchat/core/crypto/DoubleRatchetSession;", "jsonStr", "getChat", "Lcom/quickchat/core/model/Chat;", "getChatsFlow", "Lkotlinx/coroutines/flow/Flow;", "", "getMessagesFlow", "Lcom/quickchat/core/model/Message;", "recipientPhone", "getOrCreateSession", "partnerPhone", "incomingEphemeralKey", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "handleIncomingMessage", "socketMsg", "Lcom/quickchat/core/network/websocket/SocketMessage;", "(Lcom/quickchat/core/network/websocket/SocketMessage;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "initSocketConnection", "observeSocketEvents", "saveSession", "session", "(Ljava/lang/String;Lcom/quickchat/core/crypto/DoubleRatchetSession;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "searchMessages", "query", "sendMessage", "messageText", "type", "Lcom/quickchat/core/model/MessageType;", "(Ljava/lang/String;Ljava/lang/String;Lcom/quickchat/core/model/MessageType;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "sendTyping", "isTyping", "(Ljava/lang/String;ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "serializeSession", "syncOfflineOutbox", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateChatLastMessage", "msg", "(Ljava/lang/String;Lcom/quickchat/core/model/Message;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "network_debug"})
public final class ChatRepositoryImpl implements com.quickchat.core.network.repository.ChatRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.quickchat.core.network.api.QuickChatApi api = null;
    @org.jetbrains.annotations.NotNull()
    private final com.quickchat.core.network.websocket.SocketManager socketManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.quickchat.core.database.dao.ChatDao chatDao = null;
    @org.jetbrains.annotations.NotNull()
    private final com.quickchat.core.database.dao.MessageDao messageDao = null;
    @org.jetbrains.annotations.NotNull()
    private final com.quickchat.core.database.dao.SessionDao sessionDao = null;
    @org.jetbrains.annotations.NotNull()
    private final com.quickchat.core.database.dao.OutboxDao outboxDao = null;
    @org.jetbrains.annotations.NotNull()
    private final com.quickchat.core.network.repository.UserRepository userRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope repositoryScope = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.Map<java.lang.String, java.lang.Boolean>> _activeTypingState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.Map<java.lang.String, java.lang.Boolean>> activeTypingState = null;
    
    @javax.inject.Inject()
    public ChatRepositoryImpl(@org.jetbrains.annotations.NotNull()
    com.quickchat.core.network.api.QuickChatApi api, @org.jetbrains.annotations.NotNull()
    com.quickchat.core.network.websocket.SocketManager socketManager, @org.jetbrains.annotations.NotNull()
    com.quickchat.core.database.dao.ChatDao chatDao, @org.jetbrains.annotations.NotNull()
    com.quickchat.core.database.dao.MessageDao messageDao, @org.jetbrains.annotations.NotNull()
    com.quickchat.core.database.dao.SessionDao sessionDao, @org.jetbrains.annotations.NotNull()
    com.quickchat.core.database.dao.OutboxDao outboxDao, @org.jetbrains.annotations.NotNull()
    com.quickchat.core.network.repository.UserRepository userRepository) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.StateFlow<java.util.Map<java.lang.String, java.lang.Boolean>> getActiveTypingState() {
        return null;
    }
    
    @java.lang.Override()
    public void initSocketConnection(@org.jetbrains.annotations.NotNull()
    java.lang.String phone) {
    }
    
    @java.lang.Override()
    public void closeSocketConnection() {
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.util.List<com.quickchat.core.model.Chat>> getChatsFlow() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.util.List<com.quickchat.core.model.Message>> getMessagesFlow(@org.jetbrains.annotations.NotNull()
    java.lang.String recipientPhone) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object getChat(@org.jetbrains.annotations.NotNull()
    java.lang.String phone, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.quickchat.core.model.Chat> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object clearUnreadCount(@org.jetbrains.annotations.NotNull()
    java.lang.String phone, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object sendMessage(@org.jetbrains.annotations.NotNull()
    java.lang.String recipientPhone, @org.jetbrains.annotations.NotNull()
    java.lang.String messageText, @org.jetbrains.annotations.NotNull()
    com.quickchat.core.model.MessageType type, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object sendTyping(@org.jetbrains.annotations.NotNull()
    java.lang.String recipientPhone, boolean isTyping, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object searchMessages(@org.jetbrains.annotations.NotNull()
    java.lang.String query, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.quickchat.core.model.Message>> $completion) {
        return null;
    }
    
    private final java.lang.Object syncOfflineOutbox(kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final void observeSocketEvents() {
    }
    
    private final java.lang.Object handleIncomingMessage(com.quickchat.core.network.websocket.SocketMessage socketMsg, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.Object updateChatLastMessage(java.lang.String recipientPhone, com.quickchat.core.model.Message msg, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.Object getOrCreateSession(java.lang.String partnerPhone, java.lang.String incomingEphemeralKey, kotlin.coroutines.Continuation<? super com.quickchat.core.crypto.DoubleRatchetSession> $completion) {
        return null;
    }
    
    private final java.lang.Object saveSession(java.lang.String partnerPhone, com.quickchat.core.crypto.DoubleRatchetSession session, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.String serializeSession(com.quickchat.core.crypto.DoubleRatchetSession session) {
        return null;
    }
    
    private final com.quickchat.core.crypto.DoubleRatchetSession deserializeSession(java.lang.String jsonStr) {
        return null;
    }
}