package io.kirill.ebayapp.mobilephone.clients.ebay.models.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.kirill.ebayapp.mobilephone.clients.ebay.models.Price;
import io.kirill.ebayapp.mobilephone.clients.ebay.models.Seller;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResult {
  private final String itemId;
  private final String title;
  private final String itemWebUrl;
  private final Seller seller;
  private final Price price;
}
