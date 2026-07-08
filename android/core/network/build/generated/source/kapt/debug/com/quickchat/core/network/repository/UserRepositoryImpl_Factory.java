package com.quickchat.core.network.repository;

import android.content.SharedPreferences;
import com.quickchat.core.database.AppDatabase;
import com.quickchat.core.network.api.QuickChatApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class UserRepositoryImpl_Factory implements Factory<UserRepositoryImpl> {
  private final Provider<QuickChatApi> apiProvider;

  private final Provider<SharedPreferences> prefsProvider;

  private final Provider<AppDatabase> dbProvider;

  public UserRepositoryImpl_Factory(Provider<QuickChatApi> apiProvider,
      Provider<SharedPreferences> prefsProvider, Provider<AppDatabase> dbProvider) {
    this.apiProvider = apiProvider;
    this.prefsProvider = prefsProvider;
    this.dbProvider = dbProvider;
  }

  @Override
  public UserRepositoryImpl get() {
    return newInstance(apiProvider.get(), prefsProvider.get(), dbProvider.get());
  }

  public static UserRepositoryImpl_Factory create(Provider<QuickChatApi> apiProvider,
      Provider<SharedPreferences> prefsProvider, Provider<AppDatabase> dbProvider) {
    return new UserRepositoryImpl_Factory(apiProvider, prefsProvider, dbProvider);
  }

  public static UserRepositoryImpl newInstance(QuickChatApi api, SharedPreferences prefs,
      AppDatabase db) {
    return new UserRepositoryImpl(api, prefs, db);
  }
}
