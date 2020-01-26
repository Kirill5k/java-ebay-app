package io.kirill.ebayapp.common.domain;

import java.math.BigDecimal;
import java.time.Instant;

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
    var isAboutToEnd = details.getDateEnded() != null && details.getDateEnded().minusSeconds(60 * 10).isBefore(Instant.now());
    var template = isAboutToEnd
        ? "about to end soon \"%s\": ebay: £%s, cex: (exchange £%s, cash £%s) %s"
        : "just listed \"%s\": ebay: £%s, cex: (exchange £%s, cash £%s) %s";
    return String.format(template, searchQuery(), details.getPrice(), getResellPrice().getExchange(), getResellPrice().getCash(), details.getUrl());
  }
}
