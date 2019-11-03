package io.kirill.ebayapp.mobilephone.clients.ebay.mappers;

import io.kirill.ebayapp.mobilephone.clients.ebay.models.Price;
import io.kirill.ebayapp.mobilephone.clients.ebay.models.item.Item;
import io.kirill.ebayapp.mobilephone.clients.ebay.models.item.ItemImage;
import io.kirill.ebayapp.mobilephone.clients.ebay.models.item.ItemProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        .color("silver")
        .title("title")
        .shortDescription("description")
        .brand("apple")
        .price(new Price(BigDecimal.valueOf(9.99), "GBP"))
        .itemWebUrl(itemUrl)
        .image(new ItemImage(imageUrl))
        .localizedAspects(List.of(
            new ItemProperty("type", "Brand", "Apple"),
            new ItemProperty("type", "Network", "Unlocked"),
            new ItemProperty("type", "Model", "Iphone 6s"),
            new ItemProperty("type", "Colour", "Space Grey"),
            new ItemProperty("type", "Storage Capacity", "16 GB")
        ))
        .build();

    var phone = itemMapper.toMobilePhone(item);

    assertThat(phone.getNetwork()).isEqualTo("Unlocked");
    assertThat(phone.getStorageCapacity()).isEqualTo("16GB");
    assertThat(phone.getMake()).isEqualTo("Apple");
    assertThat(phone.getModel()).isEqualTo("Iphone 6s");
    assertThat(phone.getColour()).isEqualTo("Space Grey");
    assertThat(phone.getPrice()).isEqualTo(BigDecimal.valueOf(9.99));
    assertThat(phone.getCondition()).isEqualTo("new");
    assertThat(phone.getUrl()).isEqualTo(itemUrl);
    assertThat(phone.getListingTitle()).isEqualTo("title");
    assertThat(phone.getListingDescription()).isEqualTo("description");
    assertThat(phone.getDatePosted()).isBetween(Instant.now().minusSeconds(10), Instant.now().plusSeconds(10));
    assertThat(phone.getImage()).isEqualTo(imageUrl);
  }
}