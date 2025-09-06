package user.reporting_service.exception;

import java.util.List;

public class ResourceNotFoundException extends RuntimeException {
    private final List<String> errors;

    public ResourceNotFoundException(String message, List<String> errors) {
        super(message);
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
