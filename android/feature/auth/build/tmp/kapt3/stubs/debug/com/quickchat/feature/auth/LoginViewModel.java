package com.quickchat.feature.auth;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000h\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b \n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0012\n\u0002\u0010\u0012\n\u0002\b\u0006\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0014\u0010?\u001a\u00020@2\f\u0010A\u001a\b\u0012\u0004\u0012\u00020@0BJ\u0006\u0010C\u001a\u00020@J\u0010\u0010D\u001a\u00020@2\u0006\u0010E\u001a\u00020FH\u0002J\u0006\u0010G\u001a\u00020@J\u0006\u0010H\u001a\u00020@J\u0006\u0010I\u001a\u00020@J\u0006\u0010J\u001a\u00020@J\u000e\u0010K\u001a\u00020@2\u0006\u0010L\u001a\u00020\u0011J1\u0010M\u001a\u00020@2\u0006\u0010E\u001a\u00020F2!\u0010N\u001a\u001d\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\bP\u0012\b\bQ\u0012\u0004\b\b(R\u0012\u0004\u0012\u00020@0OJC\u0010S\u001a\u00020@2\u0006\u0010T\u001a\u00020\u00072\u0006\u0010Q\u001a\u00020\u00072\b\u0010U\u001a\u0004\u0018\u00010\u00072!\u0010N\u001a\u001d\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\bP\u0012\b\bQ\u0012\u0004\b\b(R\u0012\u0004\u0012\u00020@0OJ)\u0010V\u001a\u00020@2!\u0010N\u001a\u001d\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\bP\u0012\b\bQ\u0012\u0004\b\b(R\u0012\u0004\u0012\u00020@0OJ\u000e\u0010W\u001a\u00020@2\u0006\u0010X\u001a\u00020\u0007J\u000e\u0010Y\u001a\u00020@2\u0006\u0010Q\u001a\u00020\u0007J\u000e\u0010Z\u001a\u00020@2\u0006\u0010[\u001a\u00020\u0007J\u000e\u0010\\\u001a\u00020@2\u0006\u0010]\u001a\u00020\u0007J\u000e\u0010^\u001a\u00020@2\u0006\u0010_\u001a\u00020\u0007J\u000e\u0010`\u001a\u00020@2\u0006\u0010[\u001a\u00020\u0007J\"\u0010a\u001a\u00020@2\u0006\u0010b\u001a\u00020c2\u0012\u0010N\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020@0OJ*\u0010d\u001a\u00020@2\u0006\u0010!\u001a\u00020\u00072\u0006\u0010\u001a\u001a\u00020\u00072\u0012\u0010N\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020@0OJ\u000e\u0010e\u001a\u00020@2\u0006\u0010Q\u001a\u00020\u0007J\u0010\u0010f\u001a\u00020@2\u0006\u0010Q\u001a\u00020\u0007H\u0002J)\u0010g\u001a\u00020@2!\u0010N\u001a\u001d\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\bP\u0012\b\bQ\u0012\u0004\b\b(R\u0012\u0004\u0012\u00020@0OJ)\u0010h\u001a\u00020@2!\u0010A\u001a\u001d\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\bP\u0012\b\bQ\u0012\u0004\b\b(R\u0012\u0004\u0012\u00020@0OR\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\t\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00110\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0019\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00070\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001dR\u0019\u0010\u001e\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u001f0\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\u001dR\u0017\u0010!\u001a\b\u0012\u0004\u0012\u00020\u00070\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010\u001dR\u0019\u0010#\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00070\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b$\u0010\u001dR\u0017\u0010%\u001a\b\u0012\u0004\u0012\u00020\u000b0\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b&\u0010\u001dR\u0017\u0010\'\u001a\b\u0012\u0004\u0012\u00020\u00070\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b(\u0010\u001dR\u0017\u0010)\u001a\b\u0012\u0004\u0012\u00020\u00070\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b*\u0010\u001dR\u0017\u0010+\u001a\b\u0012\u0004\u0012\u00020\u000b0\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b,\u0010\u001dR\u0017\u0010-\u001a\b\u0012\u0004\u0012\u00020\u000b0\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b.\u0010\u001dR\u0017\u0010/\u001a\b\u0012\u0004\u0012\u00020\u00110\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b0\u0010\u001dR\u0017\u00101\u001a\b\u0012\u0004\u0012\u00020\u00070\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b2\u0010\u001dR\u0017\u00103\u001a\b\u0012\u0004\u0012\u00020\u000b0\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b4\u0010\u001dR\u0017\u00105\u001a\b\u0012\u0004\u0012\u00020\u00070\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b6\u0010\u001dR\u0017\u00107\u001a\b\u0012\u0004\u0012\u00020\u000b0\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b8\u0010\u001dR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b9\u0010:R\u0017\u0010;\u001a\b\u0012\u0004\u0012\u00020\u00070\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b<\u0010\u001dR\u0019\u0010=\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00070\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b>\u0010\u001d\u00a8\u0006i"}, d2 = {"Lcom/quickchat/feature/auth/LoginViewModel;", "Landroidx/lifecycle/ViewModel;", "userRepository", "Lcom/quickchat/core/network/repository/UserRepository;", "(Lcom/quickchat/core/network/repository/UserRepository;)V", "_about", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "_displayName", "_error", "_googleLinkPhoneRequired", "", "_linkPhone", "_linkPhoneCode", "_linkPhoneCodeSent", "_loading", "_loginFlowState", "Lcom/quickchat/feature/auth/LoginFlowState;", "_otp", "_otpSent", "_phone", "_showMockGoogleChooser", "_tempEmail", "_tempGoogleUid", "_username", "_usernameError", "about", "Lkotlinx/coroutines/flow/StateFlow;", "getAbout", "()Lkotlinx/coroutines/flow/StateFlow;", "currentUser", "Lcom/quickchat/core/model/User;", "getCurrentUser", "displayName", "getDisplayName", "error", "getError", "googleLinkPhoneRequired", "getGoogleLinkPhoneRequired", "linkPhone", "getLinkPhone", "linkPhoneCode", "getLinkPhoneCode", "linkPhoneCodeSent", "getLinkPhoneCodeSent", "loading", "getLoading", "loginFlowState", "getLoginFlowState", "otp", "getOtp", "otpSent", "getOtpSent", "phone", "getPhone", "showMockGoogleChooser", "getShowMockGoogleChooser", "getUserRepository", "()Lcom/quickchat/core/network/repository/UserRepository;", "username", "getUsername", "usernameError", "getUsernameError", "completeProfile", "", "onSuccess", "Lkotlin/Function0;", "dismissMockGoogleChooser", "initializeFirebaseApp", "context", "Landroid/content/Context;", "logout", "resetStates", "sendLinkPhoneOtp", "sendOtp", "setLoginFlowState", "state", "signInWithGoogle", "onComplete", "Lkotlin/Function1;", "Lkotlin/ParameterName;", "name", "isNewUser", "signInWithGoogleMock", "email", "avatar", "skipLinkPhone", "updateAbout", "text", "updateDisplayName", "updateLinkPhone", "newPhone", "updateLinkPhoneCode", "newCode", "updateOtp", "newOtp", "updatePhone", "updateProfileAvatar", "mediaBytes", "", "updateProfileDetails", "updateUsername", "validateUsernameLocal", "verifyLinkPhoneOtp", "verifyOtp", "auth_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class LoginViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.quickchat.core.network.repository.UserRepository userRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.quickchat.feature.auth.LoginFlowState> _loginFlowState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.quickchat.feature.auth.LoginFlowState> loginFlowState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _phone = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> phone = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _otp = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> otp = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _otpSent = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> otpSent = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _loading = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> loading = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _error = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> error = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _linkPhone = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> linkPhone = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _linkPhoneCode = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> linkPhoneCode = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _linkPhoneCodeSent = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> linkPhoneCodeSent = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _googleLinkPhoneRequired = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> googleLinkPhoneRequired = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _showMockGoogleChooser = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> showMockGoogleChooser = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _tempGoogleUid = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _tempEmail = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _displayName = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> displayName = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _about = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> about = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _username = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> username = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _usernameError = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> usernameError = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.quickchat.core.model.User> currentUser = null;
    
    @javax.inject.Inject()
    public LoginViewModel(@org.jetbrains.annotations.NotNull()
    com.quickchat.core.network.repository.UserRepository userRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.quickchat.core.network.repository.UserRepository getUserRepository() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.quickchat.feature.auth.LoginFlowState> getLoginFlowState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getPhone() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getOtp() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getOtpSent() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getLoading() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getError() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getLinkPhone() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getLinkPhoneCode() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getLinkPhoneCodeSent() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getGoogleLinkPhoneRequired() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getShowMockGoogleChooser() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getDisplayName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getAbout() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getUsername() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getUsernameError() {
        return null;
    }
    
    public final void setLoginFlowState(@org.jetbrains.annotations.NotNull()
    com.quickchat.feature.auth.LoginFlowState state) {
    }
    
    public final void updatePhone(@org.jetbrains.annotations.NotNull()
    java.lang.String newPhone) {
    }
    
    public final void updateOtp(@org.jetbrains.annotations.NotNull()
    java.lang.String newOtp) {
    }
    
    public final void updateLinkPhone(@org.jetbrains.annotations.NotNull()
    java.lang.String newPhone) {
    }
    
    public final void updateLinkPhoneCode(@org.jetbrains.annotations.NotNull()
    java.lang.String newCode) {
    }
    
    public final void updateDisplayName(@org.jetbrains.annotations.NotNull()
    java.lang.String name) {
    }
    
    public final void updateAbout(@org.jetbrains.annotations.NotNull()
    java.lang.String text) {
    }
    
    public final void updateUsername(@org.jetbrains.annotations.NotNull()
    java.lang.String name) {
    }
    
    private final void validateUsernameLocal(java.lang.String name) {
    }
    
    public final void sendOtp() {
    }
    
    public final void verifyOtp(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onSuccess) {
    }
    
    private final void initializeFirebaseApp(android.content.Context context) {
    }
    
    public final void signInWithGoogle(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onComplete) {
    }
    
    public final void signInWithGoogleMock(@org.jetbrains.annotations.NotNull()
    java.lang.String email, @org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.Nullable()
    java.lang.String avatar, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onComplete) {
    }
    
    public final void dismissMockGoogleChooser() {
    }
    
    public final void sendLinkPhoneOtp() {
    }
    
    public final void verifyLinkPhoneOtp(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onComplete) {
    }
    
    public final void skipLinkPhone(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onComplete) {
    }
    
    public final void completeProfile(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onSuccess) {
    }
    
    public final void updateProfileAvatar(@org.jetbrains.annotations.NotNull()
    byte[] mediaBytes, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onComplete) {
    }
    
    public final void updateProfileDetails(@org.jetbrains.annotations.NotNull()
    java.lang.String displayName, @org.jetbrains.annotations.NotNull()
    java.lang.String about, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onComplete) {
    }
    
    public final void resetStates() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.quickchat.core.model.User> getCurrentUser() {
        return null;
    }
    
    public final void logout() {
    }
}