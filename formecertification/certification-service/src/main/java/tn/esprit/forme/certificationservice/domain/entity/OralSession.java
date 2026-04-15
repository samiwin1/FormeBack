package tn.esprit.forme.certificationservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.forme.certificationservice.domain.entity.converter.MeetingProviderConverter;
import tn.esprit.forme.certificationservice.domain.enums.MeetingProvider;
import tn.esprit.forme.certificationservice.domain.enums.OralSessionStatus;

import java.time.LocalDateTime;


@Entity
@Table(name = "oral_sessions", indexes = {
        @Index(name = "idx_oral_sessions_scheduled_at", columnList = "scheduled_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class OralSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "certification_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_oral_sessions_certification"))
    private CertificationCatalog certification;

    @Column(nullable = false)
    private String title;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    private Integer durationMinutes;

    @Column(nullable = false)
    @Convert(converter = MeetingProviderConverter.class)
    private MeetingProvider meetingProvider;

    private String meetingLink;

    @Column(nullable = false)
    private Long evaluatorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OralSessionStatus status;

    private String calendarEventId;
}
