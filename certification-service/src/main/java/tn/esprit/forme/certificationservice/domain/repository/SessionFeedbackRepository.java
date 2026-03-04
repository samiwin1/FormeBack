package tn.esprit.forme.certificationservice.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.forme.certificationservice.domain.entity.SessionFeedback;

import java.util.List;
import java.util.Optional;

public interface SessionFeedbackRepository extends JpaRepository<SessionFeedback, Long> {

    boolean existsByLearnerIdAndIssuedCertificationId(Long learnerId, Long issuedCertificationId);

    List<SessionFeedback> findBySessionId(Long sessionId);

    @Query("SELECT AVG(f.sessionRating) FROM SessionFeedback f WHERE f.sessionId = :sessionId")
    Optional<Double> findAvgSessionRatingBySessionId(@Param("sessionId") Long sessionId);

    @Query("""
            SELECT AVG(f.evaluatorRating)
            FROM SessionFeedback f
            JOIN OralSession s ON f.sessionId = s.id
            WHERE s.evaluatorId = :evaluatorId
            """)
    Optional<Double> findAvgEvaluatorRatingByEvaluatorId(@Param("evaluatorId") Long evaluatorId);

    @Query("SELECT DISTINCT f.issuedCertificationId FROM SessionFeedback f WHERE f.learnerId = :learnerId")
    List<Long> findIssuedCertificationIdsWithFeedbackByLearnerId(@Param("learnerId") Long learnerId);
}

