package io.kirill.ebayapp.common.clients.ebay.models.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.kirill.ebayapp.common.clients.ebay.models.Price;
import io.kirill.ebayapp.common.clients.ebay.models.Seller;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@Builder
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResult {
  private final String itemId;
  private final String title;
  private final String itemWebUrl;
  private final Seller seller;
  private final Price price;
  private final Price currentBidPrice;
  private final Integer bidCount;
}
