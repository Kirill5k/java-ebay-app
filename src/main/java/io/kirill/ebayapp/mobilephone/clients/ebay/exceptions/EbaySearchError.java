package io.kirill.ebayapp.mobilephone.clients.ebay.exceptions;

import io.kirill.ebayapp.common.exception.ClientException;
import org.springframework.http.HttpStatus;

public class EbaySearchError extends ClientException {

  public EbaySearchError(HttpStatus status, String message) {
    super(status, String.format("error sending search req to ebay: %s", message));
  }
}
