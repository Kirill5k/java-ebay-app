package io.kirill.ebayapp.mobilephone;

public class MobilePhoneBuilder {

  public static MobilePhone.MobilePhoneBuilder iphone6s() {
    return MobilePhone.builder()
        .make("Apple")
        .model("Iphone 6s")
        .storageCapacity("16GB")
        .colour("Space Grey")
        .network("Unlocked");
  }
}
