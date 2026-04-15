package tn.esprit.voucherfraud.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.voucherfraud.entities.FraudAlert;

import java.util.List;

@Repository
public interface FraudAlertRepository extends JpaRepository<FraudAlert, Long> {
    List<FraudAlert> findByPartnerId(Long partnerId);
    List<FraudAlert> findByStatus(String status);
    List<FraudAlert> findByVoucherCode(String voucherCode);
}