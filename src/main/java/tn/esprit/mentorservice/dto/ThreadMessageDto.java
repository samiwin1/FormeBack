package tn.esprit.mentorservice.dto;

import java.time.LocalDateTime;

public record ThreadMessageDto(
        Long id,
        String role,       // USER or ASSISTANT
        String content,
        Long sessionId,    // for assistant messages — used by the feedback component
        LocalDateTime createdAt
) {
}
