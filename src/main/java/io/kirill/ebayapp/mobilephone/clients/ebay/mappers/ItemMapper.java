package io.kirill.ebayapp.mobilephone.clients.ebay.mappers;

import io.kirill.ebayapp.mobilephone.MobilePhone;
import io.kirill.ebayapp.mobilephone.clients.ebay.models.item.Item;
import io.kirill.ebayapp.mobilephone.clients.ebay.models.item.ItemImage;
import io.kirill.ebayapp.mobilephone.clients.ebay.models.item.ItemProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

@Component
public class ItemMapper {
  private static final String MAKE_PROPERTY = "Brand";
  private static final String MODEL_PROPERTY = "Model";
  private static final String COLOUR_PROPERTY = "Colour";
  private static final String STORAGE_CAPACITY_PROPERTY = "Storage Capacity";
  private static final String NETWORK_PROPERTY = "Network";

  public MobilePhone toMobilePhone(Item item) {
    Map<String, String> properties = ofNullable(item.getLocalizedAspects())
        .stream().flatMap(Collection::stream)
        .collect(toMap(ItemProperty::getName, ItemProperty::getValue));

    return MobilePhone.builder()
        .make(properties.getOrDefault(MAKE_PROPERTY, item.getBrand()))
        .storageCapacity(properties.getOrDefault(STORAGE_CAPACITY_PROPERTY, "").replaceAll(" ", ""))
        .model(properties.getOrDefault(MODEL_PROPERTY, item.getMpn()))
        .colour(properties.getOrDefault(COLOUR_PROPERTY, item.getColor()))
        .network(properties.get(NETWORK_PROPERTY))
        .condition(item.getCondition())
        .price(item.getPrice().getValue())
        .listingTitle(item.getTitle())
        .listingDescription(item.getShortDescription())
        .datePosted(Instant.now())
        .url(item.getItemWebUrl())
        .image(ofNullable(item.getImage()).map(ItemImage::getImageUrl).orElse(null))
        .build();
  }
}
