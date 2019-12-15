package io.kirill.ebayapp.common.domain;

import java.math.BigDecimal;

public interface PriceQuery<T> {
  String queryString();
  BigDecimal originalPrice();
  boolean isSearchable();
  T withResellPrice(BigDecimal resellPrice);
  boolean isProfitableToResell(int expectedMarginPercentage);
}
