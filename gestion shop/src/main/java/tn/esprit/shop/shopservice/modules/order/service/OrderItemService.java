package tn.esprit.shop.shopservice.modules.order.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.shop.shopservice.modules.order.entity.OrderItem;
import tn.esprit.shop.shopservice.modules.order.repository.OrderItemRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class OrderItemService implements IOrderItemService {

    OrderItemRepository orderItemRepository;

    @Override
    public List<OrderItem> getAllOrderItems() {
        return orderItemRepository.findAll();
    }

    @Override
    public OrderItem addOrderItem(OrderItem orderItem) {
        return orderItemRepository.save(orderItem);
    }

    @Override
    public OrderItem getOrderItemBy(long id) {
        return orderItemRepository.findById(id).orElse(null);
    }

    @Override
    public OrderItem updateOrderItem(OrderItem orderItem) {
        return orderItemRepository.save(orderItem);
    }

    @Override
    public void deleteOrderItem(long id) {
        orderItemRepository.deleteById(id);
    }

    @Override
    public List<OrderItem> addListOrderItems(List<OrderItem> orderItems) {
        return orderItemRepository.saveAll(orderItems);
    }

    @Override
    public List<OrderItem> findByOrderId(Long orderId) {
        return orderItemRepository.findByOrder_IdOrder(orderId);
    }

    @Override
    public List<OrderItem> findByProductId(Long productId) {
        return orderItemRepository.findByProduct_IdProduct(productId);
    }
}
