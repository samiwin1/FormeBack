package tn.esprit.formation_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.formation_service.dto.ExamHistoryItem;
import tn.esprit.formation_service.entity.Examen;
import tn.esprit.formation_service.entity.ResultExamen;
import tn.esprit.formation_service.service.ExamEngineService;
import tn.esprit.formation_service.service.ExamenService;

import java.util.List;

@RestController
@RequestMapping("/api/examens")
public class ExamenController {

    private final ExamenService examenService;
    private final ExamEngineService examEngineService;

    public ExamenController(ExamenService examenService, ExamEngineService examEngineService) {
        this.examenService = examenService;
        this.examEngineService = examEngineService;
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<ResultExamen> startExam(@PathVariable Long id, @RequestParam Long userId) {
        ResultExamen result = examEngineService.startExam(id, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/history")
    public ResponseEntity<List<ExamHistoryItem>> getHistory(
            @RequestParam Long userId,
            @RequestParam(required = false) Long formationId) {
        return ResponseEntity.ok(examenService.getExamHistory(userId, formationId));
    }

    @PostMapping
    public ResponseEntity<Examen> create(@RequestBody Examen examen) {
        Examen saved = examenService.save(examen);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Examen> getById(@PathVariable Long id) {
        return examenService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/formation/{formationId}")
    public ResponseEntity<Examen> getByFormationId(@PathVariable Long formationId) {
        return examenService.findByFormationId(formationId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Examen>> getAll() {
        return ResponseEntity.ok(examenService.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Examen> update(@PathVariable Long id, @RequestBody Examen examen) {
        return examenService.findById(id)
                .map(existing -> ResponseEntity.ok(examenService.update(id, examen)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!examenService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        examenService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
