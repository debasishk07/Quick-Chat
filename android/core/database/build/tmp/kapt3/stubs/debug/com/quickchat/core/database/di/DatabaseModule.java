package com.quickchat.core.database.di;

@dagger.Module()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000N\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0012\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u001a\u0010\u0006\u001a\u00020\u00072\b\b\u0001\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000bH\u0007J\u0010\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u0007H\u0007J\u0010\u0010\u000f\u001a\u00020\u000b2\u0006\u0010\u0010\u001a\u00020\u0011H\u0007J\u0010\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u000e\u001a\u00020\u0007H\u0007J\u0010\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u000e\u001a\u00020\u0007H\u0007J\u0012\u0010\u0016\u001a\u00020\u00112\b\b\u0001\u0010\b\u001a\u00020\tH\u0007J\u0010\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u000e\u001a\u00020\u0007H\u0007J\u0010\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u000e\u001a\u00020\u0007H\u0007R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001b"}, d2 = {"Lcom/quickchat/core/database/di/DatabaseModule;", "", "()V", "KEY_SEALED_PASSPHRASE", "", "PREFS_NAME", "provideAppDatabase", "Lcom/quickchat/core/database/AppDatabase;", "context", "Landroid/content/Context;", "passphraseBytes", "", "provideChatDao", "Lcom/quickchat/core/database/dao/ChatDao;", "db", "provideDatabasePassphrase", "sharedPrefs", "Landroid/content/SharedPreferences;", "provideMessageDao", "Lcom/quickchat/core/database/dao/MessageDao;", "provideOutboxDao", "Lcom/quickchat/core/database/dao/OutboxDao;", "provideSecureSharedPreferences", "provideSessionDao", "Lcom/quickchat/core/database/dao/SessionDao;", "provideStatusDao", "Lcom/quickchat/core/database/dao/StatusDao;", "database_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public final class DatabaseModule {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String PREFS_NAME = "quickchat_secure_prefs";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_SEALED_PASSPHRASE = "sealed_db_passphrase";
    @org.jetbrains.annotations.NotNull()
    public static final com.quickchat.core.database.di.DatabaseModule INSTANCE = null;
    
    private DatabaseModule() {
        super();
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final android.content.SharedPreferences provideSecureSharedPreferences(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final byte[] provideDatabasePassphrase(@org.jetbrains.annotations.NotNull()
    android.content.SharedPreferences sharedPrefs) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.quickchat.core.database.AppDatabase provideAppDatabase(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    byte[] passphraseBytes) {
        return null;
    }
    
    @dagger.Provides()
    @org.jetbrains.annotations.NotNull()
    public final com.quickchat.core.database.dao.ChatDao provideChatDao(@org.jetbrains.annotations.NotNull()
    com.quickchat.core.database.AppDatabase db) {
        return null;
    }
    
    @dagger.Provides()
    @org.jetbrains.annotations.NotNull()
    public final com.quickchat.core.database.dao.MessageDao provideMessageDao(@org.jetbrains.annotations.NotNull()
    com.quickchat.core.database.AppDatabase db) {
        return null;
    }
    
    @dagger.Provides()
    @org.jetbrains.annotations.NotNull()
    public final com.quickchat.core.database.dao.StatusDao provideStatusDao(@org.jetbrains.annotations.NotNull()
    com.quickchat.core.database.AppDatabase db) {
        return null;
    }
    
    @dagger.Provides()
    @org.jetbrains.annotations.NotNull()
    public final com.quickchat.core.database.dao.SessionDao provideSessionDao(@org.jetbrains.annotations.NotNull()
    com.quickchat.core.database.AppDatabase db) {
        return null;
    }
    
    @dagger.Provides()
    @org.jetbrains.annotations.NotNull()
    public final com.quickchat.core.database.dao.OutboxDao provideOutboxDao(@org.jetbrains.annotations.NotNull()
    com.quickchat.core.database.AppDatabase db) {
        return null;
    }
}