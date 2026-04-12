package tn.esprit.shop.shopservice.modules.payment.controller;

import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.shop.shopservice.modules.order.entity.Order;
import tn.esprit.shop.shopservice.modules.order.service.IOrderService;
import tn.esprit.shop.shopservice.modules.payment.entity.Payment;
import tn.esprit.shop.shopservice.modules.payment.service.IPaymentService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    private final IPaymentService ipaymentservice;
    private final IOrderService iorderservice;
    private final String stripeSecretKey;

    @Autowired
    public PaymentController(IPaymentService ipaymentservice,
                             IOrderService iorderservice,
                             @Value("${stripe.secret-key}") String stripeSecretKey) {
        this.ipaymentservice = ipaymentservice;
        this.iorderservice = iorderservice;
        this.stripeSecretKey = stripeSecretKey;
    }

    @GetMapping("/listPayments")
    public List<Payment> listPayments() {
        return ipaymentservice.getAllPayments();
    }

    @PostMapping("/addPayment")
    public Payment addPayment(@RequestBody Payment payment) {
        return ipaymentservice.addPayment(payment);
    }

    @GetMapping("/getPayment/{id}")
    public Payment getPayment(@PathVariable long id) {
        return ipaymentservice.getPaymentBy(id);
    }

    @PutMapping("/updatePayment/{id}")
    public Payment updatePayment(@PathVariable long id, @RequestBody Payment payment) {
        payment.setIdPayment(id);
        return ipaymentservice.updatePayment(payment);
    }

    @DeleteMapping("/deletePayment/{id}")
    public void deletePayment(@PathVariable long id) {
        ipaymentservice.deletePayment(id);
    }

    @PostMapping("/addListPayments")
    public List<Payment> addListPayments(@RequestBody List<Payment> payments) {
        return ipaymentservice.addListPayments(payments);
    }

    @GetMapping("/getByOrderId/{orderId}")
    public List<Payment> getByOrderId(@PathVariable Long orderId) {
        return ipaymentservice.findByOrderId(orderId);
    }

    @GetMapping("/getByUserId/{userId}")
    public List<Payment> getByUserId(@PathVariable Long userId) {
        return ipaymentservice.findByUserId(userId);
    }

    @GetMapping("/getByStatus/{status}")
    public List<Payment> getByStatus(@PathVariable String status) {
        return ipaymentservice.findByStatus(status);
    }

    @PostMapping("/create-intent")
    public ResponseEntity<?> createPaymentIntent(@RequestBody Map<String, Object> body) {
        try {
            if (body == null || body.get("orderId") == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "orderId is required"));
            }
            Long orderId = Long.valueOf(body.get("orderId").toString());
            Order order = iorderservice.getOrderBy(orderId);
            if (order == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Order not found"));
            }
            if (order.getTotalAmount() == null || order.getTotalAmount() <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Order amount must be greater than 0"));
            }

            Stripe.apiKey = stripeSecretKey;

            // Stripe expects amount in cents (order totalAmount is in major units, e.g. TND)
            long amountInCents = order.getTotalAmount() * 100L;

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency("eur")
                    .putMetadata("orderId", orderId.toString())
                    .putMetadata("userId", order.getUserId().toString())
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);

            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setUserId(order.getUserId());
            payment.setAmount(order.getTotalAmount());
            payment.setCurrency(order.getCurrency());
            payment.setStatus("INITIATED");
            payment.setProvider("STRIPE");
            payment.setProviderPaymentId(intent.getId());
            ipaymentservice.addPayment(payment);

            Map<String, String> response = new HashMap<>();
            response.put("clientSecret", intent.getClientSecret());
            response.put("paymentIntentId", intent.getId());
            return ResponseEntity.ok(response);

        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid orderId"));
        } catch (Exception e) {
            e.printStackTrace();
            String message = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            return ResponseEntity.status(500).body(Map.of("error", "Payment intent failed: " + message));
        }
    }
}
