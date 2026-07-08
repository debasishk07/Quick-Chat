package com.quickchat.core.network.settings;

import android.content.SharedPreferences;
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
public final class SettingsManager_Factory implements Factory<SettingsManager> {
  private final Provider<SharedPreferences> prefsProvider;

  public SettingsManager_Factory(Provider<SharedPreferences> prefsProvider) {
    this.prefsProvider = prefsProvider;
  }

  @Override
  public SettingsManager get() {
    return newInstance(prefsProvider.get());
  }

  public static SettingsManager_Factory create(Provider<SharedPreferences> prefsProvider) {
    return new SettingsManager_Factory(prefsProvider);
  }

  public static SettingsManager newInstance(SharedPreferences prefs) {
    return new SettingsManager(prefs);
  }
}
