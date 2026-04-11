package com.example.service;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AiService {

    // ── Mots-clés pour l'analyse de sentiment ────────────────────────────────

    private static final List<String> NEGATIVE_KEYWORDS = List.of(
        "bad", "poor", "terrible", "awful", "horrible", "slow", "worst",
        "disappointed", "disappointing", "useless", "broken", "failed",
        "problem", "issue", "error", "wrong", "never", "not good",
        "mauvais", "nul", "terrible", "lent", "décevant", "problème",
        "horrible", "inutile", "défaillant", "médiocre", "inacceptable"
    );

    private static final List<String> POSITIVE_KEYWORDS = List.of(
        "good", "great", "excellent", "amazing", "wonderful", "fantastic",
        "outstanding", "perfect", "love", "best", "helpful", "efficient",
        "satisfied", "happy", "impressed", "recommend", "awesome",
        "bon", "excellent", "super", "parfait", "génial", "satisfait",
        "impressionnant", "recommande", "formidable", "efficace", "bravo"
    );

    // ── Feature IA 1 : Analyse de sentiment ──────────────────────────────────

    public String analyzeSentiment(String comment) {
        if (comment == null || comment.isBlank()) return "NEUTRAL";

        String lowerComment = comment.toLowerCase();

        long negativeCount = NEGATIVE_KEYWORDS.stream()
                .filter(lowerComment::contains)
                .count();

        long positiveCount = POSITIVE_KEYWORDS.stream()
                .filter(lowerComment::contains)
                .count();

        if (negativeCount > positiveCount) return "NEGATIVE";
        if (positiveCount > negativeCount) return "POSITIVE";

        // Si pas de mots-clés, utiliser le rating comme indice
        return "NEUTRAL";
    }

    // ── Feature IA 2 : Suggestion de réponse automatique ─────────────────────

    public String suggestResponse(String title, String comment,
                                   Integer rating, String category) {

        // Réponses basées sur le rating et la catégorie
        if (rating != null && rating <= 2) {
            return suggestNegativeResponse(category);
        } else if (rating != null && rating == 3) {
            return suggestNeutralResponse(category);
        } else {
            return suggestPositiveResponse(category);
        }
    }

    private String suggestNegativeResponse(String category) {
        return switch (category != null ? category : "OVERALL") {
            case "SUPPORT" -> "Dear Partner, thank you for bringing this to our attention. " +
                "We sincerely apologize for the poor support experience you encountered. " +
                "We are actively reviewing our support processes to ensure faster response times. " +
                "A dedicated support manager will contact you within 24 hours to address your concerns personally. " +
                "We value your partnership and are committed to making this right.";

            case "CONTENT" -> "Dear Partner, we appreciate your honest feedback regarding our training content. " +
                "We understand that outdated or irrelevant content can impact the learning experience significantly. " +
                "Our content team has been notified and will prioritize reviewing and updating the materials you mentioned. " +
                "We will keep you informed of the improvements made. " +
                "Thank you for helping us enhance our platform.";

            case "DELIVERY" -> "Dear Partner, thank you for your feedback on our delivery process. " +
                "We are sorry to hear that the delivery of your training materials did not meet your expectations. " +
                "We are investigating the issue and will implement corrective measures immediately. " +
                "Your satisfaction is our top priority and we appreciate your patience.";

            case "PRICE" -> "Dear Partner, we appreciate you sharing your concerns about our pricing. " +
                "We understand the importance of value for money, especially for institutional partnerships. " +
                "Our team will reach out to discuss potential adjustments or customized packages that better fit your needs. " +
                "We are committed to offering you the best possible value.";

            default -> "Dear Partner, thank you for your honest feedback. " +
                "We sincerely apologize that your experience did not meet your expectations. " +
                "We take all feedback seriously and will use your input to improve our services. " +
                "A member of our partnership team will contact you shortly to discuss how we can better serve you.";
        };
    }

    private String suggestNeutralResponse(String category) {
        return "Dear Partner, thank you for taking the time to share your feedback with us. " +
            "We appreciate your balanced assessment of our " + (category != null ? category.toLowerCase() : "services") + ". " +
            "We are always looking for ways to improve and your input is valuable in helping us do so. " +
            "We would love to hear more about what specific improvements would make your experience exceptional. " +
            "Please feel free to reach out to your dedicated account manager for any further discussion.";
    }

    private String suggestPositiveResponse(String category) {
        return "Dear Partner, thank you so much for your wonderful feedback! " +
            "We are delighted to hear that you are satisfied with our " + (category != null ? category.toLowerCase() : "services") + ". " +
            "Your positive experience motivates our entire team to continue delivering excellence. " +
            "We truly value your partnership and look forward to continuing to support your organization's training goals. " +
            "Please do not hesitate to reach out if there is anything else we can do for you.";
    }
}
