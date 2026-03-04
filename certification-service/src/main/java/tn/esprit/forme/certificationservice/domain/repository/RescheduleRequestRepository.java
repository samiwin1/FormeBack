package tn.esprit.forme.certificationservice.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.forme.certificationservice.domain.entity.RescheduleRequest;
import tn.esprit.forme.certificationservice.domain.enums.RescheduleStatus;

import java.util.List;

public interface RescheduleRequestRepository extends JpaRepository<RescheduleRequest, Long> {
    List<RescheduleRequest> findByStatus(RescheduleStatus status);
    List<RescheduleRequest> findByAssignmentLearnerIdOrderByRequestedAtDesc(Long learnerId);
    long countByStatus(RescheduleStatus status);
}
