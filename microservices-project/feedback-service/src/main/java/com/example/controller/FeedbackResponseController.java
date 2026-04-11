package com.example.controller;

import com.example.entity.FeedbackResponse;
import com.example.service.FeedbackResponseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/feedbacks")
public class FeedbackResponseController {

    private final FeedbackResponseService responseService;

    public FeedbackResponseController(FeedbackResponseService responseService) {
        this.responseService = responseService;
    }

    @GetMapping("/{feedbackId}/response")
    public ResponseEntity<FeedbackResponse> getResponseByFeedbackId(@PathVariable Long feedbackId) {
        return responseService.getResponseByFeedbackId(feedbackId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{feedbackId}/response/suggest")
    public ResponseEntity<FeedbackResponse> createResponseWithAiSuggestion(@PathVariable Long feedbackId) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(responseService.createResponseWithAiSuggestion(feedbackId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{feedbackId}/response/accept")
    public ResponseEntity<FeedbackResponse> acceptSuggestion(@PathVariable Long feedbackId) {
        try {
            return ResponseEntity.ok(responseService.acceptSuggestion(feedbackId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{feedbackId}/response/custom")
    public ResponseEntity<FeedbackResponse> writeCustomResponse(
            @PathVariable Long feedbackId,
            @RequestBody Map<String, String> body) {
        try {
            String customResponse = body.get("response");
            return ResponseEntity.ok(responseService.writeCustomResponse(feedbackId, customResponse));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{feedbackId}/response")
    public ResponseEntity<Void> deleteResponse(@PathVariable Long feedbackId) {
        responseService.deleteResponse(feedbackId);
        return ResponseEntity.noContent().build();
    }
}
