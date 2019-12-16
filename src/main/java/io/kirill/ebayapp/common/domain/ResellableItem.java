package io.kirill.ebayapp.common.domain;

import java.math.BigDecimal;

import static java.util.Optional.ofNullable;

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

  default String goodDealMessage() {
    var details = getListingDetails();
    return String.format("good deal on \"%s\": ebay: £%s, cex: £%s %s", searchQuery(), details.getPrice(), getResellPrice().getExchange(), details.getUrl());
  }
}
