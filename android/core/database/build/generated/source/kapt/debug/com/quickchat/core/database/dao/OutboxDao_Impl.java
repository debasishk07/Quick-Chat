package com.quickchat.core.database.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.quickchat.core.database.entities.OutboxEntity;
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

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class OutboxDao_Impl implements OutboxDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<OutboxEntity> __insertionAdapterOfOutboxEntity;

  private final SharedSQLiteStatement __preparedStmtOfDequeueMessage;

  public OutboxDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfOutboxEntity = new EntityInsertionAdapter<OutboxEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `outbox_queue` (`id`,`recipientPhone`,`isGroup`,`messageType`,`plainText`,`mediaPath`,`timestamp`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final OutboxEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
        if (entity.getRecipientPhone() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getRecipientPhone());
        }
        final int _tmp = entity.isGroup() ? 1 : 0;
        statement.bindLong(3, _tmp);
        if (entity.getMessageType() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getMessageType());
        }
        if (entity.getPlainText() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getPlainText());
        }
        if (entity.getMediaPath() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getMediaPath());
        }
        statement.bindLong(7, entity.getTimestamp());
      }
    };
    this.__preparedStmtOfDequeueMessage = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM outbox_queue WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object enqueueMessage(final OutboxEntity msg,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfOutboxEntity.insert(msg);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object dequeueMessage(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDequeueMessage.acquire();
        int _argIndex = 1;
        if (id == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, id);
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
          __preparedStmtOfDequeueMessage.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllQueuedMessages(final Continuation<? super List<OutboxEntity>> $completion) {
    final String _sql = "SELECT * FROM outbox_queue ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<OutboxEntity>>() {
      @Override
      @NonNull
      public List<OutboxEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRecipientPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "recipientPhone");
          final int _cursorIndexOfIsGroup = CursorUtil.getColumnIndexOrThrow(_cursor, "isGroup");
          final int _cursorIndexOfMessageType = CursorUtil.getColumnIndexOrThrow(_cursor, "messageType");
          final int _cursorIndexOfPlainText = CursorUtil.getColumnIndexOrThrow(_cursor, "plainText");
          final int _cursorIndexOfMediaPath = CursorUtil.getColumnIndexOrThrow(_cursor, "mediaPath");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final List<OutboxEntity> _result = new ArrayList<OutboxEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final OutboxEntity _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
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
            final String _tmpMessageType;
            if (_cursor.isNull(_cursorIndexOfMessageType)) {
              _tmpMessageType = null;
            } else {
              _tmpMessageType = _cursor.getString(_cursorIndexOfMessageType);
            }
            final String _tmpPlainText;
            if (_cursor.isNull(_cursorIndexOfPlainText)) {
              _tmpPlainText = null;
            } else {
              _tmpPlainText = _cursor.getString(_cursorIndexOfPlainText);
            }
            final String _tmpMediaPath;
            if (_cursor.isNull(_cursorIndexOfMediaPath)) {
              _tmpMediaPath = null;
            } else {
              _tmpMediaPath = _cursor.getString(_cursorIndexOfMediaPath);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            _item = new OutboxEntity(_tmpId,_tmpRecipientPhone,_tmpIsGroup,_tmpMessageType,_tmpPlainText,_tmpMediaPath,_tmpTimestamp);
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
