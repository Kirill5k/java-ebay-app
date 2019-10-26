package io.kirill.ebayapp.ebay;

import io.kirill.ebayapp.configs.EbayConfig;
import io.kirill.ebayapp.ebay.models.AuthResponse;
import io.kirill.ebayapp.ebay.models.AuthToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.web.reactive.function.BodyInserters.fromFormData;

@Component
class EbayClient {
  private static final String GRANT_FIELD = "grant_type";
  private static final String BASIC_GRANT = "client_credentials";
  private static final String SCOPE_FIELD = "scope";
  private static final String BASIC_API_SCOPE = "https://api.ebay.com/oauth/api_scope";

  private final EbayConfig ebayConfig;
  private final WebClient webClient;

  EbayClient(WebClient.Builder webClientBuilder, EbayConfig ebayConfig) {
    this.ebayConfig = ebayConfig;
    this.webClient = webClientBuilder
        .baseUrl(ebayConfig.getBaseUrl())
        .build();
  }

  Mono<AuthToken> authenticate() {
    return webClient
        .post()
        .uri(ebayConfig.getAuthPath())
        .headers(headers -> headers.setBasicAuth(ebayConfig.getClientId(), ebayConfig.getClientSecret()))
        .headers(headers -> headers.setContentType(APPLICATION_FORM_URLENCODED))
        .body(fromFormData(SCOPE_FIELD, BASIC_API_SCOPE).with(GRANT_FIELD, BASIC_GRANT))
        .retrieve()
        .bodyToMono(AuthResponse.class)
        .map(authResponse -> new AuthToken(authResponse.getAccessToken(), authResponse.getExpiresIn()));
  }
}
