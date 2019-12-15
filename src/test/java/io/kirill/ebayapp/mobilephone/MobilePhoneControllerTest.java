package io.kirill.ebayapp.mobilephone;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;

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
        .getLatest(anyInt());

    client
        .get()
        .uri("/api/mobile-phones")
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentTypeCompatibleWith(APPLICATION_JSON)
        .expectBody()
        .jsonPath("$[0].model").isEqualTo("Iphone 6s");

    verify(mobilePhoneService).getLatest(100);
  }


  @Test
  void feed() {
    doAnswer(inv -> Flux.just(iphone6s, iphone6s, iphone6s))
        .when(mobilePhoneService)
        .feedLatest();

    FluxExchangeResult<MobilePhone> mobiles = client
        .get()
        .uri("/api/mobile-phones/feed")
        .accept(TEXT_EVENT_STREAM)
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentTypeCompatibleWith(TEXT_EVENT_STREAM)
        .returnResult(MobilePhone.class);

    StepVerifier.create(mobiles.getResponseBody())
        .expectNext(iphone6s, iphone6s, iphone6s)
        .thenCancel()
        .verify();
  }
}