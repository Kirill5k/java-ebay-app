package io.kirill.ebayapp.mobilephone.clients.ebay;

import io.kirill.ebayapp.mobilephone.domain.ListingDetails;
import io.kirill.ebayapp.mobilephone.domain.MobilePhone;
import io.kirill.ebayapp.common.clients.ebay.models.Price;
import io.kirill.ebayapp.common.clients.ebay.models.item.Item;
import io.kirill.ebayapp.common.clients.ebay.models.item.ItemImage;
import io.kirill.ebayapp.common.clients.ebay.models.item.ItemProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

@Component
class ItemMapper {
  private static final List<String> VALID_NETWORKS = List.of("unlocked", "o2", "three", "ee", "vodafone", "three", "tesco");
  private static final String UNLOCKED_NETWORK = "Unlocked";


  private static final String TITLE_CONDITION_TRIGGER_WORDS = String.join("|",
      "cracked", "fault", "spares", "repair", "smashed", "no touch", "broken", "not work", "damag");

  private static final String DESCRIPTION_CONDITION_TRIGGER_WORDS = String.join("|",
      "no touchid", "no touch id", "no faceid", "no face id", "home button fault", "faulty home", "faulty touch",
      "is icloud lock", "has icloud lock",  "has activation lock", "icloud locked",
      "is fault",  "faulty screen", "is damag", "is slight damag", "damaged screen",
      "has crack", "have crack", "has slight crack", "got crack", "cracked screen", "hairline crack", "has small crack",
      "is badly crack", "is crack", "is slight crack", "cracked display",
      "spares/repair", "spares or parts", "spares or repair", "for parts only", "spares or repair", "parts only", "spares repair", "spares & repair",
      "doesnt work", "dont work", "not work", "cant work",
      "are broke", "is smashed", "is broke", "smashed screen",
      "has some screen burn", "has screen burn", "needs replac", "needs glass replac", "needs new screen", "few dents"
      );
  private static final String FAULTY_CONDITION = "Faulty";

  private static final String MAKE_PROPERTY = "Brand";
  private static final String MODEL_PROPERTY = "Model";
  private static final String MANUFACTURER_COLOR_PROPERTY = "Manufacturer Colour";
  private static final String COLOUR_PROPERTY = "Colour";
  private static final String STORAGE_CAPACITY_PROPERTY = "Storage Capacity";
  private static final String NETWORK_PROPERTY = "Network";

  MobilePhone toMobilePhone(Item item) {
    Map<String, String> properties = ofNullable(item.getLocalizedAspects())
        .stream().flatMap(Collection::stream)
        .collect(toMap(ItemProperty::getName, ItemProperty::getValue));

    var listingDetails = ListingDetails.builder()
        .type(ofNullable(item.getBuyingOptions()).filter(opts -> opts.contains("FIXED_PRICE")).map($ -> "BUY_IT_NOW").orElse("AUCTION"))
        .originalCondition(item.getCondition())
        .title(item.getTitle())
        .dateEnded(item.getItemEndDate())
        .datePosted(Instant.now())
        .description(item.getShortDescription())
        .image(ofNullable(item.getImage()).map(ItemImage::getImageUrl).orElse(null))
        .url(item.getItemWebUrl())
        .build();

    return MobilePhone.builder()
        .make(properties.getOrDefault(MAKE_PROPERTY, item.getBrand()))
        .storageCapacity(mapStorage(properties))
        .model(properties.getOrDefault(MODEL_PROPERTY, item.getMpn()))
        .colour(mapColour(properties.getOrDefault(MANUFACTURER_COLOR_PROPERTY, properties.get(COLOUR_PROPERTY))))
        .manufacturerColour(mapColour(properties.get(MANUFACTURER_COLOR_PROPERTY)))
        .network(mapNetwork(properties))
        .condition(mapCondition(item))
        .price(ofNullable(item.getPrice()).map(Price::getValue).orElse(null))
        .mpn(item.getMpn())
        .listingDetails(listingDetails)
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
        .orElse(null);
  }

  private String mapStorage(Map<String, String> properties) {
    return ofNullable(properties.get(STORAGE_CAPACITY_PROPERTY))
        .map(s -> s.split("[/,]")[0].trim())
        .map(s -> s.replaceAll(" ", ""))
        .orElse(null);
  }

  private String mapCondition(Item item) {
    return mapConditionFromTitle(item).or(() -> mapConditionFromDescription(item)).orElseGet(item::getCondition);
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
