package tn.esprit.formation_service.service;

public interface ContentLockService {

    /**
     * Unlocks the next content block (order_index + 1) after evaluation is passed.
     */
    void unlockNextContent(Long evaluationId, Long formationId);

    /**
     * Locks all content blocks with order_index greater than the block containing this evaluation.
     * Used when evaluation fails (rollback).
     */
    void rollbackContent(Long evaluationId, Long formationId);

    /**
     * Validates if user can access the content (unlocked and evaluation passed if applicable).
     */
    boolean validateContentAccess(Long contenuId, Long userId);

    /**
     * Resets lock state for formation: first block unlocked, all others locked.
     */
    void resetLocksForFormation(Long formationId);
}
