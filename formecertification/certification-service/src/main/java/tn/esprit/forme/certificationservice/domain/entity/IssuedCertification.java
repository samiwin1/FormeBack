package tn.esprit.forme.certificationservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.forme.certificationservice.domain.enums.IssuedCertificationStatus;

import java.time.LocalDateTime;


@Entity
@Table(name = "issued_certifications",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_issued_cert_learner_cert_formation",
                        columnNames = {"learner_id", "certification_id", "formation_id"}),
                @UniqueConstraint(name = "uk_issued_cert_certificate_number",
                        columnNames = {"certificate_number"})
        },
        indexes = {
                @Index(name = "idx_issued_certifications_learner_id", columnList = "learner_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class IssuedCertification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long learnerId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "certification_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_issued_certifications_certification"))
    private CertificationCatalog certification;

    @Column(nullable = false)
    private Long formationId;

    @Column(nullable = false)
    private Double writtenScore;
    @Column(nullable = false)
    private Double oralScore;
    @Column(nullable = false)
    private Double finalScore;
    @Column(nullable = false)
    private LocalDateTime issuedAt;
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    @Column(nullable = false, unique = true, length = 32)
    private String certificateNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssuedCertificationStatus status;

    private String pdfPath;
}
