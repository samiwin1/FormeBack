package tn.esprit.forme.certificationservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.forme.certificationservice.domain.enums.AssignmentStatus;

import java.time.LocalDateTime;


@Entity
@Table(name = "oral_exam_assignments", indexes = {
        @Index(name = "idx_oral_exam_assignments_learner_id", columnList = "learner_id"),
        @Index(name = "idx_oral_exam_assignments_oral_session_id", columnList = "oral_session_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class OralExamAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "oral_session_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_oral_exam_assignments_oral_session"))
    private OralSession oralSession;

    @Column(nullable = false)
    private Long learnerId;

    @Column(nullable = true)
    private Long formationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus status;

    private Double oralScore;

    @Column(columnDefinition = "TEXT")
    private String evaluatorComment;

    private LocalDateTime gradedAt;
    private Integer attemptNumber;
    
    // Assignment-specific scheduled time (overrides session scheduledAt if present)
    @Column(name = "scheduled_at_override")
    private LocalDateTime scheduledAtOverride;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Get the effective scheduled time for this assignment.
     * Returns assignment-specific time if set, otherwise falls back to session time.
     */
    @Transient
    public LocalDateTime getEffectiveScheduledAt() {
        return scheduledAtOverride != null ? scheduledAtOverride : 
               (oralSession != null ? oralSession.getScheduledAt() : null);
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.attemptNumber == null) {
            this.attemptNumber = 1;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
