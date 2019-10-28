package io.kirill.ebayapp.mobilephone;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class MobilePhoneTest {

  final MobilePhone iphone6s = MobilePhoneBuilder.iphone6s().build();

  @Test
  void fullName() {
    assertThat(iphone6s.fullName()).isEqualTo("Apple Iphone 6s 16GB Space Grey Unlocked");
    assertThat(iphone6s.withColour(null).fullName()).isEqualTo("Apple Iphone 6s 16GB Unlocked");
    assertThat(iphone6s.withMake(null).fullName()).isEqualTo("Iphone 6s 16GB Space Grey Unlocked");
  }

  @Test
  void hasAllDetails() {
    assertThat(iphone6s.hasAllDetails()).isTrue();
    assertThat(iphone6s.withColour(null).hasAllDetails()).isFalse();
    assertThat(iphone6s.withMake(null).hasAllDetails()).isFalse();
  }

  @Test
  void isProfitableToResell() {
    var goodDealPhone = iphone6s.withPrice(BigDecimal.valueOf(100)).withResellPrice(BigDecimal.valueOf(160));
    assertThat(goodDealPhone.isProfitableToResell(50)).isTrue();

    var badDealPhone = iphone6s.withPrice(BigDecimal.valueOf(100)).withResellPrice(BigDecimal.valueOf(140));
    assertThat(badDealPhone.isProfitableToResell(50)).isFalse();
  }
}