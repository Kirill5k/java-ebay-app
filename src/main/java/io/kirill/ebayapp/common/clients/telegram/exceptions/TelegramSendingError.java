package io.kirill.ebayapp.common.clients.telegram.exceptions;

import io.kirill.ebayapp.common.exception.ClientException;
import org.springframework.http.HttpStatus;

public class TelegramSendingError extends ClientException {

  public TelegramSendingError(HttpStatus status, String message) {
    super(status, String.format("error sending message to telegram: %s", message));
  }
}
