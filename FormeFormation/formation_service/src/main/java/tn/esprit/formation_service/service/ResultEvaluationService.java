package tn.esprit.formation_service.service;

import tn.esprit.formation_service.entity.ResultEvaluation;

import java.util.List;
import java.util.Optional;

public interface ResultEvaluationService {

    ResultEvaluation save(ResultEvaluation resultEvaluation);
    Optional<ResultEvaluation> findById(Long id);
    List<ResultEvaluation> findAll();
    List<ResultEvaluation> findByEvaluationId(Long evaluationId);
    List<ResultEvaluation> findByUser_id(Long user_id);
    List<ResultEvaluation> findByEvaluationIdAndUser_id(Long evaluationId, Long userId);
    void deleteById(Long id);
    ResultEvaluation update(Long id, ResultEvaluation resultEvaluation);
    void deleteByUserIdAndFormationId(Long userId, Long formationId);
}
