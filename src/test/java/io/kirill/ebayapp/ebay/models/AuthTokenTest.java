package io.kirill.ebayapp.ebay.models;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class AuthTokenTest {

  @Test
  void isValid() {
    var expiresIn = 1000L;

    var validToken = new AuthToken("token", expiresIn);
    assertThat(validToken.isValid()).isTrue();

    var expiredToken = new AuthToken("token", Instant.now().minusSeconds(expiresIn).minusSeconds(2L));
    assertThat(expiredToken.isValid()).isFalse();
  }
}