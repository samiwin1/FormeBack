package tn.esprit.mentorservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
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

    public GoogleAIClient(@Qualifier("vanillaWebClientBuilder") WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    public String generateText(String prompt, int maxOutputTokens, double temperature) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Google AI API key is not configured (google.ai.api-key).");
        }

        List<String> modelCandidates = modelCandidates();
        Exception lastException = null;

        for (String candidateModel : modelCandidates) {
            try {
                Map<String, Object> response = callGenerateContent(candidateModel, prompt, maxOutputTokens, temperature);
                return extractText(response);
            } catch (WebClientResponseException.NotFound notFound) {
                lastException = notFound;
                log.warn("Model {} not found, trying next fallback.", candidateModel);
            } catch (Exception e) {
                lastException = e;
                log.error("Google AI call failed for model {}: {}", candidateModel, e.getMessage());
            }
        }

        String tried = String.join(", ", modelCandidates);
        String suffix = lastException != null ? " Last error: " + lastException.getMessage() : "";
        throw new IllegalStateException("AI generation failed. Tried models: " + tried + "." + suffix);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> callGenerateContent(
            String modelName,
            String prompt,
            int maxOutputTokens,
            double temperature
    ) {
        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                ),
                "generationConfig", Map.of(
                        "temperature", temperature,
                        "topK", 40,
                        "topP", 0.95,
                        "maxOutputTokens", maxOutputTokens
                )
        );

        return webClient.post()
                .uri(buildGenerateContentUrl(modelName) + "?key=" + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .doOnNext(err -> log.error("Google AI error body: {}", err))
                                .then(clientResponse.createException()))
                .bodyToMono(Map.class)
                .block(Duration.ofSeconds(60));
    }

    @SuppressWarnings("unchecked")
    private String extractText(Map<String, Object> response) {
        if (response == null) {
            throw new IllegalStateException("Empty response from Google AI");
        }
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
        if (candidates == null || candidates.isEmpty()) {
            throw new IllegalStateException("Google AI response has no candidates");
        }
        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        if (content == null) {
            throw new IllegalStateException("Google AI response has no content");
        }
        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
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
