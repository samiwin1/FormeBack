package tn.esprit.mentorservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.mentorservice.common.ApiException;
import tn.esprit.mentorservice.domain.FeedbackRating;
import tn.esprit.mentorservice.domain.MentorChannel;
import tn.esprit.mentorservice.dto.UserSummaryItemDto;
import tn.esprit.mentorservice.entity.*;
import tn.esprit.mentorservice.repository.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/mentor/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminUserController {

    private final LearnerPortfolioRepository portfolioRepository;
    private final MentorSessionRepository sessionRepository;
    private final MentorFeedbackRepository feedbackRepository;
    private final StudyPlanRepository studyPlanRepository;
    private final ConversationThreadRepository threadRepository;

    @GetMapping
    public List<UserSummaryItemDto> getUsersWithPortfolios() {
        List<Long> userIds = portfolioRepository.findAllUserIdsWithPortfolio();
        return userIds.stream()
                .map(userId -> {
                    Optional<LearnerPortfolio> portfolio = portfolioRepository.findByUserId(userId);
                    return portfolio.map(p -> UserSummaryItemDto.builder()
                            .userId(userId)
                            .currentRole(p.getCurrentRole())
                            .targetRole(p.getTargetRole())
                            .experienceLevel(p.getExperienceLevel())
                            .createdAt(p.getCreatedAt())
                            .build()).orElse(null);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @GetMapping("/{userId}/summary")
    public Map<String, Object> getUserSummary(@PathVariable Long userId) {
        Optional<LearnerPortfolio> portfolioOpt = portfolioRepository.findWithSkillsByUserId(userId);

        if (portfolioOpt.isEmpty()) {
            throw new ApiException(404, "User portfolio not found");
        }

        LearnerPortfolio portfolio = portfolioOpt.get();

        // Build portfolio section
        Map<String, Object> portfolioData = new LinkedHashMap<>();
        portfolioData.put("currentRole", portfolio.getCurrentRole());
        portfolioData.put("targetRole", portfolio.getTargetRole());
        portfolioData.put("experienceLevel", portfolio.getExperienceLevel().name());
        portfolioData.put("learningStyle", portfolio.getLearningStyle().name());
        portfolioData.put("weeklyStudyHours", portfolio.getWeeklyStudyHours());
        portfolioData.put("skills", portfolio.getSkills().stream()
                .map(skill -> Map.of(
                    "skillName", skill.getSkillName(),
                    "level", skill.getSkillLevel().name()
                ))
                .collect(Collectors.toList()));

        // Build sessions section
        long totalSessions = sessionRepository.countByUserId(userId);
        List<Object[]> sessionsByChannelRows = sessionRepository.countByUserIdGroupedByChannel(userId);
        Map<String, Long> sessionsByChannel = sessionsByChannelRows.stream()
                .filter(row -> row.length == 2 && row[0] instanceof MentorChannel && row[1] instanceof Number)
                .collect(Collectors.toMap(
                    row -> ((MentorChannel) row[0]).name(),
                    row -> ((Number) row[1]).longValue()
                ));
        Optional<Instant> lastSessionAt = sessionRepository.findLastSessionAtByUserId(userId);

        Map<String, Object> sessionsData = new LinkedHashMap<>();
        sessionsData.put("total", totalSessions);
        sessionsData.put("byChannel", sessionsByChannel);
        sessionsData.put("lastSessionAt", lastSessionAt.orElse(null));

        // Build feedback section
        List<MentorFeedback> feedbacks = feedbackRepository.findByUserIdOrderByCreatedAtDesc(userId);
        long upCount = feedbackRepository.countByUserIdAndRating(userId, FeedbackRating.UP);
        long downCount = feedbackRepository.countByUserIdAndRating(userId, FeedbackRating.DOWN);
        Optional<Instant> lastFeedbackAt = feedbackRepository.findLastFeedbackAtByUserId(userId);

        Map<String, Object> feedbackData = new LinkedHashMap<>();
        feedbackData.put("total", feedbacks.size());
        feedbackData.put("upCount", upCount);
        feedbackData.put("downCount", downCount);
        feedbackData.put("satisfactionRate", feedbacks.isEmpty() ? 0.0 : (upCount * 100.0) / feedbacks.size());
        feedbackData.put("lastFeedbackAt", lastFeedbackAt.orElse(null));

        // Build study plans section
        long totalPlans = studyPlanRepository.countByUserId(userId);
        Optional<Object[]> latestPlan = studyPlanRepository.findLatestPlanTitleAndDateByUserId(userId);

        Map<String, Object> studyPlansData = new LinkedHashMap<>();
        studyPlansData.put("total", totalPlans);
        studyPlansData.put("lastPlanTitle", latestPlan.map(arr -> (String) arr[0]).orElse(null));
        studyPlansData.put("lastPlanCreatedAt", latestPlan.map(arr -> (Instant) arr[1]).orElse(null));

        // Build threads section
        List<ConversationThread> activeThreads = threadRepository.findByUserIdAndIsActiveTrueOrderByUpdatedAtDesc(userId);

        Map<String, Object> threadsData = new LinkedHashMap<>();
        threadsData.put("activeCount", activeThreads.size());

        // Determine last active timestamp
        Instant lastActiveAt = null;
        if (lastSessionAt.isPresent()) {
            lastActiveAt = lastSessionAt.get();
        }
        if (lastFeedbackAt.isPresent() && (lastActiveAt == null || lastFeedbackAt.get().isAfter(lastActiveAt))) {
            lastActiveAt = lastFeedbackAt.get();
        }
        if (!activeThreads.isEmpty()) {
            Instant threadUpdatedAt = toInstant(activeThreads.get(0).getUpdatedAt());
            if (lastActiveAt == null || threadUpdatedAt.isAfter(lastActiveAt)) {
                lastActiveAt = threadUpdatedAt;
            }
        }

        // Build final response
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", userId);
        result.put("hasPortfolio", true);
        result.put("portfolio", portfolioData);
        result.put("sessions", sessionsData);
        result.put("feedback", feedbackData);
        result.put("studyPlans", studyPlansData);
        result.put("threads", threadsData);
        result.put("lastActiveAt", lastActiveAt);

        return result;
    }

    private Instant toInstant(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.atZone(ZoneId.systemDefault()).toInstant();
    }
}
