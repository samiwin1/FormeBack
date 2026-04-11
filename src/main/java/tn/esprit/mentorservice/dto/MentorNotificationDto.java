package tn.esprit.mentorservice.dto;

import java.time.LocalDateTime;

public record MentorNotificationDto(
        Long id,
        String type,
        String title,
        String message,
        Long referenceId,
        boolean read,
        LocalDateTime createdAt
) {}
