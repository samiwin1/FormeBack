package tn.esprit.formation_service.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.formation_service.entity.ContenuFormation;
import tn.esprit.formation_service.entity.Evaluation;
import tn.esprit.formation_service.entity.Examen;
import tn.esprit.formation_service.entity.ResultEvaluation;
import tn.esprit.formation_service.entity.ResultExamen;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ValidationServiceImpl implements ValidationService {

    private final EvaluationService evaluationService;
    private final ContenuFormationService contenuFormationService;
    private final ResultEvaluationService resultEvaluationService;
    private final ExamenService examenService;
    private final ResultExamenService resultExamenService;

    public ValidationServiceImpl(EvaluationService evaluationService,
                                  ContenuFormationService contenuFormationService,
                                  ResultEvaluationService resultEvaluationService,
                                  ExamenService examenService,
                                  ResultExamenService resultExamenService) {
        this.evaluationService = evaluationService;
        this.contenuFormationService = contenuFormationService;
        this.resultEvaluationService = resultEvaluationService;
        this.examenService = examenService;
        this.resultExamenService = resultExamenService;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateEvaluation(int score, int passingScore) {
        return score >= passingScore;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canStartExam(Long userId, Long formationId) {
        List<ContenuFormation> blocks = contenuFormationService.findByFormationId(formationId);

        Set<Long> blockLinkedEvaluationIds = blocks.stream()
                .filter(b -> b.getEvaluation() != null)
                .map(b -> b.getEvaluation().getId())
                .collect(Collectors.toSet());

        if (blockLinkedEvaluationIds.isEmpty()) {
            return true;
        }

        List<ResultEvaluation> userResults = resultEvaluationService.findByUser_id(userId);
        Set<Long> passedEvaluationIds = userResults.stream()
                .filter(r -> Boolean.TRUE.equals(r.getPassed()))
                .map(r -> r.getEvaluation() != null ? r.getEvaluation().getId() : null)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        boolean allEvaluationsPassed = blockLinkedEvaluationIds.stream()
                .allMatch(passedEvaluationIds::contains);

        if (!allEvaluationsPassed) {
            return false;
        }

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFormationCompleted(Long userId, Long formationId) {
        List<Evaluation> evaluations = evaluationService.findByFormationId(formationId);
        List<ContenuFormation> blocks = contenuFormationService.findByFormationId(formationId);

        Set<Long> blockLinkedEvaluationIds = blocks.stream()
                .filter(b -> b.getEvaluation() != null)
                .map(b -> b.getEvaluation().getId())
                .collect(Collectors.toSet());

        if (blockLinkedEvaluationIds.isEmpty()) {
            return examenPassed(userId, formationId);
        }

        List<ResultEvaluation> userResults = resultEvaluationService.findByUser_id(userId);
        Set<Long> passedEvaluationIds = userResults.stream()
                .filter(r -> Boolean.TRUE.equals(r.getPassed()))
                .map(r -> r.getEvaluation() != null ? r.getEvaluation().getId() : null)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        boolean allEvaluationsPassed = blockLinkedEvaluationIds.stream()
                .allMatch(passedEvaluationIds::contains);

        return allEvaluationsPassed && examenPassed(userId, formationId);
    }

    private boolean examenPassed(Long userId, Long formationId) {
        Optional<Examen> examenOpt = examenService.findByFormationId(formationId);
        if (examenOpt.isEmpty()) {
            return true;
        }
        List<ResultExamen> userResults = resultExamenService.findByUser_id(userId);
        return userResults.stream()
                .filter(r -> r.getExamen() != null && r.getExamen().getFormation() != null
                        && formationId.equals(r.getExamen().getFormation().getId()))
                .anyMatch(r -> Boolean.TRUE.equals(r.getPassed()));
    }
}
