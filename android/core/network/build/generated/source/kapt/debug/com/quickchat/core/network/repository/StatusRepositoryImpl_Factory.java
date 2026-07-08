package com.quickchat.core.network.repository;

import com.quickchat.core.database.dao.StatusDao;
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
public final class StatusRepositoryImpl_Factory implements Factory<StatusRepositoryImpl> {
  private final Provider<QuickChatApi> apiProvider;

  private final Provider<StatusDao> statusDaoProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  public StatusRepositoryImpl_Factory(Provider<QuickChatApi> apiProvider,
      Provider<StatusDao> statusDaoProvider, Provider<UserRepository> userRepositoryProvider) {
    this.apiProvider = apiProvider;
    this.statusDaoProvider = statusDaoProvider;
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public StatusRepositoryImpl get() {
    return newInstance(apiProvider.get(), statusDaoProvider.get(), userRepositoryProvider.get());
  }

  public static StatusRepositoryImpl_Factory create(Provider<QuickChatApi> apiProvider,
      Provider<StatusDao> statusDaoProvider, Provider<UserRepository> userRepositoryProvider) {
    return new StatusRepositoryImpl_Factory(apiProvider, statusDaoProvider, userRepositoryProvider);
  }

  public static StatusRepositoryImpl newInstance(QuickChatApi api, StatusDao statusDao,
      UserRepository userRepository) {
    return new StatusRepositoryImpl(api, statusDao, userRepository);
  }
}
