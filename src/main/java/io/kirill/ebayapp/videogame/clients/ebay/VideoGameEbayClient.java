package io.kirill.ebayapp.videogame.clients.ebay;

import static java.util.stream.Collectors.toList;

import io.kirill.ebayapp.common.clients.ebay.EbayAuthClient;
import io.kirill.ebayapp.common.clients.ebay.EbayClient;
import io.kirill.ebayapp.common.clients.ebay.EbaySearchClient;
import io.kirill.ebayapp.common.clients.ebay.exceptions.EbayAuthError;
import io.kirill.ebayapp.common.clients.ebay.models.search.SearchResult;
import io.kirill.ebayapp.videogame.VideoGame;
import java.time.Instant;
import java.util.List;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
public class VideoGameEbayClient implements EbayClient {
  private static final int VIDEO_GAMES_CATEGORY_ID = 139973;

  private final static List<String> SEARCH_QUERIES = List.of("PS4", "SWITCH", "XBOX ONE");

  private final static String DEFAULT_FILTER = "conditionIds:{1000|1500|2000|2500|3000|4000|5000}," +
      "deliveryCountry:GB," +
      "price:[0..100]," +
      "priceCurrency:GBP," +
      "itemLocationCountry:GB,";

  private final static String NEWLY_LISTED_FILTER = DEFAULT_FILTER + "buyingOptions:{FIXED_PRICE},itemStartDate:[%s]";
  private final static String ENDING_SOON_FILTER = DEFAULT_FILTER + "buyingOptions:{AUCTION},itemEndDate:[..%s]";

  private static final String TITLE_TRIGGER_WORDS = String.join("|",
      "digital code", "digital-code", "download code", "upgrade code", "style covers", "no case", "credits",
      "coin", "skins", "bundle", "no game", "digital key", "download key", "just the case", "cartridge only", "disc only",
      "player generator", "pve official", "read description", "see description",
      "fallout 76 (\\w+\\s){4,}", "borderlands 3 (\\w+\\s){4,}", "rocket league (\\w+\\s){4,}"
  );

  private final EbayAuthClient authClient;
  private final EbaySearchClient searchClient;
  private final VideoGameMapper videoGameMapper;


  public Flux<VideoGame> getGamesListedInLastMinutes(int minutes) {
    var filter = searchFilter(NEWLY_LISTED_FILTER, Instant.now().minusSeconds(minutes * 60));
    return findGames(filter);
  }

  public Flux<VideoGame> getGamesEndingIn(int minutes) {
    var filter = searchFilter(ENDING_SOON_FILTER, Instant.now().plusSeconds(minutes * 60));
    return findGames(filter);
  }

  private Flux<VideoGame> findGames(String filter) {
    return authClient.accessToken()
        .flatMapMany(t -> Flux.merge(
            SEARCH_QUERIES.stream().map(q -> paramsWithQuery(VIDEO_GAMES_CATEGORY_ID, filter, q)).map(p -> searchClient.search(t, p)).collect(toList())
        ))
        .doOnError(e -> e instanceof EbayAuthError, e -> authClient.switchAccount())
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
