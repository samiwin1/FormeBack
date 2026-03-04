package tn.esprit.forme.certificationservice.application.dto.reschedule;

import tn.esprit.forme.certificationservice.domain.enums.RescheduleStatus;

import java.time.LocalDateTime;

public record RescheduleResponse(
        Long id,
        Long assignmentId,
        LocalDateTime proposedDatetime,
        String message,
        LocalDateTime requestedAt,
        LocalDateTime decidedAt,
        String adminComment,
        RescheduleStatus status
) {
}
