package com.quickchat.feature.chat;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000B\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\u001a]\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032!\u0010\u0004\u001a\u001d\u0012\u0013\u0012\u00110\u0006\u00a2\u0006\f\b\u0007\u0012\b\b\b\u0012\u0004\b\b(\t\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00010\u000b2\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00010\u000b2\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00010\u000bH\u0007\u001a.\u0010\u000e\u001a\u00020\u00012\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00062\u0006\u0010\u0012\u001a\u00020\u00132\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00010\u000bH\u0007\u001a~\u0010\u0015\u001a\u00020\u00012\f\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00010\u000b26\u0010\u0017\u001a2\u0012\u0013\u0012\u00110\u0006\u00a2\u0006\f\b\u0007\u0012\b\b\b\u0012\u0004\b\b(\t\u0012\u0013\u0012\u00110\u0006\u00a2\u0006\f\b\u0007\u0012\b\b\b\u0012\u0004\b\b(\b\u0012\u0004\u0012\u00020\u00010\u00182.\u0010\u0019\u001a*\u0012\u0004\u0012\u00020\u0006\u0012\u001a\u0012\u0018\u0012\u0006\u0012\u0004\u0018\u00010\u001a\u0012\u0006\u0012\u0004\u0018\u00010\u0006\u0012\u0004\u0012\u00020\u00010\u0018\u0012\u0004\u0012\u00020\u00010\u0018H\u0007\u00a8\u0006\u001b"}, d2 = {"ChatListScreen", "", "viewModel", "Lcom/quickchat/feature/chat/ChatListViewModel;", "onNavigateToChat", "Lkotlin/Function1;", "", "Lkotlin/ParameterName;", "name", "phone", "onNavigateToStatus", "Lkotlin/Function0;", "onNavigateToProfile", "onNavigateToSettings", "ChatRow", "chat", "Lcom/quickchat/core/model/Chat;", "lastMessageText", "isTyping", "", "onClick", "StartChatDialog", "onDismiss", "onConfirm", "Lkotlin/Function2;", "onSearchUsername", "Lcom/quickchat/core/model/User;", "chat_debug"})
public final class ChatListScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void ChatListScreen(@org.jetbrains.annotations.NotNull()
    com.quickchat.feature.chat.ChatListViewModel viewModel, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onNavigateToChat, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToStatus, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToProfile, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToSettings) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void ChatRow(@org.jetbrains.annotations.NotNull()
    com.quickchat.core.model.Chat chat, @org.jetbrains.annotations.NotNull()
    java.lang.String lastMessageText, boolean isTyping, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void StartChatDialog(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.String, ? super java.lang.String, kotlin.Unit> onConfirm, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.String, ? super kotlin.jvm.functions.Function2<? super com.quickchat.core.model.User, ? super java.lang.String, kotlin.Unit>, kotlin.Unit> onSearchUsername) {
    }
}