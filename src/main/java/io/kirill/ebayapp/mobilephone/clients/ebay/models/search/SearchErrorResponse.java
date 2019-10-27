package io.kirill.ebayapp.mobilephone.clients.ebay.models.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.List;

@Value
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchErrorResponse {
  private final List<SearchError> errors;
}
