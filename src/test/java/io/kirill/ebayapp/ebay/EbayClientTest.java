package io.kirill.ebayapp.ebay;

import io.kirill.ebayapp.configs.EbayConfig;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.util.Base64Utils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class EbayClientTest {
  private static final String EBAY_URI = "/ebay";

  final MockWebServer mockWebServer = new MockWebServer();

  EbayClient ebayClient;

  @BeforeEach
  void setup() {
    var baseUri = mockWebServer.url(EBAY_URI).toString();
    var ebayConfig = new EbayConfig("client-id", "client-secret", baseUri, "/auth", "/search", "/item");
    ebayClient = new EbayClient(WebClient.builder(), ebayConfig);
  }

  @AfterEach
  void tearDown() throws Exception {
    mockWebServer.shutdown();
  }

  @Test
  void authenticate() throws Exception {
    mockWebServer.enqueue(new MockResponse()
        .setResponseCode(200)
        .setBody("{\"access_token\": \"secret-token\", \"expires_in\": 7200}")
        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE));

    var authToken = ebayClient.authenticate();

    StepVerifier
        .create(authToken)
        .expectNextMatches(token -> token.getToken().equals("secret-token") && !token.hasExpired())
        .verifyComplete();

    var recordedRequest = mockWebServer.takeRequest();
    assertThat(recordedRequest.getHeader(AUTHORIZATION)).isEqualTo(String.format("Basic %s", Base64Utils.encodeToString("client-id:client-secret".getBytes())));
    assertThat(recordedRequest.getHeader(CONTENT_TYPE)).startsWith(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
    assertThat(recordedRequest.getPath()).isEqualTo("/ebay/auth");
    assertThat(recordedRequest.getMethod()).isEqualTo("POST");
  }
}