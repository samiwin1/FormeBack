package tn.esprit.forme.certificationservice.application.dto.dashboard;

import java.time.LocalDateTime;

public record RescheduleAdminItemDto(
        Long id,
        Long assignmentId,
        Long learnerId,
        String learnerName,
        Long sessionId,
        LocalDateTime sessionScheduledAt,
        LocalDateTime proposedDatetime,
        String message,
        LocalDateTime requestedAt,
        String status
) {
}
