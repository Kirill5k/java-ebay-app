package io.kirill.ebayapp.common.clients.ebay;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import io.kirill.ebayapp.TestUtils;
import io.kirill.ebayapp.common.configs.EbayConfig;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

class EbaySearchClientTest {
  private static final String EBAY_URI = "/ebay";

  final MockWebServer mockWebServer = new MockWebServer();

  EbaySearchClient ebaySearchClient;

  String accessToken = "access-token";

  @BeforeEach
  void setup() {
    var baseUri = mockWebServer.url(EBAY_URI).toString();
    var creds = new EbayConfig.Credentials("client-id", "client-secret");
    var ebayConfig = new EbayConfig(baseUri, "/auth", "/search", "/item", new EbayConfig.Credentials[]{creds});
    ebaySearchClient = new EbaySearchClient(WebClient.builder(), ebayConfig);
  }

  @AfterEach
  void tearDown() throws Exception {
    mockWebServer.shutdown();
  }

  @Test
  void searchForNewestInCategoryFrom() throws Exception {
    mockWebServer.enqueue(new MockResponse()
        .setResponseCode(200)
        .setBody("{\"itemSummaries\": [{\"itemId\": \"item-1\", \"price\": {\"value\": \"99.99\"}}, {\"itemId\": \"item-2\", \"seller\": {\"feedbackPercentage\": \"100.0\"}}]}")
        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE));

    var params = new LinkedMultiValueMap<String, String>();
    params.add("category_ids", "9355");
    params.add("filter", "conditionIds:%257B1000%7C1500%7C2000%7C2500%7C3000%7C4000%7C5000%257D,deliveryCountry:GB,price:%5B39..800%5D&");
    var items = ebaySearchClient.search(accessToken, params);

    StepVerifier
        .create(items)
        .expectNextMatches(item -> item.getItemId().equals("item-1") && item.getPrice().getValue().doubleValue() == 99.99)
        .expectNextMatches(item -> item.getItemId().equals("item-2") && item.getSeller().getFeedbackPercentage() == 100.0)
        .verifyComplete();

    var recordedRequest = mockWebServer.takeRequest();
    assertThat(recordedRequest.getHeader(AUTHORIZATION)).isEqualTo("Bearer access-token");
    assertThat(recordedRequest.getHeader(CONTENT_TYPE)).isEqualTo(APPLICATION_JSON_VALUE);
    assertThat(recordedRequest.getHeader(ACCEPT)).isEqualTo(APPLICATION_JSON_VALUE);
    assertThat(recordedRequest.getHeader("X-EBAY-C-MARKETPLACE-ID")).isEqualTo("EBAY_GB");
    assertThat(recordedRequest.getPath()).isEqualTo("/ebay/search?category_ids=9355&filter=conditionIds:%25257B1000%257C1500%257C2000%257C2500%257C3000%257C4000%257C5000%25257D,deliveryCountry:GB,price:%255B39..800%255D%26");
    assertThat(recordedRequest.getMethod()).isEqualTo("GET");
  }

  @Test
  void searchForNewestInCategoryFromWhenNoResult() throws Exception {
    mockWebServer.enqueue(new MockResponse()
        .setResponseCode(200)
        .setBody("{\"itemSummaries\": null}")
        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE));

    var filter = "conditionIds:%257B1000%7C1500%7C2000%7C2500%7C3000%7C4000%7C5000%257D,deliveryCountry:GB,price:%5B39..800%5D";
    var items = ebaySearchClient.search(accessToken, new LinkedMultiValueMap<String, String>());

    StepVerifier
        .create(items)
        .verifyComplete();
  }

  @Test
  void searchForNewestInCategoryFromWhenError() throws Exception {
    mockWebServer.enqueue(new MockResponse()
        .setResponseCode(400)
        .setBody("{\"errors\": [{\"message\": \"error from ebay\"}]}")
        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE));

    var items = ebaySearchClient.search(accessToken, new LinkedMultiValueMap<String, String>());

    StepVerifier
        .create(items)
        .verifyErrorMatches(e -> e.getMessage().equals("error sending search req to ebay: error from ebay"));
  }

  @Test
  void getItem() throws Exception {
    mockWebServer.enqueue(new MockResponse()
        .setResponseCode(200)
        .setBody(TestUtils.getFileContent("classpath:ebay-item.json"))
        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE));

    var item = ebaySearchClient.getItem(accessToken, "item-1");

    StepVerifier
        .create(item)
        .expectNextMatches(i -> i.getItemId().equals("v1|264565980510|0"))
        .verifyComplete();

    var recordedRequest = mockWebServer.takeRequest();
    assertThat(recordedRequest.getHeader(AUTHORIZATION)).isEqualTo("Bearer access-token");
    assertThat(recordedRequest.getHeader(CONTENT_TYPE)).isEqualTo(APPLICATION_JSON_VALUE);
    assertThat(recordedRequest.getHeader(ACCEPT)).isEqualTo(APPLICATION_JSON_VALUE);
    assertThat(recordedRequest.getHeader("X-EBAY-C-MARKETPLACE-ID")).isEqualTo("EBAY_GB");
    assertThat(recordedRequest.getPath()).isEqualTo("/ebay/item/item-1");
    assertThat(recordedRequest.getMethod()).isEqualTo("GET");
  }

  @Test
  void getItemWhen404Error() throws Exception {
    mockWebServer.enqueue(new MockResponse()
        .setResponseCode(404)
        .setBody("{\"errors\": [{\"message\": \"item not found\"}]}")
        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE));

    var item = ebaySearchClient.getItem(accessToken, "item-1");

    StepVerifier
        .create(item)
        .verifyComplete();
  }
}