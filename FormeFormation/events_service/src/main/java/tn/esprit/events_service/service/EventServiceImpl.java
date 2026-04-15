package tn.esprit.events_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.events_service.dto.*;
import tn.esprit.events_service.entity.Event;
import tn.esprit.events_service.entity.EventDeposit;
import tn.esprit.events_service.entity.EventParticipant;
import tn.esprit.events_service.entity.EventPartner;
import tn.esprit.events_service.entity.EventTier;
import tn.esprit.events_service.exception.BusinessException;
import tn.esprit.events_service.exception.ForbiddenException;
import tn.esprit.events_service.exception.ResourceNotFoundException;
import tn.esprit.events_service.exception.TierFullException;
import tn.esprit.events_service.notification.EventEmailService;
import tn.esprit.events_service.repository.EventDepositRepository;
import tn.esprit.events_service.repository.EventParticipantRepository;
import tn.esprit.events_service.repository.EventPartnerRepository;
import tn.esprit.events_service.repository.EventRepository;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private static final String TEASER_MESSAGE = "Full description, requirements, and success metrics will be visible when the event starts.";

    private static final String JOINED_WAITING_MESSAGE =
            "You successfully joined this event. Come back when it starts to view the full brief, requirements, and submission workspace.";

    private final EventRepository eventRepository;
    private final EventPartnerRepository eventPartnerRepository;
    private final EventParticipantRepository eventParticipantRepository;
    private final EventDepositRepository eventDepositRepository;
    private final EventEmailService eventEmailService;

    @Override
    @Transactional
    public EventFullResponse create(CreateEventRequest request) {
        validateEventDates(request.getStartDate(), request.getDeadline());
        Event e = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .requirements(request.getRequirements())
                .successMetrics(request.getSuccessMetrics())
                .startDate(request.getStartDate())
                .deadline(request.getDeadline())
                .maxVip(request.getMaxVip())
                .maxGold(request.getMaxGold())
                .maxSilver(request.getMaxSilver())
                .vipPrice(request.getVipPrice())
                .goldPrice(request.getGoldPrice())
                .silverPrice(request.getSilverPrice())
                .build();
        return toFull(eventRepository.save(e));
    }

    @Override
    @Transactional
    public EventFullResponse update(Long eventId, UpdateEventRequest request) {
        Event e = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));
        validateEventDates(request.getStartDate(), request.getDeadline());
        e.setTitle(request.getTitle());
        e.setDescription(request.getDescription());
        e.setRequirements(request.getRequirements());
        e.setSuccessMetrics(request.getSuccessMetrics());
        e.setStartDate(request.getStartDate());
        e.setDeadline(request.getDeadline());
        e.setMaxVip(request.getMaxVip());
        e.setMaxGold(request.getMaxGold());
        e.setMaxSilver(request.getMaxSilver());
        e.setVipPrice(request.getVipPrice());
        e.setGoldPrice(request.getGoldPrice());
        e.setSilverPrice(request.getSilverPrice());
        return toFull(eventRepository.save(e));
    }

    @Override
    @Transactional
    public void delete(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event not found: " + eventId);
        }
        eventRepository.deleteById(eventId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullResponse> listAll() {
        return eventRepository.findAll().stream().map(this::toFull).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullResponse> listCatalog(Long viewerUserId) {
        Instant now = Instant.now();
        return eventRepository.findAll().stream()
                .map(e -> {
                    EventFullResponse full = toFull(e);
                    if (viewerUserId != null) {
                        if (eventPartnerRepository.existsByEventIdAndPartnerId(e.getId(), viewerUserId)) {
                            full.setViewerIsSponsor(true);
                        }
                        eventParticipantRepository.findByEventIdAndUserId(e.getId(), viewerUserId)
                                .ifPresent(p -> {
                                    full.setViewerHasJoined(true);
                                    full.setViewerScore(p.getScore());
                                    boolean started = !now.isBefore(e.getStartDate());
                                    if (!started) {
                                        full.setDescription("");
                                        full.setRequirements("");
                                        full.setSuccessMetrics("");
                                    }
                                });
                    }
                    return full;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Object getForViewer(Long eventId, ViewerRole role, Long viewerUserId) {
        Event e = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));
        Instant now = Instant.now();
        boolean started = !now.isBefore(e.getStartDate());
        boolean isParticipant = viewerUserId != null
                && eventParticipantRepository.findByEventIdAndUserId(eventId, viewerUserId).isPresent();
        boolean isSponsor = viewerUserId != null
                && eventPartnerRepository.existsByEventIdAndPartnerId(eventId, viewerUserId);

        if (role == ViewerRole.ADMIN || started || isSponsor) {
            EventFullResponse full = toFull(e);
            full.setViewerHasJoined(isParticipant);
            full.setViewerIsSponsor(isSponsor);
            applyParticipantScore(full, eventId, viewerUserId, isParticipant);
            return full;
        }
        if (!started && isParticipant) {
            Integer score = viewerUserId == null
                    ? null
                    : eventParticipantRepository.findByEventIdAndUserId(eventId, viewerUserId)
                            .map(EventParticipant::getScore)
                            .orElse(null);
            return publicTeaser(e, JOINED_WAITING_MESSAGE, true, score);
        }
        return publicTeaser(e, TEASER_MESSAGE, false, null);
    }

    @Override
    @Transactional
    public EventParticipant join(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));
        if (eventPartnerRepository.existsByEventIdAndPartnerId(eventId, userId)) {
            throw new BusinessException("Sponsors cannot register as participants for the same event.");
        }
        if (eventParticipantRepository.findByEventIdAndUserId(eventId, userId).isPresent()) {
            throw new BusinessException("User already joined this event.");
        }
        EventParticipant p = EventParticipant.builder()
                .event(event)
                .userId(userId)
                .build();
        return eventParticipantRepository.save(p);
    }

    @Override
    @Transactional
    public EventPartner buyPartnerTier(Long eventId, Long sponsorUserId, EventTier tier, String sponsorEmail) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));

        if (eventParticipantRepository.findByEventIdAndUserId(eventId, sponsorUserId).isPresent()) {
            throw new BusinessException("Participants cannot purchase a sponsorship tier for an event they joined as a competitor.");
        }

        int tierPrice = switch (tier) {
            case VIP -> event.getVipPrice();
            case GOLD -> event.getGoldPrice();
            case SILVER -> event.getSilverPrice();
        };
        if (tierPrice <= 0) {
            throw new BusinessException("This sponsorship tier is not offered for this event (price not set).");
        }

        if (eventPartnerRepository.existsByEventIdAndPartnerId(eventId, sponsorUserId)) {
            throw new BusinessException("You already sponsor this event.");
        }

        int max = switch (tier) {
            case VIP -> event.getMaxVip();
            case GOLD -> event.getMaxGold();
            case SILVER -> event.getMaxSilver();
        };

        long current = eventPartnerRepository.countByEventIdAndTier(eventId, tier);
        if (current >= max) {
            eventEmailService.sendTierFullNotice(sponsorUserId, sponsorEmail, event.getTitle(), tier);
            throw new TierFullException("This partner tier is full for this event.");
        }

        EventPartner saved = eventPartnerRepository.save(EventPartner.builder()
                .event(event)
                .partnerId(sponsorUserId)
                .tier(tier)
                .build());

        eventEmailService.sendPartnerTierConfirmation(sponsorUserId, sponsorEmail, event.getTitle(), tier);
        return saved;
    }

    @Override
    @Transactional
    public AdminParticipantRow updateParticipantScore(Long eventId, Long participantId, int score) {
        EventParticipant p = eventParticipantRepository.findById(participantId)
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found: " + participantId));
        if (!p.getEvent().getId().equals(eventId)) {
            throw new BusinessException("Participant does not belong to this event.");
        }
        p.setScore(score);
        eventParticipantRepository.save(p);

        var depOpt = eventDepositRepository.findByParticipantId(p.getId());
        var d = depOpt.orElse(null);
        return AdminParticipantRow.builder()
                .participantId(p.getId())
                .userId(p.getUserId())
                .joinedAt(p.getJoinedAt())
                .score(p.getScore())
                .depositId(d != null ? d.getId() : null)
                .zipOriginalFilename(d != null ? d.getZipOriginalFilename() : null)
                .readmeOriginalFilename(d != null ? d.getReadmeOriginalFilename() : null)
                .submittedAt(d != null ? d.getSubmittedAt() : null)
                .readmeHint(d != null ? EventDepositServiceImpl.README_DETAIL_HINT : null)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminParticipantRow> listParticipantsForAdmin(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event not found: " + eventId);
        }
        return buildAdminParticipantRows(eventId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminParticipantRow> listParticipantsForSponsor(Long eventId, Long sponsorUserId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event not found: " + eventId);
        }
        if (sponsorUserId == null) {
            throw new ForbiddenException("Sign in is required to view the participant roster.");
        }
        if (!eventPartnerRepository.existsByEventIdAndPartnerId(eventId, sponsorUserId)) {
            throw new ForbiddenException("Only event sponsors can view this roster.");
        }
        return buildAdminParticipantRows(eventId);
    }

    private List<AdminParticipantRow> buildAdminParticipantRows(Long eventId) {
        return eventParticipantRepository.findByEventId(eventId).stream()
                .map(p -> {
                    var depOpt = eventDepositRepository.findByParticipantId(p.getId());
                    var d = depOpt.orElse(null);
                    return AdminParticipantRow.builder()
                            .participantId(p.getId())
                            .userId(p.getUserId())
                            .joinedAt(p.getJoinedAt())
                            .score(p.getScore())
                            .depositId(d != null ? d.getId() : null)
                            .zipOriginalFilename(d != null ? d.getZipOriginalFilename() : null)
                            .readmeOriginalFilename(d != null ? d.getReadmeOriginalFilename() : null)
                            .submittedAt(d != null ? d.getSubmittedAt() : null)
                            .readmeHint(d != null ? EventDepositServiceImpl.README_DETAIL_HINT : null)
                            .build();
                })
                .sorted(Comparator.comparing(AdminParticipantRow::getJoinedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopParticipantRow> getTopParticipants(Long eventId, ViewerRole role, Long sponsorUserId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event not found: " + eventId);
        }

        if (role != ViewerRole.ADMIN) {
            if (sponsorUserId == null) {
                throw new ForbiddenException("Sign in is required to view top participants.");
            }
            if (!eventPartnerRepository.existsByEventIdAndPartnerId(eventId, sponsorUserId)) {
                throw new ForbiddenException("Only event sponsors or admins can view top participants.");
            }
        }

        List<EventParticipant> scored = eventParticipantRepository.findByEventId(eventId).stream()
                .filter(p -> p.getScore() != null)
                .sorted(Comparator.comparing(EventParticipant::getScore).reversed())
                .collect(Collectors.toList());

        if (scored.isEmpty()) {
            return List.of();
        }

        int n = scored.size();
        int topCount = (int) Math.ceil(n * 0.25d);
        topCount = Math.max(1, topCount);
        topCount = Math.min(topCount, n);

        return scored.stream().limit(topCount).map(this::toTopRow).collect(Collectors.toList());
    }

    private TopParticipantRow toTopRow(EventParticipant p) {
        var depOpt = eventDepositRepository.findByParticipantId(p.getId());
        return TopParticipantRow.builder()
                .participantId(p.getId())
                .userId(p.getUserId())
                .score(p.getScore())
                .readmeContent(depOpt.map(d -> d.getReadmeContent()).orElse(null))
                .depositId(depOpt.map(EventDeposit::getId).orElse(null))
                .build();
    }

    private void validateEventDates(Instant start, Instant deadline) {
        if (!deadline.isAfter(start)) {
            throw new BusinessException("Deadline must be after start date.");
        }
    }

    private void applyParticipantScore(
            EventFullResponse full,
            Long eventId,
            Long viewerUserId,
            boolean isParticipant
    ) {
        if (isParticipant && viewerUserId != null) {
            eventParticipantRepository.findByEventIdAndUserId(eventId, viewerUserId)
                    .ifPresent(p -> full.setViewerScore(p.getScore()));
        }
    }

    private EventTeaserResponse publicTeaser(Event e, String message, boolean viewerJoined, Integer viewerScore) {
        Long id = e.getId();
        return EventTeaserResponse.builder()
                .id(id)
                .title(e.getTitle())
                .startDate(e.getStartDate())
                .deadline(e.getDeadline())
                .message(message)
                .viewerHasJoined(viewerJoined)
                .viewerScore(viewerScore)
                .maxVip(e.getMaxVip())
                .maxGold(e.getMaxGold())
                .maxSilver(e.getMaxSilver())
                .vipPrice(e.getVipPrice())
                .goldPrice(e.getGoldPrice())
                .silverPrice(e.getSilverPrice())
                .currentVip((int) eventPartnerRepository.countByEventIdAndTier(id, EventTier.VIP))
                .currentGold((int) eventPartnerRepository.countByEventIdAndTier(id, EventTier.GOLD))
                .currentSilver((int) eventPartnerRepository.countByEventIdAndTier(id, EventTier.SILVER))
                .build();
    }

    private EventFullResponse toFull(Event e) {
        Long id = e.getId();
        return EventFullResponse.builder()
                .id(id)
                .title(e.getTitle())
                .description(e.getDescription())
                .requirements(e.getRequirements())
                .successMetrics(e.getSuccessMetrics())
                .startDate(e.getStartDate())
                .deadline(e.getDeadline())
                .maxVip(e.getMaxVip())
                .maxGold(e.getMaxGold())
                .maxSilver(e.getMaxSilver())
                .vipPrice(e.getVipPrice())
                .goldPrice(e.getGoldPrice())
                .silverPrice(e.getSilverPrice())
                .currentVip((int) eventPartnerRepository.countByEventIdAndTier(id, EventTier.VIP))
                .currentGold((int) eventPartnerRepository.countByEventIdAndTier(id, EventTier.GOLD))
                .currentSilver((int) eventPartnerRepository.countByEventIdAndTier(id, EventTier.SILVER))
                .participantCount(eventParticipantRepository.countByEventId(id))
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
