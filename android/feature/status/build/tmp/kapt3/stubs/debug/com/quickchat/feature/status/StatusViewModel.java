package com.quickchat.feature.status;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000V\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u0012\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J \u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u001b2\b\u0010\u001c\u001a\u0004\u0018\u00010\u001d2\u0006\u0010\u001e\u001a\u00020\u001fJ\u0016\u0010 \u001a\u00020\u00192\u0006\u0010!\u001a\u00020\u001d2\u0006\u0010\"\u001a\u00020\u001dJ\u0006\u0010#\u001a\u00020\u0019J\u0006\u0010$\u001a\u00020\u0019R\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\n\u001a\b\u0012\u0004\u0012\u00020\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\u000b\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\r0\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u0017\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\t0\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u000fR\u001d\u0010\u0012\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00140\u00130\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u000fR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\t0\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u000fR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006%"}, d2 = {"Lcom/quickchat/feature/status/StatusViewModel;", "Landroidx/lifecycle/ViewModel;", "statusRepository", "Lcom/quickchat/core/network/repository/StatusRepository;", "userRepository", "Lcom/quickchat/core/network/repository/UserRepository;", "(Lcom/quickchat/core/network/repository/StatusRepository;Lcom/quickchat/core/network/repository/UserRepository;)V", "_loading", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "_uploadSuccess", "currentUser", "Lkotlinx/coroutines/flow/StateFlow;", "Lcom/quickchat/core/model/User;", "getCurrentUser", "()Lkotlinx/coroutines/flow/StateFlow;", "loading", "getLoading", "statusFeeds", "", "Lcom/quickchat/core/model/UserStatus;", "getStatusFeeds", "uploadSuccess", "getUploadSuccess", "postMediaStatus", "", "mediaBytes", "", "caption", "", "type", "Lcom/quickchat/core/model/StatusMediaType;", "postTextStatus", "text", "backgroundColor", "purgeExpired", "resetUploadState", "status_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class StatusViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.quickchat.core.network.repository.StatusRepository statusRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.quickchat.core.network.repository.UserRepository userRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.quickchat.core.model.User> currentUser = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.quickchat.core.model.UserStatus>> statusFeeds = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _loading = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> loading = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _uploadSuccess = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> uploadSuccess = null;
    
    @javax.inject.Inject()
    public StatusViewModel(@org.jetbrains.annotations.NotNull()
    com.quickchat.core.network.repository.StatusRepository statusRepository, @org.jetbrains.annotations.NotNull()
    com.quickchat.core.network.repository.UserRepository userRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.quickchat.core.model.User> getCurrentUser() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.quickchat.core.model.UserStatus>> getStatusFeeds() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getLoading() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getUploadSuccess() {
        return null;
    }
    
    public final void purgeExpired() {
    }
    
    public final void postTextStatus(@org.jetbrains.annotations.NotNull()
    java.lang.String text, @org.jetbrains.annotations.NotNull()
    java.lang.String backgroundColor) {
    }
    
    public final void postMediaStatus(@org.jetbrains.annotations.NotNull()
    byte[] mediaBytes, @org.jetbrains.annotations.Nullable()
    java.lang.String caption, @org.jetbrains.annotations.NotNull()
    com.quickchat.core.model.StatusMediaType type) {
    }
    
    public final void resetUploadState() {
    }
}