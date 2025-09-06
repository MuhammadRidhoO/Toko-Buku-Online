package user.catalog_service.infrastructure.exception;

import java.util.List;

public class BadRequestException extends RuntimeException {
  private final List<String> errors;

  public BadRequestException(String message, List<String> errors) {
    super(message);
    this.errors = errors;
  }

  public List<String> getErrors() {
    return errors;
  }
}