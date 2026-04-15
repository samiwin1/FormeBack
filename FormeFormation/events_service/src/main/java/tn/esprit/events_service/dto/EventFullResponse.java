package tn.esprit.events_service.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class EventFullResponse {
    private Long id;
    private String title;
    private String description;
    private String requirements;
    private String successMetrics;
    private Instant startDate;
    private Instant deadline;
    private int maxVip;
    private int maxGold;
    private int maxSilver;
    private int vipPrice;
    private int goldPrice;
    private int silverPrice;
    private int currentVip;
    private int currentGold;
    private int currentSilver;
    private long participantCount;
    /** True when the authenticated viewer (X-User-Id) is registered as a participant for this event. */
    private boolean viewerHasJoined;
    /** True when the viewer has purchased a sponsorship tier for this event (same row as participant is disallowed). */
    @Builder.Default
    private boolean viewerIsSponsor = false;
    /** Admin-assigned score for this viewer when they joined; null if not graded or not a participant. */
    private Integer viewerScore;
    private Instant createdAt;
    private Instant updatedAt;
}
