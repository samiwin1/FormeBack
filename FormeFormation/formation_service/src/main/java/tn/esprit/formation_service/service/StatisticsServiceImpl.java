package tn.esprit.formation_service.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.formation_service.dto.*;
import tn.esprit.formation_service.entity.Evaluation;
import tn.esprit.formation_service.entity.Examen;
import tn.esprit.formation_service.entity.Formation;
import tn.esprit.formation_service.repository.ResultEvaluationRepository;
import tn.esprit.formation_service.repository.ResultExamenRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private static final int TOP_N = 5;
    private static final double HIGH_FAILURE_THRESHOLD = 0.5;
    private static final double EVALUATION_OBSTACLE_THRESHOLD = 0.6;
    private static final double RETRY_OBSTACLE_AVG_ATTEMPTS = 2.0;
    private static final double LOW_SCORE_THRESHOLD = 60.0;
    private static final double LONG_DURATION_MULTIPLIER = 1.5;

    private final ResultEvaluationRepository resultEvaluationRepository;
    private final ResultExamenRepository resultExamenRepository;
    private final FormationService formationService;
    private final EvaluationService evaluationService;
    private final ExamenService examenService;
    private final ValidationService validationService;
    private final GeminiApiService geminiApiService;

    public StatisticsServiceImpl(ResultEvaluationRepository resultEvaluationRepository,
                                ResultExamenRepository resultExamenRepository,
                                FormationService formationService,
                                EvaluationService evaluationService,
                                ExamenService examenService,
                                ValidationService validationService,
                                GeminiApiService geminiApiService) {
        this.resultEvaluationRepository = resultEvaluationRepository;
        this.resultExamenRepository = resultExamenRepository;
        this.formationService = formationService;
        this.evaluationService = evaluationService;
        this.examenService = examenService;
        this.validationService = validationService;
        this.geminiApiService = geminiApiService;
    }

    @Override
    @Transactional(readOnly = true)
    public GlobalAnalyticsResponse getGlobalAnalytics() {
        TrainingCompletionStats trainingCompletion = computeTrainingCompletion();
        AssessmentSuccessStats assessmentSuccess = computeAssessmentSuccess();
        ExamAnalysisStats examAnalysis = computeExamAnalysis();
        LearningObstacleStats learningObstacles = computeLearningObstacles();

        return new GlobalAnalyticsResponse(trainingCompletion, assessmentSuccess, examAnalysis, learningObstacles);
    }

    @Override
    @Transactional(readOnly = true)
    public GlobalAnalyticsResponse getGlobalAnalyticsWithInsights() {
        GlobalAnalyticsResponse analytics = getGlobalAnalytics();
        try {
            String insights = geminiApiService.generateAnalyticsInsights(analytics);
            if (insights != null && !insights.isBlank()) {
                analytics.setAiInsights(insights);
            }
        } catch (Exception e) {
            // Graceful fallback: return analytics without AI insights
        }
        return analytics;
    }

    private TrainingCompletionStats computeTrainingCompletion() {
        Set<FormationUserPair> started = new HashSet<>();
        for (Object[] row : resultEvaluationRepository.findDistinctFormationUserPairsFromEvaluations()) {
            started.add(new FormationUserPair(((Number) row[0]).longValue(), ((Number) row[1]).longValue()));
        }
        for (Object[] row : resultExamenRepository.findDistinctFormationUserPairsFromExamsAny()) {
            started.add(new FormationUserPair(((Number) row[0]).longValue(), ((Number) row[1]).longValue()));
        }

        long totalStarted = started.size();
        Map<Long, Long> completedByFormation = new HashMap<>();
        long totalCompleted = 0;

        for (FormationUserPair pair : started) {
            if (validationService.isFormationCompleted(pair.userId, pair.formationId)) {
                totalCompleted++;
                completedByFormation.merge(pair.formationId, 1L, Long::sum);
            }
        }

        Map<Long, Long> startedByFormation = started.stream()
                .collect(Collectors.groupingBy(p -> p.formationId, Collectors.counting()));

        double completionRatePercent = totalStarted > 0 ? (100.0 * totalCompleted / totalStarted) : 0.0;

        List<FormationStatItem> topCompleted = buildTopFormationStats(completedByFormation, startedByFormation, true);
        List<FormationStatItem> topAbandoned = buildTopAbandonedFormationStats(startedByFormation, completedByFormation);

        return new TrainingCompletionStats(totalStarted, totalCompleted, completionRatePercent, topCompleted, topAbandoned);
    }

    private List<FormationStatItem> buildTopFormationStats(Map<Long, Long> completedByFormation,
                                                          Map<Long, Long> startedByFormation,
                                                          boolean byCompletion) {
        return completedByFormation.entrySet().stream()
                .map(e -> {
                    Formation f = formationService.findById(e.getKey()).orElse(null);
                    long started = startedByFormation.getOrDefault(e.getKey(), 0L);
                    double rate = started > 0 ? (100.0 * e.getValue() / started) : 0;
                    return new FormationStatItem(e.getKey(), f != null ? f.getTitle() : "?", e.getValue(), rate);
                })
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .limit(TOP_N)
                .collect(Collectors.toList());
    }

    private List<FormationStatItem> buildTopAbandonedFormationStats(Map<Long, Long> startedByFormation,
                                                                   Map<Long, Long> completedByFormation) {
        return startedByFormation.entrySet().stream()
                .map(e -> {
                    long started = e.getValue();
                    long completed = completedByFormation.getOrDefault(e.getKey(), 0L);
                    long abandoned = started - completed;
                    Formation f = formationService.findById(e.getKey()).orElse(null);
                    double rate = started > 0 ? (100.0 * abandoned / started) : 0;
                    return new FormationStatItem(e.getKey(), f != null ? f.getTitle() : "?", abandoned, rate);
                })
                .filter(item -> item.getCount() > 0)
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .limit(TOP_N)
                .collect(Collectors.toList());
    }

    private AssessmentSuccessStats computeAssessmentSuccess() {
        long totalAttempts = resultEvaluationRepository.countTotalAttempts();
        long passedAttempts = resultEvaluationRepository.countPassedAttempts();
        Double avg = resultEvaluationRepository.averageScore();
        double averageScore = avg != null ? avg : 0.0;
        double successRatePercent = totalAttempts > 0 ? (100.0 * passedAttempts / totalAttempts) : 0.0;

        List<EvaluationFailureItem> highFailure = new ArrayList<>();
        for (Object[] row : resultEvaluationRepository.findEvaluationAggregates()) {
            Long evalId = ((Number) row[0]).longValue();
            long total = ((Number) row[1]).longValue();
            long passed = ((Number) row[2]).longValue();
            double failureRate = total > 0 ? (1.0 - (double) passed / total) : 0;
            if (failureRate > HIGH_FAILURE_THRESHOLD) {
                Evaluation eval = evaluationService.findById(evalId).orElse(null);
                String formationTitle = eval != null && eval.getFormation() != null
                        ? eval.getFormation().getTitle() : "?";
                highFailure.add(new EvaluationFailureItem(evalId, eval != null ? eval.getTitle() : "?",
                        formationTitle, failureRate * 100, total));
            }
        }

        return new AssessmentSuccessStats(totalAttempts, passedAttempts, successRatePercent, averageScore, highFailure);
    }

    private ExamAnalysisStats computeExamAnalysis() {
        long completed = resultExamenRepository.countCompletedExams();
        long passed = resultExamenRepository.countPassedExams();
        Double avgScore = resultExamenRepository.averageScoreCompleted();
        Double avgDuration = resultExamenRepository.averageCompletionMinutes();

        double passRate = completed > 0 ? (100.0 * passed / completed) : 0.0;
        double averageScore = avgScore != null ? avgScore : 0.0;

        List<ExamAnalysisStats.RepeatedFailureItem> repeatedFailures = new ArrayList<>();
        for (Object[] row : resultExamenRepository.findRepeatedFailures()) {
            Long examenId = ((Number) row[0]).longValue();
            Long userId = ((Number) row[1]).longValue();
            int count = ((Number) row[2]).intValue();
            Examen ex = examenService.findById(examenId).orElse(null);
            repeatedFailures.add(new ExamAnalysisStats.RepeatedFailureItem(
                    examenId, ex != null ? ex.getTitle() : "?", userId, count));
        }

        Map<Long, Double> examAvgScores = new HashMap<>();
        for (Object[] row : resultExamenRepository.findExamAverageScores()) {
            examAvgScores.put(((Number) row[0]).longValue(), ((Number) row[1]).doubleValue());
        }

        List<ExamAnalysisStats.LowScoreExamItem> lowScores = new ArrayList<>();
        for (Map.Entry<Long, Double> e : examAvgScores.entrySet()) {
            if (e.getValue() < LOW_SCORE_THRESHOLD) {
                Examen ex = examenService.findById(e.getKey()).orElse(null);
                String formationTitle = ex != null && ex.getFormation() != null ? ex.getFormation().getTitle() : "?";
                lowScores.add(new ExamAnalysisStats.LowScoreExamItem(
                        e.getKey(), ex != null ? ex.getTitle() : "?", formationTitle, e.getValue()));
            }
        }

        List<ExamAnalysisStats.LongDurationExamItem> longDurations = new ArrayList<>();
        for (Object[] row : resultExamenRepository.findExamAverageDurations()) {
            Long examenId = ((Number) row[0]).longValue();
            double avgDur = ((Number) row[1]).doubleValue();
            Examen ex = examenService.findById(examenId).orElse(null);
            int expected = ex != null && ex.getDuration_minutes() != null ? ex.getDuration_minutes() : 60;
            if (avgDur > LONG_DURATION_MULTIPLIER * expected) {
                String formationTitle = ex != null && ex.getFormation() != null ? ex.getFormation().getTitle() : "?";
                longDurations.add(new ExamAnalysisStats.LongDurationExamItem(
                        examenId, ex != null ? ex.getTitle() : "?", formationTitle, avgDur, expected));
            }
        }

        ExamAnalysisStats.AbnormalExamPatterns abnormal = new ExamAnalysisStats.AbnormalExamPatterns(
                repeatedFailures, lowScores, longDurations);

        return new ExamAnalysisStats(passRate, averageScore, avgDuration, abnormal);
    }

    private LearningObstacleStats computeLearningObstacles() {
        List<LearningObstacleStats.ObstacleItem> evaluationObstacles = new ArrayList<>();
        List<LearningObstacleStats.ObstacleItem> retryObstacles = new ArrayList<>();

        for (Object[] row : resultEvaluationRepository.findEvaluationAggregates()) {
            Long evalId = ((Number) row[0]).longValue();
            long total = ((Number) row[1]).longValue();
            long passed = ((Number) row[2]).longValue();
            Double avgAttempt = row[4] != null ? ((Number) row[4]).doubleValue() : 0.0;

            double failureRate = total > 0 ? (1.0 - (double) passed / total) : 0;
            if (failureRate > EVALUATION_OBSTACLE_THRESHOLD) {
                Evaluation eval = evaluationService.findById(evalId).orElse(null);
                String formationTitle = eval != null && eval.getFormation() != null ? eval.getFormation().getTitle() : "?";
                evaluationObstacles.add(new LearningObstacleStats.ObstacleItem(
                        evalId, eval != null ? eval.getTitle() : "?", formationTitle, failureRate * 100));
            }
            if (avgAttempt >= RETRY_OBSTACLE_AVG_ATTEMPTS) {
                Evaluation eval = evaluationService.findById(evalId).orElse(null);
                String formationTitle = eval != null && eval.getFormation() != null ? eval.getFormation().getTitle() : "?";
                retryObstacles.add(new LearningObstacleStats.ObstacleItem(
                        evalId, eval != null ? eval.getTitle() : "?", formationTitle, avgAttempt));
            }
        }

        Set<FormationUserPair> startedFromEvals = new HashSet<>();
        for (Object[] row : resultEvaluationRepository.findDistinctFormationUserPairsFromEvaluations()) {
            startedFromEvals.add(new FormationUserPair(((Number) row[0]).longValue(), ((Number) row[1]).longValue()));
        }
        Set<FormationUserPair> reachedExam = new HashSet<>();
        for (Object[] row : resultExamenRepository.findDistinctFormationUserPairsFromExams()) {
            reachedExam.add(new FormationUserPair(((Number) row[0]).longValue(), ((Number) row[1]).longValue()));
        }

        Map<Long, Long> dropOffCountByFormation = new HashMap<>();
        for (FormationUserPair p : startedFromEvals) {
            if (!reachedExam.contains(p)) {
                Optional<Examen> exOpt = examenService.findByFormationId(p.formationId);
                if (exOpt.isPresent()) {
                    dropOffCountByFormation.merge(p.formationId, 1L, Long::sum);
                }
            }
        }

        List<LearningObstacleStats.DropOffObstacleItem> dropOffObstacles = dropOffCountByFormation.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .map(e -> {
                    Formation f = formationService.findById(e.getKey()).orElse(null);
                    return new LearningObstacleStats.DropOffObstacleItem(
                            e.getKey(), f != null ? f.getTitle() : "?", e.getValue());
                })
                .sorted((a, b) -> Long.compare(b.getUserCount(), a.getUserCount()))
                .limit(TOP_N)
                .collect(Collectors.toList());

        return new LearningObstacleStats(evaluationObstacles, retryObstacles, dropOffObstacles);
    }

    private record FormationUserPair(long formationId, long userId) {}
}
