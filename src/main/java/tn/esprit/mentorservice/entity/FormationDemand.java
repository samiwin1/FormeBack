package tn.esprit.mentorservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "formation_demand")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormationDemand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "requested_role", length = 100)
    private String requestedRole;

    @Column(name = "detected_skills_gap", columnDefinition = "TEXT")
    private String detectedSkillsGap;

    @Column(nullable = false)
    @Builder.Default
    private boolean fulfilled = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
