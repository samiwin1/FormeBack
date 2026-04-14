package tn.esprit.partnerperformance.services;

import org.springframework.stereotype.Service;
import tn.esprit.partnerperformance.dto.LeaderboardRow;
import tn.esprit.partnerperformance.dto.PartnerKpi;
import tn.esprit.partnerperformance.entities.PerformanceAlert;
import tn.esprit.partnerperformance.repositories.PerformanceAlertRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class PartnerPerformanceService {

    private final PerformanceAlertRepository alertRepository;
    private final Random random = new Random();

    public PartnerPerformanceService(PerformanceAlertRepository alertRepository) {
        this.alertRepository = alertRepository;
        seedMockAlerts();
    }

    // Mock data seeding to demo dashboard
    private void seedMockAlerts() {
        if (alertRepository.count() == 0) {
            alertRepository.save(new PerformanceAlert(null, 1L, "LOW_REDEMPTION", "HIGH", "Redemption rate dropped below 5% for the last week.", "OPEN", LocalDateTime.now().minusDays(2), null));
            alertRepository.save(new PerformanceAlert(null, 2L, "INACTIVE_PARTNER", "MEDIUM", "No new codes issued in 30 days.", "OPEN", LocalDateTime.now().minusHours(12), null));
            alertRepository.save(new PerformanceAlert(null, 5L, "SUDDEN_SPIKE", "LOW", "Unusual high volume of code generation in short period.", "OPEN", LocalDateTime.now().minusHours(1), null));
        }
    }

    public PartnerKpi getPartnerKpi(Long partnerId, String period) {
        // Return dummy data reflecting realistic values
        long issued = 1000 + random.nextInt(5000);
        long redeemed = issued / 2 + random.nextInt((int)(issued / 4));
        return new PartnerKpi(
                Math.round(((double) redeemed / issued * 100.0) * 10.0) / 10.0,
                issued,
                redeemed,
                (double) (redeemed * (10 + random.nextInt(10)))
        );
    }

    public List<LeaderboardRow> getLeaderboard(String period, String metric, int limit) {
        List<LeaderboardRow> rows = new ArrayList<>();
        double baseVal = "revenue".equals(metric) ? 10000 : 100;
        for (int i = 1; i <= limit; i++) {
            rows.add(new LeaderboardRow(
                    i,
                    (long) (i * 11),
                    "Partner Tech " + (char)('A' + (i % 26)),
                    Math.round((baseVal - (i * (baseVal/limit*0.8))) * 10.0) / 10.0
            ));
        }
        return rows;
    }

    public List<PerformanceAlert> getOpenAlerts() {
        return alertRepository.findByStatusOrderByCreatedAtDesc("OPEN");
    }

    public PerformanceAlert resolveAlert(Long alertId) {
        PerformanceAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));
        alert.setStatus("RESOLVED");
        alert.setResolvedAt(LocalDateTime.now());
        return alertRepository.save(alert);
    }
}
