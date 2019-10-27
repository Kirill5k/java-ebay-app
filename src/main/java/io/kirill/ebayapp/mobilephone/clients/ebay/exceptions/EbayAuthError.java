package io.kirill.ebayapp.mobilephone.clients.ebay.exceptions;

import io.kirill.ebayapp.common.exception.ClientException;
import org.springframework.http.HttpStatus;

public class EbayAuthError extends ClientException {

  public EbayAuthError(HttpStatus status, String message) {
    super(status, String.format("error authenticating with ebay: %s", message));
  }
}
