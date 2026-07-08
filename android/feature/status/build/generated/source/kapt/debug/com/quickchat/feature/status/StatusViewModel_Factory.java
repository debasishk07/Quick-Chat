package com.quickchat.feature.status;

import com.quickchat.core.network.repository.StatusRepository;
import com.quickchat.core.network.repository.UserRepository;
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
public final class StatusViewModel_Factory implements Factory<StatusViewModel> {
  private final Provider<StatusRepository> statusRepositoryProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  public StatusViewModel_Factory(Provider<StatusRepository> statusRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider) {
    this.statusRepositoryProvider = statusRepositoryProvider;
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public StatusViewModel get() {
    return newInstance(statusRepositoryProvider.get(), userRepositoryProvider.get());
  }

  public static StatusViewModel_Factory create(Provider<StatusRepository> statusRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider) {
    return new StatusViewModel_Factory(statusRepositoryProvider, userRepositoryProvider);
  }

  public static StatusViewModel newInstance(StatusRepository statusRepository,
      UserRepository userRepository) {
    return new StatusViewModel(statusRepository, userRepository);
  }
}
