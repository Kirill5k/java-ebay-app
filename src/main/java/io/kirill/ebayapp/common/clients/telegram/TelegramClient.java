package io.kirill.ebayapp.common.clients.telegram;

import io.kirill.ebayapp.common.configs.TelegramConfig;
import io.kirill.ebayapp.mobilephone.MobilePhone;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class TelegramClient {
  private final static String MESSAGE_TEMPLATE = "good deal on \"%s\": asking price %s, cex price %s %s";

  private final WebClient webClient;
  private final String channel;

  TelegramClient(WebClient.Builder webClientBuilder, TelegramConfig telegramConfig) {
    this.channel = telegramConfig.getChannelId();
    this.webClient = webClientBuilder
        .baseUrl(telegramConfig.getBaseUrl() + telegramConfig.getMessagePath())
        .build();
  }

  public Mono<Void> informAboutThePhone(MobilePhone phone) {
    var message = String.format(MESSAGE_TEMPLATE, phone.fullName(), phone.getPrice(), phone.getResellPrice(), phone.getUrl());
    log.info(message);
    return webClient
        .get()
        .uri(builder -> builder.queryParam("chat_id", channel).queryParam("text", message).build())
        .retrieve()
        .bodyToMono(String.class)
        .flatMap($ -> Mono.empty());
  }
}
