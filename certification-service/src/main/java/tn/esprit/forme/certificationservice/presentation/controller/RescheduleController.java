package tn.esprit.forme.certificationservice.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.forme.certificationservice.application.dto.dashboard.RescheduleAdminItemDto;
import tn.esprit.forme.certificationservice.application.dto.reschedule.CreateRescheduleRequest;
import tn.esprit.forme.certificationservice.application.dto.reschedule.RescheduleResponse;
import tn.esprit.forme.certificationservice.application.service.DashboardAggregationService;
import tn.esprit.forme.certificationservice.application.service.RescheduleService;
import tn.esprit.forme.certificationservice.domain.enums.RescheduleStatus;
import tn.esprit.forme.certificationservice.security.SecurityUtils;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RescheduleController {

    private final RescheduleService service;
    private final DashboardAggregationService dashboardService;

    @PostMapping("/api/oral-assignments/{id}/reschedule")
    @PreAuthorize("hasRole('USER') and @professionGuard.isLearner()")
    public RescheduleResponse create(@PathVariable Long id, @Valid @RequestBody CreateRescheduleRequest request) {
        return service.create(id, request, SecurityUtils.currentUserId());
    }

    @PatchMapping("/api/reschedule/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public RescheduleResponse approve(@PathVariable Long id,
                                      @RequestParam(value = "adminComment", required = false) String adminComment) {
        return service.approve(id, adminComment);
    }

    @PatchMapping("/api/reschedule/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public RescheduleResponse reject(@PathVariable Long id,
                                     @RequestParam(value = "adminComment", required = false) String adminComment,
                                     @RequestParam(value = "replacementSessionId", required = false) Long replacementSessionId) {
        return service.reject(id, adminComment, replacementSessionId);
    }

    @GetMapping("/api/admin/reschedule/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public List<RescheduleAdminItemDto> pending() {
        return dashboardService.getPendingRescheduleRequests();
    }

    @GetMapping("/api/admin/reschedule")
    @PreAuthorize("hasRole('ADMIN')")
    public List<RescheduleAdminItemDto> list(@RequestParam(value = "status", required = false) RescheduleStatus status) {
        return dashboardService.getRescheduleRequests(status);
    }
}
