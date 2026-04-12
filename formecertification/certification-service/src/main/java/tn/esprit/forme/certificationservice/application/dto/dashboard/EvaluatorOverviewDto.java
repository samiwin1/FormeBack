package tn.esprit.forme.certificationservice.application.dto.dashboard;

import java.time.LocalDateTime;
import java.util.List;

public record EvaluatorOverviewDto(
        int sessionsTodayCount,
        int learnersToEvaluateCount,
        List<SessionTodayItemDto> sessionsToday
) {
    public record SessionTodayItemDto(
            Long id,
            String title,
            LocalDateTime scheduledAt,
            int learnerCount
    ) {
    }
}
