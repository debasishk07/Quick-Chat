package com.quickchat.feature.chat;

import com.quickchat.core.network.api.QuickChatApi;
import com.quickchat.core.network.repository.ChatRepository;
import com.quickchat.core.network.repository.UserRepository;
import com.quickchat.core.network.websocket.SocketManager;
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
public final class ChatRoomViewModel_Factory implements Factory<ChatRoomViewModel> {
  private final Provider<ChatRepository> chatRepositoryProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  private final Provider<SocketManager> socketManagerProvider;

  private final Provider<QuickChatApi> apiProvider;

  public ChatRoomViewModel_Factory(Provider<ChatRepository> chatRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider,
      Provider<SocketManager> socketManagerProvider, Provider<QuickChatApi> apiProvider) {
    this.chatRepositoryProvider = chatRepositoryProvider;
    this.userRepositoryProvider = userRepositoryProvider;
    this.socketManagerProvider = socketManagerProvider;
    this.apiProvider = apiProvider;
  }

  @Override
  public ChatRoomViewModel get() {
    return newInstance(chatRepositoryProvider.get(), userRepositoryProvider.get(), socketManagerProvider.get(), apiProvider.get());
  }

  public static ChatRoomViewModel_Factory create(Provider<ChatRepository> chatRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider,
      Provider<SocketManager> socketManagerProvider, Provider<QuickChatApi> apiProvider) {
    return new ChatRoomViewModel_Factory(chatRepositoryProvider, userRepositoryProvider, socketManagerProvider, apiProvider);
  }

  public static ChatRoomViewModel newInstance(ChatRepository chatRepository,
      UserRepository userRepository, SocketManager socketManager, QuickChatApi api) {
    return new ChatRoomViewModel(chatRepository, userRepository, socketManager, api);
  }
}
