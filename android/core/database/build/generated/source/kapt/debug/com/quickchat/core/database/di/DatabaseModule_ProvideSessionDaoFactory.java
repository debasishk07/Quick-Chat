package com.quickchat.core.database.di;

import com.quickchat.core.database.AppDatabase;
import com.quickchat.core.database.dao.SessionDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class DatabaseModule_ProvideSessionDaoFactory implements Factory<SessionDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideSessionDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public SessionDao get() {
    return provideSessionDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideSessionDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideSessionDaoFactory(dbProvider);
  }

  public static SessionDao provideSessionDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideSessionDao(db));
  }
}
