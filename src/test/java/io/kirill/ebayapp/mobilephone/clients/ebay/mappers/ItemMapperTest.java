package io.kirill.ebayapp.mobilephone.clients.ebay.mappers;

import io.kirill.ebayapp.mobilephone.clients.ebay.models.Price;
import io.kirill.ebayapp.mobilephone.clients.ebay.models.item.Item;
import io.kirill.ebayapp.mobilephone.clients.ebay.models.item.ItemImage;
import io.kirill.ebayapp.mobilephone.clients.ebay.models.item.ItemProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ItemMapperTest {
  String itemUrl = "http://ebay.com/item";
  String imageUrl = "http://ebay.com/item.jpg";

  ItemMapper itemMapper;

  @BeforeEach
  void setUp() {
    itemMapper = new ItemMapper();
  }

  @Test
  void toMobilePhone() {
    var item = Item.builder()
        .condition("new")
        .mpn("MN4U2BA")
        .color("silver")
        .title("title")
        .shortDescription("description")
        .description("full description")
        .brand("apple")
        .price(new Price(BigDecimal.valueOf(9.99), "GBP"))
        .itemWebUrl(itemUrl)
        .image(new ItemImage(imageUrl))
        .localizedAspects(List.of(
            new ItemProperty("type", "Brand", "Apple"),
            new ItemProperty("type", "Network", "Unlocked"),
            new ItemProperty("type", "Model", "Iphone 6s"),
            new ItemProperty("type", "Colour", "Grey"),
            new ItemProperty("type", "Manufacturer Colour", "Space Grey, Blue"),
            new ItemProperty("type", "Storage Capacity", "16 GB / 32 GB")
        ))
        .build();

    var phone = itemMapper.toMobilePhone(item);

    assertThat(phone.getNetwork()).isEqualTo("Unlocked");
    assertThat(phone.getStorageCapacity()).isEqualTo("16GB");
    assertThat(phone.getMake()).isEqualTo("Apple");
    assertThat(phone.getModel()).isEqualTo("Iphone 6s");
    assertThat(phone.getColour()).isEqualTo("Space Grey");
    assertThat(phone.getManufacturerColour()).isEqualTo("Space Grey");
    assertThat(phone.getPrice()).isEqualTo(BigDecimal.valueOf(9.99));
    assertThat(phone.getCondition()).isEqualTo("new");
    assertThat(phone.getUrl()).isEqualTo(itemUrl);
    assertThat(phone.getListingTitle()).isEqualTo("title");
    assertThat(phone.getListingDescription()).isEqualTo("description");
    assertThat(phone.getFullDescription()).isEqualTo("full description");
    assertThat(phone.getDatePosted()).isBetween(Instant.now().minusSeconds(10), Instant.now().plusSeconds(10));
    assertThat(phone.getImage()).isEqualTo(imageUrl);
    assertThat(phone.getMpn()).isEqualTo("MN4U2BA");
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "blah blah has a crack blah",
      "blah blah no touchid blah blah",
      "no touchid",
      "has cracked screen",
      "bla bla touch id doesn't work blah blah",
      "bla bla touch id doesnt work blah blah",
      "bla bla touch id can't work blah blah",
      "blah blah screen is cracked blah blah",
      "blah blah there is a crack blah blah",
      "blah spares/repairs blah"
  })
  void toMobilePhoneWithFaultyCondition(String description) {
    var item = Item.builder().description(description).build();
    assertThat(itemMapper.toMobilePhone(item).getCondition()).isEqualTo("Faulty");
  }

  @ParameterizedTest
  @ValueSource(strings = {"blah blah has touchid blah blah"})
  void toMobilePhoneWithGoodCondition(String description) {
    var item = Item.builder().description(description).condition("Working").build();
    assertThat(itemMapper.toMobilePhone(item).getCondition()).isEqualTo("Working");
  }
}