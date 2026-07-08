package com.quickchat.feature.chat;

import com.quickchat.core.network.repository.ChatRepository;
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
public final class ChatListViewModel_Factory implements Factory<ChatListViewModel> {
  private final Provider<ChatRepository> chatRepositoryProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  public ChatListViewModel_Factory(Provider<ChatRepository> chatRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider) {
    this.chatRepositoryProvider = chatRepositoryProvider;
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public ChatListViewModel get() {
    return newInstance(chatRepositoryProvider.get(), userRepositoryProvider.get());
  }

  public static ChatListViewModel_Factory create(Provider<ChatRepository> chatRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider) {
    return new ChatListViewModel_Factory(chatRepositoryProvider, userRepositoryProvider);
  }

  public static ChatListViewModel newInstance(ChatRepository chatRepository,
      UserRepository userRepository) {
    return new ChatListViewModel(chatRepository, userRepository);
  }
}
