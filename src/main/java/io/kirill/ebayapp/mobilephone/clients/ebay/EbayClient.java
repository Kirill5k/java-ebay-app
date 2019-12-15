package io.kirill.ebayapp.mobilephone.clients.ebay;

import io.kirill.ebayapp.common.clients.ebay.EbayAuthClient;
import io.kirill.ebayapp.common.clients.ebay.EbaySearchClient;
import io.kirill.ebayapp.common.clients.ebay.models.search.SearchResult;
import io.kirill.ebayapp.mobilephone.MobilePhone;
import lombok.RequiredArgsConstructor;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.Map;
import java.util.function.Predicate;

import static java.util.concurrent.TimeUnit.MINUTES;
import static net.jodah.expiringmap.ExpiringMap.ExpirationPolicy.CREATED;

@Component
@RequiredArgsConstructor
public class EbayClient {
  private static final int MOBILES_PHONES_CATEGORY_ID = 9355;

  private static final int MIN_FEEDBACK_SCORE = 6;
  private static final double MIN_FEEDBACK_PERCENT = 90;

  private final EbayAuthClient authClient;
  private final EbaySearchClient searchClient;
  private final MobilePhoneMapper mobilePhoneMapper;

  private Map<String, String> ids = ExpiringMap.builder()
      .expirationPolicy(CREATED)
      .expiration(60, MINUTES)
      .build();

  public Flux<MobilePhone> getPhonesListedInTheLastMinutes(int minutes) {
    return authClient.accessToken()
        .flatMapMany(token -> searchClient.searchForNewestInCategoryFrom(token, MOBILES_PHONES_CATEGORY_ID, Instant.now().minusSeconds(minutes * 60)))
        .filter(hasTrustedSeller)
        .filter(searchResult -> !ids.containsKey(searchResult.getItemId()))
        .flatMap(sr -> authClient.accessToken().flatMap(token -> searchClient.getItem(token, sr.getItemId())))
        .doOnNext(item -> ids.put(item.getItemId(), ""))
        .map(mobilePhoneMapper::map);
  }

  private Predicate<SearchResult> hasTrustedSeller = searchResult -> searchResult.getSeller() != null &&
      searchResult.getSeller().getFeedbackPercentage() != null &&
      searchResult.getSeller().getFeedbackScore() != null &&
      searchResult.getSeller().getFeedbackPercentage() > MIN_FEEDBACK_PERCENT &&
      searchResult.getSeller().getFeedbackScore() > MIN_FEEDBACK_SCORE;
}
