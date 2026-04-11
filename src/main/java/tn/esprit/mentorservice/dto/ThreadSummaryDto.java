package tn.esprit.mentorservice.dto;

import java.time.LocalDateTime;

public record ThreadSummaryDto(
        Long id,
        String title,
        Long formationId,
        LocalDateTime updatedAt,
        long messageCount,
        String lastMessage
) {
}
