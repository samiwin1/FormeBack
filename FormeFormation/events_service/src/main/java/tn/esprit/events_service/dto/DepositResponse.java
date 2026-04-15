package tn.esprit.events_service.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class DepositResponse {
    private Long depositId;
    private Long participantId;
    private Long userId;
    private String zipOriginalFilename;
    private String readmeOriginalFilename;
    private Instant submittedAt;
    private String readmeHint;
}
