package com.quickchat.core.network.di;

import com.quickchat.core.network.api.QuickChatApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.OkHttpClient;

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
public final class NetworkModule_ProvideQuickChatApiFactory implements Factory<QuickChatApi> {
  private final Provider<OkHttpClient> okHttpClientProvider;

  public NetworkModule_ProvideQuickChatApiFactory(Provider<OkHttpClient> okHttpClientProvider) {
    this.okHttpClientProvider = okHttpClientProvider;
  }

  @Override
  public QuickChatApi get() {
    return provideQuickChatApi(okHttpClientProvider.get());
  }

  public static NetworkModule_ProvideQuickChatApiFactory create(
      Provider<OkHttpClient> okHttpClientProvider) {
    return new NetworkModule_ProvideQuickChatApiFactory(okHttpClientProvider);
  }

  public static QuickChatApi provideQuickChatApi(OkHttpClient okHttpClient) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideQuickChatApi(okHttpClient));
  }
}
