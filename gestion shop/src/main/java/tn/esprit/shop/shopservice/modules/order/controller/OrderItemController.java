package tn.esprit.shop.shopservice.modules.order.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.shop.shopservice.modules.order.entity.OrderItem;
import tn.esprit.shop.shopservice.modules.order.service.IOrderItemService;

import java.util.List;

@RestController
@RequestMapping("/orderItem")
@AllArgsConstructor
public class OrderItemController {

    IOrderItemService iorderItemservice;

    @GetMapping("/listOrderItems")
    public List<OrderItem> listOrderItems() {
        return iorderItemservice.getAllOrderItems();
    }

    @PostMapping("/addOrderItem")
    public OrderItem addOrderItem(@RequestBody OrderItem orderItem) {
        return iorderItemservice.addOrderItem(orderItem);
    }

    @GetMapping("/getOrderItem/{id}")
    public OrderItem getOrderItem(@PathVariable long id) {
        return iorderItemservice.getOrderItemBy(id);
    }

    @PutMapping("/updateOrderItem/{id}")
    public OrderItem updateOrderItem(@PathVariable long id, @RequestBody OrderItem orderItem) {
        orderItem.setIdOrderItem(id);
        return iorderItemservice.updateOrderItem(orderItem);
    }

    @DeleteMapping("/deleteOrderItem/{id}")
    public void deleteOrderItem(@PathVariable long id) {
        iorderItemservice.deleteOrderItem(id);
    }

    @PostMapping("/addListOrderItems")
    public List<OrderItem> addListOrderItems(@RequestBody List<OrderItem> orderItems) {
        return iorderItemservice.addListOrderItems(orderItems);
    }

    @GetMapping("/getByOrderId/{orderId}")
    public List<OrderItem> getByOrderId(@PathVariable Long orderId) {
        return iorderItemservice.findByOrderId(orderId);
    }

    @GetMapping("/getByProductId/{productId}")
    public List<OrderItem> getByProductId(@PathVariable Long productId) {
        return iorderItemservice.findByProductId(productId);
    }
}
