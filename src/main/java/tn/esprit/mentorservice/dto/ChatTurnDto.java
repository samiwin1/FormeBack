package tn.esprit.mentorservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatTurnDto(
        @NotBlank @Size(max = 20) String role,
        @NotBlank @Size(max = 8000) String content
) {
}
