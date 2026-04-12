package tn.esprit.formation_service.service;

import tn.esprit.formation_service.dto.EvaluationHistoryItem;
import tn.esprit.formation_service.dto.EvaluationSubmitResponse;
import tn.esprit.formation_service.entity.Evaluation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface EvaluationService {

    Evaluation save(Evaluation evaluation);
    Optional<Evaluation> findById(Long id);
    List<Evaluation> findAll();
    List<Evaluation> findByFormationId(Long formationId);
    void deleteById(Long id);
    Evaluation update(Long id, Evaluation evaluation);

    EvaluationSubmitResponse submitEvaluation(Long evaluationId, Long userId, Map<String, Integer> answers);

    List<EvaluationHistoryItem> getEvaluationHistory(Long userId, Long formationId);
}
