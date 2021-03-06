package io.kirill.ebayapp.mobilephone;

import io.kirill.ebayapp.common.domain.ListingDetails;
import io.kirill.ebayapp.common.domain.ResellPrice;
import io.kirill.ebayapp.common.domain.ResellableItem;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

@Value
@Document
@Builder
@With
@RequiredArgsConstructor
public class MobilePhone implements ResellableItem<MobilePhone> {
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
  private final ResellPrice resellPrice;

  public boolean isInWorkingCondition() {
    return !condition.equals("Faulty");
  }

  @Override
  public String searchQuery() {
    return Stream.of(make, model, storageCapacity, colour, network)
        .filter(Objects::nonNull)
        .collect(joining(" "));
  }

  @Override
  public boolean isSearchable() {
    return Stream.of(make, model, network).noneMatch(Objects::isNull);
  }
}
