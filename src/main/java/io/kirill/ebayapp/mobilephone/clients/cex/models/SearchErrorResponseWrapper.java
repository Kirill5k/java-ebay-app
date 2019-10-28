package io.kirill.ebayapp.mobilephone.clients.cex.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchErrorResponseWrapper {
  private final SearchErrorResponse response;
}
