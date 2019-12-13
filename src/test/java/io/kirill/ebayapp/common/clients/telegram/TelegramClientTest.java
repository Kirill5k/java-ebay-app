package io.kirill.ebayapp.common.clients.telegram;

import io.kirill.ebayapp.common.configs.TelegramConfig;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

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
}