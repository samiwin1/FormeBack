package tn.esprit.formation_service.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import tn.esprit.formation_service.dto.IncorrectAnswerItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Mirrors frontend quiz-scoring logic.
 * Content format: { "questions": [ { "text", "options", "correctIndex" } ] }
 * Answers: question index as string key -> selected option index. E.g. {"0": 1, "1": 0}
 * Returns score 0-100 or 0 if content is not structured.
 */
public final class QuizScoringUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private QuizScoringUtil() {
    }

    /**
     * Extract incorrect answers for AI explanation (attempt 2 + failed).
     * Returns list of IncorrectAnswerItem with questionText, correctAnswer, userAnswer.
     */
    public static List<IncorrectAnswerItem> extractIncorrectAnswers(String content, Map<String, Integer> answers) {
        List<IncorrectAnswerItem> result = new ArrayList<>();
        if (content == null || content.isBlank() || answers == null) {
            return result;
        }
        try {
            JsonNode root = OBJECT_MAPPER.readTree(content.trim());
            JsonNode questionsNode = root.get("questions");
            if (questionsNode == null || !questionsNode.isArray() || questionsNode.isEmpty()) {
                return result;
            }
            for (int i = 0; i < questionsNode.size(); i++) {
                JsonNode q = questionsNode.get(i);
                JsonNode correctIndexNode = q.get("correctIndex");
                if (correctIndexNode == null || !correctIndexNode.isNumber()) {
                    continue;
                }
                int expected = correctIndexNode.asInt();
                Integer selected = answers.get(String.valueOf(i));
                if (selected == null || selected == expected) {
                    continue;
                }
                String questionText = q.has("text") ? q.get("text").asText() : "Question " + (i + 1);
                JsonNode optionsNode = q.get("options");
                String correctAnswer = getOptionText(optionsNode, expected);
                String userAnswer = getOptionText(optionsNode, selected);
                result.add(new IncorrectAnswerItem(questionText, correctAnswer, userAnswer));
            }
        } catch (Exception e) {
            return result;
        }
        return result;
    }

    private static String getOptionText(JsonNode optionsNode, int index) {
        if (optionsNode == null || !optionsNode.isArray() || index < 0 || index >= optionsNode.size()) {
            return "Unknown";
        }
        JsonNode opt = optionsNode.get(index);
        return opt != null ? opt.asText() : "Unknown";
    }

    public static int scoreStructuredAnswers(String content, Map<String, Integer> answers) {
        if (content == null || content.isBlank() || answers == null) {
            return 0;
        }
        try {
            JsonNode root = OBJECT_MAPPER.readTree(content.trim());
            JsonNode questionsNode = root.get("questions");
            if (questionsNode == null || !questionsNode.isArray() || questionsNode.isEmpty()) {
                return 0;
            }
            int correct = 0;
            int total = questionsNode.size();
            for (int i = 0; i < total; i++) {
                JsonNode q = questionsNode.get(i);
                JsonNode correctIndexNode = q.get("correctIndex");
                if (correctIndexNode == null || !correctIndexNode.isNumber()) {
                    continue;
                }
                int expected = correctIndexNode.asInt();
                Integer selected = answers.get(String.valueOf(i));
                if (selected != null && selected == expected) {
                    correct++;
                }
            }
            return total > 0 ? (int) Math.round(((double) correct / total) * 100.0) : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
