package tn.esprit.mentorservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
    name = "reminder_log",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "sent_date", "reminder_type"})
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReminderLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "sent_date", nullable = false)
    private LocalDate sentDate;

    @Column(name = "reminder_type", nullable = false, length = 20)
    private String reminderType;   // "STREAK_AT_RISK" or "STREAK_BROKEN"

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
    }
}
