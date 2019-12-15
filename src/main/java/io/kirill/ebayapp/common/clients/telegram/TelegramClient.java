package io.kirill.ebayapp.common.clients.telegram;

import io.kirill.ebayapp.common.clients.telegram.exceptions.TelegramSendingError;
import io.kirill.ebayapp.common.configs.TelegramConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Slf4j
@Component
public class TelegramClient {
  private final WebClient webClient;
  private final String mainChannel;
  private final String secondaryChannel;

  TelegramClient(WebClient.Builder webClientBuilder, TelegramConfig telegramConfig) {
    this.mainChannel = telegramConfig.getMainChannelId();
    this.secondaryChannel = telegramConfig.getSecondaryChannelId();
    this.webClient = webClientBuilder
        .baseUrl(telegramConfig.getBaseUrl() + telegramConfig.getMessagePath())
        .build();
  }

  public Mono<Void> sendMessageToMainChannel(String message) {
    return sendMessage(mainChannel, message);
  }

  public Mono<Void> sendMessageToSecondaryChannel(String message) {
    return sendMessage(secondaryChannel, message);
  }

  private Mono<Void> sendMessage(String channelId, String message) {
    log.info(message);
    return webClient
        .get()
        .uri(builder -> builder.queryParam("chat_id", channelId).queryParam("text", message).build())
        .retrieve()
        .onStatus(HttpStatus::isError, mapError())
        .bodyToMono(String.class)
        .flatMap($ -> Mono.empty());
  }

  private Function<ClientResponse, Mono<? extends Throwable>> mapError() {
    return res -> res.bodyToMono(String.class)
        .flatMap(resBody -> Mono.error(new TelegramSendingError(res.statusCode(), resBody)));
  }
}
