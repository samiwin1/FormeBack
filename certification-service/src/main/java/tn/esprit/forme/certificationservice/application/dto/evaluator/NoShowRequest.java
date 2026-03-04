package tn.esprit.forme.certificationservice.application.dto.evaluator;

import jakarta.validation.constraints.NotNull;

public record NoShowRequest(
        @NotNull
        Long assignmentId
) {}
