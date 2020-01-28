package io.kirill.ebayapp.mobilephone;

import static org.assertj.core.api.Assertions.assertThat;

import io.kirill.ebayapp.common.domain.ResellPrice;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

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
    assertThat(iphone6s.withResellPrice(new ResellPrice(null, BigDecimal.valueOf(160)))
        .isProfitableToResell(50)).isTrue();

    assertThat(iphone6s.withResellPrice(new ResellPrice(null, BigDecimal.valueOf(140)))
        .isProfitableToResell(50)).isFalse();

    assertThat(iphone6s.withResellPrice(new ResellPrice(null, null))
        .isProfitableToResell(50)).isFalse();

    assertThat(iphone6s.withResellPrice(null)
        .isProfitableToResell(50)).isFalse();

    assertThat(iphone6s.withResellPrice(new ResellPrice(null, BigDecimal.valueOf(100)))
        .isProfitableToResell(-10)).isTrue();
  }

  @Test
  void goodDealMessageForBuyItNow() {
    assertThat(iphone6s.goodDealMessage()).isEqualTo("NEW \"Apple Iphone 6s 16GB Space Grey Unlocked\" - ebay: £100.0, cex: £120(20%)/£1 ebay.com");
  }

  @Test
  void goodDealMessageForEndingSoonItem() {
    var endingSoonDetails = iphone6s.getListingDetails().withDateEnded(Instant.now().plusSeconds(3*60));

    assertThat(iphone6s.withListingDetails(endingSoonDetails).goodDealMessage())
        .isEqualTo("ENDING \"Apple Iphone 6s 16GB Space Grey Unlocked\" - ebay: £100.0, cex: £120(20%)/£1 ebay.com");
  }

  @Test
  void goodDealMessageForEndingNotTooSoonItem() {
    var endingSoonDetails = iphone6s.getListingDetails().withDateEnded(Instant.now().plusSeconds(15*60));

    assertThat(iphone6s.withListingDetails(endingSoonDetails).goodDealMessage())
        .isEqualTo("NEW \"Apple Iphone 6s 16GB Space Grey Unlocked\" - ebay: £100.0, cex: £120(20%)/£1 ebay.com");
  }
}