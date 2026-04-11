package tn.esprit.mentorservice.dto;

import tn.esprit.mentorservice.domain.FeedbackRating;

import java.time.LocalDateTime;

public record FeedbackResponseDto(
        Long id,
        Long sessionId,
        FeedbackRating rating,
        String comment,
        LocalDateTime createdAt
) {
}
