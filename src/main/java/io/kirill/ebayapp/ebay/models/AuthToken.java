package io.kirill.ebayapp.ebay.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class AuthToken {
  @Getter
  private final String token;
  private final Long expiresIn;
  private final LocalDateTime creationTime;

  public AuthToken(String token, Long expiresIn) {
    this.token = token;
    this.expiresIn = expiresIn;
    this.creationTime = LocalDateTime.now();
  }

  public boolean hasExpired() {
    return creationTime.plusSeconds(expiresIn).isBefore(LocalDateTime.now());
  }
}
