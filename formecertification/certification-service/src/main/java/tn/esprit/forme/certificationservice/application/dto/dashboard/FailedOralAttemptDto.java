package tn.esprit.forme.certificationservice.application.dto.dashboard;

public record FailedOralAttemptDto(
        Long assignmentId,
        Long learnerId,
        String learnerName,
        Long oralSessionId,
        Long certificationId,
        Long formationId,
        Integer attemptNumber,
        Double oralScore,
        String status
) {
}
