package tn.esprit.forme.certificationservice.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.forme.certificationservice.application.dto.dashboard.EvaluatorOverviewDto;
import tn.esprit.forme.certificationservice.application.dto.dashboard.MyCertificationStatusDto;
import tn.esprit.forme.certificationservice.application.dto.dashboard.MyExamStatusDto;
import tn.esprit.forme.certificationservice.application.dto.reschedule.RescheduleResponse;
import tn.esprit.forme.certificationservice.application.service.DashboardAggregationService;
import tn.esprit.forme.certificationservice.security.SecurityUtils;

import java.util.List;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor

public class MeDashboardController {

    private final DashboardAggregationService service;

    @GetMapping("/exam-status")
    @PreAuthorize("hasRole('USER') and @professionGuard.isLearner()")
    public MyExamStatusDto examStatus(@RequestParam(value = "formationId", required = false) Long formationId) {
        return service.getMyExamStatus(SecurityUtils.currentUserId(), formationId);
    }

    @GetMapping("/certification-status")
    @PreAuthorize("hasRole('USER') and @professionGuard.isLearner()")
    public MyCertificationStatusDto certificationStatus(@RequestParam(value = "formationId", required = false) Long formationId) {
        return service.getMyCertificationStatus(SecurityUtils.currentUserId(), formationId);
    }

    @GetMapping("/reschedule")
    @PreAuthorize("hasRole('USER') and @professionGuard.isLearner()")
    public List<RescheduleResponse> myRescheduleRequests() {
        return service.getMyRescheduleRequests(SecurityUtils.currentUserId());
    }

    @GetMapping("/evaluator-overview")
    @PreAuthorize("hasRole('USER') and @professionGuard.isEvaluator()")
    public EvaluatorOverviewDto evaluatorOverview() {
        return service.getEvaluatorOverview(SecurityUtils.currentUserId());
    }
}
