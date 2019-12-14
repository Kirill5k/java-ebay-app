package io.kirill.ebayapp.common.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kirill.ebayapp.mobilephone.domain.MobilePhone;
import io.kirill.ebayapp.mobilephone.MobilePhoneService;
import io.vavr.CheckedFunction1;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.Map;

@Configuration
public class WebSocketConfig {

  @Bean
  WebSocketHandler webSocketHandler(MobilePhoneService service, ObjectMapper mapper) {
    CheckedFunction1<MobilePhone, String> phoneToJson = mapper::writeValueAsString;
    return session -> session.send(service.feedLatest().map(phoneToJson.unchecked()).map(session::textMessage));
  }

  @Bean
  SimpleUrlHandlerMapping simpleUrlHandlerMapping(WebSocketHandler webSocketHandler) {
    return new SimpleUrlHandlerMapping(Map.of("/ws/mobile-phones/feed", webSocketHandler), 10);
  }

  @Bean
  WebSocketHandlerAdapter webSocketHandlerAdapter() {
    return new WebSocketHandlerAdapter();
  }
}
