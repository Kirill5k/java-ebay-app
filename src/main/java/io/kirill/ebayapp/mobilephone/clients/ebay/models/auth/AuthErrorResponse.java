package io.kirill.ebayapp.mobilephone.clients.ebay.models.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor
public class AuthErrorResponse {
  private final String error;
  @JsonProperty("error_description")
  private final String description;
}
