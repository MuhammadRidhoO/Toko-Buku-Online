package user.reporting_service.dto;

import java.util.List;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalesReportDto {
  private long totalBooksSold;
  private double totalRevenue;
  private List<BookSalesDto> details;
}