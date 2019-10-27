package io.kirill.ebayapp.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public class ClientException extends RuntimeException {
  private final HttpStatus status;
  private final String message;
}
