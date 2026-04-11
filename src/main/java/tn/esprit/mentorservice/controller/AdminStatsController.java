package tn.esprit.mentorservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.mentorservice.client.UserProfileClient;
import tn.esprit.mentorservice.dto.AdminStatsDto;
import tn.esprit.mentorservice.repository.FormationDemandRepository;
import tn.esprit.mentorservice.repository.LearnerStreakRepository;
import tn.esprit.mentorservice.repository.MentorFeedbackRepository;
import tn.esprit.mentorservice.repository.MentorSessionRepository;

import java.util.List;

@RestController
@RequestMapping("/mentor/admin/stats")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminStatsController {

    private final MentorSessionRepository sessionRepo;
    private final MentorFeedbackRepository feedbackRepo;
    private final FormationDemandRepository demandRepo;
    private final LearnerStreakRepository streakRepo;
    private final UserProfileClient userProfileClient;

    @GetMapping("/usage")
    public AdminStatsDto.UsageStatsDto getUsage() {
        List<AdminStatsDto.DailyUsageDto> daily = sessionRepo.dailyUsage().stream()
                .map(r -> new AdminStatsDto.DailyUsageDto(r[0].toString(), ((Number) r[1]).longValue()))
                .toList();
        List<AdminStatsDto.MonthlyUsageDto> monthly = sessionRepo.monthlyUsage().stream()
                .map(r -> new AdminStatsDto.MonthlyUsageDto(r[0].toString(), ((Number) r[1]).longValue()))
                .toList();
        return new AdminStatsDto.UsageStatsDto(daily, monthly);
    }

    @GetMapping("/formation-demand")
    public List<AdminStatsDto.DemandStatDto> getFormationDemand() {
        return demandRepo.countByRole().stream()
                .limit(10)
                .map(r -> new AdminStatsDto.DemandStatDto(r[0].toString(), ((Number) r[1]).longValue()))
                .toList();
    }

    @GetMapping("/feedback")
    public AdminStatsDto.FeedbackStatsDto getFeedback() {
        long up = 0, down = 0;
        for (Object[] row : feedbackRepo.countByRating()) {
            if ("UP".equals(row[0].toString())) up = ((Number) row[1]).longValue();
            else if ("DOWN".equals(row[0].toString())) down = ((Number) row[1]).longValue();
        }
        return new AdminStatsDto.FeedbackStatsDto(up, down);
    }

    @GetMapping("/top-streaks")
    public List<AdminStatsDto.TopStreakDto> getTopStreaks() {
        return streakRepo.findTopByStreak(PageRequest.of(0, 10)).stream()
                .map(s -> {
                    String name = userProfileClient.fetchUserInfo(s.getUserId())
                            .map(info -> {
                                String first = info.get("firstName");
                                String last  = info.get("lastName");
                                if (first != null && last != null) return first + " " + last;
                                if (first != null) return first;
                                return info.getOrDefault("email", "User #" + s.getUserId());
                            })
                            .orElse("User #" + s.getUserId());
                    return new AdminStatsDto.TopStreakDto(s.getUserId(), name, s.getCurrentStreak());
                })
                .toList();
    }
}
