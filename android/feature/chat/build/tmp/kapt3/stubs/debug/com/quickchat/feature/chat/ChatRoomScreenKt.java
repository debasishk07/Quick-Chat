package com.quickchat.feature.chat;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000D\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0004\u001a\"\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u0007\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u0006\u0010\u0007\u001aQ\u0010\b\u001a\u00020\u00012\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\u00032\u0006\u0010\f\u001a\u00020\r2\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00010\u000f2!\u0010\u0010\u001a\u001d\u0012\u0013\u0012\u00110\u0003\u00a2\u0006\f\b\u0012\u0012\b\b\u0013\u0012\u0004\b\b(\u0014\u0012\u0004\u0012\u00020\u00010\u0011H\u0007\u001a \u0010\u0015\u001a\u00020\u00012\u0006\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\rH\u0007\u001a\u0010\u0010\u001b\u001a\u00020\u00012\u0006\u0010\u001c\u001a\u00020\u0003H\u0007\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006\u001d"}, d2 = {"ChatRoomBackground", "", "wallpaperStyle", "", "strokeColor", "Landroidx/compose/ui/graphics/Color;", "ChatRoomBackground-4WTKRHQ", "(Ljava/lang/String;J)V", "ChatRoomScreen", "viewModel", "Lcom/quickchat/feature/chat/ChatRoomViewModel;", "chatWallpaper", "chatFontSize", "", "onNavigateBack", "Lkotlin/Function0;", "onNavigateToVerify", "Lkotlin/Function1;", "Lkotlin/ParameterName;", "name", "phone", "MessageBubble", "message", "Lcom/quickchat/core/model/Message;", "isMe", "", "fontSize", "TypingBubble", "partnerName", "chat_debug"})
public final class ChatRoomScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void ChatRoomScreen(@org.jetbrains.annotations.NotNull()
    com.quickchat.feature.chat.ChatRoomViewModel viewModel, @org.jetbrains.annotations.NotNull()
    java.lang.String chatWallpaper, float chatFontSize, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateBack, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onNavigateToVerify) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void MessageBubble(@org.jetbrains.annotations.NotNull()
    com.quickchat.core.model.Message message, boolean isMe, float fontSize) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void TypingBubble(@org.jetbrains.annotations.NotNull()
    java.lang.String partnerName) {
    }
}