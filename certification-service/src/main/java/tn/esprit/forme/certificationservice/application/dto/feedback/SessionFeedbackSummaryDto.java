package tn.esprit.forme.certificationservice.application.dto.feedback;

public record SessionFeedbackSummaryDto(
        Long sessionId,
        String sessionTitle,
        double avgSessionRating,
        double avgEvaluatorRating,
        int totalFeedbacks
) {
}

