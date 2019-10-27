package io.kirill.ebayapp.mobilephone.clients.ebay.models.item;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.kirill.ebayapp.mobilephone.clients.ebay.models.Price;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.List;

@Value
@Builder
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Item {
  private final String itemId;
  private final String title;
  private final String shortDescription;
  private final String description;
  private final String mpn;
  private final String color;
  private final String brand;
  private final Price price;
  private final String categoryPath;
  private final String condition;
  private final String itemWebUrl;
  private final List<ItemProperty> localizedAspects;
}
