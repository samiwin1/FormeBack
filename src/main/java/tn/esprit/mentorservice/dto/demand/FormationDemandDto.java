package tn.esprit.mentorservice.dto.demand;

import java.time.Instant;

public record FormationDemandDto(
        Long id,
        Long userId,
        String requestedRole,
        String detectedSkillsGap,
        Instant createdAt
) {}
