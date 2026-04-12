package tn.esprit.formation_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.formation_service.dto.EvaluationHistoryItem;
import tn.esprit.formation_service.dto.EvaluationSubmitRequest;
import tn.esprit.formation_service.dto.EvaluationSubmitResponse;
import tn.esprit.formation_service.entity.Evaluation;
import tn.esprit.formation_service.exception.MaxAttemptsExceededException;
import tn.esprit.formation_service.service.EvaluationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/evaluations")
public class EvaluationController {

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @PostMapping("/{evaluationId}/submit")
    public ResponseEntity<?> submit(@PathVariable Long evaluationId, @RequestBody EvaluationSubmitRequest request) {
        if (request.getUserId() == null) {
            return ResponseEntity.badRequest().body("userId is required");
        }
        try {
            Map<String, Integer> answers = request.getAnswers() != null ? request.getAnswers() : Map.of();
            EvaluationSubmitResponse response = evaluationService.submitEvaluation(
                    evaluationId, request.getUserId(), answers);
            return ResponseEntity.ok(response);
        } catch (MaxAttemptsExceededException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(java.util.Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<EvaluationHistoryItem>> getHistory(
            @RequestParam Long userId,
            @RequestParam(required = false) Long formationId) {
        return ResponseEntity.ok(evaluationService.getEvaluationHistory(userId, formationId));
    }

    @PostMapping
    public ResponseEntity<Evaluation> create(@RequestBody Evaluation evaluation) {
        Evaluation saved = evaluationService.save(evaluation);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Evaluation> getById(@PathVariable Long id) {
        return evaluationService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Evaluation>> getAll() {
        return ResponseEntity.ok(evaluationService.findAll());
    }

    @GetMapping("/formation/{formationId}")
    public ResponseEntity<List<Evaluation>> getByFormationId(@PathVariable Long formationId) {
        return ResponseEntity.ok(evaluationService.findByFormationId(formationId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Evaluation> update(@PathVariable Long id, @RequestBody Evaluation evaluation) {
        return evaluationService.findById(id)
                .map(existing -> ResponseEntity.ok(evaluationService.update(id, evaluation)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!evaluationService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        evaluationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
