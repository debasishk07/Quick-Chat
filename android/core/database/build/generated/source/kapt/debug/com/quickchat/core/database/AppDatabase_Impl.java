package com.quickchat.core.database;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.quickchat.core.database.dao.ChatDao;
import com.quickchat.core.database.dao.ChatDao_Impl;
import com.quickchat.core.database.dao.MessageDao;
import com.quickchat.core.database.dao.MessageDao_Impl;
import com.quickchat.core.database.dao.OutboxDao;
import com.quickchat.core.database.dao.OutboxDao_Impl;
import com.quickchat.core.database.dao.SessionDao;
import com.quickchat.core.database.dao.SessionDao_Impl;
import com.quickchat.core.database.dao.StatusDao;
import com.quickchat.core.database.dao.StatusDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile ChatDao _chatDao;

  private volatile MessageDao _messageDao;

  private volatile StatusDao _statusDao;

  private volatile SessionDao _sessionDao;

  private volatile OutboxDao _outboxDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `messages` (`id` TEXT NOT NULL, `senderPhone` TEXT NOT NULL, `recipientPhone` TEXT NOT NULL, `isGroup` INTEGER NOT NULL, `ciphertext` TEXT NOT NULL, `iv` TEXT NOT NULL, `ephemeralPublicKey` TEXT, `messageType` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `status` TEXT NOT NULL, `plainText` TEXT, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `chats` (`recipientPhone` TEXT NOT NULL, `displayName` TEXT NOT NULL, `avatarUrl` TEXT, `isGroup` INTEGER NOT NULL, `lastMessageId` TEXT, `unreadCount` INTEGER NOT NULL, `isPinned` INTEGER NOT NULL, `isMuted` INTEGER NOT NULL, `isArchived` INTEGER NOT NULL, PRIMARY KEY(`recipientPhone`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `statuses` (`id` TEXT NOT NULL, `senderPhone` TEXT NOT NULL, `mediaUrl` TEXT NOT NULL, `caption` TEXT, `mediaType` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `expiresAt` INTEGER NOT NULL, `viewersJson` TEXT NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `crypto_sessions` (`recipientPhone` TEXT NOT NULL, `sessionJson` TEXT NOT NULL, PRIMARY KEY(`recipientPhone`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `outbox_queue` (`id` TEXT NOT NULL, `recipientPhone` TEXT NOT NULL, `isGroup` INTEGER NOT NULL, `messageType` TEXT NOT NULL, `plainText` TEXT, `mediaPath` TEXT, `timestamp` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'b3012d56e1545d779f6d589c196f460d')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `messages`");
        db.execSQL("DROP TABLE IF EXISTS `chats`");
        db.execSQL("DROP TABLE IF EXISTS `statuses`");
        db.execSQL("DROP TABLE IF EXISTS `crypto_sessions`");
        db.execSQL("DROP TABLE IF EXISTS `outbox_queue`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsMessages = new HashMap<String, TableInfo.Column>(11);
        _columnsMessages.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("senderPhone", new TableInfo.Column("senderPhone", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("recipientPhone", new TableInfo.Column("recipientPhone", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("isGroup", new TableInfo.Column("isGroup", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("ciphertext", new TableInfo.Column("ciphertext", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("iv", new TableInfo.Column("iv", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("ephemeralPublicKey", new TableInfo.Column("ephemeralPublicKey", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("messageType", new TableInfo.Column("messageType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("plainText", new TableInfo.Column("plainText", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMessages = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesMessages = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoMessages = new TableInfo("messages", _columnsMessages, _foreignKeysMessages, _indicesMessages);
        final TableInfo _existingMessages = TableInfo.read(db, "messages");
        if (!_infoMessages.equals(_existingMessages)) {
          return new RoomOpenHelper.ValidationResult(false, "messages(com.quickchat.core.database.entities.MessageEntity).\n"
                  + " Expected:\n" + _infoMessages + "\n"
                  + " Found:\n" + _existingMessages);
        }
        final HashMap<String, TableInfo.Column> _columnsChats = new HashMap<String, TableInfo.Column>(9);
        _columnsChats.put("recipientPhone", new TableInfo.Column("recipientPhone", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChats.put("displayName", new TableInfo.Column("displayName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChats.put("avatarUrl", new TableInfo.Column("avatarUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChats.put("isGroup", new TableInfo.Column("isGroup", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChats.put("lastMessageId", new TableInfo.Column("lastMessageId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChats.put("unreadCount", new TableInfo.Column("unreadCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChats.put("isPinned", new TableInfo.Column("isPinned", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChats.put("isMuted", new TableInfo.Column("isMuted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChats.put("isArchived", new TableInfo.Column("isArchived", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysChats = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesChats = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoChats = new TableInfo("chats", _columnsChats, _foreignKeysChats, _indicesChats);
        final TableInfo _existingChats = TableInfo.read(db, "chats");
        if (!_infoChats.equals(_existingChats)) {
          return new RoomOpenHelper.ValidationResult(false, "chats(com.quickchat.core.database.entities.ChatEntity).\n"
                  + " Expected:\n" + _infoChats + "\n"
                  + " Found:\n" + _existingChats);
        }
        final HashMap<String, TableInfo.Column> _columnsStatuses = new HashMap<String, TableInfo.Column>(8);
        _columnsStatuses.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStatuses.put("senderPhone", new TableInfo.Column("senderPhone", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStatuses.put("mediaUrl", new TableInfo.Column("mediaUrl", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStatuses.put("caption", new TableInfo.Column("caption", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStatuses.put("mediaType", new TableInfo.Column("mediaType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStatuses.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStatuses.put("expiresAt", new TableInfo.Column("expiresAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStatuses.put("viewersJson", new TableInfo.Column("viewersJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysStatuses = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesStatuses = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoStatuses = new TableInfo("statuses", _columnsStatuses, _foreignKeysStatuses, _indicesStatuses);
        final TableInfo _existingStatuses = TableInfo.read(db, "statuses");
        if (!_infoStatuses.equals(_existingStatuses)) {
          return new RoomOpenHelper.ValidationResult(false, "statuses(com.quickchat.core.database.entities.StatusEntity).\n"
                  + " Expected:\n" + _infoStatuses + "\n"
                  + " Found:\n" + _existingStatuses);
        }
        final HashMap<String, TableInfo.Column> _columnsCryptoSessions = new HashMap<String, TableInfo.Column>(2);
        _columnsCryptoSessions.put("recipientPhone", new TableInfo.Column("recipientPhone", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCryptoSessions.put("sessionJson", new TableInfo.Column("sessionJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCryptoSessions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCryptoSessions = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCryptoSessions = new TableInfo("crypto_sessions", _columnsCryptoSessions, _foreignKeysCryptoSessions, _indicesCryptoSessions);
        final TableInfo _existingCryptoSessions = TableInfo.read(db, "crypto_sessions");
        if (!_infoCryptoSessions.equals(_existingCryptoSessions)) {
          return new RoomOpenHelper.ValidationResult(false, "crypto_sessions(com.quickchat.core.database.entities.SessionEntity).\n"
                  + " Expected:\n" + _infoCryptoSessions + "\n"
                  + " Found:\n" + _existingCryptoSessions);
        }
        final HashMap<String, TableInfo.Column> _columnsOutboxQueue = new HashMap<String, TableInfo.Column>(7);
        _columnsOutboxQueue.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOutboxQueue.put("recipientPhone", new TableInfo.Column("recipientPhone", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOutboxQueue.put("isGroup", new TableInfo.Column("isGroup", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOutboxQueue.put("messageType", new TableInfo.Column("messageType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOutboxQueue.put("plainText", new TableInfo.Column("plainText", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOutboxQueue.put("mediaPath", new TableInfo.Column("mediaPath", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOutboxQueue.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysOutboxQueue = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesOutboxQueue = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoOutboxQueue = new TableInfo("outbox_queue", _columnsOutboxQueue, _foreignKeysOutboxQueue, _indicesOutboxQueue);
        final TableInfo _existingOutboxQueue = TableInfo.read(db, "outbox_queue");
        if (!_infoOutboxQueue.equals(_existingOutboxQueue)) {
          return new RoomOpenHelper.ValidationResult(false, "outbox_queue(com.quickchat.core.database.entities.OutboxEntity).\n"
                  + " Expected:\n" + _infoOutboxQueue + "\n"
                  + " Found:\n" + _existingOutboxQueue);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "b3012d56e1545d779f6d589c196f460d", "bc26b81d2880753a8f765d98dc9ac2dd");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "messages","chats","statuses","crypto_sessions","outbox_queue");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `messages`");
      _db.execSQL("DELETE FROM `chats`");
      _db.execSQL("DELETE FROM `statuses`");
      _db.execSQL("DELETE FROM `crypto_sessions`");
      _db.execSQL("DELETE FROM `outbox_queue`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(ChatDao.class, ChatDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(MessageDao.class, MessageDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(StatusDao.class, StatusDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SessionDao.class, SessionDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(OutboxDao.class, OutboxDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public ChatDao chatDao() {
    if (_chatDao != null) {
      return _chatDao;
    } else {
      synchronized(this) {
        if(_chatDao == null) {
          _chatDao = new ChatDao_Impl(this);
        }
        return _chatDao;
      }
    }
  }

  @Override
  public MessageDao messageDao() {
    if (_messageDao != null) {
      return _messageDao;
    } else {
      synchronized(this) {
        if(_messageDao == null) {
          _messageDao = new MessageDao_Impl(this);
        }
        return _messageDao;
      }
    }
  }

  @Override
  public StatusDao statusDao() {
    if (_statusDao != null) {
      return _statusDao;
    } else {
      synchronized(this) {
        if(_statusDao == null) {
          _statusDao = new StatusDao_Impl(this);
        }
        return _statusDao;
      }
    }
  }

  @Override
  public SessionDao sessionDao() {
    if (_sessionDao != null) {
      return _sessionDao;
    } else {
      synchronized(this) {
        if(_sessionDao == null) {
          _sessionDao = new SessionDao_Impl(this);
        }
        return _sessionDao;
      }
    }
  }

  @Override
  public OutboxDao outboxDao() {
    if (_outboxDao != null) {
      return _outboxDao;
    } else {
      synchronized(this) {
        if(_outboxDao == null) {
          _outboxDao = new OutboxDao_Impl(this);
        }
        return _outboxDao;
      }
    }
  }
}
