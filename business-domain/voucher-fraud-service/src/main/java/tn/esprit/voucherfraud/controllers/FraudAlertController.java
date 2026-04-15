package tn.esprit.voucherfraud.controllers;

import org.springframework.web.bind.annotation.*;
import tn.esprit.voucherfraud.entities.FraudAlert;
import tn.esprit.voucherfraud.services.FraudDetectionService;

import java.util.List;

@RestController
@RequestMapping("/api/voucher-fraud")
@CrossOrigin(origins = "http://localhost:4200")
public class FraudAlertController {

    private final FraudDetectionService detectionService;

    public FraudAlertController(FraudDetectionService detectionService) {
        this.detectionService = detectionService;
    }

    @GetMapping("/alerts")
    public List<FraudAlert> getAllAlerts() {
        return detectionService.getAllAlerts();
    }

    @GetMapping("/alerts/partner/{partnerId}")
    public List<FraudAlert> getAlertsByPartner(@PathVariable Long partnerId) {
        return detectionService.getAlertsByPartner(partnerId);
    }

    @PostMapping("/detect")
    public FraudAlert reportFraud(@RequestBody FraudAlert alert) {
        return detectionService.triggerAlert(alert);
    }

    @PutMapping("/alerts/{id}/status")
    public FraudAlert updateStatus(@PathVariable Long id, @RequestParam String status) {
        return detectionService.updateAlertStatus(id, status);
    }
}