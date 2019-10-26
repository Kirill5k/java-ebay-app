package io.kirill.ebayapp.ebay.models;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class AuthTokenTest {

  @Test
  void hasExpired() {
    var expiresIn = 1000L;

    var validToken = new AuthToken("token", expiresIn);
    assertThat(validToken.hasExpired()).isFalse();

    var expiredToken = new AuthToken("token", expiresIn, LocalDateTime.now().minusSeconds(expiresIn).minusSeconds(1));
    assertThat(expiredToken.hasExpired()).isTrue();
  }
}