package tn.esprit.events_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tn.esprit.events_service.entity.EventPartner;
import tn.esprit.events_service.entity.EventTier;
import tn.esprit.events_service.exception.BusinessException;
import tn.esprit.events_service.service.EventService;

import java.util.Map;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventPartnerController {

    private final EventService eventService;

    @PostMapping("/{eventId}/partners/buy")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> buyTier(
            @PathVariable Long eventId,
            @RequestParam String tier,
            @RequestHeader("X-User-Id") Long sponsorUserId,
            @RequestHeader(value = "X-Partner-Email", required = false) String sponsorEmail
    ) {
        EventTier eventTier;
        try {
            eventTier = EventTier.valueOf(tier.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("Invalid tier. Use VIP, GOLD, or SILVER.");
        }
        EventPartner saved = eventService.buyPartnerTier(eventId, sponsorUserId, eventTier, sponsorEmail);
        return Map.of(
                "id", saved.getId(),
                "eventId", eventId,
                "sponsorUserId", sponsorUserId,
                "tier", saved.getTier().name()
        );
    }
}
