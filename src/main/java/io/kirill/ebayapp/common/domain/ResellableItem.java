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
    return ofNullable(getResellPrice())
        .map(ResellPrice::getExchange)
        .map(exchangePrice -> (exchangePrice.doubleValue() * 100 / originalPrice().doubleValue() - 100) > expectedMarginPercentage)
        .orElse(false);
  }

  default String goodDealMessage() {
    var details = getListingDetails();
    var type = details.getType();
    var template = type.equals("BUY_IT_NOW") ? "just listed \"%s\": ebay: £%s, cex: £%s %s" : "about to end soon \"%s\": ebay: £%s, cex: £%s %s";
    return String.format(template, searchQuery(), details.getPrice(), getResellPrice().getExchange(), details.getUrl());
  }
}
