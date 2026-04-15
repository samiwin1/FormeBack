package tn.esprit.forme.certificationservice.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.forme.certificationservice.application.service.NotificationService;
import tn.esprit.forme.certificationservice.domain.entity.Notification;
import tn.esprit.forme.certificationservice.security.SecurityUtils;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/me/notifications")
@RequiredArgsConstructor

public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN','SUPER_ADMIN')")
    public List<Notification> myNotifications() {
        try {
            return notificationService.getMyNotifications(SecurityUtils.currentUserId());
        } catch (Exception e) {
            return List.of();
        }
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyRole('USER','ADMIN','SUPER_ADMIN')")
    public Map<String, Long> unreadCount() {
        try {
            long count = notificationService.countUnread(SecurityUtils.currentUserId());
            return Map.of("count", count);
        } catch (Exception e) {
            return Map.of("count", 0L);
        }
    }

    @PatchMapping("/mark-all-read")
    @PreAuthorize("hasAnyRole('USER','ADMIN','SUPER_ADMIN')")
    public void markAllRead() {
        notificationService.markAllRead(SecurityUtils.currentUserId());
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('USER','ADMIN','SUPER_ADMIN')")
    public void markOneRead(@PathVariable Long id) {
        notificationService.markOneRead(id, SecurityUtils.currentUserId());
    }
}
