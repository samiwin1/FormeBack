package tn.esprit.shop.shopservice.modules.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.shop.shopservice.modules.order.entity.OrderItem;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrder_IdOrder(Long orderId);
    List<OrderItem> findByProduct_IdProduct(Long productId);
}
