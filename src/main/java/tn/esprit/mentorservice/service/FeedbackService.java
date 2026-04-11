package tn.esprit.mentorservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.mentorservice.common.ApiException;
import tn.esprit.mentorservice.domain.FeedbackRating;
import tn.esprit.mentorservice.dto.FeedbackRequest;
import tn.esprit.mentorservice.dto.FeedbackResponseDto;
import tn.esprit.mentorservice.entity.MentorFeedback;
import tn.esprit.mentorservice.entity.MentorSession;
import tn.esprit.mentorservice.repository.MentorFeedbackRepository;
import tn.esprit.mentorservice.repository.MentorSessionRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final MentorFeedbackRepository feedbackRepository;
    private final MentorSessionRepository sessionRepository;

    @Transactional
    public FeedbackResponseDto submitFeedback(Long userId, FeedbackRequest request) {
        // Verify the session belongs to this user
        MentorSession session = sessionRepository.findById(request.sessionId())
                .filter(s -> s.getUserId().equals(userId))
                .orElseThrow(() -> new ApiException(404, "Session not found or does not belong to you"));

        // Upsert: update if already exists, create otherwise
        MentorFeedback feedback = feedbackRepository
                .findBySessionIdAndUserId(session.getId(), userId)
                .orElseGet(() -> MentorFeedback.builder()
                        .sessionId(session.getId())
                        .userId(userId)
                        .build());

        feedback.setRating(request.rating());
        feedback.setComment(request.comment());
        feedback = feedbackRepository.save(feedback);
        return toDto(feedback);
    }

    @Transactional(readOnly = true)
    public List<FeedbackResponseDto> getUserFeedbackHistory(Long userId) {
        return feedbackRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /** Returns a plain-text summary of recent negative feedback for AI prompt injection. */
    @Transactional(readOnly = true)
    public String buildFeedbackContextForPrompt(Long userId) {
        long upCount = feedbackRepository.countByUserIdAndRating(userId, FeedbackRating.UP);
        long downCount = feedbackRepository.countByUserIdAndRating(userId, FeedbackRating.DOWN);
        if (upCount == 0 && downCount == 0) {
            return null;
        }
        List<MentorFeedback> recentDown = feedbackRepository
                .findTop3ByUserIdAndRatingOrderByCreatedAtDesc(userId, FeedbackRating.DOWN);

        StringBuilder sb = new StringBuilder();
        sb.append("## Learner Feedback History\n");
        sb.append("The learner has rated ").append(upCount).append(" response(s) as helpful and ")
          .append(downCount).append(" as unhelpful.\n");
        if (!recentDown.isEmpty()) {
            sb.append("Recent negative feedback (adjust your style accordingly):\n");
            for (MentorFeedback f : recentDown) {
                if (f.getComment() != null && !f.getComment().isBlank()) {
                    sb.append("- \"").append(f.getComment().trim()).append("\" (rated DOWN)\n");
                } else {
                    sb.append("- (no comment, session #").append(f.getSessionId()).append(", rated DOWN)\n");
                }
            }
            sb.append("If feedback suggests responses were too generic, be more specific. ")
              .append("If too long, be more concise.\n");
        }
        return sb.toString();
    }

    private FeedbackResponseDto toDto(MentorFeedback f) {
        return new FeedbackResponseDto(f.getId(), f.getSessionId(), f.getRating(), f.getComment(), f.getCreatedAt());
    }
}
