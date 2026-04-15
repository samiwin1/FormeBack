package tn.esprit.forme.certificationservice.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.forme.certificationservice.application.dto.feedback.*;
import tn.esprit.forme.certificationservice.application.service.FeedbackService;
import tn.esprit.forme.certificationservice.security.SecurityUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor

public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping("/me/feedback")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<FeedbackResponse> submitFeedback(
            @Valid @RequestBody SubmitFeedbackRequest request
    ) {
        FeedbackResponse response = feedbackService.submitFeedback(SecurityUtils.currentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me/feedback/pending")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PendingFeedbackDto> getPendingFeedback() {
        PendingFeedbackDto dto = feedbackService.hasPendingFeedback(SecurityUtils.currentUserId());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/admin/feedback/session/{sessionId}/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SessionFeedbackSummaryDto> getSessionFeedbackSummary(
            @PathVariable Long sessionId
    ) {
        return ResponseEntity.ok(feedbackService.getSessionFeedbackSummary(sessionId));
    }

    @GetMapping("/admin/feedback/evaluator/{evaluatorId}/avg-rating")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Double>> getEvaluatorAvgRating(
            @PathVariable Long evaluatorId
    ) {
        OptionalDouble avgOpt = feedbackService.getAvgEvaluatorRating(evaluatorId);
        Map<String, Double> body = new HashMap<>();
        body.put("avgRating", avgOpt.isPresent() ? avgOpt.getAsDouble() : 0.0d);
        return ResponseEntity.ok(body);
    }
}
