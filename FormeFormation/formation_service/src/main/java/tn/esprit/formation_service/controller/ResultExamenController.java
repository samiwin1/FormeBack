package tn.esprit.formation_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.formation_service.dto.SaveAnswerRequest;
import tn.esprit.formation_service.entity.ResultExamen;
import tn.esprit.formation_service.service.ExamEngineService;
import tn.esprit.formation_service.service.ResultExamenService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/result-examens")
public class ResultExamenController {

    private final ResultExamenService resultExamenService;
    private final ExamEngineService examEngineService;

    public ResultExamenController(ResultExamenService resultExamenService, ExamEngineService examEngineService) {
        this.resultExamenService = resultExamenService;
        this.examEngineService = examEngineService;
    }

    @PostMapping("/{id}/save-answer")
    public ResponseEntity<Void> saveAnswer(@PathVariable Long id, @RequestBody SaveAnswerRequest request) {
        Map<String, Integer> answers = request.getAnswers() != null ? request.getAnswers() : Map.of();
        examEngineService.saveAnswer(id, answers);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<ResultExamen> submitExam(@PathVariable Long id) {
        ResultExamen result = examEngineService.submitExam(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/remaining-time")
    public ResponseEntity<Map<String, Integer>> getRemainingTime(@PathVariable Long id) {
        int remaining = examEngineService.getRemainingSeconds(id);
        return ResponseEntity.ok(Map.of("remainingSeconds", remaining));
    }

    @PostMapping
    public ResponseEntity<ResultExamen> create(@RequestBody ResultExamen resultExamen) {
        ResultExamen saved = resultExamenService.save(resultExamen);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResultExamen> getById(@PathVariable Long id) {
        return resultExamenService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<ResultExamen>> getAll() {
        return ResponseEntity.ok(resultExamenService.findAll());
    }

    @GetMapping("/examen/{examenId}")
    public ResponseEntity<List<ResultExamen>> getByExamenId(@PathVariable Long examenId) {
        return ResponseEntity.ok(resultExamenService.findByExamenId(examenId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ResultExamen>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(resultExamenService.findByUser_id(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResultExamen> update(@PathVariable Long id, @RequestBody ResultExamen resultExamen) {
        return resultExamenService.findById(id)
                .map(existing -> ResponseEntity.ok(resultExamenService.update(id, resultExamen)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!resultExamenService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        resultExamenService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
