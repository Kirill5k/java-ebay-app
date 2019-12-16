package io.kirill.ebayapp.mobilephone;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MobilePhoneTest {

  final MobilePhone iphone6s = MobilePhoneBuilder.iphone6s().build();

  @Test
  void fullName() {
    assertThat(iphone6s.searchQuery()).isEqualTo("Apple Iphone 6s 16GB Space Grey Unlocked");
    assertThat(iphone6s.withColour(null).searchQuery()).isEqualTo("Apple Iphone 6s 16GB Unlocked");
    assertThat(iphone6s.withMake(null).searchQuery()).isEqualTo("Iphone 6s 16GB Space Grey Unlocked");
  }

  @Test
  void hasMinAmountOfDetails() {
    assertThat(iphone6s.isSearchable()).isTrue();
    assertThat(iphone6s.withModel(null).isSearchable()).isFalse();
    assertThat(iphone6s.withMake(null).isSearchable()).isFalse();
  }

  @Test
  void isProfitableToResell() {
    var goodDealPhoneDetails = iphone6s.getListingDetails().withPrice(BigDecimal.valueOf(100)).withResellPrice(BigDecimal.valueOf(160));
    assertThat(iphone6s.withListingDetails(goodDealPhoneDetails).isProfitableToResell(50)).isTrue();

    var badDealPhoneDetails = iphone6s.getListingDetails().withPrice(BigDecimal.valueOf(100)).withResellPrice(BigDecimal.valueOf(140));
    assertThat(iphone6s.withListingDetails(badDealPhoneDetails).isProfitableToResell(50)).isFalse();

    var detailsWithoutResellPrice = iphone6s.getListingDetails().withPrice(BigDecimal.valueOf(100)).withResellPrice(null);
    assertThat(iphone6s.withListingDetails(detailsWithoutResellPrice).isProfitableToResell(50)).isFalse();

    var anotherDealPhoneDetails = iphone6s.getListingDetails().withPrice(BigDecimal.valueOf(100)).withResellPrice(BigDecimal.valueOf(100));
    assertThat(iphone6s.withListingDetails(anotherDealPhoneDetails).isProfitableToResell(-10)).isTrue();
  }
}