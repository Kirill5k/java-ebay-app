package io.kirill.ebayapp;

import io.kirill.ebayapp.mobilephone.MobilePhoneService;
import io.kirill.ebayapp.videogame.VideoGameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppRunner {
  private static final int MINUTES_PERIOD = 15;
  private static final int MIN_MARGIN_PERCENTAGE = 25;

  private final MobilePhoneService mobilePhoneService;
  private final VideoGameService videoGameService;

  @Scheduled(fixedDelay = 120000)
  void searchForPhones() {
    mobilePhoneService.getLatestFromEbay(MINUTES_PERIOD)
        .delayElements(Duration.ofMillis(450))
        .flatMap(mobilePhoneService::findResellPrice)
        .flatMap(mobilePhoneService::save)
        .filter(phone -> phone.isProfitableToResell(MIN_MARGIN_PERCENTAGE) && phone.isInWorkingCondition())
        .flatMap(mobilePhoneService::informAboutThePhone)
        .doOnError(error -> log.error("error during app run: {} {}", error.getMessage(), error))
        .onErrorResume(e -> Mono.empty())
        .subscribe();
  }

  @Scheduled(fixedDelay = 120000)
  void searchForPS4Games() {
    videoGameService.getLatestFromEbay(MINUTES_PERIOD)
        .delayElements(Duration.ofMillis(450))
        .flatMap(videoGameService::findResellPrice)
        .filter(game -> game.isProfitableToResell(-10))
        .flatMap(videoGameService::sendNotification)
        .doOnError(error -> log.error("error during app run: {} {}", error.getMessage(), error))
        .onErrorResume(e -> Mono.empty())
        .subscribe();
  }
}