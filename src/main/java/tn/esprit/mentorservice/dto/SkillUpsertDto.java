package tn.esprit.mentorservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import tn.esprit.mentorservice.domain.SelfAssessedSkillLevel;

public record SkillUpsertDto(
        @NotBlank String skillName,
        @NotNull SelfAssessedSkillLevel level
) {
}
