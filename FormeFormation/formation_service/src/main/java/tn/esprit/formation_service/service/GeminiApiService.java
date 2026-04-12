package tn.esprit.formation_service.service;

import tn.esprit.formation_service.dto.GlobalAnalyticsResponse;
import tn.esprit.formation_service.dto.IncorrectAnswerItem;

import java.util.List;

/**
 * Service for communicating with Google Gemini API.
 * All AI calls are server-side only; frontend never calls Gemini directly.
 */
public interface GeminiApiService {

    /**
     * Get short educational explanations for incorrect quiz answers.
     * Triggered when user fails evaluation on 2nd attempt.
     *
     * @param items         incorrect answer items (question, correct, user answer)
     * @param evaluationTitle evaluation title for context
     * @return list of items with aiExplanation populated, or empty list on failure
     */
    List<IncorrectAnswerItem> explainMistakes(List<IncorrectAnswerItem> items, String evaluationTitle);

    /**
     * Generate formation structure as JSON string.
     * Used by admin AI formation generator.
     *
     * @param title                formation title
     * @param description          formation description
     * @param objectives           learning objectives
     * @param level                difficulty level
     * @param skillsTargeted       skills targeted
     * @param numberOfContentBlocks number of content blocks to generate
     * @return raw JSON string or null on failure
     */
    String generateFormationStructure(String title, String description, String objectives,
                                     String level, String skillsTargeted, int numberOfContentBlocks);

    /**
     * Generate analytics insights from pre-computed platform statistics.
     *
     * @param analytics GlobalAnalyticsResponse summary
     * @return insight text or null on failure
     */
    String generateAnalyticsInsights(GlobalAnalyticsResponse analytics);
}
