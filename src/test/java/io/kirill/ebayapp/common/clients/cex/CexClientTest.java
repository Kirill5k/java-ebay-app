package io.kirill.ebayapp.common.clients.cex;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import io.kirill.ebayapp.common.configs.CexConfig;
import io.kirill.ebayapp.mobilephone.MobilePhone;
import io.kirill.ebayapp.mobilephone.MobilePhoneBuilder;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

class CexClientTest {
  private static final String CEX_URI = "/cex";

  final MockWebServer mockWebServer = new MockWebServer();

  CexClient cexClient;

  MobilePhone iphone6s = MobilePhoneBuilder.iphone6s().build();

  @BeforeEach
  void setup() {
    var baseUri = mockWebServer.url(CEX_URI).toString();
    var cexConfig = new CexConfig(baseUri, "/search");
    cexClient = new CexClient(WebClient.builder(), cexConfig);
  }

  @AfterEach
  void tearDown() throws Exception {
    mockWebServer.shutdown();
  }

  @Test
  void getMinResellPrice() throws Exception {
    mockWebServer.enqueue(new MockResponse()
        .setResponseCode(200)
        .setBody("{\"response\": {\"data\": {\"boxes\": [{\"boxName\": \"box-1\", \"cashPrice\": 10.99, \"exchangePrice\": 12.99}, {\"boxName\": \"box-1\", \"exchangePrice\": 20.0}]}}}")
        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE));

    var averagePrice = cexClient.getMinResellPrice(iphone6s);
    var anotherPrice = cexClient.getMinResellPrice(iphone6s);

      StepVerifier
          .create(averagePrice)
          .expectNextMatches(price -> price.getExchange().doubleValue() == 12 && price.getCash().doubleValue() == 10)
          .verifyComplete();

    var recordedRequest = mockWebServer.takeRequest();
    assertThat(mockWebServer.getRequestCount()).isEqualTo(1);
    assertThat(recordedRequest.getHeader(CONTENT_TYPE)).isEqualTo(APPLICATION_JSON_VALUE);
    assertThat(recordedRequest.getHeader(ACCEPT)).isEqualTo(APPLICATION_JSON_VALUE);
    assertThat(recordedRequest.getPath()).isEqualTo("/cex/search?q=Apple%20Iphone%206s%2016GB%20Space%20Grey%20Unlocked");
    assertThat(recordedRequest.getMethod()).isEqualTo("GET");
  }

  @Test
  void getMinResellPriceWithIncompleteDetails() {
    StepVerifier
        .create(cexClient.getMinResellPrice(iphone6s.withModel(null)))
        .verifyComplete();

    assertThat(mockWebServer.getRequestCount()).isZero();
  }

  @Test
  void getMinResellPriceWhenNoResults() {
    mockWebServer.enqueue(new MockResponse()
        .setResponseCode(200)
        .setBody("{\"response\": {\"data\": {\"results\": []}}}")
        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE));

    var averagePrice = cexClient.getMinResellPrice(iphone6s);

    StepVerifier
        .create(averagePrice)
        .expectNextMatches(price -> price.getExchange() == null && price.getCash() == null)
        .verifyComplete();
  }

  @Test
  void getMinResellPriceWhenReturnsError() throws Exception {
    mockWebServer.enqueue(new MockResponse()
        .setResponseCode(400)
        .setBody("{\"response\": {\"data\": \"\", \"error\": {\"internal_message\": \"error-message\"}}}")
        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE));

    var averagePrice = cexClient.getMinResellPrice(iphone6s);

    StepVerifier
        .create(averagePrice)
        .verifyErrorMessage("error sending search req to cex: error-message");

    var recordedRequest = mockWebServer.takeRequest();
    assertThat(recordedRequest.getPath()).isEqualTo("/cex/search?q=Apple%20Iphone%206s%2016GB%20Space%20Grey%20Unlocked");
    assertThat(recordedRequest.getMethod()).isEqualTo("GET");
  }

  @Test
  void getMinResellPriceWhenReturns429Error() throws Exception {
    mockWebServer.enqueue(new MockResponse()
        .setResponseCode(429)
        .setBody("{\"response\": {\"data\": \"\", \"error\": {\"internal_message\": \"error-message\"}}}")
        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE));

    var averagePrice = cexClient.getMinResellPrice(iphone6s);

    StepVerifier
        .create(averagePrice)
        .verifyErrorMessage("error sending search req to cex: too many requests");

    var recordedRequest = mockWebServer.takeRequest();
    assertThat(recordedRequest.getPath()).isEqualTo("/cex/search?q=Apple%20Iphone%206s%2016GB%20Space%20Grey%20Unlocked");
    assertThat(recordedRequest.getMethod()).isEqualTo("GET");
  }
}