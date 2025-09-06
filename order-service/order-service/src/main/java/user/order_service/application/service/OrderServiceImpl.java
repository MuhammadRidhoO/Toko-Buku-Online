package user.order_service.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import user.order_service.application.dto.CreateOrderRequest;
import user.order_service.application.dto.OrderItemDto;
import user.order_service.application.dto.OrderResponse;
import user.order_service.domain.model.Order;
import user.order_service.domain.model.OrderItem;
import user.order_service.domain.repository.OrderRepository;
import user.order_service.infrastructure.exception.AccessDeniedException;
import user.order_service.infrastructure.exception.ApiResponse;
import user.order_service.infrastructure.exception.BadRequestException;
import user.order_service.infrastructure.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

  private final OrderRepository orderRepo;
  private final RestTemplate restTemplate;

  @Value("${catalog.base-url}")
  private String catalogBaseUrl;

  public OrderServiceImpl(OrderRepository orderRepo, RestTemplate restTemplate) {
    this.orderRepo = orderRepo;
    this.restTemplate = restTemplate;
  }

  @Override
  @Transactional
  public OrderResponse createOrder(String userEmail, CreateOrderRequest request, String token) {

    BigDecimal total = BigDecimal.ZERO;
    List<OrderItem> items = new ArrayList<>();
    Map<Long, Integer> reservedMap = new HashMap<>();
    List<String> errorMessages = new ArrayList<>();

    try {
      for (OrderItemDto it : request.getItems()) {
        String bookUrl = catalogBaseUrl + "/books/" + it.getBookId();

        try {
          HttpEntity<Void> getEntity = buildAuthEntity(token);
          ResponseEntity<ApiResponse<Map<String, Object>>> resp = restTemplate.exchange(bookUrl, HttpMethod.GET,
              getEntity,
              new ParameterizedTypeReference<ApiResponse<Map<String, Object>>>() {
              });

          ApiResponse<Map<String, Object>> apiResponse = resp.getBody();
          if (apiResponse == null || apiResponse.getData() == null) {
            errorMessages.add("Book not found with id " + it.getBookId());
            continue;
          }

          Map<String, Object> body = apiResponse.getData();

          BigDecimal price = new BigDecimal(body.get("price").toString());
          int stock = Integer.parseInt(body.get("stock").toString());

          if (stock < it.getQuantity()) {
            errorMessages.add("Not enough stock for book id " + it.getBookId());
            continue;
          }

          BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(it.getQuantity()));
          total = total.add(itemTotal);

          OrderItem oi = OrderItem.builder()
              .bookId(it.getBookId())
              .quantity(it.getQuantity())
              .price(price)
              .build();
          items.add(oi);

        } catch (HttpClientErrorException.NotFound ex) {

          errorMessages.add("Book not found with id " + it.getBookId());
        } catch (RestClientException ex) {

          errorMessages.add("Failed to fetch book " + it.getBookId());
        }
      }

      if (!errorMessages.isEmpty()) {
        throw new ResourceNotFoundException("Validation failed", errorMessages);
      }

      for (OrderItem oi : items) {
        String decUrl = catalogBaseUrl + "/books/" + oi.getBookId() + "/decrement?qty=" + oi.getQuantity();
        HttpEntity<Void> postEntity = buildAuthEntity(token);
        ResponseEntity<ApiResponse<Void>> decResp = restTemplate.exchange(decUrl, HttpMethod.POST, postEntity,
            new ParameterizedTypeReference<ApiResponse<Void>>() {
            });

        if (!decResp.getStatusCode().is2xxSuccessful()) {
          errorMessages.add("Failed to reserve stock for book id " + oi.getBookId());
        } else {
          reservedMap.merge(oi.getBookId(), oi.getQuantity(), Integer::sum);
        }
      }

      if (!errorMessages.isEmpty()) {
        rollbackReserved(reservedMap, token);
        throw new ResourceNotFoundException("Failed to reserve stock", errorMessages);
      }

      Order order = Order.builder()
          .userEmail(userEmail)
          .totalPrice(total)
          .status("PENDING")
          .createdAt(LocalDateTime.now())
          .items(items)
          .build();

      Order saved = orderRepo.save(order);
      return toResponse(saved);

    } catch (RuntimeException ex) {
      if (!reservedMap.isEmpty()) {
        rollbackReserved(reservedMap, token);
      }
      throw ex;
    }
  }

  @Override
  @Transactional
  public OrderResponse payOrder(Long orderId, String payerEmail) {
    Order order = orderRepo.findById(orderId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Order not found with id: " + orderId, List.of("ORDER_NOT_FOUND")));

    if (!order.getUserEmail().equalsIgnoreCase(payerEmail)) {
      throw new BadRequestException("Invalid credentials", List.of("User email does not match order owner"));
    }

    if ("PAID".equalsIgnoreCase(order.getStatus())) {
      return toResponse(order);
    }
    if ("CANCELLED".equalsIgnoreCase(order.getStatus())) {
      throw new BadRequestException("Failed pay order", List.of("Cannot pay a cancelled order"));
    }

    order.setStatus("PAID");
    Order saved = orderRepo.save(order);
    return toResponse(saved);
  }

  @Override
  @Transactional
  public OrderResponse cancelOrder(Long orderId, String cancelledByEmail, String token) {
    Order order = orderRepo.findById(orderId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Order not found with id: " + orderId, List.of("ORDER_NOT_FOUND")));

    if (!order.getUserEmail().equalsIgnoreCase(cancelledByEmail)) {
      throw new BadRequestException("Invalid credentials",
          List.of("Only order owner can cancel the order"));
    }

    if ("CANCELLED".equalsIgnoreCase(order.getStatus())) {
      return toResponse(order);
    }

    if ("PAID".equalsIgnoreCase(order.getStatus())) {
      throw new BadRequestException("Failed cancel order", List.of("Cannot cancel a paid order"));
    }

    if ("PENDING".equalsIgnoreCase(order.getStatus())) {
      Map<Long, Integer> incMap = new HashMap<>();
      for (OrderItem item : order.getItems()) {
        incMap.merge(item.getBookId(), item.getQuantity(), Integer::sum);
      }
      rollbackReserved(incMap, token);
    }

    order.setStatus("CANCELLED");
    Order saved = orderRepo.save(order);
    return toResponse(saved);
  }

  @Override
  public List<OrderResponse> listOrders(String requesterEmail, boolean isAdmin) {
    List<Order> list = isAdmin ? orderRepo.findAll() : orderRepo.findByUserEmail(requesterEmail);
    return list.stream().map(this::toResponse).collect(Collectors.toList());
  }

  @Override
  public OrderResponse getOrder(Long orderId, String requesterEmail, boolean isAdmin) {
    Order order = orderRepo.findById(orderId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Order not found with id: " + orderId, List.of("ORDER_NOT_FOUND")));
    if (!isAdmin && !order.getUserEmail().equalsIgnoreCase(requesterEmail)) {
      throw new AccessDeniedException("Invalid credentials", List.of("Access denied"));
    }
    return toResponse(order);
  }

  private HttpEntity<Void> buildAuthEntity(String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);
    return new HttpEntity<>(headers);
  }

  private void rollbackReserved(Map<Long, Integer> map, String token) {
    for (Map.Entry<Long, Integer> e : map.entrySet()) {
      Long bookId = e.getKey();
      Integer qty = e.getValue();
      String incUrl = catalogBaseUrl + "/books/" + bookId + "/increment?qty=" + qty;
      try {
        HttpEntity<Void> entity = buildAuthEntity(token);
        restTemplate.exchange(incUrl, HttpMethod.POST, entity, Map.class);
      } catch (Exception ex) {
        System.err.println("Failed to rollback increment for book " + bookId + " qty=" + qty + " : " + ex.getMessage());
      }
    }
  }

  private OrderResponse toResponse(Order o) {
    List<OrderItemDto> items = o.getItems().stream()
        .map(i -> new OrderItemDto(i.getBookId(), i.getQuantity(), i.getPrice()))
        .collect(Collectors.toList());

    return OrderResponse.builder()
        .id(o.getId())
        .userEmail(o.getUserEmail())
        .totalPrice(o.getTotalPrice())
        .status(o.getStatus())
        .createdAt(o.getCreatedAt())
        .items(items)
        .build();
  }
}
