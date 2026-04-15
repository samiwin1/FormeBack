package tn.esprit.forme.certificationservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.forme.certificationservice.domain.enums.RescheduleStatus;

import java.time.LocalDateTime;


@Entity
@Table(name = "reschedule_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class RescheduleRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_reschedule_requests_assignment"))
    private OralExamAssignment assignment;

    @Column(name = "proposed_date", nullable = false)
    private LocalDateTime proposedDatetime;

    @Column(name = "proposed_datetime1", nullable = false)
    private LocalDateTime proposedDatetimeLegacy1;

    @Column(name = "proposed_datetime", nullable = false)
    private LocalDateTime proposedDatetimeLegacy2;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    private LocalDateTime decidedAt;

    @Column(columnDefinition = "TEXT")
    private String adminComment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RescheduleStatus status;

    @PrePersist
    void onCreate() {
        if (requestedAt == null) {
            requestedAt = LocalDateTime.now();
        }
        if (proposedDatetimeLegacy1 == null) {
            proposedDatetimeLegacy1 = proposedDatetime;
        }
        if (proposedDatetimeLegacy2 == null) {
            proposedDatetimeLegacy2 = proposedDatetime;
        }
    }
}
