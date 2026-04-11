package tn.esprit.mentorservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record HeldCertificationDto(
        @NotBlank @Size(max = 200) String name,
        @Min(1970) @Max(2100) Integer year
) {
}
