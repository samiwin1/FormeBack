package tn.esprit.partnerperformance.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "performance_alerts")
public class PerformanceAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long partnerId;
    private String type; // e.g. "LOW_REDEMPTION", "NO_SALES"
    private String severity; // "HIGH", "MEDIUM", "LOW"
    private String message;
    private String status; // "OPEN", "RESOLVED"
    
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    public PerformanceAlert() {}

    public PerformanceAlert(Long id, Long partnerId, String type, String severity, String message, String status, LocalDateTime createdAt, LocalDateTime resolvedAt) {
        this.id = id;
        this.partnerId = partnerId;
        this.type = type;
        this.severity = severity;
        this.message = message;
        this.status = status;
        this.createdAt = createdAt;
        this.resolvedAt = resolvedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPartnerId() { return partnerId; }
    public void setPartnerId(Long partnerId) { this.partnerId = partnerId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
