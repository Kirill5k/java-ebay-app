package io.kirill.ebayapp.mobilephone.clients.cex;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import io.kirill.ebayapp.common.configs.CexConfig;
import io.kirill.ebayapp.mobilephone.MobilePhone;
import io.kirill.ebayapp.mobilephone.clients.cex.exceptions.CexSearchError;
import io.kirill.ebayapp.mobilephone.clients.cex.models.SearchData;
import io.kirill.ebayapp.mobilephone.clients.cex.models.SearchErrorResponse;
import io.kirill.ebayapp.mobilephone.clients.cex.models.SearchErrorResponseWrapper;
import io.kirill.ebayapp.mobilephone.clients.cex.models.SearchError;
import io.kirill.ebayapp.mobilephone.clients.cex.models.SearchResponseWrapper;
import io.kirill.ebayapp.mobilephone.clients.cex.models.SearchResult;
import java.math.BigDecimal;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.function.Function;

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

  public Mono<BigDecimal> getAveragePrice(MobilePhone phone) {
    var query = phone.fullName();
    return webClient
        .get()
        .uri(builder -> builder.queryParam("q", query).build())
        .retrieve()
        .onStatus(HttpStatus::isError, mapToError)
        .bodyToMono(SearchResponseWrapper.class)
        .map(respWrapper -> ofNullable(respWrapper.getResponse().getData()).map(SearchData::getBoxes).orElse(emptyList()))
        .doOnNext(results -> log.info("query \"{}\" returned {} results", query, results.size()))
        .filter(results -> !results.isEmpty())
        .map(results -> results.stream().mapToDouble(SearchResult::getExchangePrice).average().getAsDouble())
        .map(Math::floor)
        .map(BigDecimal::valueOf);
  }

  private Function<ClientResponse, Mono<? extends Throwable>> mapToError = r -> r.bodyToMono(SearchErrorResponseWrapper.class)
      .map(SearchErrorResponseWrapper::getResponse)
      .map(SearchErrorResponse::getError)
      .map(SearchError::getMessage)
      .map(m -> new CexSearchError(r.statusCode(), m));
}
