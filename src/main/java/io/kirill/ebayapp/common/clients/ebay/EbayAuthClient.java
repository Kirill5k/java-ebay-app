package io.kirill.ebayapp.common.clients.ebay;

import io.kirill.ebayapp.common.clients.ebay.exceptions.EbayAuthError;
import io.kirill.ebayapp.common.clients.ebay.models.AuthToken;
import io.kirill.ebayapp.common.clients.ebay.models.auth.AuthErrorResponse;
import io.kirill.ebayapp.common.clients.ebay.models.auth.AuthResponse;
import io.kirill.ebayapp.common.configs.EbayConfig;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static java.util.Optional.ofNullable;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.web.reactive.function.BodyInserters.fromFormData;

@Component
public class EbayAuthClient {
  private static final long TOKEN_EXPIRY_OFFSET = 30;

  private static final String GRANT_FIELD = "grant_type";
  private static final String BASIC_GRANT = "client_credentials";
  private static final String SCOPE_FIELD = "scope";
  private static final String BASIC_API_SCOPE = "https://api.ebay.com/oauth/api_scope";

  private final WebClient webClient;

  AuthToken authToken;

  public EbayAuthClient(WebClient.Builder webClientBuilder, EbayConfig ebayConfig) {
    this.webClient = webClientBuilder
        .baseUrl(ebayConfig.getBaseUrl() + ebayConfig.getAuthPath())
        .defaultHeaders(headers -> headers.setBasicAuth(ebayConfig.getClientId(), ebayConfig.getClientSecret()))
        .defaultHeaders(headers -> headers.setContentType(APPLICATION_FORM_URLENCODED))
        .build();
  }

  public Mono<String> accessToken() {
    return ofNullable(authToken)
        .filter(AuthToken::isValid)
        .map(AuthToken::getToken)
        .map(Mono::just)
        .orElseGet(() -> authenticate().doOnNext(token -> authToken = token).map(AuthToken::getToken));
  }

  private Mono<AuthToken> authenticate() {
    return webClient
        .post()
        .body(fromFormData(SCOPE_FIELD, BASIC_API_SCOPE).with(GRANT_FIELD, BASIC_GRANT))
        .retrieve()
        .onStatus(HttpStatus::isError, r -> r.bodyToMono(AuthErrorResponse.class).map(e -> new EbayAuthError(r.statusCode(), e.getDescription())))
        .bodyToMono(AuthResponse.class)
        .map(authResponse -> new AuthToken(authResponse.getAccessToken(), authResponse.getExpiresIn() - TOKEN_EXPIRY_OFFSET));
  }
}