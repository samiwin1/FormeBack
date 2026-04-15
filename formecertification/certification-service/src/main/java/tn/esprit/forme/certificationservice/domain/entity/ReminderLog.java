package tn.esprit.forme.certificationservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "reminder_logs",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_reminder_assignment",
                        columnNames = {"assignment_id", "reminder_type"}
                ),
                @UniqueConstraint(
                        name = "uq_reminder_session_learner",
                        columnNames = {"session_id", "learner_id", "stage"}
                )
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ReminderLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // For SessionReminderService (24h reminder)
    @Column(name = "assignment_id")
    private Long assignmentId;

    @Column(name = "reminder_type", length = 50)
    private String reminderType;

    // For SmartReminderService (J-7, J-3, J-1, H-2)
    @Column(name = "session_id")
    private Long sessionId;

    @Column(name = "learner_id")
    private Long learnerId;

    @Column(name = "stage", length = 10)
    private String stage;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @PrePersist
    void onCreate() {
        if (this.sentAt == null) {
            this.sentAt = LocalDateTime.now();
        }
        if (this.reminderType == null && this.assignmentId != null) {
            this.reminderType = "SESSION_24H";
        }
    }
}
