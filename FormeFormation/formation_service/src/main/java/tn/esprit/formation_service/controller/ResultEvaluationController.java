package tn.esprit.formation_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.formation_service.entity.ResultEvaluation;
import tn.esprit.formation_service.service.ResultEvaluationService;

import java.util.List;

@RestController
@RequestMapping("/api/result-evaluations")
public class ResultEvaluationController {

    private final ResultEvaluationService resultEvaluationService;

    public ResultEvaluationController(ResultEvaluationService resultEvaluationService) {
        this.resultEvaluationService = resultEvaluationService;
    }

    @PostMapping
    public ResponseEntity<ResultEvaluation> create(@RequestBody ResultEvaluation resultEvaluation) {
        ResultEvaluation saved = resultEvaluationService.save(resultEvaluation);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResultEvaluation> getById(@PathVariable Long id) {
        return resultEvaluationService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<ResultEvaluation>> getAll() {
        return ResponseEntity.ok(resultEvaluationService.findAll());
    }

    @GetMapping("/evaluation/{evaluationId}")
    public ResponseEntity<List<ResultEvaluation>> getByEvaluationId(@PathVariable Long evaluationId) {
        return ResponseEntity.ok(resultEvaluationService.findByEvaluationId(evaluationId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ResultEvaluation>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(resultEvaluationService.findByUser_id(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResultEvaluation> update(@PathVariable Long id, @RequestBody ResultEvaluation resultEvaluation) {
        return resultEvaluationService.findById(id)
                .map(existing -> ResponseEntity.ok(resultEvaluationService.update(id, resultEvaluation)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!resultEvaluationService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        resultEvaluationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
