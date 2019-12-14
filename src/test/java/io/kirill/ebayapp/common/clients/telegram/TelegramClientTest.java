package io.kirill.ebayapp.common.clients.telegram;

import io.kirill.ebayapp.common.configs.TelegramConfig;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class TelegramClientTest {
  private static final String TELEGRAM_URI = "/telegram";

  final MockWebServer mockWebServer = new MockWebServer();

  TelegramClient telegramClient;

  @BeforeEach
  void setup() {
    var baseUri = mockWebServer.url(TELEGRAM_URI).toString();
    var telegramConfig = new TelegramConfig(baseUri, "message-path", "main-channel-id");
    telegramClient = new TelegramClient(WebClient.builder(), telegramConfig);
  }

  @AfterEach
  void tearDown() throws Exception {
    mockWebServer.shutdown();
  }

  @Test
  void sendMessageToMainChannel() throws Exception {
    mockWebServer.enqueue(new MockResponse()
        .setResponseCode(200)
        .setBody("{}")
        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE));

    var result = telegramClient.sendMessageToMainChannel("Hello, World!");

    StepVerifier.create(result).verifyComplete();

    var recordedRequest = mockWebServer.takeRequest();
    assertThat(recordedRequest.getPath()).isEqualTo("/telegrammessage-path?chat_id=main-channel-id&text=Hello,%20World!");
    assertThat(recordedRequest.getMethod()).isEqualTo("GET");
  }

  @Test
  void sendMessageToMainChannelWhenError() throws Exception {
    mockWebServer.enqueue(new MockResponse()
        .setResponseCode(400)
        .setBody("{\"message\": \"error\"}")
        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE));

    var result = telegramClient.sendMessageToMainChannel("Hello, World!");

    StepVerifier
        .create(result)
        .verifyErrorMatches(e -> e.getMessage().equals("error sending message to telegram: {\"message\": \"error\"}"));

    var recordedRequest = mockWebServer.takeRequest();
    assertThat(recordedRequest.getPath()).isEqualTo("/telegrammessage-path?chat_id=main-channel-id&text=Hello,%20World!");
    assertThat(recordedRequest.getMethod()).isEqualTo("GET");
  }
}