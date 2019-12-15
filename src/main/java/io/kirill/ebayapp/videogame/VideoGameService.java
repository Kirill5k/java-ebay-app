package io.kirill.ebayapp.videogame;

import io.kirill.ebayapp.common.clients.cex.CexClient;
import io.kirill.ebayapp.common.clients.telegram.TelegramClient;
import io.kirill.ebayapp.videogame.clients.ebay.VideoGameEbayClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class VideoGameService {
  private final static String MESSAGE_TEMPLATE = "good deal on \"%s\": asking price %s, cex price %s %s";

  private final VideoGameEbayClient videoGameEbayClient;
  private final CexClient cexClient;
  private final TelegramClient telegramClient;

  public Flux<VideoGame> getLatestFromEbay(int minutes) {
    return videoGameEbayClient.getPS4GamesListedInLastMinutes(minutes);
  }

  public Mono<VideoGame> findResellPrice(VideoGame videoGame) {
    return Mono.just(videoGame)
        .flatMap(cexClient::getMinResellPrice)
        .map(videoGame::withResellPrice)
        .defaultIfEmpty(videoGame);
  }

  public Mono<Void> sendNotification(VideoGame videoGame) {
    var details = videoGame.getListingDetails();
    var message = String.format(MESSAGE_TEMPLATE, videoGame.queryString(), details.getPrice(), details.getResellPrice(), details.getUrl());
    return telegramClient.sendMessageToSecondaryChannel(message);
  }
}
