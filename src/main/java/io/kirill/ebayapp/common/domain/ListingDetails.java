package io.kirill.ebayapp.common.domain;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.With;

import java.math.BigDecimal;
import java.time.Instant;

@Value
@With
@Builder
@RequiredArgsConstructor
public class ListingDetails {
  private final String type;
  private final String originalCondition;
  private final String title;
  private final String description;
  private final Instant datePosted;
  private final Instant dateEnded;
  private final String url;
  private final String image;
  private final String seller;
  private final BigDecimal price;
}
