package tn.esprit.formation_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.formation_service.entity.ResultEvaluation;

import java.util.List;

public interface ResultEvaluationRepository extends JpaRepository<ResultEvaluation, Long> {

    List<ResultEvaluation> findByEvaluation_Id(Long evaluationId);

    @Query("SELECT r FROM ResultEvaluation r LEFT JOIN FETCH r.evaluation WHERE r.user_id = :userId")
    List<ResultEvaluation> findByUser_id(@Param("userId") Long user_id);

    @Query("SELECT r FROM ResultEvaluation r WHERE r.evaluation.id = :evaluationId AND r.user_id = :userId ORDER BY r.attempt_number ASC")
    List<ResultEvaluation> findByEvaluationIdAndUser_id(@Param("evaluationId") Long evaluationId, @Param("userId") Long userId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM ResultEvaluation r WHERE r.user_id = :userId AND r.evaluation.formation.id = :formationId")
    int deleteByUserIdAndFormationId(@Param("userId") Long userId, @Param("formationId") Long formationId);

    @Query("SELECT COUNT(r) FROM ResultEvaluation r")
    long countTotalAttempts();

    @Query("SELECT COUNT(r) FROM ResultEvaluation r WHERE r.passed = true")
    long countPassedAttempts();

    @Query("SELECT AVG(r.score) FROM ResultEvaluation r WHERE r.score IS NOT NULL")
    Double averageScore();

    @Query(value = "SELECT evaluation_id, COUNT(*), SUM(CASE WHEN passed = 1 THEN 1 ELSE 0 END), AVG(score), AVG(attempt_number) " +
           "FROM result_evaluation GROUP BY evaluation_id", nativeQuery = true)
    List<Object[]> findEvaluationAggregates();

    @Query("SELECT DISTINCT r.evaluation.formation.id, r.user_id FROM ResultEvaluation r")
    List<Object[]> findDistinctFormationUserPairsFromEvaluations();
}
