package tn.esprit.mentorservice.dto;

import java.util.List;

/**
 * Aggregated context passed to the AI prompt for learning-path generation.
 * All data fields are pre-serialised JSON strings produced by LearnerContextBuilder.
 */
public record LearningPathContext(
        String portfolioJson,
        String formationCatalogJson,
        String progressJson,
        String certificationContextJson,
        List<String> unavailableSources
) {
}
