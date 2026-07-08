package com.quickchat.core.database.di;

import com.quickchat.core.database.AppDatabase;
import com.quickchat.core.database.dao.StatusDao;
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
public final class DatabaseModule_ProvideStatusDaoFactory implements Factory<StatusDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideStatusDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public StatusDao get() {
    return provideStatusDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideStatusDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideStatusDaoFactory(dbProvider);
  }

  public static StatusDao provideStatusDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideStatusDao(db));
  }
}
