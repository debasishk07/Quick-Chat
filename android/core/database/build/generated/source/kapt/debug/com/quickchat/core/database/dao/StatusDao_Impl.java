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
import com.quickchat.core.database.entities.StatusEntity;
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
public final class StatusDao_Impl implements StatusDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<StatusEntity> __insertionAdapterOfStatusEntity;

  private final SharedSQLiteStatement __preparedStmtOfPurgeExpiredStatuses;

  private final SharedSQLiteStatement __preparedStmtOfClear;

  public StatusDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfStatusEntity = new EntityInsertionAdapter<StatusEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `statuses` (`id`,`senderPhone`,`mediaUrl`,`caption`,`mediaType`,`timestamp`,`expiresAt`,`viewersJson`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final StatusEntity entity) {
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
        if (entity.getMediaUrl() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getMediaUrl());
        }
        if (entity.getCaption() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getCaption());
        }
        if (entity.getMediaType() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getMediaType());
        }
        statement.bindLong(6, entity.getTimestamp());
        statement.bindLong(7, entity.getExpiresAt());
        if (entity.getViewersJson() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getViewersJson());
        }
      }
    };
    this.__preparedStmtOfPurgeExpiredStatuses = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM statuses WHERE expiresAt < ?";
        return _query;
      }
    };
    this.__preparedStmtOfClear = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM statuses";
        return _query;
      }
    };
  }

  @Override
  public Object insertStatus(final StatusEntity status,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfStatusEntity.insert(status);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object purgeExpiredStatuses(final long now, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfPurgeExpiredStatuses.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, now);
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
          __preparedStmtOfPurgeExpiredStatuses.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clear(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClear.acquire();
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
          __preparedStmtOfClear.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<StatusEntity>> getAllStatusesFlow() {
    final String _sql = "SELECT * FROM statuses ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"statuses"}, new Callable<List<StatusEntity>>() {
      @Override
      @NonNull
      public List<StatusEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSenderPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "senderPhone");
          final int _cursorIndexOfMediaUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "mediaUrl");
          final int _cursorIndexOfCaption = CursorUtil.getColumnIndexOrThrow(_cursor, "caption");
          final int _cursorIndexOfMediaType = CursorUtil.getColumnIndexOrThrow(_cursor, "mediaType");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfExpiresAt = CursorUtil.getColumnIndexOrThrow(_cursor, "expiresAt");
          final int _cursorIndexOfViewersJson = CursorUtil.getColumnIndexOrThrow(_cursor, "viewersJson");
          final List<StatusEntity> _result = new ArrayList<StatusEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final StatusEntity _item;
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
            final String _tmpMediaUrl;
            if (_cursor.isNull(_cursorIndexOfMediaUrl)) {
              _tmpMediaUrl = null;
            } else {
              _tmpMediaUrl = _cursor.getString(_cursorIndexOfMediaUrl);
            }
            final String _tmpCaption;
            if (_cursor.isNull(_cursorIndexOfCaption)) {
              _tmpCaption = null;
            } else {
              _tmpCaption = _cursor.getString(_cursorIndexOfCaption);
            }
            final String _tmpMediaType;
            if (_cursor.isNull(_cursorIndexOfMediaType)) {
              _tmpMediaType = null;
            } else {
              _tmpMediaType = _cursor.getString(_cursorIndexOfMediaType);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final long _tmpExpiresAt;
            _tmpExpiresAt = _cursor.getLong(_cursorIndexOfExpiresAt);
            final String _tmpViewersJson;
            if (_cursor.isNull(_cursorIndexOfViewersJson)) {
              _tmpViewersJson = null;
            } else {
              _tmpViewersJson = _cursor.getString(_cursorIndexOfViewersJson);
            }
            _item = new StatusEntity(_tmpId,_tmpSenderPhone,_tmpMediaUrl,_tmpCaption,_tmpMediaType,_tmpTimestamp,_tmpExpiresAt,_tmpViewersJson);
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
  public Object getStatus(final String statusId,
      final Continuation<? super StatusEntity> $completion) {
    final String _sql = "SELECT * FROM statuses WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (statusId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, statusId);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<StatusEntity>() {
      @Override
      @Nullable
      public StatusEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSenderPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "senderPhone");
          final int _cursorIndexOfMediaUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "mediaUrl");
          final int _cursorIndexOfCaption = CursorUtil.getColumnIndexOrThrow(_cursor, "caption");
          final int _cursorIndexOfMediaType = CursorUtil.getColumnIndexOrThrow(_cursor, "mediaType");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfExpiresAt = CursorUtil.getColumnIndexOrThrow(_cursor, "expiresAt");
          final int _cursorIndexOfViewersJson = CursorUtil.getColumnIndexOrThrow(_cursor, "viewersJson");
          final StatusEntity _result;
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
            final String _tmpMediaUrl;
            if (_cursor.isNull(_cursorIndexOfMediaUrl)) {
              _tmpMediaUrl = null;
            } else {
              _tmpMediaUrl = _cursor.getString(_cursorIndexOfMediaUrl);
            }
            final String _tmpCaption;
            if (_cursor.isNull(_cursorIndexOfCaption)) {
              _tmpCaption = null;
            } else {
              _tmpCaption = _cursor.getString(_cursorIndexOfCaption);
            }
            final String _tmpMediaType;
            if (_cursor.isNull(_cursorIndexOfMediaType)) {
              _tmpMediaType = null;
            } else {
              _tmpMediaType = _cursor.getString(_cursorIndexOfMediaType);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final long _tmpExpiresAt;
            _tmpExpiresAt = _cursor.getLong(_cursorIndexOfExpiresAt);
            final String _tmpViewersJson;
            if (_cursor.isNull(_cursorIndexOfViewersJson)) {
              _tmpViewersJson = null;
            } else {
              _tmpViewersJson = _cursor.getString(_cursorIndexOfViewersJson);
            }
            _result = new StatusEntity(_tmpId,_tmpSenderPhone,_tmpMediaUrl,_tmpCaption,_tmpMediaType,_tmpTimestamp,_tmpExpiresAt,_tmpViewersJson);
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
