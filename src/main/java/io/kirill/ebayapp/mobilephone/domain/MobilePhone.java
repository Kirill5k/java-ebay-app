package io.kirill.ebayapp.mobilephone.domain;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

@Value
@Document
@Builder
@With
@RequiredArgsConstructor
public class MobilePhone {
  @Id
  private final String id;
  private final String make;
  private final String model;
  private final String colour;
  private final String manufacturerColour;
  private final String storageCapacity;
  private final String network;
  private final String condition;
  private final String mpn;
  private final ListingDetails listingDetails;

  public String fullName() {
    return Stream.of(make, model, storageCapacity, colour, network)
        .filter(Objects::nonNull)
        .collect(joining(" "));
  }

  public boolean hasMinAmountOfDetails() {
    return Stream.of(make, model, network).noneMatch(Objects::isNull);
  }

  public boolean isProfitableToResell(int expectedMarginPercentage) {
    return listingDetails.getResellPrice() != null && listingDetails.isProfitableToResell(expectedMarginPercentage);
  }

  public boolean isInWorkingCondition() {
    return !condition.equals("Faulty");
  }

  public MobilePhone withResellPrice(BigDecimal resellPrice) {
    return withListingDetails(listingDetails.withResellPrice(resellPrice));
  }
}
