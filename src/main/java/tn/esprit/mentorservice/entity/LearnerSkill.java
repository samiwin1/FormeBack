package tn.esprit.mentorservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tn.esprit.mentorservice.domain.SelfAssessedSkillLevel;

import java.time.Instant;

@Entity
@Table(name = "learner_skill")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearnerSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private LearnerPortfolio portfolio;

    @Column(name = "skill_name", nullable = false, length = 200)
    private String skillName;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "skill_level", nullable = false, length = 32)
    private SelfAssessedSkillLevel skillLevel;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
    }
}
