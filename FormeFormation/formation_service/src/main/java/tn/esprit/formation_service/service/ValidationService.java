package tn.esprit.formation_service.service;

public interface ValidationService {

    /**
     * Validates if score meets passing threshold.
     */
    boolean validateEvaluation(int score, int passingScore);

    /**
     * Checks if user can start the exam for the formation.
     * Rule 1: All evaluations must be passed.
     * Rule 2: No required content remains locked.
     */
    boolean canStartExam(Long userId, Long formationId);

    /**
     * Computes formation completion status dynamically.
     * Formation is completed if exam passed AND all evaluations passed.
     */
    boolean isFormationCompleted(Long userId, Long formationId);
}
