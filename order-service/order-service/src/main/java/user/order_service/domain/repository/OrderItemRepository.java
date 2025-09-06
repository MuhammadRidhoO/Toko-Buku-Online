package user.order_service.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import user.order_service.domain.model.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}