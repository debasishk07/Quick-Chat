package com.quickchat.feature.chat;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000`\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u000e\n\u0002\u0010\u0002\n\u0002\b\u0007\b\u0007\u0018\u00002\u00020\u0001B\'\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\u0010\u0010*\u001a\u00020+2\u0006\u0010,\u001a\u00020\u0015H\u0002J\u000e\u0010-\u001a\u00020+2\u0006\u0010,\u001a\u00020\u0015J\u000e\u0010.\u001a\u00020+2\u0006\u0010/\u001a\u00020\u0015J\u000e\u00100\u001a\u00020+2\u0006\u00101\u001a\u00020\rR\u0014\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u000e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00100\u000f0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00120\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00150\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00150\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0017\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00150\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\r0\u0019\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u001aR\u0010\u0010\u001b\u001a\u0004\u0018\u00010\u001cX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u001d\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00100\u000f0\u0019\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001aR\u0017\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u00120\u0019\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\u001aR\u0017\u0010!\u001a\b\u0012\u0004\u0012\u00020\r0\u0019\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010\u001aR\u0010\u0010#\u001a\u0004\u0018\u00010\u001cX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0017\u0010$\u001a\b\u0012\u0004\u0012\u00020\u00150\u0019\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010\u001aR\u0017\u0010&\u001a\b\u0012\u0004\u0012\u00020\u00150\u0019\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010\u001aR\u0019\u0010(\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00150\u0019\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010\u001aR\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u00062"}, d2 = {"Lcom/quickchat/feature/chat/ChatRoomViewModel;", "Landroidx/lifecycle/ViewModel;", "chatRepository", "Lcom/quickchat/core/network/repository/ChatRepository;", "userRepository", "Lcom/quickchat/core/network/repository/UserRepository;", "socketManager", "Lcom/quickchat/core/network/websocket/SocketManager;", "api", "Lcom/quickchat/core/network/api/QuickChatApi;", "(Lcom/quickchat/core/network/repository/ChatRepository;Lcom/quickchat/core/network/repository/UserRepository;Lcom/quickchat/core/network/websocket/SocketManager;Lcom/quickchat/core/network/api/QuickChatApi;)V", "_isPartnerOnline", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "_messages", "", "Lcom/quickchat/core/model/Message;", "_partnerLastSeen", "", "_partnerTyping", "_recipientName", "", "_recipientPhone", "_securityFingerprint", "isPartnerOnline", "Lkotlinx/coroutines/flow/StateFlow;", "()Lkotlinx/coroutines/flow/StateFlow;", "messageCollectionJob", "Lkotlinx/coroutines/Job;", "messages", "getMessages", "partnerLastSeen", "getPartnerLastSeen", "partnerTyping", "getPartnerTyping", "presenceCollectionJob", "recipientName", "getRecipientName", "recipientPhone", "getRecipientPhone", "securityFingerprint", "getSecurityFingerprint", "computeSecurityFingerprint", "", "phone", "initRecipient", "sendMessage", "text", "sendTyping", "isTyping", "chat_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class ChatRoomViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.quickchat.core.network.repository.ChatRepository chatRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.quickchat.core.network.repository.UserRepository userRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.quickchat.core.network.websocket.SocketManager socketManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.quickchat.core.network.api.QuickChatApi api = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _recipientPhone = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> recipientPhone = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _recipientName = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> recipientName = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.quickchat.core.model.Message>> _messages = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.quickchat.core.model.Message>> messages = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isPartnerOnline = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isPartnerOnline = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Long> _partnerLastSeen = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Long> partnerLastSeen = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _partnerTyping = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> partnerTyping = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _securityFingerprint = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> securityFingerprint = null;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job messageCollectionJob;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job presenceCollectionJob;
    
    @javax.inject.Inject()
    public ChatRoomViewModel(@org.jetbrains.annotations.NotNull()
    com.quickchat.core.network.repository.ChatRepository chatRepository, @org.jetbrains.annotations.NotNull()
    com.quickchat.core.network.repository.UserRepository userRepository, @org.jetbrains.annotations.NotNull()
    com.quickchat.core.network.websocket.SocketManager socketManager, @org.jetbrains.annotations.NotNull()
    com.quickchat.core.network.api.QuickChatApi api) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getRecipientPhone() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getRecipientName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.quickchat.core.model.Message>> getMessages() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isPartnerOnline() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Long> getPartnerLastSeen() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getPartnerTyping() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getSecurityFingerprint() {
        return null;
    }
    
    public final void initRecipient(@org.jetbrains.annotations.NotNull()
    java.lang.String phone) {
    }
    
    public final void sendMessage(@org.jetbrains.annotations.NotNull()
    java.lang.String text) {
    }
    
    public final void sendTyping(boolean isTyping) {
    }
    
    private final void computeSecurityFingerprint(java.lang.String phone) {
    }
}