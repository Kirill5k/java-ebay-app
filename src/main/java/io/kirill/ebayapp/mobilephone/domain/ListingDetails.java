package io.kirill.ebayapp.mobilephone.domain;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.time.Instant;

@Value
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
}
