package tn.esprit.forme.certificationservice.application.dto.dashboard;

public record PassedOralWithoutCertificateDto(
        Long assignmentId,
        Long learnerId,
        String learnerName,
        Long oralSessionId,
        Long certificationId,
        Long formationId,
        Double oralScore,
        Double writtenScore,
        Double finalScore,
        String status
) {
}
