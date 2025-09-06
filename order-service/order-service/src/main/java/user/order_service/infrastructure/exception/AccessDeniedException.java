package user.order_service.infrastructure.exception;

import java.util.List;

public class AccessDeniedException extends RuntimeException {
  private final List<String> errors;

  public AccessDeniedException(String message, List<String> errors) {
    super(message);
    this.errors = errors;
  }

  public List<String> getErrors() {
    return errors;
  }
}
