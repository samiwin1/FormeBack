package tn.esprit.formation_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.formation_service.dto.GlobalAnalyticsResponse;
import tn.esprit.formation_service.service.StatisticsService;

@RestController
@RequestMapping("/api/formations/admin")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    /**
     * Global training analytics. Admin-only.
     * Statistics aggregate across all formations.
     */
    @GetMapping("/statistics")
    public ResponseEntity<GlobalAnalyticsResponse> getGlobalStatistics() {
        return ResponseEntity.ok(statisticsService.getGlobalAnalytics());
    }

    /**
     * Global analytics with AI-generated insights. Admin-only.
     */
    @GetMapping("/statistics/insights")
    public ResponseEntity<GlobalAnalyticsResponse> getGlobalStatisticsWithInsights() {
        return ResponseEntity.ok(statisticsService.getGlobalAnalyticsWithInsights());
    }
}
