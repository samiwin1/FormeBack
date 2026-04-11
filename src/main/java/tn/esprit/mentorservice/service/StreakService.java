package tn.esprit.mentorservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.mentorservice.dto.StreakResponseDto;
import tn.esprit.mentorservice.entity.LearnerStreak;
import tn.esprit.mentorservice.repository.LearnerStreakRepository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StreakService {

    private final LearnerStreakRepository streakRepo;

    /**
     * Called after every mentor session is persisted.
     * Updates streak for the given userId.
     */
    @Transactional
    public void recordActivity(long userId) {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());

        LearnerStreak streak = streakRepo.findByUserId(userId)
                .orElseGet(() -> LearnerStreak.builder()
                        .userId(userId)
                        .currentStreak(0)
                        .bestStreak(0)
                        .build());

        LocalDate last = streak.getLastActiveDate();

        if (last != null && last.equals(today)) {
            // Already active today — nothing to update
            return;
        }

        if (last == null || last.isBefore(today.minusDays(1))) {
            // Gap of more than one day — reset streak
            streak.setCurrentStreak(1);
        } else {
            // Consecutive day — increment
            streak.setCurrentStreak(streak.getCurrentStreak() + 1);
        }

        if (streak.getCurrentStreak() > streak.getBestStreak()) {
            streak.setBestStreak(streak.getCurrentStreak());
        }

        streak.setLastActiveDate(today);
        streakRepo.save(streak);
        log.debug("Streak updated for userId={} → current={}", userId, streak.getCurrentStreak());
    }

    @Transactional(readOnly = true)
    public StreakResponseDto getStreak(long userId) {
        return streakRepo.findByUserId(userId)
                .map(s -> new StreakResponseDto(
                        s.getCurrentStreak(),
                        s.getBestStreak(),
                        s.getLastActiveDate(),
                        LocalDate.now(ZoneId.systemDefault()).equals(s.getLastActiveDate())))
                .orElse(new StreakResponseDto(0, 0, null, false));
    }

    /**
     * Returns all streak records where the user was NOT active today.
     * Used by the reminder scheduler.
     */
    @Transactional(readOnly = true)
    public List<LearnerStreak> findUsersAtRisk(LocalDate today) {
        return streakRepo.findAll().stream()
                .filter(s -> s.getLastActiveDate() != null
                        && !s.getLastActiveDate().equals(today)
                        && s.getCurrentStreak() > 0)
                .toList();
    }
}
