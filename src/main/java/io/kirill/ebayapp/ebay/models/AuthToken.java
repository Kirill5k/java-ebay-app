package io.kirill.ebayapp.ebay.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class AuthToken {
  @Getter
  private final String token;
  private final Instant expiryTime;

  public AuthToken(String token, Long expiresIn) {
    this.token = token;
    this.expiryTime = Instant.now().plusSeconds(expiresIn);
  }

  public boolean isValid() {
    return expiryTime.isAfter(Instant.now());
  }
}
