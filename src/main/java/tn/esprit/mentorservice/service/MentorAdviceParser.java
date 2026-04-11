package tn.esprit.mentorservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MentorAdviceParser {

    private final ObjectMapper objectMapper;

    public ParsedAdvice parse(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return new ParsedAdvice("", null, null);
        }
        String cleaned = stripMarkdownFence(rawText.trim());
        try {
            JsonNode node = objectMapper.readTree(cleaned);
            String summary = textOrEmpty(node, "summary");
            if (summary.isEmpty()) {
                summary = cleaned.length() > 600 ? cleaned.substring(0, 600) + "…" : cleaned;
            }
            return new ParsedAdvice(summary, cleaned, node);
        } catch (Exception e) {
            String summary = rawText.length() > 600 ? rawText.substring(0, 600) + "…" : rawText;
            return new ParsedAdvice(summary, null, null);
        }
    }

    private static String stripMarkdownFence(String s) {
        if (!s.startsWith("```")) {
            return s;
        }
        int firstNl = s.indexOf('\n');
        int lastFence = s.lastIndexOf("```");
        if (firstNl > 0 && lastFence > firstNl) {
            return s.substring(firstNl + 1, lastFence).trim();
        }
        return s;
    }

    private static String textOrEmpty(JsonNode node, String field) {
        JsonNode n = node.get(field);
        return n == null || n.isNull() ? "" : n.asText("").trim();
    }

    public record ParsedAdvice(String summary, String structuredJson, JsonNode rootNode) {
    }
}
