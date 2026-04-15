package tn.esprit.events_service.service;

import tn.esprit.events_service.dto.*;
import tn.esprit.events_service.entity.EventPartner;
import tn.esprit.events_service.entity.EventParticipant;
import tn.esprit.events_service.entity.EventTier;

import java.util.List;

public interface EventService {

    EventFullResponse create(CreateEventRequest request);

    EventFullResponse update(Long eventId, UpdateEventRequest request);

    void delete(Long eventId);

    List<EventFullResponse> listAll();

    /** Public catalog; when {@code viewerUserId} is set, each row includes joined/score for that user. */
    List<EventFullResponse> listCatalog(Long viewerUserId);

    /**
     * Full details for admins, after start, registered participants, or users who purchased a sponsor tier;
     * teaser before start otherwise.
     */
    Object getForViewer(Long eventId, ViewerRole role, Long viewerUserId);

    EventParticipant join(Long eventId, Long userId);

    /** {@code sponsorUserId} is the platform user id (stored in {@code event_partners.partner_id}). */
    EventPartner buyPartnerTier(Long eventId, Long sponsorUserId, EventTier tier, String sponsorEmailOptional);

    List<TopParticipantRow> getTopParticipants(Long eventId, ViewerRole role, Long sponsorUserId);

    /** All users who joined the event, with submission metadata when present. Admin-only via controller. */
    List<AdminParticipantRow> listParticipantsForAdmin(Long eventId);

    /** Same rows as admin roster; only callers with a sponsor row for this event (or admin via other endpoints). */
    List<AdminParticipantRow> listParticipantsForSponsor(Long eventId, Long sponsorUserId);

    /** Set or update admin mark (0–100) for a participant that belongs to the event. */
    AdminParticipantRow updateParticipantScore(Long eventId, Long participantId, int score);
}
