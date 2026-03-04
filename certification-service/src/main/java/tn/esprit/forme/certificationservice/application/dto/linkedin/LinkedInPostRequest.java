package tn.esprit.forme.certificationservice.application.dto.linkedin;

import jakarta.validation.constraints.NotNull;

public record LinkedInPostRequest(
        @NotNull
        Long issuedCertificationId
) {
}

