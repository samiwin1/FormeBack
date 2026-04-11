package tn.esprit.mentorservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Readiness assessment for one target certification in a learning-path response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CertificationReadiness(
        String certificationName,
        String targetDate,
        boolean onTrack,
        int remainingFormations,
        String riskLevel,
        String recommendation
) {
}
