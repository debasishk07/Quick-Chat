package com.quickchat.core.database.dao;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0005\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\u0007\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0014\u0010\b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\n0\tH\'J\u0018\u0010\f\u001a\u0004\u0018\u00010\u000b2\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\r\u001a\u00020\u00032\u0006\u0010\u000e\u001a\u00020\u000bH\u00a7@\u00a2\u0006\u0002\u0010\u000f\u00a8\u0006\u0010"}, d2 = {"Lcom/quickchat/core/database/dao/ChatDao;", "", "clearUnreadCount", "", "recipientPhone", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteChat", "getAllChatsFlow", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/quickchat/core/database/entities/ChatEntity;", "getChat", "insertOrUpdateChat", "chat", "(Lcom/quickchat/core/database/entities/ChatEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "database_debug"})
@androidx.room.Dao()
public abstract interface ChatDao {
    
    @androidx.room.Query(value = "SELECT * FROM chats ORDER BY isPinned DESC, lastMessageId DESC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.quickchat.core.database.entities.ChatEntity>> getAllChatsFlow();
    
    @androidx.room.Query(value = "SELECT * FROM chats WHERE recipientPhone = :recipientPhone")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getChat(@org.jetbrains.annotations.NotNull()
    java.lang.String recipientPhone, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.quickchat.core.database.entities.ChatEntity> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertOrUpdateChat(@org.jetbrains.annotations.NotNull()
    com.quickchat.core.database.entities.ChatEntity chat, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "UPDATE chats SET unreadCount = 0 WHERE recipientPhone = :recipientPhone")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object clearUnreadCount(@org.jetbrains.annotations.NotNull()
    java.lang.String recipientPhone, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM chats WHERE recipientPhone = :recipientPhone")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteChat(@org.jetbrains.annotations.NotNull()
    java.lang.String recipientPhone, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}