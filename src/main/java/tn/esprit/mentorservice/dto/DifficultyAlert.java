package tn.esprit.mentorservice.dto;

import java.time.LocalDateTime;

public record DifficultyAlert(
        String alertType,          // REPEATED_FAILURE | DECLINING_SCORES | LOW_PASS_SCORE | STALLED_PROGRESS
        String severity,           // HIGH | MEDIUM | LOW
        Long formationId,
        String formationTitle,
        String description,
        int failedAttempts,
        Double lastScore,
        LocalDateTime lastAttemptDate
) {
}
