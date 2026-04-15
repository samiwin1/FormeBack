package tn.esprit.shop.shopservice.modules.order.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.shop.shopservice.modules.cart.entity.Cart;
import tn.esprit.shop.shopservice.modules.cart.entity.CartItem;
import tn.esprit.shop.shopservice.modules.cart.service.ICartItemService;
import tn.esprit.shop.shopservice.modules.cart.service.ICartService;
import tn.esprit.shop.shopservice.modules.order.entity.Order;
import tn.esprit.shop.shopservice.modules.order.entity.OrderItem;
import tn.esprit.shop.shopservice.modules.order.service.IOrderItemService;
import tn.esprit.shop.shopservice.modules.order.service.IOrderService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
@AllArgsConstructor
public class OrderController {

    IOrderService iorderservice;
    IOrderItemService iorderItemservice;
    ICartService icartservice;
    ICartItemService icartItemservice;

    @GetMapping("/listOrders")
    public List<Order> listOrders() {
        return iorderservice.getAllOrders();
    }

    @PostMapping("/addOrder")
    public Order addOrder(@RequestBody Order order) {
        return iorderservice.addOrder(order);
    }

    @GetMapping("/getOrder/{id}")
    public Order getOrder(@PathVariable long id) {
        return iorderservice.getOrderBy(id);
    }

    @PutMapping("/updateOrder/{id}")
    public Order updateOrder(@PathVariable long id, @RequestBody Order order) {
        order.setIdOrder(id);
        return iorderservice.updateOrder(order);
    }

    @DeleteMapping("/deleteOrder/{id}")
    public void deleteOrder(@PathVariable long id) {
        iorderservice.deleteOrder(id);
    }

    @PostMapping("/addListOrders")
    public List<Order> addListOrders(@RequestBody List<Order> orders) {
        return iorderservice.addListOrders(orders);
    }

    @GetMapping("/getByUserId/{userId}")
    public List<Order> getByUserId(@PathVariable Long userId) {
        return iorderservice.findByUserId(userId);
    }

    @GetMapping("/getByStatus/{status}")
    public List<Order> getByStatus(@PathVariable String status) {
        return iorderservice.findByStatus(status);
    }

    @PutMapping("/updateOrderStatus/{id}")
    public ResponseEntity<?> updateOrderStatus(@PathVariable long id, @RequestBody Map<String, String> body) {
        String status = body != null ? body.get("status") : null;
        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().body("Missing 'status' in request body");
        }
        Order order = iorderservice.updateStatus(id, status);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(order);
    }

    @PostMapping("/checkout/{userId}")
    public ResponseEntity<?> checkout(@PathVariable Long userId) {
        Cart cart = icartservice.findActiveCartByUserId(userId);
        if (cart == null) {
            return ResponseEntity.badRequest().body("No active cart found for user " + userId);
        }

        List<CartItem> cartItems = icartItemservice.findByCartId(cart.getIdCart());
        if (cartItems == null || cartItems.isEmpty()) {
            return ResponseEntity.badRequest().body("Cart is empty");
        }

        int totalAmount = cartItems.stream()
                .mapToInt(ci -> ci.getUnitPriceSnapshot() * ci.getQuantity())
                .sum();

        Order order = new Order();
        order.setUserId(userId);
        order.setStatus("PENDING");
        order.setTotalAmount(totalAmount);
        order.setCurrency("TND");
        Order savedOrder = iorderservice.addOrder(order);

        for (CartItem ci : cartItems) {
            OrderItem oi = new OrderItem();
            oi.setOrder(savedOrder);
            oi.setProduct(ci.getProduct());
            oi.setFormationId(ci.getFormationId());
            oi.setQuantity(ci.getQuantity());
            oi.setUnitPriceSnapshot(ci.getUnitPriceSnapshot());
            String title = ci.getFormationTitleSnapshot();
            if (title == null || title.isBlank()) {
                title = ci.getTitleSnapshot();
            }
            if (title == null || title.isBlank()) {
                title = "Formation #" + ci.getFormationId();
            }
            oi.setFormationTitleSnapshot(title);
            oi.setTitleSnapshot(title);
            iorderItemservice.addOrderItem(oi);
        }

        return ResponseEntity.ok(savedOrder);
    }
}
