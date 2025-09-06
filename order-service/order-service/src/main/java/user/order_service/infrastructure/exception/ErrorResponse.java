package user.order_service.infrastructure.exception;

import lombok.*;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private boolean success = false;
    private String message;
    private List<String> errors;
}
