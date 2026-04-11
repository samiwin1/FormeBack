package tn.esprit.mentorservice.dto.formation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LearnerProgressSnapshotDto(
        Long userId,
        Long formationIdFilter,
        List<ExamAttemptSummaryDto> examAttempts,
        List<EvaluationAttemptSummaryDto> evaluationAttempts,
        String notes
) {
}
