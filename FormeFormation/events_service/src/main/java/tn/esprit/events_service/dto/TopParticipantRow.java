package tn.esprit.events_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopParticipantRow {
    private Long participantId;
    private Long userId;
    private Integer score;
    private String readmeContent;
    private Long depositId;
}
