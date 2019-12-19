package io.kirill.ebayapp.common.clients.ebay.models.item;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.kirill.ebayapp.common.clients.ebay.models.Price;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.time.Instant;
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
  private final Price currentBidPrice;
  private final String categoryPath;
  private final String condition;
  private final String itemWebUrl;
  private final List<ItemProperty> localizedAspects;
  private final ItemImage image;
  private final ItemSeller seller;
  private final Instant itemEndDate;
  private final List<String> buyingOptions;
}
