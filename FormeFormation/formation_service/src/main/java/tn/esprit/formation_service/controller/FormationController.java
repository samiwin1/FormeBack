package tn.esprit.formation_service.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.formation_service.dto.FormationProgressResponse;
import tn.esprit.formation_service.entity.Formation;
import tn.esprit.formation_service.service.ContentLockService;
import tn.esprit.formation_service.service.FormationProgressService;
import tn.esprit.formation_service.service.FormationSearchService;
import tn.esprit.formation_service.service.FormationService;
import tn.esprit.formation_service.service.ResultEvaluationService;
import tn.esprit.formation_service.service.ResultExamenService;

import java.util.Optional;

@RestController
@RequestMapping("/api/formations")
public class FormationController {

    private final FormationService formationService;
    private final FormationProgressService formationProgressService;
    private final FormationSearchService formationSearchService;
    private final ResultEvaluationService resultEvaluationService;
    private final ResultExamenService resultExamenService;
    private final ContentLockService contentLockService;

    public FormationController(
            FormationService formationService,
            FormationProgressService formationProgressService,
            FormationSearchService formationSearchService,
            ResultEvaluationService resultEvaluationService,
            ResultExamenService resultExamenService,
            ContentLockService contentLockService) {
        this.formationService = formationService;
        this.formationProgressService = formationProgressService;
        this.formationSearchService = formationSearchService;
        this.resultEvaluationService = resultEvaluationService;
        this.resultExamenService = resultExamenService;
        this.contentLockService = contentLockService;
    }

    @PostMapping
    public ResponseEntity<Formation> create(@RequestBody Formation formation) {
        Formation saved = formationService.save(formation);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{id}/progress")
    public ResponseEntity<FormationProgressResponse> getProgress(
            @PathVariable("id") Long formationId,
            @RequestParam(required = false) Long userId) {
        Optional<FormationProgressResponse> opt = formationProgressService.getFormationProgress(userId, formationId);
        if (!opt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(opt.get());
    }

    @PostMapping("/{id}/reset-progress")
    public ResponseEntity<Void> resetProgress(
            @PathVariable("id") Long formationId,
            @RequestParam Long userId) {
        if (!formationService.findById(formationId).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        resultEvaluationService.deleteByUserIdAndFormationId(userId, formationId);
        resultExamenService.deleteByUserIdAndFormationId(userId, formationId);
        contentLockService.resetLocksForFormation(formationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Formation> getById(@PathVariable("id") Long id) {
        Optional<Formation> opt = formationService.findById(id);
        if (!opt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(opt.get());
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        boolean usePagination = category != null || level != null || (search != null && !search.isBlank()) || page != null || size != null;
        if (usePagination) {
            int pageNum = page != null ? page : 0;
            int pageSize = size != null ? size : 10;
            Pageable pageable = PageRequest.of(pageNum, pageSize);
            String searchTerm = (search != null && !search.isBlank()) ? search.trim() : null;
            Page<Formation> result = formationSearchService.findAllFiltered("published", category, level, searchTerm, pageable);
            return ResponseEntity.ok(java.util.Map.of(
                    "content", result.getContent(),
                    "totalElements", result.getTotalElements(),
                    "totalPages", result.getTotalPages()
            ));
        }
        return ResponseEntity.ok(formationService.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Formation> update(@PathVariable("id") Long id, @RequestBody Formation formation) {
        Optional<Formation> opt = formationService.findById(id);
        if (!opt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(formationService.update(id, formation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        if (formationService.findById(id).isPresent()) {
            formationService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
