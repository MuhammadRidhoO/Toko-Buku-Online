package user.order_service.application.service;

import user.order_service.application.dto.CreateOrderRequest;
import user.order_service.application.dto.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(String userEmail, CreateOrderRequest request, String token);

    OrderResponse payOrder(Long orderId, String payerEmail);

    // OrderResponse cancelOrder(Long orderId, String cancelledByEmail);

    OrderResponse cancelOrder(Long orderId, String cancelledByEmail, String token);

    List<OrderResponse> listOrders(String requesterEmail, boolean isAdmin);

    OrderResponse getOrder(Long orderId, String requesterEmail, boolean isAdmin);
}