package io.kirill.ebayapp.mobilephone.clients.ebay;

import static org.assertj.core.api.Assertions.assertThat;

import io.kirill.ebayapp.common.clients.ebay.models.Price;
import io.kirill.ebayapp.common.clients.ebay.models.item.Item;
import io.kirill.ebayapp.common.clients.ebay.models.item.ItemImage;
import io.kirill.ebayapp.common.clients.ebay.models.item.ItemProperty;
import io.kirill.ebayapp.common.clients.ebay.models.item.ItemSeller;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MobilePhoneMapperTest {
  String itemUrl = "http://ebay.com/item";
  String imageUrl = "http://ebay.com/item.jpg";

  MobilePhoneMapper mobilePhoneMapper;

  @BeforeEach
  void setUp() {
    mobilePhoneMapper = new MobilePhoneMapper();
  }

  @Test
  void toMobilePhone() {
    var itemEndDate = Instant.now();
    var item = Item.builder()
        .condition("new and awesome")
        .mpn("MN4U2BA")
        .color("silver")
        .title("title")
        .shortDescription("<p style=\"{color: blue}\">description</p>")
        .description("full description")
        .brand("apple")
        .price(new Price(BigDecimal.valueOf(9.99), "GBP"))
        .itemWebUrl(itemUrl)
        .image(new ItemImage(imageUrl))
        .itemEndDate(itemEndDate)
        .seller(new ItemSeller("boris"))
        .localizedAspects(List.of(
            new ItemProperty("type", "Brand", "Apple"),
            new ItemProperty("type", "Network", "Vodafone"),
            new ItemProperty("type", "Model", "Iphone 6s"),
            new ItemProperty("type", "Colour", "Grey"),
            new ItemProperty("type", "Manufacturer Colour", "(PRODUCT)Space gray midnight, Blue"),
            new ItemProperty("type", "Storage Capacity", "16 GB / 32 GB")
        ))
        .build();

    var phone = mobilePhoneMapper.map(item);

    assertThat(phone.getNetwork()).isEqualTo("Vodafone");
    assertThat(phone.getStorageCapacity()).isEqualTo("16GB");
    assertThat(phone.getMake()).isEqualTo("Apple");
    assertThat(phone.getModel()).isEqualTo("Iphone 6s");
    assertThat(phone.getColour()).isEqualTo("Grey");
    assertThat(phone.getManufacturerColour()).isEqualTo("(PRODUCT)Space gray midnight, Blue");
    assertThat(phone.getCondition()).isEqualTo("new and awesome");
    assertThat(phone.getMpn()).isEqualTo("MN4U2BA");

    var listingDetails = phone.getListingDetails();
    assertThat(listingDetails.getType()).isEqualTo("AUCTION");
    assertThat(listingDetails.getUrl()).isEqualTo(itemUrl);
    assertThat(listingDetails.getTitle()).isEqualTo("title");
    assertThat(listingDetails.getDescription()).isEqualTo("description");
    assertThat(listingDetails.getDatePosted()).isBetween(Instant.now().minusSeconds(10), Instant.now().plusSeconds(10));
    assertThat(listingDetails.getImage()).isEqualTo(imageUrl);
    assertThat(listingDetails.getOriginalCondition()).isEqualTo("new and awesome");
    assertThat(listingDetails.getDateEnded()).isEqualTo(itemEndDate);
    assertThat(listingDetails.getSeller()).isEqualTo("boris");
    assertThat(listingDetails.getPrice()).isEqualTo(BigDecimal.valueOf(9.99));
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
      "blah spares/repairs blah",
      "please read needs new screen!. Condition is Used. Dispatched with Royal Mail 1st Class.\n<br>"
  })
  void toMobilePhoneWithFaultyCondition(String description) {
    var item = Item.builder().shortDescription(null).description(description).build();
    assertThat(mobilePhoneMapper.map(item).getCondition()).isEqualTo("Faulty");
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "blah blah has a crack blah",
      "blah blah no touchid blah blah",
      "no touchid",
      "has a crack",
      "has a small crack",
      "has cracked screen",
      "bla bla touch id don't work blah blah",
      "bla bla touch id doesn't work blah blah",
      "bla bla touch id doesnt work blah blah",
      "bla bla touch id can't work blah blah",
      "blah blah screen is cracked blah blah",
      "blah blah there is a crack blah blah",
      "blah blah NEEDS NEW SCREEN! blah blah",
      "blah spares/repairs blah"
  })
  void toMobilePhoneWithFaultyConditionByAnalyzingShortDescription(String description) {
    var item = Item.builder().title("best").shortDescription(description).description(null).build();
    assertThat(mobilePhoneMapper.map(item).getCondition()).isEqualTo("Faulty");
  }

  @ParameterizedTest
  @ValueSource(strings = {"blah blah has touchid blah blah"})
  void toMobilePhoneWithGoodCondition(String description) {
    var item = Item.builder().description(description).condition("Working").build();
    assertThat(mobilePhoneMapper.map(item).getCondition()).isEqualTo("Working");
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "good but smashed",
      "galaxy s8 smashed screen",
      "iphone for spares repairs",
      "iphone with Cracked Screen",
      "blah spares/repairs blah"
  })
  void toMobilePhoneWithFaultyConditionByAnalyzingListingTitle(String title) {
    var item = Item.builder().title(title).description("10 out of 10").shortDescription(null).build();
    assertThat(mobilePhoneMapper.map(item).getCondition()).isEqualTo("Faulty");
  }
}