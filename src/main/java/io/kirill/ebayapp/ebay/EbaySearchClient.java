package io.kirill.ebayapp.ebay;

import io.kirill.ebayapp.configs.EbayConfig;
import io.kirill.ebayapp.ebay.models.SearchItem;
import io.kirill.ebayapp.ebay.models.SearchResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;

import static java.time.Instant.now;
import static java.time.temporal.ChronoField.MILLI_OF_SECOND;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
class EbaySearchClient {
  private static final long TIME_OFFSET = 15 * 60;

  private static final String MARKET_PLACE_HEADER = "X-EBAY-C-MARKETPLACE-ID";
  private static final String GB_MARKET_PLACE = "EBAY_GB";

  private static final String CATEGORY_IDS_QUERY = "category_ids";
  private static final int MOBILES_PHONES_CATEGORY_ID = 9355;

  private static final String FILTER_QUERY = "filter";
  private final static String DEFAULT_FILTER = "conditionIds:{1000|1500|2000|2500|3000|4000|5000}," +
      "buyingOptions:{FIXED_PRICE}," +
      "deliveryCountry:GB," +
      "price:[10..500]," +
      "priceCurrency:GBP," +
      "itemLocationCountry:GB," +
      "itemStartDate:[%s]";

  private final String searchPath;
  private final String itemPath;
  private final WebClient webClient;

  EbaySearchClient(WebClient.Builder webClientBuilder, EbayConfig ebayConfig) {
    this.searchPath = ebayConfig.getSearchPath();
    this.itemPath = ebayConfig.getItemPath();
    this.webClient = webClientBuilder
        .baseUrl(ebayConfig.getBaseUrl())
        .defaultHeaders(headers -> headers.setContentType(APPLICATION_JSON))
        .defaultHeaders(headers -> headers.setAccept(List.of(APPLICATION_JSON)))
        .defaultHeaders(headers -> headers.set(MARKET_PLACE_HEADER, GB_MARKET_PLACE))
        .build();
  }

  Flux<SearchItem> getAllMobilesPhonesPostedInTheLast15Mins(String accessToken) {
    return webClient
        .get()
        .uri(builder -> builder.path(searchPath)
            .queryParam(CATEGORY_IDS_QUERY, MOBILES_PHONES_CATEGORY_ID)
            .queryParam(FILTER_QUERY, String.format(DEFAULT_FILTER, now().with(MILLI_OF_SECOND, 0).minusSeconds(TIME_OFFSET)).replaceAll("\\{", "%7B").replaceAll("}", "%7D"))
            .build())
        .headers(headers -> headers.setBearerAuth(accessToken))
        .retrieve()
        .bodyToMono(SearchResponse.class)
        .map(SearchResponse::getItemSummaries)
        .flatMapMany(Flux::fromIterable);
  }
}
