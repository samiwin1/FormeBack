package tn.esprit.forme.certificationservice.application.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import tn.esprit.forme.certificationservice.domain.entity.CertificationCatalog;
import tn.esprit.forme.certificationservice.domain.entity.OralExamAssignment;
import tn.esprit.forme.certificationservice.domain.entity.OralSession;
import tn.esprit.forme.certificationservice.domain.entity.ReminderLog;
import tn.esprit.forme.certificationservice.domain.enums.AssignmentStatus;
import tn.esprit.forme.certificationservice.domain.enums.OralSessionStatus;
import tn.esprit.forme.certificationservice.domain.repository.CertificationCatalogRepository;
import tn.esprit.forme.certificationservice.domain.repository.OralExamAssignmentRepository;
import tn.esprit.forme.certificationservice.domain.repository.OralSessionRepository;
import tn.esprit.forme.certificationservice.domain.repository.ReminderLogRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j

public class SessionReminderService {

    private final OralSessionRepository oralSessionRepository;
    private final OralExamAssignmentRepository oralAssignmentRepository;
    private final ReminderLogRepository reminderLogRepository;
    private final UserDirectoryAggregationService userDirectoryAggregationService;
    private final CertificationCatalogRepository certificationCatalogRepository;
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.notification.email-enabled:false}")
    private boolean emailEnabled;

    @Scheduled(cron = "0 0 8 * * *")
    public void sendSessionReminders() {
        if (!emailEnabled) {
            log.info("Email disabled — skipping session reminders");
            return;
        }

        LocalDateTime windowStart = LocalDateTime.now().plusHours(23);
        LocalDateTime windowEnd = LocalDateTime.now().plusHours(25);

        log.info("Checking sessions scheduled between {} and {}", windowStart, windowEnd);

        List<OralSession> upcomingSessions = oralSessionRepository.findByStatusAndScheduledAtBetween(
                OralSessionStatus.PLANNED, windowStart, windowEnd);

        log.info("Found {} upcoming sessions for reminder", upcomingSessions.size());

        for (OralSession session : upcomingSessions) {
            processSessionReminders(session);
        }
    }

    private void processSessionReminders(OralSession session) {
        List<AssignmentStatus> activeStatuses = List.of(
                AssignmentStatus.ASSIGNED,
                AssignmentStatus.RESCHEDULED
        );

        List<OralExamAssignment> assignments = oralAssignmentRepository.findByOralSession_IdAndStatusIn(
                session.getId(), activeStatuses);

        for (OralExamAssignment assignment : assignments) {
            try {
                sendReminderIfNotAlreadySent(assignment, session);
            } catch (Exception e) {
                log.error("Failed to send reminder for assignmentId={}: {}",
                        assignment.getId(), e.getMessage());
                // continue to next assignment — don't stop all reminders
            }
        }
    }

    private void sendReminderIfNotAlreadySent(OralExamAssignment assignment, OralSession session) {
        boolean alreadySent = reminderLogRepository.existsByAssignmentIdAndReminderType(
                assignment.getId(), "SESSION_24H");

        if (alreadySent) {
            log.debug("Reminder already sent for assignmentId={}", assignment.getId());
            return;
        }

        String learnerEmail = userDirectoryAggregationService.resolveUserEmail(assignment.getLearnerId());
        if (learnerEmail == null || learnerEmail.isBlank()) {
            log.warn("No email found for learnerId={}", assignment.getLearnerId());
            return;
        }

        String learnerName = userDirectoryAggregationService.resolveUserName(assignment.getLearnerId());
        String certificationTitle = certificationCatalogRepository
                .findById(session.getCertification().getId())
                .map(CertificationCatalog::getTitle)
                .orElse("Certification #" + session.getCertification().getId());

        sendReminderEmail(learnerEmail, learnerName, session, certificationTitle);

        ReminderLog reminderLog = ReminderLog.builder()
                .assignmentId(assignment.getId())
                .reminderType("SESSION_24H")
                .sentAt(LocalDateTime.now())
                .build();
        reminderLogRepository.save(reminderLog);

        log.info("Reminder sent to {} for session '{}'", learnerEmail, session.getTitle());
    }

    private void sendReminderEmail(String toEmail, String learnerName,
                                    OralSession session, String certificationTitle) {
        try {
            Context ctx = new Context();
            ctx.setVariable("learnerName", learnerName);
            ctx.setVariable("sessionTitle", session.getTitle());
            ctx.setVariable("certificationTitle", certificationTitle);
            ctx.setVariable("scheduledAt", session.getScheduledAt());
            ctx.setVariable("meetingLink", session.getMeetingLink());
            ctx.setVariable("meetingProvider", session.getMeetingProvider().name());

            String htmlContent = templateEngine.process("session-reminder", ctx);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("⏰ Reminder: Your oral exam is tomorrow — " + session.getTitle());
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
    }
}
