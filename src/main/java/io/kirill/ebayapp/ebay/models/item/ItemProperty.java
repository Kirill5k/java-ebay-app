package io.kirill.ebayapp.ebay.models.item;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemProperty {
  private final String type;
  private final String name;
  private final String value;
}
