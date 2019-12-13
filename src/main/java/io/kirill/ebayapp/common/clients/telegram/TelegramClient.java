package io.kirill.ebayapp.common.clients.telegram;

import io.kirill.ebayapp.common.configs.TelegramConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class TelegramClient {
  private final WebClient webClient;
  private final String channel;

  TelegramClient(WebClient.Builder webClientBuilder, TelegramConfig telegramConfig) {
    this.channel = telegramConfig.getChannelId();
    this.webClient = webClientBuilder
        .baseUrl(telegramConfig.getBaseUrl() + telegramConfig.getMessagePath())
        .build();
  }

  public Mono<Void> sendMessageToMainChannel(String message) {
    return sendMessage(channel, message);
  }

  private Mono<Void> sendMessage(String channelId, String message) {
    log.info(message);
    return webClient
        .get()
        .uri(builder -> builder.queryParam("chat_id", channelId).queryParam("text", message).build())
        .retrieve()
        .bodyToMono(String.class)
        .flatMap($ -> Mono.empty());
  }
}
