package tn.esprit.mentorservice.dto;

import tn.esprit.mentorservice.domain.MentorChannel;

import java.time.Instant;

public record MentorHistoryItemDto(
        Long sessionId,
        MentorChannel channel,
        Long formationId,
        Instant createdAt,
        String summary,
        String structuredJsonPreview,
        boolean bookmarked
) {
}
