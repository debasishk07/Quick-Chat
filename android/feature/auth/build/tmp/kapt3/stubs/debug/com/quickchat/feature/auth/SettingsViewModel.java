package com.quickchat.feature.auth;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0007\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\"\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0014\u0010\u0016\u001a\u00020\u00172\f\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00170\u0019J\u0006\u0010\u001a\u001a\u00020\u0017J\u0006\u0010\u001b\u001a\u00020\u0010J\u0006\u0010\u001c\u001a\u00020\u0010J\u0006\u0010\u001d\u001a\u00020\tJ\u0006\u0010\u001e\u001a\u00020\u0010J\u0006\u0010\u001f\u001a\u00020\tJ\u0006\u0010 \u001a\u00020\tJ\u0006\u0010!\u001a\u00020\u0010J\u0006\u0010\"\u001a\u00020\tJ\u0006\u0010#\u001a\u00020\tJ\u0006\u0010$\u001a\u00020\u0010J\u0006\u0010%\u001a\u00020\tJ\u0006\u0010&\u001a\u00020\tJ\u0006\u0010\'\u001a\u00020\u0017J\u000e\u0010(\u001a\u00020\u00172\u0006\u0010)\u001a\u00020\u0010J\u000e\u0010*\u001a\u00020\u00172\u0006\u0010)\u001a\u00020\u0010J\u000e\u0010+\u001a\u00020\u00172\u0006\u0010)\u001a\u00020\fJ\u000e\u0010,\u001a\u00020\u00172\u0006\u0010)\u001a\u00020\u0010J\u000e\u0010-\u001a\u00020\u00172\u0006\u0010)\u001a\u00020\tJ\u000e\u0010.\u001a\u00020\u00172\u0006\u0010)\u001a\u00020\u0010J\u000e\u0010/\u001a\u00020\u00172\u0006\u0010)\u001a\u00020\tJ\u000e\u00100\u001a\u00020\u00172\u0006\u0010)\u001a\u00020\tJ\u000e\u00101\u001a\u00020\u00172\u0006\u0010)\u001a\u00020\u0010J\u000e\u00102\u001a\u00020\u00172\u0006\u0010)\u001a\u00020\tJ\u000e\u00103\u001a\u00020\u00172\u0006\u0010)\u001a\u00020\tJ\u000e\u00104\u001a\u00020\u00172\u0006\u0010)\u001a\u00020\u0010J\u000e\u00105\u001a\u00020\u00172\u0006\u00106\u001a\u00020\u0010J\u000e\u00107\u001a\u00020\u00172\u0006\u0010)\u001a\u00020\tJ*\u00108\u001a\u00020\u00172\u0006\u00109\u001a\u00020\u00102\u0006\u0010:\u001a\u00020\t2\u0012\u0010;\u001a\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\u00170<J\u000e\u0010=\u001a\u00020\u00172\u0006\u0010)\u001a\u00020\tR\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\n\u001a\b\u0012\u0004\u0012\u00020\f0\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0017\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00100\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u000eR\u0017\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\t0\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u000eR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00100\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u000eR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006>"}, d2 = {"Lcom/quickchat/feature/auth/SettingsViewModel;", "Landroidx/lifecycle/ViewModel;", "settingsManager", "Lcom/quickchat/core/network/settings/SettingsManager;", "userRepository", "Lcom/quickchat/core/network/repository/UserRepository;", "(Lcom/quickchat/core/network/settings/SettingsManager;Lcom/quickchat/core/network/repository/UserRepository;)V", "_deleteSuccess", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "chatFontSize", "Lkotlinx/coroutines/flow/StateFlow;", "", "getChatFontSize", "()Lkotlinx/coroutines/flow/StateFlow;", "chatWallpaper", "", "getChatWallpaper", "deleteSuccess", "getDeleteSuccess", "theme", "getTheme", "clearCache", "", "onDone", "Lkotlin/Function0;", "deleteAccount", "getAutoDownloadMobile", "getAutoDownloadWifi", "getEnterToSend", "getLastSeenVisibility", "getNotificationPreviewEnabled", "getNotificationsEnabled", "getProfilePhotoVisibility", "getReadReceiptsEnabled", "getSoundEnabled", "getStatusVisibility", "getTwoStepVerificationEnabled", "getVibrationEnabled", "resetDeleteState", "setAutoDownloadMobile", "value", "setAutoDownloadWifi", "setChatFontSize", "setChatWallpaper", "setEnterToSend", "setLastSeenVisibility", "setNotificationPreviewEnabled", "setNotificationsEnabled", "setProfilePhotoVisibility", "setReadReceiptsEnabled", "setSoundEnabled", "setStatusVisibility", "setTheme", "themeValue", "setTwoStepVerificationEnabled", "setUsernameSearchEnabled", "userId", "enabled", "onResult", "Lkotlin/Function1;", "setVibrationEnabled", "auth_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class SettingsViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.quickchat.core.network.settings.SettingsManager settingsManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.quickchat.core.network.repository.UserRepository userRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> theme = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> chatWallpaper = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Float> chatFontSize = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _deleteSuccess = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> deleteSuccess = null;
    
    @javax.inject.Inject()
    public SettingsViewModel(@org.jetbrains.annotations.NotNull()
    com.quickchat.core.network.settings.SettingsManager settingsManager, @org.jetbrains.annotations.NotNull()
    com.quickchat.core.network.repository.UserRepository userRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getTheme() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getChatWallpaper() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Float> getChatFontSize() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getDeleteSuccess() {
        return null;
    }
    
    public final void setTheme(@org.jetbrains.annotations.NotNull()
    java.lang.String themeValue) {
    }
    
    public final void setChatWallpaper(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
    
    public final void setChatFontSize(float value) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getLastSeenVisibility() {
        return null;
    }
    
    public final void setLastSeenVisibility(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
    
    public final boolean getReadReceiptsEnabled() {
        return false;
    }
    
    public final void setReadReceiptsEnabled(boolean value) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getProfilePhotoVisibility() {
        return null;
    }
    
    public final void setProfilePhotoVisibility(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getStatusVisibility() {
        return null;
    }
    
    public final void setStatusVisibility(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
    
    public final void setUsernameSearchEnabled(@org.jetbrains.annotations.NotNull()
    java.lang.String userId, boolean enabled, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onResult) {
    }
    
    public final boolean getNotificationsEnabled() {
        return false;
    }
    
    public final void setNotificationsEnabled(boolean value) {
    }
    
    public final boolean getSoundEnabled() {
        return false;
    }
    
    public final void setSoundEnabled(boolean value) {
    }
    
    public final boolean getVibrationEnabled() {
        return false;
    }
    
    public final void setVibrationEnabled(boolean value) {
    }
    
    public final boolean getNotificationPreviewEnabled() {
        return false;
    }
    
    public final void setNotificationPreviewEnabled(boolean value) {
    }
    
    public final boolean getEnterToSend() {
        return false;
    }
    
    public final void setEnterToSend(boolean value) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getAutoDownloadWifi() {
        return null;
    }
    
    public final void setAutoDownloadWifi(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getAutoDownloadMobile() {
        return null;
    }
    
    public final void setAutoDownloadMobile(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
    
    public final boolean getTwoStepVerificationEnabled() {
        return false;
    }
    
    public final void setTwoStepVerificationEnabled(boolean value) {
    }
    
    public final void clearCache(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onDone) {
    }
    
    public final void deleteAccount() {
    }
    
    public final void resetDeleteState() {
    }
}