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
import com.quickchat.core.database.entities.MessageEntity;
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
public final class MessageDao_Impl implements MessageDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<MessageEntity> __insertionAdapterOfMessageEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateMessageStatus;

  public MessageDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMessageEntity = new EntityInsertionAdapter<MessageEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `messages` (`id`,`senderPhone`,`recipientPhone`,`isGroup`,`ciphertext`,`iv`,`ephemeralPublicKey`,`messageType`,`timestamp`,`status`,`plainText`) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MessageEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
        if (entity.getSenderPhone() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getSenderPhone());
        }
        if (entity.getRecipientPhone() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getRecipientPhone());
        }
        final int _tmp = entity.isGroup() ? 1 : 0;
        statement.bindLong(4, _tmp);
        if (entity.getCiphertext() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getCiphertext());
        }
        if (entity.getIv() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getIv());
        }
        if (entity.getEphemeralPublicKey() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getEphemeralPublicKey());
        }
        if (entity.getMessageType() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getMessageType());
        }
        statement.bindLong(9, entity.getTimestamp());
        if (entity.getStatus() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getStatus());
        }
        if (entity.getPlainText() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getPlainText());
        }
      }
    };
    this.__preparedStmtOfUpdateMessageStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE messages SET status = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertMessage(final MessageEntity message,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfMessageEntity.insert(message);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateMessageStatus(final String messageId, final String status,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateMessageStatus.acquire();
        int _argIndex = 1;
        if (status == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, status);
        }
        _argIndex = 2;
        if (messageId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, messageId);
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
          __preparedStmtOfUpdateMessageStatus.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<MessageEntity>> getMessagesFlow(final String chatPartnerPhone) {
    final String _sql = "SELECT * FROM messages WHERE senderPhone = ? OR recipientPhone = ? ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (chatPartnerPhone == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, chatPartnerPhone);
    }
    _argIndex = 2;
    if (chatPartnerPhone == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, chatPartnerPhone);
    }
    return CoroutinesRoom.createFlow(__db, false, new String[] {"messages"}, new Callable<List<MessageEntity>>() {
      @Override
      @NonNull
      public List<MessageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSenderPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "senderPhone");
          final int _cursorIndexOfRecipientPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "recipientPhone");
          final int _cursorIndexOfIsGroup = CursorUtil.getColumnIndexOrThrow(_cursor, "isGroup");
          final int _cursorIndexOfCiphertext = CursorUtil.getColumnIndexOrThrow(_cursor, "ciphertext");
          final int _cursorIndexOfIv = CursorUtil.getColumnIndexOrThrow(_cursor, "iv");
          final int _cursorIndexOfEphemeralPublicKey = CursorUtil.getColumnIndexOrThrow(_cursor, "ephemeralPublicKey");
          final int _cursorIndexOfMessageType = CursorUtil.getColumnIndexOrThrow(_cursor, "messageType");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfPlainText = CursorUtil.getColumnIndexOrThrow(_cursor, "plainText");
          final List<MessageEntity> _result = new ArrayList<MessageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MessageEntity _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpSenderPhone;
            if (_cursor.isNull(_cursorIndexOfSenderPhone)) {
              _tmpSenderPhone = null;
            } else {
              _tmpSenderPhone = _cursor.getString(_cursorIndexOfSenderPhone);
            }
            final String _tmpRecipientPhone;
            if (_cursor.isNull(_cursorIndexOfRecipientPhone)) {
              _tmpRecipientPhone = null;
            } else {
              _tmpRecipientPhone = _cursor.getString(_cursorIndexOfRecipientPhone);
            }
            final boolean _tmpIsGroup;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsGroup);
            _tmpIsGroup = _tmp != 0;
            final String _tmpCiphertext;
            if (_cursor.isNull(_cursorIndexOfCiphertext)) {
              _tmpCiphertext = null;
            } else {
              _tmpCiphertext = _cursor.getString(_cursorIndexOfCiphertext);
            }
            final String _tmpIv;
            if (_cursor.isNull(_cursorIndexOfIv)) {
              _tmpIv = null;
            } else {
              _tmpIv = _cursor.getString(_cursorIndexOfIv);
            }
            final String _tmpEphemeralPublicKey;
            if (_cursor.isNull(_cursorIndexOfEphemeralPublicKey)) {
              _tmpEphemeralPublicKey = null;
            } else {
              _tmpEphemeralPublicKey = _cursor.getString(_cursorIndexOfEphemeralPublicKey);
            }
            final String _tmpMessageType;
            if (_cursor.isNull(_cursorIndexOfMessageType)) {
              _tmpMessageType = null;
            } else {
              _tmpMessageType = _cursor.getString(_cursorIndexOfMessageType);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final String _tmpPlainText;
            if (_cursor.isNull(_cursorIndexOfPlainText)) {
              _tmpPlainText = null;
            } else {
              _tmpPlainText = _cursor.getString(_cursorIndexOfPlainText);
            }
            _item = new MessageEntity(_tmpId,_tmpSenderPhone,_tmpRecipientPhone,_tmpIsGroup,_tmpCiphertext,_tmpIv,_tmpEphemeralPublicKey,_tmpMessageType,_tmpTimestamp,_tmpStatus,_tmpPlainText);
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
  public Object getMessage(final String messageId,
      final Continuation<? super MessageEntity> $completion) {
    final String _sql = "SELECT * FROM messages WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (messageId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, messageId);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<MessageEntity>() {
      @Override
      @Nullable
      public MessageEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSenderPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "senderPhone");
          final int _cursorIndexOfRecipientPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "recipientPhone");
          final int _cursorIndexOfIsGroup = CursorUtil.getColumnIndexOrThrow(_cursor, "isGroup");
          final int _cursorIndexOfCiphertext = CursorUtil.getColumnIndexOrThrow(_cursor, "ciphertext");
          final int _cursorIndexOfIv = CursorUtil.getColumnIndexOrThrow(_cursor, "iv");
          final int _cursorIndexOfEphemeralPublicKey = CursorUtil.getColumnIndexOrThrow(_cursor, "ephemeralPublicKey");
          final int _cursorIndexOfMessageType = CursorUtil.getColumnIndexOrThrow(_cursor, "messageType");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfPlainText = CursorUtil.getColumnIndexOrThrow(_cursor, "plainText");
          final MessageEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpSenderPhone;
            if (_cursor.isNull(_cursorIndexOfSenderPhone)) {
              _tmpSenderPhone = null;
            } else {
              _tmpSenderPhone = _cursor.getString(_cursorIndexOfSenderPhone);
            }
            final String _tmpRecipientPhone;
            if (_cursor.isNull(_cursorIndexOfRecipientPhone)) {
              _tmpRecipientPhone = null;
            } else {
              _tmpRecipientPhone = _cursor.getString(_cursorIndexOfRecipientPhone);
            }
            final boolean _tmpIsGroup;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsGroup);
            _tmpIsGroup = _tmp != 0;
            final String _tmpCiphertext;
            if (_cursor.isNull(_cursorIndexOfCiphertext)) {
              _tmpCiphertext = null;
            } else {
              _tmpCiphertext = _cursor.getString(_cursorIndexOfCiphertext);
            }
            final String _tmpIv;
            if (_cursor.isNull(_cursorIndexOfIv)) {
              _tmpIv = null;
            } else {
              _tmpIv = _cursor.getString(_cursorIndexOfIv);
            }
            final String _tmpEphemeralPublicKey;
            if (_cursor.isNull(_cursorIndexOfEphemeralPublicKey)) {
              _tmpEphemeralPublicKey = null;
            } else {
              _tmpEphemeralPublicKey = _cursor.getString(_cursorIndexOfEphemeralPublicKey);
            }
            final String _tmpMessageType;
            if (_cursor.isNull(_cursorIndexOfMessageType)) {
              _tmpMessageType = null;
            } else {
              _tmpMessageType = _cursor.getString(_cursorIndexOfMessageType);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final String _tmpPlainText;
            if (_cursor.isNull(_cursorIndexOfPlainText)) {
              _tmpPlainText = null;
            } else {
              _tmpPlainText = _cursor.getString(_cursorIndexOfPlainText);
            }
            _result = new MessageEntity(_tmpId,_tmpSenderPhone,_tmpRecipientPhone,_tmpIsGroup,_tmpCiphertext,_tmpIv,_tmpEphemeralPublicKey,_tmpMessageType,_tmpTimestamp,_tmpStatus,_tmpPlainText);
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

  @Override
  public Object searchMessages(final String query,
      final Continuation<? super List<MessageEntity>> $completion) {
    final String _sql = "SELECT * FROM messages WHERE plainText LIKE '%' || ? || '%' ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (query == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, query);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<MessageEntity>>() {
      @Override
      @NonNull
      public List<MessageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSenderPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "senderPhone");
          final int _cursorIndexOfRecipientPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "recipientPhone");
          final int _cursorIndexOfIsGroup = CursorUtil.getColumnIndexOrThrow(_cursor, "isGroup");
          final int _cursorIndexOfCiphertext = CursorUtil.getColumnIndexOrThrow(_cursor, "ciphertext");
          final int _cursorIndexOfIv = CursorUtil.getColumnIndexOrThrow(_cursor, "iv");
          final int _cursorIndexOfEphemeralPublicKey = CursorUtil.getColumnIndexOrThrow(_cursor, "ephemeralPublicKey");
          final int _cursorIndexOfMessageType = CursorUtil.getColumnIndexOrThrow(_cursor, "messageType");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfPlainText = CursorUtil.getColumnIndexOrThrow(_cursor, "plainText");
          final List<MessageEntity> _result = new ArrayList<MessageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MessageEntity _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpSenderPhone;
            if (_cursor.isNull(_cursorIndexOfSenderPhone)) {
              _tmpSenderPhone = null;
            } else {
              _tmpSenderPhone = _cursor.getString(_cursorIndexOfSenderPhone);
            }
            final String _tmpRecipientPhone;
            if (_cursor.isNull(_cursorIndexOfRecipientPhone)) {
              _tmpRecipientPhone = null;
            } else {
              _tmpRecipientPhone = _cursor.getString(_cursorIndexOfRecipientPhone);
            }
            final boolean _tmpIsGroup;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsGroup);
            _tmpIsGroup = _tmp != 0;
            final String _tmpCiphertext;
            if (_cursor.isNull(_cursorIndexOfCiphertext)) {
              _tmpCiphertext = null;
            } else {
              _tmpCiphertext = _cursor.getString(_cursorIndexOfCiphertext);
            }
            final String _tmpIv;
            if (_cursor.isNull(_cursorIndexOfIv)) {
              _tmpIv = null;
            } else {
              _tmpIv = _cursor.getString(_cursorIndexOfIv);
            }
            final String _tmpEphemeralPublicKey;
            if (_cursor.isNull(_cursorIndexOfEphemeralPublicKey)) {
              _tmpEphemeralPublicKey = null;
            } else {
              _tmpEphemeralPublicKey = _cursor.getString(_cursorIndexOfEphemeralPublicKey);
            }
            final String _tmpMessageType;
            if (_cursor.isNull(_cursorIndexOfMessageType)) {
              _tmpMessageType = null;
            } else {
              _tmpMessageType = _cursor.getString(_cursorIndexOfMessageType);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final String _tmpPlainText;
            if (_cursor.isNull(_cursorIndexOfPlainText)) {
              _tmpPlainText = null;
            } else {
              _tmpPlainText = _cursor.getString(_cursorIndexOfPlainText);
            }
            _item = new MessageEntity(_tmpId,_tmpSenderPhone,_tmpRecipientPhone,_tmpIsGroup,_tmpCiphertext,_tmpIv,_tmpEphemeralPublicKey,_tmpMessageType,_tmpTimestamp,_tmpStatus,_tmpPlainText);
            _result.add(_item);
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
