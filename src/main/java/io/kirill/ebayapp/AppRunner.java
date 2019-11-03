package io.kirill.ebayapp;

import io.kirill.ebayapp.mobilephone.MobilePhoneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppRunner {
  private static final int MINUTES_PERIOD = 5;
  private static final int MIN_MARGIN_PERCENTAGE = 25;

  private final MobilePhoneService mobilePhoneService;

  @Scheduled(fixedDelay = MINUTES_PERIOD * 60000)
  void run() {
    mobilePhoneService.getLatestFromEbay(MINUTES_PERIOD)
        .delayElements(Duration.ofSeconds(1))
        .flatMap(mobilePhoneService::findResellPrice)
        .flatMap(mobilePhoneService::save)
        .filter(phone -> phone.getResellPrice() != null && phone.isProfitableToResell(MIN_MARGIN_PERCENTAGE))
        .flatMap(mobilePhoneService::informAboutThePhone)
        .doOnError(error -> log.error("error during app run: {} {}", error.getMessage(), error))
        .subscribe();
  }
}
