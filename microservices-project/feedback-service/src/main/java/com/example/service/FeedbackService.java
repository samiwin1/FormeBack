package com.example.service;

import com.example.entity.Feedback;
import com.example.repository.FeedbackRepository;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final AiService aiService;

    public FeedbackService(FeedbackRepository feedbackRepository, AiService aiService) {
        this.feedbackRepository = feedbackRepository;
        this.aiService = aiService;
    }

    public List<Feedback> getAllFeedbacks() {
        return feedbackRepository.findAll();
    }

    public Optional<Feedback> getFeedbackById(Long id) {
        return feedbackRepository.findById(id);
    }

    public List<Feedback> getFeedbacksByPartner(Long partnerId) {
        return feedbackRepository.findByPartnerId(partnerId);
    }

    public List<Feedback> getFeedbacksByPack(Long packId) {
        return feedbackRepository.findByPackId(packId);
    }

    public List<Feedback> getFeedbacksByStatus(String status) {
        return feedbackRepository.findByStatus(status);
    }

    public List<Feedback> getFeedbacksByRating(Integer rating) {
        return feedbackRepository.findByRating(rating);
    }

    public List<Feedback> getFeedbacksBySentiment(String sentiment) {
        return feedbackRepository.findBySentiment(sentiment);
    }

    public Feedback createFeedback(Feedback feedback) {
        String sentiment = aiService.analyzeSentiment(feedback.getComment());
        feedback.setSentiment(sentiment);
        feedback.setStatus("PENDING");
        return feedbackRepository.save(feedback);
    }

    public Feedback updateFeedback(Long id, Feedback updated) {
        return feedbackRepository.findById(id).map(feedback -> {
            feedback.setRating(updated.getRating());
            feedback.setTitle(updated.getTitle());
            feedback.setComment(updated.getComment());
            feedback.setCategory(updated.getCategory());
            return feedbackRepository.save(feedback);
        }).orElseThrow(() -> new RuntimeException("Feedback not found with id: " + id));
    }

    public void deleteFeedback(Long id) {
        if (feedbackRepository.existsById(id)) {
            feedbackRepository.deleteById(id);
        }
    }

    public Feedback approveFeedback(Long id) {
        return feedbackRepository.findById(id).map(feedback -> {
            feedback.setStatus("APPROVED");
            return feedbackRepository.save(feedback);
        }).orElseThrow(() -> new RuntimeException("Feedback not found with id: " + id));
    }

    public Feedback rejectFeedback(Long id) {
        return feedbackRepository.findById(id).map(feedback -> {
            feedback.setStatus("REJECTED");
            return feedbackRepository.save(feedback);
        }).orElseThrow(() -> new RuntimeException("Feedback not found with id: " + id));
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalFeedbacks", feedbackRepository.count());
        stats.put("pendingFeedbacks", feedbackRepository.countByStatus("PENDING"));
        stats.put("approvedFeedbacks", feedbackRepository.countByStatus("APPROVED"));
        stats.put("rejectedFeedbacks", feedbackRepository.countByStatus("REJECTED"));
        stats.put("positiveFeedbacks", feedbackRepository.countBySentiment("POSITIVE"));
        stats.put("neutralFeedbacks", feedbackRepository.countBySentiment("NEUTRAL"));
        stats.put("negativeFeedbacks", feedbackRepository.countBySentiment("NEGATIVE"));
        stats.put("globalAverageRating", feedbackRepository.getGlobalAverageRating());
        return stats;
    }

    public Map<String, Object> getStatsByPack(Long packId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("averageRating", feedbackRepository.getAverageRatingByPack(packId));
        stats.put("totalFeedbacks", feedbackRepository.findByPackId(packId).size());
        return stats;
    }

    public Map<String, Object> getStatsByPartner(Long partnerId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("averageRating", feedbackRepository.getAverageRatingByPartner(partnerId));
        stats.put("totalFeedbacks", feedbackRepository.findByPartnerId(partnerId).size());
        return stats;
    }
}
