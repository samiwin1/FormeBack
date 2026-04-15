package tn.esprit.voucherfraud.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_alerts")
public class FraudAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String voucherCode;
    private Long partnerId;
    private String alertType; // e.g., MULTIPLE_REDEMPTION_ATTEMPT, BRUTE_FORCE
    private Integer severityLevel; // 1 to 5
    private String detectionDetails;
    private LocalDateTime detectedAt;
    
    private String status; // INVESTIGATION_PENDING, CONFIRMED, DISMISSED

    @PrePersist
    public void onPrePersist() {
        this.detectedAt = LocalDateTime.now();
        if (this.status == null) this.status = "INVESTIGATION_PENDING";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getVoucherCode() { return voucherCode; }
    public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }
    public Long getPartnerId() { return partnerId; }
    public void setPartnerId(Long partnerId) { this.partnerId = partnerId; }
    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }
    public Integer getSeverityLevel() { return severityLevel; }
    public void setSeverityLevel(Integer severityLevel) { this.severityLevel = severityLevel; }
    public String getDetectionDetails() { return detectionDetails; }
    public void setDetectionDetails(String detectionDetails) { this.detectionDetails = detectionDetails; }
    public LocalDateTime getDetectedAt() { return detectedAt; }
    public void setDetectedAt(LocalDateTime detectedAt) { this.detectedAt = detectedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}