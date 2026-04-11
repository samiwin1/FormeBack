package tn.esprit.mentorservice.dto;

import jakarta.validation.constraints.Size;

public record CreateThreadRequest(
        @Size(max = 200) String title,
        Long formationId
) {
}
