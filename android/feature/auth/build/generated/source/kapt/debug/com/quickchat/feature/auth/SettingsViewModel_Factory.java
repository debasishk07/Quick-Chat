package com.quickchat.feature.auth;

import com.quickchat.core.network.repository.UserRepository;
import com.quickchat.core.network.settings.SettingsManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<SettingsManager> settingsManagerProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  public SettingsViewModel_Factory(Provider<SettingsManager> settingsManagerProvider,
      Provider<UserRepository> userRepositoryProvider) {
    this.settingsManagerProvider = settingsManagerProvider;
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(settingsManagerProvider.get(), userRepositoryProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<SettingsManager> settingsManagerProvider,
      Provider<UserRepository> userRepositoryProvider) {
    return new SettingsViewModel_Factory(settingsManagerProvider, userRepositoryProvider);
  }

  public static SettingsViewModel newInstance(SettingsManager settingsManager,
      UserRepository userRepository) {
    return new SettingsViewModel(settingsManager, userRepository);
  }
}
