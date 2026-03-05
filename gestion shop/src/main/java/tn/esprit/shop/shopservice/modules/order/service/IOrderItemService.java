package tn.esprit.shop.shopservice.modules.order.service;

import tn.esprit.shop.shopservice.modules.order.entity.OrderItem;

import java.util.List;

public interface IOrderItemService {

    List<OrderItem> getAllOrderItems();

    OrderItem addOrderItem(OrderItem orderItem);

    OrderItem getOrderItemBy(long id);

    OrderItem updateOrderItem(OrderItem orderItem);

    void deleteOrderItem(long id);

    List<OrderItem> addListOrderItems(List<OrderItem> orderItems);

    List<OrderItem> findByOrderId(Long orderId);

    List<OrderItem> findByProductId(Long productId);
}
