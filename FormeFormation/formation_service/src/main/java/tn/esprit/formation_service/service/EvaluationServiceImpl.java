package tn.esprit.formation_service.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.formation_service.dto.EvaluationHistoryItem;
import tn.esprit.formation_service.dto.EvaluationSubmitResponse;
import tn.esprit.formation_service.dto.IncorrectAnswerItem;
import tn.esprit.formation_service.entity.Evaluation;
import tn.esprit.formation_service.entity.Formation;
import tn.esprit.formation_service.entity.ResultEvaluation;
import tn.esprit.formation_service.exception.MaxAttemptsExceededException;
import tn.esprit.formation_service.repository.EvaluationRepository;
import tn.esprit.formation_service.repository.FormationRepository;
import tn.esprit.formation_service.util.QuizScoringUtil;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class EvaluationServiceImpl implements EvaluationService {

    private static final int DEFAULT_PASSING_SCORE = 75;
    private static final int DEFAULT_MAX_ATTEMPTS = 3;

    private final EvaluationRepository evaluationRepository;
    private final FormationRepository formationRepository;
    private final ResultEvaluationService resultEvaluationService;
    private final GeminiApiService geminiApiService;

    public EvaluationServiceImpl(EvaluationRepository evaluationRepository,
                                 FormationRepository formationRepository,
                                 ResultEvaluationService resultEvaluationService,
                                 GeminiApiService geminiApiService) {
        this.evaluationRepository = evaluationRepository;
        this.formationRepository = formationRepository;
        this.resultEvaluationService = resultEvaluationService;
        this.geminiApiService = geminiApiService;
    }

    @Override
    @Transactional
    public Evaluation save(Evaluation evaluation) {
        if (evaluation.getFormation() != null && evaluation.getFormation().getId() != null) {
            Formation formationRef = formationRepository.getReferenceById(evaluation.getFormation().getId());
            evaluation.setFormation(formationRef);
        }
        return evaluationRepository.save(evaluation);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Evaluation> findById(Long id) {
        return evaluationRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Evaluation> findAll() {
        return evaluationRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Evaluation> findByFormationId(Long formationId) {
        return evaluationRepository.findByFormation_Id(formationId);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        evaluationRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Evaluation update(Long id, Evaluation evaluation) {
        Evaluation existing = evaluationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evaluation not found with id: " + id));
        evaluation.setId(existing.getId());
        return evaluationRepository.save(evaluation);
    }

    @Override
    @Transactional
    public EvaluationSubmitResponse submitEvaluation(Long evaluationId, Long userId, Map<String, Integer> answers) {
        Evaluation evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new RuntimeException("Evaluation not found with id: " + evaluationId));

        int maxAttempts = evaluation.getMax_attempts() != null ? evaluation.getMax_attempts() : DEFAULT_MAX_ATTEMPTS;
        int passingScore = evaluation.getPassing_score() != null ? evaluation.getPassing_score() : DEFAULT_PASSING_SCORE;

        List<ResultEvaluation> existingAttempts = resultEvaluationService.findByEvaluationIdAndUser_id(evaluationId, userId);
        int attemptCount = existingAttempts.size();

        boolean alreadyPassed = existingAttempts.stream().anyMatch(r -> Boolean.TRUE.equals(r.getPassed()));
        if (alreadyPassed) {
            throw new MaxAttemptsExceededException("You have already passed this evaluation.");
        }

        if (attemptCount >= maxAttempts) {
            throw new MaxAttemptsExceededException("Maximum attempts reached");
        }

        int score = QuizScoringUtil.scoreStructuredAnswers(evaluation.getContent(), answers);
        boolean passed = score >= passingScore;

        ResultEvaluation result = new ResultEvaluation();
        result.setEvaluation(evaluation);
        result.setUser_id(userId);
        result.setScore(score);
        result.setAttempt_number(attemptCount + 1);
        result.setPassed(passed);
        result.setAnswered_at(Instant.now());
        resultEvaluationService.save(result);

        int remainingAttempts = Math.max(0, maxAttempts - (attemptCount + 1));
        int attemptNumber = attemptCount + 1;

        List<IncorrectAnswerItem> mistakeExplanations = null;
        if (attemptNumber == 2 && !passed && evaluation.getContent() != null && !evaluation.getContent().isBlank()) {
            List<IncorrectAnswerItem> incorrectItems = QuizScoringUtil.extractIncorrectAnswers(evaluation.getContent(), answers);
            if (!incorrectItems.isEmpty()) {
                mistakeExplanations = geminiApiService.explainMistakes(incorrectItems, evaluation.getTitle() != null ? evaluation.getTitle() : "Quiz");
            }
        }

        return new EvaluationSubmitResponse(score, passed, remainingAttempts, attemptNumber, mistakeExplanations);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvaluationHistoryItem> getEvaluationHistory(Long userId, Long formationId) {
        List<ResultEvaluation> results = resultEvaluationService.findByUser_id(userId);
        List<EvaluationHistoryItem> items = new ArrayList<>();
        for (ResultEvaluation r : results) {
            Evaluation evaluation = r.getEvaluation();
            if (evaluation == null) continue;
            if (formationId != null && evaluation.getFormation() != null
                    && !formationId.equals(evaluation.getFormation().getId())) {
                continue;
            }
            EvaluationHistoryItem item = new EvaluationHistoryItem();
            item.setEvaluationId(evaluation.getId());
            item.setEvaluationTitle(evaluation.getTitle());
            item.setFormationId(evaluation.getFormation() != null ? evaluation.getFormation().getId() : null);
            item.setAttemptNumber(r.getAttempt_number());
            item.setScore(r.getScore());
            item.setPassed(r.getPassed());
            item.setAnsweredAt(r.getAnswered_at());
            items.add(item);
        }
        items.sort(Comparator.comparing(EvaluationHistoryItem::getAnsweredAt, Comparator.nullsLast(Comparator.reverseOrder())));
        return items;
    }
}
