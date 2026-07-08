package com.quickchat.feature.chat;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000P\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010$\n\u0002\u0010\u000e\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0006\u0010\u0015\u001a\u00020\u0016J\b\u0010\u0017\u001a\u00020\u0016H\u0014J,\u0010\u0018\u001a\u00020\u00162\u0006\u0010\u0019\u001a\u00020\u00122\u001c\u0010\u001a\u001a\u0018\u0012\u0006\u0012\u0004\u0018\u00010\u000e\u0012\u0006\u0012\u0004\u0018\u00010\u0012\u0012\u0004\u0012\u00020\u00160\u001bJ\u0016\u0010\u001c\u001a\u00020\u00162\u0006\u0010\u001d\u001a\u00020\u00122\u0006\u0010\u001e\u001a\u00020\u0012R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0007\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0019\u0010\r\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000e0\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\fR#\u0010\u0010\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0012\u0012\u0004\u0012\u00020\u00130\u00110\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\fR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001f"}, d2 = {"Lcom/quickchat/feature/chat/ChatListViewModel;", "Landroidx/lifecycle/ViewModel;", "chatRepository", "Lcom/quickchat/core/network/repository/ChatRepository;", "userRepository", "Lcom/quickchat/core/network/repository/UserRepository;", "(Lcom/quickchat/core/network/repository/ChatRepository;Lcom/quickchat/core/network/repository/UserRepository;)V", "chats", "Lkotlinx/coroutines/flow/StateFlow;", "", "Lcom/quickchat/core/model/Chat;", "getChats", "()Lkotlinx/coroutines/flow/StateFlow;", "currentUser", "Lcom/quickchat/core/model/User;", "getCurrentUser", "typingStates", "", "", "", "getTypingStates", "logout", "", "onCleared", "searchUserByUsername", "username", "onResult", "Lkotlin/Function2;", "startChatWithContact", "phone", "name", "chat_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class ChatListViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.quickchat.core.network.repository.ChatRepository chatRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.quickchat.core.network.repository.UserRepository userRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.quickchat.core.model.User> currentUser = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.quickchat.core.model.Chat>> chats = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.Map<java.lang.String, java.lang.Boolean>> typingStates = null;
    
    @javax.inject.Inject()
    public ChatListViewModel(@org.jetbrains.annotations.NotNull()
    com.quickchat.core.network.repository.ChatRepository chatRepository, @org.jetbrains.annotations.NotNull()
    com.quickchat.core.network.repository.UserRepository userRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.quickchat.core.model.User> getCurrentUser() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.quickchat.core.model.Chat>> getChats() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.Map<java.lang.String, java.lang.Boolean>> getTypingStates() {
        return null;
    }
    
    public final void startChatWithContact(@org.jetbrains.annotations.NotNull()
    java.lang.String phone, @org.jetbrains.annotations.NotNull()
    java.lang.String name) {
    }
    
    public final void searchUserByUsername(@org.jetbrains.annotations.NotNull()
    java.lang.String username, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super com.quickchat.core.model.User, ? super java.lang.String, kotlin.Unit> onResult) {
    }
    
    public final void logout() {
    }
    
    @java.lang.Override()
    protected void onCleared() {
    }
}