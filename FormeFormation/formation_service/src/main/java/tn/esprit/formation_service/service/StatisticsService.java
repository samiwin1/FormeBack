package tn.esprit.formation_service.service;

import tn.esprit.formation_service.dto.GlobalAnalyticsResponse;

public interface StatisticsService {

    /**
     * Computes global training analytics across all formations.
     * Admin-only.
     */
    GlobalAnalyticsResponse getGlobalAnalytics();

    /**
     * Same as getGlobalAnalytics but includes AI-generated insights.
     * Admin-only.
     */
    GlobalAnalyticsResponse getGlobalAnalyticsWithInsights();
}
