package user.reporting_service.controller;

import lombok.RequiredArgsConstructor;
import user.reporting_service.dto.BestsellerDto;
import user.reporting_service.dto.PriceStatsDto;
import user.reporting_service.dto.SalesReportDto;
import user.reporting_service.exception.ApiResponse;
import user.reporting_service.service.ReportService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReportController {

  private final ReportService reportService;

  @GetMapping("/reports/sales")
  public ResponseEntity<ApiResponse<SalesReportDto>> getSales(@RequestHeader("Authorization") String authHeader) {
    String token = authHeader.replace("Bearer ", "");
    return ResponseEntity.ok(ApiResponse.success("Sales Report", reportService.getSalesReport(token)));
  }

  @GetMapping("/reports/bestseller")
  public ResponseEntity<ApiResponse<List<BestsellerDto>>> getBestseller(
      @RequestHeader("Authorization") String authHeader) {
    String token = authHeader.replace("Bearer ", "");
    return ResponseEntity.ok(ApiResponse.success("Bestseller Books", reportService.getBestseller(token)));
  }

  @GetMapping("/reports/prices")
  public ResponseEntity<ApiResponse<PriceStatsDto>> getPrices(@RequestHeader("Authorization") String authHeader) {
    String token = authHeader.replace("Bearer ", "");
    return ResponseEntity.ok(ApiResponse.success("Book Price Statistics", reportService.getPriceStats(token)));
  }
}