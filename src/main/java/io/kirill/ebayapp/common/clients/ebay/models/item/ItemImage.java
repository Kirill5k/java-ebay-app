package io.kirill.ebayapp.common.clients.ebay.models.item;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemImage {
  private final String imageUrl;
}
