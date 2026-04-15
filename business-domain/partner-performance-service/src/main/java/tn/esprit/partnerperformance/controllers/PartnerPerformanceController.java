package tn.esprit.partnerperformance.controllers;

import org.springframework.web.bind.annotation.*;
import tn.esprit.partnerperformance.dto.LeaderboardRow;
import tn.esprit.partnerperformance.dto.PartnerKpi;
import tn.esprit.partnerperformance.entities.PerformanceAlert;
import tn.esprit.partnerperformance.services.PartnerPerformanceService;

import java.util.List;

@RestController
@RequestMapping("/api/partner-performance/v1")
@CrossOrigin(origins = "http://localhost:4200") // Enable frontend dev requests
public class PartnerPerformanceController {

    private final PartnerPerformanceService performanceService;

    public PartnerPerformanceController(PartnerPerformanceService performanceService) {
        this.performanceService = performanceService;
    }

    // e.g., /api/partner-performance/v1/partners/1/kpis?period=30d
    @GetMapping("/partners/{partnerId}/kpis")
    public PartnerKpi getPartnerKpis(
            @PathVariable Long partnerId,
            @RequestParam(defaultValue = "30d") String period) {
        return performanceService.getPartnerKpi(partnerId, period);
    }

    // e.g., /api/partner-performance/v1/leaderboard?period=30d&metric=redemptionRate&limit=10
    @GetMapping("/leaderboard")
    public List<LeaderboardRow> getLeaderboard(
            @RequestParam(defaultValue = "30d") String period,
            @RequestParam(defaultValue = "redemptionRate") String metric,
            @RequestParam(defaultValue = "10") int limit) {
        return performanceService.getLeaderboard(period, metric, limit);
    }

    // e.g., /api/partner-performance/v1/alerts
    @GetMapping("/alerts")
    public List<PerformanceAlert> getAlerts() {
        return performanceService.getOpenAlerts();
    }

    // e.g., /api/partner-performance/v1/alerts/1/resolve
    @PostMapping("/alerts/{id}/resolve")
    public PerformanceAlert resolveAlert(@PathVariable Long id) {
        return performanceService.resolveAlert(id);
    }
}
