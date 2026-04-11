package tn.esprit.mentorservice.dto.formation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

/**
 * TODO: requires endpoint in formation-service that returns all exam attempts for a user
 * across all formations including: formationId, formationTitle, score, passed, attemptDate, attemptNumber.
 * Currently a stub — not called by default; difficulty detection uses the existing
 * LearnerProgressSnapshotDto instead.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ExamAttemptDto(
        Long formationId,
        String formationTitle,
        Integer score,
        Boolean passed,
        LocalDateTime attemptDate,
        Integer attemptNumber
) {
}
