package tn.esprit.forme.certificationservice.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.forme.certificationservice.application.dto.dashboard.EligibleLearnerDto;
import tn.esprit.forme.certificationservice.application.dto.dashboard.FailedOralAttemptDto;
import tn.esprit.forme.certificationservice.application.dto.dashboard.PassedOralWithoutCertificateDto;
import tn.esprit.forme.certificationservice.application.dto.dashboard.PendingOralEvaluationDto;
import tn.esprit.forme.certificationservice.application.dto.dashboard.AdminDashboardStatsDto;
import tn.esprit.forme.certificationservice.application.service.DashboardAggregationService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/oral")
@RequiredArgsConstructor

public class AdminOralDashboardController {

    private final DashboardAggregationService service;

    @GetMapping("/eligible")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public List<EligibleLearnerDto> eligible(@RequestParam("formationId") Long formationId) {
        try {
            return service.getEligibleLearnersForOral(formationId);
        } catch (Exception e) {
            return List.of();
        }
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public List<PendingOralEvaluationDto> pending() {
        try {
            return service.getPendingOralEvaluations();
        } catch (Exception e) {
            return List.of();
        }
    }

    @GetMapping("/passed")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public List<PassedOralWithoutCertificateDto> passedWithoutCertificate() {
        try {
            return service.getPassedOralWithoutCertificate();
        } catch (Exception e) {
            return List.of();
        }
    }

    @GetMapping("/failed")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public List<FailedOralAttemptDto> failedAfterTwoAttempts() {
        try {
            return service.getFailedAfterTwoAttempts();
        } catch (Exception e) {
            return List.of();
        }
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public org.springframework.http.ResponseEntity<?> stats(@RequestParam(value = "formationId", required = false) Long formationId) {
        try {
            return org.springframework.http.ResponseEntity.ok(service.getAdminStats(formationId));
        } catch (Exception e) {
            AdminDashboardStatsDto fallback = new AdminDashboardStatsDto(0L, 0L, 0L, 0L, 0L);
            return org.springframework.http.ResponseEntity.ok(fallback);
        }
    }
}
