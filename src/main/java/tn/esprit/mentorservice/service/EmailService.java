package tn.esprit.mentorservice.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${mentor.reminder.from:noreply@forme.app}")
    private String fromAddress;

    @SuppressWarnings("null")
    public void sendStreakReminderEmail(String toEmail, String firstName, int streak, String reminderType) {
        if (toEmail == null || toEmail.isBlank()) return;
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            String from = fromAddress != null ? fromAddress : "noreply@forme.app";
            helper.setFrom(from);
            helper.setTo(toEmail);

            if ("STREAK_AT_RISK".equals(reminderType)) {
                helper.setSubject("🔥 Keep your " + streak + "-day streak alive!");
                helper.setText(buildAtRiskHtml(firstName != null ? firstName : toEmail, streak), true);
            } else {
                helper.setSubject("Your streak was reset — start fresh today!");
                helper.setText(buildBrokenHtml(firstName != null ? firstName : toEmail), true);
            }

            mailSender.send(msg);
            log.debug("Reminder email sent to {}", toEmail);
        } catch (Exception e) {
            log.warn("Failed to send reminder email to {}: {}", toEmail, e.getMessage());
        }
    }

    private String buildAtRiskHtml(String firstName, int streak) {
        return """
                <div style="font-family:sans-serif;max-width:480px;margin:auto;padding:24px">
                  <h2 style="color:#f59e0b">🔥 Don't break your streak, %s!</h2>
                  <p style="color:#374151">You have a <strong>%d-day learning streak</strong>.
                  Come back today to keep it going.</p>
                  <a href="http://localhost:4200/me/mentor"
                     style="display:inline-block;background:#6366f1;color:#fff;padding:12px 24px;
                            border-radius:8px;text-decoration:none;font-weight:600;margin-top:8px">
                    Open AI Mentor
                  </a>
                  <p style="color:#9ca3af;font-size:12px;margin-top:24px">
                    You are receiving this because your reminder style is set to active.
                    Update it anytime in your learning profile.
                  </p>
                </div>""".formatted(firstName, streak);
    }

    private String buildBrokenHtml(String firstName) {
        return """
                <div style="font-family:sans-serif;max-width:480px;margin:auto;padding:24px">
                  <h2 style="color:#6366f1">Start a new streak today, %s!</h2>
                  <p style="color:#374151">Your previous streak ended.
                  Every expert was once a beginner — let's start fresh.</p>
                  <a href="http://localhost:4200/me/mentor"
                     style="display:inline-block;background:#6366f1;color:#fff;padding:12px 24px;
                            border-radius:8px;text-decoration:none;font-weight:600;margin-top:8px">
                    Open AI Mentor
                  </a>
                  <p style="color:#9ca3af;font-size:12px;margin-top:24px">
                    You are receiving this because your reminder style is set to active.
                    Update it anytime in your learning profile.
                  </p>
                </div>""".formatted(firstName);
    }
}
