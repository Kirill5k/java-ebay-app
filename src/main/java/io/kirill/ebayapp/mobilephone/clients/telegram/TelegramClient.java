package io.kirill.ebayapp.mobilephone.clients.telegram;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import io.kirill.ebayapp.common.configs.CexConfig;
import io.kirill.ebayapp.common.configs.TelegramConfig;
import io.kirill.ebayapp.mobilephone.MobilePhone;
import io.kirill.ebayapp.mobilephone.clients.cex.exceptions.CexSearchError;
import io.kirill.ebayapp.mobilephone.clients.cex.models.SearchData;
import io.kirill.ebayapp.mobilephone.clients.cex.models.SearchError;
import io.kirill.ebayapp.mobilephone.clients.cex.models.SearchErrorResponse;
import io.kirill.ebayapp.mobilephone.clients.cex.models.SearchErrorResponseWrapper;
import io.kirill.ebayapp.mobilephone.clients.cex.models.SearchResponseWrapper;
import io.kirill.ebayapp.mobilephone.clients.cex.models.SearchResult;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
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
