package io.kirill.ebayapp.mobilephone;

import io.kirill.ebayapp.mobilephone.domain.ListingDetails;
import io.kirill.ebayapp.mobilephone.domain.MobilePhone;

import java.time.Instant;

public class MobilePhoneBuilder {

  public static MobilePhone.MobilePhoneBuilder iphone6s() {
    var listingDetails = ListingDetails.builder()
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
        .make("Apple")
        .model("Iphone 6s")
        .storageCapacity("16GB")
        .colour("Space Grey")
        .network("Unlocked");
  }
}
