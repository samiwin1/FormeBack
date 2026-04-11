package tn.esprit.mentorservice.dto;

import java.time.LocalDate;

public record StreakResponseDto(
        int currentStreak,
        int bestStreak,
        LocalDate lastActiveDate,
        boolean activeToday
) {
}
