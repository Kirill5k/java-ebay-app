package io.kirill.ebayapp.ebay.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.List;

@Value
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResponse {

  private final List<SearchItem> itemSummaries;
}
