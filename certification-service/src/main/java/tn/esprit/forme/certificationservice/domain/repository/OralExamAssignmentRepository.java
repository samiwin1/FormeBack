package tn.esprit.forme.certificationservice.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.forme.certificationservice.domain.entity.OralExamAssignment;
import tn.esprit.forme.certificationservice.domain.enums.AssignmentStatus;

import java.util.List;
import java.util.Optional;

public interface OralExamAssignmentRepository extends JpaRepository<OralExamAssignment, Long> {
    List<OralExamAssignment> findByLearnerId(Long learnerId);
    List<OralExamAssignment> findByOralSessionId(Long oralSessionId);
    List<OralExamAssignment> findByOralSessionEvaluatorId(Long evaluatorId);
    List<OralExamAssignment> findByStatus(AssignmentStatus status);
    long countByStatus(AssignmentStatus status);
    long countByOralSessionId(Long oralSessionId);
    Optional<OralExamAssignment> findTopByLearnerIdAndOralSessionCertificationIdOrderByAttemptNumberDescCreatedAtDesc(
            Long learnerId,
            Long certificationId
    );
    boolean existsByLearnerIdAndOralSessionCertificationIdAndStatusIn(
            Long learnerId,
            Long certificationId,
            List<AssignmentStatus> statuses
    );
    List<OralExamAssignment> findByOralSession_IdAndStatusIn(Long sessionId, List<AssignmentStatus> statuses);
}
