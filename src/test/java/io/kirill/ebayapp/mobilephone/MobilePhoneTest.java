package io.kirill.ebayapp.mobilephone;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MobilePhoneTest {

  final MobilePhone iphone6s = MobilePhoneBuilder.iphone6s().build();

  @Test
  void fullName() {
    assertThat(iphone6s.fullName()).isEqualTo("Apple Iphone 6s 16GB Space Grey Unlocked");
    assertThat(iphone6s.withColour(null).fullName()).isEqualTo("Apple Iphone 6s 16GB Unlocked");
    assertThat(iphone6s.withMake(null).fullName()).isEqualTo("Iphone 6s 16GB Space Grey Unlocked");
  }

  @Test
  void hasMinAmountOfDetails() {
    assertThat(iphone6s.hasMinAmountOfDetails()).isTrue();
    assertThat(iphone6s.withModel(null).hasMinAmountOfDetails()).isFalse();
    assertThat(iphone6s.withMake(null).hasMinAmountOfDetails()).isFalse();
  }

  @Test
  void isProfitableToResell() {
    var goodDealPhone = iphone6s.withPrice(BigDecimal.valueOf(100)).withResellPrice(BigDecimal.valueOf(160));
    assertThat(goodDealPhone.isProfitableToResell(50)).isTrue();

    var badDealPhone = iphone6s.withPrice(BigDecimal.valueOf(100)).withResellPrice(BigDecimal.valueOf(140));
    assertThat(badDealPhone.isProfitableToResell(50)).isFalse();
  }
}