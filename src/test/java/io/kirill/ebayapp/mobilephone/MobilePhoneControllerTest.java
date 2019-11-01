package io.kirill.ebayapp.mobilephone;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

@WebFluxTest(MobilePhoneController.class)
class MobilePhoneControllerTest {

  @Autowired
  WebTestClient client;

  @MockBean
  MobilePhoneService mobilePhoneService;

  MobilePhone iphone6s = MobilePhoneBuilder.iphone6s().build();

  @Test
  void getAll() {
    doAnswer(inv -> Flux.just(iphone6s, iphone6s, iphone6s))
        .when(mobilePhoneService)
        .getAll(anyInt());

    client
        .get()
        .uri("/api/mobile-phones")
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentTypeCompatibleWith(APPLICATION_JSON)
        .expectBody()
        .jsonPath("$[0].model").isEqualTo("Iphone 6s");

    verify(mobilePhoneService).getAll(100);
  }
}