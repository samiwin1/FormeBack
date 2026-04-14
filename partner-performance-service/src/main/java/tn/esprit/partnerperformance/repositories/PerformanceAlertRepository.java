package tn.esprit.partnerperformance.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.partnerperformance.entities.PerformanceAlert;

import java.util.List;

@Repository
public interface PerformanceAlertRepository extends JpaRepository<PerformanceAlert, Long> {
    List<PerformanceAlert> findByStatusOrderByCreatedAtDesc(String status);
    List<PerformanceAlert> findByPartnerIdAndStatus(Long partnerId, String status);
}
