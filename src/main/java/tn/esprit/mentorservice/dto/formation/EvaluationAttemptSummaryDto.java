package tn.esprit.mentorservice.dto.formation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EvaluationAttemptSummaryDto(
        Long evaluationId,
        Long formationId,
        Integer score,
        Boolean passed,
        Integer attemptNumber
) {
}
