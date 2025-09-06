package user.order_service.application.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
  private Long id;
  private String userEmail;
  private BigDecimal totalPrice;
  private String status;
  private LocalDateTime createdAt;
  private List<OrderItemDto> items;
}
