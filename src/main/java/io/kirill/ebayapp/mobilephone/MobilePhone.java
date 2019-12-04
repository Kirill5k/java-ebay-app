package io.kirill.ebayapp.mobilephone;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
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
  private final String listingTitle;
  private final String listingDescription;
  private final Instant datePosted;
  private final String url;
  private final String image;
  private final BigDecimal price;
  private final BigDecimal resellPrice;
  private final String mpn;

  public String fullName() {
    return Stream.of(make, model, storageCapacity, colour, network)
        .filter(Objects::nonNull)
        .collect(joining(" "));
  }

  public boolean hasMinAmountOfDetails() {
    return Stream.of(make, model, network).noneMatch(Objects::isNull);
  }

  public boolean isProfitableToResell(int expectedMarginPercentage) {
    return (resellPrice.doubleValue() * 100 / price.doubleValue() - 100) > expectedMarginPercentage;
  }

  public boolean isInWorkingCondition() {
    return !condition.equals("Faulty");
  }
}
