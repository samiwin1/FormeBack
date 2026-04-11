package tn.esprit.mentorservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tn.esprit.mentorservice.client.UserProfileClient;
import tn.esprit.mentorservice.domain.ReminderStyle;
import tn.esprit.mentorservice.domain.StreakSensitivity;
import tn.esprit.mentorservice.dto.MentorExtendedPreferences;
import tn.esprit.mentorservice.entity.LearnerPortfolio;
import tn.esprit.mentorservice.entity.LearnerStreak;
import tn.esprit.mentorservice.entity.ReminderLog;
import tn.esprit.mentorservice.repository.LearnerPortfolioRepository;
import tn.esprit.mentorservice.repository.ReminderLogRepository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "mentor.reminder.enabled", havingValue = "true", matchIfMissing = true)
public class ReminderSchedulerService {

    private final StreakService streakService;
    private final LearnerPortfolioRepository portfolioRepo;
    private final ReminderLogRepository reminderLogRepo;
    private final UserProfileClient userProfileClient;
    private final EmailService emailService;

    @Scheduled(cron = "${mentor.reminder.cron:0 0 8 * * *}")
    public void sendDailyReminders() {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        log.info("Running daily reminder job for {}", today);

        List<LearnerStreak> atRisk = streakService.findUsersAtRisk(today);
        int sent = 0;

        for (LearnerStreak s : atRisk) {
            try {
                if (processUser(s, today)) sent++;
            } catch (Exception e) {
                log.warn("Reminder failed for userId={}: {}", s.getUserId(), e.getMessage());
            }
        }
        log.info("Reminder job complete — processed {} users, sent {} emails.", atRisk.size(), sent);
    }

    private boolean processUser(LearnerStreak s, LocalDate today) {
        long userId = s.getUserId();

        LearnerPortfolio portfolio = portfolioRepo.findByUserId(userId).orElse(null);
        if (portfolio == null) return false;

        MentorExtendedPreferences prefs = portfolio.getExtendedPreferences();
        ReminderStyle style = (prefs != null && prefs.getReminderStyle() != null)
                ? prefs.getReminderStyle() : ReminderStyle.NONE;

        if (style == ReminderStyle.NONE) return false;

        StreakSensitivity sensitivity = (prefs != null && prefs.getStreakSensitivity() != null)
                ? prefs.getStreakSensitivity() : StreakSensitivity.MEDIUM;

        // LIGHT mode: only send if streak meets sensitivity threshold
        int threshold = switch (sensitivity) {
            case LOW    -> 1;
            case MEDIUM -> 3;
            case HIGH   -> 7;
        };
        if (style == ReminderStyle.LIGHT && s.getCurrentStreak() < threshold) return false;

        // Determine type: BROKEN if missed more than 1 day, AT_RISK if missed just today
        boolean streakBroken = s.getLastActiveDate().isBefore(today.minusDays(1));
        String type = streakBroken ? "STREAK_BROKEN" : "STREAK_AT_RISK";

        // Deduplicate — one email per user per type per day
        if (reminderLogRepo.existsByUserIdAndSentDateAndReminderType(userId, today, type)) return false;

        // Resolve email + first name from user-service
        Map<String, String> userInfo = userProfileClient.fetchUserInfo(userId).orElse(null);
        if (userInfo == null) return false;

        String email     = userInfo.get("email");
        String firstName = userInfo.getOrDefault("firstName", email);

        emailService.sendStreakReminderEmail(email, firstName, s.getCurrentStreak(), type);

        ReminderLog logEntry = ReminderLog.builder()
                .userId(userId)
                .sentDate(today)
                .reminderType(type)
                .build();
        reminderLogRepo.save(logEntry);

        log.debug("Sent {} to userId={}", type, userId);
        return true;
    }
}
