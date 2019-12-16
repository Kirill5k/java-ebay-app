package io.kirill.ebayapp.common.domain;

import java.math.BigDecimal;

public interface ResellableItem<T> {
  String searchQuery();
  boolean isSearchable();
  ListingDetails getListingDetails();
  T withListingDetails(ListingDetails listingDetails);

  default BigDecimal originalPrice() {
    return getListingDetails().getPrice();
  }

  default T withResellPrice(BigDecimal resellPrice) {
    return withListingDetails(getListingDetails().withResellPrice(resellPrice));
  }

  default boolean isProfitableToResell(int expectedMarginPercentage) {
    return getListingDetails().getResellPrice() != null && getListingDetails().isProfitableToResell(expectedMarginPercentage);
  }
}
