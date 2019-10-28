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
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
  private final String storageCapacity;
  private final String network;
  private final String condition;
  private final String listingTitle;
  private final String listingDescription;
  private final Instant datePosted;
  private final String url;
  private final BigDecimal price;
  private final BigDecimal resellPrice;

  public String fullName() {
    return Stream.of(make, model, storageCapacity, colour, network)
        .filter(Objects::nonNull)
        .collect(Collectors.joining(" "));
  }

  public boolean hasAllDetails() {
    return Stream.of(make, model, storageCapacity, colour, network).noneMatch(Objects::isNull);
  }

  public boolean isProfitableToResell(int expectedMarginPercentage) {
    return (resellPrice.multiply(BigDecimal.valueOf(100)).divide(price).intValue() - 100) > expectedMarginPercentage;
  }
}
