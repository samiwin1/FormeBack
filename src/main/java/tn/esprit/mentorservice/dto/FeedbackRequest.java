package tn.esprit.mentorservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tn.esprit.mentorservice.domain.FeedbackRating;

public record FeedbackRequest(
        @NotNull Long sessionId,
        @NotNull FeedbackRating rating,
        @Size(max = 1000) String comment
) {
}
