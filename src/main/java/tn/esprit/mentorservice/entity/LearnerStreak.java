package tn.esprit.mentorservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "learner_streak")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LearnerStreak {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "current_streak", nullable = false)
    private int currentStreak;

    @Column(name = "best_streak", nullable = false)
    private int bestStreak;

    @Column(name = "last_active_date")
    private LocalDate lastActiveDate;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        this.updatedAt = Instant.now();
    }
}
