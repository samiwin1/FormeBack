package tn.esprit.shop.shopservice.modules.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.shop.shopservice.modules.payment.entity.Payment;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByOrder_IdOrder(Long orderId);
    List<Payment> findByUserId(Long userId);
    List<Payment> findByStatus(String status);
}
