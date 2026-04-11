package tn.esprit.mentorservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.mentorservice.domain.MentorNotificationType;
import tn.esprit.mentorservice.dto.MentorNotificationDto;
import tn.esprit.mentorservice.entity.MentorNotification;
import tn.esprit.mentorservice.repository.MentorNotificationRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MentorNotificationService {

    private final MentorNotificationRepository repo;

    // ── Create ──────────────────────────────────────────────────────────────

    /** Broadcast to all admins: a new formation demand has been recorded. */
    public void notifyAdminsNewDemand(Long demandId, String role) {
        repo.save(MentorNotification.builder()
                .type(MentorNotificationType.NEW_FORMATION_DEMAND)
                .title("New formation demand")
                .message("A learner is requesting a formation on: \"" + role + "\". Review and create it when ready.")
                .referenceId(demandId)
                .build());
    }

    /** Notify the specific learner that their requested formation is now available. */
    public void notifyUserFormationFulfilled(Long userId, Long demandId, String role) {
        repo.save(MentorNotification.builder()
                .targetUserId(userId)
                .type(MentorNotificationType.FORMATION_FULFILLED)
                .title("Formation now available!")
                .message("Great news! A formation on \"" + role + "\" that you requested has been created. Check it out and apply!")
                .referenceId(demandId)
                .build());
    }

    // ── Admin read ──────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<MentorNotificationDto> getAdminNotifications() {
        return repo.findByTargetUserIdIsNullOrderByCreatedAtDesc()
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public long countAdminUnread() {
        return repo.countByTargetUserIdIsNullAndReadFalse();
    }

    public void markAllAdminRead() {
        repo.markAllAdminRead();
    }

    public void markAdminOneRead(Long id) {
        repo.findById(id).filter(n -> n.getTargetUserId() == null)
                .ifPresent(n -> { n.setRead(true); repo.save(n); });
    }

    // ── User read ───────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<MentorNotificationDto> getUserNotifications(Long userId) {
        return repo.findByTargetUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public long countUserUnread(Long userId) {
        return repo.countByTargetUserIdAndReadFalse(userId);
    }

    public void markAllUserRead(Long userId) {
        repo.markAllUserRead(userId);
    }

    public void markUserOneRead(Long notificationId, Long userId) {
        repo.findById(notificationId)
                .filter(n -> userId.equals(n.getTargetUserId()))
                .ifPresent(n -> { n.setRead(true); repo.save(n); });
    }

    // ── Mapping ─────────────────────────────────────────────────────────────

    private MentorNotificationDto toDto(MentorNotification n) {
        return new MentorNotificationDto(
                n.getId(),
                n.getType().name(),
                n.getTitle(),
                n.getMessage(),
                n.getReferenceId(),
                n.isRead(),
                n.getCreatedAt()
        );
    }
}
