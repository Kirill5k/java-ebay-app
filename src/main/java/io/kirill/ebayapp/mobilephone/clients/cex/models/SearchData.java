package io.kirill.ebayapp.mobilephone.clients.cex.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.List;

@Value
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchData {
  private final List<SearchResult> boxes;
}
