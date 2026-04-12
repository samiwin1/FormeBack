package tn.esprit.shop.shopservice.modules.order.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.shop.shopservice.modules.order.entity.Order;
import tn.esprit.shop.shopservice.modules.order.repository.OrderRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class OrderService implements IOrderService {

    OrderRepository orderRepository;

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Order addOrder(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public Order getOrderBy(long id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Override
    public Order updateOrder(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public void deleteOrder(long id) {
        orderRepository.deleteById(id);
    }

    @Override
    public List<Order> addListOrders(List<Order> orders) {
        return orderRepository.saveAll(orders);
    }

    @Override
    public List<Order> findByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    public List<Order> findByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    @Override
    public Order updateStatus(Long orderId, String status) {
        return orderRepository.findById(orderId)
                .map(order -> {
                    order.setStatus(status);
                    return orderRepository.save(order);
                })
                .orElse(null);
    }
}
