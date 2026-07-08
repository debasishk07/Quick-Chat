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
import com.quickchat.core.database.entities.SessionEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class SessionDao_Impl implements SessionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SessionEntity> __insertionAdapterOfSessionEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteSession;

  public SessionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSessionEntity = new EntityInsertionAdapter<SessionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `crypto_sessions` (`recipientPhone`,`sessionJson`) VALUES (?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SessionEntity entity) {
        if (entity.getRecipientPhone() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getRecipientPhone());
        }
        if (entity.getSessionJson() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getSessionJson());
        }
      }
    };
    this.__preparedStmtOfDeleteSession = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM crypto_sessions WHERE recipientPhone = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertSession(final SessionEntity session,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSessionEntity.insert(session);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteSession(final String recipientPhone,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteSession.acquire();
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
          __preparedStmtOfDeleteSession.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getSession(final String recipientPhone,
      final Continuation<? super SessionEntity> $completion) {
    final String _sql = "SELECT * FROM crypto_sessions WHERE recipientPhone = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (recipientPhone == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, recipientPhone);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<SessionEntity>() {
      @Override
      @Nullable
      public SessionEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfRecipientPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "recipientPhone");
          final int _cursorIndexOfSessionJson = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionJson");
          final SessionEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpRecipientPhone;
            if (_cursor.isNull(_cursorIndexOfRecipientPhone)) {
              _tmpRecipientPhone = null;
            } else {
              _tmpRecipientPhone = _cursor.getString(_cursorIndexOfRecipientPhone);
            }
            final String _tmpSessionJson;
            if (_cursor.isNull(_cursorIndexOfSessionJson)) {
              _tmpSessionJson = null;
            } else {
              _tmpSessionJson = _cursor.getString(_cursorIndexOfSessionJson);
            }
            _result = new SessionEntity(_tmpRecipientPhone,_tmpSessionJson);
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
