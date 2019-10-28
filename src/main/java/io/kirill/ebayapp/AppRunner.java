package io.kirill.ebayapp;

import io.kirill.ebayapp.mobilephone.MobilePhone;
import io.kirill.ebayapp.mobilephone.MobilePhoneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppRunner {
  private static final int MINUTES_PERIOD = 10;
  private static final int EXPECTED_MARGIN_PERCENTAGE = 50;

  private final MobilePhoneService mobilePhoneService;

  @Scheduled(fixedDelay = MINUTES_PERIOD * 60 * 1000)
  public void run() {
    mobilePhoneService.getLatestPhonesFromEbay(MINUTES_PERIOD)
        .filter(MobilePhone::hasAllDetails)
        .flatMap(mobilePhoneService::findResellPrice)
        .filter(phone -> phone.isProfitableToResell(EXPECTED_MARGIN_PERCENTAGE))
        .doOnNext(phone -> log.info("found good deal on \"{}\": asking price {}, cex price P{}", phone.fullName(), phone.getPrice(), phone.getResellPrice()))
        .doOnError(error -> log.error("error during app run: {}", error.getMessage(), error))
        .subscribe();
  }
}
