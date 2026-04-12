package tn.esprit.forme.certificationservice.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.forme.certificationservice.domain.entity.OralSession;
import tn.esprit.forme.certificationservice.domain.enums.OralSessionStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface OralSessionRepository extends JpaRepository<OralSession, Long> {
    List<OralSession> findByEvaluatorId(Long evaluatorId);
    long countByStatus(OralSessionStatus status);
    List<OralSession> findByStatusAndScheduledAtBetween(OralSessionStatus status, LocalDateTime start, LocalDateTime end);
    
    // Evaluator-specific queries
    List<OralSession> findByEvaluatorIdAndStatus(Long evaluatorId, OralSessionStatus status);
    List<OralSession> findByEvaluatorIdAndStatusIn(Long evaluatorId, List<OralSessionStatus> statuses);
}
