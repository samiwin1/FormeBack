package tn.esprit.forme.certificationservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.forme.certificationservice.domain.entity.Notification;
import tn.esprit.forme.certificationservice.domain.repository.NotificationRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j

public class NotificationService {

    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final NotificationRepository notificationRepository;

    @Transactional
    public void notifyCertificateIssued(Long userId, String certTitle, double finalScore, Long certId) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type("CERTIFICATE_ISSUED")
                .title("Your certificate is ready")
                .message("Congratulations! You obtained \"" + certTitle + "\" with final score " + String.format("%.1f", finalScore) + "/100.")
                .referenceId(certId)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
        log.info("Saved CERTIFICATE_ISSUED notification for userId={}", userId);
    }

    @Transactional
    public void notifyFeedbackRequested(Long userId, String certTitle, Long sessionId, Long issuedCertId) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type("FEEDBACK_REQUESTED")
                .title("⭐ How was your oral exam?")
                .message("You received your \"" + certTitle + "\" certificate. Please rate your experience.")
                .referenceId(issuedCertId)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
        log.info("Saved FEEDBACK_REQUESTED notification for userId={}", userId);
    }

    @Transactional
    public void notifySessionAssigned(Long userId, String sessionTitle, LocalDateTime scheduledAt, String meetingLink, Long sessionId) {
        String scheduleText = scheduledAt == null ? "-" : DATETIME_FMT.format(scheduledAt);
        Notification notification = Notification.builder()
                .userId(userId)
                .type("SESSION_ASSIGNED")
                .title("Oral session scheduled")
                .message("You are assigned to \"" + sessionTitle + "\" on " + scheduleText + ". Link: " + (meetingLink == null ? "-" : meetingLink))
                .referenceId(sessionId)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
        log.info("Saved SESSION_ASSIGNED notification for userId={}", userId);
    }

    @Transactional(readOnly = true)
    public List<Notification> getMyNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public void markAllRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    @Transactional
    public void markOneRead(Long notificationId, Long userId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            if (notification.getUserId().equals(userId)) {
                notification.setRead(true);
                notificationRepository.save(notification);
            }
        });
    }
}
