package com.quickchat.feature.status;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u00004\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0002\b\u0004\u001a\u0018\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u0007\u001a]\u0010\u0006\u001a\u00020\u00012\u0006\u0010\u0007\u001a\u00020\b2\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00010\n2!\u0010\f\u001a\u001d\u0012\u0013\u0012\u00110\u000e\u00a2\u0006\f\b\u000f\u0012\b\b\u0010\u0012\u0004\b\b(\u0011\u0012\u0004\u0012\u00020\u00010\r2\f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00010\nH\u0007\u00a8\u0006\u0013"}, d2 = {"StatusDashedBorderAvatar", "", "user", "Lcom/quickchat/core/model/User;", "segmentsCount", "", "StatusListScreen", "viewModel", "Lcom/quickchat/feature/status/StatusViewModel;", "onNavigateToCreateTextStatus", "Lkotlin/Function0;", "onNavigateToCreateMediaStatus", "onNavigateToViewStatus", "Lkotlin/Function1;", "", "Lkotlin/ParameterName;", "name", "phone", "onNavigateBack", "status_debug"})
public final class StatusListScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void StatusListScreen(@org.jetbrains.annotations.NotNull()
    com.quickchat.feature.status.StatusViewModel viewModel, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToCreateTextStatus, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToCreateMediaStatus, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onNavigateToViewStatus, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateBack) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void StatusDashedBorderAvatar(@org.jetbrains.annotations.NotNull()
    com.quickchat.core.model.User user, int segmentsCount) {
    }
}