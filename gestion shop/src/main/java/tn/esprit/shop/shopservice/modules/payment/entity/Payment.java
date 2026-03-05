package tn.esprit.shop.shopservice.modules.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.shop.shopservice.modules.order.entity.Order;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPayment;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String provider;

    private String providerPaymentId;
    private String paymentMethod;

    private String failureReason;
    private Integer refundAmount;

    private LocalDateTime transactionDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void beforeSave() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (transactionDate == null) transactionDate = LocalDateTime.now();
        if (status == null) status = "INITIATED";
    }

    @PreUpdate
    public void beforeUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
