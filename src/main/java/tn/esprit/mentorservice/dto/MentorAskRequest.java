package tn.esprit.mentorservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record MentorAskRequest(
        @NotBlank String question,
        Long formationId,
        String locale,
        @Valid List<ChatTurnDto> conversationHistory,
        Long threadId     // optional — if set, continues that persisted thread
) {
}
