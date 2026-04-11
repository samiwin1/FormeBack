package tn.esprit.mentorservice.dto;

import java.util.List;

public class AdminStatsDto {
    public record DailyUsageDto(String day, long count) {}
    public record MonthlyUsageDto(String month, long count) {}
    public record UsageStatsDto(List<DailyUsageDto> daily, List<MonthlyUsageDto> monthly) {}
    public record FeedbackStatsDto(long up, long down) {}
    public record DemandStatDto(String role, long count) {}
    public record TopStreakDto(long userId, String userName, int streak) {}
}
