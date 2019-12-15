package io.kirill.ebayapp.mobilephone.clients.cex;

import io.kirill.ebayapp.common.configs.CexConfig;
import io.kirill.ebayapp.common.domain.PriceQuery;
import io.kirill.ebayapp.mobilephone.clients.cex.exceptions.CexSearchError;
import io.kirill.ebayapp.mobilephone.clients.cex.models.SearchData;
import io.kirill.ebayapp.mobilephone.clients.cex.models.SearchError;
import io.kirill.ebayapp.mobilephone.clients.cex.models.SearchErrorResponse;
import io.kirill.ebayapp.mobilephone.clients.cex.models.SearchErrorResponseWrapper;
import io.kirill.ebayapp.mobilephone.clients.cex.models.SearchResponseWrapper;
import io.kirill.ebayapp.mobilephone.clients.cex.models.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Component
public class CexClient {

  private final WebClient webClient;

  CexClient(WebClient.Builder webClientBuilder, CexConfig cexConfig) {
    this.webClient = webClientBuilder
        .baseUrl(cexConfig.getBaseUrl() + cexConfig.getSearchPath())
        .defaultHeaders(headers -> headers.setContentType(APPLICATION_JSON))
        .defaultHeaders(headers -> headers.setAccept(List.of(APPLICATION_JSON)))
        .build();
  }

  public <T> Mono<BigDecimal> getMinPrice(PriceQuery<T> priceQuery) {
    var query = priceQuery.queryString();
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
