package tn.esprit.forme.certificationservice.application.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Slf4j

public class GoogleAIClient {

    @Value("${google.ai.api-key}")
    private String apiKey;

    @Value("${google.ai.api-url}")
    private String apiUrl;

    @Value("${google.ai.model}")
    private String model;

    @Value("${google.ai.fallback-models:gemini-2.0-flash,gemini-1.5-flash,gemini-1.5-pro}")
    private String fallbackModels;

    private final WebClient webClient;

    public GoogleAIClient(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    @SuppressWarnings("unchecked")
    public String generateLinkedInPost(String prompt) {
        List<String> modelCandidates = modelCandidates();
        Exception lastException = null;

        for (String candidateModel : modelCandidates) {
            try {
                log.info("Calling Google AI API with model: {}", candidateModel);
                log.debug("API URL base: {}, API Key length: {}", apiUrl, apiKey != null ? apiKey.length() : 0);

                Map<String, Object> response = callGenerateContent(candidateModel, prompt);
                String result = extractText(response);

                log.info("Successfully generated LinkedIn post of {} characters using model {}", result.length(), candidateModel);
                return result;
            } catch (WebClientResponseException.NotFound notFound) {
                lastException = notFound;
                log.warn("Model {} not found. Trying next fallback model if available.", candidateModel);
            } catch (Exception e) {
                log.error("Google AI API call failed: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
                throw new RuntimeException("AI post generation temporarily unavailable: " + e.getMessage());
            }
        }

        String tried = String.join(", ", modelCandidates);
        String suffix = lastException != null ? " Last error: " + lastException.getMessage() : "";
        throw new RuntimeException("AI post generation temporarily unavailable. No valid model found among: " + tried + "." + suffix);
    }

    private Map<String, Object> callGenerateContent(String modelName, String prompt) {
        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                ),
                "generationConfig", Map.of(
                        "temperature", 0.7,
                        "topK", 40,
                        "topP", 0.95,
                        "maxOutputTokens", 500
                )
        );

        return webClient.post()
                .uri(buildGenerateContentUrl(modelName) + "?key=" + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), clientResponse -> {
                    log.error("Google AI API error - Status: {}", clientResponse.statusCode());
                    return clientResponse.bodyToMono(String.class)
                            .doOnNext(errorBody -> log.error("Google AI API error body: {}", errorBody))
                            .then(clientResponse.createException());
                })
                .bodyToMono(Map.class)
                .block(Duration.ofSeconds(30));
    }

    @SuppressWarnings("unchecked")
    private String extractText(Map<String, Object> response) {
        if (response == null) {
            throw new IllegalStateException("Empty response from Google AI API");
        }

        log.debug("Google AI API response: {}", response);

        List<Map<String, Object>> candidates =
                (List<Map<String, Object>>) response.get("candidates");
        if (candidates == null || candidates.isEmpty()) {
            throw new IllegalStateException("Google AI response has no candidates");
        }

        Map<String, Object> content =
                (Map<String, Object>) candidates.get(0).get("content");
        if (content == null) {
            throw new IllegalStateException("Google AI response has no content");
        }

        List<Map<String, Object>> parts =
                (List<Map<String, Object>>) content.get("parts");
        if (parts == null || parts.isEmpty()) {
            throw new IllegalStateException("Google AI response has no parts");
        }

        Object text = parts.get(0).get("text");
        return text == null ? "" : text.toString();
    }

    private List<String> modelCandidates() {
        Set<String> models = new LinkedHashSet<>();
        if (model != null && !model.isBlank()) {
            models.add(model.trim());
        }
        Arrays.stream(fallbackModels.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .forEach(models::add);
        return List.copyOf(models);
    }

    private String buildGenerateContentUrl(String modelName) {
        String trimmed = apiUrl == null ? "" : apiUrl.trim();
        if (trimmed.isEmpty()) {
            trimmed = "https://generativelanguage.googleapis.com/v1beta";
        }

        if (trimmed.contains("/models/") && trimmed.contains(":generateContent")) {
            return trimmed.replaceAll("/models/[^:]+:generateContent", "/models/" + modelName + ":generateContent");
        }

        String base = trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
        return base + "/models/" + modelName + ":generateContent";
    }
}
