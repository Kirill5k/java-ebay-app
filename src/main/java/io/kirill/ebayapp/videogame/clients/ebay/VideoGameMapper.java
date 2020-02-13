package io.kirill.ebayapp.videogame.clients.ebay;

import static java.util.Optional.ofNullable;

import io.kirill.ebayapp.common.clients.ebay.ItemMapper;
import io.kirill.ebayapp.common.clients.ebay.models.item.Item;
import io.kirill.ebayapp.videogame.VideoGame;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class VideoGameMapper implements ItemMapper<VideoGame> {
  private static final String PLATFORM_PROPERTY = "Platform";
  private static final String NAME_PROPERTY = "Game Name";
  private static final String GENRE_PROPERTY = "Genre";
  private static final String SUB_GENRE_PROPERTY = "Sub-Genre";
  private static final String RELEASE_YEAR_PROPERTY = "Release Year";

  private static final String WORDS_TO_REMOVE_FROM_TITLE = String.join("|",
      "remastered", "playstation 4", "Nintendo switch", " - ", "sony", "ps4", "blu-ray", "Mirror", "New and sealed",
      "Brand new", "Factory Sealed", "Sealed", "Game new", ",", "Microsoft", "Free post", "Used", "xbox one", "Uk pal", "Game code",
      "Hits", "Tom clancys", "Great Condition", "Videogame fasting", "switch", "new game",
      "[^\\p{L}\\p{N}\\p{P}\\p{Z}]"
  );

  private static final List<String> PLATFORMS = List.of("PS4", "PLAYSTATION 4", "NINTENDO SWITCH", "SWITCH", "XBOX ONE");

  private static final Map<String, String> PLATFORM_MAPPINGS = Map.of(
      "SONY PLAYSTATION 4", "PS4",
      "PLAYSTATION 4", "PS4",
      "SONY PLAYSTATION 3", "PS3",
      "SONY PLAYSTATION 2", "PS2",
      "PLAYSTATION 2", "PS2",
      "NINTENDO SWITCH", "SWITCH",
      "MICROSOFT XBOX ONE", "XBOX ONE",
      "MICROSOFT XBOX 360", "XBOX 360"
  );

  @Override
  public VideoGame map(Item item) {
    var properties = mapProperties(item);
    var listingDetails = mapDetails(item);
    return VideoGame.builder()
        .name(mapName(properties.getOrDefault(NAME_PROPERTY, item.getTitle())))
        .platform(mapPlatform(item.getTitle(), properties))
        .genre(mapGenre(properties))
        .listingDetails(listingDetails)
        .releaseYear(ofNullable(properties.get(RELEASE_YEAR_PROPERTY)).orElse(null))
        .build();
  }

  private String mapName(String title) {
    var upperCaseTitle = title.toUpperCase();
    var platform = PLATFORMS.stream().filter(upperCaseTitle::contains).findFirst();
    var newTitle = platform.map(p -> title.split("(?i)" + p)[0]).filter(t -> !t.isBlank()).orElse(title);
    return newTitle.replaceAll("[’'*()/|:.\\[\\]]", "")
        .replaceFirst("(?i)\\w+(?=\\s+edition) edition", "")
        .replaceAll(String.format("(?i)%s", WORDS_TO_REMOVE_FROM_TITLE), "")
        .replaceAll("é", "e")
        .replaceFirst("^-", "")
        .trim();
  }

  private String mapGenre(Map<String, String> properties) {
    return Stream.of(properties.get(GENRE_PROPERTY), properties.get(SUB_GENRE_PROPERTY))
        .filter(Objects::nonNull)
        .collect(Collectors.joining(" / "));
  }

  private String mapPlatform(String title, Map<String, String> properties) {
    var upperCaseTitle = title.toUpperCase();
    return ofNullable(properties.get(PLATFORM_PROPERTY))
        .map(platform -> platform.split(",")[0])
        .or(() -> PLATFORMS.stream().filter(upperCaseTitle::contains).findFirst())
        .map(String::toUpperCase)
        .map(platform -> PLATFORM_MAPPINGS.getOrDefault(platform, platform))
        .map(platform -> platform.split("/")[0].trim())
        .orElse(null);
  }
}
