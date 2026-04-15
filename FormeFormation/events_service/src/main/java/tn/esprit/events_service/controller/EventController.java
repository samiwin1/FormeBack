package tn.esprit.events_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tn.esprit.events_service.dto.*;
import tn.esprit.events_service.entity.EventParticipant;
import tn.esprit.events_service.exception.ForbiddenException;
import tn.esprit.events_service.service.EventService;
import tn.esprit.events_service.util.RequestHeaderUtil;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    /** Public catalog — declared before /{eventId} so "catalog" is never parsed as an id. */
    @GetMapping("/catalog")
    public List<EventFullResponse> catalog(
            @RequestHeader(value = "X-User-Id", required = false) Long viewerUserId
    ) {
        return eventService.listCatalog(viewerUserId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullResponse create(
            @Valid @RequestBody CreateEventRequest request,
            @RequestHeader(value = "X-Viewer-Role", required = false) String roleHeader
    ) {
        requireAdmin(roleHeader);
        return eventService.create(request);
    }

    @DeleteMapping("/{eventId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long eventId,
            @RequestHeader(value = "X-Viewer-Role", required = false) String roleHeader
    ) {
        requireAdmin(roleHeader);
        eventService.delete(eventId);
    }

    @GetMapping
    public List<EventFullResponse> list(
            @RequestHeader(value = "X-Viewer-Role", required = false) String roleHeader
    ) {
        requireAdmin(roleHeader);
        return eventService.listAll();
    }

    /**
     * More specific paths must stay before {@code /{eventId}} so "participants" is not parsed as an id.
     */
    @GetMapping("/{eventId}/participants")
    public List<AdminParticipantRow> listParticipantsForAdmin(
            @PathVariable Long eventId,
            @RequestHeader(value = "X-Viewer-Role", required = false) String roleHeader
    ) {
        requireAdmin(roleHeader);
        return eventService.listParticipantsForAdmin(eventId);
    }

    /** Read-only participant roster for users who purchased a sponsorship tier on this event. */
    @GetMapping("/{eventId}/sponsor/participants")
    public List<AdminParticipantRow> listParticipantsForSponsor(
            @PathVariable Long eventId,
            @RequestHeader("X-User-Id") Long sponsorUserId
    ) {
        return eventService.listParticipantsForSponsor(eventId, sponsorUserId);
    }

    /**
     * PUT and PATCH supported: some proxies strip or mishandle PATCH; PUT is universally forwarded.
     */
    @RequestMapping(
            value = "/{eventId}/participants/{participantId}/score",
            method = {RequestMethod.PUT, RequestMethod.PATCH}
    )
    public AdminParticipantRow updateParticipantScore(
            @PathVariable Long eventId,
            @PathVariable Long participantId,
            @Valid @RequestBody UpdateParticipantScoreRequest request,
            @RequestHeader(value = "X-Viewer-Role", required = false) String roleHeader
    ) {
        requireAdmin(roleHeader);
        return eventService.updateParticipantScore(eventId, participantId, request.getScore());
    }

    @PutMapping("/{eventId}")
    public EventFullResponse update(
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventRequest request,
            @RequestHeader(value = "X-Viewer-Role", required = false) String roleHeader
    ) {
        requireAdmin(roleHeader);
        return eventService.update(eventId, request);
    }

    @GetMapping("/{eventId}")
    public Object getOne(
            @PathVariable Long eventId,
            @RequestHeader(value = "X-Viewer-Role", required = false) String roleHeader,
            @RequestHeader(value = "X-User-Id", required = false) Long viewerUserId
    ) {
        ViewerRole role = RequestHeaderUtil.parseViewerRole(roleHeader);
        return eventService.getForViewer(eventId, role, viewerUserId);
    }

    @PostMapping("/{eventId}/join")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> join(
            @PathVariable Long eventId,
            @RequestHeader("X-User-Id") Long userId
    ) {
        EventParticipant p = eventService.join(eventId, userId);
        return Map.of(
                "participantId", p.getId(),
                "eventId", eventId,
                "userId", userId
        );
    }

    @GetMapping("/{eventId}/top-participants")
    public List<TopParticipantRow> topParticipants(
            @PathVariable Long eventId,
            @RequestHeader(value = "X-Viewer-Role", required = false) String roleHeader,
            @RequestHeader(value = "X-User-Id", required = false) Long sponsorUserId
    ) {
        ViewerRole role = RequestHeaderUtil.parseViewerRole(roleHeader);
        return eventService.getTopParticipants(eventId, role, sponsorUserId);
    }

    private static void requireAdmin(String roleHeader) {
        if (RequestHeaderUtil.parseViewerRole(roleHeader) != ViewerRole.ADMIN) {
            throw new ForbiddenException("Admin only.");
        }
    }
}
