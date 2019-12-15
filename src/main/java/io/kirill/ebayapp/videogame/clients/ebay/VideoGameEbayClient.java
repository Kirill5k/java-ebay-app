package io.kirill.ebayapp.videogame.clients.ebay;

import io.kirill.ebayapp.common.clients.ebay.EbayAuthClient;
import io.kirill.ebayapp.common.clients.ebay.EbayClient;
import io.kirill.ebayapp.common.clients.ebay.EbaySearchClient;
import io.kirill.ebayapp.videogame.VideoGame;
import lombok.RequiredArgsConstructor;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Map;

import static java.util.concurrent.TimeUnit.MINUTES;
import static net.jodah.expiringmap.ExpiringMap.ExpirationPolicy.CREATED;

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

  private final EbayAuthClient authClient;
  private final EbaySearchClient searchClient;
  private final VideoGameMapper mobilePhoneMapper;

  private Map<String, String> ids = ExpiringMap.builder()
      .expirationPolicy(CREATED)
      .expiration(60, MINUTES)
      .build();

  public Flux<VideoGame> getPS4GamesListedInLastMinutes(int minutes) {
    return Flux.empty();
  }
}
