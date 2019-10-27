package io.kirill.ebayapp.ebay;

import io.kirill.ebayapp.configs.EbayConfig;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class EbaySearchClientTest {
  private static final String EBAY_URI = "/ebay";

  final MockWebServer mockWebServer = new MockWebServer();

  EbaySearchClient ebaySearchClient;

  String accessToken = "access-token";

  @BeforeEach
  void setup() {
    var baseUri = mockWebServer.url(EBAY_URI).toString();
    var ebayConfig = new EbayConfig("client-id", "client-secret", baseUri, "/auth", "/search", "/item");
    ebaySearchClient = new EbaySearchClient(WebClient.builder(), ebayConfig);
  }

  @AfterEach
  void tearDown() throws Exception {
    mockWebServer.shutdown();
  }

  @Test
  void getAllMobilesPhonesPostedInTheLast15Mins() throws Exception {
    mockWebServer.enqueue(new MockResponse()
        .setResponseCode(200)
        .setBody("{\"itemSummaries\": [{\"itemId\": \"item-1\"}, {\"itemId\": \"item-2\"}]}")
        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE));

    var items = ebaySearchClient.getAllMobilesPhonesPostedInTheLast15Mins(accessToken);

    StepVerifier
        .create(items)
        .expectNextMatches(item -> item.getItemId().equals("item-1"))
        .expectNextMatches(item -> item.getItemId().equals("item-2"))
        .verifyComplete();

    var recordedRequest = mockWebServer.takeRequest();
    assertThat(recordedRequest.getHeader(AUTHORIZATION)).isEqualTo("Bearer access-token");
    assertThat(recordedRequest.getHeader(CONTENT_TYPE)).isEqualTo(APPLICATION_JSON_VALUE);
    assertThat(recordedRequest.getHeader(ACCEPT)).isEqualTo(APPLICATION_JSON_VALUE);
    assertThat(recordedRequest.getHeader("X-EBAY-C-MARKETPLACE-ID")).isEqualTo("EBAY_GB");
    assertThat(recordedRequest.getPath()).startsWith("/ebay/search?category_ids=9355&filter=conditionIds:%257B1000%7C1500%7C2000%7C2500%7C3000%7C4000%7C5000%257D,buyingOptions:%257BFIXED_PRICE%257D,deliveryCountry:GB,price:%5B10..500%5D,priceCurrency:GBP,itemLocationCountry:GB,itemStartDate:%5B");
    assertThat(recordedRequest.getMethod()).isEqualTo("GET");

  }
}