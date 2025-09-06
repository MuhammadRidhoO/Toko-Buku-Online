package user.reporting_service.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookSalesDto {
    private Long bookId;
    private long totalQuantity;
    private double totalRevenue;
}