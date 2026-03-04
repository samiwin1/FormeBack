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
    @PreAuthorize("hasRole('ADMIN')")
    public List<EligibleLearnerDto> eligible(@RequestParam("formationId") Long formationId) {
        return service.getEligibleLearnersForOral(formationId);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public List<PendingOralEvaluationDto> pending() {
        return service.getPendingOralEvaluations();
    }

    @GetMapping("/passed")
    @PreAuthorize("hasRole('ADMIN')")
    public List<PassedOralWithoutCertificateDto> passedWithoutCertificate() {
        return service.getPassedOralWithoutCertificate();
    }

    @GetMapping("/failed")
    @PreAuthorize("hasRole('ADMIN')")
    public List<FailedOralAttemptDto> failedAfterTwoAttempts() {
        return service.getFailedAfterTwoAttempts();
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminDashboardStatsDto stats(@RequestParam(value = "formationId", required = false) Long formationId) {
        return service.getAdminStats(formationId);
    }
}
