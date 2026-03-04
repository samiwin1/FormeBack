package tn.esprit.forme.certificationservice.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    @PreAuthorize("hasRole('ADMIN')")
    public CertificationResponse create(@Valid @RequestBody CreateCertificationRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<CertificationResponse> getAll() {
        return service.findAll();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CertificationResponse update(@PathVariable Long id, @Valid @RequestBody UpdateCertificationRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public CertificationResponse publish(@PathVariable Long id) {
        return service.publish(id);
    }

    @PatchMapping("/{id}/archive")
    @PreAuthorize("hasRole('ADMIN')")
    public CertificationResponse archive(@PathVariable Long id) {
        return service.archive(id);
    }

    @PostMapping("/issue/{assignmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public IssuedCertificationResponse issue(@PathVariable Long assignmentId) {
        return issuanceService.manualIssue(assignmentId);
    }
}
