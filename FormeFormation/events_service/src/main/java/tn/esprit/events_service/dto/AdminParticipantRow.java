package tn.esprit.events_service.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/** Admin roster: one row per joined user, with optional deposit summary. */
@Data
@Builder
public class AdminParticipantRow {
    private Long participantId;
    private Long userId;
    private Instant joinedAt;
    private Integer score;
    private Long depositId;
    private String zipOriginalFilename;
    private String readmeOriginalFilename;
    private Instant submittedAt;
    private String readmeHint;
}
