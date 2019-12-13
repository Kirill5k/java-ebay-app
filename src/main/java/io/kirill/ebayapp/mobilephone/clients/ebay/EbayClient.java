package io.kirill.ebayapp.mobilephone.clients.ebay;

import static java.util.concurrent.TimeUnit.MINUTES;
import static net.jodah.expiringmap.ExpiringMap.ExpirationPolicy.CREATED;

import io.kirill.ebayapp.mobilephone.MobilePhone;
import io.kirill.ebayapp.mobilephone.clients.ebay.mappers.ItemMapper;
import io.kirill.ebayapp.mobilephone.clients.ebay.models.search.SearchResult;
import java.time.Instant;
import java.util.Map;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
public class EbayClient {
  private static final int MOBILES_PHONES_CATEGORY_ID = 9355;

  private static final int MIN_FEEDBACK_SCORE = 10;
  private static final double MIN_FEEDBACK_PERCENT = 90;

  private final EbayAuthClient authClient;
  private final EbaySearchClient searchClient;
  private final ItemMapper itemMapper;

  private Map<String, String> ids = ExpiringMap.builder()
      .expirationPolicy(CREATED)
      .expiration(30, MINUTES)
      .build();

  public Flux<MobilePhone> getPhonesListedInTheLastMinutes(int minutes) {
    return authClient.accessToken()
        .flatMapMany(token -> searchClient.searchForAllInCategory(token, MOBILES_PHONES_CATEGORY_ID, Instant.now().minusSeconds(minutes * 60)))
        .filter(hasTrustedSeller)
        .filter(searchResult -> !ids.containsKey(searchResult.getItemId()))
        .flatMap(sr -> authClient.accessToken().flatMap(token -> searchClient.getItem(token, sr.getItemId())))
        .doOnNext(item -> ids.put(item.getItemId(), ""))
        .map(itemMapper::toMobilePhone);
  }

  private Predicate<SearchResult> hasTrustedSeller = searchResult -> searchResult.getSeller() != null &&
      searchResult.getSeller().getFeedbackPercentage() != null &&
      searchResult.getSeller().getFeedbackScore() != null &&
      searchResult.getSeller().getFeedbackPercentage() > MIN_FEEDBACK_PERCENT &&
      searchResult.getSeller().getFeedbackScore() > MIN_FEEDBACK_SCORE;
}
