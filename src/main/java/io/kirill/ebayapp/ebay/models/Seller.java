package io.kirill.ebayapp.ebay.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Seller {
  private final String username;
  private final Double feedbackPercentage;
  private final Double feedbackScore;
  private final String sellerAccountType;
}
