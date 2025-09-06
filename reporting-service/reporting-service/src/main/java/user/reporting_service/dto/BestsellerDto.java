package user.reporting_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BestsellerDto {
  private Long bookId;
  private String title;
  private long totalSold;
}