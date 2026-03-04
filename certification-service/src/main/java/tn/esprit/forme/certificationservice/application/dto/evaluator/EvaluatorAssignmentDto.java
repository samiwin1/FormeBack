package tn.esprit.forme.certificationservice.application.dto.evaluator;

import tn.esprit.forme.certificationservice.domain.enums.AssignmentStatus;

import java.time.LocalDateTime;

public record EvaluatorAssignmentDto(
        Long assignmentId,
        Long learnerId,
        String learnerName,
        AssignmentStatus status,
        Double oralScore,
        String oralComment,
        LocalDateTime gradedAt,
        boolean canGrade
) {}
