package tn.esprit.forme.certificationservice.application.dto.feedback;

import java.time.LocalDateTime;

public record FeedbackResponse(
        Long id,
        Long learnerId,
        Long sessionId,
        Long issuedCertificationId,
        int sessionRating,
        int evaluatorRating,
        String comment,
        LocalDateTime submittedAt
) {
}

