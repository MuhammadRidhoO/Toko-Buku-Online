package user.reporting_service.exception;

import java.util.*;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponse<T> {
  private boolean success;
  private String message;
  private T data;
  private List<String> errors;

  public static <T> ApiResponse<T> success(String message, T data) {
    return ApiResponse.<T>builder()
        .success(true)
        .message(message)
        .data(data)
        .errors(List.of())
        .build();
  }

  public static <T> ApiResponse<T> failure(String message, List<String> errors) {
    return ApiResponse.<T>builder()
        .success(false)
        .message(message)
        .data(null)
        .errors(errors)
        .build();
  }
}
