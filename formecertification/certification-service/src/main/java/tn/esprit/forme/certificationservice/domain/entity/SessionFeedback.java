package tn.esprit.forme.certificationservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(
        name = "session_feedback",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_session_feedback_learner_cert",
                        columnNames = {"learner_id", "issued_certification_id"}
                )
        },
        indexes = {
                @Index(name = "idx_session_feedback_session", columnList = "session_id"),
                @Index(name = "idx_session_feedback_learner", columnList = "learner_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class SessionFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "learner_id", nullable = false)
    private Long learnerId;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "issued_certification_id", nullable = false)
    private Long issuedCertificationId;

    @Column(name = "session_rating", nullable = false)
    private int sessionRating;

    @Column(name = "evaluator_rating", nullable = false)
    private int evaluatorRating;

    @Column(length = 500)
    private String comment;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @PrePersist
    void onCreate() {
        if (submittedAt == null) {
            submittedAt = LocalDateTime.now();
        }
    }
}

