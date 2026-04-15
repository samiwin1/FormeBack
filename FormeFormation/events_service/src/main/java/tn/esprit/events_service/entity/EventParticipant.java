package tn.esprit.events_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "event_participants",
        uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "user_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** Admin score 0–100; null until graded. */
    @Column
    private Integer score;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant joinedAt;

    @OneToOne(mappedBy = "participant", fetch = FetchType.LAZY)
    private EventDeposit deposit;
}
