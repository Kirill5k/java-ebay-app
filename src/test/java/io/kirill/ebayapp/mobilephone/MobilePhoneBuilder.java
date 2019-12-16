package io.kirill.ebayapp.mobilephone;

import io.kirill.ebayapp.common.domain.ListingDetails;
import io.kirill.ebayapp.common.domain.ResellPrice;
import java.math.BigDecimal;
import java.time.Instant;

public class MobilePhoneBuilder {

  public static MobilePhone.MobilePhoneBuilder iphone6s() {
    var resellPrice = new ResellPrice(BigDecimal.ONE, BigDecimal.TEN);

    var listingDetails = ListingDetails.builder()
        .price(BigDecimal.valueOf(100.0))
        .originalCondition("new")
        .url("ebay.com")
        .image("ebay.com/image.jpeg")
        .description("item description")
        .title("item title")
        .type("BUY_IT_NOW")
        .datePosted(Instant.now())
        .dateEnded(Instant.now().plusSeconds(1800))
        .build();

    return MobilePhone.builder()
        .listingDetails(listingDetails)
        .resellPrice(resellPrice)
        .make("Apple")
        .model("Iphone 6s")
        .storageCapacity("16GB")
        .colour("Space Grey")
        .network("Unlocked");
  }
}
