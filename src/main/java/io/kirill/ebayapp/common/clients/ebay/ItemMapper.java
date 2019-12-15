package io.kirill.ebayapp.common.clients.ebay;

import io.kirill.ebayapp.common.clients.ebay.models.Price;
import io.kirill.ebayapp.common.clients.ebay.models.item.Item;
import io.kirill.ebayapp.common.clients.ebay.models.item.ItemImage;
import io.kirill.ebayapp.common.clients.ebay.models.item.ItemProperty;
import io.kirill.ebayapp.common.domain.ListingDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

public interface ItemMapper<T> {

  T map(Item item);

  default Map<String, String> mapProperties(Item item) {
    return ofNullable(item.getLocalizedAspects())
        .stream().flatMap(Collection::stream)
        .collect(toMap(ItemProperty::getName, ItemProperty::getValue));
  }

  default ListingDetails mapDetails(Item item) {
    return ListingDetails.builder()
        .type(ofNullable(item.getBuyingOptions()).filter(opts -> opts.contains("FIXED_PRICE")).map($ -> "BUY_IT_NOW").orElse("AUCTION"))
        .originalCondition(item.getCondition())
        .title(item.getTitle())
        .dateEnded(item.getItemEndDate())
        .datePosted(Instant.now())
        .description(item.getShortDescription())
        .image(ofNullable(item.getImage()).map(ItemImage::getImageUrl).orElse(null))
        .url(item.getItemWebUrl())
        .price(ofNullable(item.getPrice()).map(Price::getValue).orElse(null))
        .build();
  }
}