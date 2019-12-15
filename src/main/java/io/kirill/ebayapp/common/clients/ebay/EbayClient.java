package io.kirill.ebayapp.common.clients.ebay;

import io.kirill.ebayapp.common.clients.ebay.models.search.SearchResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.Instant;
import java.util.function.Predicate;

import static java.time.temporal.ChronoField.MILLI_OF_SECOND;

public interface EbayClient {
  int MIN_FEEDBACK_SCORE = 6;
  double MIN_FEEDBACK_PERCENT = 90;

  default MultiValueMap<String, String> params(int categoryId, String filter) {
    var params = new LinkedMultiValueMap<String, String>();
    params.add("category_ids", Integer.toString(categoryId));
    params.add("filter", filter);
    params.add("limit", "200");
    return params;
  }

  default MultiValueMap<String, String> paramsWithQuery(int categoryId, String filter, String query) {
    var params = params(categoryId, filter);
    params.add("q", query);
    return params;
  }

  default String searchFilter(String filter, Instant time) {
    return String.format(filter, time.with(MILLI_OF_SECOND, 0))
        .replaceAll("\\{", "%7B")
        .replaceAll("}", "%7D");
  }

  Predicate<SearchResult> hasTrustedSeller = searchResult -> searchResult.getSeller() != null &&
      searchResult.getSeller().getFeedbackPercentage() != null &&
      searchResult.getSeller().getFeedbackScore() != null &&
      searchResult.getSeller().getFeedbackPercentage() > MIN_FEEDBACK_PERCENT &&
      searchResult.getSeller().getFeedbackScore() > MIN_FEEDBACK_SCORE;
}
