package io.kirill.ebayapp.ebay.models.item;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.kirill.ebayapp.ebay.models.Price;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.List;

@Value
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Item {
  private final String itemId;
  private final String title;
  private final String shortDescription;
  private final String description;
  private final Price price;
  private final String categoryPath;
  private final String condition;
  private final String itemWebUrl;
  private final List<ItemProperty> localizedAspects;
}
