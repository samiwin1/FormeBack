package tn.esprit.shop.shopservice.modules.order.service;

import tn.esprit.shop.shopservice.modules.order.entity.Order;

import java.util.List;

public interface IOrderService {

    List<Order> getAllOrders();

    Order addOrder(Order order);

    Order getOrderBy(long id);

    Order updateOrder(Order order);

    void deleteOrder(long id);

    List<Order> addListOrders(List<Order> orders);

    List<Order> findByUserId(Long userId);

    List<Order> findByStatus(String status);

    Order updateStatus(Long orderId, String status);
}
