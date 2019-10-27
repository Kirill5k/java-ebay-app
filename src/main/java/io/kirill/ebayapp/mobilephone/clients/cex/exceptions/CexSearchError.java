package io.kirill.ebayapp.mobilephone.clients.cex.exceptions;

import io.kirill.ebayapp.common.exception.ClientException;
import org.springframework.http.HttpStatus;

public class CexSearchError extends ClientException {

  public CexSearchError(HttpStatus status, String message) {
    super(status, String.format("error sending search req to cex: %s", message));
  }
}
