package tn.esprit.mentorservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tn.esprit.mentorservice.common.ApiException;
import tn.esprit.mentorservice.dto.*;
import tn.esprit.mentorservice.entity.BookmarkedAdvice;
import tn.esprit.mentorservice.entity.MentorSession;
import tn.esprit.mentorservice.entity.StudyPlan;
import tn.esprit.mentorservice.entity.StudyPlanItem; // used in completeStudyPlanItem
import tn.esprit.mentorservice.entity.MentorAdvice;
import tn.esprit.mentorservice.repository.BookmarkedAdviceRepository;
import tn.esprit.mentorservice.repository.MentorAdviceRepository;
import tn.esprit.mentorservice.repository.MentorSessionRepository;
import tn.esprit.mentorservice.repository.StudyPlanItemRepository;
import tn.esprit.mentorservice.repository.StudyPlanRepository;
import tn.esprit.mentorservice.security.MentorPrincipal;
import tn.esprit.mentorservice.service.AdaptiveDifficultyService;
import tn.esprit.mentorservice.service.ConversationService;
import tn.esprit.mentorservice.service.FeedbackService;
import tn.esprit.mentorservice.service.MentorInteractionService;
import tn.esprit.mentorservice.service.PortfolioService;
import tn.esprit.mentorservice.service.StreakService;
import tn.esprit.mentorservice.client.GoogleAIClient;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/mentor")
@RequiredArgsConstructor
public class MentorController {

    private final PortfolioService portfolioService;
    private final MentorInteractionService mentorInteractionService;
    private final FeedbackService feedbackService;
    private final ConversationService conversationService;
    private final AdaptiveDifficultyService adaptiveDifficultyService;
    private final StudyPlanRepository studyPlanRepository;
    private final StudyPlanItemRepository studyPlanItemRepository;
    private final BookmarkedAdviceRepository bookmarkRepo;
    private final MentorSessionRepository mentorSessionRepository;
    private final MentorAdviceRepository mentorAdviceRepository;
    private final StreakService streakService;
    private final GoogleAIClient googleAIClient;

    // ── Portfolio ────────────────────────────────────────────────────────────

    @GetMapping("/portfolio/exists")
    public PortfolioExistsResponse portfolioExists(@AuthenticationPrincipal MentorPrincipal principal) {
        return new PortfolioExistsResponse(portfolioService.portfolioExists(principal.getUserId()));
    }

    @GetMapping("/portfolio")
    public PortfolioResponseDto getPortfolio(@AuthenticationPrincipal MentorPrincipal principal) {
        return portfolioService.get(principal.getUserId());
    }

    @PutMapping("/portfolio")
    public PortfolioResponseDto upsertPortfolio(
            @AuthenticationPrincipal MentorPrincipal principal,
            @Valid @RequestBody PortfolioUpsertRequest request
    ) {
        return portfolioService.upsert(principal.getUserId(), request);
    }

    // ── AI interaction ────────────────────────────────────────────────────────

    @PostMapping("/ask")
    public MentorAdviceResponseDto ask(
            @AuthenticationPrincipal MentorPrincipal principal,
            @Valid @RequestBody MentorAskRequest request
    ) {
        MentorAdviceResponseDto result = mentorInteractionService.ask(principal.getUserId(), request);
        streakService.recordActivity(principal.getUserId());
        return result;
    }

    @GetMapping("/weekly-brief")
    public MentorAdviceResponseDto weeklyBrief(
            @AuthenticationPrincipal MentorPrincipal principal,
            @RequestParam(name = "locale", required = false) String locale
    ) {
        MentorAdviceResponseDto result = mentorInteractionService.weeklyBrief(principal.getUserId(), locale);
        streakService.recordActivity(principal.getUserId());
        return result;
    }

    @GetMapping("/pre-exam-tips/{formationId}")
    public MentorAdviceResponseDto preExamTips(
            @AuthenticationPrincipal MentorPrincipal principal,
            @PathVariable("formationId") long formationId,
            @RequestParam(name = "locale", required = false) String locale
    ) {
        MentorAdviceResponseDto result = mentorInteractionService.preExamTips(principal.getUserId(), formationId, locale);
        streakService.recordActivity(principal.getUserId());
        return result;
    }

    @GetMapping("/learning-path")
    public MentorAdviceResponseDto getLearningPath(
            @AuthenticationPrincipal MentorPrincipal principal,
            @RequestParam(name = "refresh", defaultValue = "false") boolean refresh
    ) {
        MentorAdviceResponseDto result = mentorInteractionService.generateLearningPath(principal.getUserId(), refresh);
        streakService.recordActivity(principal.getUserId());
        return result;
    }

    @GetMapping("/advice/history")
    public Page<MentorHistoryItemDto> adviceHistory(
            @AuthenticationPrincipal MentorPrincipal principal,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return mentorInteractionService.adviceHistory(principal.getUserId(), page, Math.min(size, 50));
    }

    // ── Feedback ──────────────────────────────────────────────────────────────

    @PostMapping("/feedback")
    public FeedbackResponseDto submitFeedback(
            @AuthenticationPrincipal MentorPrincipal principal,
            @Valid @RequestBody FeedbackRequest request
    ) {
        return feedbackService.submitFeedback(principal.getUserId(), request);
    }

    @GetMapping("/feedback/history")
    public List<FeedbackResponseDto> feedbackHistory(@AuthenticationPrincipal MentorPrincipal principal) {
        return feedbackService.getUserFeedbackHistory(principal.getUserId());
    }

    // ── Conversation threads ──────────────────────────────────────────────────

    @GetMapping("/threads")
    public List<ThreadSummaryDto> getThreads(@AuthenticationPrincipal MentorPrincipal principal) {
        return conversationService.getUserThreads(principal.getUserId());
    }

    @PostMapping("/threads")
    @ResponseStatus(HttpStatus.CREATED)
    public ThreadDetailDto createThread(
            @AuthenticationPrincipal MentorPrincipal principal,
            @Valid @RequestBody CreateThreadRequest request
    ) {
        return conversationService.createThread(principal.getUserId(), request);
    }

    @GetMapping("/threads/{threadId}")
    public ThreadDetailDto getThread(
            @AuthenticationPrincipal MentorPrincipal principal,
            @PathVariable Long threadId
    ) {
        return conversationService.getThread(principal.getUserId(), threadId);
    }

    @DeleteMapping("/threads/{threadId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archiveThread(
            @AuthenticationPrincipal MentorPrincipal principal,
            @PathVariable Long threadId
    ) {
        conversationService.archiveThread(principal.getUserId(), threadId);
    }

    // ── Adaptive difficulty ────────────────────────────────────────────────────

    @GetMapping("/difficulty-alerts")
    public List<DifficultyAlert> getDifficultyAlerts(@AuthenticationPrincipal MentorPrincipal principal) {
        return adaptiveDifficultyService.detectDifficulties(principal.getUserId());
    }

    // ── Study plans ────────────────────────────────────────────────────────────

    @GetMapping("/study-plans")
    public List<StudyPlanResponseDto> getStudyPlans(@AuthenticationPrincipal MentorPrincipal principal) {
        return studyPlanRepository.findByUserIdOrderByCreatedAtDesc(principal.getUserId())
                .stream()
                .map(this::toStudyPlanDto)
                .toList();
    }

    private StudyPlanResponseDto toStudyPlanDto(StudyPlan plan) {
        String channel = plan.getSourceSession() != null && plan.getSourceSession().getChannel() != null
                ? plan.getSourceSession().getChannel().name()
                : null;
        List<StudyPlanItemResponseDto> items = plan.getItems().stream()
                .sorted(java.util.Comparator.comparingInt(i -> i.getSortOrder() != null ? i.getSortOrder() : 0))
                .map(i -> new StudyPlanItemResponseDto(
                        i.getId(),
                        i.getSortOrder(),
                        i.getItemType() != null ? i.getItemType().name() : null,
                        i.getTitle(),
                        i.getDurationMinutes(),
                        i.getResourceRef(),
                        i.isCompleted(),
                        i.getCompletedAt()))
                .toList();
        return new StudyPlanResponseDto(plan.getId(), plan.getTitle(), plan.getCreatedAt(),
                plan.getValidUntil(), channel, items);
    }

    @PostMapping("/remediation/{formationId}")
    public MentorAdviceResponseDto getRemediation(
            @AuthenticationPrincipal MentorPrincipal principal,
            @PathVariable long formationId
    ) {
        MentorAdviceResponseDto result = mentorInteractionService.generateRemediation(principal.getUserId(), formationId);
        streakService.recordActivity(principal.getUserId());
        return result;
    }

    @PostMapping("/study-plans/items/{itemId}/complete")
    public ItemCompleteResponseDto completeStudyPlanItem(
            @AuthenticationPrincipal MentorPrincipal principal,
            @PathVariable Long itemId
    ) {
        StudyPlanItem item = studyPlanItemRepository
                .findByIdAndUserId(itemId, principal.getUserId())
                .orElseThrow(() -> new ApiException(404, "Study plan item not found"));

        if (!item.isCompleted()) {
            item.setCompleted(true);
            item.setCompletedAt(LocalDateTime.now());
            studyPlanItemRepository.save(item);
            streakService.recordActivity(principal.getUserId());
        }

        StudyPlan plan = studyPlanRepository.findById(item.getStudyPlan().getId())
                .orElseThrow(() -> new ApiException(404, "Study plan not found"));

        List<StudyPlanItem> allItems = plan.getItems();
        long completedCount = allItems.stream().filter(StudyPlanItem::isCompleted).count();
        long totalCount = allItems.size();
        boolean planComplete = completedCount == totalCount;

        StudyPlanItem nextItem = allItems.stream()
                .filter(i -> !i.isCompleted())
                .min(Comparator.comparingInt(i -> (i.getSortOrder() != null ? i.getSortOrder() : Integer.MAX_VALUE)))
                .orElse(null);

        StreakResponseDto streak = streakService.getStreak(principal.getUserId());
        String aiMessage = generateMotivationMessage(plan.getTitle(), item, nextItem, completedCount, totalCount, streak.currentStreak(), planComplete);
        return new ItemCompleteResponseDto(aiMessage, streak.currentStreak(), planComplete);
    }

    private String generateMotivationMessage(
            String planTitle, StudyPlanItem completed, StudyPlanItem next,
            long doneCount, long totalCount, int streak, boolean planComplete
    ) {
        String nextPart = (next != null)
                ? "Next item: \"" + next.getTitle() + "\" (" + (next.getItemType() != null ? next.getItemType() : "task") + ")"
                : "This was the last item in the plan!";

        String prompt = """
                You are a warm, energetic AI mentor celebrating a learner's progress.

                Study plan: "%s"
                Just completed: "%s" (%s%s)
                Progress: %d/%d items done
                %s
                Current streak: %d day(s)

                Write a short motivational message (2-3 sentences, plain text only, no markdown, no bullet points).
                - Mention the completed item by name.
                - If there is a next item, mention it and encourage moving to it.
                - If the plan is 100%% complete, celebrate enthusiastically and congratulate finishing the whole plan.
                - Be specific, warm, and energetic. Use 1-2 relevant emojis naturally inline.
                """.formatted(
                planTitle,
                completed.getTitle(),
                completed.getItemType() != null ? completed.getItemType() : "task",
                completed.getDurationMinutes() != null ? ", " + completed.getDurationMinutes() + " min" : "",
                doneCount, totalCount,
                nextPart,
                streak
        );

        try {
            return googleAIClient.generateText(prompt, 200, 0.85);
        } catch (Exception e) {
            return planComplete
                    ? "You finished the entire plan — incredible work! 🏆 Take a moment to celebrate this achievement."
                    : "Great job completing \"" + completed.getTitle() + "\"! Keep the momentum going 💪";
        }
    }

    // ── Streak ─────────────────────────────────────────────────────────────────

    @GetMapping("/streak")
    public StreakResponseDto getStreak(@AuthenticationPrincipal MentorPrincipal principal) {
        streakService.recordActivity(principal.getUserId());
        return streakService.getStreak(principal.getUserId());
    }

    // ── Bookmarks ──────────────────────────────────────────────────────────────

    @PostMapping("/advice/{sessionId}/bookmark")
    public ResponseEntity<Map<String, Object>> toggleBookmark(
            @AuthenticationPrincipal MentorPrincipal principal,
            @PathVariable Long sessionId,
            @RequestParam(required = false) String note
    ) {
        long userId = principal.getUserId();
        long sid = sessionId;
        Optional<BookmarkedAdvice> existing = bookmarkRepo.findByUserIdAndSessionId(userId, sid);

        if (existing.isPresent()) {
            bookmarkRepo.delete(existing.get());
            return ResponseEntity.ok(Map.of("bookmarked", false, "sessionId", sid));
        }

        MentorSession session = mentorSessionRepository.findById(sid)
                .orElseThrow(() -> new ApiException(404, "Session not found"));
        if (!Long.valueOf(userId).equals(session.getUserId())) {
            throw new ApiException(403, "Not your session");
        }

        BookmarkedAdvice bm = BookmarkedAdvice.builder()
                .userId(userId)
                .session(session)
                .note(note)
                .build();
        bookmarkRepo.save(bm);
        return ResponseEntity.ok(Map.of("bookmarked", true, "sessionId", sid));
    }

    @GetMapping("/advice/bookmarks")
    public Page<BookmarkedAdviceDto> getBookmarks(
            @AuthenticationPrincipal MentorPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (size > 50) size = 50;
        Page<BookmarkedAdvice> bookmarks = bookmarkRepo.findPageByUserId(
                principal.getUserId(), PageRequest.of(page, size));

        return bookmarks.map(b -> {
            MentorAdvice advice = mentorAdviceRepository.findBySessionId(b.getSession().getId()).orElse(null);
            String preview = null;
            String summary = null;
            if (advice != null) {
                summary = advice.getSummary();
                String src = advice.getStructuredJson() != null ? advice.getStructuredJson() : advice.getSummary();
                preview = src != null && src.length() > 200 ? src.substring(0, 200) + "…" : src;
            }
            MentorSession s = b.getSession();
            return new BookmarkedAdviceDto(
                    b.getId(), s.getId(), s.getChannel(),
                    s.getFormationId(), s.getCreatedAt(), b.getCreatedAt(),
                    b.getNote(), summary, preview);
        });
    }
}
