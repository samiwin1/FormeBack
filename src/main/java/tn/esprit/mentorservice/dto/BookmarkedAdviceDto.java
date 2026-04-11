package tn.esprit.mentorservice.dto;

import tn.esprit.mentorservice.domain.MentorChannel;

import java.time.Instant;

public record BookmarkedAdviceDto(
        Long bookmarkId,
        Long sessionId,
        MentorChannel channel,
        Long formationId,
        Instant sessionCreatedAt,
        Instant bookmarkedAt,
        String note,
        String summary,
        String structuredJsonPreview
) {
}
