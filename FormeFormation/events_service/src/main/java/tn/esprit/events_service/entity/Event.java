package tn.esprit.events_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String requirements;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String successMetrics;

    @Column(nullable = false)
    private Instant startDate;

    @Column(nullable = false)
    private Instant deadline;

    @Column(nullable = false)
    @Builder.Default
    private int maxVip = 0;

    @Column(nullable = false)
    @Builder.Default
    private int maxGold = 0;

    @Column(nullable = false)
    @Builder.Default
    private int maxSilver = 0;

    /** Sponsor tier prices (major currency units, e.g. TND). 0 = not offered / contact organizer. */
    @Column(nullable = false)
    @Builder.Default
    private int vipPrice = 0;

    @Column(nullable = false)
    @Builder.Default
    private int goldPrice = 0;

    @Column(nullable = false)
    @Builder.Default
    private int silverPrice = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EventPartner> partners = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EventParticipant> participants = new ArrayList<>();
}
