package tn.esprit.forme.certificationservice.application.dto.assignment;

import tn.esprit.forme.certificationservice.domain.enums.AssignmentStatus;

import java.time.LocalDateTime;

public record OralAssignmentResponse(
        Long id,
        Long oralSessionId,
        Long learnerId,
        AssignmentStatus status,
        Double oralScore,
        String evaluatorComment,
        LocalDateTime gradedAt,
        Integer attemptNumber,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
