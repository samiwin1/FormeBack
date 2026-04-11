package tn.esprit.mentorservice.dto;

public record MentorAdviceResponseDto(
        Long sessionId,
        String summary,
        String structuredJson,
        String rawText,
        Long threadId     // set only for ASK channel; null for all other channels
) {
}
