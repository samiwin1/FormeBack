package tn.esprit.shop.shopservice.modules.payment.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.shop.shopservice.modules.payment.entity.Payment;
import tn.esprit.shop.shopservice.modules.payment.repository.PaymentRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class PaymentService implements IPaymentService {

    PaymentRepository paymentRepository;

    @Override
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Override
    public Payment addPayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    @Override
    public Payment getPaymentBy(long id) {
        return paymentRepository.findById(id).orElse(null);
    }

    @Override
    public Payment updatePayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    @Override
    public void deletePayment(long id) {
        paymentRepository.deleteById(id);
    }

    @Override
    public List<Payment> addListPayments(List<Payment> payments) {
        return paymentRepository.saveAll(payments);
    }

    @Override
    public List<Payment> findByOrderId(Long orderId) {
        return paymentRepository.findByOrder_IdOrder(orderId);
    }

    @Override
    public List<Payment> findByUserId(Long userId) {
        return paymentRepository.findByUserId(userId);
    }

    @Override
    public List<Payment> findByStatus(String status) {
        return paymentRepository.findByStatus(status);
    }
}
