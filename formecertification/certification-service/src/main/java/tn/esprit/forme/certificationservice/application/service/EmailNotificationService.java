package tn.esprit.forme.certificationservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import tn.esprit.forme.certificationservice.infrastructure.userdb.UserDbLookupService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j

public class EmailNotificationService {

    private final JavaMailSender mailSender;
    private final UserDbLookupService userDbLookupService;

    @Value("${app.notification.email-enabled:false}")
    private boolean emailEnabled;

    public void sendSessionAssigned(Long learnerId, String sessionTitle, LocalDateTime scheduledAt, String meetingLink) {
        UserDbLookupService.UserRow user = userDbLookupService.findUsersByIds(java.util.List.of(learnerId)).get(learnerId);
        if (user == null || user.email() == null || user.email().isBlank()) {
            log.warn("Skipping assignment email: no email found for learnerId={}", learnerId);
            return;
        }

        String learnerName = buildDisplayName(user.firstName(), user.lastName(), learnerId);
        sendSafely(
                user.email(),
                "Your oral exam session has been scheduled - ForMe",
                """
                Hello %s,

                Your oral exam session has been scheduled.

                Session: %s
                Date: %s
                Meet Link: %s

                Good luck!
                ForMe Platform
                """.formatted(learnerName, sessionTitle, scheduledAt, meetingLink)
        );
    }

    public void sendCertificateIssued(Long learnerId, String certificationTitle, double finalScore) {
        UserDbLookupService.UserRow user = userDbLookupService.findUsersByIds(java.util.List.of(learnerId)).get(learnerId);
        if (user == null || user.email() == null || user.email().isBlank()) {
            log.warn("Skipping certificate email: no email found for learnerId={}", learnerId);
            return;
        }

        String learnerName = buildDisplayName(user.firstName(), user.lastName(), learnerId);
        sendSafely(
                user.email(),
                "Congratulations! Your certificate is ready - ForMe",
                """
                Hello %s,

                Congratulations! You have successfully obtained your certificate.

                Certificate: %s
                Final Score: %.1f

                Log in to ForMe to download your certificate PDF.

                ForMe Platform
                """.formatted(learnerName, certificationTitle, finalScore)
        );
    }

    private void sendSafely(String to, String subject, String text) {
        if (!emailEnabled) {
            log.warn("Email disabled - skipping notification to {}", to);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("Email sent to {}", to);
        } catch (Exception ex) {
            log.error("Failed to send email to {}: {}", to, ex.getMessage());
        }
    }

    private String buildDisplayName(String firstName, String lastName, Long learnerId) {
        String displayName = ((firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName)).trim();
        return displayName.isBlank() ? "Learner #" + learnerId : displayName;
    }
}
