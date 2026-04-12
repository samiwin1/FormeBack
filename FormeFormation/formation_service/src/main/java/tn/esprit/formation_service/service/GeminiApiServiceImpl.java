package tn.esprit.formation_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import tn.esprit.formation_service.dto.GlobalAnalyticsResponse;
import tn.esprit.formation_service.dto.IncorrectAnswerItem;
import tn.esprit.formation_service.exception.GeminiApiException;

import java.util.*;

@Service
public class GeminiApiServiceImpl implements GeminiApiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiApiServiceImpl.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String SEPARATOR = "---";

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String model;

    public GeminiApiServiceImpl(@Qualifier("geminiRestTemplate") RestTemplate restTemplate,
                                @Value("${gemini.api.key:}") String apiKey,
                                @Value("${gemini.api.model:gemini-1.5-flash}") String model) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public List<IncorrectAnswerItem> explainMistakes(List<IncorrectAnswerItem> items, String evaluationTitle) {
        if (items == null || items.isEmpty() || !isApiKeyConfigured()) {
            return items != null ? items : Collections.emptyList();
        }
        try {
            StringBuilder prompt = new StringBuilder();
            prompt.append("You are an educational assistant. A learner failed a quiz on their 2nd attempt.\n");
            prompt.append("For each incorrect answer below, provide a SHORT explanation (4-5 sentences max).\n");
            prompt.append("- Explain the concept\n- Be educational and concise\n- No markdown, no long lectures\n");
            prompt.append("Output format: one paragraph per item, separated by \"").append(SEPARATOR).append("\"\n\n");
            prompt.append("Evaluation: ").append(evaluationTitle).append("\n\n");

            for (IncorrectAnswerItem item : items) {
                prompt.append("Question: ").append(item.getQuestionText()).append("\n");
                prompt.append("Correct: ").append(item.getCorrectAnswer()).append("\n");
                prompt.append("User answered: ").append(item.getUserAnswer()).append("\n").append(SEPARATOR).append("\n");
            }

            String responseText = callGemini(prompt.toString(), 512);
            if (responseText == null || responseText.isBlank()) {
                return items;
            }

            String[] parts = responseText.split(SEPARATOR);
            for (int i = 0; i < items.size() && i < parts.length; i++) {
                String explanation = parts[i].trim();
                if (!explanation.isEmpty()) {
                    items.get(i).setAiExplanation(explanation);
                }
            }
            return items;
        } catch (Exception e) {
            log.warn("Gemini explainMistakes failed: {}", e.getMessage());
            return items;
        }
    }

    @Override
    public String generateFormationStructure(String title, String description, String objectives,
                                             String level, String skillsTargeted, int numberOfContentBlocks) {
        if (!isApiKeyConfigured()) {
            throw new GeminiApiException("Gemini API key is not configured. Add gemini.api.key in application.properties.");
        }
        String prompt = buildFormationPrompt(title, description, objectives, level, skillsTargeted, numberOfContentBlocks);
        String responseText = callGeminiForFormation(prompt, 8192);
        if (responseText == null || responseText.isBlank()) {
            throw new GeminiApiException("AI did not return a valid formation structure.");
        }
        return stripMarkdownJson(responseText);
    }

    @Override
    public String generateAnalyticsInsights(GlobalAnalyticsResponse analytics) {
        if (analytics == null || !isApiKeyConfigured()) {
            return null;
        }
        try {
            String json = OBJECT_MAPPER.writeValueAsString(analytics);
            String prompt = "You are a training performance consultant. Based on the following platform analytics summary, provide:\n" +
                    "1. Key insights (2-3 sentences)\n" +
                    "2. Detected risks or learning problems (2-3 sentences)\n" +
                    "3. Actionable recommendations (2-3 sentences)\n\n" +
                    "Keep the total response under 8 sentences. Professional tone. Be concise.\n\n" +
                    "Analytics summary (JSON):\n" + json;

            return callGemini(prompt, 512);
        } catch (Exception e) {
            log.warn("Gemini generateAnalyticsInsights failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Calls Gemini for formation generation. On 503 (high demand), retries with gemini-1.5-flash.
     * Throws GeminiApiException with the actual API error message on failure.
     */
    private String callGeminiForFormation(String prompt, int maxTokens) {
        try {
            return callGeminiWithModel(prompt, maxTokens, model);
        } catch (GeminiApiException e) {
            if (e.getMessage() != null && e.getMessage().contains("high demand") && !"gemini-1.5-flash".equals(model)) {
                log.info("Primary model {} overloaded, retrying with gemini-1.5-flash", model);
                return callGeminiWithModel(prompt, maxTokens, "gemini-1.5-flash");
            }
            throw e;
        }
    }

    private String callGemini(String prompt, int maxTokens) {
        try {
            return callGeminiWithModel(prompt, maxTokens, model);
        } catch (GeminiApiException e) {
            log.warn("Gemini API call failed: {}", e.getMessage());
            return null;
        }
    }

    private String callGeminiWithModel(String prompt, int maxTokens, String modelToUse) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + modelToUse + ":generateContent?key=" + apiKey;

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));
        requestBody.put("generationConfig", Map.of(
                "maxOutputTokens", maxTokens,
                "temperature", 0.4
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers),
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = OBJECT_MAPPER.readTree(response.getBody());
                JsonNode candidates = root.get("candidates");
                if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                    JsonNode content = candidates.get(0).get("content");
                    if (content != null) {
                        JsonNode parts = content.get("parts");
                        if (parts != null && parts.isArray() && parts.size() > 0) {
                            JsonNode textNode = parts.get(0).get("text");
                            if (textNode != null) {
                                return textNode.asText();
                            }
                        }
                    }
                }
            }
            throw new GeminiApiException("AI did not return a valid response.");
        } catch (HttpStatusCodeException e) {
            String message = extractErrorMessage(e.getResponseBodyAsString());
            if (message == null) {
                message = e.getStatusCode() + ": " + e.getStatusText();
            }
            log.warn("Gemini API call failed: {}", message);
            throw new GeminiApiException(message, e);
        } catch (GeminiApiException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Gemini API call failed: {}", e.getMessage());
            throw new GeminiApiException("Gemini API error: " + e.getMessage(), e);
        }
    }

    private String extractErrorMessage(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) return null;
        try {
            JsonNode root = OBJECT_MAPPER.readTree(responseBody);
            JsonNode error = root.get("error");
            if (error != null) {
                JsonNode msg = error.get("message");
                if (msg != null) return msg.asText();
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String buildFormationPrompt(String title, String description, String objectives,
                                        String level, String skillsTargeted, int numBlocks) {
        return "You are a course designer. Generate a complete formation structure as JSON.\n" +
                "Input: title=" + (title != null ? title : "") + ", description=" + (description != null ? description : "") +
                ", objectives=" + (objectives != null ? objectives : "") + ", level=" + (level != null ? level : "") +
                ", skills=" + (skillsTargeted != null ? skillsTargeted : "") + ", numBlocks=" + numBlocks + "\n\n" +
                "Return ONLY valid JSON in this exact format (no markdown, no code blocks):\n" +
                "{\n" +
                "  \"formation\": {\"title\":\"...\",\"description\":\"...\",\"objectives\":\"...\",\"level\":\"...\",\"skills_targeted\":\"...\"},\n" +
                "  \"contents\": [\n" +
                "    {\"title\":\"...\",\"content_type\":\"text\",\"content_body\":\"...\",\"order_index\":0},\n" +
                "    {\"title\":\"...\",\"content_type\":\"text\",\"content_body\":\"...\",\"order_index\":1}\n" +
                "  ],\n" +
                "  \"evaluations\": [\n" +
                "    {\"title\":\"...\",\"content\":\"{\\\"questions\\\":[{\\\"text\\\":\"...\",\\\"options\\\":[\\\"A\\\",\\\"B\\\",\\\"C\\\"],\\\"correctIndex\\\":0}]}\",\"passing_score\":75,\"max_attempts\":3}\n" +
                "  ],\n" +
                "  \"exam\": {\"title\":\"...\",\"duration_minutes\":30,\"passing_score\":75,\"content\":\"{\\\"questions\\\":[...]}\"}\n" +
                "}\n\n" +
                "- Create " + numBlocks + " content blocks\n" +
                "- Create one evaluation (quiz) per content block with 3-5 questions each\n" +
                "- Create one final exam with 5-10 questions\n" +
                "- Use valid JSON for evaluation and exam content (questions array with text, options, correctIndex)";
    }

    private String stripMarkdownJson(String text) {
        if (text == null) return null;
        String s = text.trim();
        if (s.startsWith("```json")) {
            s = s.substring(7);
        } else if (s.startsWith("```")) {
            s = s.substring(3);
        }
        if (s.endsWith("```")) {
            s = s.substring(0, s.length() - 3);
        }
        return s.trim();
    }

    private boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.isBlank() && !"YOUR_GEMINI_API_KEY_HERE".equals(apiKey);
    }
}
