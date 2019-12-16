package io.kirill.ebayapp.common.domain;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.With;

@Value
@With
@Builder
@RequiredArgsConstructor
public class ResellPrice {
  private final BigDecimal cash;
  private final BigDecimal exchange;
}
