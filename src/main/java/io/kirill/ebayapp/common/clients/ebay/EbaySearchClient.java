package io.kirill.ebayapp.common.clients.ebay;

import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import io.kirill.ebayapp.common.clients.ebay.exceptions.EbayAuthError;
import io.kirill.ebayapp.common.clients.ebay.exceptions.EbaySearchError;
import io.kirill.ebayapp.common.clients.ebay.models.item.Item;
import io.kirill.ebayapp.common.clients.ebay.models.search.SearchError;
import io.kirill.ebayapp.common.clients.ebay.models.search.SearchErrorResponse;
import io.kirill.ebayapp.common.clients.ebay.models.search.SearchResponse;
import io.kirill.ebayapp.common.clients.ebay.models.search.SearchResult;
import io.kirill.ebayapp.common.configs.EbayConfig;
import java.util.List;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Slf4j
@Component
public class EbaySearchClient {
  private static final String MARKET_PLACE_HEADER = "X-EBAY-C-MARKETPLACE-ID";
  private static final String GB_MARKET_PLACE = "EBAY_GB";

  private final String searchPath;
  private final String itemPath;
  private final WebClient webClient;

  public EbaySearchClient(WebClient.Builder webClientBuilder, EbayConfig ebayConfig) {
    searchPath = ebayConfig.getSearchPath();
    itemPath = ebayConfig.getItemPath();
    webClient = webClientBuilder
        .baseUrl(ebayConfig.getBaseUrl())
        .clientConnector(new ReactorClientHttpConnector(HttpClient.newConnection().compress(true)))
        .defaultHeaders(headers -> headers.setContentType(APPLICATION_JSON))
        .defaultHeaders(headers -> headers.setAccept(List.of(APPLICATION_JSON)))
        .defaultHeaders(headers -> headers.set(MARKET_PLACE_HEADER, GB_MARKET_PLACE))
        .build();
  }

  public Flux<SearchResult> search(String accessToken, MultiValueMap<String, String> params) {
    return webClient
        .get()
        .uri(builder -> builder.path(searchPath).queryParams(params).build())
        .headers(headers -> headers.setBearerAuth(accessToken))
        .retrieve()
        .onStatus(HttpStatus::isError, mapErrorResponse)
        .onStatus(status -> status == TOO_MANY_REQUESTS, res -> Mono.error(new EbayAuthError(TOO_MANY_REQUESTS, "exceeded api limits")))
        .bodyToMono(SearchResponse.class)
        .doOnNext(searchResponse -> log.info("search {} returned {} items", ofNullable(params.getFirst("q")).orElse(""), searchResponse.getTotal()))
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
        .onStatus(status -> status == NOT_FOUND, res -> Mono.empty())
        .onStatus(status -> status == TOO_MANY_REQUESTS, res -> Mono.error(new EbayAuthError(TOO_MANY_REQUESTS, "exceeded api limits")))
        .bodyToMono(Item.class)
        .filter(item -> item.getItemId() != null)
        .doOnError(error -> log.error("error getting item {} details from ebay: {}", itemId, error.getMessage()));
  }

  private Function<ClientResponse, Mono<? extends Throwable>> mapErrorResponse = r -> r.bodyToMono(SearchErrorResponse.class)
        .map(e -> e.getErrors().stream().findFirst().map(SearchError::getMessage).orElse(r.toString()))
        .map(e -> new EbaySearchError(r.statusCode(), e));
}
