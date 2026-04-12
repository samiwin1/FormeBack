package tn.esprit.formation_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.formation_service.dto.GenerateFormationRequest;
import tn.esprit.formation_service.entity.Formation;
import tn.esprit.formation_service.exception.GeminiApiException;
import tn.esprit.formation_service.service.FormationGeneratorService;

@RestController
@RequestMapping("/api/formations/admin")
public class FormationGeneratorController {

    private final FormationGeneratorService formationGeneratorService;

    public FormationGeneratorController(FormationGeneratorService formationGeneratorService) {
        this.formationGeneratorService = formationGeneratorService;
    }

    @PostMapping("/generate-with-ai")
    public ResponseEntity<?> generateWithAi(@RequestBody GenerateFormationRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", "title is required"));
        }
        try {
            Formation formation = formationGeneratorService.generateAndSave(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(formation);
        } catch (GeminiApiException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(java.util.Map.of("message", e.getMessage()));
        }
    }
}
