package tn.esprit.mentorservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import tn.esprit.mentorservice.domain.ExperienceLevel;
import tn.esprit.mentorservice.domain.LearningStyle;

import java.util.List;

public record PortfolioUpsertRequest(
        String currentRole,
        String targetRole,
        @NotNull ExperienceLevel experienceLevel,
        String bio,
        Integer weeklyStudyHours,
        String preferredLanguage,
        @NotNull LearningStyle learningStyle,
        @Valid MentorExtendedPreferences extendedPreferences,
        @Valid List<SkillUpsertDto> skills
) {
}
