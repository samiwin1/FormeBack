package tn.esprit.events_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "event_deposits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDeposit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false, unique = true)
    private EventParticipant participant;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGBLOB")
    private byte[] zipData;

    @Column(nullable = false, length = 512)
    private String zipOriginalFilename;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String readmeContent;

    @Column(nullable = false, length = 512)
    private String readmeOriginalFilename;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant submittedAt;
}
