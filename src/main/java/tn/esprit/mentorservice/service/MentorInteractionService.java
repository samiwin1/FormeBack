package tn.esprit.mentorservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.mentorservice.client.CertificationMentorClient;
import tn.esprit.mentorservice.client.FormationCatalogClient;
import tn.esprit.mentorservice.client.FormationProgressClient;
import tn.esprit.mentorservice.client.GoogleAIClient;
import tn.esprit.mentorservice.domain.MentorChannel;
import tn.esprit.mentorservice.dto.ChatTurnDto;
import tn.esprit.mentorservice.dto.CreateThreadRequest;
import tn.esprit.mentorservice.dto.DifficultyAlert;
import tn.esprit.mentorservice.dto.LearningPathContext;
import tn.esprit.mentorservice.dto.MentorAdviceResponseDto;
import tn.esprit.mentorservice.dto.MentorAskRequest;
import tn.esprit.mentorservice.dto.MentorHistoryItemDto;
import tn.esprit.mentorservice.dto.ThreadDetailDto;
import tn.esprit.mentorservice.dto.certification.MentorLearnerContextDto;
import tn.esprit.mentorservice.dto.formation.FormationCatalogItemDto;
import tn.esprit.mentorservice.dto.formation.LearnerProgressSnapshotDto;
import tn.esprit.mentorservice.entity.FormationDemand;
import tn.esprit.mentorservice.entity.LearnerPortfolio;
import tn.esprit.mentorservice.entity.MentorAdvice;
import tn.esprit.mentorservice.entity.MentorSession;
import tn.esprit.mentorservice.repository.BookmarkedAdviceRepository;
import tn.esprit.mentorservice.repository.FormationDemandRepository;
import tn.esprit.mentorservice.repository.MentorAdviceRepository;
import tn.esprit.mentorservice.repository.MentorSessionRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MentorInteractionService {

    private static final int ASK_MAX_OUTPUT_TOKENS = 4096;

    private final PortfolioService portfolioService;
    private final FormationProgressClient formationProgressClient;
    private final FormationCatalogClient formationCatalogClient;
    private final CertificationMentorClient certificationMentorClient;
    private final LearnerContextBuilder learnerContextBuilder;
    private final MentorPromptService mentorPromptService;
    private final GoogleAIClient googleAIClient;
    private final MentorAdviceParser mentorAdviceParser;
    private final MentorSessionRepository mentorSessionRepository;
    private final MentorAdviceRepository mentorAdviceRepository;
    private final StudyPlanPersistenceService studyPlanPersistenceService;
    private final MentorRateLimiter mentorRateLimiter;
    private final ConversationService conversationService;
    private final FeedbackService feedbackService;
    private final AdaptiveDifficultyService adaptiveDifficultyService;
    private final BookmarkedAdviceRepository bookmarkedAdviceRepository;
    private final FormationDemandRepository formationDemandRepository;
    private final MentorNotificationService mentorNotificationService;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Value("${google.ai.model}")
    private String primaryModel;

    // ── Internal persistence helper ─────────────────────────────────────────

    private record PersistResult(MentorSession session, MentorAdvice advice) {}

    private PersistResult persistAdviceInternal(
            long userId,
            MentorChannel channel,
            Long formationId,
            LocalDate periodStart,
            String raw
    ) {
        MentorAdviceParser.ParsedAdvice parsed = mentorAdviceParser.parse(raw);

        MentorSession session = MentorSession.builder()
                .userId(userId)
                .channel(channel)
                .formationId(formationId)
                .modelUsed(primaryModel)
                .periodStart(periodStart)
                .build();
        session = mentorSessionRepository.save(session);

        MentorAdvice advice = MentorAdvice.builder()
                .session(session)
                .summary(parsed.summary())
                .structuredJson(parsed.structuredJson())
                .rawText(raw)
                .build();
        advice = mentorAdviceRepository.save(advice);

        studyPlanPersistenceService.tryPersistFromAdviceJson(userId, session, parsed.rootNode());
        return new PersistResult(session, advice);
    }

    private MentorAdviceResponseDto persistAdvice(
            long userId,
            MentorChannel channel,
            Long formationId,
            LocalDate periodStart,
            String raw
    ) {
        PersistResult r = persistAdviceInternal(userId, channel, formationId, periodStart, raw);
        return toDto(r.session(), r.advice(), null);
    }

    // ── Ask (thread-aware) ──────────────────────────────────────────────────

    @Transactional
    public MentorAdviceResponseDto ask(long userId, MentorAskRequest request) {
        mentorRateLimiter.verifyAskAllowed(userId);
        LearnerPortfolio portfolio = portfolioService.requirePortfolio(userId);

        // Resolve thread: continue existing or create a new one
        Long threadId = request.threadId();
        List<ChatTurnDto> historyTurns;
        if (threadId != null) {
            conversationService.requireThread(userId, threadId); // ownership check
            historyTurns = conversationService.getHistoryForPrompt(threadId);
        } else {
            ThreadDetailDto newThread = conversationService.createThread(
                    userId, new CreateThreadRequest(null, request.formationId()));
            threadId = newThread.id();
            historyTurns = request.conversationHistory() != null
                    ? request.conversationHistory()
                    : List.of();
        }
        final Long resolvedThreadId = threadId;

        // Save the user's message before AI call
        conversationService.addUserMessage(resolvedThreadId, request.question());

        // Build context
        Optional<LearnerProgressSnapshotDto> progress = formationProgressClient.fetchLearnerProgress(
                userId, request.formationId());
        String portfolioJson = learnerContextBuilder.buildPortfolioJson(portfolio);
        String progressJson = progress.map(learnerContextBuilder::buildProgressJson).orElse("null");
        String formationCatalogJson = learnerContextBuilder.buildFormationCatalogJson(
                formationCatalogClient.fetchPublishedCatalog());
        String certificationContextJson = certificationMentorClient.fetchLearnerContext(userId)
                .map(learnerContextBuilder::buildCertificationContextJson)
                .orElse("null");
        String feedbackContext = feedbackService.buildFeedbackContextForPrompt(userId);
        String priorConversation = formatConversationHistory(historyTurns);

        String prompt = mentorPromptService.buildAskPrompt(
                request.locale(),
                portfolioJson,
                progressJson,
                formationCatalogJson,
                certificationContextJson,
                priorConversation,
                request.question(),
                request.formationId(),
                feedbackContext
        );
        String raw = googleAIClient.generateText(prompt, ASK_MAX_OUTPUT_TOKENS, 0.35);

        // Persist AI session + advice
        PersistResult result = persistAdviceInternal(userId, MentorChannel.ASK, request.formationId(), null, raw);

        // If Gemini detected a missing formation, record the demand
        tryPersistFormationDemand(userId, result.advice().getStructuredJson());

        // Save assistant message to thread
        String assistantContent = result.advice().getSummary() != null
                ? result.advice().getSummary()
                : (result.advice().getRawText() != null ? result.advice().getRawText() : "");
        conversationService.addAssistantMessage(resolvedThreadId, assistantContent, result.session().getId());

        // Auto-title the thread using the first question if it still has the default title
        conversationService.autoTitleIfNeeded(resolvedThreadId, request.question());

        return toDto(result.session(), result.advice(), resolvedThreadId);
    }

    // ── Weekly brief ────────────────────────────────────────────────────────

    @Transactional
    public MentorAdviceResponseDto weeklyBrief(long userId, String locale) {
        LocalDate weekStart = LocalDate.now(ZoneId.systemDefault())
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        Optional<MentorSession> existingSession = mentorSessionRepository
                .findFirstByUserIdAndChannelAndPeriodStartOrderByCreatedAtDesc(
                        userId, MentorChannel.WEEKLY_BRIEF, weekStart);

        if (existingSession.isPresent()) {
            Optional<MentorAdvice> adv = mentorAdviceRepository.findBySessionId(existingSession.get().getId());
            if (adv.isPresent()) {
                return toDto(existingSession.get(), adv.get(), null);
            }
        }

        LearnerPortfolio portfolio = portfolioService.requirePortfolio(userId);
        Optional<LearnerProgressSnapshotDto> progress = formationProgressClient.fetchLearnerProgress(userId, null);
        String portfolioJson = learnerContextBuilder.buildPortfolioJson(portfolio);
        String progressJson = progress.map(learnerContextBuilder::buildProgressJson).orElse("null");
        String prompt = mentorPromptService.buildWeeklyBriefPrompt(locale, portfolioJson, progressJson);
        String raw = googleAIClient.generateText(prompt, 1536, 0.4);
        return persistAdvice(userId, MentorChannel.WEEKLY_BRIEF, null, weekStart, raw);
    }

    // ── Pre-exam tips ────────────────────────────────────────────────────────

    @Transactional
    public MentorAdviceResponseDto preExamTips(long userId, long formationId, String locale) {
        LearnerPortfolio portfolio = portfolioService.requirePortfolio(userId);
        Optional<LearnerProgressSnapshotDto> progress = formationProgressClient.fetchLearnerProgress(
                userId, formationId);
        String portfolioJson = learnerContextBuilder.buildPortfolioJson(portfolio);
        String progressJson = progress.map(learnerContextBuilder::buildProgressJson).orElse("null");
        String prompt = mentorPromptService.buildPreExamTipsPrompt(locale, portfolioJson, progressJson, formationId);
        String raw = googleAIClient.generateText(prompt, 1536, 0.35);
        return persistAdvice(userId, MentorChannel.PRE_EXAM, formationId, null, raw);
    }

    // ── Learning path ────────────────────────────────────────────────────────

    @Transactional
    public MentorAdviceResponseDto generateLearningPath(long userId, boolean refresh) {
        LocalDate weekStart = LocalDate.now(ZoneId.systemDefault())
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        if (!refresh) {
            Optional<MentorSession> existing = mentorSessionRepository
                    .findFirstByUserIdAndChannelAndPeriodStartOrderByCreatedAtDesc(
                            userId, MentorChannel.LEARNING_PATH, weekStart);
            if (existing.isPresent()) {
                Optional<MentorAdvice> adv = mentorAdviceRepository.findBySessionId(existing.get().getId());
                if (adv.isPresent()) {
                    return toDto(existing.get(), adv.get(), null);
                }
            }
        }

        LearnerPortfolio portfolio = portfolioService.requirePortfolio(userId);
        List<FormationCatalogItemDto> catalog = formationCatalogClient.fetchPublishedCatalog();
        Optional<LearnerProgressSnapshotDto> progress = formationProgressClient.fetchLearnerProgress(userId, null);
        Optional<MentorLearnerContextDto> certContext = certificationMentorClient.fetchLearnerContext(userId);

        LearningPathContext ctx = learnerContextBuilder.buildLearningPathContext(
                portfolio, catalog, progress, certContext);
        String prompt = mentorPromptService.buildLearningPathPrompt(ctx);
        String raw = googleAIClient.generateText(prompt, 4096, 0.3);
        return persistAdvice(userId, MentorChannel.LEARNING_PATH, null, weekStart, raw);
    }

    // ── Remediation ──────────────────────────────────────────────────────────

    @Transactional
    public MentorAdviceResponseDto generateRemediation(long userId, long formationId) {
        LearnerPortfolio portfolio = portfolioService.requirePortfolio(userId);
        Optional<LearnerProgressSnapshotDto> progress = formationProgressClient.fetchLearnerProgress(
                userId, formationId);
        String portfolioJson = learnerContextBuilder.buildPortfolioJson(portfolio);
        String progressJson = progress.map(learnerContextBuilder::buildProgressJson).orElse("null");

        // Find the most relevant difficulty alert for this formation, or use a generic description
        List<DifficultyAlert> alerts = adaptiveDifficultyService.detectDifficulties(userId);
        String alertDescription = alerts.stream()
                .filter(a -> Long.valueOf(formationId).equals(a.formationId()))
                .map(DifficultyAlert::description)
                .findFirst()
                .orElse("Learner is requesting remediation for formation id=" + formationId);

        String prompt = mentorPromptService.buildRemediationPrompt(
                portfolio.getPreferredLanguage(),
                portfolioJson,
                progressJson,
                alertDescription
        );
        String raw = googleAIClient.generateText(prompt, 2048, 0.35);
        return persistAdvice(userId, MentorChannel.REMEDIATION, formationId, null, raw);
    }

    // ── Advice history ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<MentorHistoryItemDto> adviceHistory(long userId, int page, int size) {
        Page<MentorAdvice> rows = mentorAdviceRepository.findPageByUserId(
                userId, PageRequest.of(page, size));
        Set<Long> bookmarkedIds = bookmarkedAdviceRepository.findSessionIdsByUserId(userId);
        return rows.map(a -> {
            MentorSession s = a.getSession();
            String preview = a.getStructuredJson();
            if (preview != null && preview.length() > 200) {
                preview = preview.substring(0, 200) + "…";
            }
            return new MentorHistoryItemDto(
                    s.getId(),
                    s.getChannel(),
                    s.getFormationId(),
                    s.getCreatedAt(),
                    a.getSummary(),
                    preview,
                    bookmarkedIds.contains(s.getId())
            );
        });
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void tryPersistFormationDemand(long userId, String structuredJson) {
        if (structuredJson == null || structuredJson.isBlank()) return;
        try {
            var node = OBJECT_MAPPER.readTree(structuredJson);
            String type = node.path("type").asText(null);
            if (!"FORMATION_MISSING".equals(type)) return;
            String role = node.path("detectedTargetRole").asText(null);
            if (role == null || role.isBlank() || "null".equalsIgnoreCase(role)) return;
            String gap = node.path("detectedSkillsGap").asText(null);
            if ("null".equalsIgnoreCase(gap)) gap = null;
            FormationDemand demand = new FormationDemand();
            demand.setUserId(userId);
            demand.setRequestedRole(role.trim());
            demand.setDetectedSkillsGap(gap);
            formationDemandRepository.save(demand);
            mentorNotificationService.notifyAdminsNewDemand(demand.getId(), demand.getRequestedRole());
        } catch (Exception ignored) {
            // Non-critical: never fail the ask response due to demand tracking
        }
    }

    private static String formatConversationHistory(List<ChatTurnDto> turns) {
        if (turns == null || turns.isEmpty()) {
            return "";
        }
        int maxTurns = 20;
        int from = Math.max(0, turns.size() - maxTurns);
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < turns.size(); i++) {
            ChatTurnDto t = turns.get(i);
            String roleLabel = "ASSISTANT".equalsIgnoreCase(t.role()) || "assistant".equalsIgnoreCase(t.role())
                    ? "Assistant" : "User";
            sb.append(roleLabel).append(": ").append(t.content().trim()).append("\n\n");
        }
        return sb.toString().trim();
    }

    private static MentorAdviceResponseDto toDto(MentorSession session, MentorAdvice advice, Long threadId) {
        return new MentorAdviceResponseDto(
                session.getId(),
                advice.getSummary(),
                advice.getStructuredJson(),
                advice.getRawText(),
                threadId
        );
    }
}
