package user.reporting_service.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PriceStatsDto {
  private BigDecimal minPrice;
  private BigDecimal maxPrice;
  private BigDecimal avgPrice;
}
