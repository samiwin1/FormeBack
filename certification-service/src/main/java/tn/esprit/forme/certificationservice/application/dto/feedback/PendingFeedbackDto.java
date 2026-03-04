package tn.esprit.forme.certificationservice.application.dto.feedback;

public record PendingFeedbackDto(
        boolean hasPending,
        Long issuedCertificationId,
        Long sessionId
) {
}

