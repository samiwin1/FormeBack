package tn.esprit.forme.certificationservice.application.dto.reschedule;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateRescheduleRequest(
        @NotNull LocalDateTime proposedDatetime,
        @NotBlank String message
) {
}
