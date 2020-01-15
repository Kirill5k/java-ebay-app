package io.kirill.ebayapp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class AppMaintainer {
  private final WebClient webClient;

  public AppMaintainer(WebClient.Builder webClientBuilder) {
    this.webClient = webClientBuilder
        .baseUrl("https://java-ebay-app.herokuapp.com/")
        .build();
  }

  @Scheduled(initialDelay = 600000, fixedDelay = 1800000)
  public void keepAwake() {
    webClient
        .get()
        .retrieve()
        .bodyToMono(String.class)
        .onErrorContinue((e, $) -> log.error("error sending request to itself: {}", e.getMessage()))
        .subscribe();
  }
}
