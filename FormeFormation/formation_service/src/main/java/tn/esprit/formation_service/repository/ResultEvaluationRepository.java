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
}
