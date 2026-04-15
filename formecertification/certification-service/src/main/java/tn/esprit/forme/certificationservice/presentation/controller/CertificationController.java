package tn.esprit.forme.certificationservice.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.forme.certificationservice.application.dto.certification.CertificationResponse;
import tn.esprit.forme.certificationservice.application.dto.certification.CreateCertificationRequest;
import tn.esprit.forme.certificationservice.application.dto.certification.UpdateCertificationRequest;
import tn.esprit.forme.certificationservice.application.dto.issued.IssuedCertificationResponse;
import tn.esprit.forme.certificationservice.application.service.CertificationIssuanceService;
import tn.esprit.forme.certificationservice.application.service.CertificationCatalogService;

import java.util.List;

@RestController
@RequestMapping("/api/certifications")
@RequiredArgsConstructor

public class CertificationController {

    private final CertificationCatalogService service;
    private final CertificationIssuanceService issuanceService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> create(@Valid @RequestBody CreateCertificationRequest request) {
        try {
            return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(service.create(request));
        } catch (tn.esprit.forme.certificationservice.exception.BusinessException
                 | tn.esprit.forme.certificationservice.exception.NotFoundException
                 | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage() != null ? e.getMessage() : "Failed to create certification"));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        try {
            return ResponseEntity.ok(service.findAll());
        } catch (Exception e) {
            return ResponseEntity.ok(java.util.List.of());
        }
    }

    @GetMapping("/count")
    public ResponseEntity<?> getCount() {
        try {
            return ResponseEntity.ok(java.util.Map.of("count", service.count()));
        } catch (Exception e) {
            return ResponseEntity.ok(java.util.Map.of("count", 0L));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public CertificationResponse update(@PathVariable Long id, @Valid @RequestBody UpdateCertificationRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public CertificationResponse publish(@PathVariable Long id) {
        return service.publish(id);
    }

    @PatchMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public CertificationResponse archive(@PathVariable Long id) {
        return service.archive(id);
    }

    @PostMapping("/issue/{assignmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public IssuedCertificationResponse issue(@PathVariable Long assignmentId) {
        return issuanceService.manualIssue(assignmentId);
    }
}
