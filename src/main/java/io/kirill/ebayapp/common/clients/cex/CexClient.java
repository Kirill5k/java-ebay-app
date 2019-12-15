package io.kirill.ebayapp.common.clients.cex;

import io.kirill.ebayapp.common.clients.cex.exceptions.CexSearchError;
import io.kirill.ebayapp.common.clients.cex.models.SearchData;
import io.kirill.ebayapp.common.clients.cex.models.SearchError;
import io.kirill.ebayapp.common.clients.cex.models.SearchErrorResponse;
import io.kirill.ebayapp.common.clients.cex.models.SearchErrorResponseWrapper;
import io.kirill.ebayapp.common.clients.cex.models.SearchResponseWrapper;
import io.kirill.ebayapp.common.clients.cex.models.SearchResult;
import io.kirill.ebayapp.common.configs.CexConfig;
import io.kirill.ebayapp.common.domain.PriceQuery;
import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static net.jodah.expiringmap.ExpiringMap.ExpirationPolicy.CREATED;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Component
public class CexClient {

  private final WebClient webClient;

  private Map<String, BigDecimal> searchResults = ExpiringMap.builder()
      .expirationPolicy(CREATED)
      .expiration(24, TimeUnit.HOURS)
      .build();

  public CexClient(WebClient.Builder webClientBuilder, CexConfig cexConfig) {
    this.webClient = webClientBuilder
        .baseUrl(cexConfig.getBaseUrl() + cexConfig.getSearchPath())
        .defaultHeaders(headers -> headers.setContentType(APPLICATION_JSON))
        .defaultHeaders(headers -> headers.setAccept(List.of(APPLICATION_JSON)))
        .build();
  }

  public <T> Mono<BigDecimal> getMinResellPrice(PriceQuery<T> priceQuery) {
    var query = priceQuery.queryString();
    if (!priceQuery.isSearchable()) {
      log.warn("not enough details to query for search price: {}", query);
      return Mono.empty();
    }
    if (searchResults.containsKey(query)) {
      log.info("found query \"{}\" in cache (£{})", query, priceQuery.originalPrice());
      return Mono.just(searchResults.get(query));
    }
    return webClient
        .get()
        .uri(builder -> builder.queryParam("q", query).build())
        .retrieve()
        .onStatus(HttpStatus::isError, mapToError)
        .onStatus(status -> status == TOO_MANY_REQUESTS, res -> Mono.error(new CexSearchError(TOO_MANY_REQUESTS, "too many requests")))
        .bodyToMono(SearchResponseWrapper.class)
        .map(SearchResponseWrapper::getResponse)
        .map(response -> ofNullable(response.getData()).map(SearchData::getBoxes).orElse(emptyList()))
        .doOnNext(results -> log.info("query \"{}\" (£{}) returned {} results", query, priceQuery.originalPrice(), results.size()))
        .filter(results -> !results.isEmpty())
        .map(results -> results.stream().mapToDouble(SearchResult::getExchangePrice).min().getAsDouble())
        .map(Math::floor)
        .map(BigDecimal::valueOf);
  }

  private Function<ClientResponse, Mono<? extends Throwable>> mapToError = r -> r.bodyToMono(SearchErrorResponseWrapper.class)
      .map(SearchErrorResponseWrapper::getResponse)
      .map(SearchErrorResponse::getError)
      .map(SearchError::getMessage)
      .map(m -> new CexSearchError(r.statusCode(), m));
}
