package io.kirill.ebayapp.videogame.clients.ebay;

import io.kirill.ebayapp.common.clients.ebay.ItemMapper;
import io.kirill.ebayapp.common.clients.ebay.models.item.Item;
import io.kirill.ebayapp.videogame.VideoGame;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

@Component
public class VideoGameMapper implements ItemMapper<VideoGame> {
  private static final String PLATFORM_PROPERTY = "Platform";
  private static final String NAME_PROPERTY = "Game Name";
  private static final String GENRE_PROPERTY = "Genre";
  private static final String SUB_GENRE_PROPERTY = "Sub-Genre";
  private static final String RELEASE_YEAR_PROPERTY = "Release Year";


  private static final List<String> PLATFORMS = List.of("PS4", "PLAYSTATION 4", "NINTENDO SWITCH", "SWITCH");

  private static final Map<String, String> PLATFORM_MAPPINGS = Map.of(
      "Sony PlayStation 4", "PS4",
      "Nintendo Switch", "Switch"
  );

  @Override
  public VideoGame map(Item item) {
    var properties = mapProperties(item);
    var listingDetails = mapDetails(item);
    return VideoGame.builder()
        .name(mapName(item.getTitle(), properties))
        .platform(mapPlatform(item.getTitle(), properties))
        .genre(mapGenre(properties))
        .listingDetails(listingDetails)
        .releaseYear(ofNullable(properties.get(RELEASE_YEAR_PROPERTY)).orElse(null))
        .build();
  }

  private String mapName(String title, Map<String, String> properties) {
    if (properties.containsKey(NAME_PROPERTY)) {
      return properties.get(NAME_PROPERTY);
    }

    var upperCaseTitle = title.toUpperCase();
    var platform = PLATFORMS.stream().filter(upperCaseTitle::contains).findFirst();
    return platform.map(p -> title.split("(?i)" + p)[0])
        .map(t -> t.replaceAll("[()/|:]", "")
            .replaceFirst("(?i)\\w+(?=\\s+edition) edition", "")
            .replaceAll("(?i)remastered|playstation 4| - |sony", "")
            .trim())
        .orElse(null);
  }

  private String mapGenre(Map<String, String> properties) {
    return Stream.of(properties.get(GENRE_PROPERTY), properties.get(SUB_GENRE_PROPERTY))
        .filter(Objects::nonNull)
        .collect(Collectors.joining(" / "));
  }

  private String mapPlatform(String title, Map<String, String> properties) {
    var upperCaseTitle = title.toUpperCase();
    var platform = PLATFORMS.stream().filter(upperCaseTitle::contains).findFirst();
    return platform
        .or(() -> ofNullable(properties.get(PLATFORM_PROPERTY)).map(PLATFORM_MAPPINGS::get))
        .orElse(null);
  }
}
