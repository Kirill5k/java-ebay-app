package io.kirill.ebayapp.common.domain;

import static java.util.Optional.ofNullable;

import java.math.BigDecimal;
import java.time.Instant;

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
    var profitPercentage = (int)(getResellPrice().getExchange().doubleValue() * 100 / originalPrice().doubleValue() - 100);
    var priceTemplate = String.format("ebay: £%s, cex: £%s(%s%%)/£%s ", details.getPrice(), getResellPrice().getExchange(), profitPercentage, getResellPrice().getCash());
    var isAboutToEnd = details.getDateEnded() != null && details.getDateEnded().minusSeconds(60 * 10).isBefore(Instant.now());
    var template = isAboutToEnd ? "ENDING \"%s\"" : "NEW \"%s\"";
    return String.format(template, searchQuery()) + " - " + priceTemplate + getListingDetails().getUrl();
  }
}
