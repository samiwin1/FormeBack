package tn.esprit.voucherfraud.services;

import org.springframework.stereotype.Service;
import tn.esprit.voucherfraud.entities.FraudAlert;
import tn.esprit.voucherfraud.repositories.FraudAlertRepository;

import java.util.List;

@Service
public class FraudDetectionService {

    private final FraudAlertRepository fraudAlertRepository;

    public FraudDetectionService(FraudAlertRepository fraudAlertRepository) {
        this.fraudAlertRepository = fraudAlertRepository;
    }

    public List<FraudAlert> getAllAlerts() {
        return fraudAlertRepository.findAll();
    }

    public FraudAlert triggerAlert(FraudAlert alert) {
        // Here we could implement heuristic checking logic
        // For simplicity now, we just save the incoming fraud heuristic triggering
        return fraudAlertRepository.save(alert);
    }

    public FraudAlert updateAlertStatus(Long id, String status) {
        FraudAlert alert = fraudAlertRepository.findById(id).orElseThrow(() -> new RuntimeException("Alert not found"));
        alert.setStatus(status);
        return fraudAlertRepository.save(alert);
    }

    public List<FraudAlert> getAlertsByPartner(Long partnerId) {
        return fraudAlertRepository.findByPartnerId(partnerId);
    }
}