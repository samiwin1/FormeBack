package tn.esprit.forme.certificationservice.application.dto.evaluator;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GradeSubmissionRequest(
        @NotNull
        @Min(0)
        @Max(20)
        Double oralScore,
        
        @Size(max = 500)
        String oralComment
) {}
