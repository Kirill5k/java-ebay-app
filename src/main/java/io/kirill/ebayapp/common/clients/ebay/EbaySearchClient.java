package io.kirill.ebayapp.common.clients.ebay;

import io.kirill.ebayapp.common.clients.ebay.exceptions.EbaySearchError;
import io.kirill.ebayapp.common.clients.ebay.models.item.Item;
import io.kirill.ebayapp.common.clients.ebay.models.search.SearchError;
import io.kirill.ebayapp.common.clients.ebay.models.search.SearchErrorResponse;
import io.kirill.ebayapp.common.clients.ebay.models.search.SearchResponse;
import io.kirill.ebayapp.common.clients.ebay.models.search.SearchResult;
import io.kirill.ebayapp.common.configs.EbayConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.function.Function;

import static java.time.temporal.ChronoField.MILLI_OF_SECOND;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Component
public class EbaySearchClient {
  private static final String MARKET_PLACE_HEADER = "X-EBAY-C-MARKETPLACE-ID";
  private static final String GB_MARKET_PLACE = "EBAY_GB";

  private static final String CATEGORY_IDS_QUERY = "category_ids";

  private static final String FILTER_QUERY = "filter";
  private final static String DEFAULT_FILTER = "conditionIds:{1000|1500|2000|2500|3000|4000|5000}," +
      "deliveryCountry:GB," +
      "price:[39..800]," +
      "priceCurrency:GBP," +
      "itemLocationCountry:GB,";

  private final static String NEWLY_LISTED_FILTER = DEFAULT_FILTER + "buyingOptions:{FIXED_PRICE},itemStartDate:[%s]";
  private final static String ENDING_SOON_FILTER = DEFAULT_FILTER + "buyingOptions:{AUCTION},itemEndDate:[%s]";

  private final String searchPath;
  private final String itemPath;
  private final WebClient webClient;

  public EbaySearchClient(WebClient.Builder webClientBuilder, EbayConfig ebayConfig) {
    this.searchPath = ebayConfig.getSearchPath();
    this.itemPath = ebayConfig.getItemPath();
    this.webClient = webClientBuilder
        .baseUrl(ebayConfig.getBaseUrl())
        .defaultHeaders(headers -> headers.setContentType(APPLICATION_JSON))
        .defaultHeaders(headers -> headers.setAccept(List.of(APPLICATION_JSON)))
        .defaultHeaders(headers -> headers.set(MARKET_PLACE_HEADER, GB_MARKET_PLACE))
        .build();
  }

  public Flux<SearchResult> searchForNewestInCategoryFrom(String accessToken, int categoryId, Instant startingTime) {
    var filter = searchFilter(NEWLY_LISTED_FILTER, startingTime);
    return search(accessToken, categoryId, filter);
  }

  private Flux<SearchResult> search(String accessToken, int categoryId, String filter) {
    return webClient
        .get()
        .uri(builder -> builder.path(searchPath)
            .queryParam(CATEGORY_IDS_QUERY, categoryId)
            .queryParam(FILTER_QUERY, filter)
            .build())
        .headers(headers -> headers.setBearerAuth(accessToken))
        .retrieve()
        .onStatus(HttpStatus::isError, mapErrorResponse)
        .bodyToMono(SearchResponse.class)
        .doOnNext(searchResponse -> log.info("search returned {} items", searchResponse.getTotal()))
        .filter(searchResponse -> searchResponse != null && searchResponse.getItemSummaries() != null)
        .map(SearchResponse::getItemSummaries)
        .flatMapMany(Flux::fromIterable);
  }

  private String searchFilter(String filter, Instant time) {
    return String.format(filter, time.with(MILLI_OF_SECOND, 0))
        .replaceAll("\\{", "%7B")
        .replaceAll("}", "%7D");
  }

  public Mono<Item> getItem(String accessToken, String itemId) {
    return webClient
        .get()
        .uri(builder -> builder.path(itemPath).pathSegment(itemId).build())
        .headers(headers -> headers.setBearerAuth(accessToken))
        .retrieve()
        .onStatus(HttpStatus::isError, mapErrorResponse)
        .bodyToMono(Item.class);
  }

  private Function<ClientResponse, Mono<? extends Throwable>> mapErrorResponse = r -> r.bodyToMono(SearchErrorResponse.class)
        .map(e -> e.getErrors().stream().findFirst().map(SearchError::getLongMessage).orElse(r.toString()))
        .map(e -> new EbaySearchError(r.statusCode(), e));
}