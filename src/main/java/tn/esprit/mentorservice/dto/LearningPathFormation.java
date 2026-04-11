package tn.esprit.mentorservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * One recommended formation in a learning-path AI response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LearningPathFormation(
        Long formationId,
        String title,
        int priority,
        String reason,
        int estimatedWeeks,
        boolean prerequisiteMet,
        String status
) {
}
