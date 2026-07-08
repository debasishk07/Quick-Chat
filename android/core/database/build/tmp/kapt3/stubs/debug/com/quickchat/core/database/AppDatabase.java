package com.quickchat.core.database;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\'\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H&J\b\u0010\u0005\u001a\u00020\u0006H&J\b\u0010\u0007\u001a\u00020\bH&J\b\u0010\t\u001a\u00020\nH&J\b\u0010\u000b\u001a\u00020\fH&\u00a8\u0006\r"}, d2 = {"Lcom/quickchat/core/database/AppDatabase;", "Landroidx/room/RoomDatabase;", "()V", "chatDao", "Lcom/quickchat/core/database/dao/ChatDao;", "messageDao", "Lcom/quickchat/core/database/dao/MessageDao;", "outboxDao", "Lcom/quickchat/core/database/dao/OutboxDao;", "sessionDao", "Lcom/quickchat/core/database/dao/SessionDao;", "statusDao", "Lcom/quickchat/core/database/dao/StatusDao;", "database_debug"})
@androidx.room.Database(entities = {com.quickchat.core.database.entities.MessageEntity.class, com.quickchat.core.database.entities.ChatEntity.class, com.quickchat.core.database.entities.StatusEntity.class, com.quickchat.core.database.entities.SessionEntity.class, com.quickchat.core.database.entities.OutboxEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends androidx.room.RoomDatabase {
    
    public AppDatabase() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.quickchat.core.database.dao.ChatDao chatDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.quickchat.core.database.dao.MessageDao messageDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.quickchat.core.database.dao.StatusDao statusDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.quickchat.core.database.dao.SessionDao sessionDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.quickchat.core.database.dao.OutboxDao outboxDao();
}