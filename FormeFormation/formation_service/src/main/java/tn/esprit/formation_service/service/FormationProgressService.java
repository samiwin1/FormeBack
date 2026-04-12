package tn.esprit.formation_service.service;

import tn.esprit.formation_service.dto.FormationProgressResponse;

import java.util.Optional;

public interface FormationProgressService {

    Optional<FormationProgressResponse> getFormationProgress(Long userId, Long formationId);
}
