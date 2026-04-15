package tn.esprit.events_service.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tn.esprit.events_service.exception.BusinessException;
import tn.esprit.events_service.exception.ForbiddenException;
import tn.esprit.events_service.exception.GeminiApiException;
import tn.esprit.events_service.exception.ResourceNotFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> notFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(ex.getMessage()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> badRequest(BusinessException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(ex.getMessage()));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, Object>> forbidden(ForbiddenException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body(ex.getMessage()));
    }

    @ExceptionHandler(GeminiApiException.class)
    public ResponseEntity<Map<String, Object>> gemini(GeminiApiException ex) {
        String raw = ex.getMessage() != null ? ex.getMessage() : "AI request failed";
        HttpStatus status = geminiHttpStatus(raw);
        String message = geminiClientMessage(raw, status);
        return ResponseEntity.status(status).body(body(message));
    }

    /** Gemini quota/rate limits are 429 upstream; we must not map those to 502 (misleading). */
    private static HttpStatus geminiHttpStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return HttpStatus.BAD_GATEWAY;
        }
        String u = raw.toUpperCase();
        if (u.contains("RESOURCE_EXHAUSTED")
                || u.contains("RESOURCE_EXHAUSTED (429)")
                || u.contains("QUOTA")
                || u.contains("RATE_LIMIT")
                || u.contains("TOO_MANY_REQUESTS")) {
            return HttpStatus.TOO_MANY_REQUESTS;
        }
        if (u.contains("PERMISSION_DENIED")
                || u.contains("API_KEY")
                || u.contains("API KEY")) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }
        return HttpStatus.BAD_GATEWAY;
    }

    private static String geminiClientMessage(String raw, HttpStatus status) {
        if (status == HttpStatus.TOO_MANY_REQUESTS) {
            return "Gemini quota or rate limit reached (free tier may be exhausted or billing not enabled for this model). "
                    + "Wait a minute and retry, or check usage limits in Google AI Studio / https://ai.google.dev/gemini-api/docs/rate-limits";
        }
        if (status == HttpStatus.SERVICE_UNAVAILABLE) {
            return "Gemini API is not available for this key (check GEMINI_API_KEY and project permissions).";
        }
        if (raw != null && raw.length() > 800) {
            return raw.substring(0, 797) + "...";
        }
        return raw;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> other(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body(ex.getMessage() != null ? ex.getMessage() : "Internal error"));
    }

    private static Map<String, Object> body(String message) {
        Map<String, Object> m = new HashMap<>();
        m.put("error", message);
        m.put("message", message);
        return m;
    }
}
