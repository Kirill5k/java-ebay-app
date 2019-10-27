package io.kirill.ebayapp.mobilephone;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class MobilePhoneTest {

  @Test
  void fullName() {
    var mobilePhone = MobilePhone.builder()
        .make("Apple")
        .model("Iphone 6s")
        .storageCapacity("16GB")
        .colour("Space Grey")
        .network("Unlocked")
        .build();

    assertThat(mobilePhone.fullName()).isEqualTo("Apple Iphone 6s 16GB Space Grey Unlocked");
  }
}