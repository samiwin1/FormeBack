package tn.esprit.mentorservice.dto;

import tn.esprit.mentorservice.domain.ExperienceLevel;
import tn.esprit.mentorservice.domain.LearningStyle;

import java.time.Instant;
import java.util.List;

public record PortfolioResponseDto(
        Long id,
        Long userId,
        String currentRole,
        String targetRole,
        ExperienceLevel experienceLevel,
        String bio,
        Integer weeklyStudyHours,
        String preferredLanguage,
        LearningStyle learningStyle,
        MentorExtendedPreferences extendedPreferences,
        List<SkillResponseDto> skills,
        Instant createdAt,
        Instant updatedAt
) {
}
