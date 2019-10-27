package io.kirill.ebayapp.ebay.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchItem {
  private final String itemId;
  private final String title;
  private final String itemWebUrl;
  private final Seller seller;
}
