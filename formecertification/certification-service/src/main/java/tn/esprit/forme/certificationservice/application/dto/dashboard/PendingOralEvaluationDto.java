package tn.esprit.forme.certificationservice.application.dto.dashboard;

import java.time.LocalDateTime;

public record PendingOralEvaluationDto(
        Long assignmentId,
        Long learnerId,
        String learnerName,
        Integer attemptNumber,
        Long oralSessionId,
        String certificationTitle,
        LocalDateTime scheduledAt,
        String meetingLink,
        Long evaluatorId,
        String evaluatorName,
        String status
) {
}
