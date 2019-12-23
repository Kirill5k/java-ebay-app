package io.kirill.ebayapp.common.clients.ebay;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import io.kirill.ebayapp.common.clients.ebay.models.AuthToken;
import io.kirill.ebayapp.common.configs.EbayConfig;
import java.time.Instant;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.util.Base64Utils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

class EbayAuthClientTest {
  private static final String EBAY_URI = "/ebay";

  final MockWebServer mockWebServer = new MockWebServer();

  EbayAuthClient ebayAuthClient;

  @BeforeEach
  void setup() {
    var baseUri = mockWebServer.url(EBAY_URI).toString();
    var creds = new EbayConfig.Credentials("client-id", "client-secret");
    var creds2 = new EbayConfig.Credentials("client-id-2", "client-secret-2");
    var ebayConfig = new EbayConfig(baseUri, "/auth", "/search", "/item", new EbayConfig.Credentials[]{creds, creds2});
    ebayAuthClient = new EbayAuthClient(WebClient.builder(), ebayConfig);
  }

  @AfterEach
  void tearDown() throws Exception {
    mockWebServer.shutdown();
  }

  @Test
  void accessToken() throws Exception {
    mockWebServer.enqueue(new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\": \"secret-token\", \"expires_in\": 7200}")
        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE));

    var authToken = ebayAuthClient.accessToken();

    StepVerifier
        .create(authToken)
        .expectNextMatches(token -> token.equals("secret-token"))
        .verifyComplete();

    var recordedRequest = mockWebServer.takeRequest();
    assertThat(recordedRequest.getHeader(AUTHORIZATION)).isEqualTo(String.format("Basic %s", Base64Utils.encodeToString("client-id:client-secret".getBytes())));
    assertThat(recordedRequest.getHeader(CONTENT_TYPE)).startsWith(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
    assertThat(recordedRequest.getPath()).isEqualTo("/ebay/auth");
    assertThat(recordedRequest.getMethod()).isEqualTo("POST");
    assertThat(recordedRequest.getBody().readUtf8()).isEqualTo("scope=https%3A%2F%2Fapi.ebay.com%2Foauth%2Fapi_scope&grant_type=client_credentials");
  }

  @Test
  void accessTokenWhenError() throws Exception {
    mockWebServer.enqueue(new MockResponse()
        .setResponseCode(400)
        .setBody("{\"error\": \"unsupported_grant_type\", \"error_description\": \"error sending request\"}")
        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE));

    var authToken = ebayAuthClient.accessToken();

    StepVerifier
        .create(authToken)
        .verifyErrorMatches(error -> error.getMessage().equals("error authenticating with ebay: error sending request"));
  }

  @Test
  void accessTokenWhenAuthenticated() throws Exception {
    ebayAuthClient.authToken = new AuthToken("valid-token", Instant.now().plusSeconds(10L));

    var authToken = ebayAuthClient.accessToken();

    StepVerifier
        .create(authToken)
        .expectNextMatches(token -> token.equals("valid-token"))
        .verifyComplete();

    assertThat(mockWebServer.getRequestCount()).isZero();
  }

  @Test
  void accessTokenWhenPreviousTokenHasExpired() {
    ebayAuthClient.authToken = new AuthToken("old-token", Instant.now().minusSeconds(10L));

    mockWebServer.enqueue(new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\": \"new-token\", \"expires_in\": 7200}")
        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE));

    var authToken = ebayAuthClient.accessToken();

    StepVerifier
        .create(authToken)
        .expectNextMatches(token -> token.equals("new-token"))
        .verifyComplete();
  }

  @Test
  void accessTokenWithDifferentAccount() throws Exception {
    mockWebServer.enqueue(new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\": \"secret-token\", \"expires_in\": 7200}")
        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE));

    ebayAuthClient.switchAccount();
    var authToken = ebayAuthClient.accessToken();

    StepVerifier
        .create(authToken)
        .expectNextMatches(token -> token.equals("secret-token"))
        .verifyComplete();

    var recordedRequest = mockWebServer.takeRequest();
    assertThat(recordedRequest.getHeader(AUTHORIZATION)).isEqualTo(String.format("Basic %s", Base64Utils.encodeToString("client-id-2:client-secret-2".getBytes())));
    assertThat(recordedRequest.getHeader(CONTENT_TYPE)).startsWith(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
    assertThat(recordedRequest.getPath()).isEqualTo("/ebay/auth");
    assertThat(recordedRequest.getMethod()).isEqualTo("POST");
    assertThat(recordedRequest.getBody().readUtf8()).isEqualTo("scope=https%3A%2F%2Fapi.ebay.com%2Foauth%2Fapi_scope&grant_type=client_credentials");
  }

  @Test
  void accessTokenWithOriginalAccount() throws Exception {
    mockWebServer.enqueue(new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\": \"secret-token\", \"expires_in\": 7200}")
        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE));

    ebayAuthClient.switchAccount();
    ebayAuthClient.switchAccount();
    var authToken = ebayAuthClient.accessToken();

    StepVerifier
        .create(authToken)
        .expectNextMatches(token -> token.equals("secret-token"))
        .verifyComplete();

    var recordedRequest = mockWebServer.takeRequest();
    assertThat(recordedRequest.getHeader(AUTHORIZATION)).isEqualTo(String.format("Basic %s", Base64Utils.encodeToString("client-id:client-secret".getBytes())));
    assertThat(recordedRequest.getHeader(CONTENT_TYPE)).startsWith(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
    assertThat(recordedRequest.getPath()).isEqualTo("/ebay/auth");
    assertThat(recordedRequest.getMethod()).isEqualTo("POST");
    assertThat(recordedRequest.getBody().readUtf8()).isEqualTo("scope=https%3A%2F%2Fapi.ebay.com%2Foauth%2Fapi_scope&grant_type=client_credentials");
  }
}