package tn.esprit.forme.certificationservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.forme.certificationservice.domain.entity.OralExamAssignment;
import tn.esprit.forme.certificationservice.domain.entity.OralSession;
import tn.esprit.forme.certificationservice.domain.entity.ReminderLog;
import tn.esprit.forme.certificationservice.domain.enums.AssignmentStatus;
import tn.esprit.forme.certificationservice.domain.repository.OralExamAssignmentRepository;
import tn.esprit.forme.certificationservice.domain.repository.OralSessionRepository;
import tn.esprit.forme.certificationservice.domain.repository.ReminderLogRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j

public class SmartReminderService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final OralSessionRepository oralSessionRepository;
    private final OralExamAssignmentRepository assignmentRepository;
    private final ReminderLogRepository reminderLogRepository;
    private final EmailNotificationService emailNotificationService;

    /**
     * Runs every hour to send adaptive reminders (J-7, J-3, J-1, H-2).
     */
    @Scheduled(cron = "${app.scheduler.reminders-cron:0 0 * * * *}")
    @Transactional
    public void dispatchSmartReminders() {
        LocalDateTime now = LocalDateTime.now();

        // consider sessions from next 7 days only
        LocalDateTime windowEnd = now.plusDays(7);
        List<OralSession> upcoming = oralSessionRepository.findAll().stream()
                .filter(s -> s.getScheduledAt() != null)
                .filter(s -> !s.getScheduledAt().isBefore(now))
                .filter(s -> !s.getScheduledAt().isAfter(windowEnd))
                .toList();

        for (OralSession session : upcoming) {
            List<OralExamAssignment> learners = assignmentRepository.findByOralSessionId(session.getId()).stream()
                    .filter(a -> a.getStatus() != AssignmentStatus.CANCELLED && a.getStatus() != AssignmentStatus.NO_SHOW)
                    .toList();

            for (OralExamAssignment assignment : learners) {
                String stage = computeStage(now, session.getScheduledAt());
                if (stage == null) {
                    continue;
                }
                if (reminderLogRepository.existsBySessionIdAndLearnerIdAndStage(
                        session.getId(), assignment.getLearnerId(), stage)) {
                    continue; // already sent this stage
                }

                sendReminder(stage, assignment, session);

                reminderLogRepository.save(ReminderLog.builder()
                        .sessionId(session.getId())
                        .learnerId(assignment.getLearnerId())
                        .stage(stage)
                        .sentAt(now)
                        .build());
            }
        }
    }

    private String computeStage(LocalDateTime now, LocalDateTime scheduledAt) {
        Duration diff = Duration.between(now, scheduledAt);
        long hours = diff.toHours();
        long days = diff.toDays();

        if (days == 7) return "J-7";
        if (days == 3) return "J-3";
        if (days == 1) return "J-1";
        if (hours == 2) return "H-2";
        return null;
    }

    private void sendReminder(String stage, OralExamAssignment assignment, OralSession session) {
        String when = session.getScheduledAt() == null ? "-" : DATE_FMT.format(session.getScheduledAt());
        String base =
                """
                Hello,

                This is a reminder for your upcoming oral exam session.

                Certification: %s
                Session: %s
                Date: %s
                Meet link: %s

                """.formatted(
                        session.getCertification().getTitle(),
                        session.getTitle(),
                        when,
                        session.getMeetingLink() == null ? "-" : session.getMeetingLink()
                );

        String extra;
        switch (stage) {
            case "J-7" -> extra = "You are one week away. Please review the written materials to maximize your chances.";
            case "J-3" -> extra = "Your session is in three days. Make sure your microphone and camera are working.";
            case "J-1" -> extra = "Your session is tomorrow. If you have a conflict, request a reschedule now to avoid a NO_SHOW.";
            case "H-2" -> extra = "Your session starts in two hours. Please be ready a few minutes early.";
            default -> extra = "";
        }

        String body = base + extra + "\n\nForMe Platform";

        try {
            emailNotificationService.sendSessionAssigned(
                    assignment.getLearnerId(),
                    session.getTitle(),
                    session.getScheduledAt(),
                    session.getMeetingLink()
            );
            log.info("Smart reminder [{}] sent for learnerId={} sessionId={}", stage, assignment.getLearnerId(), session.getId());
        } catch (Exception ex) {
            log.error("Failed to send smart reminder [{}] for learnerId={} sessionId={}: {}",
                    stage, assignment.getLearnerId(), session.getId(), ex.getMessage());
        }
    }
}
