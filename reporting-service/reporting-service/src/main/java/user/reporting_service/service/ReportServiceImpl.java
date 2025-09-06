package user.reporting_service.service;

import lombok.RequiredArgsConstructor;
import user.reporting_service.dto.BestsellerDto;
import user.reporting_service.dto.BookSalesDto;
import user.reporting_service.dto.PriceStatsDto;
import user.reporting_service.dto.SalesReportDto;
import user.reporting_service.exception.ApiResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

  private final RestTemplate restTemplate = new RestTemplate();

  @Value("${order-service.url}")
  private String orderServiceUrl;

  @Value("${catalog-service.url}")
  private String catalogServiceUrl;

  private HttpEntity<Void> buildAuthEntity(String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    return new HttpEntity<>(headers);
  }

  @Override
  public SalesReportDto getSalesReport(String token) {
    ResponseEntity<ApiResponse<List<Map<String, Object>>>> resp = restTemplate.exchange(
        orderServiceUrl,
        HttpMethod.GET,
        buildAuthEntity(token),
        new ParameterizedTypeReference<ApiResponse<List<Map<String, Object>>>>() {
        });

    List<Map<String, Object>> orders = resp.getBody().getData();
    if (orders == null) {
      return new SalesReportDto(0, 0, Collections.emptyList());
    }

    long totalBooks = 0;
    double totalRevenue = 0;

    // rincian per buku
    Map<Long, BookSalesDto> bookSalesMap = new HashMap<>();

    for (Map<String, Object> o : orders) {
      Object statusObj = o.get("status");
      if (statusObj == null || !"PAID".equalsIgnoreCase(statusObj.toString())) {
        continue;
      }

      totalRevenue += Double.parseDouble(o.get("totalPrice").toString());

      List<Map<String, Object>> items = (List<Map<String, Object>>) o.get("items");
      for (Map<String, Object> it : items) {
        long bookId = Long.parseLong(it.get("bookId").toString());
        int qty = Integer.parseInt(it.get("quantity").toString());
        double price = Double.parseDouble(it.get("price").toString());

        totalBooks += qty;
        double revenue = price * qty;

        // update rincian per buku
        BookSalesDto bookSales = bookSalesMap.getOrDefault(bookId, new BookSalesDto(bookId, 0, 0.0));
        bookSales.setTotalQuantity(bookSales.getTotalQuantity() + qty);
        bookSales.setTotalRevenue(bookSales.getTotalRevenue() + revenue);
        bookSalesMap.put(bookId, bookSales);
      }
    }

    return new SalesReportDto(totalBooks, totalRevenue, new ArrayList<>(bookSalesMap.values()));
  }

  @Override
  public List<BestsellerDto> getBestseller(String token) {
    ResponseEntity<ApiResponse<List<Map<String, Object>>>> resp = restTemplate.exchange(
        orderServiceUrl,
        HttpMethod.GET,
        buildAuthEntity(token),
        new ParameterizedTypeReference<ApiResponse<List<Map<String, Object>>>>() {
        });

    List<Map<String, Object>> orders = resp.getBody().getData();
    if (orders == null)
      return Collections.emptyList();

    Map<Long, Long> soldCount = new HashMap<>();

    for (Map<String, Object> o : orders) {
      String status = o.get("status").toString();
      if (!"PAID".equalsIgnoreCase(status)) {
        continue;
      }

      List<Map<String, Object>> items = (List<Map<String, Object>>) o.get("items");
      for (Map<String, Object> it : items) {
        Long bookId = Long.parseLong(it.get("bookId").toString());
        Long qty = Long.parseLong(it.get("quantity").toString());
        soldCount.merge(bookId, qty, Long::sum);
      }
    }

    return soldCount.entrySet().stream()
        .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
        .limit(3)
        .map(e -> {
          String title = getBookTitle(e.getKey(), token);
          return new BestsellerDto(e.getKey(), title, e.getValue());
        })
        .collect(Collectors.toList());
  }

  private String getBookTitle(Long bookId, String token) {
    ResponseEntity<Map> resp = restTemplate.exchange(
        catalogServiceUrl + "/" + bookId,
        HttpMethod.GET,
        buildAuthEntity(token),
        Map.class);
    if (resp.getBody() != null && resp.getBody().get("title") != null) {
      return resp.getBody().get("title").toString();
    }
    return "Unknown";
  }

  @Override
  public PriceStatsDto getPriceStats(String token) {
    ResponseEntity<Map<String, Object>> resp = restTemplate.exchange(
        catalogServiceUrl,
        HttpMethod.GET,
        buildAuthEntity(token),
        new ParameterizedTypeReference<Map<String, Object>>() {
        });

    Map<String, Object> body = resp.getBody();

    List<Map<String, Object>> books = (List<Map<String, Object>>) body.get("content");

    BigDecimal max = BigDecimal.ZERO;
    BigDecimal min = books.isEmpty() ? BigDecimal.ZERO : new BigDecimal(Double.MAX_VALUE);
    BigDecimal sum = BigDecimal.ZERO;

    for (Map<String, Object> b : books) {
      BigDecimal price = new BigDecimal(b.get("price").toString());
      sum = sum.add(price);
      if (price.compareTo(max) > 0)
        max = price;
      if (price.compareTo(min) < 0)
        min = price;
    }
    BigDecimal avg = books.isEmpty() ? BigDecimal.ZERO
        : sum.divide(BigDecimal.valueOf(books.size()), 2, RoundingMode.HALF_UP);

    return new PriceStatsDto(max, min, avg);
  }
}
