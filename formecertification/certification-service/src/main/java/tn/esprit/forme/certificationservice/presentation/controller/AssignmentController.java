package tn.esprit.forme.certificationservice.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.forme.certificationservice.application.dto.assignment.CreateAssignmentRequest;
import tn.esprit.forme.certificationservice.application.dto.assignment.GradeAssignmentRequest;
import tn.esprit.forme.certificationservice.application.dto.assignment.OralAssignmentResponse;
import tn.esprit.forme.certificationservice.application.dto.evaluator.EvaluatorSessionDto;
import tn.esprit.forme.certificationservice.application.dto.evaluator.GradeSubmissionRequest;
import tn.esprit.forme.certificationservice.application.service.EvaluatorSessionService;
import tn.esprit.forme.certificationservice.application.service.OralAssignmentService;
import tn.esprit.forme.certificationservice.security.SecurityUtils;

import java.util.List;

@RestController
@RequiredArgsConstructor

public class AssignmentController {

    private final OralAssignmentService service;
    private final EvaluatorSessionService evaluatorSessionService;

    @PostMapping("/api/oral-assignments")
    @PreAuthorize("hasRole('ADMIN')")
    public OralAssignmentResponse assign(@Valid @RequestBody CreateAssignmentRequest request) {
        return service.assign(request);
    }

    @PostMapping("/api/oral-assignments/{id}/grade")
    @PreAuthorize("hasRole('USER') and @professionGuard.isEvaluator()")
    public OralAssignmentResponse grade(@PathVariable Long id, @Valid @RequestBody GradeAssignmentRequest request) {
        return service.grade(id, request, SecurityUtils.currentUserId());
    }

    @GetMapping("/api/me/oral-assignments")
    @PreAuthorize("hasRole('USER') and @professionGuard.isLearner()")
    public List<OralAssignmentResponse> myAssignments() {
        return service.findByLearner(SecurityUtils.currentUserId());
    }

    @GetMapping("/api/evaluator/oral-assignments")
    @PreAuthorize("hasRole('USER') and @professionGuard.isEvaluator()")
    public List<OralAssignmentResponse> evaluatorAssignments() {
        return service.findByEvaluator(SecurityUtils.currentUserId());
    }

    @PatchMapping("/api/oral-assignments/{id}/no-show")
    @PreAuthorize("hasRole('USER') and @professionGuard.isEvaluator()")
    public void markNoShow(@PathVariable Long id) {
        service.markNoShow(id, SecurityUtils.currentUserId());
    }

    // ========== EVALUATOR ENDPOINTS ==========

    /**
     * Get evaluator's own sessions with learners
     */
    @GetMapping("/api/evaluator/my-sessions")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<EvaluatorSessionDto>> getMySessions() {
        return ResponseEntity.ok(evaluatorSessionService.getMySessionsWithAssignments(SecurityUtils.currentUserId()));
    }

    /**
     * Grade a specific assignment
     */
    @PostMapping("/api/evaluator/assignments/{assignmentId}/grade")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> gradeAssignment(
            @PathVariable Long assignmentId,
            @RequestBody @Valid GradeSubmissionRequest request) {
        evaluatorSessionService.gradeAssignment(SecurityUtils.currentUserId(), assignmentId, request);
        return ResponseEntity.ok().build();
    }

    /**
     * Mark as no-show (evaluator endpoint)
     */
    @PatchMapping("/api/evaluator/assignments/{assignmentId}/no-show")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> markAssignmentNoShow(@PathVariable Long assignmentId) {
        evaluatorSessionService.markNoShow(SecurityUtils.currentUserId(), assignmentId);
        return ResponseEntity.ok().build();
    }
}
