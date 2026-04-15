package tn.esprit.forme.certificationservice.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.forme.certificationservice.application.client.GoogleAIClient;
import tn.esprit.forme.certificationservice.application.service.SessionReminderService;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j

public class TestController {

    private final GoogleAIClient googleAIClient;
    private final SessionReminderService sessionReminderService;
    
    @Value("${google.ai.api-key}")
    private String apiKey;

    @GetMapping("/google-ai")
    public ResponseEntity<Map<String, Object>> testGoogleAI() {
        try {
            log.info("Testing Google AI API connection");
            log.info("API Key configured: {}", apiKey != null && !apiKey.isEmpty());
            log.info("API Key length: {}", apiKey != null ? apiKey.length() : 0);
            log.info("API Key starts with: {}", apiKey != null && apiKey.length() > 10 ? apiKey.substring(0, 10) + "..." : "N/A");
            
            // Validate API key format
            if (apiKey == null || apiKey.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "status", "error",
                    "message", "API key is not configured"
                ));
            }
            
            if (!apiKey.startsWith("AIza")) {
                return ResponseEntity.ok(Map.of(
                    "status", "error",
                    "message", "API key format is invalid. Should start with 'AIza'"
                ));
            }
            
            if (apiKey.length() < 35) {
                return ResponseEntity.ok(Map.of(
                    "status", "error",
                    "message", "API key appears to be too short. Expected length > 35 characters, got: " + apiKey.length()
                ));
            }
            
            String result = googleAIClient.generateLinkedInPost("Test prompt: Say hello in one sentence.");
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Google AI API is working",
                "result", result
            ));
        } catch (Exception e) {
            log.error("Google AI API test failed", e);
            return ResponseEntity.ok(Map.of(
                "status", "error",
                "message", e.getMessage(),
                "error", e.getClass().getSimpleName()
            ));
        }
    }

    @GetMapping("/session-reminders")
    public ResponseEntity<Map<String, String>> testSessionReminders() {
        try {
            log.info("Manually triggering session reminder check");
            sessionReminderService.sendSessionReminders();
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Session reminder check completed. Check logs for details."
            ));
        } catch (Exception e) {
            log.error("Session reminder test failed", e);
            return ResponseEntity.ok(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
}

