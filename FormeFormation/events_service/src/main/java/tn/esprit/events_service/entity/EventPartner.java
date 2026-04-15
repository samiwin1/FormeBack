package tn.esprit.events_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "event_partners",
        uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "partner_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventPartner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    /** Platform user id of the sponsor who purchased the tier (not a separate business partner table). */
    @Column(name = "partner_id", nullable = false)
    private Long partnerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventTier tier;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
