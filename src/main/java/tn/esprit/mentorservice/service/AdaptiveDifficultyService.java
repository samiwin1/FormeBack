package tn.esprit.mentorservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprit.mentorservice.client.FormationProgressClient;
import tn.esprit.mentorservice.dto.DifficultyAlert;
import tn.esprit.mentorservice.dto.formation.ExamAttemptSummaryDto;
import tn.esprit.mentorservice.dto.formation.LearnerProgressSnapshotDto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdaptiveDifficultyService {

    private final FormationProgressClient formationProgressClient;

    /**
     * Analyses all exam attempts for the user and returns a list of difficulty alerts.
     * Gracefully returns empty list if formation-service is unavailable.
     */
    public List<DifficultyAlert> detectDifficulties(Long userId) {
        Optional<LearnerProgressSnapshotDto> snapshotOpt;
        try {
            snapshotOpt = formationProgressClient.fetchLearnerProgress(userId, null);
        } catch (Exception e) {
            log.warn("Could not fetch progress for difficulty detection: {}", e.getMessage());
            return List.of();
        }
        if (snapshotOpt.isEmpty() || snapshotOpt.get().examAttempts() == null) {
            return List.of();
        }

        List<ExamAttemptSummaryDto> attempts = snapshotOpt.get().examAttempts();
        if (attempts.isEmpty()) {
            return List.of();
        }

        // Group attempts by formationId
        Map<Long, List<ExamAttemptSummaryDto>> byFormation = attempts.stream()
                .filter(a -> a.formationId() != null)
                .collect(Collectors.groupingBy(ExamAttemptSummaryDto::formationId));

        List<DifficultyAlert> alerts = new ArrayList<>();
        for (Map.Entry<Long, List<ExamAttemptSummaryDto>> entry : byFormation.entrySet()) {
            Long formationId = entry.getKey();
            List<ExamAttemptSummaryDto> fAttempts = entry.getValue();
            String title = fAttempts.stream()
                    .map(ExamAttemptSummaryDto::examTitle)
                    .filter(t -> t != null && !t.isBlank())
                    .findFirst()
                    .orElse("Formation #" + formationId);

            // Pattern 1: Repeated failures (2+ failed attempts)
            long failCount = fAttempts.stream()
                    .filter(a -> Boolean.FALSE.equals(a.passed()))
                    .count();
            if (failCount >= 2) {
                Double lastScore = fAttempts.stream()
                        .filter(a -> a.score() != null)
                        .mapToInt(ExamAttemptSummaryDto::score)
                        .boxed()
                        .reduce((first, second) -> second)
                        .map(Integer::doubleValue)
                        .orElse(null);
                alerts.add(new DifficultyAlert(
                        "REPEATED_FAILURE",
                        failCount >= 3 ? "HIGH" : "MEDIUM",
                        formationId,
                        title,
                        "You have failed the exam for '" + title + "' " + failCount + " time(s). Consider requesting a remediation plan.",
                        (int) failCount,
                        lastScore,
                        null
                ));
                continue; // skip lower-severity checks for this formation
            }

            // Pattern 2: Declining scores (2+ completed attempts with decreasing scores)
            List<Integer> scores = fAttempts.stream()
                    .filter(a -> a.score() != null && a.completed())
                    .map(ExamAttemptSummaryDto::score)
                    .collect(Collectors.toList());
            if (scores.size() >= 2 && isDeclining(scores)) {
                alerts.add(new DifficultyAlert(
                        "DECLINING_SCORES",
                        "MEDIUM",
                        formationId,
                        title,
                        "Your scores on '" + title + "' are trending downward. Last score: " + scores.get(scores.size() - 1) + "%.",
                        0,
                        scores.get(scores.size() - 1).doubleValue(),
                        null
                ));
                continue;
            }

            // Pattern 3: Low pass score (passed but < 70%)
            fAttempts.stream()
                    .filter(a -> Boolean.TRUE.equals(a.passed()) && a.score() != null && a.score() < 70)
                    .max(Comparator.comparingInt(a -> a.score() == null ? 0 : a.score()))
                    .ifPresent(a -> alerts.add(new DifficultyAlert(
                            "LOW_PASS_SCORE",
                            "LOW",
                            formationId,
                            title,
                            "You passed '" + title + "' with a low score of " + a.score() + "%. There may be knowledge gaps worth addressing.",
                            0,
                            a.score().doubleValue(),
                            null
                    )));
        }

        return alerts;
    }

    /** Returns true if the score list has a net downward trend (last < first). */
    private static boolean isDeclining(List<Integer> scores) {
        return scores.get(scores.size() - 1) < scores.get(0);
    }
}
