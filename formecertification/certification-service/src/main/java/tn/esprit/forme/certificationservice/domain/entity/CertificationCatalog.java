package tn.esprit.forme.certificationservice.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;
import tn.esprit.forme.certificationservice.domain.enums.CertificationStatus;


@Entity
@Table(name = "certification_catalog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class CertificationCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String domain;
    private String provider;
    private String level;
    private Integer validityMonths;

    @DecimalMin("0.0")
    @DecimalMax("100.0")
    @Column(nullable = false)
    private Double thresholdFinal;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    @Column(nullable = false)
    private Double weightWritten;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    @Column(nullable = false)
    private Double weightOral;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CertificationStatus status;

    @PrePersist
    @PreUpdate
    void validateWeights() {
        if (weightWritten == null || weightOral == null) {
            throw new IllegalStateException("Weights cannot be null");
        }
        double sum = weightWritten + weightOral;
        if (Math.abs(sum - 1.0) > 0.001) {
            throw new IllegalStateException(
                String.format("Weights must sum to 1.0, but got %.3f (written: %.3f, oral: %.3f)", 
                    sum, weightWritten, weightOral)
            );
        }
    }
}
