package tn.esprit.formation_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.formation_service.entity.ContenuFormation;
import tn.esprit.formation_service.service.ContenuFormationService;

import java.util.List;

@RestController
@RequestMapping("/api/contenus-formation")
public class ContenuFormationController {

    private final ContenuFormationService contenuFormationService;

    public ContenuFormationController(ContenuFormationService contenuFormationService) {
        this.contenuFormationService = contenuFormationService;
    }

    @PostMapping
    public ResponseEntity<ContenuFormation> create(@RequestBody ContenuFormation contenuFormation) {
        ContenuFormation saved = contenuFormationService.save(contenuFormation);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContenuFormation> getById(@PathVariable Long id) {
        return contenuFormationService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<ContenuFormation>> getAll() {
        return ResponseEntity.ok(contenuFormationService.findAll());
    }

    @GetMapping("/formation/{formationId}")
    public ResponseEntity<List<ContenuFormation>> getByFormationId(@PathVariable Long formationId) {
        return ResponseEntity.ok(contenuFormationService.findByFormationId(formationId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContenuFormation> update(@PathVariable Long id, @RequestBody ContenuFormation contenuFormation) {
        return contenuFormationService.findById(id)
                .map(existing -> ResponseEntity.ok(contenuFormationService.update(id, contenuFormation)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!contenuFormationService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        contenuFormationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
