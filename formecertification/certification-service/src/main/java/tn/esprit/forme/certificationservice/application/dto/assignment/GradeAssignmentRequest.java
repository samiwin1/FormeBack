package tn.esprit.forme.certificationservice.application.dto.assignment;

import jakarta.validation.constraints.*;

public record GradeAssignmentRequest(
        @NotNull @DecimalMin("0.0") @DecimalMax("100.0") Double oralScore,
        String evaluatorComment
) {
}
