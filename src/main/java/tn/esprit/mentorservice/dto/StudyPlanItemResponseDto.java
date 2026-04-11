package tn.esprit.mentorservice.dto;

import java.time.LocalDateTime;

public record StudyPlanItemResponseDto(
        Long id,
        Integer sortOrder,
        String itemType,
        String title,
        Integer durationMinutes,
        String resourceRef,
        boolean completed,
        LocalDateTime completedAt
) {}
