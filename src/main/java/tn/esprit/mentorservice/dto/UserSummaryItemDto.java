package tn.esprit.mentorservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.mentorservice.domain.ExperienceLevel;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryItemDto {
    private Long userId;
    private String currentRole;
    private String targetRole;
    private ExperienceLevel experienceLevel;
    private Instant createdAt;
}