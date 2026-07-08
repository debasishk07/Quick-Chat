package com.quickchat.core.network.websocket;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000T\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\n\n\u0002\u0010\u000b\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0002J\u0016\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u001dJ\u0006\u0010\u001f\u001a\u00020\u001bJ\u000e\u0010 \u001a\u00020\u001b2\u0006\u0010!\u001a\u00020\u0005J\u001e\u0010\"\u001a\u00020\u001b2\u0006\u0010#\u001a\u00020\u001d2\u0006\u0010$\u001a\u00020\u001d2\u0006\u0010%\u001a\u00020\u001dJ\u0016\u0010&\u001a\u00020\u001b2\u0006\u0010$\u001a\u00020\u001d2\u0006\u0010\'\u001a\u00020(J\b\u0010)\u001a\u00020\u001bH\u0002R\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00070\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\b\u001a\b\u0012\u0004\u0012\u00020\t0\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00050\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0017\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00070\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0011R\u0017\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\t0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0011R\u0010\u0010\u0016\u001a\u0004\u0018\u00010\u0017X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u000b0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u0011\u00a8\u0006*"}, d2 = {"Lcom/quickchat/core/network/websocket/SocketManager;", "", "()V", "_incomingMessages", "Lkotlinx/coroutines/flow/MutableSharedFlow;", "Lcom/quickchat/core/network/websocket/SocketMessage;", "_messageReceipts", "Lcom/quickchat/core/network/websocket/SocketReceipt;", "_presenceChanges", "Lcom/quickchat/core/network/websocket/SocketPresence;", "_typingNotifications", "Lcom/quickchat/core/network/websocket/SocketTyping;", "gson", "Lcom/google/gson/Gson;", "incomingMessages", "Lkotlinx/coroutines/flow/SharedFlow;", "getIncomingMessages", "()Lkotlinx/coroutines/flow/SharedFlow;", "messageReceipts", "getMessageReceipts", "presenceChanges", "getPresenceChanges", "socket", "Lio/socket/client/Socket;", "typingNotifications", "getTypingNotifications", "connect", "", "baseUrl", "", "phone", "disconnect", "sendMessage", "msg", "sendReceipt", "messageId", "recipientPhone", "status", "sendTyping", "isTyping", "", "setupEventListeners", "network_debug"})
public final class SocketManager {
    @org.jetbrains.annotations.Nullable()
    private io.socket.client.Socket socket;
    @org.jetbrains.annotations.NotNull()
    private final com.google.gson.Gson gson = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableSharedFlow<com.quickchat.core.network.websocket.SocketMessage> _incomingMessages = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.SharedFlow<com.quickchat.core.network.websocket.SocketMessage> incomingMessages = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableSharedFlow<com.quickchat.core.network.websocket.SocketReceipt> _messageReceipts = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.SharedFlow<com.quickchat.core.network.websocket.SocketReceipt> messageReceipts = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableSharedFlow<com.quickchat.core.network.websocket.SocketPresence> _presenceChanges = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.SharedFlow<com.quickchat.core.network.websocket.SocketPresence> presenceChanges = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableSharedFlow<com.quickchat.core.network.websocket.SocketTyping> _typingNotifications = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.SharedFlow<com.quickchat.core.network.websocket.SocketTyping> typingNotifications = null;
    
    @javax.inject.Inject()
    public SocketManager() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.SharedFlow<com.quickchat.core.network.websocket.SocketMessage> getIncomingMessages() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.SharedFlow<com.quickchat.core.network.websocket.SocketReceipt> getMessageReceipts() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.SharedFlow<com.quickchat.core.network.websocket.SocketPresence> getPresenceChanges() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.SharedFlow<com.quickchat.core.network.websocket.SocketTyping> getTypingNotifications() {
        return null;
    }
    
    public final void connect(@org.jetbrains.annotations.NotNull()
    java.lang.String baseUrl, @org.jetbrains.annotations.NotNull()
    java.lang.String phone) {
    }
    
    public final void disconnect() {
    }
    
    private final void setupEventListeners() {
    }
    
    public final void sendMessage(@org.jetbrains.annotations.NotNull()
    com.quickchat.core.network.websocket.SocketMessage msg) {
    }
    
    public final void sendReceipt(@org.jetbrains.annotations.NotNull()
    java.lang.String messageId, @org.jetbrains.annotations.NotNull()
    java.lang.String recipientPhone, @org.jetbrains.annotations.NotNull()
    java.lang.String status) {
    }
    
    public final void sendTyping(@org.jetbrains.annotations.NotNull()
    java.lang.String recipientPhone, boolean isTyping) {
    }
}