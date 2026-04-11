package tn.esprit.mentorservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.mentorservice.domain.FeedbackRating;
import tn.esprit.mentorservice.entity.MentorFeedback;
import tn.esprit.mentorservice.repository.MentorFeedbackRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/mentor/admin/feedback")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminFeedbackController {

    private final MentorFeedbackRepository feedbackRepository;

    // ── Dashboard (single call) ────────────────────────────────────────────────

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false, defaultValue = "30") int timeRange
    ) {
        List<MentorFeedback> all = getFiltered(dateFrom, dateTo, timeRange);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("statistics", buildStatistics(all));
        result.put("trends", buildTrends(all));
        result.put("topMentors", Collections.emptyList());
        result.put("bottomMentors", Collections.emptyList());
        result.put("formationStats", Collections.emptyList());
        result.put("recentComments", buildRecentComments(all, 20));
        return result;
    }

    @GetMapping("/statistics")
    public Map<String, Object> getStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false, defaultValue = "30") int timeRange
    ) {
        return buildStatistics(getFiltered(dateFrom, dateTo, timeRange));
    }

    @GetMapping("/trends")
    public List<Map<String, Object>> getTrends(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false, defaultValue = "30") int timeRange
    ) {
        return buildTrends(getFiltered(dateFrom, dateTo, timeRange));
    }

    @GetMapping("/comments")
    public List<Map<String, Object>> getRecentComments(
            @RequestParam(required = false, defaultValue = "20") int limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false, defaultValue = "30") int timeRange
    ) {
        return buildRecentComments(getFiltered(dateFrom, dateTo, timeRange), limit);
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private List<MentorFeedback> getFiltered(LocalDate dateFrom, LocalDate dateTo, int timeRange) {
        List<MentorFeedback> all = feedbackRepository.findAll();
        LocalDateTime from;
        LocalDateTime to;

        if (dateFrom != null && dateTo != null) {
            from = dateFrom.atStartOfDay();
            to = dateTo.atTime(LocalTime.MAX);
        } else {
            to = LocalDateTime.now();
            from = to.minusDays(timeRange > 0 ? timeRange : 30);
        }

        final LocalDateTime finalFrom = from;
        final LocalDateTime finalTo = to;
        return all.stream()
                .filter(f -> f.getCreatedAt() != null
                        && !f.getCreatedAt().isBefore(finalFrom)
                        && !f.getCreatedAt().isAfter(finalTo))
                .collect(Collectors.toList());
    }

    private Map<String, Object> buildStatistics(List<MentorFeedback> feedbacks) {
        long upCount = feedbacks.stream().filter(f -> f.getRating() == FeedbackRating.UP).count();
        long downCount = feedbacks.stream().filter(f -> f.getRating() == FeedbackRating.DOWN).count();
        long total = feedbacks.size();
        double satisfactionRate = total == 0 ? 0.0 : Math.round((upCount * 100.0 / total) * 100.0) / 100.0;

        Map<String, Object> sentiment = new LinkedHashMap<>();
        sentiment.put("positive", upCount);
        sentiment.put("negative", downCount);
        sentiment.put("neutral", 0);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalFeedback", total);
        stats.put("upCount", upCount);
        stats.put("downCount", downCount);
        stats.put("satisfactionRate", satisfactionRate);
        stats.put("sentiment", sentiment);
        return stats;
    }

    private List<Map<String, Object>> buildTrends(List<MentorFeedback> feedbacks) {
        Map<String, long[]> daily = new LinkedHashMap<>();
        feedbacks.stream()
                .filter(f -> f.getCreatedAt() != null)
                .sorted(Comparator.comparing(MentorFeedback::getCreatedAt))
                .forEach(f -> {
                    String date = f.getCreatedAt().toLocalDate().toString();
                    daily.computeIfAbsent(date, k -> new long[2]);
                    if (f.getRating() == FeedbackRating.UP) daily.get(date)[0]++;
                    else if (f.getRating() == FeedbackRating.DOWN) daily.get(date)[1]++;
                });

        return daily.entrySet().stream().map(e -> {
            long up = e.getValue()[0];
            long down = e.getValue()[1];
            long total = up + down;
            double rate = total == 0 ? 0.0 : Math.round((up * 100.0 / total) * 100.0) / 100.0;
            Map<String, Object> trend = new LinkedHashMap<>();
            trend.put("date", e.getKey());
            trend.put("upCount", up);
            trend.put("downCount", down);
            trend.put("satisfactionRate", rate);
            return trend;
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildRecentComments(List<MentorFeedback> feedbacks, int limit) {
        return feedbacks.stream()
                .filter(f -> f.getComment() != null && !f.getComment().isBlank())
                .sorted(Comparator.comparing(MentorFeedback::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .map(f -> {
                    Map<String, Object> c = new LinkedHashMap<>();
                    c.put("id", f.getId());
                    c.put("mentorId", null);
                    c.put("mentorName", "User #" + f.getUserId());
                    c.put("formationId", null);
                    c.put("formationName", "AI Mentor session #" + f.getSessionId());
                    c.put("comment", f.getComment());
                    c.put("rating", f.getRating() == FeedbackRating.UP ? "UP" : "DOWN");
                    c.put("createdAt", f.getCreatedAt() != null ? f.getCreatedAt().toString() : null);
                    return c;
                })
                .collect(Collectors.toList());
    }
}
