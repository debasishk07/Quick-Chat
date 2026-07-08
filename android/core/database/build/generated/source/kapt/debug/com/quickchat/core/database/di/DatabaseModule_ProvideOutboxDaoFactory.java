package com.quickchat.core.database.di;

import com.quickchat.core.database.AppDatabase;
import com.quickchat.core.database.dao.OutboxDao;
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
public final class DatabaseModule_ProvideOutboxDaoFactory implements Factory<OutboxDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideOutboxDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public OutboxDao get() {
    return provideOutboxDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideOutboxDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideOutboxDaoFactory(dbProvider);
  }

  public static OutboxDao provideOutboxDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideOutboxDao(db));
  }
}
