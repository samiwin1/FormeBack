package tn.esprit.forme.certificationservice.application.dto.assignment;

import jakarta.validation.constraints.NotNull;

public record CreateAssignmentRequest(
        @NotNull Long oralSessionId,
        @NotNull Long learnerId,
        @NotNull Long formationId
) {
}
