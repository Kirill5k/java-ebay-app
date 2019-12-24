package io.kirill.ebayapp.mobilephone.clients.ebay;

import io.kirill.ebayapp.common.clients.ebay.EbayAuthClient;
import io.kirill.ebayapp.common.clients.ebay.EbayClient;
import io.kirill.ebayapp.common.clients.ebay.EbaySearchClient;
import io.kirill.ebayapp.common.clients.ebay.exceptions.EbayAuthError;
import io.kirill.ebayapp.mobilephone.MobilePhone;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class MobilePhoneEbayClient implements EbayClient {
  private static final int MOBILES_PHONES_CATEGORY_ID = 9355;

  private final static String DEFAULT_FILTER = "conditionIds:{1000|1500|2000|2500|3000|4000|5000}," +
      "deliveryCountry:GB," +
      "price:[39..800]," +
      "priceCurrency:GBP," +
      "itemLocationCountry:GB,";

  private final static String NEWLY_LISTED_FILTER = DEFAULT_FILTER + "buyingOptions:{FIXED_PRICE},itemStartDate:[%s]";
  private final static String ENDING_SOON_FILTER = DEFAULT_FILTER + "buyingOptions:{AUCTION},itemEndDate:[..%s]";

  private final EbayAuthClient authClient;
  private final EbaySearchClient searchClient;
  private final MobilePhoneMapper mobilePhoneMapper;

  public Flux<MobilePhone> getPhonesEndingSoon(int minutes) {
    var filter = searchFilter(ENDING_SOON_FILTER, Instant.now().plusSeconds(minutes * 60));
    return findPhones(filter);
  }

  public Flux<MobilePhone> getPhonesListedInTheLastMinutes(int minutes) {
    var filter = searchFilter(NEWLY_LISTED_FILTER, Instant.now().minusSeconds(minutes * 60));
    return findPhones(filter);
  }

  private Flux<MobilePhone> findPhones(String filter) {
    return authClient.accessToken()
        .flatMapMany(token -> searchClient.search(token, params(MOBILES_PHONES_CATEGORY_ID, filter)))
        .doOnError(e -> e instanceof EbayAuthError, e -> authClient.switchAccount())
        .filter(hasTrustedSeller)
        .filter(searchResult -> !ids.containsKey(searchResult.getItemId()))
        .flatMap(sr -> authClient.accessToken().flatMap(token -> searchClient.getItem(token, sr.getItemId())))
        .doOnNext(item -> ids.put(item.getItemId(), ""))
        .map(mobilePhoneMapper::map);
  }
}
