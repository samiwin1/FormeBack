package com.example.controller;

import com.example.entity.Feedback;
import com.example.service.FeedbackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feedbacks")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @GetMapping
    public ResponseEntity<List<Feedback>> getAllFeedbacks() {
        return ResponseEntity.ok(feedbackService.getAllFeedbacks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Feedback> getFeedbackById(@PathVariable Long id) {
        return feedbackService.getFeedbackById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/partner/{partnerId}")
    public ResponseEntity<List<Feedback>> getFeedbacksByPartner(@PathVariable Long partnerId) {
        return ResponseEntity.ok(feedbackService.getFeedbacksByPartner(partnerId));
    }

    @GetMapping("/pack/{packId}")
    public ResponseEntity<List<Feedback>> getFeedbacksByPack(@PathVariable Long packId) {
        return ResponseEntity.ok(feedbackService.getFeedbacksByPack(packId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Feedback>> getFeedbacksByStatus(@PathVariable String status) {
        return ResponseEntity.ok(feedbackService.getFeedbacksByStatus(status));
    }

    @GetMapping("/rating/{rating}")
    public ResponseEntity<List<Feedback>> getFeedbacksByRating(@PathVariable Integer rating) {
        return ResponseEntity.ok(feedbackService.getFeedbacksByRating(rating));
    }

    @GetMapping("/sentiment/{sentiment}")
    public ResponseEntity<List<Feedback>> getFeedbacksBySentiment(@PathVariable String sentiment) {
        return ResponseEntity.ok(feedbackService.getFeedbacksBySentiment(sentiment));
    }

    @PostMapping
    public ResponseEntity<Feedback> createFeedback(@RequestBody Feedback feedback) {
        return ResponseEntity.status(HttpStatus.CREATED).body(feedbackService.createFeedback(feedback));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Feedback> updateFeedback(@PathVariable Long id, @RequestBody Feedback feedback) {
        try {
            return ResponseEntity.ok(feedbackService.updateFeedback(id, feedback));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeedback(@PathVariable Long id) {
        feedbackService.deleteFeedback(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<Feedback> approveFeedback(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(feedbackService.approveFeedback(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<Feedback> rejectFeedback(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(feedbackService.rejectFeedback(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(feedbackService.getStats());
    }

    @GetMapping("/stats/pack/{packId}")
    public ResponseEntity<Map<String, Object>> getStatsByPack(@PathVariable Long packId) {
        return ResponseEntity.ok(feedbackService.getStatsByPack(packId));
    }

    @GetMapping("/stats/partner/{partnerId}")
    public ResponseEntity<Map<String, Object>> getStatsByPartner(@PathVariable Long partnerId) {
        return ResponseEntity.ok(feedbackService.getStatsByPartner(partnerId));
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Feedbacks OK");
    }
}
