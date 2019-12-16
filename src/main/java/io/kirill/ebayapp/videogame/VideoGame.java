package io.kirill.ebayapp.videogame;

import io.kirill.ebayapp.common.domain.ListingDetails;
import io.kirill.ebayapp.common.domain.ResellPrice;
import io.kirill.ebayapp.common.domain.ResellableItem;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.With;

@Value
@With
@Builder
@RequiredArgsConstructor
public class VideoGame implements ResellableItem<VideoGame> {
  private final String name;
  private final String platform;
  private final String genre;
  private final String releaseYear;
  private final ListingDetails listingDetails;
  private final ResellPrice resellPrice;

  @Override
  public String searchQuery() {
    return String.format("%s %s", name, platform);
  }

  @Override
  public boolean isSearchable() {
    return name != null && platform != null;
  }
}
