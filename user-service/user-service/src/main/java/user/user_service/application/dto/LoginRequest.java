package user.user_service.application.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @Email
    @NotBlank(message = "email is required")
    private String email;

    @Size(min = 8)
    @NotBlank(message = "Password is required")
    private String password;
}
