package com.example.service;

import com.example.entity.Feedback;
import com.example.entity.FeedbackResponse;
import com.example.repository.FeedbackRepository;
import com.example.repository.FeedbackResponseRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class FeedbackResponseService {

    private final FeedbackResponseRepository responseRepository;
    private final FeedbackRepository feedbackRepository;
    private final AiService aiService;

    public FeedbackResponseService(FeedbackResponseRepository responseRepository,
                                   FeedbackRepository feedbackRepository,
                                   AiService aiService) {
        this.responseRepository = responseRepository;
        this.feedbackRepository = feedbackRepository;
        this.aiService = aiService;
    }

    public Optional<FeedbackResponse> getResponseByFeedbackId(Long feedbackId) {
        return responseRepository.findByFeedbackId(feedbackId);
    }

    public FeedbackResponse createResponseWithAiSuggestion(Long feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("Feedback not found with id: " + feedbackId));

        String suggestion = aiService.suggestResponse(
                feedback.getTitle(),
                feedback.getComment(),
                feedback.getRating(),
                feedback.getCategory()
        );

        FeedbackResponse response = new FeedbackResponse();
        response.setFeedbackId(feedbackId);
        response.setSuggestedResponse(suggestion);
        // Leave response empty until accepted or custom written
        
        return responseRepository.save(response);
    }

    public FeedbackResponse acceptSuggestion(Long feedbackId) {
        FeedbackResponse response = responseRepository.findByFeedbackId(feedbackId)
                .orElseThrow(() -> new RuntimeException("Feedback Response not found for feedback id: " + feedbackId));

        response.setResponse(response.getSuggestedResponse());
        return responseRepository.save(response);
    }

    public FeedbackResponse writeCustomResponse(Long feedbackId, String customResponse) {
        FeedbackResponse response = responseRepository.findByFeedbackId(feedbackId)
                .orElseThrow(() -> new RuntimeException("Feedback Response not found for feedback id: " + feedbackId));

        response.setResponse(customResponse);
        return responseRepository.save(response);
    }

    public void deleteResponse(Long feedbackId) {
        responseRepository.findByFeedbackId(feedbackId).ifPresent(response -> {
            responseRepository.delete(response);
        });
    }
}
