package tn.esprit.mentorservice.dto.formation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExamAttemptSummaryDto(
        Long examenId,
        Long formationId,
        String examTitle,
        Integer score,
        Boolean passed,
        boolean completed
) {
}
