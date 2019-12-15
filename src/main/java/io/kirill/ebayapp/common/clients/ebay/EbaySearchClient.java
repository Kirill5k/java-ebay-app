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
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Component
public class EbaySearchClient {
  private static final String MARKET_PLACE_HEADER = "X-EBAY-C-MARKETPLACE-ID";
  private static final String GB_MARKET_PLACE = "EBAY_GB";

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

  public Flux<SearchResult> search(String accessToken, MultiValueMap<String, String> params) {
    return webClient
        .get()
        .uri(builder -> builder.path(searchPath)
            .queryParams(params)
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
