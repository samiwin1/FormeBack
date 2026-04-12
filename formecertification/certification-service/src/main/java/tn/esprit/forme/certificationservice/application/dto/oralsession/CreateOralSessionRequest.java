package tn.esprit.forme.certificationservice.application.dto.oralsession;

import jakarta.validation.constraints.*;
import tn.esprit.forme.certificationservice.domain.enums.MeetingProvider;

import java.time.LocalDateTime;

public record CreateOralSessionRequest(
        @NotNull Long certificationId,
        @NotBlank String title,
        @NotNull LocalDateTime scheduledAt,
        @NotNull @Positive Integer durationMinutes,
        @NotNull MeetingProvider meetingProvider,
        @NotBlank String meetingLink,
        @NotNull Long evaluatorId
) {
}
