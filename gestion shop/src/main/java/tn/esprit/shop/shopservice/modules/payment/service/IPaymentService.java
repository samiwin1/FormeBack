package tn.esprit.shop.shopservice.modules.payment.service;

import tn.esprit.shop.shopservice.modules.payment.entity.Payment;

import java.util.List;

public interface IPaymentService {

    List<Payment> getAllPayments();

    Payment addPayment(Payment payment);

    Payment getPaymentBy(long id);

    Payment updatePayment(Payment payment);

    void deletePayment(long id);

    List<Payment> addListPayments(List<Payment> payments);

    List<Payment> findByOrderId(Long orderId);

    List<Payment> findByUserId(Long userId);

    List<Payment> findByStatus(String status);
}
