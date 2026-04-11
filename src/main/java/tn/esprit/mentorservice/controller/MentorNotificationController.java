package tn.esprit.mentorservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tn.esprit.mentorservice.dto.MentorNotificationDto;
import tn.esprit.mentorservice.security.MentorPrincipal;
import tn.esprit.mentorservice.service.MentorNotificationService;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class MentorNotificationController {

    private final MentorNotificationService notificationService;

    // ── Admin endpoints ─────────────────────────────────────────────────────

    @GetMapping("/mentor/admin/notifications")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public List<MentorNotificationDto> getAdminNotifications() {
        return notificationService.getAdminNotifications();
    }

    @GetMapping("/mentor/admin/notifications/unread-count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Map<String, Long> adminUnreadCount() {
        return Map.of("count", notificationService.countAdminUnread());
    }

    @PatchMapping("/mentor/admin/notifications/mark-all-read")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> markAllAdminRead() {
        notificationService.markAllAdminRead();
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/mentor/admin/notifications/{id}/read")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> markAdminOneRead(@PathVariable Long id) {
        notificationService.markAdminOneRead(id);
        return ResponseEntity.noContent().build();
    }

    // ── Learner endpoints ───────────────────────────────────────────────────

    @GetMapping("/mentor/me/notifications")
    public List<MentorNotificationDto> getUserNotifications(@AuthenticationPrincipal MentorPrincipal principal) {
        return notificationService.getUserNotifications(principal.getUserId());
    }

    @GetMapping("/mentor/me/notifications/unread-count")
    public Map<String, Long> userUnreadCount(@AuthenticationPrincipal MentorPrincipal principal) {
        return Map.of("count", notificationService.countUserUnread(principal.getUserId()));
    }

    @PatchMapping("/mentor/me/notifications/mark-all-read")
    public ResponseEntity<Void> markAllUserRead(@AuthenticationPrincipal MentorPrincipal principal) {
        notificationService.markAllUserRead(principal.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/mentor/me/notifications/{id}/read")
    public ResponseEntity<Void> markUserOneRead(@PathVariable Long id,
                                                 @AuthenticationPrincipal MentorPrincipal principal) {
        notificationService.markUserOneRead(id, principal.getUserId());
        return ResponseEntity.noContent().build();
    }
}
