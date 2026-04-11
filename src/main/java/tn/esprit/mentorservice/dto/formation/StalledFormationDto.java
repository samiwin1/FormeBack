package tn.esprit.mentorservice.dto.formation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

/**
 * TODO: requires endpoint in formation-service that returns formations where the user has been
 * enrolled 14+ days with less than 10% module progress since last activity.
 * Currently a stub — not called by default; stalled-progress detection is not yet active.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record StalledFormationDto(
        Long formationId,
        String formationTitle,
        LocalDateTime enrolledAt,
        double moduleProgressPercent,
        LocalDateTime lastActivityAt
) {
}
