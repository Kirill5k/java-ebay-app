package io.kirill.ebayapp.common.domain;

import static java.util.Optional.ofNullable;

import java.math.BigDecimal;

public interface ResellableItem<T> {
  String searchQuery();
  boolean isSearchable();
  ListingDetails getListingDetails();
  ResellPrice getResellPrice();
  T withResellPrice(ResellPrice resellPrice);

  default BigDecimal originalPrice() {
    return getListingDetails().getPrice();
  }

  default boolean isProfitableToResell(int expectedMarginPercentage) {
    return ofNullable(getResellPrice()).map(ResellPrice::getExchange)
        .map(exchangePrice -> (exchangePrice.doubleValue() * 100 / originalPrice().doubleValue() - 100) > expectedMarginPercentage)
        .orElse(false);
  }
}
