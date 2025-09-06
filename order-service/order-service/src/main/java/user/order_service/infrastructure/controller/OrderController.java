package user.order_service.infrastructure.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import user.order_service.application.dto.CreateOrderRequest;
import user.order_service.application.dto.OrderResponse;
import user.order_service.application.service.OrderService;
import user.order_service.infrastructure.config.security.JwtUtil;
import user.order_service.infrastructure.exception.ApiResponse;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class OrderController {

    private final OrderService orderService;
    private final JwtUtil jwtUtil;

    public OrderController(OrderService orderService, JwtUtil jwtUtil) {
        this.orderService = orderService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @RequestHeader("Authorization") String tokenHeader) {

        String token = stripBearer(tokenHeader);
        String email = jwtUtil.extractEmail(token);

        OrderResponse response = orderService.createOrder(email, request, token);

        return ResponseEntity.ok(ApiResponse.success("Order created successfully", response));

    }

    @PostMapping("/orders/{id}/pay")
    public ResponseEntity<?> payOrder(
            @PathVariable Long id,
            @RequestHeader("Authorization") String tokenHeader) {

        String token = stripBearer(tokenHeader);
        String email = jwtUtil.extractEmail(token);

        OrderResponse response = orderService.payOrder(id, email);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Order paid successfully",
                "data", response));
    }

    @PostMapping("/orders/{id}/cancel")
    public ResponseEntity<?> cancelOrder(
            @PathVariable Long id,
            @RequestHeader("Authorization") String tokenHeader) {

        String token = stripBearer(tokenHeader);
        String email = jwtUtil.extractEmail(token);

        OrderResponse response = orderService.cancelOrder(id, email, token);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Order cancelled successfully",
                "data", response));
    }

    @GetMapping("/orders")
    public ResponseEntity<?> listOrders(
            @RequestHeader("Authorization") String tokenHeader) {

        String token = stripBearer(tokenHeader);
        String email = jwtUtil.extractEmail(token);
        String role = jwtUtil.extractRole(token);

        boolean isAdmin = "ADMIN".equalsIgnoreCase(role);

        List<OrderResponse> list = orderService.listOrders(email, isAdmin);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Orders retrieved",
                "data", list));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<?> getOrder(
            @PathVariable Long id,
            @RequestHeader("Authorization") String tokenHeader) {

        String token = stripBearer(tokenHeader);
        String email = jwtUtil.extractEmail(token);
        String role = jwtUtil.extractRole(token);
        String name = jwtUtil.extractName(token);

        boolean isAdmin = "ADMIN".equalsIgnoreCase(role);

        OrderResponse order = orderService.getOrder(id, email, isAdmin);

        return ResponseEntity.ok(ApiResponse.success("Find Order with username: " + name, order));

    }

    private String stripBearer(String header) {
        if (header == null)
            throw new IllegalArgumentException("Authorization header missing");
        if (header.startsWith("Bearer "))
            return header.substring(7);
        return header;
    }
}
