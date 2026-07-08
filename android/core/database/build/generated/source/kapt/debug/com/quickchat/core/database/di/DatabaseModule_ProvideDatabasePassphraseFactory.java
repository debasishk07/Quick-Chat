package com.quickchat.core.database.di;

import android.content.SharedPreferences;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideDatabasePassphraseFactory implements Factory<byte[]> {
  private final Provider<SharedPreferences> sharedPrefsProvider;

  public DatabaseModule_ProvideDatabasePassphraseFactory(
      Provider<SharedPreferences> sharedPrefsProvider) {
    this.sharedPrefsProvider = sharedPrefsProvider;
  }

  @Override
  public byte[] get() {
    return provideDatabasePassphrase(sharedPrefsProvider.get());
  }

  public static DatabaseModule_ProvideDatabasePassphraseFactory create(
      Provider<SharedPreferences> sharedPrefsProvider) {
    return new DatabaseModule_ProvideDatabasePassphraseFactory(sharedPrefsProvider);
  }

  public static byte[] provideDatabasePassphrase(SharedPreferences sharedPrefs) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideDatabasePassphrase(sharedPrefs));
  }
}
