package io.kirill.ebayapp.videogame;

import io.kirill.ebayapp.common.domain.ListingDetails;
import io.kirill.ebayapp.common.domain.PriceQuery;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.With;

@Value
@With
@RequiredArgsConstructor
public class VideoGame implements PriceQuery<VideoGame> {
  private final String name;
  private final String platform;
  private final ListingDetails listingDetails;

  @Override
  public String queryString() {
    return null;
  }

  @Override
  public boolean isSearchable() {
    return name != null && platform != null;
  }
}
