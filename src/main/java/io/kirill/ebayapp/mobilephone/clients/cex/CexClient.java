package io.kirill.ebayapp.mobilephone.clients.cex;

import io.kirill.ebayapp.common.configs.CexConfig;
import io.kirill.ebayapp.mobilephone.MobilePhone;
import io.kirill.ebayapp.mobilephone.clients.cex.exceptions.CexSearchError;
import io.kirill.ebayapp.mobilephone.clients.cex.models.SearchError;
import io.kirill.ebayapp.mobilephone.clients.cex.models.SearchResponse;
import io.kirill.ebayapp.mobilephone.clients.cex.models.SearchResponseWrapper;
import io.kirill.ebayapp.mobilephone.clients.cex.models.SearchResult;
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
        .build();
  }

  public Mono<Double> getAveragePrice(MobilePhone phone) {
    var query = phone.fullName();
    log.info("getting price for {}", query);
    return webClient
        .get()
        .uri(builder -> builder.queryParam("q", query).build())
        .retrieve()
        .onStatus(HttpStatus::isError, mapToError)
        .bodyToMono(SearchResponseWrapper.class)
        .map(respWrapper -> respWrapper.getResponse().getData().getResults())
        .doOnNext(results -> log.info("query \"{}\" returned {} results", query, results.size()))
        .map(results -> results.stream().mapToDouble(SearchResult::getExchangePrice).average().orElse(Double.MAX_VALUE));
  }

  private Function<ClientResponse, Mono<? extends Throwable>> mapToError = r -> r.bodyToMono(SearchResponseWrapper.class)
      .map(SearchResponseWrapper::getResponse)
      .map(SearchResponse::getError)
      .map(SearchError::getMessage)
      .map(m -> new CexSearchError(r.statusCode(), m));
}
