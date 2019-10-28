package io.kirill.ebayapp;

import io.kirill.ebayapp.mobilephone.MobilePhone;
import io.kirill.ebayapp.mobilephone.MobilePhoneService;
import java.time.Duration;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppRunner implements CommandLineRunner {
  private static final int MINUTES_PERIOD = 10;
  private static final int EXPECTED_MARGIN_PERCENTAGE = 50;

  private final MobilePhoneService mobilePhoneService;

  @Override
  public void run(String... args) throws Exception {
    Flux.fromStream(Stream.iterate(0, i -> i + 1))
        .delayElements(Duration.ofMinutes(MINUTES_PERIOD))
        .flatMap($ -> mobilePhoneService.getLatestPhonesFromEbay(MINUTES_PERIOD))
        .filter(MobilePhone::hasAllDetails)
        .flatMap(mobilePhoneService::findResellPrice)
        .filter(phone -> phone.isProfitableToResell(EXPECTED_MARGIN_PERCENTAGE))
        .doOnNext(phone -> log.info("found good deal on \"{}\": asking price {}, cex price P{}", phone.fullName(), phone.getPrice(), phone.getResellPrice()))
        .subscribe();
  }
}
