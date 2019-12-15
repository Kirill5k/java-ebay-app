package io.kirill.ebayapp.common.clients.cex.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResult {
  private final String boxId;
  private final String boxName;
  private final String superCatName;
  private final Integer sellPrice;
  private final Integer cashPrice;
  private final Integer exchangePrice;
}
