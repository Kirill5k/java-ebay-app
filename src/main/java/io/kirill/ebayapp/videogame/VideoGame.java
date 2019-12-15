package io.kirill.ebayapp.videogame;

import io.kirill.ebayapp.common.domain.ListingDetails;
import io.kirill.ebayapp.common.domain.PriceQuery;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.With;

@Value
@With
@Builder
@RequiredArgsConstructor
public class VideoGame implements PriceQuery<VideoGame> {
  private final String name;
  private final String platform;
  private final String genre;
  private final Integer year;
  private final ListingDetails listingDetails;

  @Override
  public String queryString() {
    return String.format("%s %s", name, platform);
  }

  @Override
  public boolean isSearchable() {
    return name != null && platform != null;
  }
}
