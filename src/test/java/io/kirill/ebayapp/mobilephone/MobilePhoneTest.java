package io.kirill.ebayapp.mobilephone;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class MobilePhoneTest {

  final MobilePhone iphone6s = MobilePhone.builder()
      .make("Apple")
      .model("Iphone 6s")
      .storageCapacity("16GB")
      .colour("Space Grey")
      .network("Unlocked")
      .build();

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
}