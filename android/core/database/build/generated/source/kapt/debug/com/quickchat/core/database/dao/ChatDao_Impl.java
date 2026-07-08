package com.quickchat.core.database.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.quickchat.core.database.entities.ChatEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ChatDao_Impl implements ChatDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ChatEntity> __insertionAdapterOfChatEntity;

  private final SharedSQLiteStatement __preparedStmtOfClearUnreadCount;

  private final SharedSQLiteStatement __preparedStmtOfDeleteChat;

  public ChatDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfChatEntity = new EntityInsertionAdapter<ChatEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `chats` (`recipientPhone`,`displayName`,`avatarUrl`,`isGroup`,`lastMessageId`,`unreadCount`,`isPinned`,`isMuted`,`isArchived`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ChatEntity entity) {
        if (entity.getRecipientPhone() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getRecipientPhone());
        }
        if (entity.getDisplayName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getDisplayName());
        }
        if (entity.getAvatarUrl() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getAvatarUrl());
        }
        final int _tmp = entity.isGroup() ? 1 : 0;
        statement.bindLong(4, _tmp);
        if (entity.getLastMessageId() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getLastMessageId());
        }
        statement.bindLong(6, entity.getUnreadCount());
        final int _tmp_1 = entity.isPinned() ? 1 : 0;
        statement.bindLong(7, _tmp_1);
        final int _tmp_2 = entity.isMuted() ? 1 : 0;
        statement.bindLong(8, _tmp_2);
        final int _tmp_3 = entity.isArchived() ? 1 : 0;
        statement.bindLong(9, _tmp_3);
      }
    };
    this.__preparedStmtOfClearUnreadCount = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE chats SET unreadCount = 0 WHERE recipientPhone = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteChat = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM chats WHERE recipientPhone = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertOrUpdateChat(final ChatEntity chat,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfChatEntity.insert(chat);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object clearUnreadCount(final String recipientPhone,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearUnreadCount.acquire();
        int _argIndex = 1;
        if (recipientPhone == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, recipientPhone);
        }
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearUnreadCount.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteChat(final String recipientPhone,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteChat.acquire();
        int _argIndex = 1;
        if (recipientPhone == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, recipientPhone);
        }
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteChat.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ChatEntity>> getAllChatsFlow() {
    final String _sql = "SELECT * FROM chats ORDER BY isPinned DESC, lastMessageId DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"chats"}, new Callable<List<ChatEntity>>() {
      @Override
      @NonNull
      public List<ChatEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfRecipientPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "recipientPhone");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfAvatarUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "avatarUrl");
          final int _cursorIndexOfIsGroup = CursorUtil.getColumnIndexOrThrow(_cursor, "isGroup");
          final int _cursorIndexOfLastMessageId = CursorUtil.getColumnIndexOrThrow(_cursor, "lastMessageId");
          final int _cursorIndexOfUnreadCount = CursorUtil.getColumnIndexOrThrow(_cursor, "unreadCount");
          final int _cursorIndexOfIsPinned = CursorUtil.getColumnIndexOrThrow(_cursor, "isPinned");
          final int _cursorIndexOfIsMuted = CursorUtil.getColumnIndexOrThrow(_cursor, "isMuted");
          final int _cursorIndexOfIsArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "isArchived");
          final List<ChatEntity> _result = new ArrayList<ChatEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ChatEntity _item;
            final String _tmpRecipientPhone;
            if (_cursor.isNull(_cursorIndexOfRecipientPhone)) {
              _tmpRecipientPhone = null;
            } else {
              _tmpRecipientPhone = _cursor.getString(_cursorIndexOfRecipientPhone);
            }
            final String _tmpDisplayName;
            if (_cursor.isNull(_cursorIndexOfDisplayName)) {
              _tmpDisplayName = null;
            } else {
              _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            }
            final String _tmpAvatarUrl;
            if (_cursor.isNull(_cursorIndexOfAvatarUrl)) {
              _tmpAvatarUrl = null;
            } else {
              _tmpAvatarUrl = _cursor.getString(_cursorIndexOfAvatarUrl);
            }
            final boolean _tmpIsGroup;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsGroup);
            _tmpIsGroup = _tmp != 0;
            final String _tmpLastMessageId;
            if (_cursor.isNull(_cursorIndexOfLastMessageId)) {
              _tmpLastMessageId = null;
            } else {
              _tmpLastMessageId = _cursor.getString(_cursorIndexOfLastMessageId);
            }
            final int _tmpUnreadCount;
            _tmpUnreadCount = _cursor.getInt(_cursorIndexOfUnreadCount);
            final boolean _tmpIsPinned;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsPinned);
            _tmpIsPinned = _tmp_1 != 0;
            final boolean _tmpIsMuted;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsMuted);
            _tmpIsMuted = _tmp_2 != 0;
            final boolean _tmpIsArchived;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsArchived);
            _tmpIsArchived = _tmp_3 != 0;
            _item = new ChatEntity(_tmpRecipientPhone,_tmpDisplayName,_tmpAvatarUrl,_tmpIsGroup,_tmpLastMessageId,_tmpUnreadCount,_tmpIsPinned,_tmpIsMuted,_tmpIsArchived);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getChat(final String recipientPhone,
      final Continuation<? super ChatEntity> $completion) {
    final String _sql = "SELECT * FROM chats WHERE recipientPhone = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (recipientPhone == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, recipientPhone);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ChatEntity>() {
      @Override
      @Nullable
      public ChatEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfRecipientPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "recipientPhone");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfAvatarUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "avatarUrl");
          final int _cursorIndexOfIsGroup = CursorUtil.getColumnIndexOrThrow(_cursor, "isGroup");
          final int _cursorIndexOfLastMessageId = CursorUtil.getColumnIndexOrThrow(_cursor, "lastMessageId");
          final int _cursorIndexOfUnreadCount = CursorUtil.getColumnIndexOrThrow(_cursor, "unreadCount");
          final int _cursorIndexOfIsPinned = CursorUtil.getColumnIndexOrThrow(_cursor, "isPinned");
          final int _cursorIndexOfIsMuted = CursorUtil.getColumnIndexOrThrow(_cursor, "isMuted");
          final int _cursorIndexOfIsArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "isArchived");
          final ChatEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpRecipientPhone;
            if (_cursor.isNull(_cursorIndexOfRecipientPhone)) {
              _tmpRecipientPhone = null;
            } else {
              _tmpRecipientPhone = _cursor.getString(_cursorIndexOfRecipientPhone);
            }
            final String _tmpDisplayName;
            if (_cursor.isNull(_cursorIndexOfDisplayName)) {
              _tmpDisplayName = null;
            } else {
              _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            }
            final String _tmpAvatarUrl;
            if (_cursor.isNull(_cursorIndexOfAvatarUrl)) {
              _tmpAvatarUrl = null;
            } else {
              _tmpAvatarUrl = _cursor.getString(_cursorIndexOfAvatarUrl);
            }
            final boolean _tmpIsGroup;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsGroup);
            _tmpIsGroup = _tmp != 0;
            final String _tmpLastMessageId;
            if (_cursor.isNull(_cursorIndexOfLastMessageId)) {
              _tmpLastMessageId = null;
            } else {
              _tmpLastMessageId = _cursor.getString(_cursorIndexOfLastMessageId);
            }
            final int _tmpUnreadCount;
            _tmpUnreadCount = _cursor.getInt(_cursorIndexOfUnreadCount);
            final boolean _tmpIsPinned;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsPinned);
            _tmpIsPinned = _tmp_1 != 0;
            final boolean _tmpIsMuted;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsMuted);
            _tmpIsMuted = _tmp_2 != 0;
            final boolean _tmpIsArchived;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsArchived);
            _tmpIsArchived = _tmp_3 != 0;
            _result = new ChatEntity(_tmpRecipientPhone,_tmpDisplayName,_tmpAvatarUrl,_tmpIsGroup,_tmpLastMessageId,_tmpUnreadCount,_tmpIsPinned,_tmpIsMuted,_tmpIsArchived);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
