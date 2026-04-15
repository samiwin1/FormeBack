package tn.esprit.events_service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateParticipantScoreRequest {

    /** Admin mark / grade, 0–100 (same as {@link tn.esprit.events_service.entity.EventParticipant#getScore()}). */
    @NotNull
    @Min(0)
    @Max(100)
    private Integer score;
}
