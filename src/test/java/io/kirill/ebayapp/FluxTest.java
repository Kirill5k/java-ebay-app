package io.kirill.ebayapp;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class FluxTest {

  @Test
  void filterAllOut() throws Exception {
    var flux = Flux.range(0, 1000).filterWhen(n -> Mono.just(n >= 500 && n < 600));

    StepVerifier
        .create(flux)
        .expectNextCount(100)
        .verifyComplete();
  }
}
