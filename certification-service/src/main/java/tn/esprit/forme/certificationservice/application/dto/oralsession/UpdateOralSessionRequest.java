package tn.esprit.forme.certificationservice.application.dto.oralsession;

import jakarta.validation.constraints.*;
import tn.esprit.forme.certificationservice.domain.enums.MeetingProvider;
import tn.esprit.forme.certificationservice.domain.enums.OralSessionStatus;

import java.time.LocalDateTime;

public record UpdateOralSessionRequest(
        @NotBlank String title,
        @NotNull LocalDateTime scheduledAt,
        @NotNull @Positive Integer durationMinutes,
        @NotNull MeetingProvider meetingProvider,
        @NotBlank String meetingLink,
        @NotNull Long evaluatorId,
        @NotNull OralSessionStatus status
) {
}
