package tn.esprit.forme.certificationservice.application.dto.certification;

import jakarta.validation.constraints.*;

public record UpdateCertificationRequest(
        @NotBlank String title,
        String domain,
        String provider,
        String level,
        @NotNull @Positive Integer validityMonths,
        @NotNull @DecimalMin("0.0") @DecimalMax("100.0") Double thresholdFinal,
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double weightWritten,
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double weightOral
) {
}
