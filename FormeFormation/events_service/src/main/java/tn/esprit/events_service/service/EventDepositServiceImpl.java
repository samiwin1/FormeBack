package tn.esprit.events_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.events_service.dto.DepositResponse;
import tn.esprit.events_service.dto.ViewerRole;
import tn.esprit.events_service.entity.Event;
import tn.esprit.events_service.entity.EventDeposit;
import tn.esprit.events_service.entity.EventParticipant;
import tn.esprit.events_service.exception.BusinessException;
import tn.esprit.events_service.exception.ForbiddenException;
import tn.esprit.events_service.exception.ResourceNotFoundException;
import tn.esprit.events_service.repository.EventDepositRepository;
import tn.esprit.events_service.repository.EventParticipantRepository;
import tn.esprit.events_service.repository.EventPartnerRepository;
import tn.esprit.events_service.repository.EventRepository;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventDepositServiceImpl implements EventDepositService {

    public static final String README_DETAIL_HINT =
            "Be very detailed in your README so evaluators can fully understand your work.";

    private final EventRepository eventRepository;
    private final EventParticipantRepository eventParticipantRepository;
    private final EventPartnerRepository eventPartnerRepository;
    private final EventDepositRepository eventDepositRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<DepositResponse> getMyDeposit(Long eventId, Long userId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event not found: " + eventId);
        }
        return eventParticipantRepository.findByEventIdAndUserId(eventId, userId)
                .flatMap(p -> eventDepositRepository.findByParticipantId(p.getId()))
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public DepositResponse submitDeposit(Long eventId, Long userId, MultipartFile zipFile, MultipartFile readmeFile) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));

        Instant now = Instant.now();
        if (now.isBefore(event.getStartDate())) {
            throw new BusinessException("Submissions open when the event starts.");
        }
        if (now.isAfter(event.getDeadline())) {
            throw new BusinessException("The submission deadline has passed.");
        }

        if (eventPartnerRepository.existsByEventIdAndPartnerId(eventId, userId)) {
            throw new BusinessException("Sponsors cannot submit participant work for this event.");
        }

        EventParticipant participant = eventParticipantRepository.findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new BusinessException("Join the event before submitting your work."));

        validateZip(zipFile);
        validateReadme(readmeFile);

        byte[] zipBytes;
        byte[] readmeBytes;
        try {
            zipBytes = zipFile.getBytes();
            readmeBytes = readmeFile.getBytes();
        } catch (Exception e) {
            throw new BusinessException("Could not read uploaded files: " + e.getMessage());
        }

        if (zipBytes.length == 0) {
            throw new BusinessException("ZIP archive must not be empty.");
        }

        String readmeText = new String(readmeBytes, StandardCharsets.UTF_8).trim();
        if (readmeText.isEmpty()) {
            throw new BusinessException("README file must contain text.");
        }

        if (eventDepositRepository.findByParticipantId(participant.getId()).isPresent()) {
            throw new BusinessException("You have already submitted for this event. Contact an admin if you need changes.");
        }

        EventDeposit deposit = EventDeposit.builder()
                .participant(participant)
                .build();

        deposit.setZipData(zipBytes);
        deposit.setZipOriginalFilename(safeName(zipFile.getOriginalFilename(), "submission.zip"));
        deposit.setReadmeContent(readmeText);
        deposit.setReadmeOriginalFilename(safeName(readmeFile.getOriginalFilename(), "README.md"));

        EventDeposit saved = eventDepositRepository.save(deposit);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void scoreDeposit(Long eventId, Long depositId, int score, ViewerRole role) {
        assertAdmin(role);
        if (score < 0 || score > 100) {
            throw new BusinessException("Score must be between 0 and 100.");
        }

        EventDeposit deposit = eventDepositRepository.findByIdAndEventId(depositId, eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Deposit not found for this event."));

        EventParticipant p = deposit.getParticipant();
        p.setScore(score);
        eventParticipantRepository.save(p);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getZipForDownload(Long eventId, Long depositId, ViewerRole role, Long userId) {
        if (role != ViewerRole.ADMIN) {
            if (userId == null || !eventPartnerRepository.existsByEventIdAndPartnerId(eventId, userId)) {
                throw new ForbiddenException("Only admins or event sponsors can download submissions.");
            }
        }
        EventDeposit deposit = eventDepositRepository.findByIdAndEventId(depositId, eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Deposit not found for this event."));
        return deposit.getZipData();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepositResponse> listDepositsForAdmin(Long eventId, ViewerRole role) {
        assertAdmin(role);
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event not found: " + eventId);
        }

        return eventParticipantRepository.findByEventId(eventId).stream()
                .map(EventParticipant::getId)
                .flatMap(pid -> eventDepositRepository.findByParticipantId(pid).stream())
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private void assertAdmin(ViewerRole role) {
        if (role != ViewerRole.ADMIN) {
            throw new ForbiddenException("Admin only.");
        }
    }

    private static void validateZip(MultipartFile zip) {
        if (zip == null || zip.isEmpty()) {
            throw new BusinessException("Both a ZIP archive and a README file are required. " + README_DETAIL_HINT);
        }
        String name = zip.getOriginalFilename();
        if (name == null || !name.toLowerCase(Locale.ROOT).endsWith(".zip")) {
            throw new BusinessException("You must upload a .zip file for your project. " + README_DETAIL_HINT);
        }
    }

    private static void validateReadme(MultipartFile readme) {
        if (readme == null || readme.isEmpty()) {
            throw new BusinessException("Both a ZIP archive and a README file are required. " + README_DETAIL_HINT);
        }
        String name = readme.getOriginalFilename();
        if (!isReadmeFileName(name)) {
            throw new BusinessException(
                    "README must be named README.md or README.txt (case-insensitive). " + README_DETAIL_HINT
            );
        }
    }

    private static boolean isReadmeFileName(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return false;
        }
        String n = originalFilename.toLowerCase(Locale.ROOT).trim();
        return n.equals("readme.md") || n.equals("readme.txt");
    }

    private static String safeName(String original, String fallback) {
        if (original == null || original.isBlank()) {
            return fallback;
        }
        return original;
    }

    private DepositResponse toResponse(EventDeposit d) {
        return DepositResponse.builder()
                .depositId(d.getId())
                .participantId(d.getParticipant().getId())
                .userId(d.getParticipant().getUserId())
                .zipOriginalFilename(d.getZipOriginalFilename())
                .readmeOriginalFilename(d.getReadmeOriginalFilename())
                .submittedAt(d.getSubmittedAt())
                .readmeHint(README_DETAIL_HINT)
                .build();
    }
}
