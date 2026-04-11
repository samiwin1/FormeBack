package tn.esprit.mentorservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.mentorservice.common.ApiException;
import tn.esprit.mentorservice.domain.MessageRole;
import tn.esprit.mentorservice.dto.ChatTurnDto;
import tn.esprit.mentorservice.dto.CreateThreadRequest;
import tn.esprit.mentorservice.dto.ThreadDetailDto;
import tn.esprit.mentorservice.dto.ThreadMessageDto;
import tn.esprit.mentorservice.dto.ThreadSummaryDto;
import tn.esprit.mentorservice.entity.ConversationMessage;
import tn.esprit.mentorservice.entity.ConversationThread;
import tn.esprit.mentorservice.repository.ConversationMessageRepository;
import tn.esprit.mentorservice.repository.ConversationThreadRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private static final int MAX_HISTORY_TURNS = 20;
    private static final String DEFAULT_TITLE = "New Conversation";

    private final ConversationThreadRepository threadRepository;
    private final ConversationMessageRepository messageRepository;

    @Transactional(readOnly = true)
    public List<ThreadSummaryDto> getUserThreads(Long userId) {
        return threadRepository.findByUserIdAndIsActiveTrueOrderByUpdatedAtDesc(userId)
                .stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ThreadDetailDto getThread(Long userId, Long threadId) {
        ConversationThread thread = requireThread(userId, threadId);
        List<ConversationMessage> messages = messageRepository.findByThreadIdOrderByCreatedAtAsc(threadId);
        // Limit to last 100 messages for the detail view
        if (messages.size() > 100) {
            messages = messages.subList(messages.size() - 100, messages.size());
        }
        return toDetail(thread, messages);
    }

    @Transactional
    public ThreadDetailDto createThread(Long userId, CreateThreadRequest request) {
        String title = (request.title() != null && !request.title().isBlank())
                ? request.title().trim()
                : DEFAULT_TITLE;
        ConversationThread thread = ConversationThread.builder()
                .userId(userId)
                .title(title)
                .formationId(request.formationId())
                .build();
        thread = threadRepository.save(thread);
        return toDetail(thread, List.of());
    }

    @Transactional
    public void archiveThread(Long userId, Long threadId) {
        ConversationThread thread = requireThread(userId, threadId);
        thread.setActive(false);
        threadRepository.save(thread);
    }

    /** Adds a USER message to the thread and bumps updatedAt. */
    @Transactional
    public ThreadMessageDto addUserMessage(Long threadId, String content) {
        ConversationThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new ApiException(404, "Thread not found"));
        ConversationMessage msg = ConversationMessage.builder()
                .thread(thread)
                .role(MessageRole.USER)
                .content(content)
                .build();
        msg = messageRepository.save(msg);
        thread.setUpdatedAt(LocalDateTime.now());
        threadRepository.save(thread);
        return toMessageDto(msg);
    }

    /** Adds an ASSISTANT message to the thread, linking it to the mentor session. */
    @Transactional
    public ThreadMessageDto addAssistantMessage(Long threadId, String content, Long sessionId) {
        ConversationThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new ApiException(404, "Thread not found"));
        ConversationMessage msg = ConversationMessage.builder()
                .thread(thread)
                .role(MessageRole.ASSISTANT)
                .content(content)
                .sessionId(sessionId)
                .build();
        msg = messageRepository.save(msg);
        thread.setUpdatedAt(LocalDateTime.now());
        threadRepository.save(thread);
        return toMessageDto(msg);
    }

    /**
     * Auto-sets the thread title from the first user message if still "New Conversation".
     * Truncates to 50 chars at a word boundary.
     */
    @Transactional
    public void autoTitleIfNeeded(Long threadId, String firstQuestion) {
        ConversationThread thread = threadRepository.findById(threadId).orElse(null);
        if (thread == null || !DEFAULT_TITLE.equals(thread.getTitle())) {
            return;
        }
        String title = truncateAtWordBoundary(firstQuestion, 50);
        thread.setTitle(title);
        threadRepository.save(thread);
    }

    /**
     * Returns the last N messages of a thread as ChatTurnDto list, ready for the AI prompt.
     */
    @Transactional(readOnly = true)
    public List<ChatTurnDto> getHistoryForPrompt(Long threadId) {
        List<ConversationMessage> messages = messageRepository.findByThreadIdOrderByCreatedAtAsc(threadId);
        if (messages.size() > MAX_HISTORY_TURNS) {
            messages = messages.subList(messages.size() - MAX_HISTORY_TURNS, messages.size());
        }
        return messages.stream()
                .map(m -> new ChatTurnDto(m.getRole().name(), m.getContent()))
                .collect(Collectors.toList());
    }

    /** Loads the thread and verifies ownership, throwing 403/404 on failure. */
    public ConversationThread requireThread(Long userId, Long threadId) {
        return threadRepository.findByIdAndUserId(threadId, userId)
                .orElseThrow(() -> new ApiException(404, "Thread not found or does not belong to you"));
    }

    // ── Mapping helpers ───────���───────────────────────────────────────────

    private ThreadSummaryDto toSummary(ConversationThread t) {
        long count = messageRepository.countByThreadId(t.getId());
        List<ConversationMessage> last = messageRepository.findByThreadIdOrderByCreatedAtAsc(t.getId());
        String preview = last.isEmpty() ? "" :
                truncateAtWordBoundary(last.get(last.size() - 1).getContent(), 80);
        return new ThreadSummaryDto(t.getId(), t.getTitle(), t.getFormationId(), t.getUpdatedAt(), count, preview);
    }

    private ThreadDetailDto toDetail(ConversationThread t, List<ConversationMessage> messages) {
        List<ThreadMessageDto> dtos = messages.stream().map(this::toMessageDto).collect(Collectors.toList());
        return new ThreadDetailDto(t.getId(), t.getTitle(), t.getFormationId(), dtos);
    }

    private ThreadMessageDto toMessageDto(ConversationMessage m) {
        return new ThreadMessageDto(m.getId(), m.getRole().name(), m.getContent(), m.getSessionId(), m.getCreatedAt());
    }

    private static String truncateAtWordBoundary(String text, int maxLen) {
        if (text == null) return "";
        String s = text.trim();
        if (s.length() <= maxLen) return s;
        int cut = s.lastIndexOf(' ', maxLen);
        if (cut <= 0) cut = maxLen;
        return s.substring(0, cut) + "…";
    }
}
