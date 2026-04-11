package tn.esprit.mentorservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A skill gap identified in a learning-path response — skill not covered by any available formation.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SkillGap(
        String skillName,
        String currentLevel,
        String targetLevel,
        String suggestion
) {
}
