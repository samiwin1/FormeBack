package tn.esprit.mentorservice.dto;

import tn.esprit.mentorservice.domain.SelfAssessedSkillLevel;

public record SkillResponseDto(String skillName, SelfAssessedSkillLevel level) {
}
