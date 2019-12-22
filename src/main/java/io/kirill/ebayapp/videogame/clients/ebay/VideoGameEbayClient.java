package io.kirill.ebayapp.videogame.clients.ebay;

import io.kirill.ebayapp.common.clients.ebay.EbayAuthClient;
import io.kirill.ebayapp.common.clients.ebay.EbayClient;
import io.kirill.ebayapp.common.clients.ebay.EbaySearchClient;
import io.kirill.ebayapp.common.clients.ebay.models.search.SearchResult;
import io.kirill.ebayapp.videogame.VideoGame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.function.Predicate;

@Component
@RequiredArgsConstructor
public class VideoGameEbayClient implements EbayClient {
  private static final int VIDEO_GAMES_CATEGORY_ID = 139973;

  private final static String DEFAULT_FILTER = "conditionIds:{1000|1500|2000|2500|3000|4000|5000}," +
      "deliveryCountry:GB," +
      "price:[1..50]," +
      "priceCurrency:GBP," +
      "itemLocationCountry:GB,";

  private final static String NEWLY_LISTED_FILTER = DEFAULT_FILTER + "buyingOptions:{FIXED_PRICE},itemStartDate:[%s]";

  private static final String TITLE_TRIGGER_WORDS = String.join("|",
      "coins", "skins", "bundle");

  private final EbayAuthClient authClient;
  private final EbaySearchClient searchClient;
  private final VideoGameMapper videoGameMapper;


  public Flux<VideoGame> getPS4GamesListedInLastMinutes(int minutes) {
    var filter = searchFilter(NEWLY_LISTED_FILTER, Instant.now().minusSeconds(minutes * 60));
    return authClient.accessToken()
        .flatMapMany(t -> Flux.merge(
            searchClient.search(t, paramsWithQuery(VIDEO_GAMES_CATEGORY_ID, filter, "PS4")),
            searchClient.search(t, paramsWithQuery(VIDEO_GAMES_CATEGORY_ID, filter, "SWITCH"))
        ))
        .filter(hasTrustedSeller)
        .filter(isVideoGame)
        .filter(searchResult -> !ids.containsKey(searchResult.getItemId()))
        .flatMap(sr -> authClient.accessToken().flatMap(token -> searchClient.getItem(token, sr.getItemId())))
        .doOnNext(item -> ids.put(item.getItemId(), ""))
        .map(videoGameMapper::map);
  }

  private Predicate<SearchResult> isVideoGame = searchResult -> !searchResult.getTitle().toLowerCase()
      .matches(String.format("^.*?(%s).*$", TITLE_TRIGGER_WORDS));
}
