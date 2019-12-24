package io.kirill.ebayapp.mobilephone.clients.ebay;

import io.kirill.ebayapp.common.clients.ebay.ItemMapper;
import io.kirill.ebayapp.common.clients.ebay.models.item.Item;
import io.kirill.ebayapp.mobilephone.MobilePhone;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

@Component
class MobilePhoneMapper implements ItemMapper<MobilePhone> {
  private static final List<String> VALID_NETWORKS = List.of("unlocked", "o2", "three", "ee", "vodafone", "three", "tesco");
  private static final String UNLOCKED_NETWORK = "Unlocked";


  private static final String TITLE_CONDITION_TRIGGER_WORDS = String.join("|",
      "cracked", "fault", "spares", "repair", "smashed", "no touch", "no face", "broken", "not work", "damag", "no service", "screenburn", "screen burn");

  private static final String DESCRIPTION_CONDITION_TRIGGER_WORDS = String.join("|",
      "no touchid", "no touch id", "no faceid", "no face id", "home button fault", "faulty home", "faulty touch",
      "is icloud lock", "has icloud lock",  "has activation lock", "icloud locked",
      "faulty screen", "is damag", "is slight damag", "damaged screen", "badly damag", "light damag",
      "has crack", "have crack", "has slight crack", "got crack", "cracked screen", "hairline crack", "has small crack", "some crack", "crack on screen",
      "is small crack", "is badly crack", "is crack", "is slight crack", "cracked display", "got some crack",
      "spares/repair", "spares or parts", "spares or repair", "for parts only", "spares or repair", "parts only", "spares repair", "spares & repair",
      "doesnt work", "dont work", "not work", "cant work", "isnt work",
      "are broke", "is smashed", "is broke", "smashed screen",
      "has some screen burn", "has screen burn", "needs glass replac", "needs new screen"
      );
  private static final String FAULTY_CONDITION = "Faulty";

  private static final String MAKE_PROPERTY = "Brand";
  private static final String MODEL_PROPERTY = "Model";
  private static final String MANUFACTURER_COLOR_PROPERTY = "Manufacturer Colour";
  private static final String COLOUR_PROPERTY = "Colour";
  private static final String STORAGE_CAPACITY_PROPERTY = "Storage Capacity";
  private static final String NETWORK_PROPERTY = "Network";

  @Override
  public MobilePhone map(Item item) {
    var properties = mapProperties(item);

    return MobilePhone.builder()
        .make(properties.getOrDefault(MAKE_PROPERTY, item.getBrand()))
        .storageCapacity(mapStorage(properties))
        .model(properties.getOrDefault(MODEL_PROPERTY, item.getMpn()))
        .colour(mapColour(properties.getOrDefault(MANUFACTURER_COLOR_PROPERTY, properties.get(COLOUR_PROPERTY))))
        .manufacturerColour(mapColour(properties.get(MANUFACTURER_COLOR_PROPERTY)))
        .network(mapNetwork(properties))
        .condition(mapCondition(item))
        .mpn(item.getMpn())
        .listingDetails(mapDetails(item))
        .build();
  }

  private String mapNetwork(Map<String, String> properties) {
    return ofNullable(properties.get(NETWORK_PROPERTY))
        .filter(network -> VALID_NETWORKS.contains(network.toLowerCase()))
        .orElse(UNLOCKED_NETWORK);
  }

  private String mapColour(String colour) {
    return ofNullable(colour)
        .map(c -> c.split("[/,]")[0].trim())
        .map(c -> c.replaceAll("(?i)Gray", "Grey"))
        .orElse(null);
  }

  private String mapStorage(Map<String, String> properties) {
    return ofNullable(properties.get(STORAGE_CAPACITY_PROPERTY))
        .map(s -> s.split("[/,]")[0].trim())
        .map(s -> s.replaceAll(" ", ""))
        .orElse(null);
  }

  private String mapCondition(Item item) {
    return mapConditionFromTitle(item)
        .or(() -> mapConditionFromDescription(item))
        .orElseGet(item::getCondition);
  }

  private Optional<String> mapConditionFromTitle(Item item) {
    return ofNullable(item.getTitle())
        .map(String::toLowerCase)
        .filter(title -> title.matches(String.format("^.*?(%s).*$", TITLE_CONDITION_TRIGGER_WORDS)))
        .map($ -> FAULTY_CONDITION);
  }

  private Optional<String> mapConditionFromDescription(Item item) {
    return Stream.of(item.getDescription(), item.getShortDescription())
        .filter(Objects::nonNull)
        .map(String::toLowerCase)
        .reduce(String::concat)
        .map(cond -> cond.replaceAll(" a ", " ").replaceAll("'", ""))
        .filter(d -> d.matches(String.format("^.*?(%s).*$", DESCRIPTION_CONDITION_TRIGGER_WORDS)))
        .map($ -> FAULTY_CONDITION);
  }
}
