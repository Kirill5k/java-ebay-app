package io.kirill.ebayapp.common.clients.ebay.models.item;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@Builder
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShippingOption {
  private final String shippingServiceCode;
  private final String type;
  private final ShippingCost shippingCost;
}
