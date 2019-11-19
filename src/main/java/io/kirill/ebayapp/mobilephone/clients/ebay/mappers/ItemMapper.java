package io.kirill.ebayapp.mobilephone.clients.ebay.mappers;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

import io.kirill.ebayapp.mobilephone.MobilePhone;
import io.kirill.ebayapp.mobilephone.clients.ebay.models.Price;
import io.kirill.ebayapp.mobilephone.clients.ebay.models.item.Item;
import io.kirill.ebayapp.mobilephone.clients.ebay.models.item.ItemImage;
import io.kirill.ebayapp.mobilephone.clients.ebay.models.item.ItemProperty;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ItemMapper {
  private static final List<String> VALID_NETWORKS = List.of("unlocked", "o2", "three", "ee", "vodafone", "three", "tesco");
  private static final String UNLOCKED_NETWORK = "Unlocked";

  private static final String CONDITION_TRIGGER_WORDS = String.join("|",
      "has cracks", "no touchid", "no touch id", "no faceid", "home button fault", "has a crack", "has cracked",
      "spares or parts", "screen cracked", "smashed screen", "cracked screen", "is faulty", "is a crack", "faulty screen",
      "is cracked", "spares or repair", "doesn't work", "not work", "few dents", "is damaged", "damaged screen", "for parts only",
      "is a slight crack", "needs a new screen", "isn't working", "has a slight crack"
  );
  private static final String FAULTY_CONDITION = "Faulty";

  private static final String MAKE_PROPERTY = "Brand";
  private static final String MODEL_PROPERTY = "Model";
  private static final String MANUFACTURER_COLOR_PROPERTY = "Manufacturer Colour";
  private static final String COLOUR_PROPERTY = "Colour";
  private static final String STORAGE_CAPACITY_PROPERTY = "Storage Capacity";
  private static final String NETWORK_PROPERTY = "Network";

  public MobilePhone toMobilePhone(Item item) {
    Map<String, String> properties = ofNullable(item.getLocalizedAspects())
        .stream().flatMap(Collection::stream)
        .collect(toMap(ItemProperty::getName, ItemProperty::getValue));

    return MobilePhone.builder()
        .make(properties.getOrDefault(MAKE_PROPERTY, item.getBrand()))
        .storageCapacity(mapStorage(properties))
        .model(properties.getOrDefault(MODEL_PROPERTY, item.getMpn()))
        .colour(mapColour(properties.getOrDefault(MANUFACTURER_COLOR_PROPERTY, properties.get(COLOUR_PROPERTY))))
        .manufacturerColour(mapColour(properties.get(MANUFACTURER_COLOR_PROPERTY)))
        .network(mapNetwork(properties))
        .condition(mapCondition(item))
        .price(ofNullable(item.getPrice()).map(Price::getValue).orElse(null))
        .listingTitle(item.getTitle())
        .listingDescription(item.getShortDescription())
        .datePosted(Instant.now())
        .url(item.getItemWebUrl())
        .image(ofNullable(item.getImage()).map(ItemImage::getImageUrl).orElse(null))
        .mpn(item.getMpn())
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
    return ofNullable(item.getDescription())
        .map(String::toLowerCase)
        .filter(d -> d.matches(String.format("^.*?(%s).*$", CONDITION_TRIGGER_WORDS)))
        .map($ -> FAULTY_CONDITION)
        .orElseGet(item::getCondition);
  }
}
