package io.kirill.ebayapp;

import io.kirill.ebayapp.mobilephone.MobilePhone;
import io.kirill.ebayapp.mobilephone.MobilePhoneService;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppRunner {
  private static final int MINUTES_PERIOD = 2;
  private static final int MIN_MARGIN_PERCENTAGE = 25;

  private final MobilePhoneService mobilePhoneService;

  @Scheduled(fixedDelay = MINUTES_PERIOD * 60000)
  public void run() {
    mobilePhoneService.getLatestPhonesFromEbay(MINUTES_PERIOD)
        .filter(MobilePhone::hasAllDetails)
        .delayElements(Duration.ofSeconds(1))
        .flatMap(mobilePhoneService::findResellPrice)
        .filter(phone -> phone.isProfitableToResell(MIN_MARGIN_PERCENTAGE))
        .flatMap(mobilePhoneService::informAboutThePhone)
        .doOnError(error -> log.error("error during app run: {} {}", error.getMessage(), error))
        .subscribe();
  }
}
