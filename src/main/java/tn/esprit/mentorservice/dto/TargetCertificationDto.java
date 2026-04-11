package tn.esprit.mentorservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TargetCertificationDto(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 120) String provider,
        @Size(max = 32) String targetExamDate
) {
}
