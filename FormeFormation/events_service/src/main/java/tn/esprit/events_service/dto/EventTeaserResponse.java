package tn.esprit.events_service.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/** Shown before {@code startDate} to non-admin viewers (details unlock when the event starts). */
@Data
@Builder
public class EventTeaserResponse {
    private Long id;
    private String title;
    private Instant startDate;
    private Instant deadline;
    private String message;
    /** True when the viewer already registered as a participant (pre-start waiting state). */
    @Builder.Default
    private boolean viewerHasJoined = false;
    private Integer viewerScore;
    private int maxVip;
    private int maxGold;
    private int maxSilver;
    private int vipPrice;
    private int goldPrice;
    private int silverPrice;
    private int currentVip;
    private int currentGold;
    private int currentSilver;
}
