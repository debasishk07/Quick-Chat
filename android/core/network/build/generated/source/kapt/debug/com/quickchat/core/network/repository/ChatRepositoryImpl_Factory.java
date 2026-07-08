package com.quickchat.core.network.repository;

import com.quickchat.core.database.dao.ChatDao;
import com.quickchat.core.database.dao.MessageDao;
import com.quickchat.core.database.dao.OutboxDao;
import com.quickchat.core.database.dao.SessionDao;
import com.quickchat.core.network.api.QuickChatApi;
import com.quickchat.core.network.websocket.SocketManager;
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
public final class ChatRepositoryImpl_Factory implements Factory<ChatRepositoryImpl> {
  private final Provider<QuickChatApi> apiProvider;

  private final Provider<SocketManager> socketManagerProvider;

  private final Provider<ChatDao> chatDaoProvider;

  private final Provider<MessageDao> messageDaoProvider;

  private final Provider<SessionDao> sessionDaoProvider;

  private final Provider<OutboxDao> outboxDaoProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  public ChatRepositoryImpl_Factory(Provider<QuickChatApi> apiProvider,
      Provider<SocketManager> socketManagerProvider, Provider<ChatDao> chatDaoProvider,
      Provider<MessageDao> messageDaoProvider, Provider<SessionDao> sessionDaoProvider,
      Provider<OutboxDao> outboxDaoProvider, Provider<UserRepository> userRepositoryProvider) {
    this.apiProvider = apiProvider;
    this.socketManagerProvider = socketManagerProvider;
    this.chatDaoProvider = chatDaoProvider;
    this.messageDaoProvider = messageDaoProvider;
    this.sessionDaoProvider = sessionDaoProvider;
    this.outboxDaoProvider = outboxDaoProvider;
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public ChatRepositoryImpl get() {
    return newInstance(apiProvider.get(), socketManagerProvider.get(), chatDaoProvider.get(), messageDaoProvider.get(), sessionDaoProvider.get(), outboxDaoProvider.get(), userRepositoryProvider.get());
  }

  public static ChatRepositoryImpl_Factory create(Provider<QuickChatApi> apiProvider,
      Provider<SocketManager> socketManagerProvider, Provider<ChatDao> chatDaoProvider,
      Provider<MessageDao> messageDaoProvider, Provider<SessionDao> sessionDaoProvider,
      Provider<OutboxDao> outboxDaoProvider, Provider<UserRepository> userRepositoryProvider) {
    return new ChatRepositoryImpl_Factory(apiProvider, socketManagerProvider, chatDaoProvider, messageDaoProvider, sessionDaoProvider, outboxDaoProvider, userRepositoryProvider);
  }

  public static ChatRepositoryImpl newInstance(QuickChatApi api, SocketManager socketManager,
      ChatDao chatDao, MessageDao messageDao, SessionDao sessionDao, OutboxDao outboxDao,
      UserRepository userRepository) {
    return new ChatRepositoryImpl(api, socketManager, chatDao, messageDao, sessionDao, outboxDao, userRepository);
  }
}
