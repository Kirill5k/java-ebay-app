package io.kirill.ebayapp.ebay.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor
public class AuthResponse {
  @JsonProperty("access_token")
  private final String accessToken;
  @JsonProperty("expires_in")
  private final Long expiresIn;
  @JsonProperty("token_type")
  private final String tokenType;
}
