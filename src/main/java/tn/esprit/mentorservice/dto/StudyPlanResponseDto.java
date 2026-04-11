package tn.esprit.mentorservice.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record StudyPlanResponseDto(
        Long id,
        String title,
        Instant createdAt,
        LocalDate validUntil,
        String sourceChannel,
        List<StudyPlanItemResponseDto> items
) {}
