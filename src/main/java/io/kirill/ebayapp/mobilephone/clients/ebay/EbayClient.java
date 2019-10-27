package io.kirill.ebayapp.mobilephone.clients.ebay;

import io.kirill.ebayapp.mobilephone.MobilePhone;
import io.kirill.ebayapp.mobilephone.clients.ebay.mappers.ItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class EbayClient {
  private static final long TIME_OFFSET = 10 * 60;
  private static final int MOBILES_PHONES_CATEGORY_ID = 9355;

  private static final int MIN_FEEDBACK_SCORE = 10;
  private static final double MIN_FEEDBACK_PERCENT = 90;

  private final EbayAuthClient authClient;
  private final EbaySearchClient searchClient;
  private final ItemMapper itemMapper;

  public Flux<MobilePhone> getPhonesListedInLast10Mins() {
    return authClient.accessToken()
        .flatMapMany(token -> searchClient.searchForAllInCategory(token, MOBILES_PHONES_CATEGORY_ID, Instant.now().minusSeconds(TIME_OFFSET)))
        .filter(sr -> sr.getSeller() != null && sr.getSeller().getFeedbackPercentage() > MIN_FEEDBACK_PERCENT && sr.getSeller().getFeedbackScore() > MIN_FEEDBACK_SCORE)
        .flatMap(sr -> authClient.accessToken().flatMap(token -> searchClient.getItem(token, sr.getItemId())))
        .map(itemMapper::toMobilePhone);
  }
}
