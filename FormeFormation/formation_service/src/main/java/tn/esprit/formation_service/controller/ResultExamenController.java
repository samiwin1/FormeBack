package tn.esprit.formation_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.formation_service.entity.ResultExamen;
import tn.esprit.formation_service.service.ResultExamenService;

import java.util.List;

@RestController
@RequestMapping("/api/result-examens")
@CrossOrigin(origins = "http://localhost:4200")
public class ResultExamenController {

    private final ResultExamenService resultExamenService;

    public ResultExamenController(ResultExamenService resultExamenService) {
        this.resultExamenService = resultExamenService;
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
