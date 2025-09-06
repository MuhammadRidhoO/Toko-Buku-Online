package user.reporting_service.service;

import java.util.List;

import user.reporting_service.dto.BestsellerDto;
import user.reporting_service.dto.PriceStatsDto;
import user.reporting_service.dto.SalesReportDto;

public interface ReportService {
  SalesReportDto getSalesReport(String token);

  List<BestsellerDto> getBestseller(String token);

  PriceStatsDto getPriceStats(String token);
}