package tn.esprit.formation_service.service;

import tn.esprit.formation_service.dto.GenerateFormationRequest;
import tn.esprit.formation_service.entity.Formation;

/**
 * Generates formations using AI and persists them via existing CRUD services.
 */
public interface FormationGeneratorService {

    /**
     * Generate a complete formation (contents, evaluations, exam) from AI and save to database.
     *
     * @param request generation parameters
     * @return created Formation or null if AI failed
     */
    Formation generateAndSave(GenerateFormationRequest request);
}
